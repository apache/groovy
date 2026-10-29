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
package org.apache.groovy.lsp.internal.feature;

import groovy.lang.GroovySystem;
import groovy.lang.MetaMethod;
import org.apache.groovy.lsp.internal.util.ReflectionParameterNames;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extension methods offered on completion, including the GDK and
 * classpath extension modules. {@link CompletionService} asks this
 * catalog for the methods that apply to a receiver.
 */
final class ExtensionCatalog {

    static final String OBJECT_NAME = "Object";

    /** Instance and static extension methods, including the GDK and classpath modules. */
    static final List<MetaMethod> EXTENSION_METHODS = extensionCatalog();

    /**
     * Instance or static extension methods, including the GDK and any
     * {@code META-INF/groovy} extension module. The detail is a real
     * signature; the receiver parameter is not part of it.
     */
    static void addExtensionMethods(final List<CompletionItem> items, final String prefix,
                                            final ClassNode receiver, final boolean staticAccess) {
        Set<String> seen = new LinkedHashSet<>();
        for (MetaMethod method : extensionMethods(receiver, staticAccess)) {
            String name = method.getName();
            if (name != null && !name.isEmpty() && name.charAt(0) != '<' && name.indexOf('$') < 0
                    && CompletionService.prefixMatches(name, prefix)) {
                String parameters = extensionParameters(method);
                String returns = simpleName(method.getReturnType());
                String detail = returns + " " + name + "(" + parameters + ")";
                if (seen.add(name + " " + detail) && !alreadyOffered(items, name, detail)) {
                    CompletionItem item = new CompletionItem(name);
                    item.setKind(CompletionItemKind.Method);
                    item.setDetail(detail);
                    CompletionService.attachParts(item, returns, parameters);
                    item.setSortText("2" + name);
                    item.setInsertText(name);
                    items.add(item);
                }
            }
        }
    }

    static List<MetaMethod> extensionMethods(final ClassNode receiver, final boolean staticAccess) {
        List<MetaMethod> methods = null;
        Class<?> type = typeClass(receiver);
        if (type != null) {
            // A loaded receiver lists only the extension methods that apply
            // to it. An empty list stays empty: the whole catalog is not a
            // substitute for a type that adds nothing.
            List<MetaMethod> found = InvokerHelper.getMetaClass(type).getMetaMethods();
            methods = found == null ? List.of() : found;
        } else {
            methods = EXTENSION_METHODS;
        }
        return methods.stream()
                .filter(method -> method != null && method.isStatic() == staticAccess)
                .toList();
    }

    static Class<?> typeClass(final ClassNode receiver) {
        if (!CompletionService.usable(receiver) || !receiver.isResolved()) {
            return null;
        }
        try {
            return receiver.getTypeClass();
        } catch (GroovyBugError | RuntimeException | LinkageError ignored) {
            // getTypeClass throws GroovyBugError when the node is not a loaded class
            return null;
        }
    }

    /**
     * Methods Groovy adds to existing classes: the GDK records, the VM plugin
     * variants, and every extension module visible to the Groovy runtime.
     */
    static List<MetaMethod> extensionCatalog() {
        Set<MetaMethod> methods = new LinkedHashSet<>();
        addInstanceExtensions(methods, DefaultGroovyMethods.class);
        for (Class<?> extra : DefaultGroovyMethods.ADDITIONAL_CLASSES) {
            addInstanceExtensions(methods, extra);
        }
        for (Class<?> plugin : VMPluginFactory.getPlugin().getPluginDefaultGroovyMethods()) {
            addInstanceExtensions(methods, plugin);
        }
        addStaticExtensions(methods, DefaultGroovyStaticMethods.class);
        addStaticPlugins(methods, VMPluginFactory.getPlugin().getPluginStaticGroovyMethods());
        if (GroovySystem.getMetaClassRegistry() instanceof MetaClassRegistryImpl registry) {
            addModuleMethods(methods, registry.getModuleRegistry().getModules());
        }
        return List.copyOf(methods);
    }

    /** Static extension classes contributed by the VM plugin. The Java 8 plugin list is empty. */
    static void addStaticPlugins(final Set<MetaMethod> methods, final Class<?>[] plugins) {
        if (plugins == null) {
            return;
        }
        for (Class<?> plugin : plugins) {
            addStaticExtensions(methods, plugin);
        }
    }

    /** One module that fails to load does not hide the others. */
    static void addModuleMethods(final Set<MetaMethod> methods, final Iterable<? extends ExtensionModule> modules) {
        if (modules == null) {
            return;
        }
        for (ExtensionModule module : modules) {
            try {
                methods.addAll(module.getMetaMethods());
            } catch (RuntimeException | LinkageError ignored) {
                // a module that cannot load does not hide the others
            }
        }
    }

    static void addInstanceExtensions(final Set<MetaMethod> into, final Class<?> type) {
        addExtensions(into, type, true);
    }

    static void addStaticExtensions(final Set<MetaMethod> into, final Class<?> type) {
        addExtensions(into, type, false);
    }

    static void addExtensions(final Set<MetaMethod> into, final Class<?> type, final boolean instance) {
        if (type == null) {
            return;
        }
        for (Method method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())
                    || method.getDeclaringClass() == Object.class || method.getParameterCount() == 0) {
                continue;
            }
            into.add(instance ? new NewInstanceMetaMethod(resolveCached(method)) : new NewStaticMetaMethod(resolveCached(method)));
        }
    }

    /** {@link CachedMethod#find} misses a method that is not in its cache. */
    static CachedMethod resolveCached(final Method method) {
        return cachedOrNew(method, CachedMethod.find(method));
    }

    static CachedMethod cachedOrNew(final Method method, final CachedMethod found) {
        if (found == null) {
            return new CachedMethod(method);
        }
        return found;
    }

    static boolean alreadyOffered(final List<CompletionItem> items, final String name, final String detail) {
        int arity = arityOf(detail);
        for (CompletionItem item : items) {
            if (name.equals(item.getLabel()) && arityOf(item.getDetail()) == arity) {
                return true;
            }
        }
        return false;
    }

    static int arityOf(final String detail) {
        if (detail == null) {
            return -1;
        }
        int open = detail.lastIndexOf('(');
        int close = detail.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return -1;
        }
        String params = detail.substring(open + 1, close).trim();
        if (params.isEmpty()) {
            return 0;
        }
        int depth = 0;
        int commas = 0;
        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (c == '<' || c == '(') {
                depth++;
            } else if (c == '>' || c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                commas++;
            }
        }
        return commas + 1;
    }

    /**
     * {@code ReturnType name(Type param, ...)} for an extension method.
     * The first bytecode parameter is the receiver and is left out.
     */
    static String extensionParameters(final MetaMethod method) {
        CachedClass[] logical = method.getParameterTypes();
        int count = logical == null ? 0 : logical.length;
        String[] names = parameterNames(method, count);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            Class<?> type = logical[i] == null ? null : logical[i].getTheClass();
            builder.append(simpleName(type)).append(' ').append(names[i]);
        }
        return builder.toString();
    }

    /**
     * Names of the call parameters. Extension methods hide the receiver, so a
     * reflective {@code arg1} is shown as {@code arg0}.
     */
    static String[] parameterNames(final MetaMethod method, final int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = "arg" + i;
        }
        String[] resolved = method instanceof ReflectionMetaMethod reflection
                ? reflectionNames(reflection, count)
                : extensionParameterNames().get(extensionKey(method));
        if (resolved == null) {
            return names;
        }
        for (int i = 0; i < count && i < resolved.length; i++) {
            String name = resolved[i];
            if (name != null && !name.isEmpty() && !name.matches("arg\\d+")) {
                names[i] = name;
            }
        }
        return names;
    }

    /**
     * Names of the parameters the caller writes. A {@link NewMetaMethod} hides
     * the receiver, which is the first bytecode parameter.
     */
    static String[] reflectionNames(final ReflectionMetaMethod reflection, final int count) {
        String[] names = new String[count];
        MetaMethod cached = reflection.getCachedMethod();
        if (!(cached instanceof CachedMethod cachedMethod)) {
            return names;
        }
        Method reflected = cachedMethod.getCachedMethod();
        int start = reflection instanceof NewMetaMethod ? 1 : 0;
        String[] resolved = ReflectionParameterNames.names(reflected);
        for (int i = 0; i < count; i++) {
            int source = start + i;
            if (source >= resolved.length) {
                break;
            }
            names[i] = resolved[source];
        }
        return names;
    }

    static Map<String, String[]> extensionParameterNames() {
        return ExtensionParameterNameCache.NAMES;
    }

    /**
     * Logical parameter names for extension methods, keyed by name and erased
     * parameter types. Runtime entries are often generated proxies that no
     * longer point at the original method. The map is published by class
     * initialization, so readers do not need a volatile reference.
     */
    static final class ExtensionParameterNameCache {
        static final Map<String, String[]> NAMES = load();

        static Map<String, String[]> load() {
            Set<MetaMethod> sources = new LinkedHashSet<>();
            // StringGroovyMethods and the other JDK categories are not in
            // ADDITIONAL_CLASSES. The runtime serves them as generated
            // proxies, so their names have to be read from the source class.
            for (Class<?> type : DefaultGroovyMethods.DGM_LIKE_CLASSES) {
                addInstanceExtensions(sources, type);
                addStaticExtensions(sources, type);
            }
            sources.addAll(EXTENSION_METHODS);
            Map<String, String[]> loaded = new HashMap<>();
            for (MetaMethod method : sources) {
                if (method instanceof ReflectionMetaMethod reflection) {
                    int count = method.getParameterTypes() == null ? 0 : method.getParameterTypes().length;
                    loaded.putIfAbsent(extensionKey(method), reflectionNames(reflection, count));
                }
            }
            return Map.copyOf(loaded);
        }
    }

    /** Name plus erased call-parameter types. The receiver is not included. */
    static String extensionKey(final MetaMethod method) {
        StringBuilder key = new StringBuilder();
        key.append(method.isStatic() ? 'S' : 'I').append(method.getName());
        Class<?>[] types = method.getNativeParameterTypes();
        if (types != null) {
            for (Class<?> type : types) {
                key.append('\0').append(type == null ? "?" : type.getName());
            }
        }
        return key.toString();
    }

    static String simpleName(final Class<?> type) {
        if (type == null) {
            return OBJECT_NAME;
        }
        if (type.isArray()) {
            return simpleName(type.getComponentType()) + "[]";
        }
        String name = type.getSimpleName();
        return name.isEmpty() ? type.getName() : name;
    }
}
