package com.subsel.healthledger.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.DefaultThrowableAnalyzer;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.io.IOException;

@Component
public class CustomOAuth2WebResponseExceptionTranslator implements WebResponseExceptionTranslator {

	private final Logger log = LoggerFactory.getLogger(CustomOAuth2WebResponseExceptionTranslator.class);

	private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

	@Override
	public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
		Throwable[] causeChain = this.throwableAnalyzer.determineCauseChain(e);
		Exception ase;
		//Authentication related exception
		ase = (UsernameNotFoundException)this.throwableAnalyzer.getFirstThrowableOfType(UsernameNotFoundException.class, causeChain);
		if (ase != null){
			log.error("Username not found: {}", ase.getMessage());
			return this.handleOAuth2Exception(new UnauthorizedException(e.getMessage(), e));
		}
		ase = (AuthenticationException)this.throwableAnalyzer.getFirstThrowableOfType(AuthenticationException.class, causeChain);
		if (ase != null) {
			log.error("AuthenticationException: {}", ase.getMessage());
			return this.handleOAuth2Exception(new UnauthorizedException(e.getMessage(), e));
		}
		//OAuth2Exception exception in exception chain
		ase = (OAuth2Exception)this.throwableAnalyzer.getFirstThrowableOfType(OAuth2Exception.class, causeChain);
		if (ase != null) {
			log.error("OAuth2Exception: {}", ase.getMessage());
			return this.handleOAuth2Exception((OAuth2Exception)ase);
		}
		//Exception chain contains access denied exception
		ase = (AccessDeniedException)this.throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class, causeChain);
		if (ase instanceof AccessDeniedException) {
			log.error("AccessDeniedException: {}", ase.getMessage());
			return this.handleOAuth2Exception(new ForbiddenException(ase.getMessage(), ase));
		}
		//Exception chain contains Http method request exception
		ase = (HttpRequestMethodNotSupportedException)this.throwableAnalyzer.getFirstThrowableOfType(HttpRequestMethodNotSupportedException.class, causeChain);
		if(ase instanceof HttpRequestMethodNotSupportedException){
			log.error("HttpRequestMethodNotSupportedException: {}", ase.getMessage());
			return this.handleOAuth2Exception(new MethodNotAllowed(ase.getMessage(), ase));
		}
		return this.handleOAuth2Exception(new ServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e));
	}

	private ResponseEntity<OAuth2Exception> handleOAuth2Exception(OAuth2Exception e) throws IOException {
		int status = e.getHttpErrorCode();
//		int status = HttpStatus.UNAUTHORIZED.value();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cache-Control", "no-store");
		headers.set("Pragma", "no-cache");
		if (status == HttpStatus.UNAUTHORIZED.value() || e instanceof InsufficientScopeException) {
			headers.set("WWW-Authenticate", String.format("%s %s", "Bearer", e.getSummary()));
		}
//		CustomOAuth2Exception exception = new CustomOAuth2Exception(e.getMessage(),e);
		ResponseEntity<OAuth2Exception> response = new ResponseEntity(e, headers, HttpStatus.valueOf(status));
		return response;
	}

	private static class MethodNotAllowed extends OAuth2Exception {
		public MethodNotAllowed(String msg, Throwable t) {
			super(msg, t);
		}
		@Override
		public String getOAuth2ErrorCode() {
			return "method_not_allowed";
		}
		@Override
		public int getHttpErrorCode() {
			return 405;
		}
	}

	private static class UnauthorizedException extends OAuth2Exception {
		public UnauthorizedException(String msg, Throwable t) {
			super(msg, t);
		}
		@Override
		public String getOAuth2ErrorCode() {
			return "unauthorized";
		}
		@Override
		public int getHttpErrorCode() {
			return 401;
		}
	}

	private static class ServerErrorException extends OAuth2Exception {
		public ServerErrorException(String msg, Throwable t) {
			super(msg, t);
		}
		@Override
		public String getOAuth2ErrorCode() {
			return "server_error";
		}
		@Override
		public int getHttpErrorCode() {
			return 500;
		}
	}

	private static class ForbiddenException extends OAuth2Exception {
		public ForbiddenException(String msg, Throwable t) {
			super(msg, t);
		}
		@Override
		public String getOAuth2ErrorCode() {
			return "access_denied";
		}
		@Override
		public int getHttpErrorCode() {
			return 403;
		}
	}
}
