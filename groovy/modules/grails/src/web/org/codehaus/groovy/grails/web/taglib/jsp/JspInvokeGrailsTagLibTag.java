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
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.util.ExpressionEvaluationUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.beans.PropertyDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.*;

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

    private static final String ZERO_ARGUMENTS = "zeroArgumentsFlag";
    private static final String GROOVY_DEFAULT_ARGUMENT = "it";
    private static final String NAME_ATTRIBUTE = "name";

    private String name;
    private int invocationCount;
    private List invocationArgs = new ArrayList();
    private List invocationBodyContent = new ArrayList();
    private BeanWrapper bean;
    protected Map attributes = new HashMap();
    private StringWriter sw;
    private PrintWriter out;
    private JspWriter jspWriter;


    public JspInvokeGrailsTagLibTag() {
        this.bean = new BeanWrapperImpl(this);
    }

    public final int doStartTag() throws JspException {
        PropertyDescriptor[] pds = this.bean.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor pd = pds[i];
            if( pd.getPropertyType() == String.class &&
                !pd.getName().equals(NAME_ATTRIBUTE) &&
                this.bean.isWritableProperty(pd.getName()) &&
                this.bean.isReadableProperty(pd.getName())) {
                String propertyValue = (String)this.bean.getPropertyValue(pd.getName());
                if(propertyValue != null) {
                    if(ExpressionEvaluationUtils.isExpressionLanguage(propertyValue)) {
                        this.attributes.put(pd.getName(), ExpressionEvaluationUtils.evaluate(pd.getName(),propertyValue,Object.class,super.pageContext));
                    }
                    else {
                        this.attributes.put(pd.getName(), propertyValue);
                    }
                }
            }
        }
        return doStartTagInternal();
    }

    protected int doStartTagInternal() {
      GroovyObject tagLib = (GroovyObject)pageContext.getRequest().getAttribute(GrailsApplicationAttributes.TAG_LIB);
        if(tagLib != null) {
            sw = new StringWriter();
            out = new PrintWriter(sw);
            tagLib.setProperty( TagLibDynamicMethods.OUT_PROPERTY, out );
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
                    public Object doCall(Object o) {
                        return call(new Object[]{o});
                    }
                    public Object doCall(Object[] args) {
                        return call(args);
                    }
                    public Object call(Object[] args) {
                        invocationCount++;
                        if(args.length > 0) {
                            invocationArgs.add(args[0]);
                        }
                        else {
                            invocationArgs.add(ZERO_ARGUMENTS);
                        }
                        out.print("<jsp-body-gen"+invocationCount+">");
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

        Collections.reverse(invocationArgs);
        setCurrentArgument();
        return EVAL_BODY_BUFFERED;
    }

    private void setCurrentArgument() {
        if(invocationCount > 0) {
            Object arg = invocationArgs.get(invocationCount - 1);
            if(arg.equals(ZERO_ARGUMENTS)) {
                pageContext.setAttribute(GROOVY_DEFAULT_ARGUMENT, null);
            }
            else {
                pageContext.setAttribute(GROOVY_DEFAULT_ARGUMENT, arg);
            }
        }
    }

    public int doAfterBody() throws JspException {

        BodyContent b = getBodyContent();
        if(invocationCount > 0) {
            if(b != null) {
                jspWriter = b.getEnclosingWriter();
                invocationBodyContent.add(b.getString());
                b.clearBody();
            }
        }

        invocationCount--;
        setCurrentArgument();
        if(invocationCount <= 0)  {
            String tagContent = sw.toString();

            int i = 1;
            StringBuffer buf = new StringBuffer();
            for (Iterator iter = invocationBodyContent.iterator(); iter.hasNext();i++) {
                String body = (String) iter.next();
                String replaceFlag = "<jsp-body-gen"+i+">";
                int j = tagContent.indexOf(replaceFlag);
                if(j > -1) {
                    buf.append(tagContent.substring(0,j))
                        .append(body)
                        .append(tagContent.substring(j + replaceFlag.length(), tagContent.length()));
                    tagContent = buf.toString();
                    buf.delete(0, buf.length());
                }
            }
            try {
                jspWriter.write(tagContent);
                out.close();
            } catch (IOException e) {
                throw new JspTagException("I/O error writing tag contents ["+tagContent +"] to response out");
            }
            return SKIP_BODY;
        }
        else
            return EVAL_BODY_BUFFERED;
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
