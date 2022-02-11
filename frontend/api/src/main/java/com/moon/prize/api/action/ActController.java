package com.moon.prize.api.action;

import com.moon.prize.api.config.LuaScript;
import com.moon.prize.commons.config.RabbitKeys;
import com.moon.prize.commons.config.RedisKeys;
import com.moon.prize.commons.db.entity.CardGame;
import com.moon.prize.commons.db.entity.CardProduct;
import com.moon.prize.commons.db.entity.CardUser;
import com.moon.prize.commons.db.entity.CardUserGame;
import com.moon.prize.commons.db.entity.CardUserHit;
import com.moon.prize.commons.utils.ApiResult;
import com.moon.prize.commons.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/act")
@Api(tags = {"抽奖模块"})
public class ActController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private LuaScript luaScript;

    @GetMapping("/go/{gameid}")
    @ApiOperation(value = "抽奖")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id", example = "1", required = true)
    })
    public ApiResult<Object> act(@PathVariable int gameid, HttpServletRequest request) {
        Date now = new Date();
        // 从 redis 中获取活动基本信息
        CardGame game = (CardGame) redisUtil.get(RedisKeys.INFO + gameid);
        /*
         * 判断活动是否开始
         *  1. 如果活动信息还没加载进 redis，视为无效
         *  2. 如果活动已经加载预热完成，即 redis 中存在，但是开始时间大于当前时间，也视为无效
         * 均返回提示信息
         */
        if (game == null || game.getStarttime().after(now)) {
            return new ApiResult<>(-1, "活动未开始", null);
        }
        // 判断活动是否已结束
        if (now.after(game.getEndtime())) {
            return new ApiResult<>(-1, "活动已结束", null);
        }
        // 获取当前用户信息
        HttpSession session = request.getSession();
        CardUser user = (CardUser) redisUtil.get(RedisKeys.SESSIONID + session.getId());
        // 判断用户是否登陆
        if (user == null) {
            return new ApiResult<>(-1, "未登陆", null);
        }

        // 判断用户是否已经参加过活动。主要是让第一次点击进来的用户，更新数据库表，记录该用户参与过活动
        if (!redisUtil.hasKey(RedisKeys.USERGAME + user.getId() + "_" + gameid)) {
            // 如第一次抽奖，则向 redis 记录当前用户，存储的 key 为 id_gameid
            redisUtil.set(RedisKeys.USERGAME + user.getId() + "_" + gameid, 1, (game.getEndtime().getTime() - now.getTime()) / 1000);
            // 持久化抽奖记录，扔给消息队列处理
            CardUserGame userGame = new CardUserGame();
            userGame.setUserid(user.getId());
            userGame.setGameid(gameid);
            userGame.setCreatetime(new Date());
            // 发送消息队列，异步记录用户参与活动的数据
            rabbitTemplate.convertAndSend(RabbitKeys.QUEUE_PLAY, userGame);
        }

        // 从 redis 中获取用户已中奖次数
        Integer count = (Integer) redisUtil.get(RedisKeys.USERHIT + gameid + "_" + user.getId());
        if (count == null) {
            count = 0;
            // 第一次进来的，初始用户已中奖次数为0
            redisUtil.set(RedisKeys.USERHIT + gameid + "_" + user.getId(), count, (game.getEndtime().getTime() - now.getTime()) / 1000);
        }
        // 根据会员等级，获取本活动允许的最大中奖数
        Integer maxcount = (Integer) redisUtil.hget(RedisKeys.MAXGOAL + gameid, user.getLevel() + "");
        // 如果没设置，默认为0，即：不限制次数
        maxcount = maxcount == null ? 0 : maxcount;
        // 限制中奖次数与用户已中奖次数进行对比
        if (maxcount > 0 && count >= maxcount) {
            // 如果达到最大次数，不允许抽奖
            return new ApiResult<>(-1, "您已达到最大中奖数", null);
        }

        /* ===== 以上校验全部通过，准许进入抽奖逻辑 ===== */

        /* 使用java方式实现从redis中获取令牌，此方式会存在并发问题 */
        // Long token = (Long) redisUtil.leftPop(RedisKeys.TOKENS + gameid);
        // if (token == null) {
        //     // 令牌为空，说明奖品已抽完了
        //     return new ApiResult<>(-1, "奖品已抽光", null);
        // }
        // /*
        //  * 判断令牌时间戳大小，即是否中奖
        //  *  特别注意，取出的令牌要除以1000，都真正的时间戳，参考job项目，令牌生成部分逻辑
        //  */
        // if (now.getTime() < token / 1000) {
        //     // 当前时间小于令牌时间戳，说明奖品未到发放时间点，放回令牌，返回未中奖
        //     redisUtil.leftPush(RedisKeys.TOKENS + gameid, token);
        //     return new ApiResult<>(0, "未中奖", null);
        // }

        /* 代码改造 解决并发问题，使用lua脚本方式来获取令牌，保证操作的原子性 */
        Long token = luaScript.tokenCheck(RedisKeys.TOKENS + gameid, String.valueOf(now.getTime()));
        if (token == 0) {
            return new ApiResult<>(-1, "奖品已抽光", null);
        } else if (token == 1) {
            return new ApiResult<>(0, "未中奖", null);
        }

        // 以上逻辑走完，说明很幸运，中奖了！
        // 根据 活动id+时间戳 从缓存中查询抽中的奖品：
        CardProduct product = (CardProduct) redisUtil.get(RedisKeys.TOKEN + gameid + "_" + token);
        // 更新当前用户的中奖次数，加1
        redisUtil.incr(RedisKeys.USERHIT + gameid + "_" + user.getId(), 1);
        // 投放消息给队列，中奖后的耗时业务，交给消息模块处理
        CardUserHit hit = new CardUserHit();
        hit.setGameid(gameid);
        hit.setHittime(now);
        hit.setProductid(product.getId());
        hit.setUserid(user.getId());
        rabbitTemplate.convertAndSend(RabbitKeys.EXCHANGE_DIRECT, RabbitKeys.QUEUE_HIT, hit);

        // 返回给前台中奖信息
        return new ApiResult<>(1, "恭喜中奖", product);
    }

    @GetMapping("/info/{gameid}")
    @ApiOperation(value = "缓存信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id", example = "1", required = true)
    })
    public ApiResult<Object> info(@PathVariable int gameid) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(RedisKeys.INFO + gameid, redisUtil.get(RedisKeys.INFO + gameid));
        List<Object> tokens = redisUtil.lrange(RedisKeys.TOKENS + gameid, 0, -1);
        Map<String, Object> tokenMap = new LinkedHashMap<>();
        tokens.forEach(o -> tokenMap.put(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(Long.parseLong(o.toString()) / 1000)),
                redisUtil.get(RedisKeys.TOKEN + gameid + "_" + o))
        );
        map.put(RedisKeys.TOKENS + gameid, tokenMap);
        map.put(RedisKeys.MAXGOAL + gameid, redisUtil.hmget(RedisKeys.MAXGOAL + gameid));
        map.put(RedisKeys.MAXENTER + gameid, redisUtil.hmget(RedisKeys.MAXENTER + gameid));
        return new ApiResult<>(200, "缓存信息", map);
    }
}
