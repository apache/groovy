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
package org.apache.groovy.antlr;

import groovy.lang.groovydoc.Groovydoc;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.tools.groovydoc.LinkArgument;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyConstructorDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyExecutableMemberDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyFieldDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyMethodDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyParameter;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyType;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.codehaus.groovy.transform.trait.Traits.isTrait;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * A visitor which collects Groovydoc information.
 */
public class GroovydocVisitor extends ClassCodeVisitorSupport {
    private final SourceUnit unit;
    private final List<LinkArgument> links;
    private String packagePath;
    private SimpleGroovyClassDoc currentClassDoc = null;
    private Map<String, GroovyClassDoc> classDocs = new HashMap<>();
    private static final String FS = "/";
    private final Map<String, String> aliases = new HashMap<>();

    public GroovydocVisitor(final SourceUnit unit, String packagePath, List<LinkArgument> links) {
        this.unit = unit;
        this.packagePath = packagePath;
        this.links = links;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public void visitClass(ClassNode node) {
        List<String> imports = node.getModule().getImports().stream().map(ImportNode::getClassName).collect(Collectors.toList());
        String name = node.getNameWithoutPackage();

        if (node instanceof InnerClassNode) {
            name = name.replaceAll("\\$", ".");
        }
        currentClassDoc = new SimpleGroovyClassDoc(imports, aliases, name, links);
        if (node.isEnum()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.ENUM_DEF);
        }
        if (node.isAnnotationDefinition()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.ANNOTATION_DEF);
        }
        if (node.isInterface()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.INTERFACE_DEF);
        }
        if (isTrait(node)) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.TRAIT_DEF);
        }
        if (node.isScript()) {
            currentClassDoc.setScript(true);
        }
        for (ClassNode iface : node.getInterfaces()) {
            currentClassDoc.addInterfaceName(makeType(iface));
        }
        if (!node.isInterface() && node.getSuperClass() != null) {
            String superName = makeType(node.getSuperClass());
            currentClassDoc.setSuperClassName(superName);
            String superSimpleName = node.getSuperClass().getNameWithoutPackage();
            if (!classDocs.containsKey(superSimpleName)) {
                SimpleGroovyClassDoc superDoc = new SimpleGroovyClassDoc(imports, superName);
                superDoc.setFullPathName(superName);
            }
        }
        if (Modifier.isPublic(node.getModifiers())) {
            currentClassDoc.setPublic(true);
        }
        if (Modifier.isProtected(node.getModifiers())) {
            currentClassDoc.setProtected(true);
        }
        if (Modifier.isPrivate(node.getModifiers())) {
            currentClassDoc.setPrivate(true);
        }
        if (Modifier.isStatic(node.getModifiers())) {
            currentClassDoc.setStatic(true);
        }
        if (Modifier.isFinal(node.getModifiers())) {
            currentClassDoc.setFinal(true);
        }
        if (Modifier.isAbstract(node.getModifiers())) {
            currentClassDoc.setAbstract(true);
        }
        currentClassDoc.setFullPathName(packagePath + FS + name);
        currentClassDoc.setGroovy(true);
        currentClassDoc.setNameWithTypeArgs(name);
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        super.visitClass(node);
        SimpleGroovyClassDoc parent = currentClassDoc;
        boolean explicitCons = false;
        for (GroovyMethodDoc meth : currentClassDoc.methods()) {
            if (meth instanceof SimpleGroovyConstructorDoc) {
                explicitCons = true;
                break;
            }
        }
        if (!explicitCons) {
            // add default no-arg constructor
            SimpleGroovyConstructorDoc cons = new SimpleGroovyConstructorDoc(name, currentClassDoc);
            cons.setPublic(true);
            currentClassDoc.add(cons);
        }
        Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
        while (innerClasses.hasNext()) {
            visitClass(innerClasses.next());
            parent.addNested(currentClassDoc);
            currentClassDoc = parent;
        }
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        SimpleGroovyConstructorDoc cons = new SimpleGroovyConstructorDoc(currentClassDoc.simpleTypeName(), currentClassDoc);
        setConstructorOrMethodCommon(node, cons);
        currentClassDoc.add(cons);
        super.visitConstructor(node);
    }

    @Override
    public void visitMethod(MethodNode node) {
        SimpleGroovyMethodDoc meth = new SimpleGroovyMethodDoc(node.getName(), currentClassDoc);
        meth.setReturnType(new SimpleGroovyType(makeType(node.getReturnType())));
        setConstructorOrMethodCommon(node, meth);
        currentClassDoc.add(meth);
        processPropertiesFromGetterSetter(meth);
    }

    private void processPropertiesFromGetterSetter(SimpleGroovyMethodDoc currentMethodDoc) {
        String methodName = currentMethodDoc.name();
        int len = methodName.length();
        String prefix = null;
        String propName = null;
        if (len > 3 && methodName.startsWith("get")) {
            prefix = "get";
            propName = methodName.substring(3);
        } else if (len > 3 && methodName.startsWith("set")) {
            prefix = "set";
            propName = methodName.substring(3);
        } else if (len > 2 && methodName.startsWith("is")) {
            prefix = "is";
            propName = methodName.substring(2);
        } else {
            // Not a (get/set/is) method that contains a property name
            return;
        }

        SimpleGroovyClassDoc classDoc = currentClassDoc;
        // TODO: not sure why but groovy.ui.view.BasicContentPane#buildOutputArea classDoc is null
        if (classDoc == null) {
            return;
        }
        GroovyMethodDoc methods[] = classDoc.methods();

        //find expected method name
        String expectedMethodName = null;
        if ("set".equals(prefix) && (currentMethodDoc.parameters().length >= 1 && !currentMethodDoc.parameters()[0].typeName().equals("boolean"))) {
            expectedMethodName = "get" + propName;
        } else if ("get".equals(prefix) && !currentMethodDoc.returnType().typeName().equals("boolean")) {
            expectedMethodName = "set" + propName;
        } else if ("is".equals(prefix)) {
            expectedMethodName = "set" + propName;
        } else {
            expectedMethodName = "is" + propName;
        }

        for (GroovyMethodDoc methodDoc : methods) {
            if (methodDoc.name().equals(expectedMethodName)) {

                //extract the field name
                String fieldName = propName.substring(0, 1).toLowerCase() + propName.substring(1);
                SimpleGroovyFieldDoc currentFieldDoc = new SimpleGroovyFieldDoc(fieldName, classDoc);

                //find the type of the field; if it's a setter, need to get the type of the params
                if (expectedMethodName.startsWith("set") && methodDoc.parameters().length >= 1) {
                    String typeName = methodDoc.parameters()[0].typeName();
                    currentFieldDoc.setType(new SimpleGroovyType(typeName));
                } else {
                    //if it's not setter, get the type info of the return type of the get* method
                    currentFieldDoc.setType(methodDoc.returnType());
                }

                if (methodDoc.isPublic() && currentMethodDoc.isPublic()) {
                    classDoc.addProperty(currentFieldDoc);
                    break;
                }
            }
        }
    }

    @Override
    public void visitProperty(PropertyNode node) {
        String name = node.getName();
        SimpleGroovyFieldDoc field = new SimpleGroovyFieldDoc(name, currentClassDoc);
        field.setType(new SimpleGroovyType(makeType(node.getType())));
        Groovydoc groovydoc = node.getGroovydoc();
        field.setRawCommentText(groovydoc == null ? "" : groovydoc.getContent());
        currentClassDoc.addProperty(field);
        super.visitProperty(node);
    }

    private String makeType(ClassNode node) {
        return node.getName().replaceAll("\\.", "/").replaceAll("\\$", ".");
    }

    @Override
    public void visitField(FieldNode node) {
        String name = node.getName();
        SimpleGroovyFieldDoc field = new SimpleGroovyFieldDoc(name, currentClassDoc);
        field.setType(new SimpleGroovyType(makeType(node.getType())));
        boolean isProp = node.getDeclaringClass().getProperty(name) != null;
        int mods = node.getModifiers();
        if (currentClassDoc.isScript() && (mods & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) == 0) {
            // handle @Field props
            isProp = true;
            currentClassDoc.addProperty(field);
        }
        if (!isProp) {
            if (node.isPublic()) {
                field.setPublic(true);
            }
            if (node.isProtected()) {
                field.setProtected(true);
            }
            if (node.isPrivate()) {
                field.setPrivate(true);
            }
            if (node.isStatic()) {
                field.setStatic(true);
            }
            if (node.isFinal()) {
                field.setFinal(true);
            }
            field.setRawCommentText(node.getGroovydoc().getContent());
            currentClassDoc.add(field);
        }
        super.visitField(node);
    }

    private void setConstructorOrMethodCommon(MethodNode node, SimpleGroovyExecutableMemberDoc methOrCons) {
        methOrCons.setRawCommentText(node.getGroovydoc().getContent());
        if (node.isPublic()) {
            methOrCons.setPublic(true);
        }
        if (node.isAbstract()) {
            methOrCons.setAbstract(true);
        }
        if (node.isProtected()) {
            methOrCons.setProtected(true);
        }
        if (node.isPrivate()) {
            methOrCons.setPrivate(true);
        }
        if (node.isStatic()) {
            methOrCons.setStatic(true);
        }
        if (node.isFinal()) {
            methOrCons.setFinal(true);
        }
        for (Parameter param : node.getParameters()) {
            SimpleGroovyParameter p = new SimpleGroovyParameter(param.getName());
            p.setType(new SimpleGroovyType(makeType(param.getType())));
            methOrCons.add(p);
        }
    }

    public Map<String, GroovyClassDoc> getGroovyClassDocs() {
        return classDocs;
    }
}
