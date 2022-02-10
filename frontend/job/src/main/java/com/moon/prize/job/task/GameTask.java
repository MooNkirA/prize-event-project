package com.moon.prize.job.task;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.moon.prize.commons.config.RedisKeys;
import com.moon.prize.commons.db.entity.CardGame;
import com.moon.prize.commons.db.entity.CardGameExample;
import com.moon.prize.commons.db.entity.CardGameProduct;
import com.moon.prize.commons.db.entity.CardGameProductExample;
import com.moon.prize.commons.db.entity.CardGameRules;
import com.moon.prize.commons.db.entity.CardGameRulesExample;
import com.moon.prize.commons.db.entity.CardProduct;
import com.moon.prize.commons.db.entity.CardProductDto;
import com.moon.prize.commons.db.mapper.CardGameMapper;
import com.moon.prize.commons.db.mapper.CardGameProductMapper;
import com.moon.prize.commons.db.mapper.CardGameRulesMapper;
import com.moon.prize.commons.db.mapper.GameLoadMapper;
import com.moon.prize.commons.utils.RedisUtil;
import com.moon.prize.job.annotation.ElasticSimpleJob;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 活动信息预热，每隔1分钟执行一次
 * 查找未来1分钟内（含1分钟），要开始的活动
 */
@Component
@ElasticSimpleJob(cron = "0 * * * * ?")
public class GameTask implements SimpleJob {
    private final static Logger log = LoggerFactory.getLogger(GameTask.class);
    @Autowired
    private CardGameMapper gameMapper;
    @Autowired
    private CardGameProductMapper gameProductMapper;
    @Autowired
    private CardGameRulesMapper gameRulesMapper;
    @Autowired
    private GameLoadMapper gameLoadMapper;
    @Autowired
    private RedisUtil redisUtil;

    /* 定时任务执行的业务逻辑方法 */
    @Override
    public void execute(ShardingContext shardingContext) {
        // 当前时间
        Date now = new Date();
        // 查询将来1分钟内要开始的活动 CardGameExample 是代码生成器生成的查询条件对象
        CardGameExample example = new CardGameExample();
        CardGameExample.Criteria criteria = example.createCriteria();
        // 开始时间大于当前时间
        criteria.andStarttimeGreaterThan(now);
        // 小于等于（当前时间+1分钟）
        criteria.andStarttimeLessThanOrEqualTo(DateUtils.addMinutes(now, 1));
        // 获取到符合要求的活动列表（在1分钟内开始）
        List<CardGame> list = gameMapper.selectByExample(example);
        if (list.size() == 0) {
            // 没有查到要开始的活动
            log.info("game list scan : size = 0");
            return;
        }
        log.info("game list scan : size = {}", list.size());
        // 有相关活动数据，则将活动数据预热，进redis
        list.forEach(game -> {
            // 活动开始时间
            long start = game.getStarttime().getTime();
            // 活动结束时间
            long end = game.getEndtime().getTime();
            // 计算活动结束时间到现在还有多少秒，作为redis key过期时间
            long expire = (end - now.getTime()) / 1000;
            // long expire = -1; //永不过期
            // 活动持续时间（ms）
            long duration = end - start;

            // 设置活动的状态，1-已加载
            game.setStatus(1);
            // 前缀+活动id 作为 key，value 是活动实例
            redisUtil.set(RedisKeys.INFO + game.getId(), game, -1);
            // 记录日志，已加载那些活动
            log.info("load game info:{},{},{},{}", game.getId(), game.getTitle(), game.getStarttime(), game.getEndtime());

            // 活动奖品信息
            List<CardProductDto> products = gameLoadMapper.getByGameId(game.getId());
            Map<Integer, CardProduct> productMap = new HashMap<>(products.size());
            // 建立商品id与商品实例的映射关系
            products.forEach(p -> productMap.put(p.getId(), p));
            log.info("load product type:{}", productMap.size());

            // 奖品数量等配置信息
            CardGameProductExample productExample = new CardGameProductExample();
            productExample.createCriteria().andGameidEqualTo(game.getId());
            // 查询活动与奖品关系表
            List<CardGameProduct> gameProducts = gameProductMapper.selectByExample(productExample);
            log.info("load bind product:{}", gameProducts.size());

            // 令牌桶（队列类型）
            List<Long> tokenList = new ArrayList();
            gameProducts.forEach(cgp -> {
                // 生成 amount 个 start 到 end 之间的随机时间戳做令牌
                for (int i = 0; i < cgp.getAmount(); i++) {
                    long rnd = start + new Random().nextInt((int) duration);
                    /*
                     * 乘1000，再额外加一个随机数，是防止时间段奖品多时重复
                     * 记得取令牌判断时间时，除以1000，还原真正的时间戳
                     */
                    long token = rnd * 1000 + new Random().nextInt(999);
                    // 将令牌放入令牌桶
                    tokenList.add(token);
                    // 以令牌做 key，对应的商品为 value，创建 redis 缓存
                    log.info("token -> game : {} -> {}", token / 1000, productMap.get(cgp.getProductid()).getName());
                    // 活动id+令牌token 作为 key，与实际奖品之间建立映射关系，并设置key在活动结束时过期
                    redisUtil.set(RedisKeys.TOKEN + game.getId() + "_" + token, productMap.get(cgp.getProductid()), expire);
                }
            });
            // 排序后放入 redis 队列
            Collections.sort(tokenList);
            log.info("load tokens:{}", tokenList);

            // 从右侧压入队列，从左到右，时间戳逐个增大
            redisUtil.rightPushAll(RedisKeys.TOKENS + game.getId(), tokenList);
            // 设置令牌桶 key 的过期时间
            redisUtil.expire(RedisKeys.TOKENS + game.getId(), expire);

            // 奖品策略配置信息
            CardGameRulesExample rulesExample = new CardGameRulesExample();
            rulesExample.createCriteria().andGameidEqualTo(game.getId());
            List<CardGameRules> rules = gameRulesMapper.selectByExample(rulesExample);
            // 遍历策略，存入 redis hset
            rules.forEach(r -> {
                redisUtil.hset(RedisKeys.MAXGOAL + game.getId(), r.getUserlevel() + "", r.getGoalTimes());
                redisUtil.hset(RedisKeys.MAXENTER + game.getId(), r.getUserlevel() + "", r.getEnterTimes());
                log.info("load rules:level={},enter={},goal={}", r.getUserlevel(), r.getEnterTimes(), r.getGoalTimes());
            });
            // 设置最大中奖次数与最大抽奖次数的 key 的过期时间
            redisUtil.expire(RedisKeys.MAXGOAL + game.getId(), expire);
            redisUtil.expire(RedisKeys.MAXENTER + game.getId(), expire);

            // 活动状态变更为已预热，禁止管理后台再随便变动
            game.setStatus(1);
            gameMapper.updateByPrimaryKey(game);
        });
    }
}
