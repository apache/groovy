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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.LinkArgument;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyAnnotationRef;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyConstructorDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyExecutableMemberDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyFieldDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyMethodDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyParameter;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyProgramElementDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        final Map<String, String> aliases = new HashMap<>();
        final List<String> imports = new ArrayList<>();
        for (ImportNode iNode : node.getModule().getImports()) {
            String name = iNode.getClassName();
            imports.add(name);
            if (iNode.getAlias() != null && !iNode.getAlias().isEmpty()) {
                aliases.put(iNode.getAlias(), name.replace('.', '/'));
            }
        }
        String name = node.getNameWithoutPackage();

        if (node instanceof InnerClassNode) {
            name = name.replace('$', '.');
        }
        currentClassDoc = new SimpleGroovyClassDoc(withDefaultImports(imports), aliases, name, links);
        if (node.isEnum()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.ENUM_DEF);
        } else if (node.isAnnotationDefinition()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.ANNOTATION_DEF);
        } else if (isTrait(node)) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.TRAIT_DEF);
        } else if (node.isInterface()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.INTERFACE_DEF);
        }
        if (node.isScript()) {
            currentClassDoc.setScript(true);
        }
        for (ClassNode iface : node.getInterfaces()) {
            currentClassDoc.addInterfaceName(makeType(iface));
        }
        currentClassDoc.setRawCommentText(getDocContent(node.getGroovydoc()));
        currentClassDoc.setNameWithTypeArgs(name + genericTypesAsString(node.getGenericsTypes()));
        if (!node.isInterface() && node.getSuperClass() != null) {
            String superName = makeType(node.getSuperClass());
            currentClassDoc.setSuperClassName(superName);
            String superSimpleName = node.getSuperClass().getNameWithoutPackage();
            if (!classDocs.containsKey(superSimpleName)) {
                SimpleGroovyClassDoc superDoc = new SimpleGroovyClassDoc(imports, superName);
                superDoc.setFullPathName(superName);
            }
        }
        processModifiers(currentClassDoc, node, node.getModifiers());
        processAnnotations(currentClassDoc, node);
        if (Modifier.isAbstract(node.getModifiers())) {
            currentClassDoc.setAbstract(true);
        }
        currentClassDoc.setFullPathName(packagePath + FS + name);
        currentClassDoc.setGroovy(true);
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        super.visitClass(node);
        SimpleGroovyClassDoc parent = currentClassDoc;
        if (currentClassDoc.constructors().length == 0) {
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

    private List<String> withDefaultImports(List<String> imports) {
        imports = imports != null ? imports : new ArrayList<>();
        imports.add(packagePath + "/*");  // everything in this package
        for (String pkg : ResolveVisitor.DEFAULT_IMPORTS) {
            imports.add(pkg.replace('.', '/') + "*");
        }
        return imports;
    }

    private static final Pattern JAVADOC_COMMENT_PATTERN = Pattern.compile("(?s)/\\*\\*(.*?)\\*/");

    private String getDocContent(Groovydoc groovydoc) {
        if (groovydoc == null) return "";
        String result = groovydoc.getContent();
        if (result == null) result = "";
        Matcher m = JAVADOC_COMMENT_PATTERN.matcher(result);
        if (m.find()) {
            result = m.group(1).trim();
        }
        return result;
    }

    private void processAnnotations(SimpleGroovyProgramElementDoc element, AnnotatedNode node) {
        for (AnnotationNode an : node.getAnnotations()) {
            String name = an.getClassNode().getName();
            element.addAnnotationRef(new SimpleGroovyAnnotationRef(name, an.getText()));
        }
    }

    private void processAnnotations(SimpleGroovyParameter param, AnnotatedNode node) {
        for (AnnotationNode an : node.getAnnotations()) {
            String name = an.getClassNode().getName();
            param.addAnnotationRef(new SimpleGroovyAnnotationRef(name, an.getText()));
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
        super.visitMethod(node);
        meth.setTypeParameters(genericTypesAsString(node.getGenericsTypes()));
    }

    private String genericTypesAsString(GenericsType[] genericsTypes) {
        if (genericsTypes == null || genericsTypes.length == 0)
            return "";
        return "<" + DefaultGroovyMethods.join(genericsTypes, ", ") + ">";
    }

    private void processPropertiesFromGetterSetter(SimpleGroovyMethodDoc currentMethodDoc) {
        String methodName = currentMethodDoc.name();
        int len = methodName.length();
        String prefix;
        String propName;
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
        GroovyMethodDoc[] methods = classDoc.methods();

        //find expected method name
        String expectedMethodName;
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
        SimpleGroovyFieldDoc fieldDoc = new SimpleGroovyFieldDoc(name, currentClassDoc);
        fieldDoc.setType(new SimpleGroovyType(makeType(node.getType())));
        int mods = node.getModifiers();
        if (!hasAnno(node.getField(), "PackageScope")) {
            processModifiers(fieldDoc, node.getField(), mods);
            Groovydoc groovydoc = node.getGroovydoc();
            fieldDoc.setRawCommentText(groovydoc == null ? "" : getDocContent(groovydoc));
            currentClassDoc.addProperty(fieldDoc);
        }
        processAnnotations(fieldDoc, node.getField());
        super.visitProperty(node);
    }

    private String makeType(ClassNode node) {
        final ClassNode cn = node.isArray() ? node.getComponentType() : node;
        return cn.getName().replace('.', '/').replace('$', '.')
            + genericTypesAsString(cn.getGenericsTypes())
            + (node.isArray() ? "[]" : "")
            ;
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        if (currentClassDoc.isScript()) {
            if (hasAnno(expression, "Field")) {
                VariableExpression varx = expression.getVariableExpression();
                SimpleGroovyFieldDoc field = new SimpleGroovyFieldDoc(varx.getName(), currentClassDoc);
                field.setType(new SimpleGroovyType(makeType(varx.getType())));
                int mods = varx.getModifiers();
                processModifiers(field, varx, mods);
                boolean isProp = (mods & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) == 0;
                if (isProp) {
                    currentClassDoc.addProperty(field);
                } else {
                    currentClassDoc.add(field);
                }
            }
        }
        super.visitDeclarationExpression(expression);
    }

    private void processModifiers(SimpleGroovyProgramElementDoc element, AnnotatedNode node, int mods) {
        if (Modifier.isStatic(mods)) {
            element.setStatic(true);
        }
        if (hasAnno(node, "PackageScope")) {
            element.setPackagePrivate(true);
        } else {
            if (Modifier.isPublic(mods)) {
                element.setPublic(true);
            }
            if (Modifier.isProtected(mods)) {
                element.setProtected(true);
            }
            if (Modifier.isPrivate(mods)) {
                element.setPrivate(true);
            }
            if (Modifier.isFinal(mods)) {
                element.setFinal(true);
            }
        }
    }

    @Override
    public void visitField(FieldNode node) {
        String name = node.getName();
        SimpleGroovyFieldDoc fieldDoc = new SimpleGroovyFieldDoc(name, currentClassDoc);
        fieldDoc.setType(new SimpleGroovyType(makeType(node.getType())));
        processModifiers(fieldDoc, node, node.getModifiers());
        processAnnotations(fieldDoc, node);
        fieldDoc.setRawCommentText(getDocContent(node.getGroovydoc()));
        currentClassDoc.add(fieldDoc);
        super.visitField(node);
    }

    private void setConstructorOrMethodCommon(MethodNode node, SimpleGroovyExecutableMemberDoc methOrCons) {
        methOrCons.setRawCommentText(getDocContent(node.getGroovydoc()));
        processModifiers(methOrCons, node, node.getModifiers());
        processAnnotations(methOrCons, node);
        if (node.isAbstract()) {
            methOrCons.setAbstract(true);
        }
        for (Parameter param : node.getParameters()) {
            SimpleGroovyParameter p = new SimpleGroovyParameter(param.getName());
            p.setType(new SimpleGroovyType(makeType(param.getType())));
            processAnnotations(p, param);
            methOrCons.add(p);
        }
    }

    private boolean hasAnno(AnnotatedNode node, String annoSuffix) {
        for (AnnotationNode annotationNode : node.getAnnotations()) {
            // check name to cover non/resolved cases
            if (annotationNode.getClassNode().getName().endsWith(annoSuffix)) return true;
        }
        return false;
    }

    public Map<String, GroovyClassDoc> getGroovyClassDocs() {
        return classDocs;
    }
}
