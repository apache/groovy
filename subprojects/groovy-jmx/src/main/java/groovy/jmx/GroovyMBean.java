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
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

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
import java.lang.reflect.Array;
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
    private final Map<String, List<Operation>> operations = new HashMap<String, List<Operation>>();

    /**
     * Creates a facade for the named MBean.
     *
     * @param server the server connection to use
     * @param objectName the name of the target MBean
     * @throws JMException if the MBean name is invalid or metadata cannot be read
     * @throws IOException if the server connection fails
     */
    public GroovyMBean(MBeanServerConnection server, String objectName) throws JMException, IOException {
        this(server, objectName, false);
    }

    /**
     * Creates a facade for the named MBean.
     *
     * @param server the server connection to use
     * @param objectName the name of the target MBean
     * @param ignoreErrors whether access errors should be ignored
     * @throws JMException if the MBean name is invalid or metadata cannot be read
     * @throws IOException if the server connection fails
     */
    public GroovyMBean(MBeanServerConnection server, String objectName, boolean ignoreErrors) throws JMException, IOException {
        this(server, new ObjectName(objectName), ignoreErrors);
    }

    /**
     * Creates a facade for the named MBean.
     *
     * @param server the server connection to use
     * @param name the object name of the target MBean
     * @throws JMException if metadata cannot be read
     * @throws IOException if the server connection fails
     */
    public GroovyMBean(MBeanServerConnection server, ObjectName name) throws JMException, IOException {
        this(server, name, false);
    }

    /**
     * Creates a facade for the named MBean.
     *
     * @param server the server connection to use
     * @param name the object name of the target MBean
     * @param ignoreErrors whether access errors should be ignored
     * @throws JMException if metadata cannot be read
     * @throws IOException if the server connection fails
     */
    public GroovyMBean(MBeanServerConnection server, ObjectName name, boolean ignoreErrors) throws JMException, IOException {
        this.server = server;
        this.name = name;
        this.ignoreErrors = ignoreErrors;
        this.beanInfo = server.getMBeanInfo(name);

        MBeanOperationInfo[] operationInfos = beanInfo.getOperations();
        for (MBeanOperationInfo info : operationInfos) {
            String[] signature = createSignature(info);
            String operationKey = createOperationKey(info.getName(), signature.length);
            List<Operation> overloads = operations.get(operationKey);
            if (overloads == null) {
                overloads = new ArrayList<Operation>(1);
                operations.put(operationKey, overloads);
            }
            overloads.add(new Operation(info.getName(), signature));
        }
    }

    /**
     * Returns the backing server connection.
     *
     * @return the server connection
     */
    public MBeanServerConnection server() {
        return server;
    }

    /**
     * Returns the object name of the wrapped MBean.
     *
     * @return the MBean name
     */
    public ObjectName name() {
        return name;
    }

    /**
     * Returns cached metadata for the wrapped MBean.
     *
     * @return the MBean metadata
     */
    public MBeanInfo info() {
        return beanInfo;
    }

    /**
     * Reads an attribute from the wrapped MBean.
     *
     * @param property the attribute name
     * @return the attribute value
     */
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

    /**
     * Writes an attribute on the wrapped MBean.
     *
     * @param property the attribute name
     * @param value the new attribute value
     */
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

    /**
     * Invokes an operation on the wrapped MBean.
     *
     * @param method the operation name
     * @param arguments the operation arguments
     * @return the invocation result
     */
    @Override
    public Object invokeMethod(String method, Object arguments) {
        Object[] argArray;
        if (arguments instanceof Object[]) {
            argArray = (Object[]) arguments;
        } else {
            argArray = new Object[]{arguments};
        }
        String[] signature;
        Object[] callArgs = argArray;
        List<Operation> matches = operationsFor(method, argArray.length);
        if (matches == null || matches.isEmpty()) {
            return super.invokeMethod(method, arguments);
        } else if (matches.size() == 1) {
            signature = matches.get(0).signature;
        } else {
            Selection selection = select(matches, argArray);
            signature = selection.signature;
            callArgs = selection.arguments;
        }

        try {
            return server.invoke(name, method, callArgs, signature);
        }
        catch (MBeanException e) {
            throwExceptionWithTarget("Could not invoke method: " + method + ". Reason: ", e);
        }
        catch (Exception e) {
            throwException("Could not invoke method: " + method + ". Reason: ", e);
        }
        return null;
    }

    /**
     * Builds the signature array for an operation.
     *
     * @param info the operation metadata
     * @return the operation signature
     */
    protected String[] createSignature(MBeanOperationInfo info) {
        MBeanParameterInfo[] params = info.getSignature();
        String[] answer = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            answer[i] = params[i].getType();
        }
        return answer;
    }

    /**
     * Groups operations that share a name and a parameter count.
     * Overloads in that group are distinguished when the operation is invoked.
     *
     * @param operation the mbean operation name
     * @param params the number of parameters the operation supports
     * @return key for the overload group
     */
    protected String createOperationKey(String operation, int params) {
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
     * List of string representations of all the attributes on the MBean.
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
     * Description of all the operations available on the MBean.
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
    @Override
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

    private List<Operation> operationsFor(String method, int arity) {
        List<Operation> grouped = operations.get(createOperationKey(method, arity));
        if (grouped == null) {
            return null;
        }
        if (grouped.size() == 1) {
            Operation only = grouped.get(0);
            if (method.equals(only.name) && only.signature.length == arity) {
                return grouped;
            }
        }
        List<Operation> matched = new ArrayList<Operation>(grouped.size());
        for (Operation operation : grouped) {
            if (method.equals(operation.name) && operation.signature.length == arity) {
                matched.add(operation);
            }
        }
        return matched;
    }

    /**
     * Chooses an overload. Java method-invocation conversion is preferred, so a
     * short argument can still reach an int parameter. Groovy coercions such as
     * GString to String are used only when no overload is applicable that way,
     * and those arguments are converted before the server sees them. A remaining
     * tie keeps the last registered operation.
     */
    private static Selection select(List<Operation> matches, Object[] args) {
        List<Candidate> candidates = new ArrayList<Candidate>(matches.size());
        for (Operation operation : matches) {
            candidates.add(new Candidate(operation, operation.parameterTypes(args)));
        }
        List<Candidate> applicable = applicable(candidates, args, true);
        boolean coerce = false;
        if (applicable.isEmpty()) {
            applicable = applicable(candidates, args, false);
            coerce = !applicable.isEmpty();
        }
        if (applicable.isEmpty()) {
            Operation last = matches.get(matches.size() - 1);
            return new Selection(last.signature, args);
        }
        List<Candidate> best = mostSpecific(applicable, !coerce);
        best = closest(best, args);
        Candidate chosen = best.get(best.size() - 1);
        Object[] callArgs = coerce ? coerce(args, chosen.types) : args;
        return new Selection(chosen.operation.signature, callArgs);
    }

    private static List<Candidate> applicable(List<Candidate> candidates, Object[] args, boolean javaRules) {
        List<Candidate> matched = new ArrayList<Candidate>(candidates.size());
        for (Candidate candidate : candidates) {
            if (accepts(args, candidate.types, javaRules)) {
                matched.add(candidate);
            }
        }
        return matched;
    }

    private static boolean accepts(Object[] args, Class<?>[] params, boolean javaRules) {
        for (int i = 0; i < args.length; i++) {
            Class<?> param = params[i];
            Object arg = args[i];
            if (javaRules) {
                if (!isJavaAssignable(argumentClass(arg), param)) {
                    return false;
                }
            } else if (param == null || !groovyAssignable(param, arg)) {
                return false;
            }
        }
        return true;
    }

    private static boolean groovyAssignable(Class<?> param, Object arg) {
        if (arg == null) {
            return !param.isPrimitive();
        }
        return MetaClassHelper.isAssignableFrom(param, arg.getClass());
    }

    private static List<Candidate> mostSpecific(List<Candidate> applicable, boolean javaRules) {
        List<Candidate> best = new ArrayList<Candidate>(applicable.size());
        for (Candidate candidate : applicable) {
            boolean dominated = false;
            for (int i = 0; i < best.size(); ) {
                Candidate other = best.get(i);
                boolean better = isMoreSpecific(candidate.types, other.types, javaRules);
                boolean worse = isMoreSpecific(other.types, candidate.types, javaRules);
                if (better) {
                    best.remove(i);
                } else if (worse) {
                    dominated = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!dominated) {
                best.add(candidate);
            }
        }
        return best;
    }

    private static boolean isMoreSpecific(Class<?>[] left, Class<?>[] right, boolean javaRules) {
        boolean stricter = false;
        for (int i = 0; i < left.length; i++) {
            boolean leftToRight = compatible(left[i], right[i], javaRules);
            boolean rightToLeft = compatible(right[i], left[i], javaRules);
            if (!leftToRight) {
                return false;
            }
            if (!rightToLeft) {
                stricter = true;
            }
        }
        return stricter;
    }

    private static boolean compatible(Class<?> from, Class<?> to, boolean javaRules) {
        if (javaRules) {
            return isJavaAssignable(from, to);
        }
        Class<?> source = from.isPrimitive() ? box(from) : from;
        return MetaClassHelper.isAssignableFrom(to, source);
    }

    private static List<Candidate> closest(List<Candidate> best, Object[] args) {
        int bestDistance = Integer.MAX_VALUE;
        List<Candidate> closest = new ArrayList<Candidate>(best.size());
        for (Candidate candidate : best) {
            int distance = distance(args, candidate.types);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest.clear();
                closest.add(candidate);
            } else if (distance == bestDistance) {
                closest.add(candidate);
            }
        }
        return closest;
    }

    private static int distance(Object[] args, Class<?>[] params) {
        int sum = 0;
        for (int i = 0; i < args.length; i++) {
            int part = argumentDistance(argumentClass(args[i]), params[i]);
            if (part == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            sum += part;
        }
        return sum;
    }

    private static int argumentDistance(Class<?> arg, Class<?> param) {
        if (arg == null) {
            return REFERENCE_DISTANCE;
        }
        if (arg == param) {
            return 0;
        }
        Class<?> argPrimitive = asPrimitive(arg);
        Class<?> paramPrimitive = asPrimitive(param);
        if (argPrimitive != null && argPrimitive == paramPrimitive) {
            return BOX_DISTANCE;
        }
        if (!param.isPrimitive() && param.isAssignableFrom(arg)) {
            return REFERENCE_DISTANCE;
        }
        if (MetaClassHelper.isAssignableFrom(param, arg)) {
            return GROOVY_DISTANCE;
        }
        return Integer.MAX_VALUE;
    }

    private static Object[] coerce(Object[] args, Class<?>[] params) {
        Object[] copy = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (isJavaAssignable(argumentClass(args[i]), params[i])) {
                copy[i] = args[i];
            } else {
                copy[i] = DefaultTypeTransformation.castToType(args[i], params[i]);
            }
        }
        return copy;
    }

    private static boolean isJavaAssignable(Class<?> arg, Class<?> param) {
        if (param == null) {
            return false;
        }
        if (arg == null) {
            return !param.isPrimitive();
        }
        if (param.isPrimitive()) {
            Class<?> argPrimitive = asPrimitive(arg);
            if (argPrimitive == null) {
                return false;
            }
            return argPrimitive == param || wideningSteps(argPrimitive, param) >= 0;
        }
        Class<?> source = arg.isPrimitive() ? box(arg) : arg;
        return param.isAssignableFrom(source);
    }

    private static int wideningSteps(Class<?> from, Class<?> to) {
        if (from == byte.class) {
            if (to == short.class) return 1;
            if (to == int.class) return 2;
            if (to == long.class) return 3;
            if (to == float.class) return 4;
            if (to == double.class) return 5;
        } else if (from == short.class) {
            if (to == int.class) return 1;
            if (to == long.class) return 2;
            if (to == float.class) return 3;
            if (to == double.class) return 4;
        } else if (from == char.class) {
            if (to == int.class) return 1;
            if (to == long.class) return 2;
            if (to == float.class) return 3;
            if (to == double.class) return 4;
        } else if (from == int.class) {
            if (to == long.class) return 1;
            if (to == float.class) return 2;
            if (to == double.class) return 3;
        } else if (from == long.class) {
            if (to == float.class) return 1;
            if (to == double.class) return 2;
        } else if (from == float.class) {
            if (to == double.class) return 1;
        }
        return -1;
    }

    private static Class<?> asPrimitive(Class<?> type) {
        if (type.isPrimitive()) {
            return type;
        }
        return WRAPPER_TO_PRIMITIVE.get(type);
    }

    private static Class<?> argumentClass(Object arg) {
        return arg == null ? null : arg.getClass();
    }

    private static Class<?> box(Class<?> primitive) {
        return BOXED.get(primitive);
    }

    private static Class<?> loadType(String typeName) {
        Class<?> primitive = PRIMITIVE_TYPES.get(typeName);
        if (primitive != null) {
            return primitive;
        }
        if (typeName.endsWith("[]")) {
            Class<?> component = loadType(typeName.substring(0, typeName.length() - 2));
            if (component == null) {
                return null;
            }
            return Array.newInstance(component, 0).getClass();
        }
        Class<?> type = tryLoad(typeName, Thread.currentThread().getContextClassLoader());
        if (type != null) {
            return type;
        }
        return tryLoad(typeName, GroovyMBean.class.getClassLoader());
    }

    private static Class<?> tryLoad(String typeName, ClassLoader loader) {
        try {
            return Class.forName(typeName, false, loader);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private static final int BOX_DISTANCE = 1;
    private static final int REFERENCE_DISTANCE = 8;
    private static final int GROOVY_DISTANCE = 32;

    private static final Map<String, Class<?>> PRIMITIVE_TYPES = Map.of(
            "boolean", boolean.class,
            "byte", byte.class,
            "char", char.class,
            "short", short.class,
            "int", int.class,
            "long", long.class,
            "float", float.class,
            "double", double.class);

    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = Map.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class);

    private static final Map<Class<?>, Class<?>> BOXED = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class);

    private static final class Operation {
        final String name;
        final String[] signature;
        private final Class<?>[] resolved;

        Operation(String name, String[] signature) {
            this.name = name;
            this.signature = signature;
            this.resolved = new Class<?>[signature.length];
        }

        Class<?>[] parameterTypes(Object[] args) {
            Class<?>[] types = new Class<?>[signature.length];
            for (int i = 0; i < signature.length; i++) {
                types[i] = resolve(i, args[i]);
            }
            return types;
        }

        private Class<?> resolve(int index, Object sample) {
            Class<?> cached = resolved[index];
            if (cached != null) {
                return cached;
            }
            Class<?> type = loadType(signature[index]);
            if (type == null && sample != null && signature[index].equals(sample.getClass().getName())) {
                type = sample.getClass();
            }
            if (type != null) {
                resolved[index] = type;
            }
            return type;
        }
    }

    private static final class Candidate {
        final Operation operation;
        final Class<?>[] types;

        Candidate(Operation operation, Class<?>[] types) {
            this.operation = operation;
            this.types = types;
        }
    }

    private static final class Selection {
        final String[] signature;
        final Object[] arguments;

        Selection(String[] signature, Object[] arguments) {
            this.signature = signature;
            this.arguments = arguments;
        }
    }
}
