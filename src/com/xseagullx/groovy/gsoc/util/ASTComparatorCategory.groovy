
package com.xseagullx.groovy.gsoc.util

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class ASTComparatorCategory {
    /**
     * Main method that makes the magic. Compares all properties for object a and object b.
     * There is a lot of problems in this code, like omitted class checking and so on. Just belive, it will be used properly.
     * @param a
     * @param b
     * @return
     */
    static reflexiveEquals(a, b, ignore = []) {
        if (a.is(b))
            return true
        def difference = a.metaClass.properties.find { MetaBeanProperty p ->
            if (!p.getter)
                return false

            def name = p.name
            !(name in ignore) && a."$name" != b."$name"
        }

        if (difference)
            println("Difference was found! ${ a."$difference.name" } != ${ b."$difference.name" }")
        difference == null
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Just a bunch of copypasted methods. Maybe will wrote AST transformation for them.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    static equals(ClassNode a, ClassNode b) {
        reflexiveEquals(a, b, ['module', "declaredMethodsMap", "plainNodeReference", "typeClass"])
    }

    static equals(ConstructorNode a, ConstructorNode b) {
        reflexiveEquals(a, b)
    }

    static equals(DynamicVariable a, DynamicVariable b) {
        reflexiveEquals(a, b)
    }

    static equals(EnumConstantClassNode a, EnumConstantClassNode b) {
        reflexiveEquals(a, b)
    }

    static equals(FieldNode a, FieldNode b) {
        reflexiveEquals(a, b, ["owner", "declaringClass", "initialValueExpression"])
    }

    static equals(GenericsType a, GenericsType b) {
        reflexiveEquals(a, b)
    }

    static equals(ImportNode a, ImportNode b) {
        reflexiveEquals(a, b)
    }

    static equals(InnerClassNode a, InnerClassNode b) {
        reflexiveEquals(a, b)
    }

    static equals(InterfaceHelperClassNode a, InterfaceHelperClassNode b) {
        reflexiveEquals(a, b)
    }

    static equals(MethodNode a, MethodNode b) {
        reflexiveEquals(a, b, ["declaringClass"])
    }

    static equals(MixinNode a, MixinNode b) {
        reflexiveEquals(a, b)
    }

    static equals(ModuleNode a, ModuleNode b) {
        reflexiveEquals(a, b)
    }

    static equals(PackageNode a, PackageNode b) {
        reflexiveEquals(a, b)
    }

    static equals(Parameter a, Parameter b) {
        reflexiveEquals(a, b)
    }

    static equals(PropertyNode a, PropertyNode b) {
        reflexiveEquals(a, b, ['declaringClass', 'initialValueExpression'])
    }

    static equals(Variable a, Variable b) {
        reflexiveEquals(a, b)
    }

    static equals(VariableScope a, VariableScope b) {
        reflexiveEquals(a, b, ["clazzScope", "parent"])
    }

    static equals(Token a, Token b) {
        reflexiveEquals(a, b, ["root"])
    }

    static equals(CompileUnit a, CompileUnit b) {
        true
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Statements 
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    static equals(AssertStatement a, AssertStatement b) {
        reflexiveEquals(a, b)
    }
    
    static equals(BlockStatement a, BlockStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(BreakStatement a, BreakStatement b) {
        reflexiveEquals(a, b)
    }
    
    static equals(CaseStatement a, CaseStatement b) {
        reflexiveEquals(a, b)
    }
    
    static equals(CatchStatement a, CatchStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(ContinueStatement a, ContinueStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(DoWhileStatement a, DoWhileStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(EmptyStatement a, EmptyStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(ExpressionStatement a, ExpressionStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(ForStatement a, ForStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(IfStatement a, IfStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(LoopingStatement a, LoopingStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(ReturnStatement a, ReturnStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(SwitchStatement a, SwitchStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(SynchronizedStatement a, SynchronizedStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(ThrowStatement a, ThrowStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(TryCatchStatement a, TryCatchStatement b) {
        reflexiveEquals(a, b)
    }

    static equals(WhileStatement a, WhileStatement b) {
        reflexiveEquals(a, b)
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    // Expressions
    /////////////////////////////////////////////////////////////////////////////////////////////

    static equals(AnnotationConstantExpression a, AnnotationConstantExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ArgumentListExpression a, ArgumentListExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ArrayExpression a, ArrayExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(AttributeExpression a, AttributeExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(BinaryExpression a, BinaryExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(BitwiseNegationExpression a, BitwiseNegationExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(BooleanExpression a, BooleanExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(CastExpression a, CastExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ClassExpression a, ClassExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ClosureExpression a, ClosureExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ClosureListExpression a, ClosureListExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ConstantExpression a, ConstantExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ConstructorCallExpression a, ConstructorCallExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(DeclarationExpression a, DeclarationExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ElvisOperatorExpression a, ElvisOperatorExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(EmptyExpression a, EmptyExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ExpressionTransformer a, ExpressionTransformer b) {
        reflexiveEquals(a, b)
    }

    static equals(FieldExpression a, FieldExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(GStringExpression a, GStringExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(ListExpression a, ListExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(MapEntryExpression a, MapEntryExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(MapExpression a, MapExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(MethodCall a, MethodCall b) {
        reflexiveEquals(a, b)
    }

    static equals(MethodCallExpression a, MethodCallExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(MethodPointerExpression a, MethodPointerExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(NamedArgumentListExpression a, NamedArgumentListExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(NotExpression a, NotExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(PostfixExpression a, PostfixExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(PrefixExpression a, PrefixExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(PropertyExpression a, PropertyExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(RangeExpression a, RangeExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(SpreadExpression a, SpreadExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(SpreadMapExpression a, SpreadMapExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(StaticMethodCallExpression a, StaticMethodCallExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(TernaryExpression a, TernaryExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(TupleExpression a, TupleExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(UnaryMinusExpression a, UnaryMinusExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(UnaryPlusExpression a, UnaryPlusExpression b) {
        reflexiveEquals(a, b)
    }

    static equals(VariableExpression a, VariableExpression b) {
        reflexiveEquals(a, b)
    }
}
