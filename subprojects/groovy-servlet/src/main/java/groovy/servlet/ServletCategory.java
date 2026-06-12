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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

/**
 * Servlet support.
 */
public class ServletCategory {

    /**
     * Reads a servlet context attribute using property-style access.
     *
     * @param context the servlet context
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object get(ServletContext context, String key) {
        return context.getAttribute(key);
    }

    /**
     * Reads an HTTP session attribute using property-style access.
     *
     * @param session the HTTP session
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object get(HttpSession session, String key) {
        return session.getAttribute(key);
    }

    /**
     * Reads a request attribute using property-style access.
     *
     * @param request the servlet request
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object get(ServletRequest request, String key) {
        return request.getAttribute(key);
    }

    /**
     * Reads a page context attribute using property-style access.
     *
     * @param context the page context
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object get(PageContext context, String key) {
        return context.getAttribute(key);
    }

    /**
     * Reads a servlet context attribute using Groovy subscript syntax.
     *
     * @param context the servlet context
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object getAt(ServletContext context, String key) {
        return context.getAttribute(key);
    }

    /**
     * Reads an HTTP session attribute using Groovy subscript syntax.
     *
     * @param session the HTTP session
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object getAt(HttpSession session, String key) {
        return session.getAttribute(key);
    }

    /**
     * Reads a request attribute using Groovy subscript syntax.
     *
     * @param request the servlet request
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object getAt(ServletRequest request, String key) {
        return request.getAttribute(key);
    }

    /**
     * Reads a page context attribute using Groovy subscript syntax.
     *
     * @param context the page context
     * @param key the attribute name
     * @return the stored attribute value
     */
    public static Object getAt(PageContext context, String key) {
        return context.getAttribute(key);
    }

    /**
     * Stores a servlet context attribute using property-style access.
     *
     * @param context the servlet context
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void set(ServletContext context, String key, Object value) {
        context.setAttribute(key, value);
    }

    /**
     * Stores an HTTP session attribute using property-style access.
     *
     * @param session the HTTP session
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void set(HttpSession session, String key, Object value) {
        session.setAttribute(key, value);
    }

    /**
     * Stores a request attribute using property-style access.
     *
     * @param request the servlet request
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void set(ServletRequest request, String key, Object value) {
        request.setAttribute(key, value);
    }

    /**
     * Stores a page context attribute using property-style access.
     *
     * @param context the page context
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void set(PageContext context, String key, Object value) {
        context.setAttribute(key, value);
    }

    /**
     * Stores a servlet context attribute using Groovy subscript syntax.
     *
     * @param context the servlet context
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void putAt(ServletContext context, String key, Object value) {
        context.setAttribute(key, value);
    }

    /**
     * Stores an HTTP session attribute using Groovy subscript syntax.
     *
     * @param session the HTTP session
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void putAt(HttpSession session, String key, Object value) {
        session.setAttribute(key, value);
    }

    /**
     * Stores a request attribute using Groovy subscript syntax.
     *
     * @param request the servlet request
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void putAt(ServletRequest request, String key, Object value) {
        request.setAttribute(key, value);
    }

    /**
     * Stores a page context attribute using Groovy subscript syntax.
     *
     * @param context the page context
     * @param key the attribute name
     * @param value the attribute value
     */
    public static void putAt(PageContext context, String key, Object value) {
        context.setAttribute(key, value);
    }
}
