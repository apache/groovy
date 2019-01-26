/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.servlet;

import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.MethodClosure;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Servlet-specific binding extension to lazy load the writer or the output
 * stream from the response.
 * <p>
 * <h3>Eager variables</h3>
 * <ul>
 * <li><tt>"request"</tt> : the <code>HttpServletRequest</code> object</li>
 * <li><tt>"response"</tt> : the <code>HttpServletRequest</code> object</li>
 * <li><tt>"context"</tt> : the <code>ServletContext</code> object</li>
 * <li><tt>"application"</tt> : same as context</li>
 * <li><tt>"session"</tt> : shorthand for <code>request.getSession(<tt>false</tt>)</code> - can be null!</li>
 * <li><tt>"params"</tt> : map of all form parameters - can be empty</li>
 * <li><tt>"headers"</tt> : map of all <tt>request</tt> header fields</li>
 * </ul>
 * <p>
 * <h3>Lazy variables</h3>
 * <ul>
 * <li><tt>"out"</tt> : <code>response.getWriter()</code></li>
 * <li><tt>"sout"</tt> : <code>response.getOutputStream()</code></li>
 * <li><tt>"html"</tt> : <code>new MarkupBuilder(response.getWriter())</code> - <code>expandEmptyElements</code> flag is set to true</li>
 * <li><tt>"json"</tt> : <code>new JsonBuilder()</code></li>
 * </ul>
 * As per the Servlet specification, a call to <code>response.getWriter()</code> should not be
 * done if a call to <code>response.getOutputStream()</code> has already occurred or the other way
 * around. You may wonder then how the above lazy variables can possibly be provided - since
 * setting them up would involve calling both of the above methods. The trick is catered for
 * behind the scenes using lazy variables. Lazy bound variables can be requested without side
 * effects; under the covers the writer and stream are wrapped. That means
 * <code>response.getWriter()</code> is never directly called until some output is done using
 * 'out' or 'html'. Once a write method call is done using either of these variable, then an attempt
 * to write using 'sout' will cause an <code>IllegalStateException</code>. Similarly, if a write method
 * call on 'sout' has been done already, then any further write method call on 'out' or 'html' will cause an
 * <code>IllegalStateException</code>.
 * <p>
 * <h3>Reserved internal variable names (see "Methods" below)</h3>
 * <ul>
 * <li><tt>"forward"</tt></li>
 * <li><tt>"include"</tt></li>
 * <li><tt>"redirect"</tt></li>
 * </ul>
 *
 * If <code>response.getWriter()</code> is called directly (without using out), then a write method
 * call on 'sout' will not cause the <code>IllegalStateException</code>, but it will still be invalid.
 * It is the responsibility of the user of this class, to not to mix these different usage
 * styles. The same applies to calling <code>response.getOutputStream()</code> and using 'out' or 'html'.
 *
 * <h3>Methods</h3>
 * <ul>
 * <li><tt>"forward(String path)"</tt> : <code>request.getRequestDispatcher(path).forward(request, response)</code></li>
 * <li><tt>"include(String path)"</tt> : <code>request.getRequestDispatcher(path).include(request, response)</code></li>
 * <li><tt>"redirect(String location)"</tt> : <code>response.sendRedirect(location)</code></li>
 * </ul>
 */
public class ServletBinding extends Binding {
    
    /**
     * A OutputStream dummy that will throw a GroovyBugError for any
     * write method call to it. 
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
     */
    private static class ServletOutput {
        private final HttpServletResponse response;
        private ServletOutputStream outputStream;
        private PrintWriter writer;
        
        public ServletOutput(HttpServletResponse response) {
            this.response = response;
        }
        private ServletOutputStream getResponseStream() throws IOException {
            if (writer != null) throw new IllegalStateException("The variable 'out' or 'html' have been used already. Use either out/html or sout, not both.");
            if (outputStream == null) outputStream = response.getOutputStream();
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
            if (outputStream != null) throw new IllegalStateException("The variable 'sout' have been used already. Use either out/html or sout, not both.");
            if (writer == null) {
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
        Map params = collectParams(request);
        super.setVariable("params", params);

        /*
         * Bind request header key-value hash map.
         */
        Map<String, String> headers = new LinkedHashMap<>();
        for (Enumeration names = request.getHeaderNames(); names.hasMoreElements();) {
            String headerName = (String) names.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        super.setVariable("headers", headers);
    }

    @SuppressWarnings("unchecked")
    private Map collectParams(HttpServletRequest request) {
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
        return params;
    }

    @Override
    public void setVariable(String name, Object value) {
        lazyInit();
        validateArgs(name, "Can't bind variable to");
        excludeReservedName(name, "out");
        excludeReservedName(name, "sout");
        excludeReservedName(name, "html");
		excludeReservedName(name, "json");
        excludeReservedName(name, "forward");
        excludeReservedName(name, "include");
        excludeReservedName(name, "redirect");
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
        ServletOutput output = new ServletOutput(response);
        super.setVariable("out", output.getWriter());
        super.setVariable("sout", output.getOutputStream());

        MarkupBuilder builder = new MarkupBuilder(output.getWriter());
        builder.setExpandEmptyElements(true);
        super.setVariable("html", builder);

        try {
            Class jsonBuilderClass = this.getClass().getClassLoader().loadClass("groovy.json.StreamingJsonBuilder");
            Constructor writerConstructor = jsonBuilderClass.getConstructor(Writer.class);
            super.setVariable("json", writerConstructor.newInstance(output.getWriter()));
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // bind forward method
        MethodClosure c = new MethodClosure(this, "forward");
        super.setVariable("forward", c);
        
        // bind include method
        c = new MethodClosure(this, "include");
        super.setVariable("include", c);
        
        // bind redirect method
        c = new MethodClosure(this, "redirect");
        super.setVariable("redirect", c);
    }

    private static void validateArgs(String name, String message) {
        if (name == null) {
            throw new IllegalArgumentException(message + " null key.");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException(message + " blank key name. [length=0]");
        }
    }

    private static void excludeReservedName(String name, String reservedName) {
        if (reservedName.equals(name)) {
            throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
        }
    }
    
    public void forward(String path) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) super.getVariable("request");
        HttpServletResponse response = (HttpServletResponse) super.getVariable("response");
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    } 
    
    public void include(String path) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) super.getVariable("request");
        HttpServletResponse response = (HttpServletResponse) super.getVariable("response");
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.include(request, response);
    }

    public void redirect(String location) throws IOException {
        HttpServletResponse response = (HttpServletResponse) super.getVariable("response");
        response.sendRedirect(location);
    }    
}
