package com.format.base;

public class AppException extends RuntimeException {

	private String msg = "";

	public AppException(String msg) {
		this.msg = msg;
	}

}
