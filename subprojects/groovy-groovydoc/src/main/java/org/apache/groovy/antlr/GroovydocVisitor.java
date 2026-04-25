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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyFieldDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.runtime.ArrayGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.GroovydocAnnotationUtils;
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

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.deemedInternal;
import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;
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
    private Map<String, GroovyClassDoc> classDocs = new LinkedHashMap<>();
    private final Properties properties;
    private static final String FS = "/";
    // GROOVY-11542 stage 1: source cached lazily so per-member Markdown-run
    // scans don't re-read the file per declaration.
    private String cachedSource;
    private String[] cachedSourceLines;

    public GroovydocVisitor(final SourceUnit unit, String packagePath, List<LinkArgument> links) {
        this(unit, packagePath, links, new Properties());
    }

    public GroovydocVisitor(final SourceUnit unit, String packagePath, List<LinkArgument> links, Properties properties) {
        this.unit = unit;
        this.packagePath = packagePath;
        this.links = links;
        this.properties = properties;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public void visitClass(ClassNode node) {
        // GROOVY-10162: anonymous inner classes (e.g. those generated for each
        // enum constant with a body when the enum has an abstract method)
        // aren't user-visible types and shouldn't produce separate HTML pages
        // like `Foo.1.html` / `Foo.2.html`.
        if (node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous()) return;
        final Map<String, String> aliases = new LinkedHashMap<>();
        final List<String> imports = new ArrayList<>();
        for (ImportNode iNode : node.getModule().getImports()) {
            String name = iNode.getClassName();
            imports.add(name.replace('.', '/'));
            if (iNode.getAlias() != null && !iNode.getAlias().isEmpty()) {
                aliases.put(iNode.getAlias(), name.replace('.', '/'));
            }
        }
        for (ImportNode iNode : node.getModule().getStarImports()) {
            String name = iNode.getPackageName()+"*";
            imports.add(name.replace('.', '/'));
        }
        String name = node.getNameWithoutPackage();

        if (node instanceof InnerClassNode) {
            name = name.replace('$', '.');
        }
        currentClassDoc = new SimpleGroovyClassDoc(withDefaultImports(imports), aliases, name, links);
        if (node.isEnum()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.ENUM_DEF);
        } else if (node.isRecord()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.RECORD_DEF);
        } else if (node.isAnnotationDefinition()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.ANNOTATION_DEF);
        } else if (isTrait(node)) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.TRAIT_DEF);
        } else if (node.isInterface()) {
            currentClassDoc.setTokenType(SimpleGroovyDoc.INTERFACE_DEF);
        }
        if (node.isScript()) {
            if ("false".equals(properties.getProperty("processScripts", "true"))) return;
            currentClassDoc.setScript(true);
        }
        for (ClassNode iface : node.getInterfaces()) {
            currentClassDoc.addInterfaceName(makeType(iface));
        }
        String rawDoc = getDocContent(node.getGroovydoc());
        boolean markdownFromSource = false;
        if (rawDoc.isEmpty() && node.isScript()) {
            // GROOVY-8877: the Groovy parser does not attach a leading /** */
            // comment to the synthetic script class. Fall back to scanning the
            // source for one. The helper applies a peek-ahead rule and (for
            // the annotation case) a cross-check against existing member docs
            // so we neither drop legitimate script-level docs (e.g. before
            // `@BaseScript` where the declaration gets transformed away) nor
            // duplicate member docs that the parser already attached.
            rawDoc = extractLeadingScriptDocContent(node);
            // GROOVY-11542: Markdown sibling of the script-level lift.
            if (rawDoc.isEmpty()) {
                rawDoc = extractLeadingScriptMarkdownDocContent(node);
                if (!rawDoc.isEmpty()) markdownFromSource = true;
            }
        }
        if (rawDoc.isEmpty() && node.getLineNumber() > 0) {
            // GROOVY-11542 stage 1: try Markdown doc comment (/// run)
            // immediately preceding the declaration.
            String md = extractMarkdownDocContent(node.getLineNumber());
            if (!md.isEmpty()) {
                rawDoc = md;
                markdownFromSource = true;
            }
        }
        if (markdownFromSource) currentClassDoc.setMarkdown(true);
        currentClassDoc.setRawCommentText(rawDoc);
        currentClassDoc.setNameWithTypeArgs(name + genericTypesAsString(node.getGenericsTypes()));
        if (!node.isInterface() && !node.isEnum() && node.getSuperClass() != null) {
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
        if (currentClassDoc.isClass() && currentClassDoc.constructors().length == 0) {
            // add default no-arg constructor, but not for interfaces, traits, enums, or annotation definitions
            SimpleGroovyConstructorDoc cons = new SimpleGroovyConstructorDoc(name, currentClassDoc);
            cons.setPublic(true);
            currentClassDoc.add(cons);
        }
        Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
        while (innerClasses.hasNext()) {
            InnerClassNode inner = innerClasses.next();
            if (inner.isAnonymous()) continue; // GROOVY-10162
            visitClass(inner);
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

    private String getDocContent(Groovydoc groovydoc) {
        if (groovydoc == null) return "";
        String result = groovydoc.getContent();
        if (result == null) return "";
        // The parser supplies the raw `/** ... */` token; strip the delimiters
        // directly instead of a regex match. Equivalent to the former
        // (?s)/\*\*(.*?)\*/ pattern but avoids a spurious CodeQL polynomial-
        // regex alert and runs in linear time unambiguously.
        if (result.length() >= 5 && result.startsWith("/**") && result.endsWith("*/")) {
            return result.substring(3, result.length() - 2).trim();
        }
        return result.trim();
    }

    /**
     * GROOVY-8877: extract the first Javadoc-style comment block
     * ({@code /** ... *}{@code /}) from the top of the source file, skipping
     * line comments, plain block comments, whitespace, {@code package}
     * declarations, and {@code import} declarations.
     *
     * <p>Lifting policy, by what follows the candidate comment:
     * <ul>
     *   <li><b>package / import / another comment / end of file</b> — lift.
     *       These are unambiguous; the comment cannot belong to a following
     *       member because there isn't one.</li>
     *   <li><b>an annotation ({@code &#64;Xxx})</b> — lift only if no member
     *       of the script class already owns the same comment content. This
     *       covers the {@code @BaseScript} / {@code @Grab} pattern where the
     *       annotated declaration is consumed by an AST transform and no
     *       member survives to carry the doc, while still avoiding duplication
     *       for cases like {@code @Override void foo()} where the parser
     *       attached the comment to a real method.</li>
     *   <li><b>anything else</b> (e.g. {@code def x = 42} which could be a
     *       local variable or a field/property, or a bare declaration) —
     *       don't lift. Groovy's script form makes it unreliable to tell
     *       these apart without full parsing, so we err toward preserving
     *       what the parser decided.</li>
     * </ul>
     *
     * <p>Script authors who want a script-level doc should follow the
     * convention of separating it with a package/import/comment or putting
     * another Javadoc comment before the next member.
     */
    private String extractLeadingScriptDocContent(ClassNode scriptNode) {
        String src;
        try (Reader r = unit.getSource().getReader()) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
            src = sb.toString();
        } catch (IOException e) {
            return "";
        }
        int i = 0, n = src.length();
        while (i < n) {
            char c = src.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (c == '/' && i + 1 < n) {
                char next = src.charAt(i + 1);
                if (next == '/') {
                    int eol = src.indexOf('\n', i);
                    i = eol < 0 ? n : eol + 1;
                    continue;
                }
                if (next == '*') {
                    boolean isJavadoc = i + 2 < n && src.charAt(i + 2) == '*'
                            && (i + 3 >= n || src.charAt(i + 3) != '/');
                    int close = src.indexOf("*/", i + 2);
                    if (close < 0) return "";
                    if (isJavadoc) {
                        // full = `/** ... */`; `close` is the first `*/` from
                        // position i+2 so the delimiters bound exactly. Strip
                        // them without a regex match.
                        String content = src.substring(i + 3, close).trim();
                        LiftDecision decision = decideLift(src, close + 2);
                        return switch (decision) {
                            case LIFT -> content;
                            case SKIP -> "";
                            case CHECK_CLAIM -> isClaimedByMember(scriptNode, content) ? "" : content;
                        };
                    }
                    i = close + 2;
                    continue;
                }
            }
            // Skip `package X` / `import X` declarations by advancing to end
            // of line; any other construct means there's no leading script doc.
            if (src.startsWith("package", i) || src.startsWith("import", i)) {
                int eol = src.indexOf('\n', i);
                i = eol < 0 ? n : eol + 1;
                continue;
            }
            return "";
        }
        return "";
    }

    /**
     * GROOVY-11542 + GROOVY-8877: script-level Markdown lift. Scan the source
     * from line 1 looking for the first contiguous run of {@code ///} lines
     * that satisfies the same lift rules used for traditional Javadoc comments
     * in {@link #extractLeadingScriptDocContent}: the run must be followed by
     * a package/import/member declaration (or nothing), and must not be
     * claimed by a member that the parser already attached doc to.
     */
    private String extractLeadingScriptMarkdownDocContent(ClassNode scriptNode) {
        String[] lines = sourceLines();
        if (lines == null) return "";
        int i = 0;
        // Skip blank lines and block comments before the candidate run.
        while (i < lines.length) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) { i++; continue; }
            if (trimmed.startsWith("/*") && !trimmed.startsWith("/**")) {
                // Skip multi-line /* ... */ header comments (e.g. ASF licences).
                int closeLine = i;
                while (closeLine < lines.length && !lines[closeLine].contains("*/")) closeLine++;
                i = closeLine + 1;
                continue;
            }
            // If we hit anything that isn't a /// line, there's no script-level
            // Markdown doc here; don't consume single-line // comments either.
            if (!trimmed.startsWith("///")) return "";
            break;
        }
        if (i >= lines.length) return "";
        int start = i;
        while (i < lines.length && lines[i].trim().startsWith("///")) i++;
        int end = i - 1;
        // Peek ahead: require the next non-blank thing to be package/import
        // or nothing. `@Foo` triggers the claim-check as for /** */.
        int j = i;
        while (j < lines.length && lines[j].trim().isEmpty()) j++;
        boolean isClaim = false;
        if (j < lines.length) {
            String next = lines[j].trim();
            if (next.startsWith("package ") || next.startsWith("import ")) {
                // lift
            } else if (next.startsWith("//") || next.startsWith("/*")) {
                // lift
            } else if (next.startsWith("@")) {
                isClaim = true;
            } else {
                return "";
            }
        }
        StringBuilder body = new StringBuilder();
        for (int k = start; k <= end; k++) {
            String trimmed = lines[k].trim();
            String rest = trimmed.substring(3);
            if (rest.startsWith(" ")) rest = rest.substring(1);
            if (body.length() > 0) body.append('\n');
            body.append(rest);
        }
        String content = body.toString();
        if (isClaim && isClaimedByMember(scriptNode, content)) return "";
        return content;
    }

    /**
     * GROOVY-11542 stage 1: scan the source for a contiguous run of {@code ///}
     * Markdown doc-comment lines immediately preceding the given declaration
     * line. Returns the joined body (with {@code ///} and one optional leading
     * space stripped from each line) or {@code ""} if no Markdown run is found.
     *
     * <p>Annotation-only lines and blank lines between the comment and the
     * declaration are tolerated, matching JEP 467's "comment attaches to the
     * declaration that immediately follows, allowing annotations in between"
     * rule.
     */
    private String extractMarkdownDocContent(int declLineNumber) {
        if (declLineNumber <= 1) return "";
        String[] lines = sourceLines();
        if (lines == null) return "";
        // 1-based declLineNumber → 0-based index of line immediately above.
        int i = declLineNumber - 2;
        // Skip blank lines and lines that are only annotations.
        while (i >= 0) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) { i--; continue; }
            if (trimmed.startsWith("@") && !trimmed.startsWith("///")) { i--; continue; }
            break;
        }
        if (i < 0) return "";
        // Walk upward collecting contiguous /// lines. Stop on anything else.
        int end = i;
        while (i >= 0 && lines[i].trim().startsWith("///")) i--;
        int start = i + 1;
        if (start > end) return "";
        StringBuilder body = new StringBuilder();
        for (int k = start; k <= end; k++) {
            String trimmed = lines[k].trim();
            // Strip the leading "///"; tolerate one optional space after.
            String rest = trimmed.substring(3);
            if (rest.startsWith(" ")) rest = rest.substring(1);
            if (body.length() > 0) body.append('\n');
            body.append(rest);
        }
        return body.toString();
    }

    private String[] sourceLines() {
        if (cachedSourceLines != null) return cachedSourceLines;
        if (cachedSource == null) {
            try (Reader r = unit.getSource().getReader()) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[8192];
                int n;
                while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
                cachedSource = sb.toString();
            } catch (IOException e) {
                cachedSource = "";
            }
        }
        cachedSourceLines = cachedSource.split("\n", -1);
        return cachedSourceLines;
    }

    private enum LiftDecision { LIFT, SKIP, CHECK_CLAIM }

    /**
     * Classify what follows the candidate leading Javadoc comment and return
     * the matching lift decision. See {@link #extractLeadingScriptDocContent}
     * for the rationale.
     */
    private static LiftDecision decideLift(String src, int from) {
        int i = from, n = src.length();
        while (i < n && Character.isWhitespace(src.charAt(i))) i++;
        if (i >= n) return LiftDecision.LIFT;
        char c = src.charAt(i);
        if (c == '/' && i + 1 < n) {
            char next = src.charAt(i + 1);
            if (next == '/' || next == '*') return LiftDecision.LIFT;
        }
        if (src.startsWith("package", i) || src.startsWith("import", i)) return LiftDecision.LIFT;
        if (c == '@') return LiftDecision.CHECK_CLAIM;
        return LiftDecision.SKIP;
    }

    /**
     * Returns true if any method, field, or property of the script class
     * already owns a Groovydoc whose stripped content matches the candidate.
     * Used to resolve the {@code @Xxx} peek-ahead case — if a real member
     * claims the comment, don't lift it to script-level.
     */
    private boolean isClaimedByMember(ClassNode scriptNode, String candidate) {
        if (candidate.isEmpty()) return false;
        for (MethodNode m : scriptNode.getMethods()) {
            if (candidate.equals(getDocContent(m.getGroovydoc()))) return true;
        }
        for (FieldNode f : scriptNode.getFields()) {
            if (candidate.equals(getDocContent(f.getGroovydoc()))) return true;
        }
        for (PropertyNode p : scriptNode.getProperties()) {
            if (candidate.equals(getDocContent(p.getGroovydoc()))) return true;
        }
        return false;
    }

    private void processAnnotations(SimpleGroovyProgramElementDoc element, AnnotatedNode node) {
        for (AnnotationNode an : node.getAnnotations()) {
            String name = an.getClassNode().getName();
            if (!GroovydocAnnotationUtils.shouldDocument(name)) continue;
            element.addAnnotationRef(new SimpleGroovyAnnotationRef(name, an.getText()));
        }
    }

    private void processAnnotations(SimpleGroovyParameter param, AnnotatedNode node) {
        for (AnnotationNode an : node.getAnnotations()) {
            String name = an.getClassNode().getName();
            if (!GroovydocAnnotationUtils.shouldDocument(name)) continue;
            param.addAnnotationRef(new SimpleGroovyAnnotationRef(name, an.getText()));
        }
    }

    // GROOVY-9572: hide members annotated with groovy.transform.@Internal (per GEP-17)
    // or deemed internal by name convention (contains '$'), unless the user opts
    // in with -showInternal / showInternal=true.
    private boolean isInternal(AnnotatedNode node) {
        if ("true".equals(properties.getProperty("showInternal", "false"))) return false;
        return deemedInternal(node);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        if (node.isSynthetic()) return;
        if (isInternal(node)) return;
        SimpleGroovyConstructorDoc cons = new SimpleGroovyConstructorDoc(currentClassDoc.simpleTypeName(), currentClassDoc);
        setConstructorOrMethodCommon(node, cons);
        currentClassDoc.add(cons);
        super.visitConstructor(node);
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (currentClassDoc.isEnum() && "$INIT".equals(node.getName()))
            return;
        if (node.isSynthetic()) return;
        if (isInternal(node)) return;
        if ("false".equals(properties.getProperty("includeMainForScripts", "true"))
                && currentClassDoc.isScript() && "main".equals(node.getName()) && node.isStatic() && node.getParameters().length == 1)
            return;

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
        return "<" + ArrayGroovyMethods.join(genericsTypes, ", ") + ">";
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

        for (GroovyFieldDoc field : currentClassDoc.properties()) {
            if (propName.equals(field.name())) return;
        }
        SimpleGroovyClassDoc classDoc = currentClassDoc;
        // TODO: not sure why but groovy.ui.view.BasicContentPane#buildOutputArea classDoc is null
        if (classDoc == null) {
            return;
        }
        GroovyMethodDoc[] methods = classDoc.methods();

        //find expected method name
        String expectedMethodName;
        if ("set".equals(prefix) && (currentMethodDoc.parameters().length >= 1 && !"boolean".equals(currentMethodDoc.parameters()[0].typeName()))) {
            expectedMethodName = "get" + propName;
        } else if ("get".equals(prefix) && !"boolean".equals(currentMethodDoc.returnType().typeName())) {
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
        if (isInternal(node.getField())) return;
        String name = node.getName();
        SimpleGroovyFieldDoc fieldDoc = new SimpleGroovyFieldDoc(name, currentClassDoc);
        fieldDoc.setType(new SimpleGroovyType(makeType(node.getType())));
        int mods = node.getModifiers();
        if (!hasAnno(node.getField(), "PackageScope")) {
            processModifiers(fieldDoc, node.getField(), mods);
            Groovydoc groovydoc = node.getGroovydoc();
            String propDoc = groovydoc == null ? "" : getDocContent(groovydoc);
            if (propDoc.isEmpty() && node.getLineNumber() > 0) {
                String md = extractMarkdownDocContent(node.getLineNumber());
                if (!md.isEmpty()) { propDoc = md; fieldDoc.setMarkdown(true); }
            }
            fieldDoc.setRawCommentText(propDoc);
            currentClassDoc.addProperty(fieldDoc);
        }
        processAnnotations(fieldDoc, node.getField());
        super.visitProperty(node);
    }

    private String makeType(ClassNode node) {
        final ClassNode cn = node.isArray() ? node.getComponentType() : node;
        return cn.getName().replace('.', '/').replace('$', '.')
            + genericTypesAsString(cn.getGenericsTypes())
            + (node.isArray() ? "[]" : "");
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
            } else if (Modifier.isProtected(mods)) {
                element.setProtected(true);
            } else if (Modifier.isPrivate(mods)) {
                element.setPrivate(true);
            } else {
                element.setPackagePrivate(true);
            }
        }
        if (Modifier.isFinal(mods)) {
            element.setFinal(true);
        }
    }

    @Override
    public void visitField(FieldNode node) {
        if (node.isSynthetic()) return;
        if (isInternal(node)) return;
        String name = node.getName();
        SimpleGroovyFieldDoc fieldDoc = new SimpleGroovyFieldDoc(name, currentClassDoc);
        fieldDoc.setType(new SimpleGroovyType(makeType(node.getType())));
        processModifiers(fieldDoc, node, node.getModifiers());
        processAnnotations(fieldDoc, node);
        String fieldDoc0 = getDocContent(node.getGroovydoc());
        if (fieldDoc0.isEmpty() && node.getLineNumber() > 0) {
            String md = extractMarkdownDocContent(node.getLineNumber());
            if (!md.isEmpty()) { fieldDoc0 = md; fieldDoc.setMarkdown(true); }
        }
        fieldDoc.setRawCommentText(fieldDoc0);
        // GROOVY-6016: record the source form of compile-time-constant
        // initializers so {@value #FIELD} can resolve them at render time.
        // Leverage ExpressionUtils.transformInlineConstants (same utility used
        // by Verifier, JavaStubGenerator, AnnotationVisitor, etc.) to fold
        // simple constant expressions like `40 + 2`, `"a" + "b"`, references
        // to other static-final constants, and casts — not just bare literals.
        // Javadoc's convention is to re-quote strings/chars;
        // ConstantExpression.getText() returns the raw value without quotes.
        Expression init = node.getInitialExpression();
        if (init != null) {
            // Typed variant does numeric arithmetic folding but needs the
            // target type to equal ClassHelper.STRING_TYPE for string concat,
            // which isn't always the case at CONVERSION phase. Type-less
            // variant handles string concat via isStringType on operand types.
            // Try both; take whichever yields a ConstantExpression.
            Expression folded = transformInlineConstants(init, node.getType());
            if (!(folded instanceof ConstantExpression)) {
                folded = transformInlineConstants(init);
            }
            if (folded instanceof ConstantExpression) {
                Object value = ((ConstantExpression) folded).getValue();
                fieldDoc.setConstantValueExpression(formatConstantValue(value));
            }
        }
        if (node.isEnum()) {
            currentClassDoc.addEnumConstant(fieldDoc);
        } else {
            currentClassDoc.add(fieldDoc);
        }
        super.visitField(node);
    }

    private void setConstructorOrMethodCommon(MethodNode node, SimpleGroovyExecutableMemberDoc methOrCons) {
        String raw = getDocContent(node.getGroovydoc());
        if (raw.isEmpty() && node.getLineNumber() > 0) {
            String md = extractMarkdownDocContent(node.getLineNumber());
            if (!md.isEmpty()) { raw = md; methOrCons.setMarkdown(true); }
        }
        methOrCons.setRawCommentText(raw);
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

    /**
     * Format a compile-time constant value for {@code {@value}} rendering.
     * Strings are wrapped in double quotes and characters in single quotes,
     * matching Javadoc's behaviour. Null, numeric, and boolean values are
     * emitted via {@code String.valueOf}.
     */
    private static String formatConstantValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + value + "\"";
        if (value instanceof Character) return "'" + value + "'";
        return String.valueOf(value);
    }
}
