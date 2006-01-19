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

import org.codehaus.groovy.grails.web.pages.GroovyPage;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.Writer;
import java.io.PrintWriter;

/**
 * A tag type that gets translated directly into Groovy syntax by the GSP parser
 *
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public abstract class GroovySyntaxTag implements GrailsTag {
    protected Map tagContext;
    protected PrintWriter out;
    protected Map attributes = new HashMap();

    public void init(Map tagContext) {
        this.tagContext = tagContext;
        this.out = (PrintWriter)tagContext.get(GroovyPage.OUT);
    }

    public void setWriter(Writer w) {
        if(w instanceof PrintWriter) {
            this.out = (PrintWriter)w;
        }
        else {
            throw new IllegalArgumentException("A GroovySynax tag requires a java.io.PrintWriter instance");
        }
    }

    public void setAttributes(Map attributes) {
        for (Iterator i = attributes.keySet().iterator(); i.hasNext();) {
            String attrName = (String) i.next();
            setAttribute(attrName,attributes.get(attrName));
        }
    }

    public void setAttribute(String name, Object value) {
        if(value instanceof String ) {
            String stringValue = (String)value;
            if(stringValue.startsWith("${") && stringValue.endsWith("}")) {
                stringValue = stringValue.substring(2,stringValue.length() -1);
            }

            this.attributes.put(name.substring(1,name.length()-1),stringValue);
        }
        else {
            throw new IllegalArgumentException("A GroovySynax tag requires only string valued attributes");
        }
    }

    public abstract boolean isBufferWhiteSpace();

    public abstract boolean hasPrecedingContent();
}
