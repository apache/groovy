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
 * Example:
 *
 * <code>
 *  <gr:collect in="${thing}" expr="it.length() == 3">
 *        <p>${it}</p>
 *  </gr:collect>
 * </code>
 * @author Graeme Rocher
 * @since 19-Jan-2006
 */
public class GroovyCollectTag extends GroovySyntaxTag {
   public static final String TAG_NAME = "collect";
    private static final String ATTRIBUTE_IN = "in";
    private static final String ATTRIBUTE_EXPR = "expr";

    public boolean isBufferWhiteSpace() {
        return false;
    }

    public boolean hasPrecedingContent() {
        return true;
    }

    public void doStartTag() {
        String in = (String) attributes.get(ATTRIBUTE_IN);
        String expr = (String) attributes.get(ATTRIBUTE_EXPR);
        if(StringUtils.isBlank(in))
            throw new GrailsTagException("Tag ["+TAG_NAME+"] missing required attribute ["+ATTRIBUTE_IN+"]");
        if(StringUtils.isBlank(expr))
            throw new GrailsTagException("Tag ["+TAG_NAME+"] missing required attribute ["+ATTRIBUTE_EXPR+"]");

        out.print(in);
        out.print(".collect {");
        if(expr.startsWith("\"") && expr.endsWith("\"")) {
            expr = expr.substring(1,expr.length()-1);
        }
        out.print(expr);
        out.println("}.each {");
    }

    public void doEndTag() {
        out.println("}");
    }

    public String getName() {
        return TAG_NAME;
    }
}
