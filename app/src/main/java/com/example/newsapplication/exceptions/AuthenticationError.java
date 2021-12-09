package com.example.newsapplication.exceptions;

public class AuthenticationError extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2703194338913940279L;

	public AuthenticationError(String message){
		super(message);
	}
}
