package com.spring.study.user.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxHandler implements InvocationHandler {

	// 부가기능을 제공할 타깃 오브젝트, 어떤 타입의 오브젝트에도 적용 가능하다.
	private Object target;
	// 트랜잭션 기능을 제공하는 데 필요한 트랜잭션 매니저
	private PlatformTransactionManager txManager;
	// 트랜잭션을 적용할 메소드 이름 패턴
	private String pattern;

	public void setTarget(Object target) {
		this.target = target;
	}

	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().startsWith(pattern)) {
			return invokeInTrasaction(method, args);
		} else {
			return method.invoke(method, args);
		}
	}

	private Object invokeInTrasaction(Method method, Object[] args) throws Throwable {
		TransactionStatus status = this.txManager.getTransaction(new DefaultTransactionDefinition());
		try {
			Object ret = method.invoke(target, args);
			this.txManager.commit(status);
			return ret;
		} catch(InvocationTargetException e) {
			this.txManager.rollback(status);
			throw e.getTargetException();
		}
	}

}
