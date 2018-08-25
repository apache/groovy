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
import org.codehaus.groovy.ast.ClassHelper;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class node type is very special and should only be used by the static type checker
 * to represent types which are the union of other types. This is useful when, for example,
 * we enter a section like :
 * <pre>if (x instanceof A || x instanceof B)</pre>
 * where the type of <i>x</i> can be represented as one of <i>A</i> or <i>B</i>.
 *
 * This class node type should never leak outside of the type checker. More precisely, it should
 * only be used to check method call arguments, and nothing more.
 */
class UnionTypeClassNode extends ClassNode {
    private final ClassNode[] delegates;

    public UnionTypeClassNode(ClassNode... classNodes) {
        super("<UnionType:" + asArrayDescriptor(classNodes) + ">", 0, ClassHelper.OBJECT_TYPE);
        delegates = classNodes == null ? ClassNode.EMPTY_ARRAY : classNodes;
    }

    private static String asArrayDescriptor(ClassNode... nodes) {
        StringBuilder sb = new StringBuilder();
        for (ClassNode node : nodes) {
            if (sb.length() > 0) sb.append("+");
            sb.append(node.getText());
        }
        return sb.toString();
    }

    public ClassNode[] getDelegates() {
        return delegates;
    }

    @Override
    public ConstructorNode addConstructor(final int modifiers, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConstructor(final ConstructorNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldNode addField(final String name, final int modifiers, final ClassNode type, final Expression initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addField(final FieldNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldNode addFieldFirst(final String name, final int modifiers, final ClassNode type, final Expression initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFieldFirst(final FieldNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addInterface(final ClassNode type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodNode addMethod(final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMethod(final MethodNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMixin(final MixinNode mixin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addObjectInitializerStatements(final Statement statements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertyNode addProperty(final String name, final int modifiers, final ClassNode type, final Expression initialValueExpression, final Statement getterBlock, final Statement setterBlock) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addProperty(final PropertyNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addStaticInitializerStatements(final List<Statement> staticStatements, final boolean fieldInit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodNode addSyntheticMethod(final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTransform(final Class<? extends ASTTransformation> transform, final ASTNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean declaresInterface(final ClassNode classNode) {
        for (ClassNode delegate : delegates) {
            if (delegate.declaresInterface(classNode)) return true;
        }
        return false;
    }

    @Override
    public List<MethodNode> getAbstractMethods() {
        List<MethodNode> allMethods = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            allMethods.addAll(delegate.getAbstractMethods());
        }
        return allMethods;
    }

    @Override
    public List<MethodNode> getAllDeclaredMethods() {
        List<MethodNode> allMethods = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            allMethods.addAll(delegate.getAllDeclaredMethods());
        }
        return allMethods;
    }

    @Override
    public Set<ClassNode> getAllInterfaces() {
        Set<ClassNode> allMethods = new HashSet<>();
        for (ClassNode delegate : delegates) {
            allMethods.addAll(delegate.getAllInterfaces());
        }
        return allMethods;
    }

    @Override
    public List<AnnotationNode> getAnnotations() {
        List<AnnotationNode> nodes = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<AnnotationNode> annotations = delegate.getAnnotations();
            if (annotations != null) nodes.addAll(annotations);
        }
        return nodes;
    }

    @Override
    public List<AnnotationNode> getAnnotations(final ClassNode type) {
        List<AnnotationNode> nodes = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<AnnotationNode> annotations = delegate.getAnnotations(type);
            if (annotations != null) nodes.addAll(annotations);
        }
        return nodes;
    }

    @Override
    public ClassNode getComponentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        List<ConstructorNode> nodes = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            nodes.addAll(delegate.getDeclaredConstructors());
        }
        return nodes;
    }

    @Override
    public FieldNode getDeclaredField(final String name) {
        for (ClassNode delegate : delegates) {
            FieldNode node = delegate.getDeclaredField(name);
            if (node != null) return node;
        }
        return null;
    }

    @Override
    public MethodNode getDeclaredMethod(final String name, final Parameter[] parameters) {
        for (ClassNode delegate : delegates) {
            MethodNode node = delegate.getDeclaredMethod(name, parameters);
            if (node != null) return node;
        }
        return null;
    }

    @Override
    public List<MethodNode> getDeclaredMethods(final String name) {
        List<MethodNode> nodes = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<MethodNode> methods = delegate.getDeclaredMethods(name);
            if (methods != null) nodes.addAll(methods);
        }
        return nodes;
    }

    @Override
    public Map<String, MethodNode> getDeclaredMethodsMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodNode getEnclosingMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldNode getField(final String name) {
        for (ClassNode delegate : delegates) {
            FieldNode field = delegate.getField(name);
            if (field != null) return field;
        }
        return null;
    }

    @Override
    public List<FieldNode> getFields() {
        List<FieldNode> nodes = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<FieldNode> fields = delegate.getFields();
            if (fields != null) nodes.addAll(fields);
        }
        return nodes;
    }

    @Override
    public Iterator<InnerClassNode> getInnerClasses() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassNode[] getInterfaces() {
        Set<ClassNode> nodes = new LinkedHashSet<>();
        for (ClassNode delegate : delegates) {
            ClassNode[] interfaces = delegate.getInterfaces();
            if (interfaces != null) Collections.addAll(nodes, interfaces);
        }
        return nodes.toArray(ClassNode.EMPTY_ARRAY);
    }

    @Override
    public List<MethodNode> getMethods() {
        List<MethodNode> nodes = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<MethodNode> methods = delegate.getMethods();
            if (methods != null) nodes.addAll(methods);
        }
        return nodes;
    }

    @Override
    public List<PropertyNode> getProperties() {
        List<PropertyNode> nodes = new LinkedList<>();
        for (ClassNode delegate : delegates) {
            List<PropertyNode> properties = delegate.getProperties();
            if (properties != null) nodes.addAll(properties);
        }
        return nodes;
    }

    @Override
    public Class getTypeClass() {
        return super.getTypeClass();
    }

    @Override
    public ClassNode[] getUnresolvedInterfaces() {
        Set<ClassNode> nodes = new LinkedHashSet<>();
        for (ClassNode delegate : delegates) {
            ClassNode[] interfaces = delegate.getUnresolvedInterfaces();
            if (interfaces != null) Collections.addAll(nodes, interfaces);
        }
        return nodes.toArray(ClassNode.EMPTY_ARRAY);
    }

    @Override
    public ClassNode[] getUnresolvedInterfaces(final boolean useRedirect) {
        Set<ClassNode> nodes = new LinkedHashSet<>();
        for (ClassNode delegate : delegates) {
            ClassNode[] interfaces = delegate.getUnresolvedInterfaces(useRedirect);
            if (interfaces != null) Collections.addAll(nodes, interfaces);
        }
        return nodes.toArray(ClassNode.EMPTY_ARRAY);
    }

    @Override
    public int hashCode() {
        int hash = 13;
        for (ClassNode delegate : delegates) {
            hash = 31 * hash + delegate.hashCode();
        }
        return hash;
    }

    @Override
    public boolean implementsInterface(final ClassNode classNode) {
        for (ClassNode delegate : delegates) {
            if (delegate.implementsInterface(classNode)) return true;
        }
        return false;
    }

    @Override
    public boolean isAnnotated() {
        for (ClassNode delegate : delegates) {
            if (delegate.isAnnotated()) return true;
        }
        return false;
    }

    @Override
    public boolean isDerivedFrom(final ClassNode type) {
        for (ClassNode delegate : delegates) {
            if (delegate.isDerivedFrom(type)) return true;
        }
        return false;
    }

    @Override
    public boolean isDerivedFromGroovyObject() {
        for (ClassNode delegate : delegates) {
            if (delegate.isDerivedFromGroovyObject()) return true;
        }
        return false;
    }

    @Override
    public void removeField(final String oldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void renameField(final String oldName, final String newName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAnnotated(final boolean flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnclosingMethod(final MethodNode enclosingMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsPlaceHolder(final boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsTypes(final GenericsType[] genericsTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInterfaces(final ClassNode[] interfaces) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setModifiers(final int modifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String setName(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRedirect(final ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScript(final boolean script) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScriptBody(final boolean scriptBody) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStaticClass(final boolean staticClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSuperClass(final ClassNode superClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSyntheticPublic(final boolean syntheticPublic) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUnresolvedSuperClass(final ClassNode sn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUsingGenerics(final boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitContents(final GroovyClassVisitor visitor) {
        for (ClassNode delegate : delegates) {
            delegate.visitContents(visitor);
        }
    }
}
