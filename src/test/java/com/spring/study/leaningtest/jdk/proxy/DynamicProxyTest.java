package com.spring.study.leaningtest.jdk.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

public class DynamicProxyTest {

	@Test
	public void simpleProxy() {
		Hello hello = new HelloTarget();
		assertThat(hello.sayHello("BlackSocks"), is("Hello BlackSocks"));
		assertThat(hello.sayHi("BlackSocks"), is("Hi BlackSocks"));
		assertThat(hello.sayThankYou("BlackSocks"), is("Thank You BlackSocks"));
		
		Hello proxiedHello = (Hello) Proxy.newProxyInstance(
				getClass().getClassLoader(),				// 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로더
				new Class[] {Hello.class},					// 구현할 인터페이스
				new UppercaseHandler(new HelloTarget())		// 부가기능과 위임 코드를 담은 InvocationHandler
		);
		
//		Hello proxiedHello = new HelloUppercase(new HelloTarget());
		assertThat(proxiedHello.sayHello("BlackSocks"), is("HELLO BLACKSOCKS"));
		assertThat(proxiedHello.sayHi("BlackSocks"), is("HI BLACKSOCKS"));
		assertThat(proxiedHello.sayThankYou("BlackSocks"), is("THANK YOU BLACKSOCKS"));
	}

	@Test
	public void proxyFactoryBean() {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		// 타깃 설정
		pfBean.setTarget(new HelloTarget());
		/* 부가기능을 담은 어드바이스를 추가한다.
		여러 개를 추가할 수도 있다. */
		pfBean.addAdvice(new UppercaseAdvice());

		// FactoryBean이므로 getObject()로 생성된 프록시를 가져온다.
		Hello proxiedHello = (Hello) pfBean.getObject();

		assertThat(proxiedHello.sayHello("BlackSocks"), is("HELLO BLACKSOCKS"));
		assertThat(proxiedHello.sayHi("BlackSocks"), is("HI BLACKSOCKS"));
		assertThat(proxiedHello.sayThankYou("BlackSocks"), is("THANK YOU BLACKSOCKS"));
	}

	static class UppercaseAdvice implements MethodInterceptor {
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			/* 리플렉션의 Method와 달리 메소드 실행 시 타깃 오브젝트를 전달할 필요가 없다.
			MethodInvocation은 메소드 정보와 함께 타깃 오브젝트를 알고 있기 때문이다. */
			String ret = (String) invocation.proceed();
			// 부가기능 적용
			return ret.toUpperCase();
		}
	}

	static class HelloUppercase implements Hello {
		Hello hello;
		
		public HelloUppercase(Hello hello) {
			this.hello = hello;
		}

		public String sayHello(String name) {
			return hello.sayHello(name).toUpperCase();
		}

		public String sayHi(String name) {
			return hello.sayHi(name).toUpperCase();
		}

		public String sayThankYou(String name) {
			return hello.sayThankYou(name).toUpperCase();
		}
		
	}

	static class UppercaseHandler implements InvocationHandler {
		Object target;

		private UppercaseHandler(Object target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object ret = method.invoke(target, args);
			if (ret instanceof String && method.getName().startsWith("say")) {
				return ((String)ret).toUpperCase();
			} else {
				return ret;
			}
		}
	}

	static interface Hello {
		String sayHello(String name);
		String sayHi(String name);
		String sayThankYou(String name);
	}

	static class HelloTarget implements Hello {
		public String sayHello(String name) { return "Hello " + name; }
		public String sayHi(String name) { return "Hi " + name; }
		public String sayThankYou(String name) { return "Thank You " + name; }
	}

	@Test
	public void pointcutAdvisor() {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());

		// 메소드 이름을 비교해서 대상을 선정하는 알고리즘을 제공하는 포인트컷 생성.
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		// 이름 비교조건 설정. 
		pointcut.addMethodName("sayH*");

		// 포인트컷과 어드바이스를 Advisor로 묶어서 한 번에 추가.
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

		Hello proxiedHello = (Hello) pfBean.getObject();

		assertThat(proxiedHello.sayHello("BlackSocks"), is("HELLO BLACKSOCKS"));
		assertThat(proxiedHello.sayHi("BlackSocks"), is("HI BLACKSOCKS"));
		assertThat(proxiedHello.sayThankYou("BlackSocks"), is("Thank You BlackSocks"));
	}

}
