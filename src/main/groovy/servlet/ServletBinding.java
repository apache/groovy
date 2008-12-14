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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servlet-specific binding extension to lazy load the writer or the output
 * stream from the response.
 * <p/>
 * <p>
 * <h3>Eager variables bound</h3>
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
 * <h3>Lazy variables bound</h3>
 * <ul>
 * <li><tt>"out"</tt> : response.getWriter()</li>
 * <li><tt>"sout"</tt> : response.getOutputStream()</li>
 * <li><tt>"html"</tt> : new MarkupBuilder(response.getWriter())</li>
 * </ul>
 * </p>
 *
 * @author Guillaume Laforge
 * @author Christian Stein
 */
public class ServletBinding extends Binding {
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
        Map headers = new LinkedHashMap();
        for (Enumeration names = request.getHeaderNames(); names.hasMoreElements();) {
            String headerName = (String) names.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        super.setVariable("headers", headers);
    }

    public void setVariable(String name, Object value) {
        lazyInit();
        validateArgs(name, "Can't bind variable to");
        excludeReservedName(name, "out");
        excludeReservedName(name, "sout");
        excludeReservedName(name, "html");
        super.setVariable(name, value);
    }

    public Map getVariables() {
        lazyInit();
        return super.getVariables();
    }

    /**
     * @return a writer, an output stream, a markup builder or another requested object
     */
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
        try {
            super.setVariable("out", response.getWriter());
            super.setVariable("sout", response.getOutputStream());
            super.setVariable("html", new MarkupBuilder(response.getWriter()));
        } catch (IOException e) {
            String message = "Failed to get writer or output stream from response.";
            context.log(message, e);
            throw new RuntimeException(message, e);
        }
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

