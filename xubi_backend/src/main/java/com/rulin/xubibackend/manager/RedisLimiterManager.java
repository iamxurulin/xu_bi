package com.rulin.xubibackend.manager;

import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Redis限流管理器服务类
 * 使用Redisson实现基于Redis的限流功能
 */
@Service
public class RedisLimiterManager {

    @Resource
    /**
     * Redisson客户端，用于连接和操作Redis服务
     */
    private RedissonClient redissonClient;
    /**
     * 执行限流的方法
     * @param key 限流器的唯一标识键
     */
    public void doRateLimit(String key){
        // 根据key获取Redisson的限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 设置限流器的规则：总体类型，每秒最多2次请求
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);

        // 尝试获取1个许可，返回是否成功获取
        boolean canOp = rateLimiter.tryAcquire(1);

        // 如果无法获取许可，说明请求超限，抛出业务异常
        if(!canOp){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
