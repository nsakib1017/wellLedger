package com.subsel.healthledger.exception;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

// TODO: 19/9/20 Delete
public class CustomOAuth2Exception extends OAuth2Exception {
	public CustomOAuth2Exception(String msg, Throwable t) {
		super(msg, t);
	}

	public CustomOAuth2Exception(String msg) {
		super(msg);
	}

	@Override
	public int getHttpErrorCode() {
		return super.getHttpErrorCode();
	}
}
