package com.springboot.xoriant.distributed.cache.repository;

import com.springboot.xoriant.distributed.cache.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
