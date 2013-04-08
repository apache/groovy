/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractASTTransformUtil implements Opcodes {
    private static final Token COMPARE_EQUAL = Token.newSymbol(Types.COMPARE_EQUAL, -1, -1);
    private static final Token COMPARE_NOT_EQUAL = Token.newSymbol(Types.COMPARE_NOT_EQUAL, -1, -1);
    private static final Token INSTANCEOF = Token.newSymbol(Types.KEYWORD_INSTANCEOF, -1, -1);
    private static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);

    public static boolean hasDeclaredMethod(ClassNode cNode, String name, int argsCount) {
        List<MethodNode> ms = cNode.getDeclaredMethods(name);
        for (MethodNode m : ms) {
            Parameter[] paras = m.getParameters();
            if (paras != null && paras.length == argsCount) {
                return true;
            }
        }
        return false;
    }

    public static Statement returnFalseIfWrongType(ClassNode cNode, Expression other) {
        return new IfStatement(
                notEqualClasses(cNode, other),
                new ReturnStatement(new ConstantExpression(Boolean.FALSE)),
                new EmptyStatement()
        );
    }

    public static Statement returnFalseIfNotInstanceof(ClassNode cNode, Expression other) {
        return new IfStatement(
                isInstanceof(cNode, other),
                new EmptyStatement(),
                new ReturnStatement(new ConstantExpression(Boolean.FALSE))
        );
    }

    public static IfStatement returnFalseIfNull(Expression other) {
        return new IfStatement(
                equalsNullExpr(other),
                new ReturnStatement(new ConstantExpression(Boolean.FALSE)),
                new EmptyStatement()
        );
    }

    public static IfStatement returnTrueIfIdentical(Expression self, Expression other) {
        return new IfStatement(
                identicalExpr(self, other),
                new ReturnStatement(new ConstantExpression(Boolean.TRUE)),
                new EmptyStatement()
        );
    }

    @Deprecated
    public static Statement returnFalseIfPropertyNotEqual(FieldNode fNode, Expression other) {
        return returnFalseIfFieldNotEqual(fNode, other);
    }

    public static Statement returnFalseIfPropertyNotEqual(PropertyNode pNode, Expression other) {
        return new IfStatement(
                notEqualsPropertyExpr(pNode, other),
                new ReturnStatement(new ConstantExpression(Boolean.FALSE)),
                new EmptyStatement()
        );
    }

    public static Statement returnFalseIfFieldNotEqual(FieldNode fNode, Expression other) {
        return new IfStatement(
                notEqualsFieldExpr(fNode, other),
                new ReturnStatement(new ConstantExpression(Boolean.FALSE)),
                new EmptyStatement()
        );
    }

    public static List<PropertyNode> getInstanceProperties(ClassNode cNode) {
        final List<PropertyNode> result = new ArrayList<PropertyNode>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode);
            }
        }
        return result;
    }

    public static List<FieldNode> getInstancePropertyFields(ClassNode cNode) {
        final List<FieldNode> result = new ArrayList<FieldNode>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }

    public static List<FieldNode> getInstanceNonPropertyFields(ClassNode cNode) {
        final List<FieldNode> result = new ArrayList<FieldNode>();
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }

    public static List<FieldNode> getSuperPropertyFields(ClassNode cNode) {
        final List<FieldNode> result;
        if (cNode == ClassHelper.OBJECT_TYPE) {
            result = new ArrayList<FieldNode>();
        } else {
            result = getSuperPropertyFields(cNode.getSuperClass());
        }
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }

    public static List<FieldNode> getSuperNonPropertyFields(ClassNode cNode) {
        final List<FieldNode> result;
        if (cNode == ClassHelper.OBJECT_TYPE) {
            result = new ArrayList<FieldNode>();
        } else {
            result = getSuperNonPropertyFields(cNode.getSuperClass());
        }
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }

    public static Statement assignStatement(Expression fieldExpr, Expression value) {
        return new ExpressionStatement(assignExpr(fieldExpr, value));
    }

    private static Expression assignExpr(Expression expression, Expression value) {
        return new BinaryExpression(expression, ASSIGN, value);
    }

    public static ExpressionStatement declStatement(Expression result, Expression init) {
        return new ExpressionStatement(new DeclarationExpression(result, ASSIGN, init));
    }

    public static BooleanExpression isInstanceOf(Expression objectExpression, ClassNode cNode) {
        return new BooleanExpression(new BinaryExpression(objectExpression, INSTANCEOF, new ClassExpression(cNode)));
    }

    public static BooleanExpression equalsNullExpr(Expression argExpr) {
        return new BooleanExpression(new BinaryExpression(argExpr, COMPARE_EQUAL, new ConstantExpression(null)));
    }

    public static BooleanExpression notNullExpr(Expression argExpr) {
        return new BooleanExpression(new BinaryExpression(argExpr, COMPARE_NOT_EQUAL, new ConstantExpression(null)));
    }

    public static BooleanExpression isZeroExpr(Expression expr) {
        return new BooleanExpression(new BinaryExpression(expr, COMPARE_EQUAL, new ConstantExpression(0)));
    }

    private static BooleanExpression notEqualsFieldExpr(FieldNode fNode, Expression other) {
        final Expression fieldExpr = new VariableExpression(fNode);
        final Expression otherExpr = new PropertyExpression(other, fNode.getName());
        return new BooleanExpression(new BinaryExpression(fieldExpr, COMPARE_NOT_EQUAL, otherExpr));
    }

    public static BooleanExpression differentFieldExpr(FieldNode fNode, Expression other) {
        final Expression fieldExpr = new VariableExpression(fNode);
        final Expression otherExpr = new PropertyExpression(other, fNode.getName());
        return differentExpr(fieldExpr, otherExpr);
    }

    private static BooleanExpression notEqualsPropertyExpr(PropertyNode pNode, Expression other) {
        String getterName = "get" + Verifier.capitalize(pNode.getName());
        Expression selfGetter = new MethodCallExpression(new VariableExpression("this"), getterName, MethodCallExpression.NO_ARGUMENTS);
        Expression otherGetter = new MethodCallExpression(other, getterName, MethodCallExpression.NO_ARGUMENTS);
        return new BooleanExpression(new BinaryExpression(selfGetter, COMPARE_NOT_EQUAL, otherGetter));
    }

    public static BooleanExpression differentPropertyExpr(PropertyNode pNode, Expression other) {
        String getterName = "get" + Verifier.capitalize(pNode.getName());
        Expression selfGetter = new MethodCallExpression(new VariableExpression("this"), getterName, MethodCallExpression.NO_ARGUMENTS);
        Expression otherGetter = new MethodCallExpression(other, getterName, MethodCallExpression.NO_ARGUMENTS);
        return differentExpr(selfGetter, otherGetter);
    }

    public static BooleanExpression identicalExpr(Expression self, Expression other) {
        return new BooleanExpression(new MethodCallExpression(self, "is", new ArgumentListExpression(other)));
    }

    public static BooleanExpression differentExpr(Expression self, Expression other) {
        return new NotExpression(new BooleanExpression(new MethodCallExpression(self, "is", new ArgumentListExpression(other))));
    }

    private static BooleanExpression notEqualClasses(ClassNode cNode, Expression other) {
        return new BooleanExpression(new BinaryExpression(new ClassExpression(cNode), COMPARE_NOT_EQUAL,
                new MethodCallExpression(other, "getClass", MethodCallExpression.NO_ARGUMENTS)));
    }

    public static BooleanExpression isInstanceof(ClassNode cNode, Expression other) {
        return new BooleanExpression(new BinaryExpression(other, INSTANCEOF, new ClassExpression(cNode)));
    }

    public static boolean isOrImplements(ClassNode fieldType, ClassNode interfaceType) {
        return fieldType.equals(interfaceType) || fieldType.implementsInterface(interfaceType);
    }

    public static BooleanExpression isTrueExpr(Expression argExpr) {
        return new BooleanExpression(new BinaryExpression(argExpr, COMPARE_EQUAL, new ConstantExpression(Boolean.TRUE)));
    }

    public static BooleanExpression isOneExpr(Expression expr) {
        return new BooleanExpression(new BinaryExpression(expr, COMPARE_EQUAL, new ConstantExpression(1)));
    }

    public static Statement safeExpression(Expression fieldExpr, Expression expression) {
        return new IfStatement(
                equalsNullExpr(fieldExpr),
                new ExpressionStatement(fieldExpr),
                new ExpressionStatement(expression));
    }

    public static Statement createConstructorStatementDefault(FieldNode fNode) {
        final String name = fNode.getName();
        final Expression fieldExpr = new PropertyExpression(new VariableExpression("this"), name);
        Expression initExpr = fNode.getInitialValueExpression();
        if (initExpr == null) initExpr = new ConstantExpression(null);
        Expression value = findArg(name);
        return new IfStatement(
                equalsNullExpr(value),
                new IfStatement(
                        equalsNullExpr(initExpr),
                        new EmptyStatement(),
                        assignStatement(fieldExpr, initExpr)),
                assignStatement(fieldExpr, value));
    }

    public static Expression findArg(String argName) {
        return new PropertyExpression(new VariableExpression("args"), argName);
    }

}
