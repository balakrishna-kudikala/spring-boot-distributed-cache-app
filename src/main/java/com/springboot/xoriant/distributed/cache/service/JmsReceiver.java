package com.springboot.xoriant.distributed.cache.service;

import com.springboot.xoriant.distributed.cache.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class JmsReceiver {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private CacheManager cacheManager;

    @Value("${user.cache.name:user-cache}")
    private String cacheName;

    @JmsListener(destination = "${aq.topic.name}", containerFactory = "topicListenerFactory")
    public void receiveTopicMessage(User user) {
        logger.info("Received User from user cache topic: " + user);
        Cache cache = cacheManager.getCache(cacheName);
        logger.info("Users in cache before updating the cache: "+ cache.getNativeCache());
        cache.put(user.getUserId(), user);
        logger.info("Users in cache after updating the cache: "+ cache.getNativeCache());
    }
}
