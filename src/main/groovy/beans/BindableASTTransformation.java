/*
 * Copyright 2008-2009 the original author or authors.
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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

/**
 * Handles generation of code for the {@code @Bindable} annotation when {@code @Vetoable}
 * is not present.
 * <p/>
 * Generally, it adds (if needed) a PropertyChangeSupport field and
 * the needed add/removePropertyChangeListener methods to support the
 * listeners.
 * <p/>
 * It also generates the setter and wires the setter through the
 * PropertyChangeSupport.
 * <p/>
 * If a {@link Vetoable} annotaton is detected it does nothing and
 * lets the {@link VetoableASTTransformation} handle all the changes.
 *
 * @author Danno Ferrin (shemnon)
 * @author Chris Reeves
 */
@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class BindableASTTransformation implements ASTTransformation, Opcodes {

    protected static ClassNode boundClassNode = new ClassNode(Bindable.class);
    protected ClassNode pcsClassNode = new ClassNode(PropertyChangeSupport.class);

    /**
     * Convenience method to see if an annotated node is {@code @Bindable}.
     *
     * @param node the node to check
     * @return true if the node is bindable
     */
    public static boolean hasBindableAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations()) {
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
            if (VetoableASTTransformation.hasVetoableAnnotation(parent.getDeclaringClass()))
            {
                // VetoableASTTransformation will handle both @Bindable and @Vetoable
                return;
            }
            addListenerToProperty(source, node, declaringClass, (FieldNode) parent);
        } else if (parent instanceof ClassNode) {
            addListenerToClass(source, node, (ClassNode) parent);
        }
    }

    private void addListenerToProperty(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field) {
        String fieldName = field.getName();
        for (PropertyNode propertyNode : (Collection<PropertyNode>) declaringClass.getProperties()) {
            if (propertyNode.getName().equals(fieldName)) {
                if (field.isStatic()) {
                    //noinspection ThrowableInstanceNeverThrown
                    source.getErrorCollector().addErrorAndContinue(
                                new SyntaxErrorMessage(new SyntaxException(
                                    "@groovy.beans.Bindable cannot annotate a static property.",
                                    node.getLineNumber(),
                                    node.getColumnNumber()),
                                    source));
                } else {
                    if (needsPropertyChangeSupport(declaringClass, source)) {
                        addPropertyChangeSupport(declaringClass);
                    }
                    createListenerSetter(source, node, declaringClass, propertyNode);
                }
                return;
            }
        }
        //noinspection ThrowableInstanceNeverThrown
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(
                        "@groovy.beans.Bindable must be on a property, not a field.  Try removing the private, protected, or public modifier.",
                        node.getLineNumber(),
                        node.getColumnNumber()),
                        source));
    }

    private void addListenerToClass(SourceUnit source, AnnotationNode node, ClassNode classNode) {
        if (needsPropertyChangeSupport(classNode, source)) {
            addPropertyChangeSupport(classNode);
        }
        for (PropertyNode propertyNode : (Collection<PropertyNode>) classNode.getProperties()) {
            FieldNode field = propertyNode.getField();
            // look to see if per-field handlers will catch this one...
            if (hasBindableAnnotation(field)
                || field.isStatic()
                || VetoableASTTransformation.hasVetoableAnnotation(field))
            {
                // explicitly labeled properties are already handled,
                // don't transform static properties
                // VetoableASTTransformation will handle both @Bindable and @Vetoable
                continue;
            }
            createListenerSetter(source, node, classNode, propertyNode);
        }
    }

    /*
     * Wrap an existing setter.
     */
    private void wrapSetterMethod(ClassNode classNode, String propertyName) {
        String getterName = "get" + MetaClassHelper.capitalize(propertyName);
        MethodNode setter = classNode.getSetterMethod("set" + MetaClassHelper.capitalize(propertyName));

        if (setter != null) {
            // Get the existing code block
            Statement code = setter.getCode();

            VariableExpression oldValue = new VariableExpression("$oldValue");
            VariableExpression newValue = new VariableExpression("$newValue");
            BlockStatement block = new BlockStatement();

            // create a local variable to hold the old value from the getter
            block.addStatement(new ExpressionStatement(
                new DeclarationExpression(oldValue,
                    Token.newSymbol(Types.EQUALS, 0, 0),
                    new MethodCallExpression(VariableExpression.THIS_EXPRESSION, getterName, ArgumentListExpression.EMPTY_ARGUMENTS))));

            // call the existing block, which will presumably set the value properly
            block.addStatement(code);

            // get the new value to emit in the event
            block.addStatement(new ExpressionStatement(
                new DeclarationExpression(newValue,
                    Token.newSymbol(Types.EQUALS, 0, 0),
                    new MethodCallExpression(VariableExpression.THIS_EXPRESSION, getterName, ArgumentListExpression.EMPTY_ARGUMENTS))));

            // add the firePropertyChange method call
            block.addStatement(new ExpressionStatement(new MethodCallExpression(
                    VariableExpression.THIS_EXPRESSION,
                    "firePropertyChange",
                    new ArgumentListExpression(
                            new Expression[]{
                                    new ConstantExpression(propertyName),
                                    oldValue,
                                    newValue}))));

            // replace the existing code block with our new one
            setter.setCode(block);
        }
    }

    private void createListenerSetter(SourceUnit source, AnnotationNode node, ClassNode classNode, PropertyNode propertyNode) {
        String setterName = "set" + MetaClassHelper.capitalize(propertyNode.getName());
        if (classNode.getMethods(setterName).isEmpty()) {
            Expression fieldExpression = new FieldExpression(propertyNode.getField());
            Statement setterBlock = createBindableStatement(propertyNode, fieldExpression);

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
        return new ExpressionStatement(
                new MethodCallExpression(
                        VariableExpression.THIS_EXPRESSION,
                        "firePropertyChange",
                        new ArgumentListExpression(
                                new Expression[]{
                                        new ConstantExpression(propertyNode.getName()),
                                        fieldExpression,
                                        new BinaryExpression(
                                                fieldExpression,
                                                Token.newSymbol(Types.EQUAL, 0, 0),
                                                new VariableExpression("value"))})));
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
        Parameter[] setterParameterTypes = {new Parameter(propertyNode.getType(), "value")};
        MethodNode setter =
                new MethodNode(setterName, propertyNode.getModifiers(), ClassHelper.VOID_TYPE, setterParameterTypes, ClassNode.EMPTY_ARRAY, setterBlock);
        setter.setSynthetic(true);
        // add it to the class
        declaringClass.addMethod(setter);
    }

    /**
     * Snoops through the declaring class and all parents looking for methods
     * void addPropertyChangeListener(PropertyChangeListener),
     * void removePropertyChangeListener(PropertyChangeListener), and
     * void firePropertyChange(String, Object, Object).  If any are defined all
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
     * <p/>
     * Adds a new field:
     * <code>protected final java.beans.PropertyChangeSupport this$PropertyChangeSupport = new java.beans.PropertyChangeSupport(this)</code>"
     * <p/>
     * Also adds support methods:
     * <code>public void addPropertyChangeListener(java.beans.PropertyChangeListener)</code>
     * <code>public void addPropertyChangeListener(String, java.beans.PropertyChangeListener)</code>
     * <code>public void removePropertyChangeListener(java.beans.PropertyChangeListener)</code>
     * <code>public void removePropertyChangeListener(String, java.beans.PropertyChangeListener)</code>
     * <code>public java.beans.PropertyChangeListener[] getPropertyChangeListeners()</code>
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
                new ConstructorCallExpression(pcsClassNode,
                        new ArgumentListExpression(new Expression[]{new VariableExpression("this")})));

        // add method:
        // void addPropertyChangeListener(listener) {
        //     this$propertyChangeSupport.addPropertyChangeListner(listener)
        //  }
        declaringClass.addMethod(
                new MethodNode(
                        "addPropertyChangeListener",
                        ACC_PUBLIC | ACC_SYNTHETIC,
                        ClassHelper.VOID_TYPE,
                        new Parameter[]{new Parameter(pclClassNode, "listener")},
                        ClassNode.EMPTY_ARRAY,
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        new FieldExpression(pcsField),
                                        "addPropertyChangeListener",
                                        new ArgumentListExpression(
                                                new Expression[]{new VariableExpression("listener")})))));

        // add method:
        // void addPropertyChangeListener(name, listener) {
        //     this$propertyChangeSupport.addPropertyChangeListner(name, listener)
        //  }
        declaringClass.addMethod(
                new MethodNode(
                        "addPropertyChangeListener",
                        ACC_PUBLIC | ACC_SYNTHETIC,
                        ClassHelper.VOID_TYPE,
                        new Parameter[]{new Parameter(ClassHelper.STRING_TYPE, "name"), new Parameter(pclClassNode, "listener")},
                        ClassNode.EMPTY_ARRAY,
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        new FieldExpression(pcsField),
                                        "addPropertyChangeListener",
                                        new ArgumentListExpression(
                                                new Expression[]{new VariableExpression("name"), new VariableExpression("listener")})))));

        // add method:
        // boolean removePropertyChangeListener(listener) {
        //    return this$propertyChangeSupport.removePropertyChangeListener(listener);
        // }
        declaringClass.addMethod(
                new MethodNode(
                        "removePropertyChangeListener",
                        ACC_PUBLIC | ACC_SYNTHETIC,
                        ClassHelper.VOID_TYPE,
                        new Parameter[]{new Parameter(pclClassNode, "listener")},
                        ClassNode.EMPTY_ARRAY,
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        new FieldExpression(pcsField),
                                        "removePropertyChangeListener",
                                        new ArgumentListExpression(
                                                new Expression[]{new VariableExpression("listener")})))));

        // add method: void removePropertyChangeListener(name, listener)
        declaringClass.addMethod(
                new MethodNode(
                        "removePropertyChangeListener",
                        ACC_PUBLIC | ACC_SYNTHETIC,
                        ClassHelper.VOID_TYPE,
                        new Parameter[]{new Parameter(ClassHelper.STRING_TYPE, "name"), new Parameter(pclClassNode, "listener")},
                        ClassNode.EMPTY_ARRAY,
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        new FieldExpression(pcsField),
                                        "removePropertyChangeListener",
                                        new ArgumentListExpression(
                                                new Expression[]{new VariableExpression("name"), new VariableExpression("listener")})))));

        // add method:
        // void firePropertyChange(String name, Object oldValue, Object newValue) {
        //     this$propertyChangeSupport.firePropertyChange(name, oldValue, newValue)
        //  }
        declaringClass.addMethod(
                new MethodNode(
                        "firePropertyChange",
                        ACC_PUBLIC | ACC_SYNTHETIC,
                        ClassHelper.VOID_TYPE,
                        new Parameter[]{new Parameter(ClassHelper.STRING_TYPE, "name"), new Parameter(ClassHelper.OBJECT_TYPE, "oldValue"), new Parameter(ClassHelper.OBJECT_TYPE, "newValue")},
                        ClassNode.EMPTY_ARRAY,
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        new FieldExpression(pcsField),
                                        "firePropertyChange",
                                        new ArgumentListExpression(
                                                new Expression[]{
                                                        new VariableExpression("name"),
                                                        new VariableExpression("oldValue"),
                                                        new VariableExpression("newValue")})))));

        // add method:
        // PropertyChangeSupport[] getPropertyChangeListeners() {
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
