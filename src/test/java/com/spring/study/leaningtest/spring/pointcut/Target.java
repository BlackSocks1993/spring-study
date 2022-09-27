package com.spring.study.leaningtest.spring.pointcut;

/**
 * @Since	2022. 9. 18.
 * @Author	Hyeok
 * @Desc	포인트컷 표현식 학습 테스트
 * execution([접근제한자 패턴] 리턴 갑 타입패턴 [패키지와 클래스 타입패턴.]이름패턴 (파리미터 타입패턴 | "..", ...) [throws 예외 패턴])
 * Target Class의 minus() 메소드 풀 시그니처 :
 * public int com.spring.study.leaningtest.spring.pointcut.Target.minus(int,int) throws java.lang.RuntimeException
 * <PRE>
 * =========================
 * @History
 * Date				Name		DESC
 * 2022. 9. 18.		Hyeok		First written
 * <PRE>
 */
public class Target implements TargetInterface {

	@Override
	public void hello() {}

	@Override
	public void hello(String a) {}

	@Override
	public int minus(int a, int b) throws RuntimeException { return 0; }

	@Override
	public int plus(int a, int b) { return 0; }

	public void method() {}

}
