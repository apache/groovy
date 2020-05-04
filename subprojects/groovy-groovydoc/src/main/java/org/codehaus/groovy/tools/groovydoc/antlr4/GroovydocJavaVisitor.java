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
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.LinkArgument;
import org.codehaus.groovy.tools.groovydoc.SimpleGroovyAbstractableElementDoc;
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
        String qual = qualPath.map(value -> value.asString().replace('.', '/') + "/").orElse("");
        String id = n.getName().getIdentifier();
        String name = qual + id;
        imports.add(name);
        aliases.put(id, name);
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
        SimpleGroovyClassDoc parent = visit(n);
        currentClassDoc.setTokenType(SimpleGroovyDoc.ENUM_DEF);
        super.visit(n, arg);
        if (parent != null) {
            currentClassDoc = parent;
        }
    }

    @Override
    public void visit(EnumConstantDeclaration n, Object arg) {
        if (!currentClassDoc.isEnum()) {
            throw new GroovyBugError("Annotation member definition found when not expected");
        }
        String enumConstantName = n.getNameAsString();
        SimpleGroovyFieldDoc enumConstantDoc = new SimpleGroovyFieldDoc(enumConstantName, currentClassDoc);
        enumConstantDoc.setType(new SimpleGroovyType(currentClassDoc.getTypeDescription()));
        enumConstantDoc.setPublic(true);
        currentClassDoc.addEnumConstant(enumConstantDoc);
        processAnnotations(enumConstantDoc, n);
        n.getJavadocComment().ifPresent(javadocComment ->
                enumConstantDoc.setRawCommentText(javadocComment.getContent()));
        super.visit(n, arg);
    }

    @Override
    public void visit(AnnotationDeclaration n, Object arg) {
        SimpleGroovyClassDoc parent = visit(n);
        currentClassDoc.setTokenType(SimpleGroovyDoc.ANNOTATION_DEF);
        super.visit(n, arg);
        if (parent != null) {
            currentClassDoc.setPublic(true);
            currentClassDoc = parent;
        }
    }

    @Override
    public void visit(AnnotationMemberDeclaration n, Object arg) {
        if (!currentClassDoc.isAnnotationType()) {
            throw new GroovyBugError("Annotation member definition found when not expected");
        }
        SimpleGroovyFieldDoc fieldDoc = new SimpleGroovyFieldDoc(n.getNameAsString(), currentClassDoc);
        fieldDoc.setType(makeType(n.getType()));
        setModifiers(n.getModifiers(), fieldDoc);
        fieldDoc.setPublic(true);
        processAnnotations(fieldDoc, n);
        currentClassDoc.add(fieldDoc);
        n.getJavadocComment().ifPresent(javadocComment ->
                fieldDoc.setRawCommentText(javadocComment.getContent()));
        n.getDefaultValue().ifPresent(defValue -> {
            fieldDoc.setRawCommentText(fieldDoc.getRawCommentText() + "\n* @default " + defValue.toString());
            fieldDoc.setConstantValueExpression(defValue.toString());
        });
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        SimpleGroovyClassDoc parent = visit(n);
        if (n.isInterface()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.INTERFACE_DEF);
        } else {
            currentClassDoc.setTokenType(SimpleGroovyDoc.CLASS_DEF);
        }
        n.getExtendedTypes().forEach(et -> {
            if (n.isInterface()) {
                currentClassDoc.addInterfaceName(fullName(et));
            } else {
                currentClassDoc.setSuperClassName(fullName(et));
            }
        });
        currentClassDoc.setNameWithTypeArgs(currentClassDoc.name() + genericTypesAsString(n.getTypeParameters()));
        n.getImplementedTypes().forEach(classOrInterfaceType ->
                currentClassDoc.addInterfaceName(fullName(classOrInterfaceType)));
        super.visit(n, arg);
        if (parent != null) {
            currentClassDoc = parent;
        }
    }

    private String fullName(ClassOrInterfaceType et) {
        StringBuilder name = new StringBuilder();
        et.getScope().ifPresent(sc -> name.append(sc.toString()));
        name.append(et.getNameAsString());
        return name.toString();
    }

    private String genericTypesAsString(NodeList<TypeParameter> typeParameters) {
        if (typeParameters == null || typeParameters.size() == 0)
            return "";
        return "<" + DefaultGroovyMethods.join(typeParameters, ", ") + ">";
    }

    private SimpleGroovyClassDoc visit(TypeDeclaration<?> n) {
        SimpleGroovyClassDoc parent = null;
        List<String> imports = getImports();
        String name = n.getNameAsString();
        if (n.isNestedType()) {
            parent = currentClassDoc;
            name = parent.name() + "$" + name;
        }
        currentClassDoc = new SimpleGroovyClassDoc(imports, aliases, name.replace('$', '.'), links);
        NodeList<Modifier> mods = n.getModifiers();
        if (parent != null) {
            parent.addNested(currentClassDoc);
            if (parent.isInterface()) {
                // an inner interface/class within an interface is public
                mods.add(Modifier.publicModifier());
            }
        }
        setModifiers(mods, currentClassDoc);
        processAnnotations(currentClassDoc, n);
        currentClassDoc.setFullPathName(withSlashes(packagePath + FS + name));
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        n.getJavadocComment().ifPresent(javadocComment ->
                currentClassDoc.setRawCommentText(javadocComment.getContent()));
        return parent;
    }

    private void processAnnotations(SimpleGroovyProgramElementDoc element, NodeWithAnnotations<?> n) {
        for (AnnotationExpr an : n.getAnnotations()) {
            element.addAnnotationRef(new SimpleGroovyAnnotationRef(an.getClass().getName(), an.getNameAsString()));
        }
    }

    private void processAnnotations(SimpleGroovyParameter param, NodeWithAnnotations<?> n) {
        for (AnnotationExpr an : n.getAnnotations()) {
            param.addAnnotationRef(new SimpleGroovyAnnotationRef(an.getClass().getName(), an.getNameAsString()));
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
        return s.replace('.', '/').replace('$', '.');
    }

    @Override
    public void visit(MethodDeclaration m, Object arg) {
        SimpleGroovyMethodDoc meth = new SimpleGroovyMethodDoc(m.getNameAsString(), currentClassDoc);
        meth.setTypeParameters(genericTypesAsString(m.getTypeParameters()));
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

    private void setConstructorOrMethodCommon(CallableDeclaration<? extends CallableDeclaration<?>> n, SimpleGroovyExecutableMemberDoc methOrCons) {
        n.getJavadocComment().ifPresent(javadocComment ->
                methOrCons.setRawCommentText(javadocComment.getContent()));
        NodeList<Modifier> mods = n.getModifiers();
        if (currentClassDoc.isInterface()) {
            mods.add(Modifier.publicModifier());
        }
        setModifiers(mods, methOrCons);
        processAnnotations(methOrCons, n);
        for (Parameter param : n.getParameters()) {
            SimpleGroovyParameter p = new SimpleGroovyParameter(param.getNameAsString());
            processAnnotations(p, param);
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
        processAnnotations(field, f);
        f.getJavadocComment().ifPresent(javadocComment ->
                field.setRawCommentText(javadocComment.getContent()));
        currentClassDoc.add(field);
        super.visit(f, arg);
    }

    public Map<String, GroovyClassDoc> getGroovyClassDocs() {
        return classDocs;
    }

}
