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
package groovy.beans;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.PropertyNodeUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the {@code @Bindable} annotation when {@code @Vetoable}
 * is not present.
 * <p>
 * Generally, it adds (if needed) a PropertyChangeSupport field and
 * the needed add/removePropertyChangeListener methods to support the
 * listeners.
 * <p>
 * It also generates the setter and wires the setter through the
 * PropertyChangeSupport.
 * <p>
 * If a {@link Vetoable} annotation is detected it does nothing and
 * lets the {@link VetoableASTTransformation} handle all the changes.
 */
@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class BindableASTTransformation implements ASTTransformation, Opcodes {

    protected static final ClassNode boundClassNode = ClassHelper.make(Bindable.class);

    /**
     * Convenience method to see if an annotated node is {@code @Bindable}.
     *
     * @param node the node to check
     * @return true if the node is bindable
     */
    public static boolean hasBindableAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (boundClassNode.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes   the ast nodes
     * @param source  the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: $node.class / $parent.class");
        }
        AnnotationNode node = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];

        if (VetoableASTTransformation.hasVetoableAnnotation(parent)) {
            // VetoableASTTransformation will handle both @Bindable and @Vetoable
            return;
        }

        ClassNode declaringClass = parent.getDeclaringClass();
        if (parent instanceof FieldNode) {
            if ((((FieldNode) parent).getModifiers() & Opcodes.ACC_FINAL) != 0) {
                source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                        new SyntaxException("@groovy.beans.Bindable cannot annotate a final property.",
                                node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()),
                        source));
            }

            if (VetoableASTTransformation.hasVetoableAnnotation(parent.getDeclaringClass())) {
                // VetoableASTTransformation will handle both @Bindable and @Vetoable
                return;
            }
            addListenerToProperty(source, node, declaringClass, (FieldNode) parent);
        } else if (parent instanceof ClassNode) {
            addListenerToClass(source, (ClassNode) parent);
        }
    }

    private void addListenerToProperty(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field) {
        String fieldName = field.getName();
        for (PropertyNode propertyNode : declaringClass.getProperties()) {
            if (propertyNode.getName().equals(fieldName)) {
                if (field.isStatic()) {
                    //noinspection ThrowableInstanceNeverThrown
                    source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                            new SyntaxException("@groovy.beans.Bindable cannot annotate a static property.",
                                    node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()),
                            source));
                } else {
                    if (needsPropertyChangeSupport(declaringClass, source)) {
                        addPropertyChangeSupport(declaringClass);
                    }
                    createListenerSetter(declaringClass, propertyNode);
                }
                return;
            }
        }
        //noinspection ThrowableInstanceNeverThrown
        source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                new SyntaxException("@groovy.beans.Bindable must be on a property, not a field.  Try removing the private, protected, or public modifier.",
                        node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()),
                source));
    }

    private void addListenerToClass(SourceUnit source, ClassNode classNode) {
        if (needsPropertyChangeSupport(classNode, source)) {
            addPropertyChangeSupport(classNode);
        }
        for (PropertyNode propertyNode : classNode.getProperties()) {
            FieldNode field = propertyNode.getField();
            // look to see if per-field handlers will catch this one...
            if (hasBindableAnnotation(field)
                || ((field.getModifiers() & Opcodes.ACC_FINAL) != 0)
                || field.isStatic()
                || VetoableASTTransformation.hasVetoableAnnotation(field))
            {
                // explicitly labeled properties are already handled,
                // don't transform final properties
                // don't transform static properties
                // VetoableASTTransformation will handle both @Bindable and @Vetoable
                continue;
            }
            createListenerSetter(classNode, propertyNode);
        }
    }

    /*
     * Wrap an existing setter.
     */
    private static void wrapSetterMethod(ClassNode classNode, String propertyName) {
        String getterName = "get" + capitalize(propertyName);
        MethodNode setter = classNode.getSetterMethod("set" + capitalize(propertyName));

        if (setter != null) {
            // Get the existing code block
            Statement code = setter.getCode();

            Expression oldValue = varX("$oldValue");
            Expression newValue = varX("$newValue");
            BlockStatement block = new BlockStatement();

            // create a local variable to hold the old value from the getter
            block.addStatement(declS(oldValue, callThisX(getterName)));

            // call the existing block, which will presumably set the value properly
            block.addStatement(code);

            // get the new value to emit in the event
            block.addStatement(declS(newValue, callThisX(getterName)));

            // add the firePropertyChange method call
            block.addStatement(stmt(callThisX("firePropertyChange", args(constX(propertyName), oldValue, newValue))));

            // replace the existing code block with our new one
            setter.setCode(block);
        }
    }

    private void createListenerSetter(ClassNode classNode, PropertyNode propertyNode) {
        String setterName = "set" + capitalize(propertyNode.getName());
        if (classNode.getMethods(setterName).isEmpty()) {
            Statement setterBlock = createBindableStatement(propertyNode, fieldX(propertyNode.getField()));

            // create method void <setter>(<type> fieldName)
            createSetterMethod(classNode, propertyNode, setterName, setterBlock);
        } else {
            wrapSetterMethod(classNode, propertyNode.getName());
        }
    }

    /**
     * Creates a statement body similar to:
     * <code>this.firePropertyChange("field", field, field = value)</code>
     *
     * @param propertyNode           the field node for the property
     * @param fieldExpression a field expression for setting the property value
     * @return the created statement
     */
    protected Statement createBindableStatement(PropertyNode propertyNode, Expression fieldExpression) {
        // create statementBody
        return stmt(callThisX("firePropertyChange", args(constX(propertyNode.getName()), fieldExpression, assignX(fieldExpression, varX("value")))));
    }

    /**
     * Creates a setter method with the given body.
     *
     * @param declaringClass the class to which we will add the setter
     * @param propertyNode          the field to back the setter
     * @param setterName     the name of the setter
     * @param setterBlock    the statement representing the setter block
     */
    protected void createSetterMethod(ClassNode declaringClass, PropertyNode propertyNode, String setterName, Statement setterBlock) {
        MethodNode setter = new MethodNode(
                setterName,
                PropertyNodeUtils.adjustPropertyModifiersForMethod(propertyNode),
                ClassHelper.VOID_TYPE,
                params(param(propertyNode.getType(), "value")),
                ClassNode.EMPTY_ARRAY,
                setterBlock);
        setter.setSynthetic(true);
        // add it to the class
        declaringClass.addMethod(setter);
    }

    /**
     * Snoops through the declaring class and all parents looking for methods
     * <code>void addPropertyChangeListener(PropertyChangeListener)</code>,
     * <code>void removePropertyChangeListener(PropertyChangeListener)</code>, and
     * <code>void firePropertyChange(String, Object, Object)</code>. If any are defined all
     * must be defined or a compilation error results.
     *
     * @param declaringClass the class to search
     * @param sourceUnit the source unit, for error reporting. {@code @NotNull}.
     * @return true if property change support should be added
     */
    protected boolean needsPropertyChangeSupport(ClassNode declaringClass, SourceUnit sourceUnit) {
        boolean foundAdd = false, foundRemove = false, foundFire = false;
        ClassNode consideredClass = declaringClass;
        while (consideredClass!= null) {
            for (MethodNode method : consideredClass.getMethods()) {
                // just check length, MOP will match it up
                foundAdd = foundAdd || method.getName().equals("addPropertyChangeListener") && method.getParameters().length == 1;
                foundRemove = foundRemove || method.getName().equals("removePropertyChangeListener") && method.getParameters().length == 1;
                foundFire = foundFire || method.getName().equals("firePropertyChange") && method.getParameters().length == 3;
                if (foundAdd && foundRemove && foundFire) {
                    return false;
                }
            }
            consideredClass = consideredClass.getSuperClass();
        }
        // check if a super class has @Bindable annotations
        consideredClass = declaringClass.getSuperClass();
        while (consideredClass!=null) {
            if (hasBindableAnnotation(consideredClass)) return false;
            for (FieldNode field : consideredClass.getFields()) {
                if (hasBindableAnnotation(field)) return false;
            }
            consideredClass = consideredClass.getSuperClass();
        }
        if (foundAdd || foundRemove || foundFire) {
            sourceUnit.getErrorCollector().addErrorAndContinue(
                new SimpleMessage("@Bindable cannot be processed on "
                    + declaringClass.getName()
                    + " because some but not all of addPropertyChangeListener, removePropertyChange, and firePropertyChange were declared in the current or super classes.",
                sourceUnit)
            );
            return false;
        }
        return true;
    }

    /**
     * Adds the necessary field and methods to support property change support.
     * <p>
     * Adds a new field:
     * <pre>
     * <code>protected final java.beans.PropertyChangeSupport this$PropertyChangeSupport = new java.beans.PropertyChangeSupport(this)</code>"
     * </pre>
     * <p>
     * Also adds support methods:
     * <pre>
     * <code>public void addPropertyChangeListener(java.beans.PropertyChangeListener)</code>
     * <code>public void addPropertyChangeListener(String, java.beans.PropertyChangeListener)</code>
     * <code>public void removePropertyChangeListener(java.beans.PropertyChangeListener)</code>
     * <code>public void removePropertyChangeListener(String, java.beans.PropertyChangeListener)</code>
     * <code>public java.beans.PropertyChangeListener[] getPropertyChangeListeners()</code>
     * </pre>
     *
     * @param declaringClass the class to which we add the support field and methods
     */
    protected void addPropertyChangeSupport(ClassNode declaringClass) {
        ClassNode pcsClassNode = ClassHelper.make(PropertyChangeSupport.class);
        ClassNode pclClassNode = ClassHelper.make(PropertyChangeListener.class);
        //String pcsFieldName = "this$propertyChangeSupport";

        // add field:
        // protected final PropertyChangeSupport this$propertyChangeSupport = new java.beans.PropertyChangeSupport(this)
        FieldNode pcsField = declaringClass.addField(
                "this$propertyChangeSupport",
                ACC_FINAL | ACC_PRIVATE | ACC_SYNTHETIC,
                pcsClassNode,
                ctorX(pcsClassNode, args(varX("this"))));

        // add method:
        // void addPropertyChangeListener(listener) {
        //     this$propertyChangeSupport.addPropertyChangeListener(listener)
        //  }
        declaringClass.addMethod(
                new MethodNode(
                        "addPropertyChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(pclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(pcsField), "addPropertyChangeListener", args(varX("listener", pclClassNode))))));

        // add method:
        // void addPropertyChangeListener(name, listener) {
        //     this$propertyChangeSupport.addPropertyChangeListener(name, listener)
        //  }
        declaringClass.addMethod(
                new MethodNode(
                        "addPropertyChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(ClassHelper.STRING_TYPE, "name"), param(pclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(pcsField), "addPropertyChangeListener", args(varX("name", ClassHelper.STRING_TYPE), varX("listener", pclClassNode))))));

        // add method:
        // boolean removePropertyChangeListener(listener) {
        //    return this$propertyChangeSupport.removePropertyChangeListener(listener);
        // }
        declaringClass.addMethod(
                new MethodNode(
                        "removePropertyChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(pclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(pcsField), "removePropertyChangeListener", args(varX("listener", pclClassNode))))));

        // add method: void removePropertyChangeListener(name, listener)
        declaringClass.addMethod(
                new MethodNode(
                        "removePropertyChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(ClassHelper.STRING_TYPE, "name"), param(pclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(pcsField), "removePropertyChangeListener", args(varX("name", ClassHelper.STRING_TYPE), varX("listener", pclClassNode))))));

        // add method:
        // void firePropertyChange(String name, Object oldValue, Object newValue) {
        //     this$propertyChangeSupport.firePropertyChange(name, oldValue, newValue)
        //  }
        declaringClass.addMethod(
                new MethodNode(
                        "firePropertyChange",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(ClassHelper.STRING_TYPE, "name"), param(ClassHelper.OBJECT_TYPE, "oldValue"), param(ClassHelper.OBJECT_TYPE, "newValue")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(pcsField), "firePropertyChange", args(varX("name", ClassHelper.STRING_TYPE), varX("oldValue"), varX("newValue"))))));

        // add method:
        // PropertyChangeListener[] getPropertyChangeListeners() {
        //   return this$propertyChangeSupport.getPropertyChangeListeners
        // }
        declaringClass.addMethod(
                new MethodNode(
                        "getPropertyChangeListeners",
                        ACC_PUBLIC,
                        pclClassNode.makeArray(),
                        Parameter.EMPTY_ARRAY,
                        ClassNode.EMPTY_ARRAY,
                        returnS(callX(fieldX(pcsField), "getPropertyChangeListeners"))));

        // add method:
        // PropertyChangeListener[] getPropertyChangeListeners(String name) {
        //   return this$propertyChangeSupport.getPropertyChangeListeners(name)
        // }
        declaringClass.addMethod(
                new MethodNode(
                        "getPropertyChangeListeners",
                        ACC_PUBLIC,
                        pclClassNode.makeArray(),
                        params(param(ClassHelper.STRING_TYPE, "name")),
                        ClassNode.EMPTY_ARRAY,
                        returnS(callX(fieldX(pcsField), "getPropertyChangeListeners", args(varX("name", ClassHelper.STRING_TYPE))))));
    }
}
