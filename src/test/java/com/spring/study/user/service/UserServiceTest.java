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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.spring.study.user.dao.UserDao;
import com.spring.study.user.domain.Level;
import com.spring.study.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml"})
//@Transactional
/* 롤백 여부에 대한 기본 설정과 매니저 빈을 지정하는 데 사용할 수 있다.
 * 디폴트 트랜잭션 매니저 아이디는 관례를 따라서 transactionManager로 되어 있다.
 */
// @TransactionConfiguration(defaultRollback=false)
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
	// 롤백 여부애 대한 기본 설정이 false인 경우 해당 어노테이션을 사용해서 롤백 여부를 재설정 할 수 있다.
	// @Rollback
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
	// @Transactional
	// @Transactional(readOnly=true)
	// @Rollback(false)
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

	// 예외가 발생하지 않는 이유가 뭘까요??
	// 일단 어떤 예외가 발생할지 모르니 expected 없이 테스트를 작성한다. 발생 예외가 파악되면 expected를 추가하여 다시 테스트를 진행한다.
	@Test//(expected = TransientDataAccessResourceException.class)
	public void readOnlyTxAttribute() {
		// 트랜잭션 속성이 제대로 적용되었다면 여기서 읽기전용 속성을 위배했기 때문에 예외가 발생해야 한다.
		this.testUserService.getAll();
	}

	/*
	 * 포인트컷의 클래스 필터에 선정되도록 이름을 변경한다.
	 * 이래서 처음부터 이름을 잘 지어야 한다.
	 */
	static class TestUserService extends UserServiceImpl {
		// 테스트 픽스처의 users(3)의 id 값을 고정시켜버렸다.
		private String id = "user02";

//		private TestUserService(String id) { this.id = id; }

		// 읽기전용 트랜잭션 대상인 get을 오버라이드 한다.
		@Override
		public List<User> getAll() {	
			for (User user : super.getAll()) {
				// 강제로 쓰기 시도를 한다.
				//super.update(user);
				super.update(user);
			}
			// 메소드가 끝나기 전에 예외가 발생해야 하니 리턴 값은 적당히 넣는다.
			return null;
		}

		@Override
		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}

	static class TestUserServiceException extends RuntimeException {}

	@Test
	public void transactionSync() {
		DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
		txDefinition.setReadOnly(true);
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

		userService.deleteAll();

		userService.add(users.get(0));
		userService.add(users.get(1));

		transactionManager.commit(txStatus);
	}

}
