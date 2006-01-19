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
 *
 * Example:
 *
 * <code>
 *  <gr:grep in="${thing}" filter="${Pattern.compile('[a-zA-Z]')}">
 *        <p>${it}</p>
 *  </gr:grep>
 * </code>
 *
 * @author Graeme Rocher
 * @since 19-Jan-2006
 */
public class GroovyGrepTag extends GroovySyntaxTag{
   public static final String TAG_NAME = "grep";
    private static final String ATTRIBUTE_IN = "in";
    private static final String ATTRIBUTE_FILTER = "filter";

    public boolean isBufferWhiteSpace() {
        return false;
    }

    public boolean hasPrecedingContent() {
        return true;
    }

    public void doStartTag() {
        String in = (String) attributes.get(ATTRIBUTE_IN);
        String filter = (String) attributes.get(ATTRIBUTE_FILTER);
        if(StringUtils.isBlank(in))
            throw new GrailsTagException("Tag ["+TAG_NAME+"] missing required attribute ["+ATTRIBUTE_IN+"]");
        if(StringUtils.isBlank(filter))
            throw new GrailsTagException("Tag ["+TAG_NAME+"] missing required attribute ["+ATTRIBUTE_FILTER +"]");

        out.print(in);
        out.print(".grep (");
        if(filter.startsWith("\"") && filter.endsWith("\"")) {
            filter = filter.substring(1,filter.length()-1);
        }
        out.print(filter);
        out.println(").each {");
    }

    public void doEndTag() {
        out.println("}");
    }

    public String getName() {
        return TAG_NAME;
    }
}
