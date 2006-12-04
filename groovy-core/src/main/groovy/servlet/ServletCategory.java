/*
$Id$

Copyright 2005 (C) Guillaume Laforge. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package groovy.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

/**
 * Servlet support.
 */
public class ServletCategory {
	
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
