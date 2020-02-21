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
package org.codehaus.groovy.vmplugin.v9;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.vmplugin.v8.Java8;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Additional Java 9 based functions will be added here as needed.
 */
public class Java9 extends Java8 {
    @Override
    public Map<String, Set<String>> getDefaultImportClasses(String[] packageNames) {
        Map<String, Set<String>> result = new LinkedHashMap<>();

        List<String> javaPns = new ArrayList<>(4);
        List<String> groovyPns = new ArrayList<>(4);
        for (String prefix : packageNames) {
            String pn = prefix.substring(0, prefix.length() - 1).replace('.', '/');

            if (pn.startsWith("java/")) {
                javaPns.add(pn);
            } else if (pn.startsWith("groovy/")) {
                groovyPns.add(pn);
            } else {
                throw new GroovyBugError("unexpected package: " + pn);
            }
        }

        result.putAll(doFindClasses(URI.create("jrt:/modules/java.base/"), "java", javaPns));

        try {
            URI gsLocation = DefaultGroovyMethods.getLocation(GroovySystem.class).toURI();
            result.putAll(doFindClasses(gsLocation, "groovy", groovyPns));
        } catch (Exception e) {
            System.err.println("[WARNING] Failed to get default imported groovy classes: " + e.getMessage());
        }

        return result;
    }

    private static Map<String, Set<String>> doFindClasses(URI uri, String packageName, List<String> defaultPackageNames) {
        Map<String, Set<String>> result = ClassFinder.find(uri, packageName, true)
                .entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(defaultPackageNames::contains))
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream()
                                        .filter(e -> defaultPackageNames.contains(e))
                                        .map(e -> e.replace('/', '.') + ".")
                                        .collect(Collectors.toSet())
                        )
                );
        return result;
    }

    private static class LookupHolder {
        private static final Method PRIVATE_LOOKUP;
        private static final Constructor<MethodHandles.Lookup> LOOKUP_Constructor;

        static {
            Constructor<MethodHandles.Lookup> lookup = null;
            Method privateLookup = null;
            try { // java 9
                privateLookup = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            } catch (final NoSuchMethodException | RuntimeException e) { // java 8 or fallback if anything else goes wrong
                try {
                    lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                    if (!lookup.isAccessible()) {
                        ReflectionUtils.trySetAccessible(lookup);
                    }
                } catch (final NoSuchMethodException ex) {
                    throw new IllegalStateException("Incompatible JVM", e);
                }
            }
            PRIVATE_LOOKUP = privateLookup;
            LOOKUP_Constructor = lookup;
        }
    }

    private static Constructor<MethodHandles.Lookup> getLookupConstructor() {
        return LookupHolder.LOOKUP_Constructor;
    }

    private static Method getPrivateLookup() {
        return LookupHolder.PRIVATE_LOOKUP;
    }

    public static MethodHandles.Lookup of(final Class<?> declaringClass) {
        try {
            final Method privateLookup = getPrivateLookup();
            if (privateLookup != null) {
                return (MethodHandles.Lookup) privateLookup.invoke(null, declaringClass, MethodHandles.lookup());
            }
            return getLookupConstructor().newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).in(declaringClass);
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvocationTargetException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    @Override
    public int getVersion() {
        return 9;
    }

    @Override
    public Object getInvokeSpecialHandle(Method method, Object receiver) {
        final Class<?> receiverType = receiver.getClass();
        try {
            return of(receiverType).unreflectSpecial(method, receiverType).bindTo(receiver);
        } catch (ReflectiveOperationException e) {
            return super.getInvokeSpecialHandle(method, receiver);
        }
    }

    /**
     * This method may be used by a caller in class C to check whether to enable access to a member of declaring class D successfully
     * if {@link Java8#checkCanSetAccessible(java.lang.reflect.AccessibleObject, java.lang.Class)} returns true and any of the following hold:
     * <p>
     * 1) C and D are in the same module.
     * 2) The member is public and D is public in a package that the module containing D exports to at least the module containing C.
     * 3) The member is protected static, D is public in a package that the module containing D exports to at least the module containing C, and C is a subclass of D.
     * 4) D is in a package that the module containing D opens to at least the module containing C. All packages in unnamed and open modules are open to all modules and so this method always succeeds when D is in an unnamed or open module.
     *
     * @param accessibleObject the accessible object to check
     * @param callerClass           the callerClass to invoke {@code setAccessible}
     * @return the check result
     */
    public boolean checkCanSetAccessible(AccessibleObject accessibleObject, Class<?> callerClass) {

        if (!super.checkCanSetAccessible(accessibleObject, callerClass)) return false;

        if (callerClass == MethodHandle.class) {
            throw new IllegalCallerException();   // should not happen
        }

        if (!(accessibleObject instanceof Member)) {
            throw new IllegalArgumentException("accessibleObject should be a member of type: " + accessibleObject);   // should not happen
        }

        Member member = (Member) accessibleObject;
        Class<?> declaringClass = member.getDeclaringClass();

        Module callerModule = callerClass.getModule();
        Module declaringModule = declaringClass.getModule();

        if (callerModule == declaringModule) return true;
        if (callerModule == Object.class.getModule()) return true;
        if (!declaringModule.isNamed()) return true;

        int modifiers = member.getModifiers();

        return checkAccessible(callerClass, declaringClass, modifiers, true);
    }

    @Override
    public boolean trySetAccessible(AccessibleObject ao) {
        return ao.trySetAccessible();
    }

    @Override
    public MetaMethod transformMetaMethod(MetaClass metaClass, MetaMethod metaMethod, Class<?> caller) {
        if (!(metaMethod instanceof CachedMethod)) {
            return metaMethod;
        }

        CachedMethod cachedMethod = (CachedMethod) metaMethod;
        CachedClass methodDeclaringClass = cachedMethod.getDeclaringClass();

        if (null == methodDeclaringClass) {
            return metaMethod;
        }

        if (null == caller) {
            caller = ReflectionUtils.class; // "set accessible" are done via `org.codehaus.groovy.reflection.ReflectionUtils` as shown in warnings
        }

        return getOrTransformMetaMethod(metaClass, caller, cachedMethod);
    }

    private CachedMethod getOrTransformMetaMethod(MetaClass metaClass, Class<?> caller, CachedMethod cachedMethod) {
        CachedMethod transformedMethod = cachedMethod.getTransformedMethod();
        if (null != transformedMethod) {
            return transformedMethod;
        }

        transformedMethod = doTransformMetaMethod(metaClass, cachedMethod, caller);
        cachedMethod.setTransformedMethod(transformedMethod);

        return transformedMethod;
    }

    private CachedMethod doTransformMetaMethod(MetaClass metaClass, CachedMethod metaMethod, Class<?> caller) {
        CachedClass methodDeclaringClass = metaMethod.getDeclaringClass();
        Class<?> declaringClass = methodDeclaringClass.getTheClass();
        int methodModifiers = metaMethod.getModifiers();

        // if caller can access the method,
        // no need to transform the meta method
        if (checkAccessible(caller, declaringClass, methodModifiers, false)) {
            return metaMethod;
        }

        Class<?>[] params = metaMethod.getPT();
        Class<?> theClass = metaClass.getTheClass();
        if (declaringClass == theClass) {
            if (BigInteger.class == theClass) {
                CachedMethod bigIntegerMetaMethod = transformBigIntegerMetaMethod(metaMethod, params);
                if (bigIntegerMetaMethod != metaMethod) {
                    return bigIntegerMetaMethod;
                }
            }

            // GROOVY-9081 "3) Access public members of private class", e.g. Collections.unmodifiableMap([:]).toString()
            // try to find the visible method from its superclasses
            List<Class<?>> classList = findSuperclasses(theClass);
            classList.add(0, theClass);

            for (Class<?> sc : classList) {
                Optional<CachedMethod> optionalMetaMethod = getAccessibleMetaMethod(metaMethod, params, caller, sc, true);
                if (optionalMetaMethod.isPresent()) {
                    return optionalMetaMethod.get();
                }
            }

            return metaMethod;
        } else if (declaringClass.isAssignableFrom(theClass)) {
            // if caller can not access the method,
            // try to find the corresponding method in its derived class
            // GROOVY-9081 Sub-class derives the protected members from public class, "Invoke the members on the sub class instances"
            // e.g. StringBuilder sb = new StringBuilder(); sb.setLength(0);
            // `setLength` is the method of `AbstractStringBuilder`, which is `package-private`
            Optional<CachedMethod> optionalMetaMethod = getAccessibleMetaMethod(metaMethod, params, caller, theClass, false);
            if (optionalMetaMethod.isPresent()) {
                return optionalMetaMethod.get();
            }
        }

        return metaMethod;
    }

    private static CachedMethod transformBigIntegerMetaMethod(CachedMethod metaMethod, Class<?>[] params) {
        if (1 == params.length && MULTIPLY.equals(metaMethod.getName())) {
            Class<?> param = params[0];
            if (Long.class == param || long.class == param
                    || Integer.class == param || int.class == param
                    || Short.class == param || short.class == param) {
                return new CachedMethod(BigIntegerMultiplyMethodHolder.MULTIPLY_METHOD);
            }
        }

        return metaMethod;
    }

    private Optional<CachedMethod> getAccessibleMetaMethod(CachedMethod metaMethod, Class<?>[] params, Class<?> caller, Class<?> sc, boolean declared) {
        List<CachedMethod> metaMethodList = getMetaMethods(metaMethod, params, sc, declared);
        for (CachedMethod mm : metaMethodList) {
            if (checkAccessible(caller, mm.getDeclaringClass().getTheClass(), mm.getModifiers(), false)) {
                return Optional.of(mm);
            }
        }
        return Optional.empty();
    }

    private static List<CachedMethod> getMetaMethods(CachedMethod metaMethod, Class<?>[] params, Class<?> sc, boolean declared) {
        String metaMethodName = metaMethod.getName();
        List<Method> optionalMethodList = declared
                                            ? ReflectionUtils.getDeclaredMethods(sc, metaMethodName, params)
                                            : ReflectionUtils.getMethods(sc, metaMethodName, params);
        return optionalMethodList.stream().map(CachedMethod::new).collect(Collectors.toList());
    }

    @Override
    public boolean checkAccessible(Class<?> callerClass, Class<?> declaringClass, int memberModifiers, boolean allowIllegalAccess) {
        Module callerModule = callerClass.getModule();
        Module declaringModule = declaringClass.getModule();
        String pn = declaringClass.getPackageName();

        boolean unnamedModuleAccessNamedModule = !callerModule.isNamed() && declaringModule.isNamed();
        boolean toCheckIllegalAccess = !allowIllegalAccess && unnamedModuleAccessNamedModule;

        // class is public and package is exported to callerClass
        boolean isClassPublic = Modifier.isPublic(declaringClass.getModifiers());
        if (isClassPublic && declaringModule.isExported(pn, callerModule)) {
            // member is public
            if (Modifier.isPublic(memberModifiers)) {
                return !(toCheckIllegalAccess && isExportedForIllegalAccess(declaringModule, pn));
            }

            // member is protected-static
            if (Modifier.isProtected(memberModifiers)
                    && Modifier.isStatic(memberModifiers)
                    && isSubclassOf(callerClass, declaringClass)) {
                return !(toCheckIllegalAccess && isExportedForIllegalAccess(declaringModule, pn));
            }
        }

        // package is open to callerClass
        if (declaringModule.isOpen(pn, callerModule)) {
            return !(toCheckIllegalAccess && isOpenedForIllegalAccess(declaringModule, pn));
        }

        return false;
    }

    private static boolean isExportedForIllegalAccess(Module declaringModule, String pn) {
        return concealedPackageList(declaringModule).contains(pn);
    }

    private static boolean isOpenedForIllegalAccess(Module declaringModule, String pn) {
        if (isExportedForIllegalAccess(declaringModule, pn)) return true;
        return exportedPackageList(declaringModule).contains(pn);
    }

    private static boolean isSubclassOf(Class<?> queryClass, Class<?> ofClass) {
        while (queryClass != null) {
            if (queryClass == ofClass) {
                return true;
            }
            queryClass = queryClass.getSuperclass();
        }
        return false;
    }

    private static List<Class<?>> findSuperclasses(Class<?> clazz) {
        List<Class<?>> result = new LinkedList<>();

        for (Class<?> c = clazz.getSuperclass(); null != c; c = c.getSuperclass()) {
            result.add(c);
        }

        return result;
    }

    private static Set<String> concealedPackageList(Module module) {
        return CONCEALED_PACKAGES_TO_OPEN.computeIfAbsent(module.getName(), m -> new HashSet<>());
    }

    private static Set<String> exportedPackageList(Module module) {
        return EXPORTED_PACKAGES_TO_OPEN.computeIfAbsent(module.getName(), m -> new HashSet<>());
    }

    private static final Map<String, Set<String>> CONCEALED_PACKAGES_TO_OPEN;
    private static final Map<String, Set<String>> EXPORTED_PACKAGES_TO_OPEN;

    static {
        Tuple2<Map<String, Set<String>>, Map<String, Set<String>>> tuple2 = findConcealedAndExportedPackagesToOpen();
        CONCEALED_PACKAGES_TO_OPEN = tuple2.getV1();
        EXPORTED_PACKAGES_TO_OPEN = tuple2.getV2();
    }

    private static Tuple2<Map<String, Set<String>>, Map<String, Set<String>>> findConcealedAndExportedPackagesToOpen() {
        ModuleFinder finder = ModuleFinder.ofSystem();

        Map<String, ModuleDescriptor> map = new HashMap<>();
        finder.findAll().stream()
                .map(ModuleReference::descriptor)
                .forEach(md -> md.packages().forEach(pn -> map.putIfAbsent(pn, md)));

        final Map<String, Set<String>> concealedPackagesToOpen = new HashMap<>();
        final Map<String, Set<String>> exportedPackagesToOpen = new HashMap<>();

        Arrays.stream(JAVA8_PACKAGES())
                .forEach(pn -> {
                    ModuleDescriptor descriptor = map.get(pn);
                    if (descriptor != null && !isOpen(descriptor, pn)) {
                        String name = descriptor.name();
                        if (isExported(descriptor, pn)) {
                            exportedPackagesToOpen.computeIfAbsent(name,
                                    k -> new HashSet<>()).add(pn);
                        } else {
                            concealedPackagesToOpen.computeIfAbsent(name,
                                    k -> new HashSet<>()).add(pn);
                        }
                    }
                });

        return Tuple.tuple(concealedPackagesToOpen, exportedPackagesToOpen);
    }

    private static boolean isExported(ModuleDescriptor descriptor, String pn) {
        return descriptor.exports()
                .stream()
                .anyMatch(e -> e.source().equals(pn) && !e.isQualified());
    }

    private static boolean isOpen(ModuleDescriptor descriptor, String pn) {
        return descriptor.opens()
                .stream()
                .anyMatch(e -> e.source().equals(pn) && !e.isQualified());
    }

    private static final String MULTIPLY = "multiply";
    private static class BigIntegerMultiplyMethodHolder {
        private static final Method MULTIPLY_METHOD;
        static {
            try {
                MULTIPLY_METHOD = BigInteger.class.getDeclaredMethod(MULTIPLY, BigInteger.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new GroovyBugError("Failed to find " + MULTIPLY + " method of BigInteger", e);
            }
        }
    }

    private static String[] JAVA8_PACKAGES() {
        return new String[] {
                "apple.applescript",
                "apple.laf",
                "apple.launcher",
                "apple.security",
                "com.apple.concurrent",
                "com.apple.eawt",
                "com.apple.eawt.event",
                "com.apple.eio",
                "com.apple.laf",
                "com.apple.laf.resources",
                "com.oracle.jrockit.jfr",
                "com.oracle.jrockit.jfr.client",
                "com.oracle.jrockit.jfr.management",
                "com.oracle.security.ucrypto",
                "com.oracle.util",
                "com.oracle.webservices.internal.api",
                "com.oracle.webservices.internal.api.databinding",
                "com.oracle.webservices.internal.api.message",
                "com.oracle.webservices.internal.impl.encoding",
                "com.oracle.webservices.internal.impl.internalspi.encoding",
                "com.oracle.xmlns.internal.webservices.jaxws_databinding",
                "com.sun.accessibility.internal.resources",
                "com.sun.activation.registries",
                "com.sun.awt",
                "com.sun.beans",
                "com.sun.beans.decoder",
                "com.sun.beans.editors",
                "com.sun.beans.finder",
                "com.sun.beans.infos",
                "com.sun.beans.util",
                "com.sun.codemodel.internal",
                "com.sun.codemodel.internal.fmt",
                "com.sun.codemodel.internal.util",
                "com.sun.codemodel.internal.writer",
                "com.sun.corba.se.impl.activation",
                "com.sun.corba.se.impl.copyobject",
                "com.sun.corba.se.impl.corba",
                "com.sun.corba.se.impl.dynamicany",
                "com.sun.corba.se.impl.encoding",
                "com.sun.corba.se.impl.interceptors",
                "com.sun.corba.se.impl.io",
                "com.sun.corba.se.impl.ior",
                "com.sun.corba.se.impl.ior.iiop",
                "com.sun.corba.se.impl.javax.rmi",
                "com.sun.corba.se.impl.javax.rmi.CORBA",
                "com.sun.corba.se.impl.legacy.connection",
                "com.sun.corba.se.impl.logging",
                "com.sun.corba.se.impl.monitoring",
                "com.sun.corba.se.impl.naming.cosnaming",
                "com.sun.corba.se.impl.naming.namingutil",
                "com.sun.corba.se.impl.naming.pcosnaming",
                "com.sun.corba.se.impl.oa",
                "com.sun.corba.se.impl.oa.poa",
                "com.sun.corba.se.impl.oa.toa",
                "com.sun.corba.se.impl.orb",
                "com.sun.corba.se.impl.orbutil",
                "com.sun.corba.se.impl.orbutil.closure",
                "com.sun.corba.se.impl.orbutil.concurrent",
                "com.sun.corba.se.impl.orbutil.fsm",
                "com.sun.corba.se.impl.orbutil.graph",
                "com.sun.corba.se.impl.orbutil.threadpool",
                "com.sun.corba.se.impl.presentation.rmi",
                "com.sun.corba.se.impl.protocol",
                "com.sun.corba.se.impl.protocol.giopmsgheaders",
                "com.sun.corba.se.impl.resolver",
                "com.sun.corba.se.impl.transport",
                "com.sun.corba.se.impl.util",
                "com.sun.corba.se.internal.CosNaming",
                "com.sun.corba.se.internal.Interceptors",
                "com.sun.corba.se.internal.POA",
                "com.sun.corba.se.internal.corba",
                "com.sun.corba.se.internal.iiop",
                "com.sun.corba.se.org.omg.CORBA",
                "com.sun.corba.se.pept.broker",
                "com.sun.corba.se.pept.encoding",
                "com.sun.corba.se.pept.protocol",
                "com.sun.corba.se.pept.transport",
                "com.sun.corba.se.spi.activation",
                "com.sun.corba.se.spi.activation.InitialNameServicePackage",
                "com.sun.corba.se.spi.activation.LocatorPackage",
                "com.sun.corba.se.spi.activation.RepositoryPackage",
                "com.sun.corba.se.spi.copyobject",
                "com.sun.corba.se.spi.encoding",
                "com.sun.corba.se.spi.extension",
                "com.sun.corba.se.spi.ior",
                "com.sun.corba.se.spi.ior.iiop",
                "com.sun.corba.se.spi.legacy.connection",
                "com.sun.corba.se.spi.legacy.interceptor",
                "com.sun.corba.se.spi.logging",
                "com.sun.corba.se.spi.monitoring",
                "com.sun.corba.se.spi.oa",
                "com.sun.corba.se.spi.orb",
                "com.sun.corba.se.spi.orbutil.closure",
                "com.sun.corba.se.spi.orbutil.fsm",
                "com.sun.corba.se.spi.orbutil.proxy",
                "com.sun.corba.se.spi.orbutil.threadpool",
                "com.sun.corba.se.spi.presentation.rmi",
                "com.sun.corba.se.spi.protocol",
                "com.sun.corba.se.spi.resolver",
                "com.sun.corba.se.spi.servicecontext",
                "com.sun.corba.se.spi.transport",
                "com.sun.crypto.provider",
                "com.sun.demo.jvmti.hprof",
                "com.sun.deploy.uitoolkit.impl.fx",
                "com.sun.deploy.uitoolkit.impl.fx.ui",
                "com.sun.deploy.uitoolkit.impl.fx.ui.resources",
                "com.sun.glass.events",
                "com.sun.glass.events.mac",
                "com.sun.glass.ui",
                "com.sun.glass.ui.delegate",
                "com.sun.glass.ui.gtk",
                "com.sun.glass.ui.mac",
                "com.sun.glass.ui.win",
                "com.sun.glass.utils",
                "com.sun.image.codec.jpeg",
                "com.sun.imageio.plugins.bmp",
                "com.sun.imageio.plugins.common",
                "com.sun.imageio.plugins.gif",
                "com.sun.imageio.plugins.jpeg",
                "com.sun.imageio.plugins.png",
                "com.sun.imageio.plugins.wbmp",
                "com.sun.imageio.spi",
                "com.sun.imageio.stream",
                "com.sun.istack.internal",
                "com.sun.istack.internal.localization",
                "com.sun.istack.internal.logging",
                "com.sun.istack.internal.tools",
                "com.sun.jarsigner",
                "com.sun.java.accessibility",
                "com.sun.java.accessibility.util",
                "com.sun.java.accessibility.util.java.awt",
                "com.sun.java.browser.dom",
                "com.sun.java.browser.net",
                "com.sun.java.swing",
                "com.sun.java.swing.plaf.gtk",
                "com.sun.java.swing.plaf.gtk.resources",
                "com.sun.java.swing.plaf.motif",
                "com.sun.java.swing.plaf.motif.resources",
                "com.sun.java.swing.plaf.nimbus",
                "com.sun.java.swing.plaf.windows",
                "com.sun.java.swing.plaf.windows.resources",
                "com.sun.java.util.jar.pack",
                "com.sun.java_cup.internal.runtime",
                "com.sun.javadoc",
                "com.sun.javafx",
                "com.sun.javafx.animation",
                "com.sun.javafx.applet",
                "com.sun.javafx.application",
                "com.sun.javafx.beans",
                "com.sun.javafx.beans.event",
                "com.sun.javafx.binding",
                "com.sun.javafx.charts",
                "com.sun.javafx.collections",
                "com.sun.javafx.css",
                "com.sun.javafx.css.converters",
                "com.sun.javafx.css.parser",
                "com.sun.javafx.cursor",
                "com.sun.javafx.effect",
                "com.sun.javafx.embed",
                "com.sun.javafx.event",
                "com.sun.javafx.font",
                "com.sun.javafx.font.coretext",
                "com.sun.javafx.font.directwrite",
                "com.sun.javafx.font.freetype",
                "com.sun.javafx.font.t2k",
                "com.sun.javafx.fxml",
                "com.sun.javafx.fxml.builder",
                "com.sun.javafx.fxml.expression",
                "com.sun.javafx.geom",
                "com.sun.javafx.geom.transform",
                "com.sun.javafx.geometry",
                "com.sun.javafx.iio",
                "com.sun.javafx.iio.bmp",
                "com.sun.javafx.iio.common",
                "com.sun.javafx.iio.gif",
                "com.sun.javafx.iio.ios",
                "com.sun.javafx.iio.jpeg",
                "com.sun.javafx.iio.png",
                "com.sun.javafx.image",
                "com.sun.javafx.image.impl",
                "com.sun.javafx.jmx",
                "com.sun.javafx.logging",
                "com.sun.javafx.media",
                "com.sun.javafx.menu",
                "com.sun.javafx.perf",
                "com.sun.javafx.print",
                "com.sun.javafx.property",
                "com.sun.javafx.property.adapter",
                "com.sun.javafx.robot",
                "com.sun.javafx.robot.impl",
                "com.sun.javafx.runtime",
                "com.sun.javafx.runtime.async",
                "com.sun.javafx.runtime.eula",
                "com.sun.javafx.scene",
                "com.sun.javafx.scene.control",
                "com.sun.javafx.scene.control.behavior",
                "com.sun.javafx.scene.control.skin",
                "com.sun.javafx.scene.control.skin.resources",
                "com.sun.javafx.scene.input",
                "com.sun.javafx.scene.layout.region",
                "com.sun.javafx.scene.paint",
                "com.sun.javafx.scene.shape",
                "com.sun.javafx.scene.text",
                "com.sun.javafx.scene.transform",
                "com.sun.javafx.scene.traversal",
                "com.sun.javafx.scene.web",
                "com.sun.javafx.scene.web.behavior",
                "com.sun.javafx.scene.web.skin",
                "com.sun.javafx.sg.prism",
                "com.sun.javafx.sg.prism.web",
                "com.sun.javafx.stage",
                "com.sun.javafx.text",
                "com.sun.javafx.tk",
                "com.sun.javafx.tk.quantum",
                "com.sun.javafx.util",
                "com.sun.javafx.webkit",
                "com.sun.javafx.webkit.drt",
                "com.sun.javafx.webkit.prism",
                "com.sun.javafx.webkit.prism.theme",
                "com.sun.javafx.webkit.theme",
                "com.sun.jdi",
                "com.sun.jdi.connect",
                "com.sun.jdi.connect.spi",
                "com.sun.jdi.event",
                "com.sun.jdi.request",
                "com.sun.jmx.defaults",
                "com.sun.jmx.interceptor",
                "com.sun.jmx.mbeanserver",
                "com.sun.jmx.remote.internal",
                "com.sun.jmx.remote.protocol.iiop",
                "com.sun.jmx.remote.protocol.rmi",
                "com.sun.jmx.remote.security",
                "com.sun.jmx.remote.util",
                "com.sun.jmx.snmp",
                "com.sun.jmx.snmp.IPAcl",
                "com.sun.jmx.snmp.agent",
                "com.sun.jmx.snmp.daemon",
                "com.sun.jmx.snmp.defaults",
                "com.sun.jmx.snmp.internal",
                "com.sun.jmx.snmp.mpm",
                "com.sun.jmx.snmp.tasks",
                "com.sun.jndi.cosnaming",
                "com.sun.jndi.dns",
                "com.sun.jndi.ldap",
                "com.sun.jndi.ldap.ext",
                "com.sun.jndi.ldap.pool",
                "com.sun.jndi.ldap.sasl",
                "com.sun.jndi.rmi.registry",
                "com.sun.jndi.toolkit.corba",
                "com.sun.jndi.toolkit.ctx",
                "com.sun.jndi.toolkit.dir",
                "com.sun.jndi.toolkit.url",
                "com.sun.jndi.url.corbaname",
                "com.sun.jndi.url.dns",
                "com.sun.jndi.url.iiop",
                "com.sun.jndi.url.iiopname",
                "com.sun.jndi.url.ldap",
                "com.sun.jndi.url.ldaps",
                "com.sun.jndi.url.rmi",
                "com.sun.management",
                "com.sun.management.jmx",
                "com.sun.media.jfxmedia",
                "com.sun.media.jfxmedia.control",
                "com.sun.media.jfxmedia.effects",
                "com.sun.media.jfxmedia.events",
                "com.sun.media.jfxmedia.locator",
                "com.sun.media.jfxmedia.logging",
                "com.sun.media.jfxmedia.track",
                "com.sun.media.jfxmediaimpl",
                "com.sun.media.jfxmediaimpl.platform",
                "com.sun.media.jfxmediaimpl.platform.gstreamer",
                "com.sun.media.jfxmediaimpl.platform.ios",
                "com.sun.media.jfxmediaimpl.platform.java",
                "com.sun.media.jfxmediaimpl.platform.osx",
                "com.sun.media.sound",
                "com.sun.naming.internal",
                "com.sun.net.httpserver",
                "com.sun.net.httpserver.spi",
                "com.sun.net.ssl",
                "com.sun.net.ssl.internal.ssl",
                "com.sun.net.ssl.internal.www.protocol.https",
                "com.sun.nio.file",
                "com.sun.nio.sctp",
                "com.sun.nio.zipfs",
                "com.sun.openpisces",
                "com.sun.org.apache.bcel.internal",
                "com.sun.org.apache.bcel.internal.classfile",
                "com.sun.org.apache.bcel.internal.generic",
                "com.sun.org.apache.bcel.internal.util",
                "com.sun.org.apache.regexp.internal",
                "com.sun.org.apache.xalan.internal",
                "com.sun.org.apache.xalan.internal.extensions",
                "com.sun.org.apache.xalan.internal.lib",
                "com.sun.org.apache.xalan.internal.res",
                "com.sun.org.apache.xalan.internal.templates",
                "com.sun.org.apache.xalan.internal.utils",
                "com.sun.org.apache.xalan.internal.xslt",
                "com.sun.org.apache.xalan.internal.xsltc",
                "com.sun.org.apache.xalan.internal.xsltc.cmdline",
                "com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt",
                "com.sun.org.apache.xalan.internal.xsltc.compiler",
                "com.sun.org.apache.xalan.internal.xsltc.compiler.util",
                "com.sun.org.apache.xalan.internal.xsltc.dom",
                "com.sun.org.apache.xalan.internal.xsltc.runtime",
                "com.sun.org.apache.xalan.internal.xsltc.runtime.output",
                "com.sun.org.apache.xalan.internal.xsltc.trax",
                "com.sun.org.apache.xalan.internal.xsltc.util",
                "com.sun.org.apache.xerces.internal.dom",
                "com.sun.org.apache.xerces.internal.dom.events",
                "com.sun.org.apache.xerces.internal.impl",
                "com.sun.org.apache.xerces.internal.impl.dtd",
                "com.sun.org.apache.xerces.internal.impl.dtd.models",
                "com.sun.org.apache.xerces.internal.impl.dv",
                "com.sun.org.apache.xerces.internal.impl.dv.dtd",
                "com.sun.org.apache.xerces.internal.impl.dv.util",
                "com.sun.org.apache.xerces.internal.impl.dv.xs",
                "com.sun.org.apache.xerces.internal.impl.io",
                "com.sun.org.apache.xerces.internal.impl.msg",
                "com.sun.org.apache.xerces.internal.impl.validation",
                "com.sun.org.apache.xerces.internal.impl.xpath",
                "com.sun.org.apache.xerces.internal.impl.xpath.regex",
                "com.sun.org.apache.xerces.internal.impl.xs",
                "com.sun.org.apache.xerces.internal.impl.xs.identity",
                "com.sun.org.apache.xerces.internal.impl.xs.models",
                "com.sun.org.apache.xerces.internal.impl.xs.opti",
                "com.sun.org.apache.xerces.internal.impl.xs.traversers",
                "com.sun.org.apache.xerces.internal.impl.xs.util",
                "com.sun.org.apache.xerces.internal.jaxp",
                "com.sun.org.apache.xerces.internal.jaxp.datatype",
                "com.sun.org.apache.xerces.internal.jaxp.validation",
                "com.sun.org.apache.xerces.internal.parsers",
                "com.sun.org.apache.xerces.internal.util",
                "com.sun.org.apache.xerces.internal.utils",
                "com.sun.org.apache.xerces.internal.xinclude",
                "com.sun.org.apache.xerces.internal.xni",
                "com.sun.org.apache.xerces.internal.xni.grammars",
                "com.sun.org.apache.xerces.internal.xni.parser",
                "com.sun.org.apache.xerces.internal.xpointer",
                "com.sun.org.apache.xerces.internal.xs",
                "com.sun.org.apache.xerces.internal.xs.datatypes",
                "com.sun.org.apache.xml.internal.dtm",
                "com.sun.org.apache.xml.internal.dtm.ref",
                "com.sun.org.apache.xml.internal.dtm.ref.dom2dtm",
                "com.sun.org.apache.xml.internal.dtm.ref.sax2dtm",
                "com.sun.org.apache.xml.internal.res",
                "com.sun.org.apache.xml.internal.resolver",
                "com.sun.org.apache.xml.internal.resolver.helpers",
                "com.sun.org.apache.xml.internal.resolver.readers",
                "com.sun.org.apache.xml.internal.resolver.tools",
                "com.sun.org.apache.xml.internal.security",
                "com.sun.org.apache.xml.internal.security.algorithms",
                "com.sun.org.apache.xml.internal.security.algorithms.implementations",
                "com.sun.org.apache.xml.internal.security.c14n",
                "com.sun.org.apache.xml.internal.security.c14n.helper",
                "com.sun.org.apache.xml.internal.security.c14n.implementations",
                "com.sun.org.apache.xml.internal.security.encryption",
                "com.sun.org.apache.xml.internal.security.exceptions",
                "com.sun.org.apache.xml.internal.security.keys",
                "com.sun.org.apache.xml.internal.security.keys.content",
                "com.sun.org.apache.xml.internal.security.keys.content.keyvalues",
                "com.sun.org.apache.xml.internal.security.keys.content.x509",
                "com.sun.org.apache.xml.internal.security.keys.keyresolver",
                "com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations",
                "com.sun.org.apache.xml.internal.security.keys.storage",
                "com.sun.org.apache.xml.internal.security.keys.storage.implementations",
                "com.sun.org.apache.xml.internal.security.signature",
                "com.sun.org.apache.xml.internal.security.signature.reference",
                "com.sun.org.apache.xml.internal.security.transforms",
                "com.sun.org.apache.xml.internal.security.transforms.implementations",
                "com.sun.org.apache.xml.internal.security.transforms.params",
                "com.sun.org.apache.xml.internal.security.utils",
                "com.sun.org.apache.xml.internal.security.utils.resolver",
                "com.sun.org.apache.xml.internal.security.utils.resolver.implementations",
                "com.sun.org.apache.xml.internal.serialize",
                "com.sun.org.apache.xml.internal.serializer",
                "com.sun.org.apache.xml.internal.serializer.utils",
                "com.sun.org.apache.xml.internal.utils",
                "com.sun.org.apache.xml.internal.utils.res",
                "com.sun.org.apache.xpath.internal",
                "com.sun.org.apache.xpath.internal.axes",
                "com.sun.org.apache.xpath.internal.compiler",
                "com.sun.org.apache.xpath.internal.domapi",
                "com.sun.org.apache.xpath.internal.functions",
                "com.sun.org.apache.xpath.internal.jaxp",
                "com.sun.org.apache.xpath.internal.objects",
                "com.sun.org.apache.xpath.internal.operations",
                "com.sun.org.apache.xpath.internal.patterns",
                "com.sun.org.apache.xpath.internal.res",
                "com.sun.org.glassfish.external.amx",
                "com.sun.org.glassfish.external.arc",
                "com.sun.org.glassfish.external.probe.provider",
                "com.sun.org.glassfish.external.probe.provider.annotations",
                "com.sun.org.glassfish.external.statistics",
                "com.sun.org.glassfish.external.statistics.annotations",
                "com.sun.org.glassfish.external.statistics.impl",
                "com.sun.org.glassfish.gmbal",
                "com.sun.org.glassfish.gmbal.util",
                "com.sun.org.omg.CORBA",
                "com.sun.org.omg.CORBA.ValueDefPackage",
                "com.sun.org.omg.CORBA.portable",
                "com.sun.org.omg.SendingContext",
                "com.sun.org.omg.SendingContext.CodeBasePackage",
                "com.sun.pisces",
                "com.sun.prism",
                "com.sun.prism.d3d",
                "com.sun.prism.es2",
                "com.sun.prism.image",
                "com.sun.prism.impl",
                "com.sun.prism.impl.packrect",
                "com.sun.prism.impl.paint",
                "com.sun.prism.impl.ps",
                "com.sun.prism.impl.shape",
                "com.sun.prism.j2d",
                "com.sun.prism.j2d.paint",
                "com.sun.prism.j2d.print",
                "com.sun.prism.paint",
                "com.sun.prism.ps",
                "com.sun.prism.shader",
                "com.sun.prism.shape",
                "com.sun.prism.sw",
                "com.sun.rmi.rmid",
                "com.sun.rowset",
                "com.sun.rowset.internal",
                "com.sun.rowset.providers",
                "com.sun.scenario",
                "com.sun.scenario.animation",
                "com.sun.scenario.animation.shared",
                "com.sun.scenario.effect",
                "com.sun.scenario.effect.impl",
                "com.sun.scenario.effect.impl.es2",
                "com.sun.scenario.effect.impl.hw",
                "com.sun.scenario.effect.impl.hw.d3d",
                "com.sun.scenario.effect.impl.prism",
                "com.sun.scenario.effect.impl.prism.ps",
                "com.sun.scenario.effect.impl.prism.sw",
                "com.sun.scenario.effect.impl.state",
                "com.sun.scenario.effect.impl.sw",
                "com.sun.scenario.effect.impl.sw.java",
                "com.sun.scenario.effect.impl.sw.sse",
                "com.sun.scenario.effect.light",
                "com.sun.security.auth",
                "com.sun.security.auth.callback",
                "com.sun.security.auth.login",
                "com.sun.security.auth.module",
                "com.sun.security.cert.internal.x509",
                "com.sun.security.jgss",
                "com.sun.security.ntlm",
                "com.sun.security.sasl",
                "com.sun.security.sasl.digest",
                "com.sun.security.sasl.gsskerb",
                "com.sun.security.sasl.ntlm",
                "com.sun.security.sasl.util",
                "com.sun.source.doctree",
                "com.sun.source.tree",
                "com.sun.source.util",
                "com.sun.swing.internal.plaf.basic.resources",
                "com.sun.swing.internal.plaf.metal.resources",
                "com.sun.swing.internal.plaf.synth.resources",
                "com.sun.tools.attach",
                "com.sun.tools.attach.spi",
                "com.sun.tools.classfile",
                "com.sun.tools.corba.se.idl",
                "com.sun.tools.corba.se.idl.constExpr",
                "com.sun.tools.corba.se.idl.som.cff",
                "com.sun.tools.corba.se.idl.som.idlemit",
                "com.sun.tools.corba.se.idl.toJavaPortable",
                "com.sun.tools.doclets",
                "com.sun.tools.doclets.formats.html",
                "com.sun.tools.doclets.formats.html.markup",
                "com.sun.tools.doclets.formats.html.resources",
                "com.sun.tools.doclets.internal.toolkit",
                "com.sun.tools.doclets.internal.toolkit.builders",
                "com.sun.tools.doclets.internal.toolkit.resources",
                "com.sun.tools.doclets.internal.toolkit.taglets",
                "com.sun.tools.doclets.internal.toolkit.util",
                "com.sun.tools.doclets.internal.toolkit.util.links",
                "com.sun.tools.doclets.standard",
                "com.sun.tools.doclint",
                "com.sun.tools.doclint.resources",
                "com.sun.tools.example.debug.expr",
                "com.sun.tools.example.debug.tty",
                "com.sun.tools.extcheck",
                "com.sun.tools.hat",
                "com.sun.tools.hat.internal.model",
                "com.sun.tools.hat.internal.oql",
                "com.sun.tools.hat.internal.parser",
                "com.sun.tools.hat.internal.server",
                "com.sun.tools.hat.internal.util",
                "com.sun.tools.internal.jxc",
                "com.sun.tools.internal.jxc.ap",
                "com.sun.tools.internal.jxc.api",
                "com.sun.tools.internal.jxc.api.impl.j2s",
                "com.sun.tools.internal.jxc.gen.config",
                "com.sun.tools.internal.jxc.model.nav",
                "com.sun.tools.internal.ws",
                "com.sun.tools.internal.ws.api",
                "com.sun.tools.internal.ws.api.wsdl",
                "com.sun.tools.internal.ws.processor",
                "com.sun.tools.internal.ws.processor.generator",
                "com.sun.tools.internal.ws.processor.model",
                "com.sun.tools.internal.ws.processor.model.exporter",
                "com.sun.tools.internal.ws.processor.model.java",
                "com.sun.tools.internal.ws.processor.model.jaxb",
                "com.sun.tools.internal.ws.processor.modeler",
                "com.sun.tools.internal.ws.processor.modeler.annotation",
                "com.sun.tools.internal.ws.processor.modeler.wsdl",
                "com.sun.tools.internal.ws.processor.util",
                "com.sun.tools.internal.ws.resources",
                "com.sun.tools.internal.ws.spi",
                "com.sun.tools.internal.ws.util",
                "com.sun.tools.internal.ws.util.xml",
                "com.sun.tools.internal.ws.wscompile",
                "com.sun.tools.internal.ws.wscompile.plugin.at_generated",
                "com.sun.tools.internal.ws.wsdl.document",
                "com.sun.tools.internal.ws.wsdl.document.http",
                "com.sun.tools.internal.ws.wsdl.document.jaxws",
                "com.sun.tools.internal.ws.wsdl.document.mime",
                "com.sun.tools.internal.ws.wsdl.document.schema",
                "com.sun.tools.internal.ws.wsdl.document.soap",
                "com.sun.tools.internal.ws.wsdl.framework",
                "com.sun.tools.internal.ws.wsdl.parser",
                "com.sun.tools.internal.xjc",
                "com.sun.tools.internal.xjc.addon.accessors",
                "com.sun.tools.internal.xjc.addon.at_generated",
                "com.sun.tools.internal.xjc.addon.code_injector",
                "com.sun.tools.internal.xjc.addon.episode",
                "com.sun.tools.internal.xjc.addon.locator",
                "com.sun.tools.internal.xjc.addon.sync",
                "com.sun.tools.internal.xjc.api",
                "com.sun.tools.internal.xjc.api.impl.s2j",
                "com.sun.tools.internal.xjc.api.util",
                "com.sun.tools.internal.xjc.generator.annotation.spec",
                "com.sun.tools.internal.xjc.generator.bean",
                "com.sun.tools.internal.xjc.generator.bean.field",
                "com.sun.tools.internal.xjc.generator.util",
                "com.sun.tools.internal.xjc.model",
                "com.sun.tools.internal.xjc.model.nav",
                "com.sun.tools.internal.xjc.outline",
                "com.sun.tools.internal.xjc.reader",
                "com.sun.tools.internal.xjc.reader.dtd",
                "com.sun.tools.internal.xjc.reader.dtd.bindinfo",
                "com.sun.tools.internal.xjc.reader.gbind",
                "com.sun.tools.internal.xjc.reader.internalizer",
                "com.sun.tools.internal.xjc.reader.relaxng",
                "com.sun.tools.internal.xjc.reader.xmlschema",
                "com.sun.tools.internal.xjc.reader.xmlschema.bindinfo",
                "com.sun.tools.internal.xjc.reader.xmlschema.ct",
                "com.sun.tools.internal.xjc.reader.xmlschema.parser",
                "com.sun.tools.internal.xjc.runtime",
                "com.sun.tools.internal.xjc.util",
                "com.sun.tools.internal.xjc.writer",
                "com.sun.tools.javac",
                "com.sun.tools.javac.api",
                "com.sun.tools.javac.code",
                "com.sun.tools.javac.comp",
                "com.sun.tools.javac.file",
                "com.sun.tools.javac.jvm",
                "com.sun.tools.javac.main",
                "com.sun.tools.javac.model",
                "com.sun.tools.javac.nio",
                "com.sun.tools.javac.parser",
                "com.sun.tools.javac.processing",
                "com.sun.tools.javac.resources",
                "com.sun.tools.javac.sym",
                "com.sun.tools.javac.tree",
                "com.sun.tools.javac.util",
                "com.sun.tools.javadoc",
                "com.sun.tools.javadoc.api",
                "com.sun.tools.javadoc.resources",
                "com.sun.tools.javah",
                "com.sun.tools.javah.resources",
                "com.sun.tools.javap",
                "com.sun.tools.javap.resources",
                "com.sun.tools.jconsole",
                "com.sun.tools.jdeps",
                "com.sun.tools.jdeps.resources",
                "com.sun.tools.jdi",
                "com.sun.tools.jdi.resources",
                "com.sun.tools.script.shell",
                "com.sun.tracing",
                "com.sun.tracing.dtrace",
                "com.sun.webkit",
                "com.sun.webkit.dom",
                "com.sun.webkit.event",
                "com.sun.webkit.graphics",
                "com.sun.webkit.network",
                "com.sun.webkit.network.about",
                "com.sun.webkit.network.data",
                "com.sun.webkit.perf",
                "com.sun.webkit.plugin",
                "com.sun.webkit.text",
                "com.sun.xml.internal.bind",
                "com.sun.xml.internal.bind.annotation",
                "com.sun.xml.internal.bind.api",
                "com.sun.xml.internal.bind.api.impl",
                "com.sun.xml.internal.bind.marshaller",
                "com.sun.xml.internal.bind.unmarshaller",
                "com.sun.xml.internal.bind.util",
                "com.sun.xml.internal.bind.v2",
                "com.sun.xml.internal.bind.v2.bytecode",
                "com.sun.xml.internal.bind.v2.model.annotation",
                "com.sun.xml.internal.bind.v2.model.core",
                "com.sun.xml.internal.bind.v2.model.impl",
                "com.sun.xml.internal.bind.v2.model.nav",
                "com.sun.xml.internal.bind.v2.model.runtime",
                "com.sun.xml.internal.bind.v2.model.util",
                "com.sun.xml.internal.bind.v2.runtime",
                "com.sun.xml.internal.bind.v2.runtime.output",
                "com.sun.xml.internal.bind.v2.runtime.property",
                "com.sun.xml.internal.bind.v2.runtime.reflect",
                "com.sun.xml.internal.bind.v2.runtime.reflect.opt",
                "com.sun.xml.internal.bind.v2.runtime.unmarshaller",
                "com.sun.xml.internal.bind.v2.schemagen",
                "com.sun.xml.internal.bind.v2.schemagen.episode",
                "com.sun.xml.internal.bind.v2.schemagen.xmlschema",
                "com.sun.xml.internal.bind.v2.util",
                "com.sun.xml.internal.dtdparser",
                "com.sun.xml.internal.fastinfoset",
                "com.sun.xml.internal.fastinfoset.algorithm",
                "com.sun.xml.internal.fastinfoset.alphabet",
                "com.sun.xml.internal.fastinfoset.dom",
                "com.sun.xml.internal.fastinfoset.org.apache.xerces.util",
                "com.sun.xml.internal.fastinfoset.sax",
                "com.sun.xml.internal.fastinfoset.stax",
                "com.sun.xml.internal.fastinfoset.stax.events",
                "com.sun.xml.internal.fastinfoset.stax.factory",
                "com.sun.xml.internal.fastinfoset.stax.util",
                "com.sun.xml.internal.fastinfoset.tools",
                "com.sun.xml.internal.fastinfoset.util",
                "com.sun.xml.internal.fastinfoset.vocab",
                "com.sun.xml.internal.messaging.saaj",
                "com.sun.xml.internal.messaging.saaj.client.p2p",
                "com.sun.xml.internal.messaging.saaj.packaging.mime",
                "com.sun.xml.internal.messaging.saaj.packaging.mime.internet",
                "com.sun.xml.internal.messaging.saaj.packaging.mime.util",
                "com.sun.xml.internal.messaging.saaj.soap",
                "com.sun.xml.internal.messaging.saaj.soap.dynamic",
                "com.sun.xml.internal.messaging.saaj.soap.impl",
                "com.sun.xml.internal.messaging.saaj.soap.name",
                "com.sun.xml.internal.messaging.saaj.soap.ver1_1",
                "com.sun.xml.internal.messaging.saaj.soap.ver1_2",
                "com.sun.xml.internal.messaging.saaj.util",
                "com.sun.xml.internal.messaging.saaj.util.transform",
                "com.sun.xml.internal.org.jvnet.fastinfoset",
                "com.sun.xml.internal.org.jvnet.fastinfoset.sax",
                "com.sun.xml.internal.org.jvnet.fastinfoset.sax.helpers",
                "com.sun.xml.internal.org.jvnet.fastinfoset.stax",
                "com.sun.xml.internal.org.jvnet.mimepull",
                "com.sun.xml.internal.org.jvnet.staxex",
                "com.sun.xml.internal.rngom.ast.builder",
                "com.sun.xml.internal.rngom.ast.om",
                "com.sun.xml.internal.rngom.ast.util",
                "com.sun.xml.internal.rngom.binary",
                "com.sun.xml.internal.rngom.binary.visitor",
                "com.sun.xml.internal.rngom.digested",
                "com.sun.xml.internal.rngom.dt",
                "com.sun.xml.internal.rngom.dt.builtin",
                "com.sun.xml.internal.rngom.nc",
                "com.sun.xml.internal.rngom.parse",
                "com.sun.xml.internal.rngom.parse.compact",
                "com.sun.xml.internal.rngom.parse.host",
                "com.sun.xml.internal.rngom.parse.xml",
                "com.sun.xml.internal.rngom.util",
                "com.sun.xml.internal.rngom.xml.sax",
                "com.sun.xml.internal.rngom.xml.util",
                "com.sun.xml.internal.stream",
                "com.sun.xml.internal.stream.buffer",
                "com.sun.xml.internal.stream.buffer.sax",
                "com.sun.xml.internal.stream.buffer.stax",
                "com.sun.xml.internal.stream.dtd",
                "com.sun.xml.internal.stream.dtd.nonvalidating",
                "com.sun.xml.internal.stream.events",
                "com.sun.xml.internal.stream.util",
                "com.sun.xml.internal.stream.writers",
                "com.sun.xml.internal.txw2",
                "com.sun.xml.internal.txw2.annotation",
                "com.sun.xml.internal.txw2.output",
                "com.sun.xml.internal.ws",
                "com.sun.xml.internal.ws.addressing",
                "com.sun.xml.internal.ws.addressing.model",
                "com.sun.xml.internal.ws.addressing.policy",
                "com.sun.xml.internal.ws.addressing.v200408",
                "com.sun.xml.internal.ws.api",
                "com.sun.xml.internal.ws.api.addressing",
                "com.sun.xml.internal.ws.api.client",
                "com.sun.xml.internal.ws.api.config.management",
                "com.sun.xml.internal.ws.api.config.management.policy",
                "com.sun.xml.internal.ws.api.databinding",
                "com.sun.xml.internal.ws.api.fastinfoset",
                "com.sun.xml.internal.ws.api.ha",
                "com.sun.xml.internal.ws.api.handler",
                "com.sun.xml.internal.ws.api.message",
                "com.sun.xml.internal.ws.api.message.saaj",
                "com.sun.xml.internal.ws.api.message.stream",
                "com.sun.xml.internal.ws.api.model",
                "com.sun.xml.internal.ws.api.model.soap",
                "com.sun.xml.internal.ws.api.model.wsdl",
                "com.sun.xml.internal.ws.api.model.wsdl.editable",
                "com.sun.xml.internal.ws.api.pipe",
                "com.sun.xml.internal.ws.api.pipe.helper",
                "com.sun.xml.internal.ws.api.policy",
                "com.sun.xml.internal.ws.api.policy.subject",
                "com.sun.xml.internal.ws.api.server",
                "com.sun.xml.internal.ws.api.streaming",
                "com.sun.xml.internal.ws.api.wsdl.parser",
                "com.sun.xml.internal.ws.api.wsdl.writer",
                "com.sun.xml.internal.ws.assembler",
                "com.sun.xml.internal.ws.assembler.dev",
                "com.sun.xml.internal.ws.assembler.jaxws",
                "com.sun.xml.internal.ws.binding",
                "com.sun.xml.internal.ws.client",
                "com.sun.xml.internal.ws.client.dispatch",
                "com.sun.xml.internal.ws.client.sei",
                "com.sun.xml.internal.ws.commons.xmlutil",
                "com.sun.xml.internal.ws.config.management.policy",
                "com.sun.xml.internal.ws.config.metro.dev",
                "com.sun.xml.internal.ws.config.metro.util",
                "com.sun.xml.internal.ws.db",
                "com.sun.xml.internal.ws.db.glassfish",
                "com.sun.xml.internal.ws.developer",
                "com.sun.xml.internal.ws.dump",
                "com.sun.xml.internal.ws.encoding",
                "com.sun.xml.internal.ws.encoding.fastinfoset",
                "com.sun.xml.internal.ws.encoding.policy",
                "com.sun.xml.internal.ws.encoding.soap",
                "com.sun.xml.internal.ws.encoding.soap.streaming",
                "com.sun.xml.internal.ws.encoding.xml",
                "com.sun.xml.internal.ws.fault",
                "com.sun.xml.internal.ws.handler",
                "com.sun.xml.internal.ws.message",
                "com.sun.xml.internal.ws.message.jaxb",
                "com.sun.xml.internal.ws.message.saaj",
                "com.sun.xml.internal.ws.message.source",
                "com.sun.xml.internal.ws.message.stream",
                "com.sun.xml.internal.ws.model",
                "com.sun.xml.internal.ws.model.soap",
                "com.sun.xml.internal.ws.model.wsdl",
                "com.sun.xml.internal.ws.org.objectweb.asm",
                "com.sun.xml.internal.ws.policy",
                "com.sun.xml.internal.ws.policy.jaxws",
                "com.sun.xml.internal.ws.policy.jaxws.spi",
                "com.sun.xml.internal.ws.policy.privateutil",
                "com.sun.xml.internal.ws.policy.sourcemodel",
                "com.sun.xml.internal.ws.policy.sourcemodel.attach",
                "com.sun.xml.internal.ws.policy.sourcemodel.wspolicy",
                "com.sun.xml.internal.ws.policy.spi",
                "com.sun.xml.internal.ws.policy.subject",
                "com.sun.xml.internal.ws.protocol.soap",
                "com.sun.xml.internal.ws.protocol.xml",
                "com.sun.xml.internal.ws.resources",
                "com.sun.xml.internal.ws.runtime.config",
                "com.sun.xml.internal.ws.server",
                "com.sun.xml.internal.ws.server.provider",
                "com.sun.xml.internal.ws.server.sei",
                "com.sun.xml.internal.ws.spi",
                "com.sun.xml.internal.ws.spi.db",
                "com.sun.xml.internal.ws.streaming",
                "com.sun.xml.internal.ws.transport",
                "com.sun.xml.internal.ws.transport.http",
                "com.sun.xml.internal.ws.transport.http.client",
                "com.sun.xml.internal.ws.transport.http.server",
                "com.sun.xml.internal.ws.util",
                "com.sun.xml.internal.ws.util.exception",
                "com.sun.xml.internal.ws.util.pipe",
                "com.sun.xml.internal.ws.util.xml",
                "com.sun.xml.internal.ws.wsdl",
                "com.sun.xml.internal.ws.wsdl.parser",
                "com.sun.xml.internal.ws.wsdl.writer",
                "com.sun.xml.internal.ws.wsdl.writer.document",
                "com.sun.xml.internal.ws.wsdl.writer.document.http",
                "com.sun.xml.internal.ws.wsdl.writer.document.soap",
                "com.sun.xml.internal.ws.wsdl.writer.document.soap12",
                "com.sun.xml.internal.ws.wsdl.writer.document.xsd",
                "com.sun.xml.internal.xsom",
                "com.sun.xml.internal.xsom.impl",
                "com.sun.xml.internal.xsom.impl.parser",
                "com.sun.xml.internal.xsom.impl.parser.state",
                "com.sun.xml.internal.xsom.impl.scd",
                "com.sun.xml.internal.xsom.impl.util",
                "com.sun.xml.internal.xsom.parser",
                "com.sun.xml.internal.xsom.util",
                "com.sun.xml.internal.xsom.visitor",
                "java.applet",
                "java.awt",
                "java.awt.color",
                "java.awt.datatransfer",
                "java.awt.dnd",
                "java.awt.dnd.peer",
                "java.awt.event",
                "java.awt.font",
                "java.awt.geom",
                "java.awt.im",
                "java.awt.im.spi",
                "java.awt.image",
                "java.awt.image.renderable",
                "java.awt.peer",
                "java.awt.print",
                "java.beans",
                "java.beans.beancontext",
                "java.io",
                "java.lang",
                "java.lang.annotation",
                "java.lang.instrument",
                "java.lang.invoke",
                "java.lang.management",
                "java.lang.ref",
                "java.lang.reflect",
                "java.math",
                "java.net",
                "java.nio",
                "java.nio.channels",
                "java.nio.channels.spi",
                "java.nio.charset",
                "java.nio.charset.spi",
                "java.nio.file",
                "java.nio.file.attribute",
                "java.nio.file.spi",
                "java.rmi",
                "java.rmi.activation",
                "java.rmi.dgc",
                "java.rmi.registry",
                "java.rmi.server",
                "java.security",
                "java.security.acl",
                "java.security.cert",
                "java.security.interfaces",
                "java.security.spec",
                "java.sql",
                "java.text",
                "java.text.spi",
                "java.time",
                "java.time.chrono",
                "java.time.format",
                "java.time.temporal",
                "java.time.zone",
                "java.util",
                "java.util.concurrent",
                "java.util.concurrent.atomic",
                "java.util.concurrent.locks",
                "java.util.function",
                "java.util.jar",
                "java.util.logging",
                "java.util.prefs",
                "java.util.regex",
                "java.util.spi",
                "java.util.stream",
                "java.util.zip",
                "javafx.animation",
                "javafx.application",
                "javafx.beans",
                "javafx.beans.binding",
                "javafx.beans.property",
                "javafx.beans.property.adapter",
                "javafx.beans.value",
                "javafx.collections",
                "javafx.collections.transformation",
                "javafx.concurrent",
                "javafx.css",
                "javafx.embed.swing",
                "javafx.embed.swt",
                "javafx.event",
                "javafx.fxml",
                "javafx.geometry",
                "javafx.print",
                "javafx.scene",
                "javafx.scene.canvas",
                "javafx.scene.chart",
                "javafx.scene.control",
                "javafx.scene.control.cell",
                "javafx.scene.effect",
                "javafx.scene.image",
                "javafx.scene.input",
                "javafx.scene.layout",
                "javafx.scene.media",
                "javafx.scene.paint",
                "javafx.scene.shape",
                "javafx.scene.text",
                "javafx.scene.transform",
                "javafx.scene.web",
                "javafx.stage",
                "javafx.util",
                "javafx.util.converter",
                "javax.accessibility",
                "javax.activation",
                "javax.activity",
                "javax.annotation",
                "javax.annotation.processing",
                "javax.crypto",
                "javax.crypto.interfaces",
                "javax.crypto.spec",
                "javax.imageio",
                "javax.imageio.event",
                "javax.imageio.metadata",
                "javax.imageio.plugins.bmp",
                "javax.imageio.plugins.jpeg",
                "javax.imageio.spi",
                "javax.imageio.stream",
                "javax.jws",
                "javax.jws.soap",
                "javax.lang.model",
                "javax.lang.model.element",
                "javax.lang.model.type",
                "javax.lang.model.util",
                "javax.management",
                "javax.management.loading",
                "javax.management.modelmbean",
                "javax.management.monitor",
                "javax.management.openmbean",
                "javax.management.relation",
                "javax.management.remote",
                "javax.management.remote.rmi",
                "javax.management.timer",
                "javax.naming",
                "javax.naming.directory",
                "javax.naming.event",
                "javax.naming.ldap",
                "javax.naming.spi",
                "javax.net",
                "javax.net.ssl",
                "javax.print",
                "javax.print.attribute",
                "javax.print.attribute.standard",
                "javax.print.event",
                "javax.rmi",
                "javax.rmi.CORBA",
                "javax.rmi.ssl",
                "javax.script",
                "javax.security.auth",
                "javax.security.auth.callback",
                "javax.security.auth.kerberos",
                "javax.security.auth.login",
                "javax.security.auth.spi",
                "javax.security.auth.x500",
                "javax.security.cert",
                "javax.security.sasl",
                "javax.smartcardio",
                "javax.sound.midi",
                "javax.sound.midi.spi",
                "javax.sound.sampled",
                "javax.sound.sampled.spi",
                "javax.sql",
                "javax.sql.rowset",
                "javax.sql.rowset.serial",
                "javax.sql.rowset.spi",
                "javax.swing",
                "javax.swing.border",
                "javax.swing.colorchooser",
                "javax.swing.event",
                "javax.swing.filechooser",
                "javax.swing.plaf",
                "javax.swing.plaf.basic",
                "javax.swing.plaf.metal",
                "javax.swing.plaf.multi",
                "javax.swing.plaf.nimbus",
                "javax.swing.plaf.synth",
                "javax.swing.table",
                "javax.swing.text",
                "javax.swing.text.html",
                "javax.swing.text.html.parser",
                "javax.swing.text.rtf",
                "javax.swing.tree",
                "javax.swing.undo",
                "javax.tools",
                "javax.transaction",
                "javax.transaction.xa",
                "javax.xml",
                "javax.xml.bind",
                "javax.xml.bind.annotation",
                "javax.xml.bind.annotation.adapters",
                "javax.xml.bind.attachment",
                "javax.xml.bind.helpers",
                "javax.xml.bind.util",
                "javax.xml.crypto",
                "javax.xml.crypto.dom",
                "javax.xml.crypto.dsig",
                "javax.xml.crypto.dsig.dom",
                "javax.xml.crypto.dsig.keyinfo",
                "javax.xml.crypto.dsig.spec",
                "javax.xml.datatype",
                "javax.xml.namespace",
                "javax.xml.parsers",
                "javax.xml.soap",
                "javax.xml.stream",
                "javax.xml.stream.events",
                "javax.xml.stream.util",
                "javax.xml.transform",
                "javax.xml.transform.dom",
                "javax.xml.transform.sax",
                "javax.xml.transform.stax",
                "javax.xml.transform.stream",
                "javax.xml.validation",
                "javax.xml.ws",
                "javax.xml.ws.handler",
                "javax.xml.ws.handler.soap",
                "javax.xml.ws.http",
                "javax.xml.ws.soap",
                "javax.xml.ws.spi",
                "javax.xml.ws.spi.http",
                "javax.xml.ws.wsaddressing",
                "javax.xml.xpath",
                "jdk",
                "jdk.internal.cmm",
                "jdk.internal.dynalink",
                "jdk.internal.dynalink.beans",
                "jdk.internal.dynalink.linker",
                "jdk.internal.dynalink.support",
                "jdk.internal.instrumentation",
                "jdk.internal.org.objectweb.asm",
                "jdk.internal.org.objectweb.asm.commons",
                "jdk.internal.org.objectweb.asm.signature",
                "jdk.internal.org.objectweb.asm.tree",
                "jdk.internal.org.objectweb.asm.tree.analysis",
                "jdk.internal.org.objectweb.asm.util",
                "jdk.internal.org.xml.sax",
                "jdk.internal.org.xml.sax.helpers",
                "jdk.internal.util.xml",
                "jdk.internal.util.xml.impl",
                "jdk.jfr.events",
                "jdk.management.cmm",
                "jdk.management.resource",
                "jdk.management.resource.internal",
                "jdk.management.resource.internal.inst",
                "jdk.nashorn.api.scripting",
                "jdk.nashorn.internal",
                "jdk.nashorn.internal.codegen",
                "jdk.nashorn.internal.codegen.types",
                "jdk.nashorn.internal.ir",
                "jdk.nashorn.internal.ir.annotations",
                "jdk.nashorn.internal.ir.debug",
                "jdk.nashorn.internal.ir.visitor",
                "jdk.nashorn.internal.lookup",
                "jdk.nashorn.internal.objects",
                "jdk.nashorn.internal.objects.annotations",
                "jdk.nashorn.internal.parser",
                "jdk.nashorn.internal.runtime",
                "jdk.nashorn.internal.runtime.arrays",
                "jdk.nashorn.internal.runtime.events",
                "jdk.nashorn.internal.runtime.linker",
                "jdk.nashorn.internal.runtime.logging",
                "jdk.nashorn.internal.runtime.options",
                "jdk.nashorn.internal.runtime.regexp",
                "jdk.nashorn.internal.runtime.regexp.joni",
                "jdk.nashorn.internal.runtime.regexp.joni.ast",
                "jdk.nashorn.internal.runtime.regexp.joni.constants",
                "jdk.nashorn.internal.runtime.regexp.joni.encoding",
                "jdk.nashorn.internal.runtime.regexp.joni.exception",
                "jdk.nashorn.internal.scripts",
                "jdk.nashorn.tools",
                "jdk.net",
                "netscape.javascript",
                "oracle.jrockit.jfr",
                "oracle.jrockit.jfr.events",
                "oracle.jrockit.jfr.jdkevents",
                "oracle.jrockit.jfr.jdkevents.throwabletransform",
                "oracle.jrockit.jfr.openmbean",
                "oracle.jrockit.jfr.parser",
                "oracle.jrockit.jfr.settings",
                "oracle.jrockit.jfr.tools",
                "org.ietf.jgss",
                "org.jcp.xml.dsig.internal",
                "org.jcp.xml.dsig.internal.dom",
                "org.omg.CORBA",
                "org.omg.CORBA.DynAnyPackage",
                "org.omg.CORBA.ORBPackage",
                "org.omg.CORBA.TypeCodePackage",
                "org.omg.CORBA.portable",
                "org.omg.CORBA_2_3",
                "org.omg.CORBA_2_3.portable",
                "org.omg.CosNaming",
                "org.omg.CosNaming.NamingContextExtPackage",
                "org.omg.CosNaming.NamingContextPackage",
                "org.omg.Dynamic",
                "org.omg.DynamicAny",
                "org.omg.DynamicAny.DynAnyFactoryPackage",
                "org.omg.DynamicAny.DynAnyPackage",
                "org.omg.IOP",
                "org.omg.IOP.CodecFactoryPackage",
                "org.omg.IOP.CodecPackage",
                "org.omg.Messaging",
                "org.omg.PortableInterceptor",
                "org.omg.PortableInterceptor.ORBInitInfoPackage",
                "org.omg.PortableServer",
                "org.omg.PortableServer.CurrentPackage",
                "org.omg.PortableServer.POAManagerPackage",
                "org.omg.PortableServer.POAPackage",
                "org.omg.PortableServer.ServantLocatorPackage",
                "org.omg.PortableServer.portable",
                "org.omg.SendingContext",
                "org.omg.stub.java.rmi",
                "org.omg.stub.javax.management.remote.rmi",
                "org.relaxng.datatype",
                "org.relaxng.datatype.helpers",
                "org.w3c.dom",
                "org.w3c.dom.bootstrap",
                "org.w3c.dom.css",
                "org.w3c.dom.events",
                "org.w3c.dom.html",
                "org.w3c.dom.ls",
                "org.w3c.dom.ranges",
                "org.w3c.dom.stylesheets",
                "org.w3c.dom.traversal",
                "org.w3c.dom.views",
                "org.w3c.dom.xpath",
                "org.xml.sax",
                "org.xml.sax.ext",
                "org.xml.sax.helpers",
                "sun.applet",
                "sun.applet.resources",
                "sun.audio",
                "sun.awt",
                "sun.awt.X11",
                "sun.awt.datatransfer",
                "sun.awt.dnd",
                "sun.awt.event",
                "sun.awt.geom",
                "sun.awt.im",
                "sun.awt.image",
                "sun.awt.image.codec",
                "sun.awt.motif",
                "sun.awt.resources",
                "sun.awt.shell",
                "sun.awt.util",
                "sun.awt.windows",
                "sun.corba",
                "sun.dc",
                "sun.dc.path",
                "sun.dc.pr",
                "sun.font",
                "sun.instrument",
                "sun.invoke",
                "sun.invoke.empty",
                "sun.invoke.util",
                "sun.io",
                "sun.java2d",
                "sun.java2d.cmm",
                "sun.java2d.cmm.kcms",
                "sun.java2d.cmm.lcms",
                "sun.java2d.d3d",
                "sun.java2d.jules",
                "sun.java2d.loops",
                "sun.java2d.opengl",
                "sun.java2d.pipe",
                "sun.java2d.pipe.hw",
                "sun.java2d.pisces",
                "sun.java2d.windows",
                "sun.java2d.x11",
                "sun.java2d.xr",
                "sun.jvmstat.monitor",
                "sun.jvmstat.monitor.event",
                "sun.jvmstat.monitor.remote",
                "sun.jvmstat.perfdata.monitor",
                "sun.jvmstat.perfdata.monitor.protocol.file",
                "sun.jvmstat.perfdata.monitor.protocol.local",
                "sun.jvmstat.perfdata.monitor.protocol.rmi",
                "sun.jvmstat.perfdata.monitor.v1_0",
                "sun.jvmstat.perfdata.monitor.v2_0",
                "sun.launcher",
                "sun.launcher.resources",
                "sun.lwawt",
                "sun.lwawt.macosx",
                "sun.management",
                "sun.management.counter",
                "sun.management.counter.perf",
                "sun.management.jdp",
                "sun.management.jmxremote",
                "sun.management.resources",
                "sun.management.snmp",
                "sun.management.snmp.jvminstr",
                "sun.management.snmp.jvmmib",
                "sun.management.snmp.util",
                "sun.misc",
                "sun.misc.resources",
                "sun.net",
                "sun.net.dns",
                "sun.net.ftp",
                "sun.net.ftp.impl",
                "sun.net.httpserver",
                "sun.net.idn",
                "sun.net.sdp",
                "sun.net.smtp",
                "sun.net.spi",
                "sun.net.spi.nameservice",
                "sun.net.spi.nameservice.dns",
                "sun.net.util",
                "sun.net.www",
                "sun.net.www.content.audio",
                "sun.net.www.content.image",
                "sun.net.www.content.text",
                "sun.net.www.http",
                "sun.net.www.protocol.file",
                "sun.net.www.protocol.ftp",
                "sun.net.www.protocol.http",
                "sun.net.www.protocol.http.logging",
                "sun.net.www.protocol.http.ntlm",
                "sun.net.www.protocol.http.spnego",
                "sun.net.www.protocol.https",
                "sun.net.www.protocol.jar",
                "sun.net.www.protocol.mailto",
                "sun.net.www.protocol.netdoc",
                "sun.nio",
                "sun.nio.ch",
                "sun.nio.ch.sctp",
                "sun.nio.cs",
                "sun.nio.cs.ext",
                "sun.nio.fs",
                "sun.print",
                "sun.print.resources",
                "sun.reflect",
                "sun.reflect.annotation",
                "sun.reflect.generics.factory",
                "sun.reflect.generics.parser",
                "sun.reflect.generics.reflectiveObjects",
                "sun.reflect.generics.repository",
                "sun.reflect.generics.scope",
                "sun.reflect.generics.tree",
                "sun.reflect.generics.visitor",
                "sun.reflect.misc",
                "sun.rmi.log",
                "sun.rmi.registry",
                "sun.rmi.rmic",
                "sun.rmi.rmic.iiop",
                "sun.rmi.rmic.newrmic",
                "sun.rmi.rmic.newrmic.jrmp",
                "sun.rmi.runtime",
                "sun.rmi.server",
                "sun.rmi.transport",
                "sun.rmi.transport.proxy",
                "sun.rmi.transport.tcp",
                "sun.security.acl",
                "sun.security.action",
                "sun.security.ec",
                "sun.security.internal.interfaces",
                "sun.security.internal.spec",
                "sun.security.jca",
                "sun.security.jgss",
                "sun.security.jgss.krb5",
                "sun.security.jgss.spi",
                "sun.security.jgss.spnego",
                "sun.security.jgss.wrapper",
                "sun.security.krb5",
                "sun.security.krb5.internal",
                "sun.security.krb5.internal.ccache",
                "sun.security.krb5.internal.crypto",
                "sun.security.krb5.internal.crypto.dk",
                "sun.security.krb5.internal.ktab",
                "sun.security.krb5.internal.rcache",
                "sun.security.krb5.internal.tools",
                "sun.security.krb5.internal.util",
                "sun.security.mscapi",
                "sun.security.pkcs",
                "sun.security.pkcs10",
                "sun.security.pkcs11",
                "sun.security.pkcs11.wrapper",
                "sun.security.pkcs12",
                "sun.security.provider",
                "sun.security.provider.certpath",
                "sun.security.provider.certpath.ldap",
                "sun.security.provider.certpath.ssl",
                "sun.security.rsa",
                "sun.security.smartcardio",
                "sun.security.ssl",
                "sun.security.ssl.krb5",
                "sun.security.timestamp",
                "sun.security.tools",
                "sun.security.tools.jarsigner",
                "sun.security.tools.keytool",
                "sun.security.tools.policytool",
                "sun.security.util",
                "sun.security.validator",
                "sun.security.x509",
                "sun.swing",
                "sun.swing.icon",
                "sun.swing.plaf",
                "sun.swing.plaf.synth",
                "sun.swing.plaf.windows",
                "sun.swing.table",
                "sun.swing.text",
                "sun.swing.text.html",
                "sun.text",
                "sun.text.bidi",
                "sun.text.normalizer",
                "sun.text.resources",
                "sun.text.resources.en",
                "sun.tools.asm",
                "sun.tools.attach",
                "sun.tools.jar",
                "sun.tools.jar.resources",
                "sun.tools.java",
                "sun.tools.javac",
                "sun.tools.jcmd",
                "sun.tools.jconsole",
                "sun.tools.jconsole.inspector",
                "sun.tools.jinfo",
                "sun.tools.jmap",
                "sun.tools.jps",
                "sun.tools.jstack",
                "sun.tools.jstat",
                "sun.tools.jstatd",
                "sun.tools.native2ascii",
                "sun.tools.native2ascii.resources",
                "sun.tools.serialver",
                "sun.tools.tree",
                "sun.tools.util",
                "sun.tracing",
                "sun.tracing.dtrace",
                "sun.usagetracker",
                "sun.util",
                "sun.util.calendar",
                "sun.util.cldr",
                "sun.util.locale",
                "sun.util.locale.provider",
                "sun.util.logging",
                "sun.util.logging.resources",
                "sun.util.resources",
                "sun.util.resources.en",
                "sun.util.spi",
                "sun.util.xml"
        };
    }
}
