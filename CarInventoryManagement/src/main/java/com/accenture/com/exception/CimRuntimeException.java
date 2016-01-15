package com.accenture.com.exception;

public class CimRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5558554306065460451L;

	public CimRuntimeException() {
		super();
	}

	public CimRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CimRuntimeException(String message) {
		super(message);
	}

	public CimRuntimeException(Throwable cause) {
		super(cause);
	}

}
