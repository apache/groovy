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
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the {@code @Vetoable} annotation, and {@code @Bindable}
 * if also present.
 * <p>
 * Generally, it adds (if needed) a VetoableChangeSupport field and
 * the needed add/removeVetoableChangeListener methods to support the
 * listeners.
 * <p>
 * It also generates the setter and wires the setter through the
 * VetoableChangeSupport.
 * <p>
 * If a {@link Bindable} annotation is detected it also adds support similar
 * to what {@link BindableASTTransformation} would do.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class VetoableASTTransformation extends BindableASTTransformation {

    protected static final ClassNode constrainedClassNode = ClassHelper.make(Vetoable.class);

    /**
     * Convenience method to see if an annotated node is {@code @Vetoable}.
     *
     * @param node the node to check
     * @return true if the node is constrained
     */
    public static boolean hasVetoableAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (constrainedClassNode.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes   the AST nodes
     * @param source  the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: $node.class / $parent.class");
        }
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (nodes[1] instanceof ClassNode) {
            addListenerToClass(source, (ClassNode) nodes[1]);
        } else {
            if ((((FieldNode)nodes[1]).getModifiers() & Opcodes.ACC_FINAL) != 0) {
                source.getErrorCollector().addErrorAndContinue("@groovy.beans.Vetoable cannot annotate a final property.", node, source);
            }

            addListenerToProperty(source, node, (AnnotatedNode) nodes[1]);
        }
    }

    private void addListenerToProperty(SourceUnit source, AnnotationNode node, AnnotatedNode parent) {
        ClassNode declaringClass = parent.getDeclaringClass();
        FieldNode field = ((FieldNode) parent);
        String fieldName = field.getName();
        for (PropertyNode propertyNode : declaringClass.getProperties()) {
            boolean bindable = BindableASTTransformation.hasBindableAnnotation(parent)
                    || BindableASTTransformation.hasBindableAnnotation(parent.getDeclaringClass());

            if (propertyNode.getName().equals(fieldName)) {
                if (field.isStatic()) {
                    //noinspection ThrowableInstanceNeverThrown
                    source.getErrorCollector().addErrorAndContinue("@groovy.beans.Vetoable cannot annotate a static property.", node, source);
                } else {
                    createListenerSetter(source, bindable, declaringClass, propertyNode);
                }
                return;
            }
        }
        //noinspection ThrowableInstanceNeverThrown
        source.getErrorCollector().addErrorAndContinue("@groovy.beans.Vetoable must be on a property, not a field.  Try removing the private, protected, or public modifier.", node, source);
    }


    private void addListenerToClass(SourceUnit source, ClassNode classNode) {
        boolean bindable = BindableASTTransformation.hasBindableAnnotation(classNode);
        for (PropertyNode propertyNode : classNode.getProperties()) {
            if (!hasVetoableAnnotation(propertyNode.getField())
                && !propertyNode.getField().isFinal()
                && !propertyNode.getField().isStatic())
            {
                createListenerSetter(source,
                        bindable || BindableASTTransformation.hasBindableAnnotation(propertyNode.getField()),
                    classNode, propertyNode);
            }
        }
    }

    /**
     * Wrap an existing setter.
     */
    private static void wrapSetterMethod(ClassNode classNode, boolean bindable, PropertyNode propertyNode) {
        String getterName = propertyNode.getGetterNameOrDefault();
        String propertyName = propertyNode.getName();
        MethodNode setter = classNode.getSetterMethod(propertyNode.getSetterNameOrDefault());

        if (setter != null) {
            // Get the existing code block
            Statement code = setter.getCode();

            Expression oldValue = localVarX("$oldValue");
            Expression newValue = localVarX("$newValue");
            Expression proposedValue = varX(setter.getParameters()[0].getName());
            BlockStatement block = new BlockStatement();

            // create a local variable to hold the old value from the getter
            block.addStatement(declS(oldValue, callThisX(getterName)));

            // add the fireVetoableChange method call
            block.addStatement(stmt(callThisX("fireVetoableChange", args(
                    constX(propertyName), oldValue, proposedValue))));

            // call the existing block, which will presumably set the value properly
            block.addStatement(code);

            if (bindable) {
                // get the new value to emit in the event
                block.addStatement(declS(newValue, callThisX(getterName)));

                // add the firePropertyChange method call
                block.addStatement(stmt(callThisX("firePropertyChange", args(constX(propertyName), oldValue, newValue))));
            }

            // replace the existing code block with our new one
            setter.setCode(block);
        }
    }

    private void createListenerSetter(SourceUnit source, boolean bindable, ClassNode declaringClass, PropertyNode propertyNode) {
        if (bindable && needsPropertyChangeSupport(declaringClass, source)) {
            addPropertyChangeSupport(declaringClass);
        }
        if (needsVetoableChangeSupport(declaringClass, source)) {
            addVetoableChangeSupport(declaringClass);
        }
        String setterName = propertyNode.getSetterNameOrDefault();
        if (declaringClass.getMethods(setterName).isEmpty()) {
            Expression fieldExpression = fieldX(propertyNode.getField());
            BlockStatement setterBlock = new BlockStatement();
            setterBlock.addStatement(createConstrainedStatement(propertyNode, fieldExpression));
            if (bindable) {
                setterBlock.addStatement(createBindableStatement(propertyNode, fieldExpression));
            } else {
                setterBlock.addStatement(createSetStatement(fieldExpression));
            }

            // create method void <setter>(<type> fieldName)
            createSetterMethod(declaringClass, propertyNode, setterName, setterBlock);
        } else {
            wrapSetterMethod(declaringClass, bindable, propertyNode);
        }
    }

    /**
     * Creates a statement body similar to:
     * <code>this.fireVetoableChange("field", field, field = value)</code>
     *
     * @param propertyNode           the field node for the property
     * @param fieldExpression a field expression for setting the property value
     * @return the created statement
     */
    protected Statement createConstrainedStatement(PropertyNode propertyNode, Expression fieldExpression) {
        return stmt(callThisX("fireVetoableChange", args(constX(propertyNode.getName()), fieldExpression, varX("value"))));
    }

    /**
     * Creates a statement body similar to:
     * <code>field = value</code>.
     * <p>
     * Used when the field is not also {@code @Bindable}.
     *
     * @param fieldExpression a field expression for setting the property value
     * @return the created statement
     */
    protected Statement createSetStatement(Expression fieldExpression) {
        return assignS(fieldExpression, varX("value"));
    }

    /**
     * Snoops through the declaring class and all parents looking for a field
     * of type VetoableChangeSupport.  Remembers the field and returns false
     * if found otherwise returns true to indicate that such support should
     * be added.
     *
     * @param declaringClass the class to search
     * @return true if vetoable change support should be added
     */
    protected boolean needsVetoableChangeSupport(ClassNode declaringClass, SourceUnit sourceUnit) {
        boolean foundAdd = false, foundRemove = false, foundFire = false;
        ClassNode consideredClass = declaringClass;
        while (consideredClass!= null) {
            for (MethodNode method : consideredClass.getMethods()) {
                // just check length, MOP will match it up
                foundAdd = foundAdd || method.getName().equals("addVetoableChangeListener") && method.getParameters().length == 1;
                foundRemove = foundRemove || method.getName().equals("removeVetoableChangeListener") && method.getParameters().length == 1;
                foundFire = foundFire || method.getName().equals("fireVetoableChange") && method.getParameters().length == 3;
                if (foundAdd && foundRemove && foundFire) {
                    return false;
                }
            }
            consideredClass = consideredClass.getSuperClass();
        }
        // check if a super class has @Vetoable annotations
        consideredClass = declaringClass.getSuperClass();
        while (consideredClass!=null) {
            if (hasVetoableAnnotation(consideredClass)) return false;
            for (FieldNode field : consideredClass.getFields()) {
                if (hasVetoableAnnotation(field)) return false;
            }
            consideredClass = consideredClass.getSuperClass();
        }
        if (foundAdd || foundRemove || foundFire) {
            sourceUnit.getErrorCollector().addErrorAndContinue(
                new SimpleMessage("@Vetoable cannot be processed on "
                    + declaringClass.getName()
                    + " because some but not all of addVetoableChangeListener, removeVetoableChange, and fireVetoableChange were declared in the current or super classes.",
                sourceUnit)
            );
            return false;
        }
        return true;
    }

    /**
     * Creates a setter method with the given body.
     * <p>
     * This differs from normal setters in that we need to add a declared
     * exception java.beans.PropertyVetoException
     *
     * @param declaringClass the class to which we will add the setter
     * @param propertyNode          the field to back the setter
     * @param setterName     the name of the setter
     * @param setterBlock    the statement representing the setter block
     */
    protected void createSetterMethod(ClassNode declaringClass, PropertyNode propertyNode, String setterName, Statement setterBlock) {
        ClassNode[] exceptions = {ClassHelper.make(PropertyVetoException.class)};
        MethodNode setter = new MethodNode(
                setterName,
                PropertyNodeUtils.adjustPropertyModifiersForMethod(propertyNode),
                ClassHelper.VOID_TYPE,
                params(param(propertyNode.getType(), "value")),
                exceptions,
                setterBlock);
        setter.setSynthetic(true);
        // add it to the class
        addGeneratedMethod(declaringClass, setter);
    }

    /**
     * Adds the necessary field and methods to support vetoable change support.
     * <p>
     * Adds a new field:
     * <code>"protected final java.beans.VetoableChangeSupport this$vetoableChangeSupport = new java.beans.VetoableChangeSupport(this)"</code>
     * <p>
     * Also adds support methods:
     * <code>public void addVetoableChangeListener(java.beans.VetoableChangeListener)</code>
     * <code>public void addVetoableChangeListener(String, java.beans.VetoableChangeListener)</code>
     * <code>public void removeVetoableChangeListener(java.beans.VetoableChangeListener)</code>
     * <code>public void removeVetoableChangeListener(String, java.beans.VetoableChangeListener)</code>
     * <code>public java.beans.VetoableChangeListener[] getVetoableChangeListeners()</code>
     *
     * @param declaringClass the class to which we add the support field and methods
     */
    protected void addVetoableChangeSupport(ClassNode declaringClass) {
        ClassNode vcsClassNode = ClassHelper.make(VetoableChangeSupport.class);
        ClassNode vclClassNode = ClassHelper.make(VetoableChangeListener.class);

        // add field:
        // protected static VetoableChangeSupport this$vetoableChangeSupport = new java.beans.VetoableChangeSupport(this)
        FieldNode vcsField = declaringClass.addField(
                "this$vetoableChangeSupport",
                ACC_FINAL | ACC_PRIVATE | ACC_SYNTHETIC,
                vcsClassNode,
                ctorX(vcsClassNode, args(varX("this"))));

        // add method:
        // void addVetoableChangeListener(listener) {
        //     this$vetoableChangeSupport.addVetoableChangeListener(listener)
        //  }
        addGeneratedMethod(declaringClass,
                new MethodNode(
                        "addVetoableChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(vclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(vcsField), "addVetoableChangeListener", args(varX("listener", vclClassNode))))));

        // add method:
        // void addVetoableChangeListener(name, listener) {
        //     this$vetoableChangeSupport.addVetoableChangeListener(name, listener)
        //  }
        addGeneratedMethod(declaringClass,
                new MethodNode(
                        "addVetoableChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(ClassHelper.STRING_TYPE, "name"), param(vclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(vcsField), "addVetoableChangeListener", args(varX("name", ClassHelper.STRING_TYPE), varX("listener", vclClassNode))))));

        // add method:
        // boolean removeVetoableChangeListener(listener) {
        //    return this$vetoableChangeSupport.removeVetoableChangeListener(listener);
        // }
        addGeneratedMethod(declaringClass,
                new MethodNode(
                        "removeVetoableChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(vclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(vcsField), "removeVetoableChangeListener", args(varX("listener", vclClassNode))))));

        // add method: void removeVetoableChangeListener(name, listener)
        addGeneratedMethod(declaringClass,
                new MethodNode(
                        "removeVetoableChangeListener",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(ClassHelper.STRING_TYPE, "name"), param(vclClassNode, "listener")),
                        ClassNode.EMPTY_ARRAY,
                        stmt(callX(fieldX(vcsField), "removeVetoableChangeListener", args(varX("name", ClassHelper.STRING_TYPE), varX("listener", vclClassNode))))));

        // add method:
        // void fireVetoableChange(String name, Object oldValue, Object newValue)
        //    throws PropertyVetoException
        // {
        //     this$vetoableChangeSupport.fireVetoableChange(name, oldValue, newValue)
        //  }
        addGeneratedMethod(declaringClass,
                new MethodNode(
                        "fireVetoableChange",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        params(param(ClassHelper.STRING_TYPE, "name"), param(ClassHelper.OBJECT_TYPE, "oldValue"), param(ClassHelper.OBJECT_TYPE, "newValue")),
                        new ClassNode[] {ClassHelper.make(PropertyVetoException.class)},
                        stmt(callX(fieldX(vcsField), "fireVetoableChange", args(varX("name", ClassHelper.STRING_TYPE), varX("oldValue"), varX("newValue"))))));

        // add method:
        // VetoableChangeListener[] getVetoableChangeListeners() {
        //   return this$vetoableChangeSupport.getVetoableChangeListeners
        // }
        addGeneratedMethod(declaringClass,
                new MethodNode(
                        "getVetoableChangeListeners",
                        ACC_PUBLIC,
                        vclClassNode.makeArray(),
                        Parameter.EMPTY_ARRAY,
                        ClassNode.EMPTY_ARRAY,
                        returnS(callX(fieldX(vcsField), "getVetoableChangeListeners"))));

        // add method:
        // VetoableChangeListener[] getVetoableChangeListeners(String name) {
        //   return this$vetoableChangeSupport.getVetoableChangeListeners(name)
        // }
        addGeneratedMethod(declaringClass,
                new MethodNode(
                        "getVetoableChangeListeners",
                        ACC_PUBLIC,
                        vclClassNode.makeArray(),
                        params(param(ClassHelper.STRING_TYPE, "name")),
                        ClassNode.EMPTY_ARRAY,
                        returnS(callX(fieldX(vcsField), "getVetoableChangeListeners", args(varX("name", ClassHelper.STRING_TYPE))))));
    }

}
