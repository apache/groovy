/*
 * Created on Apr 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package groovy.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

/**
 * @author sam
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ServletCategory {
	
    /**
     * Servlet support
     */
    public static Object get(ServletContext context, String key) {
        return context.getAttribute(key);
    }
    
    public static Object get(HttpSession session, String key) {
        return session.getAttribute(key);
    }
    
    public static Object get(ServletRequest request, String key) {
        return request.getAttribute(key);
    }

    public static Object get(PageContext context, String key) {
        return context.getAttribute(key);
    }

    public static Object getAt(ServletContext context, String key) {
        return context.getAttribute(key);
    }

    public static Object getAt(HttpSession session, String key) {
        return session.getAttribute(key);
    }

    public static Object getAt(ServletRequest request, String key) {
        return request.getAttribute(key);
    }

    public static Object getAt(PageContext context, String key) {
        return context.getAttribute(key);
    }

    public static void set(ServletContext context, String key, Object value) {
        context.setAttribute(key, value);
    }

    public static void set(HttpSession session, String key, Object value) {
        session.setAttribute(key, value);
    }

    public static void set(ServletRequest request, String key, Object value) {
        request.setAttribute(key, value);
    }

    public static void set(PageContext context, String key, Object value) {
        context.setAttribute(key, value);
    }

    public static void putAt(ServletContext context, String key, Object value) {
        context.setAttribute(key, value);
    }

    public static void putAt(HttpSession session, String key, Object value) {
        session.setAttribute(key, value);
    }

    public static void putAt(ServletRequest request, String key, Object value) {
        request.setAttribute(key, value);
    }

    public static void putAt(PageContext context, String key, Object value) {
        context.setAttribute(key, value);
    }

}
