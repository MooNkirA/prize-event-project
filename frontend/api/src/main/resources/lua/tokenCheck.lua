-- local token 定义变量
-- redis.call('lpop',KEYS[1]) 调用redis 命令，从队列左侧获取一个元素
-- 注意 lua 语法中的数组第一个元素的索引是 1，而不是0
local token = redis.call('lpop',KEYS[1])
-- 获取第二个元素
local curtime = tonumber(KEYS[2])

-- 判断是否有效
if token ~= false then
    -- 数据比对，当前时间与令牌中的时间戳比较
    if ( tonumber(token)/1000 > tonumber(KEYS[2]) ) then
       redis.call('lpush',KEYS[1],token)
       return 1
    else
       -- 成功，将令牌返回
       return tonumber(token)
    end
else
    return 0
end