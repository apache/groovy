/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.servlet;

import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.GroovyBugError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Servlet-specific binding extension to lazy load the writer or the output
 * stream from the response.
 * <p/>
 * <p>
 * <h3>Eager variables</h3>
 * <ul>
 * <li><tt>"request"</tt> : the HttpServletRequest object</li>
 * <li><tt>"response"</tt> : the HttpServletRequest object</li>
 * <li><tt>"context"</tt> : the ServletContext object</li>
 * <li><tt>"application"</tt> : same as context</li>
 * <li><tt>"session"</tt> : shorthand for <code>request.getSession(<tt>false</tt>)</code> - can be null!</li>
 * <li><tt>"params"</tt> : map of all form parameters - can be empty</li>
 * <li><tt>"headers"</tt> : map of all <tt>request</tt> header fields</li>
 * </ul>
 * <p/>
 * <p>
 * <h3>Lazy variables</h3>
 * <ul>
 * <li><tt>"out"</tt> : response.getWriter()</li>
 * <li><tt>"sout"</tt> : response.getOutputStream()</li>
 * <li><tt>"html"</tt> : new MarkupBuilder(response.getWriter())</li>
 * </ul>
 * As per specification a call to response.getWriter() should not be done if
 * a call to response.getOutputStream() have been done already and the other way
 * around. Lazy bound variables can be requested without side effects, since the 
 * writer and stream is wrapped. That means response.getWriter() is not directly 
 * called if 'out' or 'html' is requested. Only if a write method call is done
 * using the variable, a write method call on 'sout' will cause a IllegalStateException.
 * If a write method call on 'sout' has been done already any further write method call
 * on 'out' or 'html' will cause a IllegalStateException. 
 * </p><p>
 * If response.getWriter() is called directly (without using out), then a write method 
 * call on 'sout' will not cause the IllegalStateException, but it will still be invalid. 
 * It is the responsibility of the user of this class, to not to mix these different usage
 * styles. The same applies to calling response.getOoutputStream() and using 'out' or 'html'.
 * </p>
 *
 * @author Guillaume Laforge
 * @author Christian Stein
 * @author Jochen Theodorou
 */
public class ServletBinding extends Binding {
    
    /**
     * A OutputStream dummy that will throw a GroovyBugError for any
     * write method call to it. 
     * 
     * @author Jochen Theodorou
     */
    private static class InvalidOutputStream extends OutputStream {
        /**
         * Will always throw a GroovyBugError
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b) {
            throw new GroovyBugError("Any write calls to this stream are invalid!");
        }
    }
    /**
     * A class to manage the response output stream and writer.
     * If the stream have been 'used', then using the writer will cause
     * a IllegalStateException. If the writer have been 'used', then 
     * using the stream will cause a IllegalStateException. 'used' means
     * any write method has been called. Simply requesting the objects will
     * not cause an exception. 
     * 
     * @author Jochen Theodorou
     */
    private static class ServletOutput {
        private HttpServletResponse response;
        private ServletOutputStream outputStream;
        private PrintWriter writer;
        
        public ServletOutput(HttpServletResponse response) {
            this.response = response;
        }
        private ServletOutputStream getResponseStream() throws IOException {
            if (writer!=null) throw new IllegalStateException("The variable 'out' or 'html' have been used already. Use either out/html or sout, not both.");
            if (outputStream==null) outputStream = response.getOutputStream();
            return outputStream;
        }
        public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                public void write(int b) throws IOException {
                    getResponseStream().write(b);                    
                }
                public void close() throws IOException {
                    getResponseStream().close();
                }
                public void flush() throws IOException {
                    getResponseStream().flush();
                }
                public void write(byte[] b) throws IOException {
                    getResponseStream().write(b);
                }
                public void write(byte[] b, int off, int len) throws IOException {
                    getResponseStream().write(b, off, len);
                }
            };
        }
        private PrintWriter getResponseWriter() {
            if (outputStream!=null) throw new IllegalStateException("The variable 'sout' have been used already. Use either out/html or sout, not both.");
            if (writer==null) {
                try {
                    writer = response.getWriter();
                } catch (IOException ioe) {
                    writer = new PrintWriter(new ByteArrayOutputStream());
                    throw new IllegalStateException("unable to get response writer",ioe);
                }
            }
            return writer;
        }
        public PrintWriter getWriter() {
            return new PrintWriter(new InvalidOutputStream()) {
                public boolean checkError() {
                    return getResponseWriter().checkError();
                }
                public void close() {
                    getResponseWriter().close();
                }
                public void flush() {
                    getResponseWriter().flush();
                }
                public void write(char[] buf) {
                    getResponseWriter().write(buf);
                }
                public void write(char[] buf, int off, int len) {
                    getResponseWriter().write(buf, off, len);
                }
                public void write(int c) {
                    getResponseWriter().write(c);
                }
                public void write(String s, int off, int len) {
                    getResponseWriter().write(s, off, len);
                }
                public void println() {
                    getResponseWriter().println();
                }
                public PrintWriter format(String format, Object... args) {
                    getResponseWriter().format(format, args);
                    return this;
                }
                public PrintWriter format(Locale l, String format,  Object... args) {
                    getResponseWriter().format(l, format, args);
                    return this;
                }
            };
        }        
    }    
    
    private boolean initialized;

    /**
     * Initializes a servlet binding.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletRequest object
     * @param context  the ServletContext object
     */
    public ServletBinding(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        /*
         * Bind the default variables.
         */
        super.setVariable("request", request);
        super.setVariable("response", response);
        super.setVariable("context", context);
        super.setVariable("application", context);

        /*
         * Bind the HTTP session object - if there is one.
         * Note: we don't create one here!
         */
        super.setVariable("session", request.getSession(false));

        /*
         * Bind form parameter key-value hash map.
         *
         * If there are multiple, they are passed as an array.
         */
        Map params = new LinkedHashMap();
        for (Enumeration names = request.getParameterNames(); names.hasMoreElements();) {
            String name = (String) names.nextElement();
            if (!super.getVariables().containsKey(name)) {
                String[] values = request.getParameterValues(name);
                if (values.length == 1) {
                    params.put(name, values[0]);
                } else {
                    params.put(name, values);
                }
            }
        }
        super.setVariable("params", params);

        /*
         * Bind request header key-value hash map.
         */
        Map<String, String> headers = new LinkedHashMap<String, String>();
        for (Enumeration names = request.getHeaderNames(); names.hasMoreElements();) {
            String headerName = (String) names.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        super.setVariable("headers", headers);
    }

    @Override
    public void setVariable(String name, Object value) {
        lazyInit();
        validateArgs(name, "Can't bind variable to");
        excludeReservedName(name, "out");
        excludeReservedName(name, "sout");
        excludeReservedName(name, "html");
        super.setVariable(name, value);
    }

    @Override
    public Map getVariables() {
        lazyInit();
        return super.getVariables();
    }

    /**
     * @return a writer, an output stream, a markup builder or another requested object
     */
    @Override
    public Object getVariable(String name) {
        lazyInit();
        validateArgs(name, "No variable with");
        return super.getVariable(name);
    }

    private void lazyInit() {
        if (initialized) return;
        initialized = true;
        HttpServletResponse response = (HttpServletResponse) super.getVariable("response");
        ServletContext context = (ServletContext) super.getVariable("context");
        ServletOutput output = new ServletOutput(response);
        super.setVariable("out", output.getWriter());
        super.setVariable("sout", output.getOutputStream());
        super.setVariable("html", new MarkupBuilder(output.getWriter()));
    }

    private void validateArgs(String name, String message) {
        if (name == null) {
            throw new IllegalArgumentException(message + " null key.");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException(message + " blank key name. [length=0]");
        }
    }

    private void excludeReservedName(String name, String reservedName) {
        if (reservedName.equals(name)) {
            throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
        }
    }
}

