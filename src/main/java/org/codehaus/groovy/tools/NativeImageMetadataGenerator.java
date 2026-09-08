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
package org.codehaus.groovy.tools;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.EmptyRange;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovySystem;
import groovy.lang.IntRange;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import groovy.lang.MetaObjectProtocol;
import groovy.lang.MetaProperty;
import groovy.lang.ObjectRange;
import groovy.lang.Reference;
import groovy.lang.Script;
import groovy.util.ProxyGenerator;
import org.apache.groovy.runtime.async.DefaultPool;
import org.apache.groovy.runtime.async.ScopedLocal;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.ComposedClosure;
import org.codehaus.groovy.runtime.ConvertedClosure;
import org.codehaus.groovy.runtime.CurriedClosure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.GStringImpl;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GeneratedLambda;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.HandleMetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.IteratorClosureAdapter;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.NullObject;

import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.codehaus.groovy.vmplugin.v8.IndyArrayAccess;
import org.codehaus.groovy.vmplugin.v8.IndyCompoundAssign;
import org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures;
import org.codehaus.groovy.vmplugin.v8.IndyInterface;
import org.codehaus.groovy.vmplugin.v8.IndyMath;
import org.codehaus.groovy.vmplugin.v9.Java9;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;

/**
 * Writes the GraalVM reachability metadata for Groovy's own runtime into the
 * groovy jar, as {@code META-INF/native-image/org.apache.groovy/groovy/reachability-metadata.json}
 * (GROOVY-12365), so that a dynamic Groovy application builds with plain
 * {@code native-image} and no agent step for anything the runtime does
 * on its own behalf.
 * <p>
 * Every entry is derived from what the runtime is known to do, rather than
 * recorded by observing an application, and is conditional on the class that
 * performs the access being reached (the metaclass registry, or the
 * invokedynamic bootstrap), so a statically compiled program that links no
 * dynamic call site carries none of it:
 * <ul>
 * <li><b>The metaclass registry bootstrap</b> (conditional on
 * {@link MetaClassRegistryImpl}). Creating the registry scans the static
 * methods of the DGM method holders, instantiates the additional meta-method
 * classes, loads every type named in the DGM records by name, and caches the
 * receiver types; a parameter or return type is cached when a call is first
 * selected. Caching an abstract type reads its methods, and those of its
 * abstract superclasses and superinterfaces, to decide whether it is a
 * single-abstract-method type. None of these classes is initialised yet when
 * that happens, hence the registry condition. This is where JDK types appear:
 * they are reflected over on every application's behalf.</li>
 * <li><b>The invokedynamic machinery</b> (conditional on {@code IndyInterface}).
 * Its classes obtain method handles to their own methods and to a few JDK and
 * MOP methods through a {@code Lookup} held in a run-time-initialised class,
 * which the image builder cannot fold, so the targets are registered.</li>
 * <li><b>Groovy-owned types that get a metaclass</b> (conditional on the
 * registry too, since a metaclass can be created for a class before the class
 * initialises). {@code CachedClass} reads their declared constructors, methods
 * and fields, up the superclass chain, and the JDK's Introspector their public
 * methods; every interface in the hierarchy is cached and SAM-checked. The
 * types are the Groovy-owned DGM receivers and the runtime classes an ordinary
 * dynamic program instantiates, such as its closure and range
 * implementations.</li>
 * <li><b>Negative lookups by naming convention</b>, for every type above that
 * gets a metaclass: {@code <Type>BeanInfo} and {@code <Type>Customizer} (the
 * Introspector) and {@code groovy.runtime.metaclass.<Type>MetaClass} (the
 * registry's custom metaclass lookup). Registering the absent names makes the
 * lookups fail with the {@code ClassNotFoundException} the callers expect
 * rather than a missing-registration error.</li>
 * </ul>
 * The JDK side of the hierarchies is the one of the JDK the jar was built
 * with; the supertypes JDK 21 adds are listed by hand, and any a later JDK
 * adds are absent until listed, which only matters under
 * {@code --exact-reachability-metadata}.
 * <p>
 * The {@code tests-native} subproject checks all of this against a probe
 * program: its {@code checkNativeMetadata} task fails on any Groovy-owned need
 * the native-image agent records that is not shipped, and its {@code nativeRun}
 * tasks build and run the probe as an image with nothing but this metadata for
 * Groovy's part.
 * <p>
 * Not emitted, deliberately: {@code Class.forName} calls with a constant name
 * (the image builder folds them, absent classes included); reflection over
 * the application's classes and the JDK types it uses dynamically; dynamic
 * proxies for closures coerced to interfaces; serialization. Those depend on
 * what the application does and remain the application's, or the
 * native-image agent's, responsibility.
 *
 * @since 6.0.0
 */
public class NativeImageMetadataGenerator {

    /** The metadata directory inside the jar, per the GraalVM convention {@code META-INF/native-image/<groupId>/<artifactId>/}. */
    public static final String METADATA_DIRECTORY = "META-INF/native-image/org.apache.groovy/groovy";
    public static final String METADATA_FILE = "reachability-metadata.json";

    /** Prefixes of the packages whose classes Groovy ships and is responsible for. */
    static final String[] GROOVY_PACKAGES = {"groovy.", "org.codehaus.groovy.", "org.apache.groovy."};

    /**
     * Runtime classes an ordinary dynamic program gives a metaclass to, beyond
     * the DGM receivers: what the runtime instantiates for closures, strings,
     * ranges and scripts, and the bootstrap classes themselves. Kept short on
     * purpose: anything derivable is derived, and the {@code tests-native}
     * subproject ({@code nativeCheck}) decides whether a class belongs here.
     */
    static final Class<?>[] INTROSPECTED_RUNTIME_TYPES = {
            GroovySystem.class,
            MetaClassRegistryImpl.class,
            MetaClassImpl.class,
            ExpandoMetaClass.class,
            DelegatingMetaClass.class,
            HandleMetaClass.class,
            InvokerHelper.class,
            GroovyObjectSupport.class,
            Closure.class,
            CurriedClosure.class,
            ComposedClosure.class,
            MethodClosure.class,
            IteratorClosureAdapter.class,
            GString.class,
            GStringImpl.class,
            IntRange.class,
            ObjectRange.class,
            EmptyRange.class,
            Script.class,
            Binding.class,
            NullObject.class,
            Reference.class, // holds a captured local variable that a closure reassigns
    };

    /**
     * The anonymous classes of {@code Closure.IDENTITY} and {@code GString.EMPTY},
     * by name: reading the constants would initialise {@code Closure}, and with
     * it GroovySystem, inside the build's converter JVM, where the DGM records
     * do not exist yet. {@code NativeImageMetadataTest} checks the names.
     */
    static final String[] INTROSPECTED_ANONYMOUS_TYPES = {
            "groovy.lang.Closure$1",
            "groovy.lang.GString$1",
    };

    /**
     * Classes whose methods the invokedynamic machinery looks up by
     * {@code MethodHandles.Lookup} (see {@code IndyGuardsFiltersAndSignatures},
     * {@code IndyInterface}, {@code IndyMath}, {@code IndyArrayAccess},
     * {@code IndyCompoundAssign} and {@code TypeTransformers}), plus the runtime
     * entry points generated code calls through the reflective cold tier before
     * their classes are initialised.
     */
    static final Class<?>[] INDY_LOOKUP_TARGETS = {
            IndyInterface.class,
            IndyGuardsFiltersAndSignatures.class,
            IndyMath.class,
            IndyArrayAccess.class,
            IndyCompoundAssign.class,
            ScriptBytecodeAdapter.class,
            InvokerHelper.class, // runScript, called through the reflective tier from a script's main
            DefaultTypeTransformation.class,
            ConvertedClosure.class,
            ProxyGenerator.class,
            GroovyCastException.class,
            CachedSAMClass.class,
            GroovyCategorySupport.class,
            GroovyObject.class,
            MetaObjectProtocol.class,
            MetaClass.class,
            MetaMethod.class,
            MetaProperty.class,
            // JDK targets of the same lookups
            Object.class,
            Number.class,
            BigDecimal.class,
            BigInteger.class,
            Collections.class,
            Proxy.class,
            ArrayList.class,
            HashSet.class,
    };

    /**
     * Interfaces a newer JDK than the build's minimum places above types in
     * the DGM records, which the hierarchy walk cannot see when the jar is
     * built on the older JDK: the sequenced collections of JDK 21 above
     * {@code List}, {@code Deque}, {@code SortedSet} and {@code SortedMap}.
     */
    static final String[] NEWER_JDK_SUPERTYPES = {
            "java.util.SequencedCollection",
            "java.util.SequencedSet",
            "java.util.SequencedMap",
    };

    private enum Flag {
        DECLARED_CONSTRUCTORS("allDeclaredConstructors"),
        PUBLIC_CONSTRUCTORS("allPublicConstructors"),
        DECLARED_METHODS("allDeclaredMethods"),
        PUBLIC_METHODS("allPublicMethods"),
        DECLARED_FIELDS("allDeclaredFields");

        final String json;

        Flag(String json) {
            this.json = json;
        }
    }

    private static final EnumSet<Flag> NONE = EnumSet.noneOf(Flag.class);
    private static final EnumSet<Flag> SAM_CHECK = EnumSet.of(Flag.DECLARED_METHODS, Flag.PUBLIC_METHODS);
    private static final EnumSet<Flag> SCANNED = EnumSet.of(Flag.DECLARED_CONSTRUCTORS, Flag.DECLARED_METHODS);
    private static final EnumSet<Flag> LOOKED_UP = EnumSet.of(Flag.DECLARED_CONSTRUCTORS, Flag.PUBLIC_CONSTRUCTORS, Flag.DECLARED_METHODS, Flag.PUBLIC_METHODS);
    private static final EnumSet<Flag> INTROSPECTED = EnumSet.of(Flag.DECLARED_CONSTRUCTORS, Flag.DECLARED_METHODS, Flag.DECLARED_FIELDS, Flag.PUBLIC_METHODS);

    /** Entries keyed by condition then type; flags of entries that meet twice are merged. */
    private final Map<String, Map<String, EnumSet<Flag>>> entries = new TreeMap<>();
    /** Individually named methods, keyed like {@link #entries}, for JDK classes too big to register wholesale. */
    private final Map<String, Map<String, Set<String>>> methods = new TreeMap<>();

    /**
     * Writes the metadata file under {@code targetDirectory}.
     *
     * @param records the DGM records {@code DgmConverter} produced
     * @param targetDirectory the classes directory the jar is assembled from
     * @return the path of the file written
     */
    public static String write(List<GeneratedMetaMethod.DgmMethodRecord> records, String targetDirectory) throws IOException {
        File file = new File(targetDirectory, METADATA_DIRECTORY + "/" + METADATA_FILE).getCanonicalFile();
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), generate(records).getBytes(StandardCharsets.UTF_8));
        return file.getPath();
    }

    /**
     * Writes the metadata of an extension module (a jar with a
     * {@code META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule}
     * descriptor) under {@code targetDirectory}, as
     * {@code META-INF/native-image/org.apache.groovy/<artifactId>/reachability-metadata.json}.
     * The registry loads the descriptor's extension classes by name, scans
     * their static methods and caches every type in those signatures, all
     * while it bootstraps; the entries are conditional on the registry.
     *
     * @param artifactId the module's artifact id, e.g. {@code groovy-nio}
     * @param instanceExtensions the descriptor's {@code extensionClasses}
     * @param staticExtensions the descriptor's {@code staticExtensionClasses}
     * @param targetDirectory the directory the jar is assembled from
     * @return the path of the file written
     */
    public static String writeForModule(String artifactId, List<Class<?>> instanceExtensions, List<Class<?>> staticExtensions,
                                        String targetDirectory) throws IOException {
        File file = new File(targetDirectory, "META-INF/native-image/org.apache.groovy/" + artifactId + "/" + METADATA_FILE).getCanonicalFile();
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), generateForModule(instanceExtensions, staticExtensions).getBytes(StandardCharsets.UTF_8));
        return file.getPath();
    }

    static String generateForModule(List<Class<?>> instanceExtensions, List<Class<?>> staticExtensions) {
        NativeImageMetadataGenerator generator = new NativeImageMetadataGenerator();
        String registry = MetaClassRegistryImpl.class.getName();
        for (Class<?> extension : instanceExtensions) generator.scannedHolder(registry, extension);
        for (Class<?> extension : staticExtensions) generator.scannedHolder(registry, extension);
        return generator.toJson();
    }

    /**
     * The build's entry point for a module:
     * {@code <artifactId> <targetDirectory> <extensionClasses> <staticExtensionClasses>},
     * the class lists comma-separated and possibly empty. The classes are
     * loaded without initialisation, so no Groovy runtime starts here.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 4) {
            throw new IllegalArgumentException("usage: <artifactId> <targetDirectory> <extensionClasses> <staticExtensionClasses>");
        }
        String path = writeForModule(args[0], loadAll(args[2]), loadAll(args[3]), args[1]);
        System.out.println("Saved native-image reachability metadata to: " + path);
    }

    private static List<Class<?>> loadAll(String commaSeparated) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        for (String name : commaSeparated.split("[,; ]")) {
            if (!name.isBlank()) classes.add(Class.forName(name.trim(), false, NativeImageMetadataGenerator.class.getClassLoader()));
        }
        return classes;
    }

    /**
     * @return the metadata as JSON, deterministic for a given runtime so that
     *         the build output is cacheable and diffs are meaningful
     */
    static String generate(List<GeneratedMetaMethod.DgmMethodRecord> records) {
        NativeImageMetadataGenerator generator = new NativeImageMetadataGenerator();
        generator.registryBootstrap(records);
        generator.indyMachinery();
        generator.asyncRuntime();
        generator.introspectedTypes(records);
        return generator.toJson();
    }

    /**
     * The async runtime reaches JDK 21 APIs through constant method-handle
     * lookups and {@code Class.forName}, each inside a fallback that would
     * silently settle for platform threads or thread locals in an image where
     * the lookup was not folded; registering the targets removes the doubt.
     */
    private void asyncRuntime() {
        // AsyncExecutors is package-private, hence by name
        for (String user : new String[]{"org.apache.groovy.runtime.async.AsyncExecutors", DefaultPool.class.getName()}) {
            method(user, Executors.class.getName(), "newVirtualThreadPerTaskExecutor");
        }
        String scopedLocal = ScopedLocal.class.getName();
        String scopedValue = "java.lang.ScopedValue"; // JDK 21+, by name: the build may run on JDK 17
        method(scopedLocal, scopedValue, "newInstance");
        method(scopedLocal, scopedValue, "get");
        method(scopedLocal, scopedValue, "isBound");
        method(scopedLocal, scopedValue, "where", scopedValue, Object.class.getName());
        method(scopedLocal, scopedValue + "$Carrier", "run", Runnable.class.getName());
    }

    private void registryBootstrap(List<GeneratedMetaMethod.DgmMethodRecord> records) {
        String registry = MetaClassRegistryImpl.class.getName();
        // the DGM-like classes: their methods are described by the records, and
        // their adapters are created through the factory, loaded by name
        for (Class<?> holder : DefaultGroovyMethods.DGM_LIKE_CLASSES) {
            for (Class<?> c = holder; c != null && c != Object.class; c = c.getSuperclass()) add(registry, c, SCANNED);
        }
        addName(registry, DgmConverter.PROXY_FACTORY_CLASS_NAME.replace('/', '.'), NONE);
        // every compiled closure and lambda implements these; their metaclasses SAM-check them
        add(registry, GeneratedClosure.class, SAM_CHECK);
        add(registry, GeneratedLambda.class, SAM_CHECK);
        // supertypes newer JDKs add to types in the records, invisible when building on an older JDK
        for (String newer : NEWER_JDK_SUPERTYPES) addName(registry, newer, SAM_CHECK);
        for (GeneratedMetaMethod.DgmMethodRecord record : records) {
            recordType(registry, record.returnType);
            for (Class<?> parameter : record.parameters) recordType(registry, parameter);
        }
        // the holders scanned reflectively: every static method's signature is cached
        List<Class<?>> scanned = new ArrayList<>();
        scanned.add(DefaultGroovyStaticMethods.class);
        for (Class<?> holder : VMPluginFactory.getPlugin().getPluginDefaultGroovyMethods()) scanned.add(holder);
        for (Class<?> holder : VMPluginFactory.getPlugin().getPluginStaticGroovyMethods()) scanned.add(holder);
        for (Class<?> holder : scanned) scannedHolder(registry, holder);
        // the meta-method classes instantiated by the registry, and the signatures they declare
        for (Class<?> holder : DefaultGroovyMethods.ADDITIONAL_CLASSES) {
            add(registry, holder, SCANNED);
            MetaMethod metaMethod = instantiate(holder);
            recordType(registry, metaMethod.getDeclaringClass().getTheClass());
            recordType(registry, metaMethod.getReturnType());
            for (Class<?> parameter : metaMethod.getNativeParameterTypes()) recordType(registry, parameter);
        }
    }

    /**
     * A class the registry loads by name and scans for static methods: its
     * declared methods up the chain, and every type in those signatures.
     */
    private void scannedHolder(String registry, Class<?> holder) {
        add(registry, holder, NONE); // loaded by name
        for (Class<?> c = holder; c != null && c != Object.class; c = c.getSuperclass()) add(registry, c, SCANNED);
        for (Method method : holder.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            recordType(registry, method.getReturnType());
            for (Class<?> parameter : method.getParameterTypes()) recordType(registry, parameter);
        }
    }

    private static MetaMethod instantiate(Class<?> metaMethodClass) {
        try {
            return (MetaMethod) metaMethodClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot instantiate " + metaMethodClass.getName(), e);
        }
    }

    /** A type named in a DGM record: loaded by name and cached, with its hierarchy. */
    private void recordType(String registry, Class<?> type) {
        if (type.isPrimitive()) return;
        add(registry, type, NONE);
        cachedHierarchy(registry, type);
    }

    /**
     * Caching a class ({@code ClassInfo.createCachedClass}) caches its
     * superclasses and every interface in the hierarchy as well, and the SAM
     * check ({@code CachedSAMClass.getSAMMethod}) reads the methods of each
     * abstract one, walking abstract superclasses and superinterfaces.
     */
    private void cachedHierarchy(String condition, Class<?> type) {
        Set<Class<?>> visited = new HashSet<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            if (c.isArray()) break;
            add(condition, c, Modifier.isAbstract(c.getModifiers()) ? SAM_CHECK : NONE);
            addInterfaces(condition, c, visited);
        }
    }

    private void addInterfaces(String condition, Class<?> type, Set<Class<?>> visited) {
        for (Class<?> implemented : type.getInterfaces()) {
            if (!visited.add(implemented)) continue;
            add(condition, implemented, SAM_CHECK);
            addInterfaces(condition, implemented, visited);
        }
    }

    private void indyMachinery() {
        String indy = IndyInterface.class.getName();
        for (Class<?> target : INDY_LOOKUP_TARGETS) add(indy, target, LOOKED_UP);
        // IndyGuardsFiltersAndSignatures.CLASS_FOR_NAME and NON_NULL
        method(indy, Class.class, "forName", String.class, boolean.class, ClassLoader.class);
        method(indy, Objects.class, "nonNull", Object.class);
        // ReflectionUtils resolves the sealed-class API through method handles
        String reflectionUtils = ReflectionUtils.class.getName();
        method(reflectionUtils, Class.class, "isSealed");
        method(reflectionUtils, Class.class, "getPermittedSubclasses");
        // the Java 9 plugin locates the groovy jar by loading two of its classes by name
        addName(Java9.class.getName(), GroovySystem.class.getName(), NONE);
        addName(Java9.class.getName(), "groovy.beans.ListenerList", NONE); // Groovy source, not visible here
    }

    private void introspectedTypes(List<GeneratedMetaMethod.DgmMethodRecord> records) {
        Set<Class<?>> types = new LinkedHashSet<>();
        for (GeneratedMetaMethod.DgmMethodRecord record : records) {
            Class<?> receiver = record.parameters[0];
            if (!receiver.isArray() && !receiver.isPrimitive() && isGroovyOwned(receiver.getName())) types.add(receiver);
        }
        Collections.addAll(types, INTROSPECTED_RUNTIME_TYPES);
        // the DGM holders get a metaclass of their own: the runtime invokes some of
        // their methods through the MOP (InvokerHelper.invokeStaticMethod for `as T[]`)
        for (Class<?> holder : DefaultGroovyMethods.DGM_LIKE_CLASSES) types.add(holder);
        types.add(DefaultGroovyStaticMethods.class);
        for (String anonymous : INTROSPECTED_ANONYMOUS_TYPES) {
            try {
                types.add(Class.forName(anonymous, false, NativeImageMetadataGenerator.class.getClassLoader()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("anonymous runtime class renamed; update INTROSPECTED_ANONYMOUS_TYPES", e);
            }
        }
        // a metaclass can be created for a class before the class initialises,
        // so the type itself is not a usable condition; the registry, which
        // every metaclass creation goes through, is
        String registry = MetaClassRegistryImpl.class.getName();
        for (Class<?> type : types) {
            String name = type.getName();
            // the metaclass walks the superclass chain for methods, fields and
            // constructors, and caches every interface, which SAM-checks it
            cachedHierarchy(registry, type);
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                add(registry, c, INTROSPECTED);
                // the Introspector probes for BeanInfo and Customizer classes up the chain
                negative(registry, c.getName() + "BeanInfo");
                negative(registry, c.getName() + "Customizer");
            }
            negative(registry, "groovy.runtime.metaclass." + name + "MetaClass");
        }
    }

    private void add(String condition, Class<?> type, EnumSet<Flag> flags) {
        addName(condition, type.getTypeName(), flags);
    }

    /** Flags of an entry that meets twice under one condition are merged. */
    private void addName(String condition, String typeName, EnumSet<Flag> flags) {
        Map<String, EnumSet<Flag>> byType = entries.computeIfAbsent(condition, k -> new TreeMap<>());
        EnumSet<Flag> existing = byType.get(typeName);
        if (existing != null) {
            existing.addAll(flags);
        } else {
            byType.put(typeName, EnumSet.copyOf(flags));
        }
    }

    private void method(String condition, Class<?> type, String name, Class<?>... parameterTypes) {
        String[] names = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) names[i] = parameterTypes[i].getTypeName();
        method(condition, type.getTypeName(), name, names);
    }

    private void method(String condition, String typeName, String name, String... parameterTypeNames) {
        addName(condition, typeName, NONE);
        StringBuilder json = new StringBuilder("{\"name\": \"").append(name).append("\", \"parameterTypes\": [");
        for (int i = 0; i < parameterTypeNames.length; i++) {
            json.append(i == 0 ? "\"" : ", \"").append(parameterTypeNames[i]).append('"');
        }
        json.append("]}");
        methods.computeIfAbsent(condition, k -> new TreeMap<>())
               .computeIfAbsent(typeName, k -> new LinkedHashSet<>())
               .add(json.toString());
    }

    private void negative(String condition, String absentType) {
        entries.computeIfAbsent(condition, k -> new TreeMap<>()).putIfAbsent(absentType, EnumSet.noneOf(Flag.class));
    }

    private String toJson() {
        StringBuilder json = new StringBuilder(64 * 1024);
        json.append("{\n  \"reflection\": [");
        boolean first = true;
        for (Map.Entry<String, Map<String, EnumSet<Flag>>> condition : entries.entrySet()) {
            for (Map.Entry<String, EnumSet<Flag>> entry : condition.getValue().entrySet()) {
                json.append(first ? "\n" : ",\n").append("    {\"condition\": {\"typeReached\": \"").append(condition.getKey())
                    .append("\"}, \"type\": \"").append(entry.getKey()).append('"');
                for (Flag flag : entry.getValue()) {
                    json.append(", \"").append(flag.json).append("\": true");
                }
                Set<String> named = methods.getOrDefault(condition.getKey(), Collections.emptyMap()).get(entry.getKey());
                if (named != null) {
                    json.append(", \"methods\": [").append(String.join(", ", named)).append(']');
                }
                json.append('}');
                first = false;
            }
        }
        json.append("\n  ]\n}\n");
        return json.toString();
    }

    static boolean isGroovyOwned(String className) {
        for (String prefix : GROOVY_PACKAGES) {
            if (className.startsWith(prefix)) return true;
        }
        return false;
    }
}
