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
package org.codehaus.groovy.tools.groovydoc.antlr4;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.LinkArgument;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyAbstractableElementDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyConstructorDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyExecutableMemberDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyFieldDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyMethodDoc;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyParameter;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GroovydocJavaVisitor extends VoidVisitorAdapter<Object> {
    private final List<LinkArgument> links;
    private SimpleGroovyClassDoc currentClassDoc = null;
    private Map<String, GroovyClassDoc> classDocs = new HashMap<>();
    private String packagePath;
    private final Map<String, String> aliases = new HashMap<>();
    private List<String> imports = new ArrayList<>();
    private static final String FS = "/";

    public GroovydocJavaVisitor(String packagePath, List<LinkArgument> links) {
        this.packagePath = packagePath;
        this.links = links;
    }

    @Override
    public void visit(ImportDeclaration n, Object arg) {
        Optional<Name> qualPath = n.getName().getQualifier();
        String qual = qualPath.map(value -> value.asString().replaceAll("\\.", "/") + "/").orElse("");
        String id = n.getName().getIdentifier();
        String name = qual + id;
        imports.add(name);
        aliases.put(id, name);
        super.visit(n, arg);
    }

    @Override
    public void visit(AnnotationDeclaration n, Object arg) {
        List<String> imports = getImports();
        currentClassDoc = new SimpleGroovyClassDoc(imports, aliases, n.getNameAsString(), links);
        setModifiers(n.getModifiers(), currentClassDoc);
        currentClassDoc.setTokenType(SimpleGroovyDoc.ANNOTATION_DEF);
        currentClassDoc.setFullPathName(withSlashes(packagePath + FS + n.getNameAsString()));
        n.getJavadocComment().ifPresent(javadocComment ->
                currentClassDoc.setRawCommentText(javadocComment.getContent()));
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        super.visit(n, arg);
    }

    private List<String> getImports() {
        List<String> imports = new ArrayList<>(this.imports);
        imports.add(packagePath + "/*");  // everything in this package
        for (String pkg : ResolveVisitor.DEFAULT_IMPORTS) {
            imports.add(pkg.replace('.', '/') + "*");
        }
        return imports;
    }

    @Override
    public void visit(EnumDeclaration n, Object arg) {
        List<String> imports = getImports();
        currentClassDoc = new SimpleGroovyClassDoc(imports, aliases, n.getNameAsString(), links);
        setModifiers(n.getModifiers(), currentClassDoc);
        currentClassDoc.setTokenType(SimpleGroovyDoc.ENUM_DEF);
        currentClassDoc.setFullPathName(withSlashes(packagePath + FS + n.getNameAsString()));
        n.getJavadocComment().ifPresent(javadocComment ->
                currentClassDoc.setRawCommentText(javadocComment.getContent()));
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        List<String> imports = getImports();
        SimpleGroovyClassDoc parent = currentClassDoc;
        String name = n.getNameAsString();
        boolean nested = n.isNestedType();
        if (nested) {
            name = parent.simpleTypeName() + "." + name;
        }
        currentClassDoc = new SimpleGroovyClassDoc(imports, aliases, name, links);
        if (nested) {
            parent.addNested(currentClassDoc);
        }
        setModifiers(n.getModifiers(), currentClassDoc);
        if (n.isInterface()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.INTERFACE_DEF);
        }
        n.getExtendedTypes().forEach(et -> {
            if (n.isInterface()) {
                currentClassDoc.addInterfaceName(et.getNameAsString());
            } else {
                currentClassDoc.setSuperClassName(et.getNameAsString());
            }
        });
        n.getImplementedTypes().forEach(classOrInterfaceType ->
                currentClassDoc.addInterfaceName(classOrInterfaceType.getNameAsString()));
        currentClassDoc.setFullPathName(packagePath + FS + name);
        currentClassDoc.setNameWithTypeArgs(name);
        n.getJavadocComment().ifPresent(javadocComment ->
                currentClassDoc.setRawCommentText(javadocComment.getContent()));
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        super.visit(n, arg);
        if (nested) {
            currentClassDoc = parent;
        }
    }

    private void setModifiers(NodeList<Modifier> modifiers, SimpleGroovyAbstractableElementDoc elementDoc) {
        if (modifiers.contains(Modifier.publicModifier())) {
            elementDoc.setPublic(true);
        }
        if (modifiers.contains(Modifier.staticModifier())) {
            elementDoc.setStatic(true);
        }
        if (modifiers.contains(Modifier.abstractModifier())) {
            elementDoc.setAbstract(true);
        }
        if (modifiers.contains(Modifier.finalModifier())) {
            elementDoc.setFinal(true);
        }
        if (modifiers.contains(Modifier.protectedModifier())) {
            elementDoc.setProtected(true);
        }
        if (modifiers.contains(Modifier.privateModifier())) {
            elementDoc.setPrivate(true);
        }
    }

    private String withSlashes(String s) {
        return s.replaceAll("\\.", "/").replaceAll("\\$", ".");
    }

    @Override
    public void visit(MethodDeclaration m, Object arg) {
        SimpleGroovyMethodDoc meth = new SimpleGroovyMethodDoc(m.getNameAsString(), currentClassDoc);
        meth.setReturnType(makeType(m.getType()));
        setConstructorOrMethodCommon(m, meth);
        currentClassDoc.add(meth);
        super.visit(m, arg);
    }

    private SimpleGroovyType makeType(Type t) {
        return new SimpleGroovyType(withSlashes(t.asString()));
    }

    @Override
    public void visit(ConstructorDeclaration c, Object arg) {
        SimpleGroovyConstructorDoc meth = new SimpleGroovyConstructorDoc(c.getNameAsString(), currentClassDoc);
        setConstructorOrMethodCommon(c, meth);
        currentClassDoc.add(meth);
        super.visit(c, arg);
    }

    private void setConstructorOrMethodCommon(CallableDeclaration<? extends CallableDeclaration> n, SimpleGroovyExecutableMemberDoc methOrCons) {
        n.getJavadocComment().ifPresent(javadocComment ->
                methOrCons.setRawCommentText(javadocComment.getContent()));
        setModifiers(n.getModifiers(), methOrCons);
        for (Parameter param : n.getParameters()) {
            SimpleGroovyParameter p = new SimpleGroovyParameter(param.getNameAsString());
            p.setType(makeType(param.getType()));
            methOrCons.add(p);
        }
    }

    @Override
    public void visit(FieldDeclaration f, Object arg) {
        String name = f.getVariable(0).getNameAsString();
        SimpleGroovyFieldDoc field = new SimpleGroovyFieldDoc(name, currentClassDoc);
        field.setType(makeType(f.getVariable(0).getType()));
        setModifiers(f.getModifiers(), field);
        f.getJavadocComment().ifPresent(javadocComment ->
                field.setRawCommentText(javadocComment.getContent()));
        currentClassDoc.add(field);
        super.visit(f, arg);
    }

    public Map<String, GroovyClassDoc> getGroovyClassDocs() {
        return classDocs;
    }

}
