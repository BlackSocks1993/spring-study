package com.spring.study.user.service;

import static com.spring.study.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static com.spring.study.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.spring.study.user.dao.UserDao;
import com.spring.study.user.domain.Level;
import com.spring.study.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml"})
public class UserServiceTest {

	// 팩토리 빈을 가져오려면 애플리케이션 컨텍스트가 필요하다.
	@Autowired ApplicationContext context;
	@Autowired UserService userService;
	@Autowired UserService testUserService;
	@Autowired UserDao userDao;
	@Autowired MailSender mailSender;
	@Autowired PlatformTransactionManager transactionManager;

	// Test fixure
	List<User> users;

	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("admin01", "김광혁", "p1", "admin1@ksug.org", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0),
				new User("admin02", "김정혁", "p2", "admin2@ksug.org", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("user01", "임지선", "p3", "user1@ksug.org", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD-1),
				new User("user02", "임혁준", "p4", "user2@ksug.org", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD),
				new User("user03", "오민규", "p5", "user3@ksug.org", Level.GOLD, 100, 2500)
		);
	}

	@Test 
	public void upgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl(); 

		MockUserDao mockUserDao = new MockUserDao(this.users);  
		userServiceImpl.setUserDao(mockUserDao);

		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);

		userServiceImpl.upgradeLevels();

		List<User> updated = mockUserDao.getUpdated();  
		assertThat(updated.size(), is(2));  
		checkUserAndLevel(updated.get(0), "admin02", Level.SILVER); 
		checkUserAndLevel(updated.get(1), "user02", Level.GOLD);

		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}

	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}

	static class MockUserDao implements UserDao { 
		private List<User> users;  
		private List<User> updated = new ArrayList(); 

		private MockUserDao(List<User> users) {
			this.users = users;
		}

		public List<User> getUpdated() {
			return this.updated;
		}

		public List<User> getAll() {  
			return this.users;
		}

		public void update(User user) {  
			updated.add(user);
		}

		public void add(User user) { throw new UnsupportedOperationException(); }
		public void deleteAll() { throw new UnsupportedOperationException(); }
		public User get(String id) { throw new UnsupportedOperationException(); }
		public int getCount() { throw new UnsupportedOperationException(); }
	}

	static class MockMailSender implements MailSender {
		private List<String> requests = new ArrayList<String>();	
		
		public List<String> getRequests() {
			return requests;
		}

		@Override
		public void send(SimpleMailMessage mailMessage) throws MailException {
			requests.add(mailMessage.getTo()[0]);  
		}

		@Override
		public void send(SimpleMailMessage[] mailMessage) throws MailException {
		}
	}

	@Test
	public void mockUpgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();

		UserDao mockUserDao = mock(UserDao.class);	    
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);

		MailSender mockMailSender = mock(MailSender.class);  
		userServiceImpl.setMailSender(mockMailSender);

		userServiceImpl.upgradeLevels();

//		verify(mockUserDao, times(2)).update(any(User.class));				  
//		verify(mockUserDao, times(2)).update(any(User.class));
//		verify(mockUserDao).update(users.get(1));
//		assertThat(users.get(1).getLevel(), is(Level.SILVER));
//		verify(mockUserDao).update(users.get(3));
//		assertThat(users.get(3).getLevel(), is(Level.GOLD));
//
//		ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);  
//		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
//		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
//		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
//		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
	}	

	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		}
		else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}

	@Test 
	public void add() {
		userDao.deleteAll();

		// GOLD 레벨
		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);

		userService.add(userWithLevel);
		userService.add(userWithoutLevel);

		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel())); 
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
	}

		  // 다이내믹 프록시 팩토리 빈을 직접 만들어 사용할 때는 없앴다가 다시 등장한 컨텍스트 무효화 애노테이션
	@Test // @DirtiesContext
	public void upgradeAllOrNothing() throws Exception {
//		TestUserService testUserService = new TestUserService(users.get(3).getId());
//		TestUserServiceImpl testUserService = new TestUserServiceImpl();
//		testUserService.setUserDao(userDao);
//		testUserService.setMailSender(mailSender);

		/*
		 * 팩토리 빈 자체를 가져와야 하므로 빈 이름에 &를 반드시 넣어야 한다.
		 * 테스트용 타깃 주입.
		 */
//		ProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", ProxyFactoryBean.class);
//		txProxyFactoryBean.setTarget(testUserService);
		// 변경된 타깃 설정을 이용해서 트랜잭션 다이내믹 프록시 오브젝트를 다시 생성한다.
//		UserService txUserService = (UserService) txProxyFactoryBean.getObject();

		userDao.deleteAll();

		for(User user : users) userDao.add(user);

		try {
			this.testUserService.upgradeLevels();
			fail("TestUserServiceException expected"); 
		}
		catch(TestUserServiceException e) {}

		checkLevelUpgraded(users.get(1), false);
	}

	/*
	 * 포인트컷의 클래스 필터에 선정되도록 이름을 변경한다.
	 * 이래서 처음부터 이름을 잘 지어야 한다.
	 */
	static class TestUserServiceImpl extends UserServiceImpl {
		// 테스트 픽스처의 users(3)의 id 값을 고정시켜버렸다.
		private String id = "user02";
		
//		private TestUserService(String id) { this.id = id; }

		@Override
		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id)) throw new TestUserServiceException();  
			super.upgradeLevel(user);
		}
	}

	static class TestUserServiceException extends RuntimeException {}

}
