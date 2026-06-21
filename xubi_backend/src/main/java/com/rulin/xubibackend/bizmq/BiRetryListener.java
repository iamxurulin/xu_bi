package com.rulin.xubibackend.bizmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.listener.RetryListenerSupport;

/**
 * RabbitMQ 重试监听器，记录每次重试的异常信息
 */
@Slf4j
public class BiRetryListener extends RetryListenerSupport {

    @Override
    public <T, E extends Throwable> void onError(org.springframework.retry.RetryContext context,
                                                  RetryCallback<T, E> callback,
                                                  Throwable exception) {
        log.warn("RabbitMQ retry attempt error: {}", exception.getMessage());
    }
}
