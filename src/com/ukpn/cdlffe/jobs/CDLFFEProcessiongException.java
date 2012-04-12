package com.ukpn.cdlffe.jobs;

public class CDLFFEProcessiongException extends Exception{
	
	/**
	   * serialVersionUID
	   */
	  private static final long serialVersionUID = 1L;

	  /**
	   * OrderProcessingException Constructor.
	   */
	  public CDLFFEProcessiongException() {
	  }

	  /**
	   * OrderProcessingException Constructor.
	   */
	  public CDLFFEProcessiongException(String message) {
	    super(message);
	  }

	  /**
	   * OrderProcessingException Constructor.
	   */
	  public CDLFFEProcessiongException(Throwable cause) {
	    super(cause);
	  }

	  /**
	   * OrderProcessingException Constructor.
	   */
	  public CDLFFEProcessiongException(String message, Throwable cause) {
	    super(message, cause);
	  }


}
