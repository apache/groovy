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
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.EnumConstantClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.InterfaceHelperClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.MixinNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.PackageNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.ExpressionTransformer
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.LoopingStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.syntax.Token

import java.util.logging.Level

@CompileStatic @Log @SuppressWarnings('GroovyUnusedDeclaration')
class ASTComparatorCategory {
    static { log.level = Level.WARNING }
    public static final List<String> LOCATION_IGNORE_LIST = ['columnNumber', 'lineNumber', 'lastColumnNumber', 'lastLineNumber', 'startLine'].asUnmodifiable()
    private static final List<String> EXPRESSION_IGNORE_LIST = (['text'] + LOCATION_IGNORE_LIST).asUnmodifiable()

    public static final Map<Class, List<String>> DEFAULT_CONFIGURATION = [
            (ClassNode)                   : ['module', 'declaredMethodsMap', 'plainNodeReference', 'typeClass', 'allInterfaces', 'orAddStaticConstructorNode', 'allDeclaredMethods', 'unresolvedSuperClass', 'innerClasses'] + LOCATION_IGNORE_LIST,
            (ConstructorNode)             : ['declaringClass'],
            (DynamicVariable)             : [] as List<String>,
            (EnumConstantClassNode)       : ['typeClass'],
            (FieldNode)                   : ['owner', 'declaringClass', 'initialValueExpression', 'assignToken'],
            (GenericsType)                : [] as List<String>,
            (ImportNode)                  : LOCATION_IGNORE_LIST,
            (InnerClassNode)              : ['module', 'declaredMethodsMap', 'plainNodeReference', 'typeClass', 'allInterfaces', 'orAddStaticConstructorNode', 'allDeclaredMethods', 'unresolvedSuperClass', 'innerClasses'] + LOCATION_IGNORE_LIST,
            (InterfaceHelperClassNode)    : [] as List<String>,
            (MethodNode)                  : ['text', 'declaringClass'],
            (MixinNode)                   : [] as List<String>,
            (ModuleNode)                  : ['context'],
            (PackageNode)                 : [] as List<String>,
            (Parameter)                   : [] as List<String>,
            (PropertyNode)                : ['declaringClass', 'initialValueExpression', 'assignToken'],
            (Variable)                    : [] as List<String>,
            (VariableScope)               : ['clazzScope', 'parent', 'declaredVariablesIterator'],
            (Token)                       : ['root', 'startColumn'],
            (AnnotationNode)              : (['text'] + LOCATION_IGNORE_LIST),
            (AssertStatement)             : ['text'],
            (BlockStatement)              : ['columnNumber', 'lineNumber', 'lastColumnNumber', 'lastLineNumber', 'text'],
            (BreakStatement)              : ['text'],
            (CaseStatement)               : ['text'],
            (CatchStatement)              : ['text'] + LOCATION_IGNORE_LIST,
            (ContinueStatement)           : ['text'],
            (DoWhileStatement)            : ['text'],
            (EmptyStatement)              : ['text'],
            (ExpressionStatement)         : ['text'],
            (ForStatement)                : ['text'],
            (IfStatement)                 : ['text'],
            (LoopingStatement)            : ['text'],
            (ReturnStatement)             : ['text'],
            (SwitchStatement)             : ['columnNumber', 'lineNumber', 'lastColumnNumber', 'lastLineNumber', 'text'],
            (SynchronizedStatement)       : ['text'],
            (ThrowStatement)              : ['text'],
            (TryCatchStatement)           : ['text'] + LOCATION_IGNORE_LIST,
            (WhileStatement)              : ['text'],
            (AnnotationConstantExpression): EXPRESSION_IGNORE_LIST,
            (ArgumentListExpression)      : EXPRESSION_IGNORE_LIST,
            (ArrayExpression)             : EXPRESSION_IGNORE_LIST,
            (AttributeExpression)         : EXPRESSION_IGNORE_LIST,
            (BinaryExpression)            : EXPRESSION_IGNORE_LIST,
            (BitwiseNegationExpression)   : EXPRESSION_IGNORE_LIST,
            (BooleanExpression)           : EXPRESSION_IGNORE_LIST,
            (CastExpression)              : EXPRESSION_IGNORE_LIST,
            (ClassExpression)             : EXPRESSION_IGNORE_LIST,
            (ClosureExpression)           : EXPRESSION_IGNORE_LIST,
            (ClosureListExpression)       : EXPRESSION_IGNORE_LIST,
            (ConstantExpression)          : EXPRESSION_IGNORE_LIST,
            (ConstructorCallExpression)   : EXPRESSION_IGNORE_LIST,
            (DeclarationExpression)       : ['text', 'columnNumber', 'lineNumber', 'lastColumnNumber', 'lastLineNumber'],
            (ElvisOperatorExpression)     : EXPRESSION_IGNORE_LIST,
            (EmptyExpression)             : EXPRESSION_IGNORE_LIST,
            (ExpressionTransformer)       : EXPRESSION_IGNORE_LIST,
            (FieldExpression)             : EXPRESSION_IGNORE_LIST,
            (GStringExpression)           : EXPRESSION_IGNORE_LIST,
            (ListExpression)              : EXPRESSION_IGNORE_LIST,
            (MapEntryExpression)          : EXPRESSION_IGNORE_LIST,
            (MapExpression)               : EXPRESSION_IGNORE_LIST,
            (MethodCall)                  : EXPRESSION_IGNORE_LIST,
            (MethodCallExpression)        : EXPRESSION_IGNORE_LIST,
            (MethodPointerExpression)     : EXPRESSION_IGNORE_LIST,
            (NamedArgumentListExpression) : EXPRESSION_IGNORE_LIST,
            (NotExpression)               : EXPRESSION_IGNORE_LIST,
            (PostfixExpression)           : EXPRESSION_IGNORE_LIST,
            (PrefixExpression)            : EXPRESSION_IGNORE_LIST,
            (PropertyExpression)          : EXPRESSION_IGNORE_LIST,
            (RangeExpression)             : EXPRESSION_IGNORE_LIST,
            (SpreadExpression)            : EXPRESSION_IGNORE_LIST,
            (SpreadMapExpression)         : EXPRESSION_IGNORE_LIST,
            (StaticMethodCallExpression)  : EXPRESSION_IGNORE_LIST,
            (TernaryExpression)           : EXPRESSION_IGNORE_LIST,
            (TupleExpression)             : EXPRESSION_IGNORE_LIST,
            (UnaryMinusExpression)        : EXPRESSION_IGNORE_LIST,
            (UnaryPlusExpression)         : EXPRESSION_IGNORE_LIST,
            (VariableExpression)          : EXPRESSION_IGNORE_LIST,
    ].asUnmodifiable()

    public static final Map<Class, List<String>> COLLECTION_PROPERTY_CONFIGURATION = [
            (ModuleNode): ['classes', 'name']
    ].asUnmodifiable()

    public static Map<Class, List<String>> configuration = DEFAULT_CONFIGURATION

    /**
     *  Keeps all checked object pairs and their comparison result.
     *  Will be cleared at {@link #apply(groovy.lang.Closure)} method }
     */
    public static final Map<List<Object>, Boolean> objects = [:]

    static String lastName

    @CompileDynamic
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
    @CompileDynamic
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

        def difference = a.metaClass.properties.find { MetaProperty mp  ->
            MetaBeanProperty p = (MetaBeanProperty) mp
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


            !(name in ignore) && (name != 'nodeMetaData' && name != 'metaDataMap' && name != 'groovydoc') && a."$name" != b."$name"
        }

        if (difference)
            log.warning(" !!!! DIFFERENCE WAS FOUND! [${extractText(a)}][${a.class}][${difference.name}]:: ${ a."$difference.name" } != ${ b."$difference.name" }")
        else
            log.info(" ==== Exit ${ a.getClass() } ${ a.hashCode() } ====== ")

        res = difference == null
        this.objects[objects] = res
        this.objects[objects.reverse(false)] = res
        res
    }

    @CompileDynamic
    static String extractText(obj) {
        return obj.metaClass.hasProperty(obj, 'text') ? obj.text : '<NO TEXT>'
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
