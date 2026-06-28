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
package org.apache.groovy.docgenerator

import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ParseResult
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.type.ArrayType
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.IntersectionType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.type.TypeParameter
import com.github.javaparser.ast.type.UnionType
import com.github.javaparser.ast.type.VoidType
import com.github.javaparser.ast.type.WildcardType
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.control.ResolveVisitor

/**
 * JavaParser-backed view of the extension source files consumed by {@link MockSourceGenerator}.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class JavaExtensionSourceSet {
    private final JavaParser parser = new JavaParser(
            new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE)
    )
    private final List<ParsedUnit> units = []
    private boolean dirty = true
    private List<JavaExtensionMethod> cachedMethods = []
    private Map<String, TypeDeclaration<?>> declarationsByFqcn = [:]
    private Map<String, Set<String>> simpleNameIndex = newSimpleNameIndex()
    private Set<String> knownTypes = new LinkedHashSet<>()
    private Map<CompilationUnit, JavaExtensionContext> contextsByUnit = [:]

    /**
     * Adds one extension source file to the in-memory model.
     *
     * @param file Java source file containing extension methods
     */
    void addSource(File file) {
        if (!file?.exists()) return

        ParseResult<CompilationUnit> result
        try {
            result = parser.parse(file)
        } catch (ParseProblemException e) {
            throw new IllegalStateException("Unable to parse ${file}: ${e.message}", e)
        }
        if (!result.result.present) {
            def details = result.problems.collect { it.toString() }.join(System.lineSeparator())
            throw new IllegalStateException("Unable to parse ${file}:${System.lineSeparator()}${details}")
        }

        units << new ParsedUnit(
                file: file,
                compilationUnit: result.result.get()
        )
        dirty = true
    }

    /**
     * Returns all parsed extension methods, rebuilding cached structures only when
     * sources have changed.
     */
    List<JavaExtensionMethod> getMethods() {
        rebuildIfNeeded()
        cachedMethods
    }

    /**
     * Resolves receiver metadata for the supplied fully-qualified class name.
     *
     * @param fqcn receiver type name (arrays allowed)
     * @return receiver metadata including primitive/interface markers
     */
    ReceiverTypeInfo typeInfoForFqcn(String fqcn) {
        rebuildIfNeeded()
        resolveTypeInfo(fqcn)
    }

    private ReceiverTypeInfo resolveTypeInfo(String fqcn) {
        String base = stripArraySuffix(fqcn)
        if (!base) {
            return new ReceiverTypeInfo(canonicalName: fqcn, primitive: false, interfaceType: false)
        }

        if (JavaExtensionContext.PRIMITIVES.contains(base)) {
            return new ReceiverTypeInfo(canonicalName: fqcn, primitive: true, interfaceType: false)
        }

        TypeDeclaration<?> declaration = declarationsByFqcn[base]
        if (declaration instanceof ClassOrInterfaceDeclaration) {
            return new ReceiverTypeInfo(canonicalName: fqcn, primitive: false, interfaceType: declaration.isInterface())
        }

        Class<?> resolvedClass = tryLoad(base)
        new ReceiverTypeInfo(
                canonicalName: fqcn,
                primitive: false,
                interfaceType: resolvedClass?.isInterface() ?: false
        )
    }

    private void rebuildIfNeeded() {
        if (!dirty) return

        resetCaches()
        indexDeclaredTypes()
        indexKnownTypesBySimpleName()
        createContextsByUnit()
        collectMethodViews()
        dirty = false
    }

    private void resetCaches() {
        declarationsByFqcn = [:]
        simpleNameIndex = newSimpleNameIndex()
        knownTypes = new LinkedHashSet<>()
        contextsByUnit = [:]
        cachedMethods = []
    }

    private void indexDeclaredTypes() {
        units.each { ParsedUnit unit ->
            String packageName = packageNameOf(unit.compilationUnit)
            unit.compilationUnit.types.each { TypeDeclaration<?> declaration ->
                collectTypes(declaration, packageName, null)
            }
        }
    }

    private void indexKnownTypesBySimpleName() {
        knownTypes.addAll(declarationsByFqcn.keySet())
        declarationsByFqcn.keySet().each { String fqcn ->
            simpleNameIndex[simpleNameOf(fqcn)] << fqcn
        }
    }

    private void createContextsByUnit() {
        units.each { ParsedUnit unit ->
            contextsByUnit[unit.compilationUnit] = new JavaExtensionContext(unit.compilationUnit, simpleNameIndex, knownTypes)
        }
    }

    private void collectMethodViews() {
        declarationsByFqcn.each { String fqcn, TypeDeclaration<?> declaration ->
            JavaExtensionContext context = contextsByUnit[declaration.findCompilationUnit().orElse(null)]
            methodDeclarationsOf(declaration).each { MethodDeclaration method ->
                cachedMethods << createMethodView(method, declaration, context)
            }
        }
    }

    private void collectTypes(TypeDeclaration<?> declaration, String packageName, String ownerFqcn) {
        String fqcn = qualifiedTypeName(declaration.nameAsString, packageName, ownerFqcn)
        declarationsByFqcn[fqcn] = declaration
        nestedTypeDeclarationsOf(declaration).each { TypeDeclaration<?> nested ->
            collectTypes(nested, packageName, fqcn)
        }
    }

    private JavaExtensionMethod createMethodView(MethodDeclaration method, TypeDeclaration<?> declaringType, JavaExtensionContext context) {
        def scopeTypeParameters = scopeTypeParametersOf(declaringType, method)
        List<JavaExtensionParameter> parameters = renderParameters(method, context, scopeTypeParameters)
        String receiverTypeName = resolveReceiverTypeName(method, context, scopeTypeParameters, parameters)

        new JavaExtensionMethod(
                name: method.nameAsString,
                declaringClassName: declaringType.nameAsString,
                publicMethod: method.isPublic(),
                staticMethod: method.isStatic(),
                deprecated: method.annotations.any { context.resolveTypeName(it.nameAsString) == 'java.lang.Deprecated' },
                receiverTypeName: receiverTypeName,
                receiverTypeInfo: receiverTypeName ? resolveTypeInfo(receiverTypeName) : null,
                returnType: context.renderType(method.type, scopeTypeParameters),
                parameters: parameters,
                typeParameters: method.typeParameters.collect {
                    context.renderTypeParameter(it, scopeTypeParameters)
                },
                exceptions: renderExceptions(method, context, scopeTypeParameters),
                javadoc: JavadocInfo.parse(method.javadocComment.orElse(null))
        )
    }

    private static List<JavaExtensionParameter> renderParameters(MethodDeclaration method, JavaExtensionContext context, Set<String> scopeTypeParameters) {
        method.parameters.collect { parameter ->
            new JavaExtensionParameter(
                    name: parameter.nameAsString,
                    type: context.renderType(parameter.type, scopeTypeParameters, parameter.varArgs),
                    varArgs: parameter.varArgs
            )
        }
    }

    private static List<String> renderExceptions(MethodDeclaration method, JavaExtensionContext context, Set<String> scopeTypeParameters) {
        method.thrownExceptions.collect { context.renderType(it, scopeTypeParameters) }
    }

    private static String resolveReceiverTypeName(MethodDeclaration method, JavaExtensionContext context, Set<String> scopeTypeParameters, List<JavaExtensionParameter> parameters) {
        if (parameters.empty) return null
        MockSourceGenerator.resolveJdkClassName(context.eraseType(method.parameters[0].type, scopeTypeParameters))
    }

    private static Set<String> scopeTypeParametersOf(TypeDeclaration<?> declaringType, MethodDeclaration method) {
        def scopeTypeParameters = new LinkedHashSet<String>(ownerTypeParametersOf(declaringType))
        scopeTypeParameters.addAll((List<String>) method.typeParameters*.nameAsString)
        scopeTypeParameters
    }

    @CompileDynamic
    private static List<String> ownerTypeParametersOf(TypeDeclaration<?> declaringType) {
        declaringType.respondsTo('getTypeParameters') ? declaringType.typeParameters*.nameAsString : []
    }

    private static String packageNameOf(CompilationUnit compilationUnit) {
        compilationUnit.packageDeclaration.map { it.nameAsString }.orElse('')
    }

    private static String qualifiedTypeName(String typeName, String packageName, String ownerFqcn) {
        ownerFqcn ? ownerFqcn + '.' + typeName : (packageName ? packageName + '.' + typeName : typeName)
    }

    private static List<TypeDeclaration<?>> nestedTypeDeclarationsOf(TypeDeclaration<?> declaration) {
        declaration.members.findAll { BodyDeclaration member -> member instanceof TypeDeclaration } as List<TypeDeclaration<?>>
    }

    private static List<MethodDeclaration> methodDeclarationsOf(TypeDeclaration<?> declaration) {
        declaration.members.findAll { BodyDeclaration member -> member instanceof MethodDeclaration } as List<MethodDeclaration>
    }

    private static String stripArraySuffix(String name) {
        name?.replaceAll(/(\[\])+$/, '')
    }

    private static String simpleNameOf(String fqcn) {
        int dot = fqcn.lastIndexOf('.')
        dot < 0 ? fqcn : fqcn.substring(dot + 1)
    }

    /**
     * Attempts to load a type by canonical name, falling back to nested-class
     * binary names ({@code Outer$Inner}) when needed.
     *
     * @param candidate candidate canonical type name
     * @return loaded class, or {@code null} when resolution fails
     */
    static Class<?> tryLoad(String candidate) {
        if (!candidate) return null
        try {
            return Class.forName(candidate)
        } catch (Throwable ignored) {
            int lastDot = candidate.lastIndexOf('.')
            while (lastDot > 0) {
                candidate = candidate.substring(0, lastDot) + '$' + candidate.substring(lastDot + 1)
                try {
                    return Class.forName(candidate)
                } catch (Throwable ignoredAgain) {
                    lastDot = candidate.lastIndexOf('.')
                }
            }
            return null
        }
    }

    private static Map<String, Set<String>> newSimpleNameIndex() {
        [:].withDefault { new LinkedHashSet<String>() }
    }
}

/**
 * Type rendering and name-resolution context derived from one compilation unit.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class JavaExtensionContext {
    public static final Set<String> PRIMITIVES = Set.of('boolean', 'byte', 'char', 'double', 'float', 'int', 'long', 'short')
    private static final List<String> DEFAULT_IMPORT_LIST = List.of(ResolveVisitor.DEFAULT_IMPORTS)
    private static final String VOID_TYPE = 'void'

    private final Map<String, String> explicitImports = [:]
    private final List<String> wildcardImports = []
    private final Map<String, Set<String>> simpleNameIndex
    private final Set<String> knownTypes
    private final String packageName

    /**
     * Creates a rendering context with import/package/type indexes for one unit.
     */
    JavaExtensionContext(
            CompilationUnit compilationUnit,
            Map<String, Set<String>> simpleNameIndex,
            Set<String> knownTypes
    ) {
        this.simpleNameIndex = simpleNameIndex
        this.knownTypes = knownTypes
        this.packageName = packageNameOf(compilationUnit)

        compilationUnit.imports.each { ImportDeclaration importDeclaration ->
            if (importDeclaration.isStatic()) return
            if (importDeclaration.asterisk) {
                wildcardImports << importDeclaration.nameAsString
            } else {
                explicitImports[importDeclaration.name.identifier] = importDeclaration.nameAsString
            }
        }
    }

    /**
     * Renders a JavaParser type to the canonical textual form expected by
     * mock-source generation.
     */
    String renderType(Type type, Set<String> typeParameterNames = Collections.emptySet(), boolean varArgs = false) {
        String rendered
        if (type instanceof VoidType) {
            rendered = VOID_TYPE
        } else if (type instanceof ArrayType) {
            rendered = renderType(type.componentType, typeParameterNames) + '[]'
        } else if (type instanceof ClassOrInterfaceType) {
            rendered = renderClassOrInterfaceType((ClassOrInterfaceType) type, typeParameterNames)
        } else if (type instanceof WildcardType) {
            rendered = renderWildcardType((WildcardType) type, typeParameterNames)
        } else if (type instanceof IntersectionType) {
            rendered = renderTypeList(((IntersectionType) type).elements as Iterable<Type>, typeParameterNames, ' & ')
        } else if (type instanceof UnionType) {
            rendered = renderTypeList(((UnionType) type).elements as Iterable<Type>, typeParameterNames, ' | ')
        } else {
            rendered = type.toString()
        }

        toVarArgIfNeeded(rendered, varArgs)
    }

    /**
     * Renders the erased type name used for receiver bucketing.
     */
    String eraseType(Type type, Set<String> typeParameterNames = Collections.emptySet()) {
        if (type instanceof ArrayType) {
            return eraseType(type.componentType, typeParameterNames) + '[]'
        }
        if (type instanceof ClassOrInterfaceType) {
            return eraseClassOrInterfaceType((ClassOrInterfaceType) type, typeParameterNames)
        }
        type.toString()
    }

    /**
     * Renders a type-parameter declaration with resolved bounds.
     */
    String renderTypeParameter(TypeParameter typeParameter, Set<String> inheritedTypeParameters) {
        def scopeTypeParameters = new LinkedHashSet<String>(inheritedTypeParameters)
        scopeTypeParameters << typeParameter.nameAsString

        def rendered = new StringBuilder(typeParameter.nameAsString)
        if (!typeParameter.typeBound.empty) {
            rendered << ' extends ' << typeParameter.typeBound.collect {
                renderType(it, scopeTypeParameters)
            }.join(' & ')
        }
        rendered.toString()
    }

    /**
     * Resolves a possibly-short type name against explicit imports, wildcard
     * imports, same-package types, default imports, and known parsed symbols.
     */
    String resolveTypeName(String name, Set<String> typeParameterNames = Collections.emptySet()) {
        if (!name || isIntrinsicName(name, typeParameterNames)) return name

        int dot = name.indexOf('.')
        if (dot < 0) return resolveSimpleName(name, typeParameterNames)

        String first = name.substring(0, dot)
        if (!startsWithUppercase(first) && !explicitImports.containsKey(first)) {
            return name
        }

        String resolvedFirst = resolveSimpleName(first, typeParameterNames)
        resolvedFirst == first ? name : resolvedFirst + name.substring(dot)
    }

    private String renderClassOrInterfaceType(ClassOrInterfaceType type, Set<String> typeParameterNames) {
        String name
        if (type.scope.present) {
            name = rawScopedName(type.scope.get(), typeParameterNames) + '.' + type.nameAsString
        } else {
            name = resolveSimpleName(type.nameAsString, typeParameterNames)
        }

        if (!type.typeArguments.present || type.typeArguments.get().empty) {
            return name
        }
        name + '<' + renderTypeList(type.typeArguments.get(), typeParameterNames, ', ') + '>'
    }

    private String rawScopedName(ClassOrInterfaceType type, Set<String> typeParameterNames) {
        if (type.scope.present) {
            return rawScopedName(type.scope.get(), typeParameterNames) + '.' + type.nameAsString
        }
        resolveSimpleName(type.nameAsString, typeParameterNames)
    }

    private String resolveSimpleName(String name, Set<String> typeParameterNames) {
        if (isIntrinsicName(name, typeParameterNames)) return name
        if (explicitImports.containsKey(name)) return explicitImports[name]

        resolveInCurrentPackage(name)
                ?: resolveFromImportPrefixes(name, wildcardImports)
                ?: resolveFromImportPrefixes(name, DEFAULT_IMPORT_LIST)
                ?: uniquelyMatchedSimpleName(name)
                ?: name
    }

    private boolean knownType(String candidate) {
        if (knownTypes.contains(candidate)) return true
        if (JavaExtensionSourceSet.tryLoad(candidate) != null) {
            knownTypes.add(candidate)
            return true
        }
        false
    }

    private String eraseClassOrInterfaceType(ClassOrInterfaceType classType, Set<String> typeParameterNames) {
        if (classType.scope.present) {
            return rawScopedName(classType.scope.get(), typeParameterNames) + '.' + classType.nameAsString
        }
        resolveSimpleName(classType.nameAsString, typeParameterNames)
    }

    private String renderWildcardType(WildcardType wildcard, Set<String> typeParameterNames) {
        if (wildcard.extendedType.present) {
            return '? extends ' + renderType(wildcard.extendedType.get(), typeParameterNames)
        }
        if (wildcard.superType.present) {
            return '? super ' + renderType(wildcard.superType.get(), typeParameterNames)
        }
        '?'
    }

    private String resolveInCurrentPackage(String name) {
        if (!packageName) return null
        String candidate = qualify(packageName, name)
        knownType(candidate) ? candidate : null
    }

    private String resolveFromImportPrefixes(String name, Iterable<String> prefixes) {
        for (String prefix : prefixes) {
            String candidate = qualify(prefix, name)
            if (knownType(candidate)) return candidate
        }
        null
    }

    private String uniquelyMatchedSimpleName(String name) {
        Set<String> matches = simpleNameIndex[name]
        matches?.size() == 1 ? matches.first() : null
    }

    private String renderTypeList(Iterable<Type> types, Set<String> typeParameterNames, String separator) {
        types.collect { renderType(it, typeParameterNames) }.join(separator)
    }

    private static boolean isIntrinsicName(String name, Set<String> typeParameterNames) {
        PRIMITIVES.contains(name) || name == VOID_TYPE || typeParameterNames.contains(name)
    }

    private static boolean startsWithUppercase(String name) {
        name && Character.isUpperCase(name.charAt(0))
    }

    private static String qualify(String prefix, String simpleName) {
        if (!prefix) return simpleName
        prefix.endsWith('.') ? prefix + simpleName : prefix + '.' + simpleName
    }

    private static String toVarArgIfNeeded(String rendered, boolean varArgs) {
        if (!varArgs || !rendered.endsWith('[]')) return rendered
        rendered[0..-3] + '...'
    }

    private static String packageNameOf(CompilationUnit compilationUnit) {
        compilationUnit.packageDeclaration.map { it.nameAsString }.orElse('')
    }
}

/**
 * Serializable view of one extension method consumed by {@link MockSourceGenerator}.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class JavaExtensionMethod {
    String name
    String declaringClassName
    boolean publicMethod
    boolean staticMethod
    boolean deprecated
    String receiverTypeName
    ReceiverTypeInfo receiverTypeInfo
    String returnType
    List<JavaExtensionParameter> parameters = []
    List<String> typeParameters = []
    List<String> exceptions = []
    JavadocInfo javadoc
}

/**
 * Serializable view of one method parameter.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class JavaExtensionParameter {
    String type
    String name
    boolean varArgs
}

/**
 * Receiver metadata used to decide mock type shape and output location.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class ReceiverTypeInfo {
    String canonicalName
    boolean primitive
    boolean interfaceType
}

/**
 * Parsed representation of method javadoc text and tags.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class JavadocInfo {
    String description = ''
    List<JavadocTagInfo> tags = []

    /**
     * Parses raw javadoc comments into plain description text and tag entries.
     *
     * @param comment raw javadoc comment node
     * @return parsed javadoc representation
     */
    static JavadocInfo parse(JavadocComment comment) {
        if (comment == null) return new JavadocInfo()

        def descriptionLines = []
        def tags = []
        JavadocTagInfo currentTag = null

        comment.content.readLines().collect {
            it.replaceFirst(/^\s*\* ?/, '')
        }.each { String line ->
            if (line.startsWith('@')) {
                def matcher = (line =~ /^@(\S+)\s*(.*)$/)
                if (matcher.matches()) {
                    currentTag = new JavadocTagInfo(name: matcher.group(1), value: matcher.group(2))
                    tags << currentTag
                }
            } else if (currentTag != null) {
                currentTag.value = currentTag.value ? currentTag.value + '\n' + line : line
            } else {
                descriptionLines << line
            }
        }

        new JavadocInfo(
                description: descriptionLines.join('\n').trim(),
                tags: tags
        )
    }
}

/**
 * Serializable javadoc tag entry.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class JavadocTagInfo {
    String name
    String value
}

/**
 * Parsed source unit paired with its originating file.
 *
 * @since 6.0.0
 */
@CompileStatic
@PackageScope
final class ParsedUnit {
    File file
    CompilationUnit compilationUnit
}
