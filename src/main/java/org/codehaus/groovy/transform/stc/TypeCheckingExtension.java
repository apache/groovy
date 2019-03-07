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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;

import java.util.Collections;
import java.util.List;

/**
 * This interface defines a high-level API for handling type checking errors. As a dynamic language and a platform
 * for developing DSLs, the Groovy language provides a lot of means to supply custom bindings or methods that are
 * not possible to find at compile time. However, it is still possible to help the compiler, for example by
 * telling it what is the type of an unresolved property.
 *
 * For basic DSL type checking, implementing those methods would help the type checker and make it silent where it
 * normally throws errors.
 *
 * @since 2.1.0
 */
public class TypeCheckingExtension {

    protected final StaticTypeCheckingVisitor typeCheckingVisitor;

    public TypeCheckingExtension(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        this.typeCheckingVisitor = typeCheckingVisitor;
    }

    /**
     * Subclasses should implement this method whenever they need to perform
     * special checks before the type checker starts working.
     */
    public void setup() {}

    /**
     * Subclasses should implement this method if they need to perform additional
     * checks after the type checker has finished its work. This is particularly
     * useful for situations where you need multiple passes. Some checks in that
     * case may be deferred to the end, using this method.
     */
    public void finish() {}

    /**
     * This method is called by the type checker when a variable expression cannot
     * be resolved. It gives the extension a chance to resolve it for the type
     * checker.
     *
     * @param vexp the unresolved variable extension
     * @return <code>boolean</code> false if the extension doesn't handle it,
     * true if the extension handles this variable.
     */
    public boolean handleUnresolvedVariableExpression(VariableExpression vexp) {
        return false;
    }

    /**
     * This method is called by the type checker when a property expression cannot
     * be resolved (for example, when a property doesn't exist). It gives the extension
     * a chance to resolve it.
     *
     * @param pexp the unresolved property
     * @return <code>boolean</code> false if this extension doesn't resolve the property, true
     * if it resolves the property.
     */
    public boolean handleUnresolvedProperty(PropertyExpression pexp) {
        return false;
    }

    /**
     * This method is called by the type checker when an attribute expression cannot
     * be resolved (for example, when an attribute doesn't exist). It gives the extension
     * a chance to resolve it.
     *
     * @param aexp the unresolved attribute
     * @return <code>boolean</code> false if this extension doesn't resolve the attribute, true
     * if it resolves the attribute.
     */
    public boolean handleUnresolvedAttribute(AttributeExpression aexp) {
        return false;
    }

    /**
     * This method is called by the type checker when a method call cannot be resolved. Extensions
     * may override this method to handle missing methods and prevent the type checker from throwing an
     * error.
     *
     *
     * @param receiver the type of the receiver
     * @param name the name of the called method
     * @param argumentList the list of arguments of the call
     * @param argumentTypes the types of the arguments of the call
     * @param call the method call itself, if needed
     * @return an empty list if the extension cannot resolve the method, or a list of potential
     * methods if the extension finds candidates. This method must not return null.
     */
    public List<MethodNode> handleMissingMethod(ClassNode receiver, String name, ArgumentListExpression argumentList, ClassNode[] argumentTypes, MethodCall call) {
        return Collections.emptyList();
    }

    /**
     * This method is called by the type checker when an assignment is not allowed by the type checker.
     * Extensions may override this method to allow such assignments where the type checker normally disallows
     * them.
     *
     * @param lhsType the type of the left hand side of the assignment, as found by the type checker
     * @param rhsType the type of the right hand side of the assignment, as found by the type checker
     * @param assignmentExpression the assignment expression which triggered this call
     * @return <code>boolean</code> false if the extension does not handle this assignment, true otherwise
     */
    public boolean handleIncompatibleAssignment(final ClassNode lhsType, final ClassNode rhsType, final Expression assignmentExpression) {
        return false;
    }

    /**
     * This method is called by the type checker before throwing an "ambiguous method" error, giving the chance
     * to the extension to select the method properly. This means that when this method is called, the "nodes"
     * parameter contains at least two methods. If the returned list still contains at least two methods, then the
     * type checker will throw an ambiguous method call error. If the returned method contains 1 element, then
     * the type checker will not throw any error.
     *
     * It is invalid to return an empty list.
     *
     * @param nodes the list of ambiguous methods
     * @param origin the expression which originated the method selection process
     * @return a single element list of disambiguated selection, or more elements if still ambiguous. It is not allowed
     * to return an empty list.
     */
    public List<MethodNode> handleAmbiguousMethods(final List<MethodNode> nodes, final Expression origin) {
        return nodes;
    }

    /**
     * Allows the extension to perform additional tasks before the type checker actually visits a method node.
     * Compared to a custom visitor, this method ensures that the node being visited is a node which would have
     * been visited by the type checker. This is in particular important for nodes which are marked with
     * {@link groovy.transform.TypeCheckingMode#SKIP}.
     * @param node a method node
     * @return false if the type checker should visit the node, or true if this extension replaces what the
     * type checker would do with the method.
     */
    public boolean beforeVisitMethod(MethodNode node) {
        return false;
    }

    /**
     * Allows the extension to perform additional tasks after the type checker actually visited a method node.
     * Compared to a custom visitor, this method ensures that the node being visited is a node which would have
     * been visited by the type checker. This is in particular important for nodes which are marked with
     * {@link groovy.transform.TypeCheckingMode#SKIP}.
     * @param node a method node
     */
    public void afterVisitMethod(MethodNode node) {
    }

    /**
     * Allows the extension to perform additional tasks before the type checker actually visits a class node.
     * Compared to a custom visitor, this method ensures that the node being visited is a node which would have
     * been visited by the type checker. This is in particular important for nodes which are marked with
     * {@link groovy.transform.TypeCheckingMode#SKIP}.
     * @param node a class node
     * @return false if the type checker should visit the node, or true if this extension replaces what the
     * type checker would do with the class.
     */
    public boolean beforeVisitClass(ClassNode node) {
        return false;
    }

    /**
     * Allows the extension to perform additional tasks after the type checker actually visited a class node.
     * Compared to a custom visitor, this method ensures that the node being visited is a node which would have
     * been visited by the type checker. This is in particular important for nodes which are marked with
     * {@link groovy.transform.TypeCheckingMode#SKIP}.
     * @param node a class node
     */
    public void afterVisitClass(ClassNode node) {
    }

    /**
     * Allows the extension to perform additional tasks before the type checker actually visits a method call.
     * Compared to a custom visitor, this method ensures that the node being visited is a node which would have
     * been visited by the type checker. This is in particular important for nodes which are marked with
     * {@link groovy.transform.TypeCheckingMode#SKIP}.
     *
     * @param call a method call, either a {@link org.codehaus.groovy.ast.expr.MethodCallExpression}, {@link org.codehaus.groovy.ast.expr.StaticMethodCallExpression}, or {@link org.codehaus.groovy.ast.expr.ConstructorCallExpression}
     * @return false if the type checker should visit the node, or true if this extension replaces what the
     * type checker would do with the method call.
     */
    public boolean beforeMethodCall(MethodCall call) {
        return false;
    }

    /**
     * Allows the extension to perform additional tasks after the type checker actually visits a method call.
     * Compared to a custom visitor, this method ensures that the node being visited is a node which would have
     * been visited by the type checker. This is in particular important for nodes which are marked with
     * {@link groovy.transform.TypeCheckingMode#SKIP}.
     * @param call a method call, either a {@link org.codehaus.groovy.ast.expr.MethodCallExpression}, {@link org.codehaus.groovy.ast.expr.StaticMethodCallExpression}, or {@link org.codehaus.groovy.ast.expr.ConstructorCallExpression}
     */
    public void afterMethodCall(MethodCall call) {
    }

    /**
     * Allows the extension to listen to method selection events. Given an expression, which may be a method
     * call expression, a static method call expression, a pre/postfix expression, ..., if a corresponding
     * method is found, this method is called.
     * @param expression the expression for which a corresponding method has been found
     * @param target the method which has been chosen by the type checker
     */
    public void onMethodSelection(Expression expression, MethodNode target) {
    }

    /**
     * Allows the extension to catch incompatible return types. This event is called whenever the type
     * checker finds that an inferred return type is incompatible with the declared return type of
     * a method.
     *
     * @param returnStatement the statement that triggered the error
     * @param inferredReturnType the inferred return type for this statement
     * @return false if the extension doesn't handle the error, true otherwise
     */
    public boolean handleIncompatibleReturnType(ReturnStatement returnStatement, ClassNode inferredReturnType) {
        return false;
    }

    // ------------------------------------------------------------------------------------------
    // Below, you will find various helper methods aimed at simplifying algorithms for subclasses
    // ------------------------------------------------------------------------------------------

    /**
     * Returns the inferred type of an expression. Delegates to the type checker implementation.
     * @param exp the expression for which we want to find the inferred type
     * @return the inferred type of the expression, as found by the type checker
     */
    public ClassNode getType(final ASTNode exp) {
        return typeCheckingVisitor.getType(exp);
    }

    /**
     * Adds a type checking error, which will be displayed to the user during compilation.
     * @param msg the message for the error
     * @param expr the expression which is the root cause of the error
     */
    public void addStaticTypeError(final String msg, final ASTNode expr) {
        typeCheckingVisitor.addStaticTypeError(msg, expr);
    }

    /**
     * Stores an inferred type for an expression. Delegates to the type checker.
     * @param exp the expression for which we want to store an inferred type
     * @param cn the type of the expression
     */
    public void storeType(final Expression exp, final ClassNode cn) {
        typeCheckingVisitor.storeType(exp, cn);
    }

    public boolean existsProperty(final PropertyExpression pexp, final boolean checkForReadOnly) {
        return typeCheckingVisitor.existsProperty(pexp, checkForReadOnly);
    }

    public boolean existsProperty(final PropertyExpression pexp, final boolean checkForReadOnly, final ClassCodeVisitorSupport visitor) {
        return typeCheckingVisitor.existsProperty(pexp, checkForReadOnly, visitor);
    }

    public ClassNode[] getArgumentTypes(final ArgumentListExpression args) {
        return typeCheckingVisitor.getArgumentTypes(args);
    }

    public MethodNode getTargetMethod(final Expression expression) {
        return (MethodNode) expression.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
    }

    public ClassNode classNodeFor(Class type) {
        return ClassHelper.make(type);
    }

    public ClassNode classNodeFor(String type) {
        return ClassHelper.make(type);
    }

    /**
     * Lookup a ClassNode by its name from the source unit
     *
     * @param type the name of the class whose ClassNode we want to lookup
     * @return a ClassNode representing the class
     */
    public ClassNode lookupClassNodeFor(String type) {
        for (ClassNode cn : typeCheckingVisitor.getSourceUnit().getAST().getClasses()) {
            if (cn.getName().equals(type))
                return cn;
        }

        return null;
    }

    public ClassNode parameterizedType(ClassNode baseType, ClassNode... genericsTypeArguments) {
        ClassNode result = baseType.getPlainNodeReference();
        if (result.isUsingGenerics()) {
            GenericsType[] gts = new GenericsType[genericsTypeArguments.length];
            int expectedLength = result.getGenericsTypes().length;
            if (expectedLength!=genericsTypeArguments.length) {
                throw new GroovyBugError("Expected number of generic type arguments for "+baseType.toString(false)+" is "+expectedLength
                + " but you gave "+genericsTypeArguments.length);
            }
            for (int i = 0; i < gts.length; i++) {
                gts[i] = new GenericsType(genericsTypeArguments[i]);
            }
            result.setGenericsTypes(gts);
        }
        return result;
    }

    /**
     * Builds a parametrized class node for List, to represent List&lt;X&gt;
     * @param componentType the classnode for the component type of the list
     * @return a classnode representing List&lt;componentType&gt;
     * @since 2.2.0
     */
    public ClassNode buildListType(ClassNode componentType) {
        return parameterizedType(ClassHelper.LIST_TYPE, componentType);
    }

    /**
     * Builds a parametrized class node representing the Map&lt;keyType,valueType&gt; type.
     * @param keyType the classnode type of the key
     * @param valueType the classnode type of the value
     * @return a class node for Map&lt;keyType,valueType&gt;
     * @since 2.2.0
     */
    public ClassNode buildMapType(ClassNode keyType, ClassNode valueType) {
        return parameterizedType(ClassHelper.MAP_TYPE, keyType, valueType);
    }

    /**
     * Given a method call, first checks that it's a static method call, and if it is, returns the
     * class node for the receiver. For example, with the following code:
     * <code></code>Person.findAll { ... }</code>, it would return the class node for <i>Person</i>.
     * If it's not a static method call, returns null.
     * @param call a method call
     * @return null if it's not a static method call, or the class node for the receiver instead.
     */
    public ClassNode extractStaticReceiver(MethodCall call) {
        if (call instanceof StaticMethodCallExpression) {
            return ((StaticMethodCallExpression) call).getOwnerType();
        } else if (call instanceof MethodCallExpression) {
            Expression objectExpr = ((MethodCallExpression) call).getObjectExpression();
            if (objectExpr instanceof ClassExpression && ClassHelper.CLASS_Type.equals(objectExpr.getType())) {
                GenericsType[] genericsTypes = objectExpr.getType().getGenericsTypes();
                if (genericsTypes!=null && genericsTypes.length==1) {
                    return genericsTypes[0].getType();
                }
            }
            if (objectExpr instanceof ClassExpression) {
                return objectExpr.getType();
            }
        }
        return null;
    }

    /**
     * Given a method call, checks if it's a static method call and if it is, tells if the receiver matches
     * the one supplied as an argument.
     * @param call a method call
     * @param receiver a class node
     * @return true if the method call is a static method call on the receiver
     */
    public boolean isStaticMethodCallOnClass(MethodCall call, ClassNode receiver) {
        ClassNode staticReceiver = extractStaticReceiver(call);
        return staticReceiver!=null && staticReceiver.equals(receiver);
    }

}
