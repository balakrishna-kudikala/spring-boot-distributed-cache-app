package com.springboot.xoriant.distributed.cache.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown=true)
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "role")
	private String role;

	public User() {
	}

	public User(String userId, String userName, String role) {
		this.userId=userId;
		this.userName = userName;
		this.role = role;
	}

	@Override
	public String toString() {
		return "User{" + "userId='" + userId + '\'' + ", userName='" + userName + '\'' + ", role='" + role + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;
		User user = (User) o;
		return getUserId().equals(user.getUserId());
	}

	public boolean deepEquals(Object other){
		if (other == null || !(other instanceof User)){
			return false;
		}
		User otherUser = (User)other;
		return this.userId.equals(otherUser.getUserId()) && this.userName.equals(otherUser.getUserName()) && this.role.equals(otherUser.getRole());
	}
	@Override
	public int hashCode() {
		return Objects.hash(getUserId());
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
