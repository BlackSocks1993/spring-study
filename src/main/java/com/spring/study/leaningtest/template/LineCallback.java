package com.spring.study.leaningtest.template;

public interface LineCallback<T> {
	T doSomethingWithLine(String line, T value);
}