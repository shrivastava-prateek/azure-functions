package com.debugchaos.azure_functions.exception;

public class AuthServiceCallFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthServiceCallFailedException(String message) {
		super(message);
	}

}
