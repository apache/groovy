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

import javax.management.*;
import java.io.IOException;
import java.util.*;

/**
 * A GroovyObject facade for an underlying MBean which acts like a normal
 * groovy object but which is actually implemented via
 * an underlying JMX MBean.
 * Properties and normal method invocations
 * delegate to the MBeanServer to the actual MBean.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Steve Button
 * @author Paul King
 * @version $Revision$
 */
public class GroovyMBean extends GroovyObjectSupport {
    private MBeanServerConnection server;
    private ObjectName name;
    private MBeanInfo beanInfo;
    private boolean ignoreErrors;
    private Map operations = new HashMap();

    public GroovyMBean(MBeanServerConnection server, String objectName) throws JMException, IOException {
        this(server, objectName, false);
    }

    public GroovyMBean(MBeanServerConnection server, String objectName, boolean ignoreErrors) throws JMException, IOException {
        this(server, new ObjectName(objectName), ignoreErrors);
    }

    public GroovyMBean(MBeanServerConnection server, ObjectName name) throws JMException, IOException {
        this(server, name, false);
    }

    public GroovyMBean(MBeanServerConnection server, ObjectName name, boolean ignoreErrors) throws JMException, IOException {
        this.server = server;
        this.name = name;
        this.ignoreErrors = ignoreErrors;
        this.beanInfo = server.getMBeanInfo(name);

        MBeanOperationInfo[] operationInfos = beanInfo.getOperations();
        for (int i = 0; i < operationInfos.length; i++) {
            MBeanOperationInfo info = operationInfos[i];
            String signature[] = createSignature(info);

            // Include a simple fix here to support overloaded operations on the MBean.
            // Construct a simple key for an operation by adding the number of parameters it uses
            String operationKey = createOperationKey(info.getName(), signature.length);
            operations.put(operationKey, signature);
        }
    }

    public MBeanServerConnection server() {
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
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not access property: " + property + ". Reason: " + e, e.getTargetException());
        }
        catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not access property: " + property + ". Reason: " + e, e);
        }
        return null;
    }

    public void setProperty(String property, Object value) {
        try {
            server.setAttribute(name, new Attribute(property, value));
        }
        catch (MBeanException e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not set property: " + property + ". Reason: " + e, e.getTargetException());
        }
        catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not set property: " + property + ". Reason: " + e, e);
        }
    }

    public Object invokeMethod(String method, Object arguments) {
        // Moved this outside the try block so we can obtain the number of parameters
        // specified in the arguments array, which is needed to find the correct method.
        Object[] argArray = null;
        if (arguments instanceof Object[]) {
            argArray = (Object[]) arguments;
        } else {
            argArray = new Object[]{arguments};
        }
        // Locate the specific method based on the name and number of parameters
        String operationKey = createOperationKey(method, argArray.length);
        String[] signature = (String[]) operations.get(operationKey);

        if (signature != null) {
            try {
                return server.invoke(name, method, argArray, signature);
            }
            catch (MBeanException e) {
                if (!ignoreErrors)
                    throw new GroovyRuntimeException("Could not invoke method: " + method + ". Reason: " + e, e.getTargetException());
            }
            catch (Exception e) {
                if (!ignoreErrors)
                    throw new GroovyRuntimeException("Could not invoke method: " + method + ". Reason: " + e, e);
            }
            return null;
        } else {
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

    /**
     * Construct a simple key based on the method name and the number of parameters
     *
     * @param operation - the mbean operation name
     * @param params    - the number of parameters the operation supports
     * @return simple unique identifier for a method
     */
    protected String createOperationKey(String operation, int params) {
        // This could be changed to support some hash of the parameter types, etc.
        return operation + "_" + params;
    }

    /**
     * List of the names of each of the attributes on the MBean
     *
     * @return list of attribute names
     */
    public Collection listAttributeNames() {
        ArrayList list = new ArrayList();
        try {
            MBeanAttributeInfo[] attrs = beanInfo.getAttributes();
            for (int i = 0; i < attrs.length; i++) {
                MBeanAttributeInfo attr = attrs[i];
                list.add(attr.getName());
            }
        } catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not list attribute names. Reason: " + e, e);
        }
        return list;
    }

    /**
     * The values of each of the attributes on the MBean
     *
     * @return list of values of each attribute
     */
    public List listAttributeValues() {
        ArrayList list = new ArrayList();
        Collection names = listAttributeNames();
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            try {
                Object val = this.getProperty(name);
                if (val != null) {
                    list.add(name + " : " + val.toString());
                }
            } catch (Exception e) {
                if (!ignoreErrors)
                    throw new GroovyRuntimeException("Could not list attribute values. Reason: " + e, e);
            }
        }
        return list;
    }

    /**
     * List of string representations of all of the attributes on the MBean.
     *
     * @return list of descriptions of each attribute on the mbean
     */
    public Collection listAttributeDescriptions() {
        ArrayList list = new ArrayList();
        try {
            MBeanAttributeInfo[] attrs = beanInfo.getAttributes();
            for (int i = 0; i < attrs.length; i++) {
                MBeanAttributeInfo attr = attrs[i];
                list.add(describeAttribute(attr));
            }
        } catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not list attribute descriptions. Reason: " + e, e);
        }
        return list;
    }

    /**
     * Description of the specified attribute name.
     *
     * @param attr - the attribute
     * @return String the description
     */
    protected String describeAttribute(MBeanAttributeInfo attr) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        if (attr.isReadable()) {
            buf.append("r");
        }
        if (attr.isWritable()) {
            buf.append("w");
        }
        buf.append(") ")
                .append(attr.getType())
                .append(" ")
                .append(attr.getName());
        return buf.toString();
    }

    /**
     * Description of the specified attribute name.
     *
     * @param attributeName - stringified name of the attribute
     * @return the description
     */
    public String describeAttribute(String attributeName) {
        String ret = "Attribute not found";
        try {
            MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
            for (int i = 0; i < attributes.length; i++) {
                MBeanAttributeInfo attribute = attributes[i];
                if (attribute.getName().equals(attributeName)) {
                    return describeAttribute(attribute);
                }
            }
        } catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not describe attribute '" + attributeName + "'. Reason: " + e, e);
        }
        return ret;
    }

    /**
     * Names of all the operations available on the MBean.
     *
     * @return all the operations on the MBean
     */
    public Collection listOperationNames() {
        ArrayList list = new ArrayList();
        try {
            MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (int i = 0; i < operations.length; i++) {
                MBeanOperationInfo operation = operations[i];
                list.add(operation.getName());
            }
        } catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not list operation names. Reason: " + e, e);
        }
        return list;
    }

    /**
     * Description of all of the operations available on the MBean.
     *
     * @return full description of each operation on the MBean
     */
    public Collection listOperationDescriptions() {
        ArrayList list = new ArrayList();
        try {
            MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (int i = 0; i < operations.length; i++) {
                MBeanOperationInfo operation = operations[i];
                list.add(describeOperation(operation));
            }
        } catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not list operation descriptions. Reason: " + e, e);
        }
        return list;
    }

    /**
     * Get the description of the specified operation.  This returns a Collection since
     * operations can be overloaded and one operationName can have multiple forms.
     *
     * @param operationName the name of the operation to describe
     * @return Collection of operation description
     */
    public List describeOperation(String operationName) {
        ArrayList list = new ArrayList();
        try {
            MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (int i = 0; i < operations.length; i++) {
                MBeanOperationInfo operation = operations[i];
                if (operation.getName().equals(operationName)) {
                    list.add(describeOperation(operation));
                }
            }
        } catch (Exception e) {
            if (!ignoreErrors)
                throw new GroovyRuntimeException("Could not describe operations matching name '" + operationName + "'. Reason: " + e, e);
        }
        return list;
    }

    /**
     * Description of the operation.
     *
     * @param operation the operation to describe
     * @return pretty-printed description
     */
    protected String describeOperation(MBeanOperationInfo operation) {
        StringBuffer buf = new StringBuffer();
        buf.append(operation.getReturnType())
                .append(" ")
                .append(operation.getName())
                .append("(");

        MBeanParameterInfo[] params = operation.getSignature();
        for (int j = 0; j < params.length; j++) {
            MBeanParameterInfo param = params[j];
            if (j != 0) {
                buf.append(", ");
            }
            buf.append(param.getType())
                    .append(" ")
                    .append(param.getName());
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * Return an end user readable representation of the underlying MBean
     *
     * @return the user readable description
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("MBean Name:")
                .append("\n  ")
                .append(name.getCanonicalName())
                .append("\n  ");
        if (!listAttributeDescriptions().isEmpty()) {
            buf.append("\nAttributes:");
            for (Iterator iterator = listAttributeDescriptions().iterator(); iterator.hasNext();) {
                buf.append("\n  ")
                        .append((String) iterator.next());
            }
        }
        if (!listOperationDescriptions().isEmpty()) {
            buf.append("\nOperations:");
            for (Iterator iterator = listOperationDescriptions().iterator(); iterator.hasNext();) {
                buf.append("\n  ")
                        .append((String) iterator.next());
            }
        }
        return buf.toString();
    }
}
