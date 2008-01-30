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

import org.codehaus.groovy.ast.ASTAnnotationMacro;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
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
import org.objectweb.asm.Opcodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

/**
 * @author Danno Ferrin (shemnon)
 */
public class BoundASTMacro implements ASTAnnotationMacro, Opcodes {

    //String pcsFieldName;
    FieldNode pcsField;

    public static boolean hasBoundAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations().values()) {
            if (Bound.class.getName().equals(annotation.getClassNode().getName())) {
                return true;
            }
        }
        return false;
    }

    public void visit(AnnotationNode node, AnnotatedNode parent, SourceUnit source, GeneratorContext context) {
        if (ConstrainedASTMacro.hasConstrainedAnnotation(parent)) {
            // ConstrainedASTMacro will handle both @Bound and @Constrained
            return;
        }

        ClassNode declaringClass = parent.getDeclaringClass();
        FieldNode field = ((FieldNode)parent);
        String fieldName = field.getName();
        for (PropertyNode propertyNode : (Collection<PropertyNode>) declaringClass.getProperties()) {
            if (propertyNode.getName().equals(fieldName)) {
                if (needsPropertyChangeSupport(declaringClass)) {
                    addPropertyChangeSupport(declaringClass);
                }
                String setterName = "set" + MetaClassHelper.capitalize(propertyNode.getName());
                if (declaringClass.getMethods(setterName).isEmpty()) {
                    Expression fieldExpression = new FieldExpression(field);
                    Statement setterBlock = createBoundStatement(field, fieldExpression);

                    // create method void <setter>(<type> fieldName)
                    createSetterMethod(declaringClass, field, setterName, setterBlock);
                } else {
                    source.getErrorCollector().addErrorAndContinue(
                        new SyntaxErrorMessage(new SyntaxException(
                            "@groovy.beans.Bound cannot handle user generated setters.",
                            node.getLineNumber(),
                            node.getColumnNumber()),
                            source));
                }
                return;
            }
        }
        source.getErrorCollector().addErrorAndContinue(
            new SyntaxErrorMessage(new SyntaxException(
                "@groovy.beans.Bound must be on a property, not a field.  Try removing the private, protected, or public modifier.",
                node.getLineNumber(),
                node.getColumnNumber()),
            source));
    }



    protected Statement createBoundStatement(FieldNode field, Expression fieldExpression) {
        // create statementBody this$propertyChangeSupport.firePropertyChange("field", field, field = value);
        return new ExpressionStatement(
            new MethodCallExpression(
                new FieldExpression(pcsField),
                    "firePropertyChange",
                        new ArgumentListExpression(
                            new Expression[] {
                                new ConstantExpression(field.getName()),
                                fieldExpression,
                                new BinaryExpression(
                                    fieldExpression,
                                    Token.newSymbol(Types.EQUAL, 0, 0),
                                    new VariableExpression("value"))})));
    }

    protected void createSetterMethod(ClassNode declaringClass, FieldNode field, String setterName, Statement setterBlock) {
        Parameter[] setterParameterTypes = { new Parameter(field.getType(), "value")};
        MethodNode setter =
            new MethodNode(setterName, field.getModifiers(), ClassHelper.VOID_TYPE, setterParameterTypes, ClassNode.EMPTY_ARRAY, setterBlock);
        setter.setSynthetic(true);
        // add it to the class
        declaringClass.addMethod(setter);
    }

    protected boolean needsPropertyChangeSupport(ClassNode declaringClass) {
        while (declaringClass != null) {
            for (FieldNode field : (Collection<FieldNode>) declaringClass.getFields()) {
                if (field.getType() == null) {
                    continue;
                }
                if (PropertyChangeSupport.class.getName().equals(field.getType().getName())) {
                    //pcsFieldName = field.getName();
                    pcsField = field;
                    return false;
                }
            }
            //TODO check add/remove conflicts
            declaringClass = declaringClass.getSuperClass();
        }
        return true;
    }

    protected void addPropertyChangeSupport(ClassNode declaringClass) {
        ClassNode pcsClassNode = ClassHelper.make(PropertyChangeSupport.class);
        ClassNode pclClassNode = ClassHelper.make(PropertyChangeListener.class);
        //String pcsFieldName = "this$propertyChangeSupport";

        // add field protected static PropertyChangeSupport this$propertyChangeSupport = new java.beans.PropertyChangeSupport(this)
        pcsField = declaringClass.addField(
            "this$propertyChangeSupport",
            ACC_FINAL | ACC_PROTECTED | ACC_SYNTHETIC,
            pcsClassNode,
            new ConstructorCallExpression(pcsClassNode,
                new ArgumentListExpression(new Expression[] {new VariableExpression("this")})));

        // add method void addPropertyChangeListener(listner) {
        //     this$propertyChangeSupport.addPropertyChangeListner(listener)
        //  }
        declaringClass.addMethod(
            new MethodNode(
                "addPropertyChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(pclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(pcsField),
                        "addPropertyChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("listener")})))));
        // add method void addPropertyChangeListener(name, listner) {
        //     this$propertyChangeSupport.addPropertyChangeListner(name, listener)
        //  }
        declaringClass.addMethod(
            new MethodNode(
                "addPropertyChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(ClassHelper.STRING_TYPE, "name"), new Parameter(pclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(pcsField),
                        "addPropertyChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("name"), new VariableExpression("listener")})))));

        // add method boolean removePropertyChangeListener(listner) {
        //    return this$propertyChangeSupport.removePropertyChangeListener(listener);
        // }
        declaringClass.addMethod(
            new MethodNode(
                "removePropertyChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(pclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(pcsField),
                        "removePropertyChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("listener")})))));
        // add method void removePropertyChangeListener(name, listner)
        declaringClass.addMethod(
            new MethodNode(
                "removePropertyChangeListener",
                ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(ClassHelper.STRING_TYPE, "name"), new Parameter(pclClassNode, "listener")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new FieldExpression(pcsField),
                        "removePropertyChangeListener",
                        new ArgumentListExpression(
                            new Expression[] {new VariableExpression("name"), new VariableExpression("listener")})))));
        // add PropertyChangeSupport[] getPropertyChangeListeners() {
        //   return this$propertyChangeSupport.getPropertyChangeListeners
        // }
        declaringClass.addMethod(
            new MethodNode(
                "getPropertyChangeListeners",
                ACC_PUBLIC | ACC_SYNTHETIC,
                pclClassNode.makeArray(),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(
                    new ExpressionStatement(
                        new MethodCallExpression(
                            new FieldExpression(pcsField),
                            "getPropertyChangeListeners",
                            ArgumentListExpression.EMPTY_ARGUMENTS)))));
    }
}
