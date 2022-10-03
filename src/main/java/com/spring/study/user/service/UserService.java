package com.spring.study.user.service;

import java.util.List;

import com.spring.study.user.domain.User;

/**
 * @Since	2022. 8. 27.
 * @Author	Hyeok
 * <PRE>
 * =========================
 * @History
 * Date				Name		DESC
 * 2022. 8. 27.		Hyeok		First written
 * <PRE>
 */
public interface UserService {

	void add(User user);
	User get(String id);
	List<User> getAll();
	void deleteAll();
	void update(User user);

	void upgradeLevels();

}
