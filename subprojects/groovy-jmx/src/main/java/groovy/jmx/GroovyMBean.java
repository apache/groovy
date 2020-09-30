/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.jmx;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A GroovyObject facade for an underlying MBean which acts like a normal
 * groovy object but which is actually implemented via
 * an underlying JMX MBean.
 * Properties and normal method invocations
 * delegate to the MBeanServer to the actual MBean.
 */
public class GroovyMBean extends GroovyObjectSupport {
    private final MBeanServerConnection server;
    private final ObjectName name;
    private MBeanInfo beanInfo;
    private final boolean ignoreErrors;
    private final Map<String, String[]> operations = new HashMap<String, String[]>();

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
        for (MBeanOperationInfo info : operationInfos) {
            String signature[] = createSignature(info);
            // Construct a simplistic key to support overloaded operations on the MBean.
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

    @Override
    public Object getProperty(String property) {
        try {
            return server.getAttribute(name, property);
        }
        catch (MBeanException e) {
            throwExceptionWithTarget("Could not access property: " + property + ". Reason: ", e);
        }
        catch (Exception e) {
            if (!ignoreErrors)
            throwException("Could not access property: " + property + ". Reason: ", e);
        }
        return null;
    }

    @Override
    public void setProperty(String property, Object value) {
        try {
            server.setAttribute(name, new Attribute(property, value));
        }
        catch (MBeanException e) {
            throwExceptionWithTarget("Could not set property: " + property + ". Reason: ", e);
        }
        catch (Exception e) {
            throwException("Could not set property: " + property + ". Reason: ", e);
        }
    }

    @Override
    public Object invokeMethod(String method, Object arguments) {
        Object[] argArray;
        if (arguments instanceof Object[]) {
            argArray = (Object[]) arguments;
        } else {
            argArray = new Object[]{arguments};
        }
        // Locate the specific method based on the name and number of parameters
        String operationKey = createOperationKey(method, argArray.length);
        String[] signature = operations.get(operationKey);

        if (signature != null) {
            try {
                return server.invoke(name, method, argArray, signature);
            }
            catch (MBeanException e) {
                throwExceptionWithTarget("Could not invoke method: " + method + ". Reason: ", e);
            }
            catch (Exception e) {
                throwException("Could not invoke method: " + method + ". Reason: ", e);
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
        // but should distinguish between reordered params while allowing normal
        // type coercions to be honored
        return operation + "_" + params;
    }

    /**
     * List of the names of each of the attributes on the MBean
     *
     * @return list of attribute names
     */
    public Collection<String> listAttributeNames() {
        List<String> list = new ArrayList<String>();
        try {
            MBeanAttributeInfo[] attrs = beanInfo.getAttributes();
            for (MBeanAttributeInfo attr : attrs) {
                list.add(attr.getName());
            }
        } catch (Exception e) {
            throwException("Could not list attribute names. Reason: ", e);
        }
        return list;
    }

    /**
     * The values of each of the attributes on the MBean
     *
     * @return list of values of each attribute
     */
    public List<String> listAttributeValues() {
        List<String> list = new ArrayList<String>();
        Collection<String> names = listAttributeNames();
        for (String name : names) {
            try {
                Object val = this.getProperty(name);
                if (val != null) {
                    list.add(name + " : " + val.toString());
                }
            } catch (Exception e) {
                throwException("Could not list attribute values. Reason: ", e);
            }
        }
        return list;
    }

    /**
     * List of string representations of all of the attributes on the MBean.
     *
     * @return list of descriptions of each attribute on the mbean
     */
    public Collection<String> listAttributeDescriptions() {
        List<String> list = new ArrayList<String>();
        try {
            MBeanAttributeInfo[] attrs = beanInfo.getAttributes();
            for (MBeanAttributeInfo attr : attrs) {
                list.add(describeAttribute(attr));
            }
        } catch (Exception e) {
            throwException("Could not list attribute descriptions. Reason: ", e);
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
        StringBuilder buf = new StringBuilder();
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
            for (MBeanAttributeInfo attribute : attributes) {
                if (attribute.getName().equals(attributeName)) {
                    return describeAttribute(attribute);
                }
            }
        } catch (Exception e) {
            throwException("Could not describe attribute '" + attributeName + "'. Reason: ", e);
        }
        return ret;
    }

    /**
     * Names of all the operations available on the MBean.
     *
     * @return all the operations on the MBean
     */
    public Collection<String> listOperationNames() {
        List<String> list = new ArrayList<String>();
        try {
            MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (MBeanOperationInfo operation : operations) {
                list.add(operation.getName());
            }
        } catch (Exception e) {
            throwException("Could not list operation names. Reason: ", e);
        }
        return list;
    }

    /**
     * Description of all of the operations available on the MBean.
     *
     * @return full description of each operation on the MBean
     */
    public Collection<String> listOperationDescriptions() {
        List<String> list = new ArrayList<String>();
        try {
            MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (MBeanOperationInfo operation : operations) {
                list.add(describeOperation(operation));
            }
        } catch (Exception e) {
            throwException("Could not list operation descriptions. Reason: ", e);
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
    public List<String> describeOperation(String operationName) {
        List<String> list = new ArrayList<String>();
        try {
            MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (MBeanOperationInfo operation : operations) {
                if (operation.getName().equals(operationName)) {
                    list.add(describeOperation(operation));
                }
            }
        } catch (Exception e) {
            throwException("Could not describe operations matching name '" + operationName + "'. Reason: ", e);
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
        StringBuilder buf = new StringBuilder();
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
        StringBuilder buf = new StringBuilder();
        buf.append("MBean Name:")
                .append("\n  ")
                .append(name.getCanonicalName())
                .append("\n  ");
        if (!listAttributeDescriptions().isEmpty()) {
            buf.append("\nAttributes:");
            for (String attrDesc : listAttributeDescriptions()) {
                buf.append("\n  ").append(attrDesc);
            }
        }
        if (!listOperationDescriptions().isEmpty()) {
            buf.append("\nOperations:");
            for (String attrDesc : listOperationDescriptions()) {
                buf.append("\n  ").append(attrDesc);
            }
        }
        return buf.toString();
    }

    private void throwException(String m, Exception e) {
        if (!ignoreErrors) {
            throw new GroovyRuntimeException(m + e, e);
        }
    }

    private void throwExceptionWithTarget(String m, MBeanException e) {
        if (!ignoreErrors) {
            throw new GroovyRuntimeException(m + e, e.getTargetException());
        }
    }
}
