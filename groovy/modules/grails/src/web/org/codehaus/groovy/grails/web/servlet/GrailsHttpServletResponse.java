/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.codehaus.groovy.grails.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Wrapper class for HttpServletResponse that allows setting the content type while getting the writer.
 * 
 * @author Steven Devijver
 * @since Jul 5, 2005
 */
public class GrailsHttpServletResponse implements HttpServletResponse {

	private HttpServletResponse delegate = null;
	
	public GrailsHttpServletResponse(HttpServletResponse delegate) {
		super();
		this.delegate = delegate;
	}

	public void addCookie(Cookie cookie) {
		this.delegate.addCookie(cookie);
	}

	public boolean containsHeader(String headerName) {
		return this.delegate.containsHeader(headerName);
	}

	public String encodeURL(String url) {
		return this.delegate.encodeURL(url);
	}

	public String encodeRedirectURL(String url) {
		return this.delegate.encodeURL(url);
	}

	public String encodeUrl(String url) {
		return this.delegate.encodeUrl(url);
	}

	public String encodeRedirectUrl(String url) {
		return this.encodeRedirectUrl(url);
	}

	public void sendError(int error, String message) throws IOException {
		this.delegate.sendError(error, message);
	}

	public void sendError(int error) throws IOException {
		this.delegate.sendError(error);
	}

	public void sendRedirect(String url) throws IOException {
		this.sendRedirect(url);
	}

	public void setDateHeader(String headerName, long value) {
		this.delegate.setDateHeader(headerName, value);
	}

	public void addDateHeader(String headerName, long value) {
		this.delegate.addDateHeader(headerName, value);
	}

	public void setHeader(String headerName, String value) {
		this.delegate.setHeader(headerName, value);
	}

	public void addHeader(String headerName, String value) {
		this.delegate.addHeader(headerName, value);
	}

	public void setIntHeader(String headerName, int value) {
		this.delegate.setIntHeader(headerName, value);
	}

	public void addIntHeader(String headerName, int value) {
		this.delegate.addIntHeader(headerName, value);
	}

	public void setStatus(int status) {
		this.delegate.setStatus(status);
	}

	public void setStatus(int status, String message) {
		this.delegate.setStatus(status, message);
	}

	public String getCharacterEncoding() {
		return this.delegate.getCharacterEncoding();
	}

	public String getContentType() {
		return this.delegate.getContentType();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return this.delegate.getOutputStream() ;
	}

	public ServletOutputStream getOutputStream(String contentType, String characterEncoding) throws IOException {
		this.delegate.setContentType(contentType);
		this.delegate.setCharacterEncoding(characterEncoding);
		return this.delegate.getOutputStream();
	}
	
	public PrintWriter getWriter() throws IOException {
		return this.delegate.getWriter();
	}

	public PrintWriter getWriter(String contentType, String characterEncoding) throws IOException {
		this.delegate.setContentType(contentType);
		this.delegate.setCharacterEncoding(characterEncoding);
		return this.delegate.getWriter();
	}
	
	public void setCharacterEncoding(String characterEncoding) {
		this.delegate.setCharacterEncoding(characterEncoding);
	}

	public void setContentLength(int contentLength) {
		this.delegate.setContentLength(contentLength);
	}

	public void setContentType(String contentType) {
		this.delegate.setContentType(contentType);
	}

	public void setBufferSize(int bufferSize) {
		this.delegate.setBufferSize(bufferSize);
	}

	public int getBufferSize() {
		return this.delegate.getBufferSize();
	}

	public void flushBuffer() throws IOException {
		this.delegate.flushBuffer();
	}

	public void resetBuffer() {
		this.delegate.resetBuffer();
	}

	public boolean isCommitted() {
		return this.delegate.isCommitted();
	}

	public void reset() {
		this.delegate.reset();
	}

	public void setLocale(Locale locale) {
		this.delegate.setLocale(locale);
	}

	public Locale getLocale() {
		return this.delegate.getLocale();
	}

}
