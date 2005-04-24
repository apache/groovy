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

import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import java.util.Map;
import java.util.Enumeration;
import java.util.HashMap;
import java.io.IOException;

/**
 * Servlet-specific binding extesion to lazy load the writer or the output stream from the response.
 * This binding also provide a markup builder named "html".
 *
 * @author Guillaume Laforge
 */
public class ServletBinding extends Binding {

    protected Binding binding = new Binding();
    private ServletResponse response;
    private MarkupBuilder html;
    private ServletContext sc;

    public ServletBinding(HttpServletRequest request, final HttpServletResponse response, ServletContext sc) {
        this.response = response;
        this.sc = sc;

        binding.setVariable("request", request);
        binding.setVariable("response", response);
        binding.setVariable("context", sc);
        binding.setVariable("application", sc);
        binding.setVariable("session", request.getSession(true));

        // Form parameters. If there are multiple its passed as a list.
        Map params = new HashMap();
        for (Enumeration paramEnum = request.getParameterNames(); paramEnum.hasMoreElements();) {
            String key = (String) paramEnum.nextElement();
            if (!binding.getVariables().containsKey(key)) {
                String[] values = request.getParameterValues(key);
                if (values.length == 1) {
                    params.put(key, values[0]);
                } else {
                    params.put(key, values);
                }
            }
        }
        binding.setVariable("param", params);

        // Headers
        Map headers = new HashMap() {
            public Object put(Object key, Object value) {
                response.setHeader(key.toString(), value.toString());
                return null;
            }
        };
        for (Enumeration headerEnum = request.getHeaderNames(); headerEnum.hasMoreElements(); ) {
            String headerName = (String) headerEnum.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        binding.setVariable("header", headers);
    }

    public void setVariable(String name, Object value) {
        binding.setVariable(name, value);
    }

    public Map getVariables() {
        return binding.getVariables();
    }

    /**
     * @return a writer, an output stream, a markup builder or another requested object
     */
    public Object getVariable(String name) {
            if ("out".equals(name))
                try {
                    return response.getWriter();
                } catch (IOException e) {
                    sc.log("Failed to get writer from response", e);
                    return null;
                }
            if ("sout".equals(name))
                try {
                    return response.getOutputStream();
                } catch (IOException e) {
                    sc.log("Failed to get outputstream from response", e);
                    return null;
                }
            if ("html".equals(name)) {
                if (html == null)
                    try {
                        html = new MarkupBuilder(response.getWriter());
                    } catch (IOException e) {
                        sc.log("Failed to get writer from response", e);
                        return null;
                    }
                return html;
            }
        return binding.getVariable(name);
    }
}
