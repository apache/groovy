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

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public class LinkTag extends AbstractTag {
    public static final String TAG_NAME = "link";

    public static final String ATTRIBUTE_CONTROLLER = "controller";
    public static final String ATTRIBUTE_ACTION = "action";
    public static final String ATTRIBUTE_ID = "id";

    protected void doStartTagInternal() throws IOException {
       StringBuffer buf = new StringBuffer();
        buf.append("<a href=\"")
            .append(contextPath)
            .append('/')
            .append(attributes.remove(ATTRIBUTE_CONTROLLER));
            if(attributes.containsKey(ATTRIBUTE_ACTION)) {
                buf.append('/')
                    .append(attributes.remove(ATTRIBUTE_ACTION));
            }
            if(attributes.containsKey(ATTRIBUTE_ID)) {
                buf.append('?')
                   .append("id=")
                   .append(attributes.remove(ATTRIBUTE_ID));
            }

            buf.append("\" ");

            if(attributes.size() > 0) {
                for (Iterator i = attributes.keySet().iterator(); i.hasNext();) {
                    String attributeName = (String) i.next();
                    Object attributeValue = attributes.get(attributeName);
                    buf.append(attributeName)
                       .append("=\"")
                       .append(attributeValue)
                       .append("\" ");
                }
            }
            buf.append('>');
            out.write(buf.toString());
    }

    protected void doEndTagInternal() throws IOException {
        out.write("</a>");
    }

}
