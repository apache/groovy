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
package org.codehaus.groovy.tools.groovydoc;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads Javadoc for external classes (primarily JDK classes) from the local
 * JDK source archive so {@code {@inheritDoc}} can be expanded when a Groovy
 * source method overrides a method declared outside the documented source set.
 */
final class ExternalJavadocSupport {
    private static final JavaParser JAVA_PARSER = new JavaParser(
            new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE)
    );
    private static final Path JDK_SRC_ZIP = detectJdkSrcZip();
    private static final Map<Class<?>, Map<MethodKey, String>> RAW_COMMENT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<ExternalMethodData>> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, GroovyMethodDoc[]> METHOD_DOC_CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger ACTIVE_CACHE_SESSIONS = new AtomicInteger();
    private static final GroovyMethodDoc[] EMPTY_GROOVYMETHODDOC_ARRAY = new GroovyMethodDoc[0];

    private ExternalJavadocSupport() {
    }

    /**
     * Returns the groovydoc representation of methods declared in the external class,
     * with comments resolved from the JDK source archive. External method comments
     * containing {@code {@inheritDoc}} are recursively resolved to their parent
     * class or interface method documentation.
     *
     * @param owner the external class documentation wrapper
     * @return array of groovydoc method representations; empty if no methods are found
     */
    static GroovyMethodDoc[] methodsFor(ExternalGroovyClassDoc owner) {
        if (ACTIVE_CACHE_SESSIONS.get() == 0) {
            try (CacheSession ignored = openCacheSession()) {
                return cachedMethodDocsFor(owner.externalClass());
            }
        }
        return cachedMethodDocsFor(owner.externalClass());
    }

    /**
     * Opens a new cache session for external Javadoc loading. While a session is
     * active, external class method documentation and comment metadata are cached
     * and reused across multiple lookups. When the last session closes, all caches
     * are automatically cleared to avoid long-term memory retention in long-lived
     * Gradle daemons.
     *
     * <p>This method should be called at the start of a batch groovydoc rendering
     * operation that will perform multiple external inheritDoc lookups.</p>
     *
     * @return a {@link CacheSession} that must be closed (typically via try-with-resources)
     */
    static CacheSession openCacheSession() {
        ACTIVE_CACHE_SESSIONS.incrementAndGet();
        return new CacheSession();
    }

    /**
     * Returns current statistics about the state of all external Javadoc caches,
     * including the number of cached raw comment texts, method metadata entries,
     * and fully-materialized method doc arrays.
     *
     * @return a {@link CacheStats} snapshot capturing all three cache sizes
     */
    static CacheStats cacheStats() {
        return new CacheStats(RAW_COMMENT_CACHE.size(), METHOD_CACHE.size(), METHOD_DOC_CACHE.size());
    }

    /**
     * Clears all external Javadoc caches. This method is automatically called when
     * the last active {@link CacheSession} is closed. It can also be called manually
     * to force a reset of cached data.
     */
    static void clearCaches() {
        RAW_COMMENT_CACHE.clear();
        METHOD_CACHE.clear();
        METHOD_DOC_CACHE.clear();
    }

    private static GroovyMethodDoc[] cachedMethodDocsFor(Class<?> externalClass) {
        return METHOD_DOC_CACHE.computeIfAbsent(externalClass, ExternalJavadocSupport::loadMethodDocs);
    }

    private static List<ExternalMethodData> loadExternalMethods(Class<?> externalClass) {
        Method[] declaredMethods = externalClass.getDeclaredMethods();
        Arrays.sort(declaredMethods, Comparator.comparing(Method::getName)
                .thenComparingInt(Method::getParameterCount)
                .thenComparing(Method::toGenericString));

        List<ExternalMethodData> result = new ArrayList<>();
        for (Method method : declaredMethods) {
            if (method.isSynthetic() || method.isBridge()) continue;
            result.add(new ExternalMethodData(
                    method.getName(),
                    typeName(method.getReturnType()),
                    parameterTypeNames(method),
                    resolveEffectiveComment(externalClass, method, new HashSet<>())
            ));
        }
        return result;
    }

    private static GroovyMethodDoc[] loadMethodDocs(Class<?> externalClass) {
        List<ExternalMethodData> methods = METHOD_CACHE.computeIfAbsent(externalClass, ExternalJavadocSupport::loadExternalMethods);
        if (methods.isEmpty()) return EMPTY_GROOVYMETHODDOC_ARRAY;

        ExternalGroovyClassDoc owner = new ExternalGroovyClassDoc(externalClass);
        GroovyMethodDoc[] docs = new GroovyMethodDoc[methods.size()];
        for (int i = 0; i < methods.size(); i++) {
            docs[i] = methods.get(i).toMethodDoc(owner);
        }
        return docs;
    }

    private static Map<MethodKey, String> loadMethodComments(Class<?> externalClass) {
        return RAW_COMMENT_CACHE.computeIfAbsent(externalClass, ExternalJavadocSupport::parseMethodComments);
    }

    /**
     * Manages the lifecycle of external Javadoc caches for a single groovydoc render session.
     * Implements reference counting: when the last session closes, all external caches are
     * cleared to prevent long-term memory retention in the Gradle daemon.
     *
     * <p>This class is not intended for public use; obtain instances via
     * {@link ExternalJavadocSupport#openCacheSession()}.</p>
     */
    static final class CacheSession implements AutoCloseable {
        private boolean closed;

        @Override
        public void close() {
            if (closed) return;
            closed = true;

            int remaining = ACTIVE_CACHE_SESSIONS.decrementAndGet();
            if (remaining <= 0) {
                ACTIVE_CACHE_SESSIONS.set(0);
                clearCaches();
            }
        }
    }

    /**
     * Snapshot of current external Javadoc cache statistics. Contains the size
     * of each of the three caches: raw comment text, method metadata, and fully
     * materialized method documentation arrays.
     *
     * <p>This class is immutable and used for diagnostics and testing.</p>
     */
    static final class CacheStats {
        private final int rawCommentCacheSize;
        private final int methodCacheSize;
        private final int methodDocCacheSize;

        private CacheStats(int rawCommentCacheSize, int methodCacheSize, int methodDocCacheSize) {
            this.rawCommentCacheSize = rawCommentCacheSize;
            this.methodCacheSize = methodCacheSize;
            this.methodDocCacheSize = methodDocCacheSize;
        }

        /**
         * Returns the number of external classes with cached raw Javadoc comment text.
         *
         * @return the size of the raw comment cache
         */
        int rawCommentCacheSize() {
            return rawCommentCacheSize;
        }

        /**
         * Returns the number of external classes with cached method metadata (method names,
         * parameter types, return types).
         *
         * @return the size of the method metadata cache
         */
        int methodCacheSize() {
            return methodCacheSize;
        }

        /**
         * Returns the number of external classes with cached fully-materialized method
         * documentation arrays ({@code GroovyMethodDoc[]}).
         *
         * @return the size of the method documentation cache
         */
        int methodDocCacheSize() {
            return methodDocCacheSize;
        }
    }

    private static Map<MethodKey, String> parseMethodComments(Class<?> externalClass) {
        Map<MethodKey, String> comments = new LinkedHashMap<>();
        Optional<CompilationUnit> source = loadCompilationUnit(externalClass);
        if (source.isEmpty()) return comments;

        Optional<TypeDeclaration<?>> type = findTypeDeclaration(source.get(), externalClass);
        if (type.isEmpty()) return comments;

        for (BodyDeclaration<?> member : type.get().getMembers()) {
            if (!(member instanceof MethodDeclaration methodDeclaration)) continue;
            Method reflectionMethod = findMatchingDeclaredMethod(externalClass, methodDeclaration);
            if (reflectionMethod == null) continue;
            String raw = methodDeclaration.getJavadocComment()
                    .map(comment -> normalizeJavadocComment(comment.getContent()))
                    .orElse("");
            comments.put(MethodKey.of(reflectionMethod), raw);
        }
        return comments;
    }

    private static String resolveEffectiveComment(Class<?> ownerClass, Method method, Set<ExternalMethodKey> visited) {
        ExternalMethodKey key = new ExternalMethodKey(ownerClass, MethodKey.of(method));
        if (!visited.add(key)) return "";

        String rawComment = loadMethodComments(ownerClass).getOrDefault(key.methodKey(), "");
        String trimmed = rawComment.trim();
        if (!trimmed.contains("{@inheritDoc}")) return rawComment;

        ExternalMethodMatch inherited = findInheritedMethod(ownerClass, method, new HashSet<>());
        if (inherited == null) {
            return rawComment.replace("{@inheritDoc}", "").trim();
        }

        String inheritedComment = resolveEffectiveComment(inherited.ownerClass(), inherited.method(), visited);
        if (trimmed.equals("{@inheritDoc}")) {
            return inheritedComment;
        }
        return rawComment.replace("{@inheritDoc}", inheritedComment);
    }

    private static Optional<CompilationUnit> loadCompilationUnit(Class<?> externalClass) {
        if (JDK_SRC_ZIP == null) return Optional.empty();
        String entryName = sourceEntryName(externalClass);
        if (entryName == null) return Optional.empty();

        try (ZipFile zip = new ZipFile(JDK_SRC_ZIP.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                entry = findFallbackEntry(zip, entryName);
                if (entry == null) return Optional.empty();
            }
            try (InputStream inputStream = zip.getInputStream(entry)) {
                String source = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                ParseResult<CompilationUnit> result = JAVA_PARSER.parse(source);
                return result.getResult();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ZipEntry findFallbackEntry(ZipFile zip, String entryName) {
        int slash = entryName.indexOf('/');
        String suffix = slash >= 0 ? "/" + entryName.substring(slash + 1) : "/" + entryName;
        return zip.stream()
                .filter(candidate -> candidate.getName().endsWith(suffix))
                .findFirst()
                .orElse(null);
    }

    private static Optional<TypeDeclaration<?>> findTypeDeclaration(CompilationUnit compilationUnit, Class<?> externalClass) {
        String packageName = externalClass.getPackageName();
        String binaryName = externalClass.getName();
        String relativeName = packageName.isEmpty() ? binaryName : binaryName.substring(packageName.length() + 1);
        String[] segments = relativeName.split("\\$");

        TypeDeclaration<?> current = null;
        for (TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
            if (typeDeclaration.getNameAsString().equals(segments[0])) {
                current = typeDeclaration;
                break;
            }
        }
        if (current == null) return Optional.empty();

        for (int i = 1; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.chars().allMatch(Character::isDigit)) return Optional.empty();
            TypeDeclaration<?> next = null;
            for (BodyDeclaration<?> member : current.getMembers()) {
                if (member instanceof TypeDeclaration<?> nested && nested.getNameAsString().equals(segment)) {
                    next = nested;
                    break;
                }
            }
            if (next == null) return Optional.empty();
            current = next;
        }
        return Optional.of(current);
    }

    private static Method findMatchingDeclaredMethod(Class<?> externalClass, MethodDeclaration methodDeclaration) {
        Method[] declaredMethods = externalClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!method.getName().equals(methodDeclaration.getNameAsString())) continue;
            if (method.isSynthetic() || method.isBridge()) continue;
            if (method.getParameterCount() != methodDeclaration.getParameters().size()) continue;

            boolean allMatch = true;
            for (int i = 0; i < method.getParameterCount(); i++) {
                String declaredType = methodDeclaration.getParameter(i).getType().asString();
                if (methodDeclaration.getParameter(i).isVarArgs()) {
                    declaredType += "[]";
                }
                if (!matchesTypeName(declaredType, method.getParameterTypes()[i], methodDeclaration, externalClass)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) return method;
        }
        return null;
    }

    private static boolean matchesTypeName(String declaredType, Class<?> reflectedType, MethodDeclaration methodDeclaration, Class<?> externalClass) {
        String normalizedDeclared = eraseTypeName(declaredType.replace("...", "[]").trim());
        if (normalizedDeclared.equals(typeName(reflectedType))) return true;
        if (normalizedDeclared.equals(reflectedType.getSimpleName())) return true;
        if (normalizedDeclared.equals(reflectedType.getTypeName())) return true;
        if (reflectedType.getCanonicalName() != null && normalizedDeclared.equals(reflectedType.getCanonicalName())) return true;

        Set<String> typeParameters = new HashSet<>();
        methodDeclaration.getTypeParameters().forEach(type -> typeParameters.add(type.getNameAsString()));
        methodDeclaration.findAncestor(TypeDeclaration.class)
                .ifPresent(type -> {
                    if (type instanceof NodeWithTypeParameters<?> nodeWithTypeParameters) {
                        nodeWithTypeParameters.getTypeParameters()
                                .forEach(parameter -> typeParameters.add(parameter.getNameAsString()));
                    }
                });
        if (typeParameters.contains(normalizedDeclared)) {
            return reflectedType == Object.class;
        }
        if (normalizedDeclared.endsWith("[]")) {
            String componentType = normalizedDeclared.substring(0, normalizedDeclared.length() - 2);
            if (typeParameters.contains(componentType)) {
                return reflectedType.isArray() && reflectedType.getComponentType() == Object.class;
            }
        }

        if (!normalizedDeclared.contains(".")) {
            String packagePrefix = externalClass.getPackageName();
            if (!packagePrefix.isEmpty() && (packagePrefix + "." + normalizedDeclared).equals(typeName(reflectedType))) return true;
            if (("java.lang." + normalizedDeclared).equals(typeName(reflectedType))) return true;
        }

        return false;
    }

    private static String eraseTypeName(String declaredType) {
        if (declaredType == null || declaredType.isEmpty()) return "";
        StringBuilder erased = new StringBuilder(declaredType.length());
        int genericDepth = 0;
        for (int i = 0; i < declaredType.length(); i++) {
            char ch = declaredType.charAt(i);
            if (ch == '<') {
                genericDepth++;
                continue;
            }
            if (ch == '>') {
                genericDepth--;
                continue;
            }
            if (genericDepth == 0) {
                erased.append(ch);
            }
        }
        String normalized = erased.toString().trim();
        if (normalized.startsWith("? extends ")) {
            normalized = normalized.substring("? extends ".length()).trim();
        } else if (normalized.startsWith("? super ")) {
            normalized = normalized.substring("? super ".length()).trim();
        } else if ("?".equals(normalized)) {
            return Object.class.getSimpleName();
        }
        return normalized;
    }

    private static ExternalMethodMatch findInheritedMethod(Class<?> ownerClass, Method method, Set<Class<?>> seen) {
        Class<?> superclass = ownerClass.getSuperclass();
        while (superclass != null && seen.add(superclass)) {
            Method declared = findDeclaredMethod(superclass, method);
            if (declared != null) return new ExternalMethodMatch(superclass, declared);
            superclass = superclass.getSuperclass();
        }

        ExternalMethodMatch direct = findInheritedInterfaceMethod(ownerClass, method, seen);
        if (direct != null) return direct;

        for (Class<?> current = ownerClass.getSuperclass(); current != null; current = current.getSuperclass()) {
            ExternalMethodMatch inherited = findInheritedInterfaceMethod(current, method, seen);
            if (inherited != null) return inherited;
        }
        return null;
    }

    private static ExternalMethodMatch findInheritedInterfaceMethod(Class<?> type, Method method, Set<Class<?>> seen) {
        for (Class<?> iface : type.getInterfaces()) {
            if (!seen.add(iface)) continue;
            Method declared = findDeclaredMethod(iface, method);
            if (declared != null) return new ExternalMethodMatch(iface, declared);
            ExternalMethodMatch deeper = findInheritedInterfaceMethod(iface, method, seen);
            if (deeper != null) return deeper;
        }
        return null;
    }

    private static Method findDeclaredMethod(Class<?> type, Method template) {
        try {
            Method method = type.getDeclaredMethod(template.getName(), template.getParameterTypes());
            return method.isSynthetic() || method.isBridge() ? null : method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static String sourceEntryName(Class<?> externalClass) {
        String binaryName = externalClass.getName();
        String packageName = externalClass.getPackageName();
        String relativeName = packageName.isEmpty() ? binaryName : binaryName.substring(packageName.length() + 1);
        int nested = relativeName.indexOf('$');
        String topLevel = nested >= 0 ? relativeName.substring(0, nested) : relativeName;
        StringBuilder entry = new StringBuilder();
        Module module = externalClass.getModule();
        if (module != null && module.isNamed()) {
            entry.append(module.getName()).append('/');
        }
        if (!packageName.isEmpty()) {
            entry.append(packageName.replace('.', '/')).append('/');
        }
        entry.append(topLevel).append(".java");
        return entry.toString();
    }

    private static String typeName(Class<?> type) {
        if (type.isArray()) return typeName(type.getComponentType()) + "[]";
        String canonicalName = type.getCanonicalName();
        return canonicalName != null ? canonicalName : type.getTypeName();
    }

    private static String normalizeJavadocComment(String content) {
        if (content == null || content.isEmpty()) return "";
        String[] lines = content.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        int start = 0;
        int end = lines.length;
        while (start < end && lines[start].trim().isEmpty()) {
            start++;
        }
        while (end > start && lines[end - 1].trim().isEmpty()) {
            end--;
        }
        StringBuilder normalized = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (normalized.length() > 0) normalized.append('\n');
            normalized.append(lines[i].replaceFirst("^\\s*\\* ?", ""));
        }
        return normalized.toString().trim();
    }

    private static List<String> parameterTypeNames(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        List<String> result = new ArrayList<>(parameterTypes.length);
        for (Class<?> parameterType : parameterTypes) {
            result.add(typeName(parameterType));
        }
        return result;
    }

    private static Path detectJdkSrcZip() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.isEmpty()) return null;

        Path home = Path.of(javaHome);
        Path direct = home.resolve("lib/src.zip");
        if (Files.isRegularFile(direct)) return direct;

        Path parent = home.getParent();
        if (parent == null) return null;

        Path sibling = parent.resolve("lib/src.zip");
        return Files.isRegularFile(sibling) ? sibling : null;
    }

    /**
     * Represents method metadata extracted from an external class (typically JDK classes).
     * Captures the method signature (name, parameter types, return type) and its raw
     * Javadoc comment text. Used as an intermediate representation before converting
     * to {@link SimpleGroovyMethodDoc} for rendering.
     *
     * <p>The raw comment text may contain {@code {@inheritDoc}} markers that are
     * expanded during cache construction.</p>
     */
    private static final class ExternalMethodData {
        private final String name;
        private final String returnTypeName;
        private final List<String> parameterTypeNames;
        private final String rawCommentText;

        private ExternalMethodData(String name, String returnTypeName, List<String> parameterTypeNames, String rawCommentText) {
            this.name = name;
            this.returnTypeName = returnTypeName;
            this.parameterTypeNames = parameterTypeNames;
            this.rawCommentText = rawCommentText;
        }

        /**
         * Converts this method data into a fully-materialized {@link SimpleGroovyMethodDoc}
         * suitable for rendering by groovydoc templates. Sets up method name, return type,
         * parameters, and raw comment text.
         *
         * @param owner the groovydoc representation of the external class that owns this method
         * @return a groovydoc method representation with all fields populated
         */
        private GroovyMethodDoc toMethodDoc(ExternalGroovyClassDoc owner) {
            SimpleGroovyMethodDoc methodDoc = new SimpleGroovyMethodDoc(name, owner);
            methodDoc.setReturnType(new SimpleGroovyType(returnTypeName));
            for (int i = 0; i < parameterTypeNames.size(); i++) {
                SimpleGroovyParameter parameter = new SimpleGroovyParameter("arg" + i);
                parameter.setType(new SimpleGroovyType(parameterTypeNames.get(i)));
                methodDoc.add(parameter);
            }
            methodDoc.setRawCommentText(rawCommentText);
            return methodDoc;
        }
    }

    /**
     * Uniquely identifies a method within an external class by its name and
     * parameter type names. Used as a cache key for storing and retrieving
     * Javadoc comment text for specific methods.
     *
     * @param name the method name
     * @param parameterTypeNames the qualified names of parameter types in order
     */
    private record MethodKey(String name, List<String> parameterTypeNames) {
        /**
         * Creates a {@code MethodKey} from a reflected {@link Method}.
         *
         * @param method the reflected method
         * @return a cache key representing this method
         */
        private static MethodKey of(Method method) {
            return new MethodKey(method.getName(), ExternalJavadocSupport.parameterTypeNames(method));
        }
    }

    /**
     * Uniquely identifies a method within a specific external class hierarchy
     * by combining the owner class with a method key. Used during recursive
     * resolution of {@code {@inheritDoc}} to prevent infinite loops when
     * cyclic inheritance patterns are encountered.
     *
     * @param ownerClass the class declaring the method
     * @param methodKey the method identifier (name and parameter types)
     */
    private record ExternalMethodKey(Class<?> ownerClass, MethodKey methodKey) {
    }

    /**
     * Represents a method found while walking an external class's inheritance chain
     * during {@code {@inheritDoc}} resolution. Pairs the class that declares the method
     * with the reflected method object itself.
     *
     * @param ownerClass the class in which this method is declared
     * @param method the reflected method object
     */
    private record ExternalMethodMatch(Class<?> ownerClass, Method method) {
    }
}
