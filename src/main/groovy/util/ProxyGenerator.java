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
import org.codehaus.groovy.runtime.memoize.LRUCache;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Classes to generate 'Proxy' objects which implement interfaces,
 * maps of closures and/or extend classes/delegates.
 *
 * @author Paul King
 * @author Guillaume Laforge
 * @author Cedric Champeau
 */
public class ProxyGenerator {
    private static final Class[] EMPTY_INTERFACE_ARRAY = new Class[0];
    private static final Map<Object,Object> EMPTY_CLOSURE_MAP = Collections.emptyMap();
    private static final Set<String> EMPTY_KEYSET = Collections.emptySet();

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

    /**
     * The adapter cache is used to cache proxy classes. When, for example, a call like:
     * map as MyClass is found, then a lookup is made into the cache to find if a suitable
     * adapter already exists. If so, then the class is reused, instead of generating a
     * new class.
     */
    private final LRUCache adapterCache = new LRUCache(16);

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

    @SuppressWarnings("unchecked")
    public GroovyObject instantiateAggregate(Map closureMap, List<Class> interfaces, Class clazz, Object[] constructorArgs) {
        Map<Object,Object> map = closureMap!=null?closureMap: EMPTY_CLOSURE_MAP;
        Class[] intfs = interfaces!=null? interfaces.toArray(new Class[interfaces.size()]): EMPTY_INTERFACE_ARRAY;
        Class base = clazz;
        if (base==null) {
            if (intfs.length>0) {
                base=intfs[0];
            } else {
                base = Object.class;
            }
        }
        Set<String> keys = map==EMPTY_CLOSURE_MAP?EMPTY_KEYSET:new HashSet<String>();
        for (Object o : map.keySet()) {
            keys.add(o.toString());
        }
        CacheKey key = new CacheKey(base, keys, intfs, emptyMethods);
        ProxyGeneratorAdapter adapter = (ProxyGeneratorAdapter) adapterCache.get(key);
        if (adapter==null) {
            adapter = new ProxyGeneratorAdapter(map, base, intfs, base.getClassLoader(), emptyMethods);
            adapterCache.put(key, adapter);
        }
        return adapter.proxy(map,constructorArgs);
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
    
    private static class CacheKey {
        private static final Comparator<Class> CLASSNAME_COMPARATOR = new Comparator<Class>() {
            public int compare(final Class o1, final Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        private final boolean emptyMethods;
        private final Set<String> methods;
        private final ClassReference baseClass;
        private final ClassReference[] interfaces;

        private CacheKey(final Class baseClass, final Set<String> methods, final Class[] interfaces, final boolean emptyMethods) {
            this.baseClass = new ClassReference(baseClass);
            this.emptyMethods = emptyMethods;
            this.interfaces = interfaces == null ? null : new ClassReference[interfaces.length];
            if (interfaces != null) {
                Arrays.sort(interfaces, CLASSNAME_COMPARATOR);
                for (int i = 0; i < interfaces.length; i++) {
                    Class anInterface = interfaces[i];
                    this.interfaces[i] = new ClassReference(anInterface);
                }
            }
            this.methods = methods;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final CacheKey cacheKey = (CacheKey) o;

            if (emptyMethods != cacheKey.emptyMethods) return false;
            if (baseClass != null ? !baseClass.equals(cacheKey.baseClass) : cacheKey.baseClass != null) return false;
            if (!Arrays.equals(interfaces, cacheKey.interfaces)) return false;
            if (methods != null ? !methods.equals(cacheKey.methods) : cacheKey.methods != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (emptyMethods ? 1 : 0);
            result = 31 * result + (methods != null ? methods.hashCode() : 0);
            result = 31 * result + (baseClass != null ? baseClass.hashCode() : 0);
            result = 31 * result + (interfaces != null ? Arrays.hashCode(interfaces) : 0);
            return result;
        }

        /**
         * A weak reference which delegates equals and hashcode to the referent.
         */
        private static class ClassReference extends WeakReference<Class> {

            public ClassReference(Class referent) {
                super(referent);
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Class thisClass = this.get();
                ClassReference that = (ClassReference) o;
                if (thisClass == null) return false;
                return thisClass.equals(that.get());
            }

            @Override
            public int hashCode() {
                Class thisClass = this.get();
                if (thisClass==null) return 0;
                return thisClass.hashCode();
            }
        }
    }

}
