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
package groovy.util;

import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ProxyGeneratorAdapter;
import org.codehaus.groovy.runtime.memoize.LRUCache;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.codehaus.groovy.transform.trait.Traits;

import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Classes to generate 'Proxy' objects which implement interfaces,
 * maps of closures and/or extend classes/delegates.
 */
public class ProxyGenerator {
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Class[] EMPTY_INTERFACE_ARRAY = EMPTY_CLASS_ARRAY;
    private static final Map<Object,Object> EMPTY_CLOSURE_MAP = Collections.emptyMap();
    private static final Set<String> EMPTY_KEYSET = Collections.emptySet();

    static {
        // wrap the standard MetaClass with the delegate
        setMetaClass(GroovySystem.getMetaClassRegistry().getMetaClass(ProxyGenerator.class));
    }

    private ClassLoader override = null;
    private boolean debug = false;
    private boolean emptyMethods = false;

    private static final Integer GROOVY_ADAPTER_CACHE_DEFAULT_SIZE = Integer.getInteger("groovy.adapter.cache.default.size", 64);

    public static final ProxyGenerator INSTANCE = new ProxyGenerator(); // TODO should we make ProxyGenerator singleton?

    /**
     * The adapter cache is used to cache proxy classes. When, for example, a call like:
     * map as MyClass is found, then a lookup is made into the cache to find if a suitable
     * adapter already exists. If so, then the class is reused, instead of generating a
     * new class.
     */
    private final LRUCache adapterCache = new LRUCache(GROOVY_ADAPTER_CACHE_DEFAULT_SIZE);

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
     * <p>
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
        if (clazz != null && Modifier.isFinal(clazz.getModifiers())) {
            throw new GroovyCastException("Cannot coerce a map to class " + clazz.getName() + " because it is a final class");
        }
        Map<Object,Object> map = closureMap != null ? closureMap : EMPTY_CLOSURE_MAP;
        ProxyGeneratorAdapter adapter = createAdapter(map, interfaces, null, clazz);

        return adapter.proxy(map, constructorArgs);
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
        return instantiateDelegateWithBaseClass(closureMap, interfaces, delegate, baseClass, null);
    }

    /**
     * Creates a proxy with a delegate object.
     *
     * @param closureMap the closure for methods not handled by the delegate
     * @param interfaces interfaces to be implemented
     * @param delegate the delegate object
     * @param baseClass the base class
     * @param name the name of the proxy, unused, but kept for compatibility with previous versions of Groovy.
     * @return a proxy object implementing the specified interfaces, and delegating to the provided object
     */
    @SuppressWarnings("unchecked")
    public GroovyObject instantiateDelegateWithBaseClass(Map closureMap, List<Class> interfaces, Object delegate, Class baseClass, String name) {
        Map<Object,Object> map = closureMap != null ? closureMap : EMPTY_CLOSURE_MAP;
        ProxyGeneratorAdapter adapter = createAdapter(map, interfaces, delegate.getClass(), baseClass);

        return adapter.delegatingProxy(delegate, map, (Object[])null);
    }

    private ProxyGeneratorAdapter createAdapter(Map closureMap, List<Class> interfaces, Class delegateClass, Class baseClass) {
        // According to https://shipilev.net/blog/2016/arrays-wisdom-ancients/#_conclusion
        // toArray(new T[0]) seems faster, safer, and contractually cleaner, and therefore should be the default choice now.
        Class[] intfs = interfaces != null ? interfaces.toArray(EMPTY_CLASS_ARRAY) : EMPTY_INTERFACE_ARRAY;
        Class base = baseClass;
        if (base == null) {
            if (intfs.length > 0) {
                base = intfs[0];
            } else {
                base = Object.class;
            }
        }
        Set<String> keys = closureMap == EMPTY_CLOSURE_MAP ? EMPTY_KEYSET : new HashSet<String>();
        for (Object o : closureMap.keySet()) {
            keys.add(o.toString());
        }
        boolean useDelegate = null != delegateClass;
        CacheKey key = new CacheKey(base, useDelegate ? delegateClass : Object.class, keys, intfs, emptyMethods, useDelegate);
        final Class b = base;

        return (ProxyGeneratorAdapter) adapterCache.getAndPut(
                key,
                k -> new ProxyGeneratorAdapter(closureMap, b, intfs, useDelegate
                        ? delegateClass.getClassLoader()
                        : b.getClassLoader(), emptyMethods, useDelegate ? delegateClass : null
                )
        );
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
    
    private static final class CacheKey {
        private static final Comparator<Class> INTERFACE_COMPARATOR = (o1, o2) -> {
            // Traits order *must* be preserved
            // See GROOVY-7285
            if (Traits.isTrait(o1)) return -1;
            if (Traits.isTrait(o2)) return 1;
            return o1.getName().compareTo(o2.getName());
        };
        private final boolean emptyMethods;
        private final boolean useDelegate;
        private final Set<String> methods;
        private final ClassReference delegateClass;
        private final ClassReference baseClass;
        private final ClassReference[] interfaces;

        private CacheKey(final Class baseClass, final Class delegateClass, final Set<String> methods, final Class[] interfaces, final boolean emptyMethods, final boolean useDelegate) {
            this.useDelegate = useDelegate;
            this.baseClass = new ClassReference(baseClass);
            this.delegateClass = new ClassReference(delegateClass);
            this.emptyMethods = emptyMethods;
            this.interfaces = interfaces == null ? null : new ClassReference[interfaces.length];
            if (interfaces != null) {
                Class[] interfacesCopy = new Class[interfaces.length];
                System.arraycopy(interfaces, 0, interfacesCopy, 0, interfaces.length);
                Arrays.sort(interfacesCopy, INTERFACE_COMPARATOR);
                for (int i = 0; i < interfacesCopy.length; i++) {
                    Class anInterface = interfacesCopy[i];
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
            if (useDelegate != cacheKey.useDelegate) return false;
            if (!Objects.equals(baseClass, cacheKey.baseClass)) return false;
            if (!Objects.equals(delegateClass, cacheKey.delegateClass)) return false;
            if (!Arrays.equals(interfaces, cacheKey.interfaces)) return false;
            if (!Objects.equals(methods, cacheKey.methods)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (emptyMethods ? 1 : 0);
            result = 31 * result + (useDelegate ? 1 : 0);
            result = 31 * result + (methods != null ? methods.hashCode() : 0);
            result = 31 * result + (baseClass != null ? baseClass.hashCode() : 0);
            result = 31 * result + (delegateClass != null ? delegateClass.hashCode() : 0);
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
                if (thisClass == null) return 0;
                return thisClass.hashCode();
            }
        }
    }

}
