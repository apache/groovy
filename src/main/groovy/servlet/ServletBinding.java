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
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import java.util.Map;
import java.util.Enumeration;
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

    public ServletBinding(HttpServletRequest request, ServletResponse response, ServletContext sc) {
        this.response = response;

        binding.setVariable("request", request);
        binding.setVariable("response", response);
        binding.setVariable("context", sc);
        binding.setVariable("application", sc);
        binding.setVariable("session", request.getSession(true));

        // Form parameters. If there are multiple its passed as a list.
        for (Enumeration paramEnum = request.getParameterNames(); paramEnum.hasMoreElements();) {
            String key = (String) paramEnum.nextElement();
            if (!binding.getVariables().containsKey(key)) {
                String[] values = request.getParameterValues(key);
                if (values.length == 1) {
                    binding.setVariable(key, values[0]);
                } else {
                    binding.setVariable(key, values);
                }
            }
        }
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
        try {
            if ("out".equals(name))
                return response.getWriter();
            if ("sout".equals(name))
                return response.getOutputStream();
            if ("html".equals(name)) {
                if (html == null)
                    html = new MarkupBuilder(response.getWriter());
                return html;
            }
        }
        catch (IOException e) { }
        return binding.getVariable(name);
    }
}
