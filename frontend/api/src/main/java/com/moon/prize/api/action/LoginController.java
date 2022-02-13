package com.moon.prize.api.action;

import com.moon.prize.commons.config.RedisKeys;
import com.moon.prize.commons.db.entity.CardUser;
import com.moon.prize.commons.db.entity.CardUserExample;
import com.moon.prize.commons.db.mapper.CardUserMapper;
import com.moon.prize.commons.utils.ApiResult;
import com.moon.prize.commons.utils.PasswordUtil;
import com.moon.prize.commons.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping(value = "/api")
@Api(tags = {"登录模块"})
public class LoginController {
    @Autowired
    private CardUserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/login")
    @ApiOperation(value = "登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "用户名", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true)
    })
    public ApiResult<Object> login(HttpServletRequest request, @RequestParam String account, @RequestParam String password) {
        // 获取用户登陆输入错误的次数，防止用户不断重复登陆
        Integer errortimes = (Integer) redisUtil.get(RedisKeys.USERLOGINTIMES + account);
        if (errortimes != null && errortimes >= 5) {
            return new ApiResult<>(0, "密码错误5次，请5分钟后再登录", null);
        }
        CardUserExample userExample = new CardUserExample();
        userExample.createCriteria().andUnameEqualTo(account).andPasswdEqualTo(PasswordUtil.encodePassword(password));
        List<CardUser> users = userMapper.selectByExample(userExample);
        if (users != null && users.size() > 0) {
            CardUser user = users.get(0);
            // 信息脱敏，不要将敏感信息带入session以免其他接口不小心泄露到前台
            user.setPasswd(null);
            user.setIdcard(null);
            HttpSession session = request.getSession();
            // 将用户id 存入 session 中
            session.setAttribute("loginUserId", user.getId());
            // 在 redis 缓存中保存一份用户的id
            redisUtil.set("loginUser:" + user.getId(), session.getId());
            redisUtil.set(RedisKeys.SESSIONID + session.getId(), user);
            return new ApiResult<>(1, "登录成功", user);
        } else {
            // 记录错误计数，5次后锁定5分钟
            redisUtil.incr(RedisKeys.USERLOGINTIMES + account, 1);
            redisUtil.expire(RedisKeys.USERLOGINTIMES + account, 60 * 5);
            return new ApiResult<>(0, "账户名或密码错误", null);
        }
    }

    @GetMapping("/logout")
    @ApiOperation(value = "退出")
    public ApiResult<Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            // 删除缓存与session
            redisUtil.del("loginUser:" + session.getAttribute("loginUserId"));
            session.invalidate();
        }
        return new ApiResult<>(1, "退出成功", null);
    }

}