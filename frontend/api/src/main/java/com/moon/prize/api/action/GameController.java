package com.moon.prize.api.action;

import com.github.pagehelper.PageHelper;
import com.moon.prize.commons.db.entity.CardGame;
import com.moon.prize.commons.db.entity.CardGameExample;
import com.moon.prize.commons.db.entity.CardProductDto;
import com.moon.prize.commons.db.entity.ViewCardUserHit;
import com.moon.prize.commons.db.entity.ViewCardUserHitExample;
import com.moon.prize.commons.db.mapper.CardGameMapper;
import com.moon.prize.commons.db.mapper.GameLoadMapper;
import com.moon.prize.commons.db.mapper.ViewCardUserHitMapper;
import com.moon.prize.commons.utils.ApiResult;
import com.moon.prize.commons.utils.PageBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@RestController
@RequestMapping(value = "/api/game")
@Api(tags = {"活动模块"})
public class GameController {
    @Autowired
    private GameLoadMapper loadMapper;
    @Autowired
    private CardGameMapper gameMapper;
    @Autowired
    private ViewCardUserHitMapper hitMapper;

    private Map<Integer, BiConsumer<CardGameExample.Criteria, Date>> criteriaMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCriteriaMap() {
        // 查全部
        criteriaMap.put(-1, (c, now) -> {
        });
        // 未开始
        criteriaMap.put(0, (c, now) -> c.andStarttimeGreaterThan(now));
        // 进行中
        criteriaMap.put(1, (c, now) -> c.andStarttimeLessThanOrEqualTo(now).andEndtimeGreaterThan(now));
        // 已结束
        criteriaMap.put(2, (c, now) -> c.andEndtimeLessThanOrEqualTo(now));
    }

    @GetMapping("/list/{status}/{curpage}/{limit}")
    @ApiOperation(value = "活动列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "活动状态（-1=全部，0=未开始，1=进行中，2=已结束）", example = "-1", required = true),
            @ApiImplicitParam(name = "curpage", value = "第几页", defaultValue = "1", dataType = "int", example = "1", required = true),
            @ApiImplicitParam(name = "limit", value = "每页条数", defaultValue = "10", dataType = "int", example = "3", required = true)
    })
    public ApiResult<Object> list(@PathVariable int status, @PathVariable int curpage, @PathVariable int limit) {
        Date now = new Date();
        CardGameExample example = new CardGameExample();
        CardGameExample.Criteria c = example.createCriteria();
        /*switch (status) {
            case -1:
                // 查全部
                break;
            case 0:
                // 未开始
                c.andStarttimeGreaterThan(now);
                break;
            case 1:
                // 进行中
                c.andStarttimeLessThanOrEqualTo(now).andEndtimeGreaterThan(now);
                break;
            case 2:
                // 已结束
                c.andEndtimeLessThanOrEqualTo(now);
                break;
        }*/

        // 使用策略模式改造 switch
        criteriaMap.get(status).accept(c, now);

        long total = gameMapper.countByExample(example);
        example.setOrderByClause("starttime desc");
        PageHelper.startPage(curpage, limit);
        return new ApiResult<>(1, "成功", new PageBean<>(curpage, limit, total, gameMapper.selectByExample(example)));
    }

    @GetMapping("/info/{gameid}")
    @ApiOperation(value = "活动信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id", example = "1", required = true)
    })
    public ApiResult<CardGame> info(@PathVariable int gameid) {
        return new ApiResult<>(1, "成功", gameMapper.selectByPrimaryKey(gameid));
    }

    @GetMapping("/products/{gameid}")
    @ApiOperation(value = "奖品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id", example = "1", required = true)
    })
    public ApiResult<List<CardProductDto>> products(@PathVariable int gameid) {
        return new ApiResult<>(1, "成功", loadMapper.getByGameId(gameid));
    }

    @GetMapping("/hit/{gameid}/{curpage}/{limit}")
    @ApiOperation(value = "中奖列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id", dataType = "int", example = "1", required = true),
            @ApiImplicitParam(name = "curpage", value = "第几页", defaultValue = "1", dataType = "int", example = "1", required = true),
            @ApiImplicitParam(name = "limit", value = "每页条数", defaultValue = "10", dataType = "int", example = "3", required = true)
    })
    public ApiResult<PageBean<ViewCardUserHit>> hit(@PathVariable int gameid, @PathVariable int curpage, @PathVariable int limit) {
        ViewCardUserHitExample example = new ViewCardUserHitExample();
        example.createCriteria().andGameidEqualTo(gameid);
        long total = hitMapper.countByExample(example);
        PageHelper.startPage(curpage, limit);
        List<ViewCardUserHit> all = hitMapper.selectByExample(example);
        return new ApiResult<>(1, "成功", new PageBean<>(curpage, limit, total, all));
    }


}