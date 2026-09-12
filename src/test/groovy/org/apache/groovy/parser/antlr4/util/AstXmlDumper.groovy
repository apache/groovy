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
package org.apache.groovy.parser.antlr4.util

import groovy.transform.CompileDynamic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.Statement

import java.lang.reflect.Modifier

/**
 * Pretty-printed XML dump of a module AST, including source positions.
 * Compact enough to inline in parser covering-set tests; richer than
 * {@link AstDumper} reconstruction because every node carries
 * {@code line}/{@code column}/{@code lastLine}/{@code lastColumn}.
 */
@CompileDynamic
final class AstXmlDumper {

    static String dump(ModuleNode ast) {
        new AstXmlDumper().dumpModule(ast)
    }

    private final StringBuilder out = new StringBuilder()
    private int indent

    private String dumpModule(ModuleNode ast) {
        elem('Module', pos(ast)) {
            if (ast.package) {
                dumpAnnotations(ast.package)
                elem('PackageNode', pos(ast.package) + [name: trimDot(ast.packageName)])
            }
            ast.imports.each { dumpImport(it) }
            ast.starImports.each { dumpImport(it) }
            ast.staticImports.values().each { dumpImport(it) }
            ast.staticStarImports.values().each { dumpImport(it) }
            if (ast.statementBlock?.statements) {
                dumpStatement(ast.statementBlock)
            }
            ast.classes.findAll { !it.script }.sort { it.name }.each { dumpClass(it) }
            ClassNode script = ast.classes.find { it.script }
            script?.methods?.each { method ->
                if (method.synthetic || method.name == 'run' || method.name == 'main') return
                if (method.annotations.any { it.classNode.nameWithoutPackage == 'Generated' }) return
                dumpMethod(method, false)
            }
        }
        return out.toString()
    }

    private void dumpImport(ImportNode n) {
        dumpAnnotations(n)
        Map attrs = pos(n)
        if (n.static && n.star) {
            attrs.type = n.type.name
            attrs.static = true
            attrs.star = true
        } else if (n.static) {
            attrs.type = n.type.name
            attrs.field = n.fieldName
            attrs.static = true
            if (n.alias && n.alias != n.fieldName) attrs.alias = n.alias
        } else if (n.star) {
            attrs.package = trimDot(n.packageName)
            attrs.star = true
        } else {
            attrs.type = n.type.name
            if (n.alias && n.alias != n.type.nameWithoutPackage) attrs.alias = n.alias
        }
        elem('ImportNode', attrs)
    }

    private void dumpClass(ClassNode n) {
        dumpAnnotations(n)
        Map attrs = pos(n)
        attrs.kind = classKind(n)
        attrs.name = n.nameWithoutPackage
        String mods = classMods(n)
        if (mods) attrs.modifiers = mods
        if (n.genericsTypes) attrs.generics = generics(n.genericsTypes)
        if (n.unresolvedSuperClass && n.unresolvedSuperClass.name != 'java.lang.Object') {
            attrs.extends = typeName(n.unresolvedSuperClass)
        }
        def ifaces = n.unresolvedInterfaces?.findAll { it } ?: []
        if (ifaces) attrs.implements = ifaces.collect { typeName(it) }.join(',')
        elem('ClassNode', attrs) {
            if (n.respondsTo('getRecordComponents')) {
                n.recordComponents?.each { rc ->
                    elem('RecordComponentNode', pos(rc) + [name: rc.name, type: typeName(rc.type)])
                }
            }
            n.fields.findAll { !it.synthetic }.each { dumpField(it) }
            n.properties.each { dumpProperty(it) }
            n.declaredConstructors.each { dumpMethod(it, true) }
            n.objectInitializerStatements.each { stmt ->
                elem('ObjectInitializer', [:]) {
                    dumpStatement(stmt)
                }
            }
            n.methods.findAll { it.name == '<clinit>' }.each { m ->
                elem('StaticInitializer', pos(m)) {
                    dumpStatement(m.code)
                }
            }
            n.methods.findAll { !it.synthetic && it.name != '<clinit>' }.each { dumpMethod(it, false) }
        }
    }

    private static String classKind(ClassNode n) {
        if (n.annotationDefinition) return 'annotation'
        if (n.enum) return 'enum'
        if ((n.respondsTo('isRecord') && n.isRecord()) || n.annotations.any { it.classNode.name.contains('RecordType') }) {
            return 'record'
        }
        if (n.annotations.any { it.classNode.name.endsWith('.Trait') || it.classNode.nameWithoutPackage == 'Trait' }) {
            return 'trait'
        }
        if (n.interface) return 'interface'
        'class'
    }

    private void dumpField(FieldNode n) {
        dumpAnnotations(n)
        Map attrs = pos(n) + [name: n.name, type: typeName(n.type)]
        String mods = fieldMods(n.modifiers)
        if (mods) attrs.modifiers = mods
        if (n.enum) attrs.enumConstant = true
        if (n.initialValueExpression && !n.enum) {
            elem('FieldNode', attrs) {
                dumpExpression(n.initialValueExpression)
            }
        } else {
            elem('FieldNode', attrs)
        }
    }

    // No-visibility members are properties; the synthetic backing field is omitted above.
    private void dumpProperty(PropertyNode n) {
        dumpAnnotations(n)
        Map attrs = pos(n) + [name: n.name, type: typeName(n.type)]
        String mods = fieldMods(n.modifiers)
        if (mods) attrs.modifiers = mods
        Expression init = n.field?.initialValueExpression
        if (init) {
            elem('PropertyNode', attrs) {
                dumpExpression(init)
            }
        } else {
            elem('PropertyNode', attrs)
        }
    }

    private void dumpMethod(MethodNode n, boolean ctor) {
        dumpAnnotations(n)
        Map attrs = pos(n) + [name: n.name]
        String mods = methodMods(n.modifiers)
        if (mods) attrs.modifiers = mods
        if (!ctor) attrs.returnType = typeName(n.returnType)
        if (n.exceptions) attrs.throws = n.exceptions.collect { typeName(it) }.join(',')
        String tag = ctor ? 'ConstructorNode' : 'MethodNode'
        elem(tag, attrs) {
            n.parameters.each { dumpParam(it) }
            if (n.code) dumpStatement(n.code)
        }
    }

    private void dumpParam(Parameter p) {
        dumpAnnotations(p)
        Map attrs = pos(p) + [name: p.name]
        boolean untyped = p.dynamicTyped || p.type == null || p.type.name == 'java.lang.Object'
        if (!untyped) attrs.type = typeName(p.type)
        if (p.hasInitialExpression()) {
            elem('Parameter', attrs) {
                dumpExpression(p.initialExpression)
            }
        } else {
            elem('Parameter', attrs)
        }
    }

    private void dumpStatement(Statement s) {
        if (s == null) return
        if (s.class.simpleName.startsWith('Empty')) {
            if (s.lineNumber > 0) elem('EmptyStatement', pos(s))
            return
        }
        Map attrs = pos(s)
        if (s.statementLabel) attrs.label = s.statementLabel
        dumpAnnotations(s instanceof AnnotatedNode ? (AnnotatedNode) s : null)
        String n = s.class.simpleName
        switch (n) {
            case 'BlockStatement':
                elem(n, attrs) { s.statements.each { dumpStatement(it) } }
                break
            case 'ExpressionStatement':
                elem(n, attrs) { dumpExpression(s.expression) }
                break
            case 'IfStatement':
                elem(n, attrs) {
                    dumpExpression(s.booleanExpression)
                    dumpStatement(s.ifBlock)
                    if (s.elseBlock && !s.elseBlock.class.simpleName.startsWith('Empty')) {
                        elem('else', [:]) { dumpStatement(s.elseBlock) }
                    }
                }
                break
            case 'SwitchStatement':
                elem(n, attrs) {
                    dumpExpression(s.expression)
                    s.caseStatements.each { dumpStatement(it) }
                    if (s.defaultStatement && !s.defaultStatement.class.simpleName.startsWith('Empty')) {
                        elem('default', [:]) { dumpStatement(s.defaultStatement) }
                    }
                }
                break
            case 'CaseStatement':
                elem(n, attrs) {
                    dumpExpression(s.expression)
                    dumpStatement(s.code)
                }
                break
            case 'ForStatement':
                elem(n, attrs) {
                    if (s.hasProperty('indexVariable') && s.indexVariable) dumpParam(s.indexVariable)
                    if (s.valueVariable && s.valueVariable.name != 'forLoopDummyParameter') dumpParam(s.valueVariable)
                    dumpExpression(s.collectionExpression)
                    dumpStatement(s.loopBlock)
                }
                break
            case 'WhileStatement':
                elem(n, attrs) {
                    dumpExpression(s.booleanExpression)
                    dumpStatement(s.loopBlock)
                }
                break
            case 'DoWhileStatement':
                elem(n, attrs) {
                    dumpStatement(s.loopBlock)
                    dumpExpression(s.booleanExpression)
                }
                break
            case 'TryCatchStatement':
                elem(n, attrs) {
                    s.resourceStatements?.each { rs ->
                        elem('resource', [:]) { dumpStatement(rs) }
                    }
                    dumpStatement(s.tryStatement)
                    s.catchStatements.each { dumpStatement(it) }
                    if (s.finallyStatement && !s.finallyStatement.class.simpleName.startsWith('Empty')) {
                        elem('finally', [:]) { dumpStatement(s.finallyStatement) }
                    }
                }
                break
            case 'CatchStatement':
                attrs.type = typeName(s.variable.type)
                attrs.variable = s.variable.name
                elem(n, attrs) { dumpStatement(s.code) }
                break
            case 'SynchronizedStatement':
                elem(n, attrs) {
                    dumpExpression(s.expression)
                    dumpStatement(s.code)
                }
                break
            case 'ReturnStatement':
                elem(n, attrs) {
                    if (s.expression) dumpExpression(s.expression)
                }
                break
            case 'ThrowStatement':
                elem(n, attrs) { dumpExpression(s.expression) }
                break
            case 'AssertStatement':
                elem(n, attrs) {
                    dumpExpression(s.booleanExpression)
                    if (s.messageExpression && !(s.messageExpression.class.simpleName == 'ConstantExpression' && !s.messageExpression.value)) {
                        dumpExpression(s.messageExpression)
                    }
                }
                break
            case 'BreakStatement':
                if (s.label) attrs.label = s.label
                elem(n, attrs)
                break
            case 'ContinueStatement':
                if (s.label) attrs.label = s.label
                elem(n, attrs)
                break
            case 'YieldStatement':
                elem(n, attrs) { dumpExpression(s.expression) }
                break
            default:
                elem(n, attrs)
                break
        }
    }

    private void dumpExpression(Expression e) {
        if (e == null) return
        if (e.class.simpleName.startsWith('Empty')) {
            if (e.lineNumber > 0) elem('EmptyExpression', pos(e))
            return
        }
        Map attrs = pos(e)
        String n = e.class.simpleName
        switch (n) {
            case 'ConstantExpression':
                attrs.value = fmtValue(e.value)
                elem(n, attrs)
                break
            case 'VariableExpression':
                attrs.name = e.name
                if (!e.dynamicTyped && e.type && e.type.name != 'java.lang.Object') attrs.type = typeName(e.type)
                elem(n, attrs)
                break
            case 'ClassExpression':
                attrs.type = typeName(e.type)
                elem(n, attrs)
                break
            case 'DeclarationExpression':
                attrs.token = e.operation.text
                elem(n, attrs) {
                    dumpExpression(e.leftExpression)
                    if (e.rightExpression && !e.rightExpression.class.simpleName.startsWith('Empty')) {
                        dumpExpression(e.rightExpression)
                    }
                }
                break
            case 'BinaryExpression':
                attrs.token = e.operation.text
                elem(n, attrs) {
                    dumpExpression(e.leftExpression)
                    dumpExpression(e.rightExpression)
                }
                break
            case 'TernaryExpression':
                elem(n, attrs) {
                    dumpExpression(e.booleanExpression)
                    dumpExpression(e.trueExpression)
                    dumpExpression(e.falseExpression)
                }
                break
            case 'ElvisOperatorExpression':
                elem(n, attrs) {
                    dumpExpression(e.trueExpression)
                    dumpExpression(e.falseExpression)
                }
                break
            case 'PrefixExpression':
                attrs.token = e.operation.text
                elem(n, attrs) { dumpExpression(e.expression) }
                break
            case 'PostfixExpression':
                attrs.token = e.operation.text
                elem(n, attrs) { dumpExpression(e.expression) }
                break
            case 'UnaryMinusExpression':
            case 'UnaryPlusExpression':
            case 'NotExpression':
            case 'BitwiseNegationExpression':
                elem(n, attrs) { dumpExpression(e.expression) }
                break
            case 'BooleanExpression':
                elem(n, attrs) { dumpExpression(e.expression) }
                break
            case 'CastExpression':
                attrs.kind = e.coerce ? 'as' : 'cast'
                attrs.type = typeName(e.type)
                elem(n, attrs) { dumpExpression(e.expression) }
                break
            case 'AttributeExpression':
            case 'PropertyExpression':
                if (e.safe) attrs.safe = true
                if (e.spreadSafe) attrs.spreadSafe = true
                elem(n, attrs) {
                    dumpExpression(e.objectExpression)
                    dumpExpression(e.property)
                }
                break
            case 'MethodCallExpression':
                if (e.safe) attrs.safe = true
                if (e.spreadSafe) attrs.spreadSafe = true
                if (e.implicitThis) attrs.implicitThis = true
                elem(n, attrs) {
                    if (!e.implicitThis) dumpExpression(e.objectExpression)
                    dumpExpression(e.method)
                    dumpExpression(e.arguments)
                }
                break
            case 'StaticMethodCallExpression':
                attrs.owner = typeName(e.ownerType)
                attrs.method = e.method
                elem(n, attrs) { dumpExpression(e.arguments) }
                break
            case 'ConstructorCallExpression':
                attrs.type = typeName(e.type)
                if (e.specialCall) attrs.special = e.superCall ? 'super' : 'this'
                if (e.usingAnonymousInnerClass) attrs.anonymous = true
                elem(n, attrs) { dumpExpression(e.arguments) }
                break
            case 'ArgumentListExpression':
            case 'TupleExpression':
                if (e.expressions.size() == 1 && n == 'ArgumentListExpression') {
                    dumpExpression(e.expressions[0])
                    break
                }
                elem(n, attrs) { e.expressions.each { dumpExpression(it) } }
                break
            case 'NamedArgumentListExpression':
                elem(n, attrs) { e.mapEntryExpressions.each { dumpExpression(it) } }
                break
            case 'ListExpression':
                elem(n, attrs) { e.expressions.each { dumpExpression(it) } }
                break
            case 'MapExpression':
                elem(n, attrs) { e.mapEntryExpressions.each { dumpExpression(it) } }
                break
            case 'MapEntryExpression':
                elem(n, attrs) {
                    dumpExpression(e.keyExpression)
                    dumpExpression(e.valueExpression)
                }
                break
            case 'RangeExpression':
                attrs.op = "${e.exclusiveLeft ? '<' : ''}..${e.exclusiveRight ? '<' : ''}"
                elem(n, attrs) {
                    dumpExpression(e.from)
                    dumpExpression(e.to)
                }
                break
            case 'SpreadExpression':
            case 'SpreadMapExpression':
                elem(n, attrs) { dumpExpression(e.expression) }
                break
            case 'GStringExpression':
                attrs.text = e.verbatimText
                elem(n, attrs) { e.values.each { dumpExpression(it) } }
                break
            case 'ArrayExpression':
                attrs.elementType = typeName(e.elementType)
                elem(n, attrs) {
                    e.sizeExpression?.each { dumpExpression(it) }
                    e.expressions.each { dumpExpression(it) }
                }
                break
            case 'ClosureExpression':
            case 'LambdaExpression':
                if (e.parameters == null) attrs.implicitIt = true
                else if (e.parameters.length == 0) attrs.parameters = ''
                else attrs.parameters = e.parameters.collect { it.name }.join(',')
                elem(n, attrs) { dumpStatement(e.code) }
                break
            case 'MethodPointerExpression':
            case 'MethodReferenceExpression':
                elem(n, attrs) {
                    dumpExpression(e.expression)
                    dumpExpression(e.methodName)
                }
                break
            case 'ClosureListExpression':
                elem(n, attrs) { e.expressions.each { dumpExpression(it) } }
                break
            case 'SwitchExpression':
                elem(n, attrs) {
                    dumpExpression(e.expression)
                    e.caseStatements.each { dumpStatement(it) }
                    if (e.defaultStatement && !e.defaultStatement.class.simpleName.startsWith('Empty')) {
                        elem('default', [:]) { dumpStatement(e.defaultStatement) }
                    }
                }
                break
            default:
                elem(n, attrs)
                break
        }
    }

    private void dumpAnnotations(AnnotatedNode node) {
        node?.annotations?.each { AnnotationNode a ->
            Map attrs = pos(a) + [class: typeName(a.classNode)]
            if (a.members) attrs.members = a.members.keySet().sort().join(',')
            elem('AnnotationNode', attrs)
        }
    }

    private void elem(String tag, Map attrs, Closure body = null) {
        String a = attrXml(attrs)
        if (body == null) {
            line "<${tag}${a}/>"
            return
        }
        int mark = out.length()
        int saved = indent
        line "<${tag}${a}>"
        indent++
        int before = out.length()
        body.call()
        indent = saved
        if (out.length() == before) {
            out.setLength(mark)
            line "<${tag}${a}/>"
        } else {
            line "</${tag}>"
        }
    }

    private void line(String text) {
        for (int i = 0; i < indent; i++) out.append('  ')
        out.append(text).append('\n')
    }

    private static Map pos(ASTNode n) {
        if (n == null) return [:]
        [line: n.lineNumber, column: n.columnNumber, lastLine: n.lastLineNumber, lastColumn: n.lastColumnNumber]
    }

    private static String attrXml(Map attrs) {
        attrs.findAll { k, v -> v != null && v != '' && v != false }.collect { k, v ->
            " $k=\"${esc(v)}\""
        }.join('')
    }

    private static String esc(Object v) {
        String s = String.valueOf(v)
        s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace('"', '&quot;')
    }

    private static String typeName(ClassNode type) {
        if (type == null) return '?'
        if (type.array) return typeName(type.componentType) + '[]'
        if (type.class.simpleName == 'IntersectionTypeClassNode') {
            return type.components.collect { typeName(it) }.join(' & ')
        }
        String name = type.unresolvedName ?: type.name
        GenericsType[] g = type.genericsTypes
        g ? name + generics(g) : name
    }

    private static String generics(GenericsType[] g) {
        if (!g) return ''
        '<' + g.collect { it.toString() }.join(', ') + '>'
    }

    private static String fmtValue(Object v) {
        if (v == null) return 'null'
        if (v instanceof String) return v
        if (v instanceof Character) return String.valueOf(v)
        String.valueOf(v)
    }

    private static String trimDot(String p) {
        p?.endsWith('.') ? p[0..-2] : p
    }

    private static String classMods(ClassNode n) {
        int m = n.modifiers & ~Modifier.PUBLIC
        if (n.interface || n.annotationDefinition) {
            m = m & ~(Modifier.ABSTRACT | Modifier.INTERFACE)
        }
        if (n.enum) m = m & ~Modifier.FINAL
        String s = Modifier.toString(m)
        s ? s.trim() : ''
    }

    private static String methodMods(int m) {
        String s = Modifier.toString(m & ~Modifier.PUBLIC)
        s ? s.trim() : ''
    }

    private static String fieldMods(int m) {
        String s = Modifier.toString(m)
        s ? s.trim() : ''
    }
}
