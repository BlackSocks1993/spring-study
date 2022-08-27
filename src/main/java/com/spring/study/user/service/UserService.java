package com.spring.study.user.service;

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
	void upgradeLevels();

}
