/*
 * Copyright 2008 the original author or authors.
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

package groovy.beans;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.Collection;

/**
 * Handles genration of code for the @Constrained annotation, and @Bound
 * if also present.
 *
 * Generally, it adds (if needed) a VetoableChangeSupport field and
 * the needed add/removeVetoableChangeListener methods to support the
 * listeners.
 *
 * It also generates the setter and wires the setter through the
 * VetoableChangeSupport.
 *
 * If a @{@link Bound} annotaton is detected it also adds support similar
 * to what {@link BoundASTTransformation} would do.
 *
 * @author Danno Ferrin (shemnon)
 */
public class ConstrainedASTTransformation extends BoundASTTransformation {

    /**
     * Convienience method to see if an annotatied node is @Bound
     *
     * @param node
     * @return
     */
    protected FieldNode vcsField;

    /**
     * Convienience method to see if an annotatied node is @Constrained
     *
     * @param node
     * @return
     */
    public static boolean hasConstrainedAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations()) {
            if (Constrained.class.getName().equals(annotation.getClassNode().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param _node
     * @param _parent
     * @param source
     * @param context
     */
    public void visit(ASTNode _node, ASTNode _parent, SourceUnit source, GeneratorContext context) {
        if (!(_node instanceof AnnotationNode) || !(_parent instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: $node.class / $parent.class");
        }
        AnnotationNode node = (AnnotationNode) _node;
        AnnotatedNode parent = (AnnotatedNode) _parent;

        boolean bound = BoundASTTransformation.hasBoundAnnotation(parent);

        ClassNode declaringClass = parent.getDeclaringClass();
        FieldNode field = ((FieldNode)parent);
        String fieldName = field.getName();
        for (PropertyNode propertyNode : (Collection<PropertyNode>) declaringClass.getProperties()) {
            if (propertyNode.getName().equals(fieldName)) {

                if (bound && needsPropertyChangeSupport(declaringClass)) {
                    addPropertyChangeSupport(declaringClass);
                }
                if (needsVetoableChangeSupport(declaringClass)) {
                    addVetoableChangeSupport(declaringClass);
                }
                String setterName = "set" + MetaClassHelper.capitalize(propertyNode.getName());
                if (declaringClass.getMethods(setterName).isEmpty()) {
                    Expression fieldExpression = new FieldExpression(field);
                    BlockStatement setterBlock = new BlockStatement();
                    setterBlock.addStatement(createConstrainedStatement(field, fieldExpression));
                    if (bound) {
                        setterBlock.addStatement(createBoundStatement(field, fieldExpression));
                    } else {
                        setterBlock.addStatement(createSetStatement(fieldExpression));
                    }

                    // create method void <setter>(<type> fieldName)
                    createSetterMethod(declaringClass, field, setterName, setterBlock);
                } else {
                    source.getErrorCollector().addErrorAndContinue(
                        new SyntaxErrorMessage(new SyntaxException(
                            "@groovy.beans.Constrained cannot handle user generated setters.",
                            node.getLineNumber(),
                            node.getColumnNumber()),
                            source));
                }
                return;
            }
        }
        source.getErrorCollector().addErrorAndContinue(
            new SyntaxErrorMessage(new SyntaxException(
                "@groovy.beans.Constrained must be on a property, not a field.  Try removing the private, protected, or public modifier.",
                node.getLineNumber(),
                node.getColumnNumber()),
            source));
    }

    /**
     * Creates a statement body silimar to
     *
     * vcsField.fireVetoableChange("field", field, field = value);
     *
     * @param field the field node for the proeprty
     * @param fieldExpression a field expression for setting the property value
     * @return
     */
    protected Statement createConstrainedStatement(FieldNode field, Expression fieldExpression) {
        return new ExpressionStatement(
            new MethodCallExpression(
                new FieldExpression(vcsField),
                    "fireVetoableChange",
                        new ArgumentListExpression(
                            new Expression[] {
                                new ConstantExpression(field.getName()),
                                fieldExpression,
                                new VariableExpression("value")})));
    }

    /**
     * Creates a statement body silimar to
     *
     * field = value
     *
     * Used when the field is not also @Bound
     *
     * @param fieldExpression a field expression for setting the property value
     * @return
     */
    protected Statement createSetStatement(Expression fieldExpression) {
        return new ExpressionStatement(
            new BinaryExpression(
                fieldExpression,
                Token.newSymbol(Types.EQUAL, 0, 0),
                new VariableExpression("value")));
    }

    /**
     * Snoops through the declaring class and all parents looking for a field
     * of type VetoableChangeSupport.  Returns the field if found
     *
     * @param declaringClass
     * @return
     */
    protected boolean needsVetoableChangeSupport(ClassNode declaringClass) {
        while (declaringClass != null) {
            for (FieldNode field : (Collection<FieldNode>) declaringClass.getFields()) {
                if (field.getType() == null) {
                    continue;
                }
                if (VetoableChangeSupport.class.getName().equals(field.getType().getName())) {
                    vcsField = field;
                    return false;
                }
            }
            //TODO check add/remove conflicts
            declaringClass = declaringClass.getSuperClass();
        }
        return true;
    }

    /**
     * Creates a seter method with the given body.
     *
     * This differs from normal setters in that we need to add a declared
     * exception java.beans.PropertyVetoException
     *
     * @param declaringClass
     * @param field
     * @param setterName
     * @param setterBlock
     */
    protected void createSetterMethod(ClassNode declaringClass, FieldNode field, String setterName, Statement setterBlock) {
        Parameter[] setterParameterTypes = { new Parameter(field.getType(), "value")};
        ClassNode[] exceptions = {new ClassNode(PropertyVetoException.class)};
        MethodNode setter =
            new MethodNode(setterName, field.getModifiers(), ClassHelper.VOID_TYPE, setterParameterTypes, exceptions, setterBlock);
        setter.setSynthetic(true);
        // add it to the class
        declaringClass.addMethod(setter);
    }

    /**
     * Adds a new field "protpected final java.beans.VetoableChangeSupport this$vetoableChangeSupport = new java.beans.VetoableChangeSupport(this)"
     *
     * Also adds support methods...
     *  public void addVetoableChangeListener(java.beans.VetoableChangeListener)
     *  public void addVetoableChangeListener(String, java.beans.VetoableChangeListener)
     *  public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
     *  public void removeVetoableChangeListener(String, java.beans.VetoableChangeListener)
     *  public java.beans.VetoableChangeListener[] getVetoableChangeListeners()
     *
     * @param declaringClass
     */
    protected void addVetoableChangeSupport(ClassNode declaringClass) {
        ClassNode vcsClassNode = ClassHelper.make(VetoableChangeSupport.class);
        ClassNode vclClassNode = ClassHelper.make(VetoableChangeListener.class);

        // add field protected static VetoableChangeSupport this$vetoableChangeSupport = new java.beans.VetoableChangeSupport(this)
        vcsField = declaringClass.addField(
            "this$vetoableChangeSupport",
            ACC_FINAL | ACC_PROTECTED | ACC_SYNTHETIC,
            vcsClassNode,
            new ConstructorCallExpression(vcsClassNode,
                new ArgumentListExpression(new Expression[] {new VariableExpression("this")})));

        // add method void addVetoableChangeListener(listner) {
        //     this$vetoableChangeSupport.addVetoableChangeListner(listener)
        //  }
        declaringClass.addMethod(
            new MethodNode(
                "addVetoableChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(vclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(vcsField),
                        "addVetoableChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("listener")})))));
        // add method void addVetoableChangeListener(name, listner) {
        //     this$vetoableChangeSupport.addVetoableChangeListner(name, listener)
        //  }
        declaringClass.addMethod(
            new MethodNode(
                "addVetoableChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(ClassHelper.STRING_TYPE, "name"), new Parameter(vclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(vcsField),
                        "addVetoableChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("name"), new VariableExpression("listener")})))));

        // add method boolean removeVetoableChangeListener(listner) {
        //    return this$vetoableChangeSupport.removeVetoableChangeListener(listener);
        // }
        declaringClass.addMethod(
            new MethodNode(
                "removeVetoableChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(vclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(vcsField),
                        "removeVetoableChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("listener")})))));
        // add method void removeVetoableChangeListener(name, listner)
        declaringClass.addMethod(
            new MethodNode(
                "removeVetoableChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(ClassHelper.STRING_TYPE, "name"), new Parameter(vclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(vcsField),
                        "removeVetoableChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("name"), new VariableExpression("listener")})))));
        // add VetoableChangeSupport[] getVetoableChangeListeners() {
        //   return this$vetoableChangeSupport.getVetoableChangeListeners
        // }
        declaringClass.addMethod(
            new MethodNode(
                "getVetoableChangeListeners",
                ACC_PUBLIC | ACC_SYNTHETIC,
                vclClassNode.makeArray(),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(
                    new ExpressionStatement(
                        new MethodCallExpression(
                            new FieldExpression(vcsField),
                            "getVetoableChangeListeners",
                            ArgumentListExpression.EMPTY_ARGUMENTS)))));
    }

}
