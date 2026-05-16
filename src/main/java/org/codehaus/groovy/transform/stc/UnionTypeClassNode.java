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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.ASTTransformation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound;
import static org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode;

/**
 * This class node type is very special and should only be used by the static type checker
 * to represent types which are the union of other types. This is useful when, for example,
 * we enter a section like:
 * <pre>if (x instanceof A || x instanceof B)</pre>
 * where the type of <i>x</i> can be represented as one of <i>A</i> or <i>B</i>.
 * <p>
 * This class node type should never leak outside of the type checker. More precisely, it should
 * only be used to check method call arguments, and nothing more.
 */
class UnionTypeClassNode extends ClassNode {

    private final ClassNode[] delegates;

    /**
     * Creates a union type backed by the supplied delegate types.
     */
    UnionTypeClassNode(final ClassNode... classNodes) {
        super(makeName(classNodes), 0, makeSuper(classNodes));
        delegates = classNodes;
        isPrimaryNode = false;
    }

    private static String makeName(final ClassNode[] nodes) {
        var sj = new StringJoiner("+", "<UnionType:", ">");
        for (ClassNode node : nodes) {
            sj.add(node.getText());
        }
        return sj.toString();
    }

    private static ClassNode makeSuper(final ClassNode[] nodes) {
        ClassNode upper = lowestUpperBound(Arrays.asList(nodes));
        if (upper instanceof LowestUpperBoundClassNode) {
            upper = upper.getUnresolvedSuperClass(false);
        } else if (upper.isInterface()) {
            upper = OBJECT_TYPE;
        }
        return upper;
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the delegate types that form this union.
     */
    ClassNode[] getDelegates() {
        return delegates;
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public ConstructorNode addConstructor(final int modifiers, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addConstructor(final ConstructorNode node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public FieldNode addField(final String name, final int modifiers, final ClassNode type, final Expression initialValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addField(final FieldNode node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public FieldNode addFieldFirst(final String name, final int modifiers, final ClassNode type, final Expression initialValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addFieldFirst(final FieldNode node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addInterface(final ClassNode type) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public MethodNode addMethod(final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addMethod(final MethodNode node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addMixin(final MixinNode mixin) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addObjectInitializerStatements(final Statement statements) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public PropertyNode addProperty(final String name, final int modifiers, final ClassNode type, final Expression initialValueExpression, final Statement getterBlock, final Statement setterBlock) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addProperty(final PropertyNode node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addStaticInitializerStatements(final List<Statement> staticStatements, final boolean fieldInit) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public MethodNode addSyntheticMethod(final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void addTransform(final Class<? extends ASTTransformation> transform, final ASTNode node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the abstract methods contributed by all delegate types.
     */
    @Override
    public List<MethodNode> getAbstractMethods() {
        List<MethodNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            answer.addAll(delegate.getAbstractMethods());
        }
        return answer;
    }

    /**
     * Returns the declared methods contributed by all delegate types.
     */
    @Override
    public List<MethodNode> getAllDeclaredMethods() {
        List<MethodNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            answer.addAll(delegate.getAllDeclaredMethods());
        }
        return answer;
    }

    /**
     * Returns the combined interface set of all delegate types.
     */
    @Override
    public Set<ClassNode> getAllInterfaces() {
        Set<ClassNode> answer = new HashSet<>();
        for (ClassNode delegate : delegates) {
            answer.addAll(delegate.getAllInterfaces());
        }
        return answer;
    }

    /**
     * Returns all annotations declared on the delegate types.
     */
    @Override
    public List<AnnotationNode> getAnnotations() {
        List<AnnotationNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<AnnotationNode> annotations = delegate.getAnnotations();
            if (annotations != null) answer.addAll(annotations);
        }
        return answer;
    }

    /**
     * Returns annotations of the supplied type declared on the delegate types.
     */
    @Override
    public List<AnnotationNode> getAnnotations(final ClassNode type) {
        List<AnnotationNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<AnnotationNode> annotations = delegate.getAnnotations(type);
            if (annotations != null) answer.addAll(annotations);
        }
        return answer;
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public ClassNode getComponentType() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the constructors declared by all delegate types.
     */
    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        List<ConstructorNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            answer.addAll(delegate.getDeclaredConstructors());
        }
        return answer;
    }

    /**
     * Returns the first matching declared field found across delegate types.
     */
    @Override
    public FieldNode getDeclaredField(final String name) {
        for (ClassNode delegate : delegates) {
            FieldNode node = delegate.getDeclaredField(name);
            if (node != null) return node;
        }
        return null;
    }

    /**
     * Returns the first matching declared method found across delegate types.
     */
    @Override
    public MethodNode getDeclaredMethod(final String name, final Parameter[] parameters) {
        for (ClassNode delegate : delegates) {
            MethodNode node = delegate.getDeclaredMethod(name, parameters);
            if (node != null) return node;
        }
        return null;
    }

    /**
     * Returns declared methods with the supplied name from all delegate types.
     */
    @Override
    public List<MethodNode> getDeclaredMethods(final String name) {
        List<MethodNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<MethodNode> methods = delegate.getDeclaredMethods(name);
            if (methods != null) answer.addAll(methods);
        }
        return answer;
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public Map<String, MethodNode> getDeclaredMethodsMap() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public MethodNode getEnclosingMethod() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the first matching field found across delegate types.
     */
    @Override
    public FieldNode getField(final String name) {
        for (ClassNode delegate : delegates) {
            FieldNode field = delegate.getField(name);
            if (field != null) return field;
        }
        return null;
    }

    /**
     * Returns fields contributed by all delegate types.
     */
    @Override
    public List<FieldNode> getFields() {
        List<FieldNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<FieldNode> fields = delegate.getFields();
            if (fields != null) answer.addAll(fields);
        }
        return answer;
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public Iterator<InnerClassNode> getInnerClasses() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the effective interface view of this union type.
     */
    @Override
    public ClassNode[] getInterfaces() {
        Set<ClassNode> answer = new LinkedHashSet<>();
        for (ClassNode delegate : delegates) {
            if (delegate.isInterface()) {
                answer.remove(delegate); answer.add(delegate);
            } else {
                answer.addAll(Arrays.asList(delegate.getInterfaces()));
            }
        }
        return answer.toArray(ClassNode.EMPTY_ARRAY);
    }

    /**
     * Returns methods contributed by all delegate types.
     */
    @Override
    public List<MethodNode> getMethods() {
        List<MethodNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<MethodNode> methods = delegate.getMethods();
            if (methods != null) answer.addAll(methods);
        }
        return answer;
    }

    /**
     * Returns a plain-node copy of this union type.
     */
    @Override
    public ClassNode getPlainNodeReference(final boolean skipPrimitives) {
        int n = delegates.length; ClassNode[] plainNodes = new ClassNode[n];
        for (int i = 0; i < n; i += 1) {
            plainNodes[i] = delegates[i].getPlainNodeReference(skipPrimitives);
        }
        return new UnionTypeClassNode(plainNodes);
    }

    /**
     * Returns properties contributed by all delegate types.
     */
    @Override
    public List<PropertyNode> getProperties() {
        List<PropertyNode> answer = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<PropertyNode> properties = delegate.getProperties();
            if (properties != null) answer.addAll(properties);
        }
        return answer;
    }

    /**
     * Returns the runtime type class for this class node.
     */
    @Override
    public Class getTypeClass() {
        return super.getTypeClass();
    }

    /**
     * Returns unresolved interfaces without redirects.
     */
    @Override
    public ClassNode[] getUnresolvedInterfaces() {
        return getUnresolvedInterfaces(false);
    }

    /**
     * Returns unresolved interfaces for this union type.
     */
    @Override
    public ClassNode[] getUnresolvedInterfaces(final boolean useRedirect) {
        ClassNode[] interfaces = getInterfaces();
        if (useRedirect) {
            for (int i = 0; i < interfaces.length; ++i) {
                interfaces[i] = interfaces[i].redirect();
            }
        }
        return interfaces;
    }

    /**
     * Returns a hash code derived from the delegate types.
     */
    @Override
    public int hashCode() {
        int hash = 13;
        for (ClassNode delegate : delegates) {
            hash = 31 * hash + delegate.hashCode();
        }
        return hash;
    }

    /**
     * Indicates whether every delegate implements the supplied interface.
     */
    @Override
    public boolean implementsInterface(final ClassNode classNode) {
        if (classNode == null || !classNode.isInterface()) return false;
        for (ClassNode delegate : delegates) {
            if (!delegate.implementsInterface(classNode)) return false;
        }
        return true;
    }

    /**
     * Indicates whether any delegate is annotated.
     */
    @Override
    public boolean isAnnotated() {
        for (ClassNode delegate : delegates) {
            if (delegate.isAnnotated()) return true;
        }
        return false;
    }

    /**
     * Indicates whether this union is compatible with the supplied type.
     */
    @Override
    public boolean isDerivedFrom(final ClassNode type) {
        return this.equals(type)
            || getUnresolvedSuperClass(false).isDerivedFrom(type);
    }

    /**
     * Indicates whether every delegate is an interface.
     */
    @Override
    public boolean isInterface() {
        for (ClassNode delegate : delegates) {
            if (!delegate.isInterface()) return false;
        }
        return true;
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void removeField(final String oldName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void renameField(final String oldName, final String newName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setAnnotated(final boolean flag) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setEnclosingMethod(final MethodNode enclosingMethod) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setGenericsPlaceHolder(final boolean b) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setGenericsTypes(final GenericsType[] genericsTypes) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets interfaces only while the node is still being initialized.
     */
    @Override
    public void setInterfaces(final ClassNode[] interfaces) {
        if (isPrimaryNode) {
            super.setInterfaces(interfaces);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setModifiers(final int modifiers) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public String setName(final String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setRedirect(final ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setScript(final boolean script) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setScriptBody(final boolean scriptBody) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setStaticClass(final boolean staticClass) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the super class only while the node is still being initialized.
     */
    @Override
    public void setSuperClass(final ClassNode superClass) {
        if (isPrimaryNode) {
            super.setSuperClass(superClass);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setSyntheticPublic(final boolean syntheticPublic) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setUnresolvedSuperClass(final ClassNode sn) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for union types.
     */
    @Override
    public void setUsingGenerics(final boolean b) {
        throw new UnsupportedOperationException();
    }

    /**
     * Visits the contents of each delegate type.
     */
    @Override
    public void visitContents(final GroovyClassVisitor visitor) {
        for (ClassNode delegate : delegates) {
            delegate.visitContents(visitor);
        }
    }
}
