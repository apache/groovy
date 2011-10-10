/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util;

import groovy.lang.*;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.runtime.ConversionHandler;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Classes to generate 'Proxy' objects which implement interfaces,
 * maps of closures and/or extend classes/delegates.
 *
 * @author Paul King
 * @author Guillaume Laforge
 */
public class ProxyGenerator {
    public static final ProxyGenerator INSTANCE = new ProxyGenerator();

    static {
        // wrap the standard MetaClass with the delegate
        setMetaClass(GroovySystem.getMetaClassRegistry().getMetaClass(ProxyGenerator.class));
    }

    private ClassLoader override = null;
    private boolean debug = false;
    private boolean emptyMethods = false;
    private List<Method> objectMethods = getInheritedMethods(Object.class, new ArrayList<Method>());
    private List<Method> groovyObjectMethods = getInheritedMethods(GroovyObject.class, new ArrayList<Method>());

    public boolean getDebug() {
        return debug;
    }

    /**
     * Instructs <code>ProxyGenerator</code> to dump generated Groovy
     * source code to standard output during construction. This is useful
     * for debugging purposes but should be turned off in production.
     *
     * @param debug true if you want generated source to be printed
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getEmptyMethods() {
        return emptyMethods;
    }

    /**
     * Changes generated methods to have empty implementations.
     * <p/>
     * Methods in generated aggregates not supplied in a closures map or
     * base class are given 'default' implementations. The implementation
     * will normally throw an <code>UnsupportedOperationException</code>
     * but setting this boolean will leave it empty.
     *
     * @param emptyMethods true if you want generated methods to be empty
     */
    public void setEmptyMethods(boolean emptyMethods) {
        this.emptyMethods = emptyMethods;
    }

    public ClassLoader getOverride() {
        return override;
    }

    public void setOverride(ClassLoader override) {
        this.override = override;
    }

    public GroovyObject instantiateAggregateFromBaseClass(Class clazz) {
        return instantiateAggregateFromBaseClass((Map) null, clazz);
    }

    public GroovyObject instantiateAggregateFromBaseClass(Map map, Class clazz) {
        return instantiateAggregateFromBaseClass(map, clazz, null);
    }

    public GroovyObject instantiateAggregateFromBaseClass(Closure cl, Class clazz) {
        Map<String, Closure> m = new HashMap<String, Closure>();
        m.put("*", cl);
        return instantiateAggregateFromBaseClass(m, clazz, null);
    }

    public GroovyObject instantiateAggregateFromBaseClass(Class clazz, Object[] constructorArgs) {
        return instantiateAggregate(null, null, clazz, constructorArgs);
    }

    public GroovyObject instantiateAggregateFromBaseClass(Map map, Class clazz, Object[] constructorArgs) {
        return instantiateAggregate(map, null, clazz, constructorArgs);
    }

    public GroovyObject instantiateAggregateFromInterface(Class clazz) {
        return instantiateAggregateFromInterface(null, clazz);
    }

    public GroovyObject instantiateAggregateFromInterface(Map map, Class clazz) {
        List<Class> interfaces = new ArrayList<Class>();
        interfaces.add(clazz);
        return instantiateAggregate(map, interfaces);
    }

    public GroovyObject instantiateAggregate(List<Class> interfaces) {
        return instantiateAggregate(null, interfaces);
    }

    public GroovyObject instantiateAggregate(Map closureMap, List<Class> interfaces) {
        return instantiateAggregate(closureMap, interfaces, null);
    }

    public GroovyObject instantiateAggregate(Map closureMap, List<Class> interfaces, Class clazz) {
        return instantiateAggregate(closureMap, interfaces, clazz, null);
    }

    public GroovyObject instantiateAggregate(Map closureMap, List<Class> interfaces, Class clazz, Object[] constructorArgs) {
        Map map = new HashMap();
        if (closureMap != null) {
            map = closureMap;
        }
        List<Class> interfacesToImplement;
        if (interfaces == null) {
            interfacesToImplement = new ArrayList<Class>();
        } else {
            interfacesToImplement = interfaces;
        }
        Class baseClass = GroovyObjectSupport.class;
        if (clazz != null) {
            baseClass = clazz;
        }
        boolean hasArgs = constructorArgs != null && constructorArgs.length > 0;
        String name = shortName(baseClass.getName()) + "_groovyProxy";
        StringBuffer buffer = new StringBuffer();

        // add class header and fields
        buffer.append("class ").append(name);
        if (clazz != null) {
            buffer.append(" extends ").append(baseClass.getName());
        }
        for (int i = 0; i < interfacesToImplement.size(); i++) {
            Class thisInterface = interfacesToImplement.get(i);
            if (i == 0) {
                buffer.append(" implements ");
            } else {
                buffer.append(", ");
            }
            buffer.append(thisInterface.getName());
        }
        buffer.append(" {\n").append("    private closureMap\n    ");

        // add constructor
        buffer.append(name).append("(map");
        if (hasArgs) {
            buffer.append(", args");
        }
        buffer.append(") {\n");
        buffer.append("        super(");
        if (hasArgs) {
            buffer.append("*args");
        }
        buffer.append(")\n");
        buffer.append("        this.closureMap = map\n");
        buffer.append("    }\n");

        // add overwriting methods
        Map<String, Method> selectedMethods = new HashMap<String, Method>();
        List<Method> publicAndProtectedMethods = getInheritedMethods(baseClass, new ArrayList<Method>());
        boolean closureIndicator = map.containsKey("*");
        for (Method method : publicAndProtectedMethods) {
            if (method.getName().indexOf('$') != -1
                    || Modifier.isFinal(method.getModifiers())
                    || (!"toString".equals(method.getName()) && ConversionHandler.isCoreObjectMethod(method))
                    || containsEquivalentMethod(selectedMethods.values(), method))
                continue;
            if (map.containsKey(method.getName()) || closureIndicator) {
                selectedMethods.put(method.getName(), method);
                addOverridingMapCall(buffer, method, closureIndicator);
            } else if (Modifier.isAbstract(method.getModifiers())) {
                selectedMethods.put(method.getName(), method);
                addMapOrDummyCall(map, buffer, method);
            }
        }

        // add interface methods
        List<Method> interfaceMethods = new ArrayList<Method>();
        for (Class thisInterface : interfacesToImplement) {
            getInheritedMethods(thisInterface, interfaceMethods);
        }
        for (Method method : interfaceMethods) {
            if (!containsEquivalentMethod(publicAndProtectedMethods, method)) {
                selectedMethods.put(method.getName(), method);
                addMapOrDummyCall(map, buffer, method);
            }
        }

        // add leftover methods from the map
        for (Object o : map.keySet()) {
            String methodName = (String) o;
            if (methodName.indexOf('$') != -1 || methodName.indexOf('*') != -1)
                continue;
            if (selectedMethods.keySet().contains(methodName)) continue;
            addNewMapCall(buffer, methodName);
        }

        // end class

        buffer.append("}\n").append("new ").append(name);
        buffer.append("(map");
        if (hasArgs) {
            buffer.append(", constructorArgs");
        }
        buffer.append(")");

        Binding binding = new Binding();
        binding.setVariable("map", map);
        binding.setVariable("constructorArgs", constructorArgs);
        ClassLoader cl = override != null ? override : baseClass.getClassLoader();
        if (clazz == null && interfacesToImplement.size() > 0) {
            Class c = interfacesToImplement.get(0);
            cl = c.getClassLoader();
        }
        GroovyShell shell = new GroovyShell(cl, binding);
        if (debug)
            System.out.println("proxy source:\n------------------\n" + buffer.toString() + "\n------------------");
        try {
            return (GroovyObject) shell.evaluate(buffer.toString());
        } catch (MultipleCompilationErrorsException err) {
            throw new GroovyRuntimeException("Error creating proxy: " + err.getMessage());
        }
    }

    public GroovyObject instantiateDelegate(Object delegate) {
        return instantiateDelegate(null, delegate);
    }

    public GroovyObject instantiateDelegate(List<Class> interfaces, Object delegate) {
        return instantiateDelegate(null, interfaces, delegate);
    }

    public GroovyObject instantiateDelegate(Map closureMap, List<Class> interfaces, Object delegate) {
        return instantiateDelegateWithBaseClass(closureMap, interfaces, delegate, null);
    }

    public GroovyObject instantiateDelegateWithBaseClass(Map closureMap, List<Class> interfaces, Object delegate) {
        return instantiateDelegateWithBaseClass(closureMap, interfaces, delegate, delegate.getClass());
    }

    public GroovyObject instantiateDelegateWithBaseClass(Map closureMap, List<Class> interfaces, Object delegate, Class baseClass) {
        String name = shortName(delegate.getClass().getName()) + "_delegateProxy";
        return instantiateDelegateWithBaseClass(closureMap, interfaces, delegate, baseClass, name);
    }

    public GroovyObject instantiateDelegateWithBaseClass(Map closureMap, List<Class> interfaces, Object delegate, Class baseClass, String name) {
        Map map = new HashMap();
        if (closureMap != null) {
            map = closureMap;
        }
        List<String> selectedMethods = new ArrayList<String>();
        List<Class> interfacesToImplement;
        if (interfaces == null) {
            interfacesToImplement = new ArrayList<Class>();
        } else {
            interfacesToImplement = interfaces;
        }
        StringBuffer buffer = new StringBuffer();

        // add class header and fields
        buffer.append("import org.codehaus.groovy.runtime.InvokerHelper\nclass ").append(name);
        if (baseClass != null) {
            buffer.append(" extends ").append(baseClass.getName());
        }

        for (int i = 0; i < interfacesToImplement.size(); i++) {
            Class thisInterface = interfacesToImplement.get(i);
            if (i == 0) {
                buffer.append(" implements ");
            } else {
                buffer.append(", ");
            }
            buffer.append(thisInterface.getName());
        }
        buffer.append(" {\n").append("    private delegate\n").append("    private closureMap\n    ");

        // add constructor
        buffer.append(name).append("(map, delegate) {\n");
        buffer.append("        this.closureMap = map\n");
        buffer.append("        this.delegate = delegate\n");
        buffer.append("    }\n");

        // add interface methods
        List<Method> interfaceMethods = new ArrayList<Method>();
        for (Class thisInterface : interfacesToImplement) {
            getInheritedMethods(thisInterface, interfaceMethods);
        }
        for (Method method : interfaceMethods) {
            if (!containsEquivalentMethod(objectMethods, method) &&
                    !containsEquivalentMethod(groovyObjectMethods, method)) {
                selectedMethods.add(method.getName());
                addWrappedCall(buffer, method, map);
            }
        }
        List<Method> additionalMethods = getInheritedMethods(delegate.getClass(), new ArrayList<Method>());
        for (Method method : additionalMethods) {
            if (method.getName().indexOf('$') != -1)
                continue;
            if (!containsEquivalentMethod(interfaceMethods, method) &&
                    !containsEquivalentMethod(objectMethods, method) &&
                    !containsEquivalentMethod(groovyObjectMethods, method)) {
                selectedMethods.add(method.getName());
                addWrappedCall(buffer, method, map);
            }
        }

        // add leftover methods from the map
        for (Object o : map.keySet()) {
            String methodName = (String) o;
            if (selectedMethods.contains(methodName)) continue;
            addNewMapCall(buffer, methodName);
        }

        // end class

        buffer.append("}\n").append("new ").append(name);
        buffer.append("(map, delegate)");

        Binding binding = new Binding();
        binding.setVariable("map", map);
        binding.setVariable("delegate", delegate);
        ClassLoader cl = override != null ? override : delegate.getClass().getClassLoader();
        GroovyShell shell = new GroovyShell(cl, binding);
        if (debug)
            System.out.println("proxy source:\n------------------\n" + buffer.toString() + "\n------------------");
        try {
            return (GroovyObject) shell.evaluate(buffer.toString());
        } catch (MultipleCompilationErrorsException err) {
            throw new GroovyRuntimeException("Error creating proxy: " + err.getMessage());
        }
    }

    private void addWrappedCall(StringBuffer buffer, Method method, Map map) {
        if (map.containsKey(method.getName())) {
            addOverridingMapCall(buffer, method, false);
        } else {
            Class[] parameterTypes = addMethodPrefix(buffer, method);
            addWrappedMethodBody(buffer, method, parameterTypes);
            addMethodSuffix(buffer);
        }
    }

    private boolean containsEquivalentMethod(Collection<Method> publicAndProtectedMethods, Method candidate) {
        for (Method method : publicAndProtectedMethods) {
            if (candidate.getName().equals(method.getName()) &&
                    candidate.getReturnType().equals(method.getReturnType()) &&
                    hasMatchingParameterTypes(candidate, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMatchingParameterTypes(Method method, Method candidate) {
        Class[] candidateParamTypes = candidate.getParameterTypes();
        Class[] methodParamTypes = method.getParameterTypes();
        if (candidateParamTypes.length != methodParamTypes.length) return false;
        for (int i = 0; i < methodParamTypes.length; i++) {
            if (!candidateParamTypes[i].equals(methodParamTypes[i])) return false;
        }
        return true;
    }

    private List<Method> getInheritedMethods(Class baseClass, List<Method> methods) {
        methods.addAll(DefaultGroovyMethods.toList(baseClass.getMethods()));
        Class currentClass = baseClass;
        while (currentClass != null) {
            Method[] protectedMethods = currentClass.getDeclaredMethods();
            for (Method method : protectedMethods) {
                if (method.getName().indexOf('$') != -1)
                    continue;
                if (Modifier.isProtected(method.getModifiers()) && !containsEquivalentMethod(methods, method))
                    methods.add(method);
            }
            currentClass = currentClass.getSuperclass();
        }
        return methods;
    }

    private void addNewMapCall(StringBuffer buffer, String methodName) {
        buffer.append("    def ").append(methodName).append("(Object[] args) {\n")
                .append("        this.@closureMap['").append(methodName).append("'] (*args)\n    }\n");
    }

    private void addOverridingMapCall(StringBuffer buffer, Method method, boolean closureIndicator) {
        Class[] parameterTypes = addMethodPrefix(buffer, method);
        addMethodBody(buffer, closureIndicator ? "*" : method.getName(), parameterTypes);
        addMethodSuffix(buffer);
    }

    private void addMapOrDummyCall(Map map, StringBuffer buffer, Method method) {
        Class[] parameterTypes = addMethodPrefix(buffer, method);
        if (map.containsKey(method.getName())) {
            addMethodBody(buffer, method.getName(), parameterTypes);
        } else if (!emptyMethods) {
            addUnsupportedBody(buffer);
        }
        addMethodSuffix(buffer);
    }

    private void addUnsupportedBody(StringBuffer buffer) {
        buffer.append("throw new UnsupportedOperationException()");
    }

    private Class[] addMethodPrefix(StringBuffer buffer, Method method) {
        buffer.append("    ").append(getSimpleName(method.getReturnType()))
                .append(" ").append(method.getName()).append("(");
        Class[] parameterTypes = method.getParameterTypes();
        for (int parameterTypeIndex = 0; parameterTypeIndex < parameterTypes.length; parameterTypeIndex++) {
            Class parameter = parameterTypes[parameterTypeIndex];
            if (parameterTypeIndex != 0) {
                buffer.append(", ");
            }
            buffer.append(getSimpleName(parameter)).append(" ")
                    .append("p").append(parameterTypeIndex);
        }
        buffer.append(") { ");
        return parameterTypes;
    }

    private void addMethodBody(StringBuffer buffer, String method, Class[] parameterTypes) {
        buffer.append("this.@closureMap['").append(method).append("'] (");
        for (int j = 0; j < parameterTypes.length; j++) {
            if (j != 0) {
                buffer.append(", ");
            }
            buffer.append("p").append(j);
        }
        buffer.append(")");
    }

    private void addWrappedMethodBody(StringBuffer buffer, Method method, Class[] parameterTypes) {
        buffer.append("\n        Object[] args = [");
        for (int j = 0; j < parameterTypes.length; j++) {
            if (j != 0) {
                buffer.append(", ");
            }
            buffer.append("p").append(j);
        }
        buffer.append("]\n        ");
        buffer.append("InvokerHelper.invokeMethod(delegate, '").append(method.getName()).append("', args)\n");
    }

    private void addMethodSuffix(StringBuffer buffer) {
        buffer.append("    }\n");
    }

    /**
     * TODO once we switch to Java 1.5 bt default, use Class#getSimpleName() directly
     *
     * @param c the class of which we want the readable simple name
     * @return the readable simple name
     */
    public String getSimpleName(Class c) {
        if (c.isArray()) {
            int dimension = 0;
            Class componentClass = c;
            while (componentClass.isArray()) {
                componentClass = componentClass.getComponentType();
                dimension++;
            }
            return componentClass.getName().replaceAll("\\$", "\\.") +
                    DefaultGroovyMethods.multiply("[]", dimension);
        } else {
            return c.getName().replaceAll("\\$", "\\.");
        }
    }

    public String shortName(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1) return name;
        return name.substring(index + 1, name.length());
    }

    private static void setMetaClass(final MetaClass metaClass) {
        final MetaClass newMetaClass = new DelegatingMetaClass(metaClass) {
            @Override
            public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
                return InvokerHelper.invokeMethod(INSTANCE, methodName, arguments);
            }
        };
        GroovySystem.getMetaClassRegistry().setMetaClass(ProxyGenerator.class, newMetaClass);
    }

}
