package com.springboot.xoriant.distributed.cache.service;

import com.springboot.xoriant.distributed.cache.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    public Optional<User> fetchUser(String userId) throws SQLException;
    public User addOrUpdateUser(User user) throws SQLException;
    public List<User> getAllUsers() throws SQLException;
    public Map getAllCache();
    public String updateCache(User user);
}
