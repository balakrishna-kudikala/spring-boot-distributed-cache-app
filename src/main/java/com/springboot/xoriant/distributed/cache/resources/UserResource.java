package com.springboot.xoriant.distributed.cache.resources;

import com.springboot.xoriant.distributed.cache.model.User;
import com.springboot.xoriant.distributed.cache.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.JmsException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/user")
public class UserResource {

	private Logger logger = LoggerFactory.getLogger(UserResource.class);

	@Autowired
	private UserService userService;

	@GetMapping("/cache-synch")
	public ResponseEntity<Map> getAllCache() {
		Map<String, String> res = new HashMap<>();
		try{
			Map map = userService.getAllCache();
			return ResponseEntity.ok(map);
		} catch (Throwable t) {
			res.put("status", "failure");
			logger.error("Unknown Error occurred", t);
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	}

	@PostMapping
	public ResponseEntity<Map> addOrUpdateUser(@RequestBody User user) {
		Map<String, String> res = new HashMap<>();
		try{
			logger.info("User Input : "+ user);
			if (user == null || StringUtils.isBlank(user.getUserId())){
				res.put("status", "failure");
				logger.info("Invalid Request");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
			}
			res.put("status", "success");
			userService.addOrUpdateUser(user);
			return ResponseEntity.ok(res);
		} catch (SQLException sqlEx){
			logger.error("SQL Error occurred", sqlEx);
		} catch (Throwable t) {
			logger.error("Unknown Error occurred", t);
		}
		res.put("status", "failure");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	}

	@GetMapping("/all")
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = null;
		try{
			users = userService.getAllUsers();
			return ResponseEntity.ok(users);
		} catch (SQLException sqlEx){
			logger.error("SQL Error occurred", sqlEx);
		} catch (Throwable t) {
			logger.error("Unknown Error occurred", t);
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Arrays.asList());
	}

	@GetMapping
	public ResponseEntity<Object> fetchUser(@RequestParam(name = "userId") String userId) {
		try{
			Optional<User> user = userService.fetchUser(userId);
			if (user != null && user.isPresent()){
				logger.info("User Object : "+ user.get());
				return ResponseEntity.ok(user.get());
			} else {
				logger.error("Invalid UserID entered. Please enter a valid UserID!!");
				return ResponseEntity.ok(Collections.singletonMap("status", "Invalid UserID entered. Please enter a valid UserID!!"));
			}
		} catch (SQLException sqlEx){
			logger.error("SQL Error occurred", sqlEx);
		} catch (Throwable t) {
			logger.error("Unknown Error occurred", t);
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	@PostMapping("/cache-synch")
	public ResponseEntity<Object> updateUserCache(@RequestBody User user) {
		Map<String, String> res = new HashMap<>();
		try{
			userService.updateCache(user);
			res.put("status", "success");
			return ResponseEntity.ok(res);
		} catch (JmsException jmsEx){
			logger.error("JMS Error occurred", jmsEx);
		} catch (Throwable t) {
			logger.error("Unknown Error occurred", t);
		}
		res.put("status", "failure");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	}
}