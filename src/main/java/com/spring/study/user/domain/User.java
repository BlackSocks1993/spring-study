package com.spring.study.user.domain;

/**
 * @Since	2022. 8. 26.
 * @Author	Hyeok
 * <PRE>
 * =========================
 * @History
 * Date				Name		DESC
 * 2022. 8. 26.		Hyeok		First written
 * <PRE>
 */
public class User {

	String id;
	String name;
	String password;
	String email;
	Level level;
	int login;
	int recommend;

	public User() {
	}

	public User(String id, String name, String password, String email,
			Level level, int login, int recommend) {
		super();
		this.id = id;
		this.name = name;
		this.password = password;
		this.email = email;
		this.level = level;
		this.login = login;
		this.recommend = recommend;
	}

	/**
	 * @Author	: falle
	 * @Date	: 2022. 8. 26.
	 * @Name	: upgradeLevel
	 * @return	: void
	 * @Desc	: 사용자 레벨을 업그레이드 가능 여부를 검증하고 업그레이드한다.
	 */
	public void upgradeLevel() {
		Level nextLevel = this.level.nextLevel();	
		if (nextLevel == null) throw new IllegalStateException(this.level + "은  업그레이드가 불가능합니다");
		else this.level = nextLevel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public int getLogin() {
		return login;
	}

	public void setLogin(int login) {
		this.login = login;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
