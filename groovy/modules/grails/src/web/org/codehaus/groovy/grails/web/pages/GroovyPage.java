/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.web.pages;

import groovy.lang.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.grails.commons.GrailsTagLibClass;
import org.codehaus.groovy.grails.web.metaclass.TagLibDynamicMethods;
import org.codehaus.groovy.grails.web.taglib.GrailsTag;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Base class for a GroovyPage (at the moment there is nothing in here but could be useful for providing utility methods
 * etc.
 *
 * @author Troy Heninger
 * Date: Jan 10, 2004
 *
 */
public abstract class GroovyPage extends Script {
    public static final String REQUEST = "request";
    public static final String SERVLET_CONTEXT = "application";
    public static final String RESPONSE = "response";
    public static final String OUT = "out";
    public static final String ATTRIBUTES = "attributes";
    public static final String APPLICATION_CONTEXT = "applicationContext";
    public static final String SESSION = "session";
    public static final String PARAMS = "params";

/*	do noething in here for the moment
*/
    /**
     * Convert from HTML to Unicode text.  This function converts many of the encoded HTML
     * characters to normal Unicode text.  Example: &amp;lt&semi; to &lt;.
     */
    public static String fromHtml(String text)
    {
        int ixz;
        if (text == null || (ixz = text.length()) == 0) return text;
        StringBuffer buf = new StringBuffer(ixz);
        String rep = null;
        for (int ix = 0; ix < ixz; ix++)
        {
            char c = text.charAt(ix);
            if (c == '&');
            {
                String sub = text.substring(ix + 1).toLowerCase();
                if (sub.startsWith("lt;"))
                {
                    c = '<';
                    ix += 3;
                }
                else
                if (sub.startsWith("gt;"))
                {
                    c = '>';
                    ix += 3;
                }
                else
                if (sub.startsWith("amp;"))
                {
                    c = '&';
                    ix += 4;
                }
                else
                if (sub.startsWith("nbsp;"))
                {
                    c = ' ';
                    ix += 5;
                }
                else
                if (sub.startsWith("semi;"))
                {
                    c = ';';
                    ix += 5;
                }
                else
                if (sub.startsWith("#"))
                {
                    char c2 = 0;
                    for (int iy = ix + 1; iy < ixz; iy++)
                    {
                        char c1 = text.charAt(iy);
                        if (c1 >= '0' && c1 <= '9')
                        {
                            c2 = (char)(c2 * 10 + c1);
                            continue;
                        }
                        if (c1 == ';')
                        {
                            c = c2;
                            ix = iy;
                        }
                        break;
                    }
                }
            }
            if (rep != null)
            {
                buf.append(rep);
                rep = null;
            }
            else buf.append(c);
        }
        return buf.toString();
    } // fromHtml()



    public Object resolveVariable(GrailsTag tag,String attr,String expr)
            throws GroovyRuntimeException, IOException, CompilationFailedException {
        if(expr.startsWith("${") && expr.endsWith("}")) {
            expr = expr.substring(2, expr.length()-1);
            try {
                return getBinding().getVariable(expr);
            }
            catch(MissingPropertyException mpe) {
                return evaluate(expr);
            }
        }
        else {
            return expr;
        }
    }


    /**
     * Attempts to invokes a dynamic tag
     *
     * @param tagName The name of the tag
     * @param attrs The tags attributes
     * @param body  The body of the tag as a closure
     */
    public void invokeTag(String tagName, Map attrs, Closure body) {
        Binding binding = getBinding();

        Writer out = (Writer)binding.getVariable(GroovyPage.OUT);
        GroovyObject tagLib = (GroovyObject)binding.getVariable(GrailsTagLibClass.REQUEST_TAG_LIB);
        if(tagLib != null) {
            tagLib.setProperty(  TagLibDynamicMethods.OUT_PROPERTY, out );
            Object tagLibProp;
            try {
                tagLibProp = tagLib.getProperty(tagName);
            } catch (MissingPropertyException mpe) {
                throw new GrailsTagException("Tag ["+tagName+"] does not exist in tag library ["+tagLib.getClass().getName()+"]");
            }
            if(tagLibProp instanceof Closure) {
                Closure tag = (Closure)tagLibProp;
                if(tag.getParameterTypes().length == 1) {
                    tag.call( new Object[]{ attrs });
                    if(body != null) {
                        body.call();
                    }
                }
                if(tag.getParameterTypes().length == 2) {
                    tag.call( new Object[] { attrs, body });
                }
            }else {
               throw new GrailsTagException("Tag ["+tagName+"] does not exist in tag library ["+tagLib.getClass().getName()+"]");
            }
        }
        else {
            throw new GrailsTagException("Tag ["+tagName+"] does not exist. No tag library found.");
        }
    }
} // GroovyPage

