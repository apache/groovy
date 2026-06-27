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
package org.codehaus.groovy.tools.stubgenerator

import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ParseResult
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.RecordDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.ArrayInitializerExpr
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.CastExpr
import com.github.javaparser.ast.expr.CharLiteralExpr
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.DoubleLiteralExpr
import com.github.javaparser.ast.expr.EnclosedExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.LongLiteralExpr
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.NullLiteralExpr
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.ast.expr.TextBlockLiteralExpr
import com.github.javaparser.ast.expr.UnaryExpr
import com.github.javaparser.ast.type.ArrayType
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.IntersectionType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.type.TypeParameter
import com.github.javaparser.ast.type.UnionType
import com.github.javaparser.ast.type.VoidType
import com.github.javaparser.ast.type.WildcardType
import org.codehaus.groovy.control.ResolveVisitor

import static groovy.io.FileType.FILES

/**
 * JavaParser-backed source set used by stub-generator tests to inspect generated Java stubs.
 */
final class JavaParserSourceSet {
    private final JavaParser parser = new JavaParser(
            new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE)
    )
    private final List<ParsedUnit> units = []
    private boolean dirty = true
    private List<JavaSourceClass> cachedClasses = []

    void addSourceTree(File root) {
        if (!root?.exists()) return

        root.eachFileRecurse(FILES) { File file ->
            if (!file.name.endsWith('.java')) return

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
                    text: file.getText('UTF-8'),
                    compilationUnit: result.result.get()
            )
            dirty = true
        }
    }

    JavaSourceClass[] getClasses() {
        rebuildIfNeeded()
        cachedClasses as JavaSourceClass[]
    }

    private void rebuildIfNeeded() {
        if (!dirty) return

        Map<TypeDeclaration<?>, String> fqcnByDeclaration = [:]
        List<TypeRecord> orderedTypes = []
        units.each { ParsedUnit unit ->
            String packageName = unit.compilationUnit.packageDeclaration.map { it.nameAsString }.orElse('')
            unit.compilationUnit.types.each { TypeDeclaration<?> declaration ->
                collectTypes(unit, declaration, packageName, null, fqcnByDeclaration, orderedTypes)
            }
        }

        Map<String, Set<String>> simpleNameIndex = [:].withDefault { new LinkedHashSet<String>() }
        Set<String> knownTypes = new LinkedHashSet<>(fqcnByDeclaration.values())
        knownTypes.each { String fqcn ->
            simpleNameIndex[simpleNameOf(fqcn)] << fqcn
        }

        Map<CompilationUnit, JavaSourceContext> contextsByUnit = [:]
        units.each { ParsedUnit unit ->
            Set<String> unitTypes = fqcnByDeclaration.findAll { TypeDeclaration<?> declaration, String ignored ->
                declaration.findCompilationUnit().orElse(null) == unit.compilationUnit
            }.values() as Set<String>
            contextsByUnit[unit.compilationUnit] = new JavaSourceContext(unit, fqcnByDeclaration, simpleNameIndex, knownTypes, unitTypes)
        }

        cachedClasses = orderedTypes.collect { TypeRecord record ->
            new JavaSourceClass(record.fqcn, record.declaration, contextsByUnit[record.unit.compilationUnit])
        }
        dirty = false
    }

    private static void collectTypes(
            ParsedUnit unit,
            TypeDeclaration<?> declaration,
            String packageName,
            String ownerFqcn,
            Map<TypeDeclaration<?>, String> fqcnByDeclaration,
            List<TypeRecord> orderedTypes
    ) {
        String fqcn = ownerFqcn ? ownerFqcn + '.' + declaration.nameAsString
                : (packageName ? packageName + '.' + declaration.nameAsString : declaration.nameAsString)
        fqcnByDeclaration[declaration] = fqcn
        orderedTypes << new TypeRecord(unit: unit, declaration: declaration, fqcn: fqcn)

        declaration.members.findAll { BodyDeclaration member -> member instanceof TypeDeclaration }
                .each { TypeDeclaration nested ->
                    collectTypes(unit, nested, packageName, fqcn, fqcnByDeclaration, orderedTypes)
                }
    }

    private static String simpleNameOf(String fqcn) {
        int dot = fqcn.lastIndexOf('.')
        dot < 0 ? fqcn : fqcn.substring(dot + 1)
    }
}

final class JavaSourceClass {
    final String fullyQualifiedName
    final JavaSourceFile source
    final List<JavaSourceMethod> methods
    final List<JavaSourceConstructor> constructors
    final List<JavaSourceAnnotation> annotations

    private final TypeDeclaration<?> declaration
    private final JavaSourceContext context
    private final Map<String, JavaSourceField> fieldsByName

    JavaSourceClass(String fullyQualifiedName, TypeDeclaration<?> declaration, JavaSourceContext context) {
        this.fullyQualifiedName = fullyQualifiedName
        this.declaration = declaration
        this.context = context
        this.source = context.source
        this.methods = declaration.members.findAll { it instanceof MethodDeclaration }
                .collect { new JavaSourceMethod((MethodDeclaration) it, this, context) }
        this.constructors = declaration.members.findAll { it instanceof ConstructorDeclaration }
                .collect { new JavaSourceConstructor((ConstructorDeclaration) it, this, context) }
        this.annotations = declaration.annotations.collect { new JavaSourceAnnotation(it, context) }
        this.fieldsByName = [:]
        declaration.members.findAll { it instanceof FieldDeclaration }.each { FieldDeclaration field ->
            field.variables.each { variable ->
                fieldsByName[variable.nameAsString] = new JavaSourceField(variable.nameAsString, variable.initializer.map {
                    context.renderExpression(it)
                }.orElse(null))
            }
        }
    }

    JavaSourceField getFieldByName(String fieldName) {
        fieldsByName[fieldName]
    }

    List<String> getImports() {
        source.imports
    }

    List<String> getInterfaces() {
        if (declaration instanceof ClassOrInterfaceDeclaration) {
            def classOrInterface = (ClassOrInterfaceDeclaration) declaration
            def types = classOrInterface.isInterface() ? classOrInterface.extendedTypes : classOrInterface.implementedTypes
            return types.collect { context.renderType(it, typeParameterNames()) }
        }
        if (declaration instanceof EnumDeclaration) {
            return ((EnumDeclaration) declaration).implementedTypes.collect { context.renderType(it, typeParameterNames()) }
        }
        if (declaration instanceof RecordDeclaration) {
            return ((RecordDeclaration) declaration).implementedTypes.collect { context.renderType(it, typeParameterNames()) }
        }
        []
    }

    String getBaseClass() {
        if (declaration instanceof ClassOrInterfaceDeclaration) {
            def classOrInterface = (ClassOrInterfaceDeclaration) declaration
            if (classOrInterface.isInterface()) return null
            if (!classOrInterface.extendedTypes.empty) {
                return context.renderType(classOrInterface.extendedTypes[0], typeParameterNames())
            }
            return 'java.lang.Object'
        }
        if (declaration instanceof EnumDeclaration) {
            return 'java.lang.Enum'
        }
        if (declaration instanceof RecordDeclaration) {
            return 'java.lang.Record'
        }
        null
    }

    Set<String> typeParameterNames() {
        declaration.respondsTo('getTypeParameters')
                ? declaration.typeParameters*.nameAsString as Set<String>
                : Collections.emptySet()
    }
}

final class JavaSourceMethod {
    final String name
    final List<JavaSourceAnnotation> annotations

    private final MethodDeclaration declaration
    private final JavaSourceClass owner
    private final JavaSourceContext context

    JavaSourceMethod(MethodDeclaration declaration, JavaSourceClass owner, JavaSourceContext context) {
        this.declaration = declaration
        this.owner = owner
        this.context = context
        this.name = declaration.nameAsString
        this.annotations = declaration.annotations.collect { new JavaSourceAnnotation(it, context) }
    }

    String getSignature() {
        def scopeTypeParameters = new LinkedHashSet<String>(owner.typeParameterNames())
        scopeTypeParameters.addAll(declaration.typeParameters*.nameAsString)

        def parts = []
        if (!declaration.modifiers.empty) {
            parts << declaration.modifiers.collect { it.keyword.asString() }.join(' ')
        }
        if (!declaration.typeParameters.empty) {
            parts << '<' + declaration.typeParameters.collect {
                context.renderTypeParameter(it, scopeTypeParameters)
            }.join(', ') + '>'
        }
        parts << context.renderType(declaration.type, scopeTypeParameters)
        parts << "${declaration.nameAsString}(" + declaration.parameters.collect {
            context.renderParameter(it, scopeTypeParameters)
        }.join(', ') + ')'

        String signature = parts.findAll { it }.join(' ')
        if (!declaration.thrownExceptions.empty) {
            signature += ' throws ' + declaration.thrownExceptions.collect {
                context.renderType(it, scopeTypeParameters)
            }.join(', ')
        }
        signature
    }
}

final class JavaSourceConstructor {
    private final ConstructorDeclaration declaration
    private final JavaSourceClass owner
    private final JavaSourceContext context

    JavaSourceConstructor(ConstructorDeclaration declaration, JavaSourceClass owner, JavaSourceContext context) {
        this.declaration = declaration
        this.owner = owner
        this.context = context
    }

    @Override
    String toString() {
        def scopeTypeParameters = new LinkedHashSet<String>(owner.typeParameterNames())
        scopeTypeParameters.addAll(declaration.typeParameters*.nameAsString)

        def parts = []
        if (!declaration.modifiers.empty) {
            parts << declaration.modifiers.collect { it.keyword.asString() }.join(' ')
        }
        parts << owner.fullyQualifiedName + '(' + declaration.parameters.collect {
            context.renderType(it.type, scopeTypeParameters, it.varArgs)
        }.join(',') + ')'

        String signature = parts.findAll { it }.join(' ')
        if (!declaration.thrownExceptions.empty) {
            signature += ' throws ' + declaration.thrownExceptions.collect {
                context.renderType(it, scopeTypeParameters)
            }.join(', ')
        }
        signature
    }
}

final class JavaSourceAnnotation {
    private final AnnotationExpr expression
    private final JavaSourceContext context

    JavaSourceAnnotation(AnnotationExpr expression, JavaSourceContext context) {
        this.expression = expression
        this.context = context
    }

    String getType() {
        context.resolveTypeName(expression.nameAsString)
    }

    Object getProperty(String name) {
        if (name == 'type') return getType()
        if (name == 'class') return getClass()
        if (name == 'metaClass') return GroovySystem.metaClassRegistry.getMetaClass(getClass())

        annotationProperty(name)
    }

    private JavaSourceAnnotationValue annotationProperty(String name) {
        Expression value = null
        if (expression instanceof NormalAnnotationExpr) {
            value = ((NormalAnnotationExpr) expression).pairs.find { it.nameAsString == name }?.value
        } else if (expression instanceof SingleMemberAnnotationExpr && name == 'value') {
            value = ((SingleMemberAnnotationExpr) expression).memberValue
        }
        value != null ? new JavaSourceAnnotationValue(value, context) : null
    }

    String getNamedParameter(String name) {
        annotationProperty(name)?.toString()
    }

    @Override
    String toString() {
        context.renderAnnotation(expression)
    }
}

final class JavaSourceAnnotationValue {
    private final Expression expression
    private final JavaSourceContext context

    JavaSourceAnnotationValue(Expression expression, JavaSourceContext context) {
        this.expression = expression
        this.context = context
    }

    JavaSourceAnnotationValue getParameterValue() {
        this
    }

    Object getProperty(String name) {
        if (name == 'parameterValue') return getParameterValue()
        if (name == 'value') return getValue()
        if (name == 'class') return getClass()
        if (name == 'metaClass') return GroovySystem.metaClassRegistry.getMetaClass(getClass())

        expression instanceof AnnotationExpr
                ? new JavaSourceAnnotation((AnnotationExpr) expression, context).getProperty(name)
                : null
    }

    String getValue() {
        if (expression instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) expression).asString()
        }
        if (expression instanceof TextBlockLiteralExpr) {
            return ((TextBlockLiteralExpr) expression).asString()
        }
        context.renderExpression(expression)
    }

    @Override
    String toString() {
        context.renderExpression(expression)
    }
}

final class JavaSourceField {
    final String name
    final String initializationExpression

    JavaSourceField(String name, String initializationExpression) {
        this.name = name
        this.initializationExpression = initializationExpression
    }
}

final class JavaSourceFile {
    final String text
    final List<String> imports

    JavaSourceFile(String text, List<String> imports) {
        this.text = text
        this.imports = imports
    }

    @Override
    String toString() {
        text
    }
}

final class JavaSourceContext {
    private static final Set<String> PRIMITIVES = [
            'boolean', 'byte', 'char', 'double', 'float', 'int', 'long', 'short'
    ] as Set<String>

    final JavaSourceFile source
    final String packageName

    private final Map<String, String> explicitImports = [:]
    private final List<String> wildcardImports = []
    private final Map<TypeDeclaration<?>, String> fqcnByDeclaration
    private final Map<String, Set<String>> simpleNameIndex
    private final Set<String> knownTypes
    private final Set<String> unitTypes

    JavaSourceContext(
            ParsedUnit unit,
            Map<TypeDeclaration<?>, String> fqcnByDeclaration,
            Map<String, Set<String>> simpleNameIndex,
            Set<String> knownTypes,
            Set<String> unitTypes
    ) {
        this.packageName = unit.compilationUnit.packageDeclaration.map { it.nameAsString }.orElse('')
        this.source = new JavaSourceFile(unit.text, collectImports(unit.compilationUnit))
        this.fqcnByDeclaration = fqcnByDeclaration
        this.simpleNameIndex = simpleNameIndex
        this.knownTypes = knownTypes
        this.unitTypes = unitTypes

        unit.compilationUnit.imports.each { ImportDeclaration importDeclaration ->
            if (importDeclaration.isStatic()) return
            if (importDeclaration.asterisk) {
                wildcardImports << importDeclaration.nameAsString
            } else {
                explicitImports[importDeclaration.name.identifier] = importDeclaration.nameAsString
            }
        }
    }

    String renderParameter(parameter, Set<String> typeParameterNames) {
        "${renderType(parameter.type, typeParameterNames, parameter.varArgs)} ${parameter.nameAsString}"
    }

    String renderType(Type type, Set<String> typeParameterNames = Collections.emptySet(), boolean varArgs = false) {
        String rendered
        if (type instanceof VoidType) {
            rendered = 'void'
        } else if (type instanceof ArrayType) {
            rendered = renderType(type.componentType, typeParameterNames) + '[]'
        } else if (type instanceof ClassOrInterfaceType) {
            rendered = renderClassOrInterfaceType((ClassOrInterfaceType) type, typeParameterNames)
        } else if (type instanceof WildcardType) {
            def wildcard = (WildcardType) type
            if (wildcard.extendedType.present) {
                rendered = '? extends ' + renderType(wildcard.extendedType.get(), typeParameterNames)
            } else if (wildcard.superType.present) {
                rendered = '? super ' + renderType(wildcard.superType.get(), typeParameterNames)
            } else {
                rendered = '?'
            }
        } else if (type instanceof IntersectionType) {
            rendered = ((IntersectionType) type).elements.collect {
                renderType(it, typeParameterNames)
            }.join(' & ')
        } else if (type instanceof UnionType) {
            rendered = ((UnionType) type).elements.collect {
                renderType(it, typeParameterNames)
            }.join(' | ')
        } else {
            rendered = type.toString()
        }

        if (varArgs && rendered.endsWith('[]')) {
            rendered = rendered[0..-3] + '...'
        }
        rendered
    }

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

    String renderExpression(Expression expression) {
        if (expression instanceof StringLiteralExpr ||
                expression instanceof TextBlockLiteralExpr ||
                expression instanceof IntegerLiteralExpr ||
                expression instanceof LongLiteralExpr ||
                expression instanceof DoubleLiteralExpr ||
                expression instanceof BooleanLiteralExpr ||
                expression instanceof CharLiteralExpr ||
                expression instanceof NullLiteralExpr) {
            return expression.toString()
        }
        if (expression instanceof NameExpr) {
            return resolveTypeName(((NameExpr) expression).nameAsString)
        }
        if (expression instanceof FieldAccessExpr) {
            def fieldAccess = (FieldAccessExpr) expression
            return renderExpression(fieldAccess.scope) + '.' + fieldAccess.nameAsString
        }
        if (expression instanceof ClassExpr) {
            return renderType(((ClassExpr) expression).type) + '.class'
        }
        if (expression instanceof AnnotationExpr) {
            return renderAnnotation((AnnotationExpr) expression)
        }
        if (expression instanceof ArrayInitializerExpr) {
            return '{' + ((ArrayInitializerExpr) expression).values.collect {
                renderExpression(it)
            }.join(', ') + '}'
        }
        if (expression instanceof EnclosedExpr) {
            return '(' + renderExpression(((EnclosedExpr) expression).inner) + ')'
        }
        if (expression instanceof CastExpr) {
            def cast = (CastExpr) expression
            return '(' + renderType(cast.type) + ') ' + renderExpression(cast.expression)
        }
        if (expression instanceof UnaryExpr) {
            def unary = (UnaryExpr) expression
            return unary.operator.asString() + renderExpression(unary.expression)
        }
        if (expression instanceof BinaryExpr) {
            def binary = (BinaryExpr) expression
            return renderExpression(binary.left) + ' ' + binary.operator.asString() + ' ' + renderExpression(binary.right)
        }
        if (expression instanceof MethodCallExpr) {
            def call = (MethodCallExpr) expression
            def prefix = call.scope.map { renderExpression(it) + '.' }.orElse('')
            return prefix + call.nameAsString + '(' + call.arguments.collect { renderExpression(it) }.join(', ') + ')'
        }
        expression.toString()
    }

    String renderAnnotation(AnnotationExpr annotation) {
        String typeName = resolveTypeName(annotation.nameAsString)
        if (annotation instanceof MarkerAnnotationExpr) {
            return "@${typeName}"
        }
        if (annotation instanceof SingleMemberAnnotationExpr) {
            return "@${typeName}(" + renderExpression(((SingleMemberAnnotationExpr) annotation).memberValue) + ')'
        }
        def normal = (NormalAnnotationExpr) annotation
        "@${typeName}(" + normal.pairs.collect {
            "${it.nameAsString} = ${renderExpression(it.value)}"
        }.join(', ') + ')'
    }

    String resolveTypeName(String name, Set<String> typeParameterNames = Collections.emptySet()) {
        if (!name) return name
        if (PRIMITIVES.contains(name) || name == 'void' || typeParameterNames.contains(name)) return name

        int dot = name.indexOf('.')
        if (dot < 0) return resolveSimpleName(name, typeParameterNames)

        String first = name.substring(0, dot)
        if (!Character.isUpperCase(first.charAt(0)) && !explicitImports.containsKey(first)) {
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
        name + '<' + type.typeArguments.get().collect { renderType(it, typeParameterNames) }.join(', ') + '>'
    }

    private String rawScopedName(ClassOrInterfaceType type, Set<String> typeParameterNames) {
        if (type.scope.present) {
            return rawScopedName(type.scope.get(), typeParameterNames) + '.' + type.nameAsString
        }
        resolveSimpleName(type.nameAsString, typeParameterNames)
    }

    private String resolveSimpleName(String name, Set<String> typeParameterNames) {
        if (PRIMITIVES.contains(name) || name == 'void' || typeParameterNames.contains(name)) return name
        if (explicitImports.containsKey(name)) return explicitImports[name]

        if (packageName) {
            String candidate = packageName + '.' + name
            if (knownType(candidate)) return candidate
        }

        Set<String> localMatches = unitTypes.findAll { String candidate -> candidate.endsWith('.' + name) || candidate == name } as Set<String>
        if (localMatches.size() == 1) return localMatches.first()

        for (String importPrefix : wildcardImports) {
            String candidate = importPrefix + '.' + name
            if (knownType(candidate)) return candidate
        }

        for (String defaultImport : ResolveVisitor.DEFAULT_IMPORTS) {
            String candidate = defaultImport + name
            if (knownType(candidate)) return candidate
        }

        Set<String> matches = simpleNameIndex[name]
        if (matches?.size() == 1) return matches.first()

        name
    }

    private boolean knownType(String candidate) {
        knownTypes.contains(candidate) || tryLoad(candidate) != null
    }

    private static Class<?> tryLoad(String candidate) {
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

    private static List<String> collectImports(CompilationUnit compilationUnit) {
        compilationUnit.imports.findAll { !it.isStatic() }.collect { ImportDeclaration importDeclaration ->
            importDeclaration.asterisk ? importDeclaration.nameAsString + '.*' : importDeclaration.nameAsString
        }
    }
}

final class ParsedUnit {
    File file
    String text
    CompilationUnit compilationUnit
}

final class TypeRecord {
    ParsedUnit unit
    TypeDeclaration<?> declaration
    String fqcn
}
