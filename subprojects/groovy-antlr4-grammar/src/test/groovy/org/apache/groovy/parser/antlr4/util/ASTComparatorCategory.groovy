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

import groovy.util.logging.Log
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

import java.util.logging.Level

@Log @SuppressWarnings("GroovyUnusedDeclaration")
class ASTComparatorCategory {
    static { log.level = Level.WARNING }
    static List<String> LOCATION_IGNORE_LIST = ["columnNumber", "lineNumber", "lastColumnNumber", "lastLineNumber", "startLine"]
    static private List<String> EXPRESSION_IGNORE_LIST = ["text"] + LOCATION_IGNORE_LIST

    /**
     *  Keeps all checked object pairs and their comparison result.
     *  Will be cleared at {@link #apply(groovy.lang.Closure)} method }
     */
    static objects = [:] as Map<List<Object>, Boolean>
    static String lastName

    static Map<Class, List<String>> DEFAULT_CONFIGURATION = [
            (ClassNode): (['module', "declaredMethodsMap", "plainNodeReference", "typeClass", "allInterfaces", "orAddStaticConstructorNode", "allDeclaredMethods", "unresolvedSuperClass", "innerClasses" ] + LOCATION_IGNORE_LIST) as List<String>,
            (ConstructorNode): ['declaringClass'],
            (DynamicVariable): [],
            (EnumConstantClassNode): ["typeClass"],
            (FieldNode): ["owner", "declaringClass", "initialValueExpression", "assignToken"],
            (GenericsType): [],
            (ImportNode): LOCATION_IGNORE_LIST,
            (InnerClassNode): (['module', "declaredMethodsMap", "plainNodeReference", "typeClass", "allInterfaces", "orAddStaticConstructorNode", "allDeclaredMethods", "unresolvedSuperClass", "innerClasses" ] + LOCATION_IGNORE_LIST) as List<String>,
            (InterfaceHelperClassNode): [],
            (MethodNode): ["text", "declaringClass"],
            (MixinNode): [],
            (ModuleNode): ["context"],
            (PackageNode): [],
            (Parameter): [],
            (PropertyNode): ['declaringClass', 'initialValueExpression', "assignToken"],
            (Variable): [],
            (VariableScope): ["clazzScope", "parent", "declaredVariablesIterator"],
            (Token): ["root", "startColumn"],
            (AnnotationNode): (["text"] + LOCATION_IGNORE_LIST) as List<String>,
            (AssertStatement): ["text"],
            (BlockStatement): ["columnNumber", "lineNumber", "lastColumnNumber", "lastLineNumber", "text"],
            (BreakStatement): ["text"],
            (CaseStatement): ["text"],
            (CatchStatement): (["text"] + LOCATION_IGNORE_LIST) as List<String>,
            (ContinueStatement): ["text"],
            (DoWhileStatement): ["text"],
            (EmptyStatement): ["text"],
            (ExpressionStatement): ["text"],
            (ForStatement): ["text"],
            (IfStatement): ["text"],
            (LoopingStatement): ["text"],
            (ReturnStatement): ["text"],
            (SwitchStatement): ["columnNumber", "lineNumber", "lastColumnNumber", "lastLineNumber", "text"],
            (SynchronizedStatement): ["text"],
            (ThrowStatement): ["text"],
            (TryCatchStatement): (["text"] + LOCATION_IGNORE_LIST) as List<String>,
            (WhileStatement): ["text"],
            (AnnotationConstantExpression): EXPRESSION_IGNORE_LIST,
            (ArgumentListExpression): EXPRESSION_IGNORE_LIST,
            (ArrayExpression): EXPRESSION_IGNORE_LIST,
            (AttributeExpression): EXPRESSION_IGNORE_LIST,
            (BinaryExpression): EXPRESSION_IGNORE_LIST,
            (BitwiseNegationExpression): EXPRESSION_IGNORE_LIST,
            (BooleanExpression): EXPRESSION_IGNORE_LIST,
            (CastExpression): EXPRESSION_IGNORE_LIST,
            (ClassExpression): EXPRESSION_IGNORE_LIST,
            (ClosureExpression): EXPRESSION_IGNORE_LIST,
            (ClosureListExpression): EXPRESSION_IGNORE_LIST,
            (ConstantExpression): EXPRESSION_IGNORE_LIST,
            (ConstructorCallExpression): EXPRESSION_IGNORE_LIST,
            (DeclarationExpression): ["text", "columnNumber", "lineNumber", "lastColumnNumber", "lastLineNumber"],
            (ElvisOperatorExpression): EXPRESSION_IGNORE_LIST,
            (EmptyExpression): EXPRESSION_IGNORE_LIST,
            (ExpressionTransformer): EXPRESSION_IGNORE_LIST,
            (FieldExpression): EXPRESSION_IGNORE_LIST,
            (GStringExpression): EXPRESSION_IGNORE_LIST,
            (ListExpression): EXPRESSION_IGNORE_LIST,
            (MapEntryExpression): EXPRESSION_IGNORE_LIST,
            (MapExpression): EXPRESSION_IGNORE_LIST,
            (MethodCall): EXPRESSION_IGNORE_LIST,
            (MethodCallExpression): EXPRESSION_IGNORE_LIST,
            (MethodPointerExpression): EXPRESSION_IGNORE_LIST,
            (NamedArgumentListExpression): EXPRESSION_IGNORE_LIST,
            (NotExpression): EXPRESSION_IGNORE_LIST,
            (PostfixExpression): EXPRESSION_IGNORE_LIST,
            (PrefixExpression): EXPRESSION_IGNORE_LIST,
            (PropertyExpression): EXPRESSION_IGNORE_LIST,
            (RangeExpression): EXPRESSION_IGNORE_LIST,
            (SpreadExpression): EXPRESSION_IGNORE_LIST,
            (SpreadMapExpression): EXPRESSION_IGNORE_LIST,
            (StaticMethodCallExpression): EXPRESSION_IGNORE_LIST,
            (TernaryExpression): EXPRESSION_IGNORE_LIST,
            (TupleExpression): EXPRESSION_IGNORE_LIST,
            (UnaryMinusExpression): EXPRESSION_IGNORE_LIST,
            (UnaryPlusExpression): EXPRESSION_IGNORE_LIST,
            (VariableExpression): EXPRESSION_IGNORE_LIST,
    ];

    static Map<Class, List<String>> COLLECTION_PROPERTY_CONFIGURATION = [
            (ModuleNode): ["classes", "name"]
    ]

    static Map<Class, List<String>> configuration = DEFAULT_CONFIGURATION;

    static void apply(config = DEFAULT_CONFIGURATION, Closure cl) {
        configuration = config
        objects.clear()
        use(ASTComparatorCategory, cl)
        configuration = DEFAULT_CONFIGURATION
    }

    /**
     * Main method that makes the magic. Compares all properties for object a and object b.
     * There is a lot of problems in this code, like omitted class checking and so on. Just belive, it will be used properly.
     * @param a
     * @param b
     * @return
     */
    static reflexiveEquals(a, b, ignore = []) {

        if (a.getClass() != b.getClass()) {
            log.warning(" !!!! DIFFERENCE WAS FOUND! ${a.getClass()} != ${b.getClass()}")
            return false;
        }

        def objects = [a, b]
        Boolean res = this.objects[objects]
        if (res != null) {
            log.info("Skipping [$a, $b] comparison as they are ${ res ? "" : "un" }equal.")
            return res;
        }
        else if (this.objects.containsKey(objects)) {
            log.info("Skipping as they are processed at higher levels.")
            return true
        }

        this.objects[objects] = null
        log.info("Equals was called for ${ a.getClass() } ${ a.hashCode() }, $lastName")
        if (a.is(b))
            return true

        def difference = a.metaClass.properties.find { MetaBeanProperty p ->
            if (!p.getter)
                return false

            def name = p.name
            lastName = "$name :::: ${ a.getClass() } ${ a.hashCode() }"


            for (Map.Entry<Class, List<String>> me : COLLECTION_PROPERTY_CONFIGURATION) {
                if (!(me.key.isCase(a) && me.key.isCase(b))) {
                    continue;
                }

                String propName = me.value[0];

                if (name != propName) {
                    continue;
                }

                def aValue = a."${propName}"; // FIXME when the propName is "classes", a classNode will be added to moduleNode.classes
                def bValue = b."${propName}";

                String orderName = me.value[1];

                return new LinkedList(aValue?.getClass()?.isArray() ? Arrays.asList(aValue) : (aValue ?: [])).sort {c1, c2 -> c1."${orderName}" <=> c2."${orderName}"} !=
                        new LinkedList(bValue?.getClass()?.isArray() ? Arrays.asList(bValue) : (bValue ?: [])).sort {c1, c2 -> c1."${orderName}" <=> c2."${orderName}"}
            }


            !(name in ignore) && (name != 'nodeMetaData' && name != 'metaDataMap') && a."$name" != b."$name"
        }

        if (difference)
            log.warning(" !!!! DIFFERENCE WAS FOUND! [${a.metaClass.hasProperty(a, 'text') ? a.text : '<NO TEXT>'}][${a.class}][${difference.name}]:: ${ a."$difference.name" } != ${ b."$difference.name" }")
        else
            log.info(" ==== Exit ${ a.getClass() } ${ a.hashCode() } ====== ")

        res = difference == null
        this.objects[objects] = res
        this.objects[objects.reverse(false)] = res
        res
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Just a bunch of copypasted methods. Maybe will wrote AST transformation for them.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    static equals(ClassNode a, ClassNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ConstructorNode a, ConstructorNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(DynamicVariable a, DynamicVariable b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(EnumConstantClassNode a, EnumConstantClassNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(FieldNode a, FieldNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(GenericsType a, GenericsType b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ImportNode a, ImportNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(InnerClassNode a, InnerClassNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(InterfaceHelperClassNode a, InterfaceHelperClassNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(MethodNode a, MethodNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(MixinNode a, MixinNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ModuleNode a, ModuleNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(PackageNode a, PackageNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(Parameter a, Parameter b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(PropertyNode a, PropertyNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(Variable a, Variable b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(VariableScope a, VariableScope b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(Token a, Token b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(CompileUnit a, CompileUnit b) {
        true
    }

    static equals(AnnotationNode a, AnnotationNode b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Statements
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    static equals(AssertStatement a, AssertStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(BlockStatement a, BlockStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(BreakStatement a, BreakStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(CaseStatement a, CaseStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(CatchStatement a, CatchStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ContinueStatement a, ContinueStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(DoWhileStatement a, DoWhileStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(EmptyStatement a, EmptyStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ExpressionStatement a, ExpressionStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ForStatement a, ForStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(IfStatement a, IfStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(LoopingStatement a, LoopingStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ReturnStatement a, ReturnStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(SwitchStatement a, SwitchStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(SynchronizedStatement a, SynchronizedStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ThrowStatement a, ThrowStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(TryCatchStatement a, TryCatchStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(WhileStatement a, WhileStatement b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Expressions
    /////////////////////////////////////////////////////////////////////////////////////////////

    static equals(AnnotationConstantExpression a, AnnotationConstantExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ArgumentListExpression a, ArgumentListExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ArrayExpression a, ArrayExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(AttributeExpression a, AttributeExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(BinaryExpression a, BinaryExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(BitwiseNegationExpression a, BitwiseNegationExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(BooleanExpression a, BooleanExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(CastExpression a, CastExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ClassExpression a, ClassExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ClosureExpression a, ClosureExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ClosureListExpression a, ClosureListExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ConstantExpression a, ConstantExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ConstructorCallExpression a, ConstructorCallExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(DeclarationExpression a, DeclarationExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ElvisOperatorExpression a, ElvisOperatorExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(EmptyExpression a, EmptyExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ExpressionTransformer a, ExpressionTransformer b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(FieldExpression a, FieldExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(GStringExpression a, GStringExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(ListExpression a, ListExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(MapEntryExpression a, MapEntryExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(MapExpression a, MapExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(MethodCall a, MethodCall b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(MethodCallExpression a, MethodCallExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(MethodPointerExpression a, MethodPointerExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(NamedArgumentListExpression a, NamedArgumentListExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(NotExpression a, NotExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(PostfixExpression a, PostfixExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(PrefixExpression a, PrefixExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(PropertyExpression a, PropertyExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(RangeExpression a, RangeExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(SpreadExpression a, SpreadExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(SpreadMapExpression a, SpreadMapExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(StaticMethodCallExpression a, StaticMethodCallExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(TernaryExpression a, TernaryExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(TupleExpression a, TupleExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(UnaryMinusExpression a, UnaryMinusExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(UnaryPlusExpression a, UnaryPlusExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }

    static equals(VariableExpression a, VariableExpression b) {
        reflexiveEquals(a, b, configuration[a.class])
    }
}
