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
package groovy.beans

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.objectweb.asm.Opcodes

import static org.codehaus.groovy.ast.stmt.ReturnStatement.RETURN_NULL_OR_VOID
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Handles generation of code for the {@code @ListenerList} annotation.
 * <p>
 * Generally, it adds the needed add&lt;Listener&gt;, remove&lt;Listener&gt; and
 * get&lt;Listener&gt;s methods to support the Java Beans API.
 * <p>
 * Additionally it adds corresponding fire&lt;Event&gt; methods.
 * <p>
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@SuppressWarnings('ParameterCount')
class ListenerListASTTransformation implements ASTTransformation, Opcodes {
    private static final Class MY_CLASS = groovy.beans.ListenerList
    private static final ClassNode COLLECTION_TYPE = ClassHelper.make(Collection)

    @SuppressWarnings('Instanceof')
    void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: ${node.class} / ${parent.class}")
        }
        AnnotationNode node = nodes[0]
        FieldNode field = nodes[1]
        ClassNode declaringClass = nodes[1].declaringClass
        ClassNode parentClass = field.type

        boolean isCollection = parentClass.isDerivedFrom(COLLECTION_TYPE) || parentClass.implementsInterface(COLLECTION_TYPE)

        if (!isCollection) {
            addError(node, source, '@' + MY_CLASS.name + ' can only annotate collection properties.')
            return
        }

        def types = field.type.genericsTypes
        if (!types) {
            addError(node, source, '@' + MY_CLASS.name + ' fields must have a generic type.')
            return
        }

        if (types[0].wildcard) {
            addError(node, source, '@' + MY_CLASS.name + ' fields with generic wildcards not yet supported.')
            return
        }

        def listener = types[0].type

        if (!field.initialValueExpression) {
            field.initialValueExpression = new ListExpression()
        }

        def name = node.getMember('name')?.value ?: listener.nameWithoutPackage

        def fireList = listener.methods.findAll { MethodNode m ->
            m.isPublic() && !m.isSynthetic() && !m.isStatic()
        }

        def synchronize = node.getMember('synchronize')?.value ?: false
        addAddListener(source, node, declaringClass, field, listener, name, synchronize)
        addRemoveListener(source, node, declaringClass, field, listener, name, synchronize)
        addGetListeners(source, node, declaringClass, field, listener, name, synchronize)

        fireList.each { MethodNode method ->
            addFireMethods(source, node, declaringClass, field, types, synchronize, method)
        }
    }

    private static addError(AnnotationNode node, SourceUnit source, String message) {
        source.errorCollector.addError(
                new SyntaxErrorMessage(
                        new SyntaxException(message, node.lineNumber, node.columnNumber), source))
    }

    /**
     * Adds the add&lt;Listener&gt; method like:
     * <pre>
     * synchronized void add${name.capitalize}(${listener.name} listener) {
     *     if (listener == null)
     *         return
     *     if (${field.name} == null)
     *        ${field.name} = []
     *     ${field.name}.add(listener)
     * }
     * </pre>
     */
    void addAddListener(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, ClassNode listener, String name, synchronize) {

        def methodModifiers = synchronize ? ACC_PUBLIC | ACC_SYNCHRONIZED : ACC_PUBLIC
        def methodReturnType = ClassHelper.make(Void.TYPE)
        def methodName = "add${name.capitalize()}"
        def cn = ClassHelper.makeWithoutCaching(listener.name)
        cn.redirect = listener
        def methodParameter = [param(cn, 'listener')] as Parameter[]

        if (declaringClass.hasMethod(methodName, methodParameter)) {
            addError node, source, "Conflict using @${MY_CLASS.name}. Class $declaringClass.name already has method $methodName"
            return
        }

        BlockStatement block = new BlockStatement()
        block.addStatements([
                ifS(
                        equalsNullX(varX('listener')),
                        RETURN_NULL_OR_VOID
                ),
                ifS(
                        equalsNullX(varX(field.name)),
                        assignS(varX(field.name), new ListExpression())
                ),
                stmt(callX(varX(field.name), constX('add'), args(varX('listener'))))
        ])
        declaringClass.addMethod(new MethodNode(methodName, methodModifiers, methodReturnType, methodParameter, [] as ClassNode[], block))
    }

    /**
     * Adds the remove<Listener> method like:
     * <pre>
     * synchronized void remove${name.capitalize}(${listener.name} listener) {
     *     if (listener == null)
     *         return
     *     if (${field.name} == null)
     *         ${field.name} = []
     *     ${field.name}.remove(listener)
     * }
     * </pre>
     */
    void addRemoveListener(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, ClassNode listener, String name, synchronize) {
        def methodModifiers = synchronize ? ACC_PUBLIC | ACC_SYNCHRONIZED : ACC_PUBLIC
        def methodReturnType = ClassHelper.make(Void.TYPE)
        def methodName = "remove${name.capitalize()}"
        def cn = ClassHelper.makeWithoutCaching(listener.name)
        cn.redirect = listener
        def methodParameter = [param(cn, 'listener')] as Parameter[]

        if (declaringClass.hasMethod(methodName, methodParameter)) {
            addError node, source, "Conflict using @${MY_CLASS.name}. Class $declaringClass.name already has method $methodName"
            return
        }

        BlockStatement block = new BlockStatement()
        block.addStatements([
                ifS(
                        equalsNullX(varX('listener')),
                        RETURN_NULL_OR_VOID
                ),
                ifS(
                        equalsNullX(varX(field.name)),
                        assignS(varX(field.name), new ListExpression())
                ),
                stmt(callX(varX(field.name), constX('remove'), args(varX('listener'))))
        ])
        declaringClass.addMethod(new MethodNode(methodName, methodModifiers, methodReturnType, methodParameter, [] as ClassNode[], block))
    }

    /**
     * Adds the get&lt;Listener&gt;s method like:
     * <pre>
     * synchronized ${name.capitalize}[] get${name.capitalize}s() {
     *     def __result = []
     *     if (${field.name} != null)
     *         __result.addAll(${field.name})
     *     return __result as ${name.capitalize}[]
     * }
     * </pre>
     */
    void addGetListeners(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, ClassNode listener, String name, synchronize) {
        def methodModifiers = synchronize ? ACC_PUBLIC | ACC_SYNCHRONIZED : ACC_PUBLIC
        def methodReturnType = listener.makeArray()
        def methodName = "get${name.capitalize()}s"
        def methodParameter = [] as Parameter[]

        if (declaringClass.hasMethod(methodName, methodParameter)) {
            addError node, source, "Conflict using @${MY_CLASS.name}. Class $declaringClass.name already has method $methodName"
            return
        }

        BlockStatement block = new BlockStatement()
        block.addStatements([
                declS(localVarX('__result', ClassHelper.DYNAMIC_TYPE), new ListExpression()),
                ifS(
                        notNullX(varX(field.name)),
                        stmt(callX(varX('__result'), constX('addAll'), args(varX(field.name))))
                ),
                returnS(castX(methodReturnType, varX('__result')))
        ])
        declaringClass.addMethod(new MethodNode(methodName, methodModifiers, methodReturnType, methodParameter, [] as ClassNode[], block))
    }

    /**
     * Adds the fire&lt;Event&gt; methods like:
     * <pre>
     * void fire${fireMethod.capitalize()}(${parameterList.join(', ')}) {
     *     if (${field.name} != null) {
     *         def __list = new ArrayList(${field.name})
     *         for (listener in __list) {
     *             listener.$eventMethod(${evt})
     *         }
     *     }
     * }
     * </pre>
     */
    void addFireMethods(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, GenericsType[] types, boolean synchronize, MethodNode method) {

        def methodReturnType = ClassHelper.make(Void.TYPE)
        def methodName = "fire${method.name.capitalize()}"
        def methodModifiers = synchronize ? ACC_PUBLIC | ACC_SYNCHRONIZED : ACC_PUBLIC

        if (declaringClass.hasMethod(methodName, method.parameters)) {
            addError node, source, "Conflict using @${MY_CLASS.name}. Class $declaringClass.name already has method $methodName"
            return
        }

        def methodArgs = args(method.parameters)

        BlockStatement block = new BlockStatement()
        def listenerListType = ClassHelper.make(ArrayList).plainNodeReference
        listenerListType.genericsTypes = types
        block.addStatements([
                ifS(
                        notNullX(varX(field.name)),
                        new BlockStatement([
                                declS(
                                        localVarX('__list', listenerListType),
                                        ctorX(listenerListType, args(varX(field.name)))
                                ),
                                new ForStatement(
                                        param(ClassHelper.DYNAMIC_TYPE, 'listener'),
                                        varX('__list'),
                                        new BlockStatement([
                                                stmt(callX(varX('listener'), method.name, methodArgs))
                                        ], new VariableScope())
                                )
                        ], new VariableScope())
                )
        ])

        def params = method.parameters.collect {
            def paramType = ClassHelper.getWrapper(it.type)
            def cn = paramType.plainNodeReference
            cn.redirect = paramType
            param(cn, it.name)
        }
        declaringClass.addMethod(methodName, methodModifiers, methodReturnType, params as Parameter[], [] as ClassNode[], block)
    }
}
