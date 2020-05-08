package com.springboot.xoriant.distributed.cache.service;

import com.springboot.xoriant.distributed.cache.model.User;
import com.springboot.xoriant.distributed.cache.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@Service("UserService")
public class UserServiceImpl implements UserService{

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Value("${aq.topic.name}")
	private String topicName;

	@Value("${user.cache.name:user-cache}")
	private String cacheName;

	@CacheEvict(cacheNames = "user-cache", key = "#userId", allEntries = true, condition = "#root.caches[0].getNativeCache().size() > 500", beforeInvocation = true)
	@Cacheable(cacheNames = "user-cache", key = "#userId",condition = "#userId.length() > 0", unless = "#result == null")
	public Optional<User> fetchUser(String userId) throws SQLException {
		logger.info("Executing the fetchUser Method for the userId: "+ userId);
		Optional<User> result = userRepository.findById(userId);
		ConcurrentMap map = (ConcurrentMap)cacheManager.getCache(cacheName).getNativeCache();
		logger.info("Cache Size: "+ map.size()+"\tCache Objects: "+ map);
		return result;
	}

	@CachePut(cacheNames = "user-cache", key = "#user.getUserId()",condition = "#user.getUserId().length() > 0")
	public User addOrUpdateUser(User user) throws SQLException {
		logger.info("Executing the addOrUpdateUser Method for the userId: "+ user.getUserId());
		User result = userRepository.save(user);
		ConcurrentMap map = (ConcurrentMap)cacheManager.getCache(cacheName).getNativeCache();
		logger.info("Cache Size: "+ map.size()+"\tCache Objects: "+ map);
		return result;
	}

	public List<User> getAllUsers() throws SQLException {
		logger.info("Executing the getAllUsers Method");
		List<User> users = userRepository.findAll();
		if (!CollectionUtils.isEmpty(users)){
			logger.info("Users in Database Size: "+ users.size()+"\tUsers : "+ users);
		}
		return users;
	}

	public ConcurrentMap getAllCache() {
		logger.info("Executing the getAllCache Method");
		ConcurrentMap map = (ConcurrentMap)cacheManager.getCache(cacheName).getNativeCache();
		logger.info("Cache Size: "+ map.size()+"\tCache Objects: "+ map);
		return map;
	}

	public String updateCache(User user) throws JmsException {
		Cache cache = cacheManager.getCache(cacheName);
		String status = null;
		logger.info("User Input: "+user+"\nCurrent Users in Cache  : "+ cache.getNativeCache());
		if (user == null || StringUtils.isBlank(user.getUserId())){
			status = "Invalid User!!";
			logger.warn(status);
			return status;
		}
		Object userInCache = cache.get(user.getUserId());
		if (userInCache != null && user.deepEquals(((SimpleValueWrapper)userInCache).get())){
			status = "User unchanged, not going to update the cache ";
			logger.info(status);
		} else {
			logger.info("Publishing the user to user cache topic!!");
			jmsTemplate.convertAndSend(topicName, user);
			status = "success";
			logger.info("Published the user to user cache topic successfully!!");
		}
		return status;
	}
}
