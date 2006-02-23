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

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;

/**
 * Allows defining of variables within the page context
 *
 * @author Graeme Rocher
 * @since 23-Feb-2006
 */
public class GroovyDefTag extends GroovySyntaxTag {
    public static final String TAG_NAME = "def";
    private static final String ATTRIBUTE_EXPR = "expr";
    private static final String ATTRIBUTE_VAR = "var";

    public void doStartTag() {
        String expr = (String) attributes.get(ATTRIBUTE_EXPR);
        String var = (String) attributes.get(ATTRIBUTE_VAR);

        if(StringUtils.isBlank(var))
            throw new GrailsTagException("Tag ["+TAG_NAME+"] missing required attribute ["+ATTRIBUTE_VAR+"]");
        if(StringUtils.isBlank(expr))
            throw new GrailsTagException("Tag ["+TAG_NAME+"] missing required attribute ["+ATTRIBUTE_EXPR+"]");

        out.print("def ");
        out.print(var.substring(1,var.length() -1));
        out.print('=');
        out.println(expr);
    }

    public void doEndTag() {
        // do nothing
    }

    public String getName() {
        return TAG_NAME;
    }

    public boolean isBufferWhiteSpace() {
        return false;
    }

    public boolean hasPrecedingContent() {
        return false;
    }
}
