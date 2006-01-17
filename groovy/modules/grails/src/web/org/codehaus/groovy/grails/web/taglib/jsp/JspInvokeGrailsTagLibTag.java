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
package org.codehaus.groovy.grails.web.taglib.jsp;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.grails.web.metaclass.TagLibDynamicMethods;
import org.codehaus.groovy.grails.web.servlet.GrailsRequestAttributes;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;
import org.springframework.web.util.ExpressionEvaluationUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A tag that invokes a tag defined in a the Grails dynamic tag library. Authors of Grails tags
 * who want their tags to work in JSP should sub-class this class and call "setName" to set
 * the name of the tag within the Grails taglib
 *
 * This tag can of course be used standalone to invoke a Grails tag from JSP:
 *
 * <code>
 *   <gr:invokeTag name="myTag" />
 * </code>
 *
 * @author Graeme Rocher
 * @since 16-Jan-2006
 */
public class JspInvokeGrailsTagLibTag extends BodyTagSupport implements DynamicAttributes  {

    private String name;
    protected Map attributes = new HashMap();

    public int doStartTag()  {
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {
        GroovyObject tagLib = (GroovyObject)pageContext.getRequest().getAttribute(GrailsRequestAttributes.TAG_LIB);
        if(tagLib != null) {
            tagLib.setProperty( TagLibDynamicMethods.OUT_PROPERTY, pageContext.getOut() );
            Object tagLibProp;
            try {
                tagLibProp = tagLib.getProperty(getName());
            } catch (MissingPropertyException mpe) {
                throw new GrailsTagException("Tag ["+getName()+"] does not exist in tag library ["+tagLib.getClass().getName()+"]");
            }
            if(tagLibProp instanceof Closure) {

                Closure body = new Closure(this) {
                    public Object doCall() {
                        return call();
                    }
                    public Object call() {
                        BodyContent b = getBodyContent();
                        if(b != null) {
                            JspWriter out = b.getEnclosingWriter();
                            try {
                                out.write(b.getString());
                            } catch (IOException e) {
                                throw new GrailsTagException("I/O error writing body of tag ["+getName()+"]: " + e.getMessage(),e);
                            }
                        }
                        return null;
                    }
                };
                Closure tag = (Closure)tagLibProp;
                if(tag.getParameterTypes().length == 1) {
                    tag.call( new Object[]{ attributes });
                    if(body != null) {
                        body.call();
                    }
                }
                if(tag.getParameterTypes().length == 2) {
                    tag.call( new Object[] { attributes, body });
                }
            }else {
               throw new GrailsTagException("Tag ["+getName()+"] does not exist in tag library ["+tagLib.getClass().getName()+"]");
            }
        }
        else {
            throw new GrailsTagException("Tag ["+getName()+"] does not exist. No tag library found.");
        }
        BodyContent b = getBodyContent();
        b.clearBody();
        return SKIP_BODY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        if(value instanceof String) {
            String stringValue = (String)value;
            if(ExpressionEvaluationUtils.isExpressionLanguage(stringValue)) {
                 this.attributes.put(localName,ExpressionEvaluationUtils.evaluate(localName,stringValue,Object.class,this.pageContext));
            } else {
                this.attributes.put(localName,value);
            }
        }else {
            this.attributes.put(localName,value);
        }
    }
}
