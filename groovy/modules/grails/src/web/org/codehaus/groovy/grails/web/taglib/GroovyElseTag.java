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

/**
 * @author Graeme Rocher
 * @since 18-Jan-2006
 */
public class GroovyElseTag extends GroovySyntaxTag {
    public static final String TAG_NAME = "else";


    public void doStartTag() {
        out.println("else {");
    }
    public void doEndTag() {
        out.println("}");
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
