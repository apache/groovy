/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.util;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;


/**
 * A GroovyObject facade for an underlying MBean which acts like a normal 
 * groovy object but which is actually implemented via 
 * an underlying JMX MBean. 
 * Properties and normal method invocations
 * delegate to the MBeanServer to the actual MBean.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyMBean extends GroovyObjectSupport {

    private MBeanServer server;
    private ObjectName name;
    private MBeanInfo beanInfo;
    private Map operations = new HashMap();

    public GroovyMBean(MBeanServer server, ObjectName name) throws JMException {
        this.server = server;
        this.name = name;
        this.beanInfo = server.getMBeanInfo(name);
        
        MBeanOperationInfo[] operationInfos = beanInfo.getOperations();
        for (int i = 0; i < operationInfos.length; i++ ) {
            MBeanOperationInfo info = operationInfos[i];
            operations.put(info.getName(), createSignature(info));
        }
    }

    public MBeanServer server() {
        return server;
    }
    
    public ObjectName name() {
        return name;
    }
    
    public MBeanInfo info() {
        return beanInfo;
    }
    
    public Object getProperty(String property) {
        try {
            return server.getAttribute(name, property);
        }
        catch (MBeanException e) {
            throw new GroovyRuntimeException("Could not access property: " + property + ". Reason: " + e, e.getTargetException());
        }
        catch (Exception e) {
            throw new GroovyRuntimeException("Could not access property: " + property + ". Reason: " + e, e);
        }
    }
    
    public void setProperty(String property, Object value) {
        try {
            server.setAttribute(name, new Attribute(property, value));
        }
        catch (MBeanException e) {
            throw new GroovyRuntimeException("Could not set property: " + property + ". Reason: " + e, e.getTargetException());
        }
        catch (Exception e) {
            throw new GroovyRuntimeException("Could not set property: " + property + ". Reason: " + e, e);
        }
    }
    
    public Object invokeMethod(String method, Object arguments) {
        String[] signature = (String[]) operations.get(method);
        if (signature != null) {
            Object[] argArray = null;
            if (arguments instanceof Object[]) {
                argArray = (Object[]) arguments;
            }
            else {
                argArray = new Object[] { arguments };
            }
            try {
                return server.invoke(name, method, argArray, signature);
            }
            catch (MBeanException e) {
                throw new GroovyRuntimeException("Could not invoke method: " + method + ". Reason: " + e, e.getTargetException());
            }
            catch (Exception e) {
                throw new GroovyRuntimeException("Could not invoke method: " + method + ". Reason: " + e, e);
            }
        }
        else {
            return super.invokeMethod(method, arguments);
        }
    }

    protected String[] createSignature(MBeanOperationInfo info) {
        MBeanParameterInfo[] params = info.getSignature();
        String[] answer = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            answer[i] = params[i].getType();
        }
        return answer;
    }
}
