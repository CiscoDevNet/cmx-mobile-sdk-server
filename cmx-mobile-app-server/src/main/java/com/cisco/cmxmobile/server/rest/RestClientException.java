package com.cisco.cmxmobile.server.rest;

public class RestClientException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = -1017966250911122067L;

    public RestClientException(Exception exception) {
        super(exception);
    }

    public RestClientException(String msg, Exception exception) {
        super(msg, exception);
    }

    public RestClientException(String msg) {
        super(msg);
    }
}
