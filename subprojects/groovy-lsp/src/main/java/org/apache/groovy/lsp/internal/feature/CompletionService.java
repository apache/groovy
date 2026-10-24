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
package org.apache.groovy.lsp.internal.feature;

import groovy.lang.groovydoc.Groovydoc;
import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.CompilerSettings;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.GroovyKeywords;
import org.apache.groovy.lsp.internal.util.ReflectionParameterNames;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import static org.objectweb.asm.Opcodes.ACC_VARARGS;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * Keyword, snippet, member, workspace-type and GDK completions.
 * Unimported workspace types carry {@code additionalTextEdits}.
 */
public final class CompletionService {

    static final int MAX_ITEMS = 80;
    /**
     * Member completion of a resolved receiver is a finite set: the type's
     * own members plus the extension methods that apply to it. The smaller
     * cap is for open searches, such as the whole GDK catalog.
     */
    static final int MAX_MEMBER_ITEMS = 1024;
    private static final String OBJECT_NAME = "Object";

    /**
     * Completes at {@code position} in {@code document}.
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return a completion list (incomplete when truncated)
     */
    public CompletionList complete(final TextDocument document, final CompilationSnapshot snapshot,
                                   final Position position, final PositionEncoding encoding) {
        return complete(document, snapshot, position, encoding, null, null);
    }

    /**
     * Completes at {@code position}. {@code parentLoader} and {@code hostConfiguration}
     * are used only when the buffer did not parse and the caret is a member
     * access: the broken line is rewritten so the declarations before it can
     * still be typed.
     *
     * @param parentLoader compile parent loader, or {@code null}
     * @param hostConfiguration console or groovysh configuration, or {@code null}
     * @return a completion list (incomplete when truncated)
     */
    public CompletionList complete(final TextDocument document, final CompilationSnapshot snapshot,
                                   final Position position, final PositionEncoding encoding,
                                   final ClassLoader parentLoader, final CompilerConfiguration hostConfiguration) {
        return complete(document, snapshot, position, encoding, parentLoader, hostConfiguration, null);
    }

    /**
     * Completes at {@code position}. {@code settings} controls the repair
     * compile used when the buffer did not parse. {@code null} uses
     * {@link CompilerSettings#defaults()}, which does not run {@code @Grab}
     * or {@code @ASTTest}.
     *
     * @param settings session settings, or {@code null}
     * @return a completion list (incomplete when truncated)
     */
    public CompletionList complete(final TextDocument document, final CompilationSnapshot snapshot,
                                   final Position position, final PositionEncoding encoding,
                                   final ClassLoader parentLoader, final CompilerConfiguration hostConfiguration,
                                   final CompilerSettings settings) {
        List<CompletionItem> items = new ArrayList<>();
        String prefix = prefixAt(document, position, encoding);
        boolean memberAccess = isMemberAccess(document, position, encoding);

        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        ModuleNode module = compiled == null ? null : compiled.getModule();
        if (module == null && memberAccess && compiled != null) {
            module = MemberRepair.repairedModule(document, position, parentLoader, hostConfiguration, settings);
        }

        if (!memberAccess) {
            addMatching(items, GroovyKeywords.KEYWORDS, prefix, CompletionItemKind.Keyword);
            addSnippets(items, prefix);
        }

        if (module != null) {
            addFromAst(items, document, compiled, snapshot, position, encoding, module);
        } else if (memberAccess && prefix.length() >= 2) {
            ExtensionCatalog.addExtensionMethods(items, prefix, null, false);
        }

        if (memberAccess) {
            items.sort(Comparator.comparing(CompletionItem::getSortText, Comparator.nullsLast(Comparator.naturalOrder())));
        }
        // A resolved receiver already listed only its own members and the
        // extension methods that apply to it. Cutting that list at the open
        // search cap drops extension methods, which then reappear once a
        // longer prefix makes the Java members few enough to fit.
        int limit = memberAccess && module != null ? MAX_MEMBER_ITEMS : MAX_ITEMS;
        boolean incomplete = items.size() > limit;
        if (incomplete) {
            items = new ArrayList<>(items.subList(0, limit));
        }
        return new CompletionList(incomplete, items);
    }

    /**
     * Fills documentation from the compiled snapshot using {@code item.data}.
     *
     * @param item unresolved item
     * @param snapshot latest compile
     * @return the same item, possibly with documentation
     */
    public CompletionItem resolve(final CompletionItem item, final CompilationSnapshot snapshot) {
        if (item == null || item.getData() == null || snapshot == null) {
            return item;
        }
        Map<String, String> data = stringMap(item.getData());
        if (data.isEmpty()) {
            return item;
        }
        String kind = data.get("k");
        String name = data.get("n");
        if ("t".equals(kind) && name != null) {
            TypeIndex.TypeHit hit = snapshot.types().byName(name);
            if (hit != null && !hit.documentation().isEmpty()) {
                item.setDocumentation(Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, hit.documentation())));
            }
        }
        if ("m".equals(kind) && name != null) {
            String owner = data.get("o");
            MethodNode method = MethodBinding.findMethod(snapshot, owner, name, data.get("d"));
            if (method != null) {
                String docs = documentation(method);
                if (!docs.isEmpty()) {
                    item.setDocumentation(Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, docs)));
                }
            }
        }
        return item;
    }

    static String prefixAt(final TextDocument document, final Position position,
                           final PositionEncoding encoding) {
        if (document == null || position == null) {
            return "";
        }
        int[] groovy = document.toGroovy(position, encoding);
        String line = Positions.lineText(document.getText(), groovy[0]);
        int end = caretOffset(line, groovy[1]);
        int start = skipIdentifierBack(line, end);
        return line.substring(start, end);
    }

    static boolean isMemberAccess(final TextDocument document, final Position position,
                                  final PositionEncoding encoding) {
        if (document == null || position == null) {
            return false;
        }
        int[] groovy = document.toGroovy(position, encoding);
        String line = Positions.lineText(document.getText(), groovy[0]);
        int idx = skipWhitespaceBack(line, caretOffset(line, groovy[1]));
        idx = skipIdentifierBack(line, idx);
        return idx > 0 && line.codePointBefore(idx) == '.';
    }

    static int caretOffset(final String line, final int groovyColumn) {
        int col = Math.max(groovyColumn - 1, 0);
        int cpCount = line.codePointCount(0, line.length());
        if (col > cpCount) {
            col = cpCount;
        }
        return line.offsetByCodePoints(0, col);
    }

    static int skipWhitespaceBack(final String line, int idx) {
        while (idx > 0 && Character.isWhitespace(line.codePointBefore(idx))) {
            idx -= Character.charCount(line.codePointBefore(idx));
        }
        return idx;
    }

    static int skipIdentifierBack(final String line, int idx) {
        while (idx > 0) {
            int cp = line.codePointBefore(idx);
            if (!Character.isJavaIdentifierPart(cp) && cp != '$') {
                break;
            }
            idx -= Character.charCount(cp);
        }
        return idx;
    }

    private static void addOverrideCompletions(final List<CompletionItem> items, final ModuleNode module,
                                               final TextDocument document, final Position position,
                                               final PositionEncoding encoding, final String prefix) {
        ClassNode enclosing = enclosingClass(module, document, position, encoding);
        if (enclosing == null || enclosing.isInterface()) {
            return;
        }
        for (MethodNode method : CodeActionService.missingAbstracts(enclosing)) {
            if (!prefixMatches(method.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(method.getName());
            item.setKind(CompletionItemKind.Method);
            item.setDetail("override " + signature(method));
            attachParts(item, typeName(method.getReturnType()), parameterList(method));
            item.setSortText("0" + method.getName());
            item.setInsertText(CodeActionService.overrideSnippet(method));
            item.setInsertTextFormat(InsertTextFormat.Snippet);
            items.add(item);
        }
    }

    private static ClassNode enclosingClass(final ModuleNode module, final TextDocument document,
                                            final Position position, final PositionEncoding encoding) {
        for (ASTNode node : AstQuery.containing(module, document, position, encoding)) {
            if (node instanceof ClassNode classNode) {
                return classNode;
            }
        }
        return null;
    }

    private static void addNamedArgumentCompletions(final List<CompletionItem> items, final ModuleNode module,
                                                    final int line, final int column,
                                                    final CompilationSnapshot snapshot, final String prefix) {
        MethodBinding.ResolvedCall resolved = MethodBinding.resolveCall(
                AstQuery.enclosingCall(module, line, column), snapshot);
        if (resolved == null || resolved.target() == null || resolved.target().getParameters() == null) {
            return;
        }
        MethodNode target = resolved.target();
        Set<String> used = usedNamedKeys(resolved.arguments());
        for (Parameter parameter : target.getParameters()) {
            if (parameter == null || parameter.getName() == null || parameter.getName().startsWith("$")
                    || used.contains(parameter.getName()) || !prefixMatches(parameter.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(parameter.getName());
            item.setKind(CompletionItemKind.Field);
            item.setDetail("named argument");
            item.setSortText("0" + parameter.getName());
            item.setInsertText(parameter.getName() + ": ");
            items.add(item);
        }
    }

    private static Set<String> usedNamedKeys(final Expression arguments) {
        Set<String> used = new LinkedHashSet<>();
        for (Expression arg : CallSites.argumentExpressions(arguments)) {
            if (arg instanceof MapExpression map) {
                addMapKeys(used, map);
            }
        }
        return used;
    }

    private static void addMapKeys(final Set<String> used, final MapExpression map) {
        for (MapEntryExpression entry : map.getMapEntryExpressions()) {
            if (entry.getKeyExpression() instanceof ConstantExpression constant
                    && constant.getValue() != null) {
                used.add(constant.getValue().toString());
            }
        }
    }

    private static void addLocals(final List<CompletionItem> items, final ModuleNode module, final int line,
                                  final int column, final String prefix) {
        MethodNode enclosing = AstQuery.enclosingMethod(module, line, column);
        if (enclosing == null) {
            return;
        }
        for (Parameter parameter : enclosing.getParameters()) {
            if (!prefixMatches(parameter.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(parameter.getName());
            item.setKind(CompletionItemKind.Variable);
            item.setDetail(typeName(parameter.getType()));
            item.setSortText("0" + parameter.getName());
            items.add(item);
        }
    }

    private static void addWorkspaceTypes(final List<CompletionItem> items, final CompiledDocument compiled,
                                          final CompilationSnapshot snapshot, final String prefix,
                                          final PositionEncoding encoding) {
        if (snapshot == null) {
            return;
        }
        ModuleNode module = compiled.getModule();
        for (TypeIndex.TypeHit hit : snapshot.types().matchingPrefix(prefix, MAX_ITEMS)) {
            CompletionItem item = new CompletionItem(hit.simpleName());
            item.setKind(typeKind(hit));
            item.setDetail(hit.name());
            item.setSortText("1" + hit.simpleName());
            item.setData(Map.of("k", "t", "n", hit.name()));
            if (ImportSupport.needsImport(module, hit.name())) {
                item.setAdditionalTextEdits(ImportSupport.addImport(compiled, hit.name(), encoding));
            }
            items.add(item);
        }
    }

    private static void addMembers(final List<CompletionItem> items, final ClassNode receiver, final String prefix) {
        Set<String> seen = new LinkedHashSet<>();
        for (MethodNode method : receiver.getMethods()) {
            addMethod(items, seen, method, prefix);
        }
        for (FieldNode field : receiver.getFields()) {
            if (field.isSynthetic() || !prefixMatches(field.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(field.getName());
            item.setKind(CompletionItemKind.Field);
            item.setDetail(typeName(field.getType()));
            item.setSortText("2" + field.getName());
            items.add(item);
        }
        for (PropertyNode property : receiver.getProperties()) {
            if (property.isSynthetic() || !prefixMatches(property.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(property.getName());
            item.setKind(CompletionItemKind.Property);
            item.setSortText("2" + property.getName());
            items.add(item);
        }
    }


    private static void addSnippets(final List<CompletionItem> items, final String prefix) {
        addSnippet(items, prefix, "class", "class ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "interface", "interface ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "trait", "trait ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "enum", "enum ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "for", "for (${1:item} in ${2:items}) {\n    $0\n}", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "if", "if (${1:condition}) {\n    $0\n}", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "try", "try {\n    $0\n} catch (${1:Exception} ${2:e}) {\n    \n}", CompletionItemKind.Snippet);
    }

    private static void addSnippet(final List<CompletionItem> items, final String prefix, final String label,
                                   final String insert, final CompletionItemKind kind) {
        if (!prefixMatches(label, prefix)) {
            return;
        }
        CompletionItem item = new CompletionItem(label);
        item.setKind(kind);
        item.setInsertText(insert);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setSortText("3" + label);
        items.add(item);
    }

    private static void addMatching(final List<CompletionItem> items, final List<String> words, final String prefix,
                                    final CompletionItemKind kind) {
        for (String word : words) {
            if (prefixMatches(word, prefix)) {
                CompletionItem item = new CompletionItem(word);
                item.setKind(kind);
                item.setSortText("3" + word);
                items.add(item);
            }
        }
    }

    static boolean prefixMatches(final String label, final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        return label.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    private static String signature(final MethodNode method) {
        return typeName(method.getReturnType()) + " " + method.getName() + "(" + parameterList(method) + ")";
    }

    private static String parameterList(final MethodNode method) {
        Parameter[] parameters = parametersOf(method);
        String[] names = displayedNames(method, parameters);
        boolean varargs = (method.getModifiers() & ACC_VARARGS) != 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            String rendered = typeName(parameters[i].getType());
            if (varargs && i == names.length - 1 && rendered.endsWith("[]")) {
                rendered = rendered.substring(0, rendered.length() - 2) + "...";
            }
            builder.append(rendered).append(' ').append(names[i]);
        }
        return builder.toString();
    }

    /** Records the parameter list and return type on {@code item}. */
    static void attachParts(final CompletionItem item, final String returns, final String parameters) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("r", returns == null ? "" : returns);
        data.put("p", parameters == null ? "" : parameters);
        item.setData(data);
    }

    /**
     * Parameter names as the caller should see them. The compiler records
     * {@code argN} unless the class was built with {@code -parameters}.
     * JDK and Groovy class files still keep the source names in
     * {@code LocalVariableTable}; those are read here, not in {@code Java8},
     * so completion can show them without pinning class loaders in the runtime.
     */
    private static Parameter[] parametersOf(final MethodNode method) {
        if (method == null) {
            return Parameter.EMPTY_ARRAY;
        }
        Parameter[] parameters = method.getParameters();
        return parameters == null ? Parameter.EMPTY_ARRAY : parameters;
    }

    /**
     * Names for {@code parameters}. The returned array has the same length,
     * so callers index it with that array and not with a second
     * {@code getParameters()} call.
     */
    private static String[] displayedNames(final MethodNode method, final Parameter[] parameters) {
        String[] names = new String[parameters.length];
        boolean synthetic = false;
        for (int i = 0; i < parameters.length; i++) {
            names[i] = parameters[i].getName();
            synthetic |= syntheticName(names[i]);
        }
        if (!synthetic) {
            return names;
        }
        String[] reflected = reflectedParameterNames(method, parameters);
        int shared = Math.min(names.length, reflected.length);
        for (int i = 0; i < shared; i++) {
            if (syntheticName(names[i]) && !syntheticName(reflected[i])) {
                names[i] = reflected[i];
            }
        }
        return names;
    }

    private static boolean syntheticName(final String name) {
        return name == null || name.isEmpty() || SYNTHETIC_PARAMETER.matcher(name).matches();
    }

    private static final Pattern SYNTHETIC_PARAMETER = Pattern.compile("arg\\d+");

    private static final String[] NO_NAMES = new String[0];

    private static String[] reflectedParameterNames(final MethodNode method, final Parameter[] parameters) {
        Class<?> owner = method == null ? null : ExtensionCatalog.typeClass(method.getDeclaringClass());
        if (owner == null || parameters.length == 0) {
            return NO_NAMES;
        }
        Class<?>[] types = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = ExtensionCatalog.typeClass(parameters[i].getType());
            if (type == null) {
                return NO_NAMES;
            }
            types[i] = type;
        }
        try {
            Executable executable = method instanceof ConstructorNode
                    ? owner.getDeclaredConstructor(types)
                    : findDeclaredMethod(owner, method.getName(), types);
            return executable == null ? NO_NAMES : ReflectionParameterNames.names(executable);
        } catch (NoSuchMethodException | SecurityException ignored) {
            return NO_NAMES;
        }
    }

    private static Method findDeclaredMethod(final Class<?> owner, final String name, final Class<?>[] types)
            throws NoSuchMethodException {
        try {
            return owner.getDeclaredMethod(name, types);
        } catch (NoSuchMethodException ignored) {
            return owner.getMethod(name, types);
        }
    }

    /**
     * Source-style type name. Array class nodes keep the JVM descriptor as
     * their internal name ({@code [C}, {@code [Ljava.lang.String;});
     * {@link ClassNode#getNameWithoutPackage()} then yields {@code [C} or
     * {@code String;}.
     */
    static String typeName(final ClassNode type) {
        if (type == null) {
            return OBJECT_NAME;
        }
        if (type.isArray()) {
            return typeName(type.getComponentType()) + "[]";
        }
        String name = type.getName();
        if (name != null && !name.isEmpty() && name.charAt(0) == '[') {
            return descriptorToName(name);
        }
        return unqualified(name);
    }

    private static String descriptorToName(final String name) {
        int dims = 0;
        int i = 0;
        while (i < name.length() && name.charAt(i) == '[') {
            dims++;
            i++;
        }
        if (dims == 0 || i >= name.length()) {
            return unqualified(name);
        }
        String base = switch (name.charAt(i)) {
            case 'B' -> "byte";
            case 'C' -> "char";
            case 'D' -> "double";
            case 'F' -> "float";
            case 'I' -> "int";
            case 'J' -> "long";
            case 'S' -> "short";
            case 'Z' -> "boolean";
            case 'L' -> {
                int end = name.indexOf(';', i);
                String binary = end < 0 ? name.substring(i + 1) : name.substring(i + 1, end);
                yield unqualified(binary);
            }
            default -> name;
        };
        return base + "[]".repeat(dims);
    }

    private static String unqualified(final String name) {
        if (name == null || name.isEmpty()) {
            return OBJECT_NAME;
        }
        int dot = name.lastIndexOf('.');
        int slash = name.lastIndexOf('/');
        int cut = Math.max(dot, slash);
        String simple = cut >= 0 ? name.substring(cut + 1) : name;
        return simple.replace('$', '.');
    }

    private static String methodSnippet(final MethodNode method) {
        String[] names = displayedNames(method, parametersOf(method));
        StringBuilder builder = new StringBuilder(method.getName()).append('(');
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("${").append(i + 1).append(':').append(names[i]).append('}');
        }
        return builder.append(')').toString();
    }

    private static void addFromAst(final List<CompletionItem> items, final TextDocument document,
                                   final CompiledDocument compiled, final CompilationSnapshot snapshot,
                                   final Position position, final PositionEncoding encoding,
                                   final ModuleNode module) {
        String prefix = prefixAt(document, position, encoding);
        boolean memberAccess = isMemberAccess(document, position, encoding);
        int[] groovy = document.toGroovy(position, encoding);
        if (memberAccess) {
            ReceiverInference.Receiver receiverType = ReceiverInference.resolveReceiver(document, compiled.getSourceUnit(), module, position, encoding);
            ReceiverInference.addVisibleMembers(items, receiverType.type, prefix, receiverType.staticAccess);
            // A reflectable receiver contributes its own extension methods at
            // once. An unresolved receiver still waits for two characters,
            // because the only list available then is the whole catalog.
            if (ExtensionCatalog.typeClass(receiverType.type) != null || prefix.length() >= 2) {
                ExtensionCatalog.addExtensionMethods(items, prefix, receiverType.type, receiverType.staticAccess);
            }
            return;
        }
        addLocals(items, module, groovy[0], groovy[1], prefix);
        addNamedArgumentCompletions(items, module, groovy[0], groovy[1], snapshot, prefix);
        addOverrideCompletions(items, module, document, position, encoding, prefix);
        for (ClassNode classNode : module.getClasses()) {
            addMembers(items, classNode, prefix);
        }
        addWorkspaceTypes(items, compiled, snapshot, prefix, encoding);
    }


    static boolean usable(final ClassNode type) {
        return SymbolIdentity.isResolvedType(type);
    }

    private static CompletionItemKind typeKind(final TypeIndex.TypeHit hit) {
        if (hit.iface()) {
            return CompletionItemKind.Interface;
        }
        if (hit.enumeration()) {
            return CompletionItemKind.Enum;
        }
        return CompletionItemKind.Class;
    }

    static void addMethod(final List<CompletionItem> items, final Set<String> seen, final MethodNode method,
                                  final String prefix) {
        if (method.isSynthetic() || !seen.add(method.getName() + "/" + method.getParameters().length)
                || !prefixMatches(method.getName(), prefix)) {
            return;
        }
        CompletionItem item = new CompletionItem(method.getName());
        // IntelliJ has no Function kind. Static methods are methods.
        item.setKind(CompletionItemKind.Method);
        String returns = typeName(method.getReturnType());
        String parameters = parameterList(method);
        item.setDetail(returns + " " + method.getName() + "(" + parameters + ")");
        item.setSortText("2" + method.getName());
        item.setInsertText(methodSnippet(method));
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        String owner = method.getDeclaringClass() == null ? "" : method.getDeclaringClass().getName();
        String descriptor = method.getTypeDescriptor() == null ? "" : method.getTypeDescriptor();
        Map<String, String> data = new LinkedHashMap<>();
        data.put("k", "m");
        data.put("n", method.getName());
        data.put("o", owner);
        data.put("d", descriptor);
        data.put("r", returns);
        data.put("p", parameters);
        item.setData(data);
        items.add(item);
    }

    private static String documentation(final AnnotatedNode node) {
        Groovydoc groovydoc = node.getGroovydoc();
        if (groovydoc == null || !groovydoc.isPresent()) {
            return "";
        }
        String content = groovydoc.getContent();
        return content == null ? "" : content;
    }

    static Map<String, String> stringMap(final Object data) {
        if (!(data instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return result;
    }
}
