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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>Wrapper for HttpServletRequest instance that also implements
 * java.util.Map. Read-only map methods are delegated to the getParameterMap method.
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 * @see javax.servlet.http.HttpServletRequest
 * @see java.util.Map
 */
public class GrailsHttpServletRequest implements HttpServletRequest, Map {

	private HttpServletRequest delegate = null;
	
	public GrailsHttpServletRequest(HttpServletRequest delegate) {
		super();
		if (delegate == null) {
			throw new IllegalArgumentException("Delegate should not be null!");
		}
		this.delegate = delegate;
	}

	public String getAuthType() {
		return this.delegate.getAuthType();
	}

	public Cookie[] getCookies() {
		return this.delegate.getCookies();
	}

	public long getDateHeader(String headerName) {
		return this.delegate.getDateHeader(headerName);
	}

	public String getHeader(String headerName) {
		return this.delegate.getHeader(headerName);
	}

	public Enumeration getHeaders(String headerName) {
		return this.delegate.getHeaders(headerName);
	}

	public Enumeration getHeaderNames() {
		return this.delegate.getHeaderNames();
	}

	public int getIntHeader(String headerName) {
		return this.delegate.getIntHeader(headerName);
	}

	public String getMethod() {
		return this.delegate.getMethod();
	}

	public String getPathInfo() {
		return this.delegate.getPathInfo();
	}

	public String getPathTranslated() {
		return this.delegate.getPathTranslated();
	}

	public String getContextPath() {
		return this.delegate.getContextPath();
	}

	public String getQueryString() {
		return this.delegate.getQueryString();
	}

	public String getRemoteUser() {
		return this.delegate.getRemoteUser();
	}

	public boolean isUserInRole(String role) {
		return this.delegate.isUserInRole(role);
	}

	public Principal getUserPrincipal() {
		return this.delegate.getUserPrincipal();
	}

	public String getRequestedSessionId() {
		return this.delegate.getRequestedSessionId();
	}

	public String getRequestURI() {
		return this.delegate.getRequestURI();
	}

	public StringBuffer getRequestURL() {
		return this.delegate.getRequestURL();
	}

	public String getServletPath() {
		return this.delegate.getServletPath();
	}

	public HttpSession getSession(boolean arg0) {
		return this.delegate.getSession(arg0);
	}

	public HttpSession getSession() {
		return this.delegate.getSession();
	}

	public boolean isRequestedSessionIdValid() {
		return this.delegate.isRequestedSessionIdValid();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return this.delegate.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return this.delegate.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return this.delegate.isRequestedSessionIdFromUrl();
	}

	public int size() {
		return this.delegate.getParameterMap().size();
	}

	public boolean isEmpty() {
		return this.delegate.getParameterMap().isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.delegate.getParameterMap().containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.delegate.getParameterMap().containsValue(value);
	}

	public Object get(Object key) {
		return this.delegate.getParameterMap().get(key);
	}

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map arg0) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Set keySet() {
		return this.delegate.getParameterMap().keySet();
	}

	public Collection values() {
		return this.delegate.getParameterMap().values();
	}

	public Set entrySet() {
		return this.delegate.getParameterMap().entrySet();
	}

	public Object getAttribute(String attributeName) {
		return this.delegate.getAttribute(attributeName);
	}

	public Enumeration getAttributeNames() {
		return this.delegate.getAttributeNames();
	}

	public String getCharacterEncoding() {
		return this.delegate.getCharacterEncoding();
	}

	public void setCharacterEncoding(String characterEncoding)
			throws UnsupportedEncodingException {
		this.delegate.setCharacterEncoding(characterEncoding);
	}

	public int getContentLength() {
		return this.delegate.getContentLength();
	}

	public String getContentType() {
		return this.delegate.getContentType();
	}

	public ServletInputStream getInputStream() throws IOException {
		return this.delegate.getInputStream();
	}

	public String getParameter(String parameterName) {
		return this.delegate.getParameter(parameterName);
	}
	
	public Enumeration getParameterNames() {
		return this.delegate.getParameterNames();
	}

	public String[] getParameterValues(String parameterName) {
		return this.delegate.getParameterValues(parameterName);
	}

	public Map getParameterMap() {
		return this.delegate.getParameterMap();
	}

	public String getProtocol() {
		return this.delegate.getProtocol();
	}

	public String getScheme() {
		return this.delegate.getScheme();
	}

	public String getServerName() {
		return this.delegate.getServerName();
	}

	public int getServerPort() {
		return this.delegate.getServerPort();
	}

	public BufferedReader getReader() throws IOException {
		return this.delegate.getReader();
	}

	public String getRemoteAddr() {
		return this.delegate.getRemoteAddr();
	}

	public String getRemoteHost() {
		return this.delegate.getRemoteHost();
	}

	public void setAttribute(String key, Object value) {
		this.delegate.setAttribute(key, value);
	}

	public void removeAttribute(String attributeName) {
		this.delegate.removeAttribute(attributeName);
	}

	public Locale getLocale() {
		return this.delegate.getLocale();
	}

	public Enumeration getLocales() {
		return this.delegate.getLocales();
	}

	public boolean isSecure() {
		return this.delegate.isSecure();
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return this.delegate.getRequestDispatcher(arg0);
	}

	public String getRealPath(String arg0) {
		return this.delegate.getRealPath(arg0);
	}

	public int getRemotePort() {
		return this.delegate.getRemotePort();
	}

	public String getLocalName() {
		return this.delegate.getLocalName();
	}

	public String getLocalAddr() {
		return this.delegate.getLocalAddr();
	}

	public int getLocalPort() {
		return this.delegate.getLocalPort();
	}

}
