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
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.GroovydocAnnotationUtils;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class GroovydocJavaVisitor
    extends VoidVisitorAdapter<Object> {
    private final List<LinkArgument> links;
    private SimpleGroovyClassDoc currentClassDoc = null;
    private final Map<String, GroovyClassDoc> classDocs = new LinkedHashMap<>();
    private final String packagePath;
    private final Map<String, String> aliases = new LinkedHashMap<>();
    private final List<String> imports = new ArrayList<>();
    private final Properties properties;
    private static final String FS = "/";

    public GroovydocJavaVisitor(String packagePath, List<LinkArgument> links) {
        this(packagePath, links, new Properties());
    }

    public GroovydocJavaVisitor(String packagePath, List<LinkArgument> links, Properties properties) {
        this.packagePath = packagePath;
        this.links = links;
        this.properties = properties;
    }

    @Override
    public void visit(ImportDeclaration n, Object arg) {
        Optional<Name> qualPath = n.getName().getQualifier();
        String qual = qualPath.map(value -> value.asString().replace('.', '/') + "/").orElse("");
        String id = n.getName().getIdentifier();
        String name = qual + id;
        if (n.isAsterisk()) name +="/*";
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
        applyJavadocComment(n.getJavadocComment(), enumConstantDoc);
        // Per-constant class bodies are anonymous implementation details.
        if (!n.getClassBody().isEmpty()) return;
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
        applyJavadocComment(n.getJavadocComment(), fieldDoc);
        n.getDefaultValue().ifPresent(defValue -> {
            // For Markdown-form comments (no `*` line prefix), the synthesised
            // @default tag goes on a bare line; traditional /** */ form keeps
            // the `* ` prefix for visual parity with existing continuation lines.
            String prefix = fieldDoc.isMarkdown() ? "\n@default " : "\n* @default ";
            fieldDoc.setRawCommentText(fieldDoc.getRawCommentText() + prefix + defValue);
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
        if (!n.isInterface() && currentClassDoc.getSuperClassName() == null && currentClassDoc.superclass() == null) {
            currentClassDoc.setSuperClassName("Object");
        }
        currentClassDoc.setNameWithTypeArgs(currentClassDoc.name() + genericTypesAsString(n.getTypeParameters()));
        n.getImplementedTypes().forEach(classOrInterfaceType ->
                currentClassDoc.addInterfaceName(fullName(classOrInterfaceType)));
        super.visit(n, arg);
        if (parent != null) {
            currentClassDoc = parent;
        }
    }

    @Override
    public void visit(final RecordDeclaration n, final Object arg) {
        SimpleGroovyClassDoc parent = visit(n);
        if (n.isRecordDeclaration()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.RECORD_DEF);
        }
        super.visit(n,arg);
        if (parent != null) {
            currentClassDoc = parent;
        }
    }

    @Override
    public void visit(final CompactConstructorDeclaration c, Object arg) {
        SimpleGroovyConstructorDoc meth = new SimpleGroovyConstructorDoc(c.getNameAsString(), currentClassDoc);
        setCompactConstructor(c, meth);
        currentClassDoc.add(meth);
        super.visit(c, arg);
    }

    private String fullName(ClassOrInterfaceType et) {
        StringBuilder name = new StringBuilder();
        et.getScope().ifPresent(sc -> name.append(sc.toString()));
        name.append(et.getNameAsString());
        return name.toString();
    }

    private String genericTypesAsString(NodeList<TypeParameter> typeParameters) {
        if (typeParameters == null || typeParameters.isEmpty())
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
        applyJavadocComment(n.getJavadocComment(), currentClassDoc);
        return parent;
    }

    private void processAnnotations(SimpleGroovyProgramElementDoc element, NodeWithAnnotations<?> n) {
        for (AnnotationExpr an : n.getAnnotations()) {
            String name = an.getNameAsString();
            if (!shouldDocument(name)) continue;
            element.addAnnotationRef(new SimpleGroovyAnnotationRef(name, getAnnotationText(an)));
        }
    }

    private void processAnnotations(SimpleGroovyParameter param, NodeWithAnnotations<?> n) {
        for (AnnotationExpr an : n.getAnnotations()) {
            String name = an.getNameAsString();
            if (!shouldDocument(name)) continue;
            param.addAnnotationRef(new SimpleGroovyAnnotationRef(name, getAnnotationText(an)));
        }
    }

    // GROOVY-4634: emit an annotation reference only when the annotation type is
    // itself marked {@code @Documented}, matching Javadoc's behavior. When the
    // annotation type cannot be resolved on the current classpath, default to
    // including it so that groovydoc does not silently drop user annotations.
    private boolean shouldDocument(String name) {
        String fqn = resolveAnnotationFqn(name);
        if (fqn == null) return true; // unresolved — show
        return GroovydocAnnotationUtils.shouldDocument(fqn);
    }

    private String resolveAnnotationFqn(String name) {
        // already fully qualified
        if (name.contains(".")) {
            if (tryLoad(name) != null) return name;
            // nested-name like "CommandLine.Parameters" — resolve outer via aliases
            int dot = name.indexOf('.');
            String outer = name.substring(0, dot);
            String rest = name.substring(dot);
            String outerFqn = aliases.get(outer);
            if (outerFqn != null) {
                String candidate = outerFqn.replace('/', '.') + rest.replace('.', '$');
                if (tryLoad(candidate) != null) return candidate;
            }
            return null;
        }
        String importedFqn = aliases.get(name);
        if (importedFqn != null) {
            String candidate = importedFqn.replace('/', '.');
            if (tryLoad(candidate) != null) return candidate;
        }
        String javaLang = "java.lang." + name;
        if (tryLoad(javaLang) != null) return javaLang;
        return null;
    }

    private static Class<?> tryLoad(String fqn) {
        try {
            return Class.forName(fqn);
        } catch (Throwable t) {
            return null;
        }
    }

    private String getAnnotationText(final AnnotationExpr an) {
        if (an != null && an.getTokenRange().isPresent()) {
            return an.getTokenRange().get().toString();
        }
        return "";
    }

    // GROOVY-9572: hide members annotated with groovy.transform.@Internal (per GEP-17)
    // or deemed internal by name convention (contains '$'), unless the user opts
    // in with -showInternal / showInternal=true. Mirrors the policy of
    // {@link org.apache.groovy.ast.tools.AnnotatedNodeUtils#deemedInternal} on
    // the Groovy-AST side, expressed against JavaParser AST nodes here.
    private boolean isInternal(NodeWithAnnotations<?> n) {
        if ("true".equals(properties.getProperty("showInternal", "false"))) return false;
        for (AnnotationExpr an : n.getAnnotations()) {
            String name = an.getNameAsString();
            if ("Internal".equals(name) || name.endsWith(".Internal")) return true;
        }
        if (n instanceof FieldDeclaration fd) {
            // Java allows multiple variables per field declaration (`int a, b$;`).
            // Treat the whole declaration as internal if *any* variable name
            // contains the synthetic-name marker; picking only the first would
            // mis-classify `int a, b$;` as non-internal.
            for (int k = 0; k < fd.getVariables().size(); k++) {
                if (fd.getVariable(k).getNameAsString().contains("$")) return true;
            }
        }
        if (n instanceof MethodDeclaration md && md.getNameAsString().contains("$")) return true;
        if (n instanceof ConstructorDeclaration cd && cd.getNameAsString().contains("$")) return true;
        return false;
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
        if (isInternal(m)) return;
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
        if (isInternal(c)) return;
        SimpleGroovyConstructorDoc meth = new SimpleGroovyConstructorDoc(c.getNameAsString(), currentClassDoc);
        setConstructorOrMethodCommon(c, meth);
        currentClassDoc.add(meth);
        super.visit(c, arg);
    }

    private void setConstructorOrMethodCommon(CallableDeclaration<? extends CallableDeclaration<?>> n, SimpleGroovyExecutableMemberDoc methOrCons) {
        applyJavadocComment(n.getJavadocComment(), methOrCons);
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

    private void setCompactConstructor(CompactConstructorDeclaration n, SimpleGroovyExecutableMemberDoc methOrCons) {
        n.getComment().ifPresent(javadocComment ->
                                            methOrCons.setRawCommentText(javadocComment.getContent()));
        NodeList<Modifier> mods = n.getModifiers();
        if (currentClassDoc.isInterface()) {
            mods.add(Modifier.publicModifier());
        }
        setModifiers(mods, methOrCons);
        processAnnotations(methOrCons, n);
        for (TypeParameter param : n.getTypeParameters()) {
            SimpleGroovyParameter p = new SimpleGroovyParameter(param.getNameAsString());
            processAnnotations(p, param);
            p.setType(makeType(param));
            methOrCons.add(p);
        }
    }

    @Override
    public void visit(FieldDeclaration f, Object arg) {
        if (isInternal(f)) return;
        String name = f.getVariable(0).getNameAsString();
        SimpleGroovyFieldDoc field = new SimpleGroovyFieldDoc(name, currentClassDoc);
        field.setType(makeType(f.getVariable(0).getType()));
        setModifiers(f.getModifiers(), field);
        processAnnotations(field, f);
        applyJavadocComment(f.getJavadocComment(), field);
        currentClassDoc.add(field);
        super.visit(f, arg);
    }

    @Override
    public void visit(ObjectCreationExpr n, Object arg) {
        // Anonymous class bodies must not contribute members to the enclosing type doc.
        if (n.getAnonymousClassBody().isPresent()) return;
        super.visit(n, arg);
    }

    public Map<String, GroovyClassDoc> getGroovyClassDocs() {
        return classDocs;
    }

    /**
     * Apply the content of a JavaParser-recognised Javadoc comment to the
     * given target. Handles both traditional {@code /** ... *}{@code /} form and
     * JEP 467 Markdown form (runs of {@code ///} lines). For Markdown, the
     * {@code ///} line prefixes are stripped and the target is flagged via
     * {@link SimpleGroovyDoc#setMarkdown(boolean)} so downstream rendering
     * routes through CommonMark.
     */
    private static void applyJavadocComment(Optional<JavadocComment> optComment, SimpleGroovyDoc target) {
        if (!optComment.isPresent()) return;
        String content = optComment.get().getContent();
        String markdownBody = tryExtractMarkdownBody(content);
        if (markdownBody != null) {
            target.setRawCommentText(markdownBody);
            target.setMarkdown(true);
        } else {
            target.setRawCommentText(content);
        }
    }

    /**
     * If {@code content} is a JEP 467 Markdown Javadoc body (every non-blank
     * line starts with {@code ///}), return the body with those line prefixes
     * stripped (one optional trailing space after {@code ///} is also
     * consumed). Returns {@code null} if the content isn't Markdown form.
     */
    private static String tryExtractMarkdownBody(String content) {
        if (content == null || content.isEmpty()) return null;
        String[] lines = content.split("\n", -1);
        boolean anyMarkdownLine = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (!trimmed.startsWith("///")) return null;
            anyMarkdownLine = true;
        }
        if (!anyMarkdownLine) return null;
        StringBuilder body = new StringBuilder();
        for (int k = 0; k < lines.length; k++) {
            if (k > 0) body.append('\n');
            String trimmed = lines[k].trim();
            if (trimmed.isEmpty()) continue;
            String rest = trimmed.substring(3);
            if (rest.startsWith(" ")) rest = rest.substring(1);
            body.append(rest);
        }
        return body.toString();
    }

}
