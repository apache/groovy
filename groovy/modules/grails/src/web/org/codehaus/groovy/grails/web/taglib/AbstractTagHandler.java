/* Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.web.taglib;

import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

/**
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public abstract class AbstractTagHandler implements GrailsTagHandler {
    protected Writer out;
    protected Map attributes;
    protected HttpServletRequest request;
    protected String contextPath;
    protected UrlPathHelper urlPathHelper = new UrlPathHelper();

    public AbstractTagHandler(HttpServletRequest request, Writer out, Map attributes) {
        if(out == null)
            throw new IllegalArgumentException("The constructor argument 'out' cannot be null");
        if(request == null)
            throw new IllegalArgumentException("The constructor argument 'request' cannot be null");

        this.out = out;
        this.request = request;
        if(attributes == null)
            this.attributes = Collections.EMPTY_MAP;
        else {
            this.attributes = attributes;
        }
        this.contextPath = urlPathHelper.getContextPath( this.request );
    }

    public void setWriter(Writer w) {
        this.out = w;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }
}
