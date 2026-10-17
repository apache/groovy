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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovySystem;
import groovy.lang.MetaMethod;
import groovy.lang.groovydoc.Groovydoc;
import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.GroovyKeywords;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ReflectionParameterNames;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import static org.objectweb.asm.Opcodes.ACC_VARARGS;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
    /** Instance and static extension methods, including the GDK and classpath modules. */
    private static final List<MetaMethod> EXTENSION_METHODS = extensionCatalog();
    /**
     * Logical parameter names for extension methods, keyed by name and erased
     * parameter types. Runtime entries are often generated proxies that no
     * longer point at the original method.
     */
    private static volatile Map<String, String[]> extensionParameterNames;

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
        List<CompletionItem> items = new ArrayList<>();
        String prefix = prefixAt(document, position, encoding);
        boolean memberAccess = isMemberAccess(document, position, encoding);

        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        ModuleNode module = compiled == null ? null : compiled.getModule();
        if (module == null && memberAccess && compiled != null) {
            module = repairedModule(document, position, parentLoader, hostConfiguration);
        }

        if (!memberAccess) {
            addMatching(items, GroovyKeywords.KEYWORDS, prefix, CompletionItemKind.Keyword);
            addSnippets(items, prefix);
        }

        if (module != null) {
            addFromAst(items, document, compiled, snapshot, position, encoding, module);
        } else if (memberAccess && prefix.length() >= 2) {
            addExtensionMethods(items, prefix, null, false);
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

    private static int caretOffset(final String line, final int groovyColumn) {
        int col = Math.max(groovyColumn - 1, 0);
        int cpCount = line.codePointCount(0, line.length());
        if (col > cpCount) {
            col = cpCount;
        }
        return line.offsetByCodePoints(0, col);
    }

    private static int skipWhitespaceBack(final String line, int idx) {
        while (idx > 0 && Character.isWhitespace(line.codePointBefore(idx))) {
            idx -= Character.charCount(line.codePointBefore(idx));
        }
        return idx;
    }

    private static int skipIdentifierBack(final String line, int idx) {
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

    /**
     * Instance or static extension methods, including the GDK and any
     * {@code META-INF/groovy} extension module. The detail is a real
     * signature; the receiver parameter is not part of it.
     */
    private static void addExtensionMethods(final List<CompletionItem> items, final String prefix,
                                            final ClassNode receiver, final boolean staticAccess) {
        Set<String> seen = new LinkedHashSet<>();
        for (MetaMethod method : extensionMethods(receiver, staticAccess)) {
            String name = method.getName();
            if (name == null || name.isEmpty() || name.charAt(0) == '<' || name.indexOf('$') >= 0
                    || !prefixMatches(name, prefix)) {
                continue;
            }
            String detail = extensionDetail(method);
            if (!seen.add(name + " " + detail) || alreadyOffered(items, name, detail)) {
                continue;
            }
            CompletionItem item = new CompletionItem(name);
            item.setKind(CompletionItemKind.Method);
            item.setDetail(detail);
            item.setSortText("2" + name);
            item.setInsertText(name);
            items.add(item);
        }
    }

    private static List<MetaMethod> extensionMethods(final ClassNode receiver, final boolean staticAccess) {
        List<MetaMethod> methods = null;
        Class<?> type = typeClass(receiver);
        if (type != null) {
            // A loaded receiver lists only the extension methods that apply
            // to it. An empty list stays empty: the whole catalog is not a
            // substitute for a type that adds nothing.
            List<MetaMethod> found = InvokerHelper.getMetaClass(type).getMetaMethods();
            methods = found == null ? List.of() : found;
        } else {
            methods = EXTENSION_METHODS;
        }
        List<MetaMethod> matched = new ArrayList<>();
        for (MetaMethod method : methods) {
            if (method != null && method.isStatic() == staticAccess) {
                matched.add(method);
            }
        }
        return matched;
    }

    private static Class<?> typeClass(final ClassNode receiver) {
        if (!usable(receiver) || !receiver.isResolved()) {
            return null;
        }
        try {
            return receiver.getTypeClass();
        } catch (GroovyBugError | RuntimeException | LinkageError ignored) {
            // getTypeClass throws GroovyBugError when the node is not a loaded class
            return null;
        }
    }

    /**
     * Methods Groovy adds to existing classes: the GDK records, the VM plugin
     * variants, and every extension module visible to the Groovy runtime.
     */
    private static List<MetaMethod> extensionCatalog() {
        Set<MetaMethod> methods = new LinkedHashSet<>();
        addInstanceExtensions(methods, DefaultGroovyMethods.class);
        for (Class<?> extra : DefaultGroovyMethods.ADDITIONAL_CLASSES) {
            addInstanceExtensions(methods, extra);
        }
        for (Class<?> plugin : VMPluginFactory.getPlugin().getPluginDefaultGroovyMethods()) {
            addInstanceExtensions(methods, plugin);
        }
        addStaticExtensions(methods, DefaultGroovyStaticMethods.class);
        for (Class<?> plugin : VMPluginFactory.getPlugin().getPluginStaticGroovyMethods()) {
            addStaticExtensions(methods, plugin);
        }
        if (GroovySystem.getMetaClassRegistry() instanceof MetaClassRegistryImpl registry) {
            for (ExtensionModule module : registry.getModuleRegistry().getModules()) {
                try {
                    methods.addAll(module.getMetaMethods());
                } catch (RuntimeException | LinkageError ignored) {
                    // a module that cannot load does not hide the others
                }
            }
        }
        return List.copyOf(methods);
    }

    private static void addInstanceExtensions(final Set<MetaMethod> into, final Class<?> type) {
        addExtensions(into, type, true);
    }

    private static void addStaticExtensions(final Set<MetaMethod> into, final Class<?> type) {
        addExtensions(into, type, false);
    }

    private static void addExtensions(final Set<MetaMethod> into, final Class<?> type, final boolean instance) {
        if (type == null) {
            return;
        }
        for (Method method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())
                    || method.getDeclaringClass() == Object.class || method.getParameterCount() == 0) {
                continue;
            }
            CachedMethod cached = CachedMethod.find(method);
            if (cached == null) {
                cached = new CachedMethod(method);
            }
            into.add(instance ? new NewInstanceMetaMethod(cached) : new NewStaticMetaMethod(cached));
        }
    }

    private static boolean alreadyOffered(final List<CompletionItem> items, final String name, final String detail) {
        int arity = arityOf(detail);
        for (CompletionItem item : items) {
            if (name.equals(item.getLabel()) && arityOf(item.getDetail()) == arity) {
                return true;
            }
        }
        return false;
    }

    private static int arityOf(final String detail) {
        if (detail == null) {
            return -1;
        }
        int open = detail.lastIndexOf('(');
        int close = detail.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return -1;
        }
        String params = detail.substring(open + 1, close).trim();
        if (params.isEmpty()) {
            return 0;
        }
        int depth = 0;
        int commas = 0;
        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (c == '<' || c == '(') {
                depth++;
            } else if (c == '>' || c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                commas++;
            }
        }
        return commas + 1;
    }

    /**
     * {@code ReturnType name(Type param, ...)} for an extension method.
     * The first bytecode parameter is the receiver and is left out.
     */
    private static String extensionDetail(final MetaMethod method) {
        CachedClass[] logical = method.getParameterTypes();
        int count = logical == null ? 0 : logical.length;
        String[] names = parameterNames(method, count);
        StringBuilder builder = new StringBuilder();
        builder.append(simpleName(method.getReturnType())).append(' ').append(method.getName()).append('(');
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            Class<?> type = logical[i] == null ? null : logical[i].getTheClass();
            builder.append(simpleName(type)).append(' ').append(names[i]);
        }
        builder.append(')');
        return builder.toString();
    }

    /**
     * Names of the call parameters. Extension methods hide the receiver, so a
     * reflective {@code arg1} is shown as {@code arg0}.
     */
    private static String[] parameterNames(final MetaMethod method, final int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = "arg" + i;
        }
        String[] resolved = method instanceof ReflectionMetaMethod reflection
                ? reflectionNames(reflection, count)
                : extensionParameterNames().get(extensionKey(method));
        if (resolved == null) {
            return names;
        }
        for (int i = 0; i < count && i < resolved.length; i++) {
            String name = resolved[i];
            if (name != null && !name.isEmpty() && !name.matches("arg\\d+")) {
                names[i] = name;
            }
        }
        return names;
    }

    /**
     * Names of the parameters the caller writes. A {@link NewMetaMethod} hides
     * the receiver, which is the first bytecode parameter.
     */
    private static String[] reflectionNames(final ReflectionMetaMethod reflection, final int count) {
        String[] names = new String[count];
        MetaMethod cached = reflection.getCachedMethod();
        if (!(cached instanceof CachedMethod cachedMethod)) {
            return names;
        }
        Method reflected = cachedMethod.getCachedMethod();
        int start = reflection instanceof NewMetaMethod ? 1 : 0;
        String[] resolved = ReflectionParameterNames.names(reflected);
        if (resolved == null) {
            return names;
        }
        for (int i = 0; i < count; i++) {
            int source = start + i;
            if (source >= resolved.length) {
                break;
            }
            names[i] = resolved[source];
        }
        return names;
    }

    private static Map<String, String[]> extensionParameterNames() {
        Map<String, String[]> names = extensionParameterNames;
        if (names != null) {
            return names;
        }
        synchronized (CompletionService.class) {
            if (extensionParameterNames == null) {
                Set<MetaMethod> sources = new LinkedHashSet<>();
                // StringGroovyMethods and the other JDK categories are not in
                // ADDITIONAL_CLASSES. The runtime serves them as generated
                // proxies, so their names have to be read from the source class.
                for (Class<?> type : DefaultGroovyMethods.DGM_LIKE_CLASSES) {
                    addInstanceExtensions(sources, type);
                    addStaticExtensions(sources, type);
                }
                sources.addAll(EXTENSION_METHODS);
                Map<String, String[]> loaded = new HashMap<>();
                for (MetaMethod method : sources) {
                    if (method instanceof ReflectionMetaMethod reflection) {
                        int count = method.getParameterTypes() == null ? 0 : method.getParameterTypes().length;
                        loaded.putIfAbsent(extensionKey(method), reflectionNames(reflection, count));
                    }
                }
                extensionParameterNames = Map.copyOf(loaded);
            }
            return extensionParameterNames;
        }
    }

    /** Name plus erased call-parameter types. The receiver is not included. */
    private static String extensionKey(final MetaMethod method) {
        StringBuilder key = new StringBuilder();
        key.append(method.isStatic() ? 'S' : 'I').append(method.getName());
        Class<?>[] types = method.getNativeParameterTypes();
        if (types != null) {
            for (Class<?> type : types) {
                key.append('\0').append(type == null ? "?" : type.getName());
            }
        }
        return key.toString();
    }

    private static String simpleName(final Class<?> type) {
        if (type == null) {
            return "Object";
        }
        if (type.isArray()) {
            return simpleName(type.getComponentType()) + "[]";
        }
        String name = type.getSimpleName();
        return name.isEmpty() ? type.getName() : name;
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
        StringBuilder builder = new StringBuilder();
        builder.append(typeName(method.getReturnType())).append(' ').append(method.getName()).append('(');
        Parameter[] parameters = method.getParameters();
        boolean varargs = parameters != null && (method.getModifiers() & ACC_VARARGS) != 0;
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                String rendered = typeName(parameters[i].getType());
                if (varargs && i == parameters.length - 1 && rendered.endsWith("[]")) {
                    rendered = rendered.substring(0, rendered.length() - 2) + "...";
                }
                builder.append(rendered).append(' ').append(parameters[i].getName());
            }
        }
        builder.append(')');
        return builder.toString();
    }

    /**
     * Source-style type name. Array class nodes keep the JVM descriptor as
     * their internal name ({@code [C}, {@code [Ljava.lang.String;});
     * {@link ClassNode#getNameWithoutPackage()} then yields {@code [C} or
     * {@code String;}.
     */
    static String typeName(final ClassNode type) {
        if (type == null) {
            return "Object";
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
            return "Object";
        }
        int dot = name.lastIndexOf('.');
        int slash = name.lastIndexOf('/');
        int cut = Math.max(dot, slash);
        String simple = cut >= 0 ? name.substring(cut + 1) : name;
        return simple.replace('$', '.');
    }

    private static String methodSnippet(final MethodNode method) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return method.getName() + "()";
        }
        StringBuilder builder = new StringBuilder(method.getName()).append('(');
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("${").append(i + 1).append(':').append(parameters[i].getName()).append('}');
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
            Receiver receiverType = resolveReceiver(document, compiled.getSourceUnit(), module, position, encoding);
            addVisibleMembers(items, receiverType.type, prefix, receiverType.staticAccess);
            // A reflectable receiver contributes its own extension methods at
            // once. An unresolved receiver still waits for two characters,
            // because the only list available then is the whole catalog.
            if (typeClass(receiverType.type) != null || prefix.length() >= 2) {
                addExtensionMethods(items, prefix, receiverType.type, receiverType.staticAccess);
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

    /**
     * Receiver of a member completion. {@code staticAccess} is true when the
     * text names a class ({@code String.}) rather than a value.
     */
    private static final class Receiver {
        private final ClassNode type;
        private final boolean staticAccess;

        private Receiver(final ClassNode type, final boolean staticAccess) {
            this.type = type == null ? ClassHelper.OBJECT_TYPE : type;
            this.staticAccess = staticAccess;
        }
    }

    private static Receiver resolveReceiver(final TextDocument document, final SourceUnit unit,
                                            final ModuleNode module, final Position position,
                                            final PositionEncoding encoding) {
        int[] groovy = document.toGroovy(position, encoding);
        String line = Positions.lineText(document.getText(), groovy[0]);
        int caret = caretOffset(line, groovy[1]);
        List<String> parts = receiverParts(line, caret);
        ClassNode type = null;
        boolean staticAccess = false;
        if (!parts.isEmpty()) {
            type = declaredType(module, parts.get(0), groovy[0], groovy[1]);
            type = concretize(type, module, unit);
            if (!usable(type)) {
                type = resolveSimpleName(parts.get(0), module, unit);
                staticAccess = usable(type);
            }
            for (int i = 1; i < parts.size() && usable(type); i++) {
                type = concretize(memberType(type, parts.get(i)), module, unit);
                staticAccess = false;
            }
        }
        if (!usable(type)) {
            int end = receiverEndOffset(line, caret);
            int column = end <= 0 ? 1 : line.codePointCount(0, end);
            type = concretize(expressionAt(module, groovy[0], column), module, unit);
            staticAccess = false;
        }
        if (!usable(type)) {
            return new Receiver(ClassHelper.OBJECT_TYPE, false);
        }
        return new Receiver(type, staticAccess);
    }

    /**
     * Dotted names before the member-access dot, so {@code a.b.} is
     * {@code [a, b]}. Parentheses and index expressions return an empty list
     * and are typed from the AST instead.
     */
    private static List<String> receiverParts(final String line, final int caret) {
        int end = receiverEndOffset(line, caret);
        List<String> parts = new ArrayList<>();
        int index = end;
        while (index > 0) {
            int start = skipIdentifierBack(line, index);
            if (start == index) {
                break;
            }
            parts.add(0, line.substring(start, index));
            index = skipWhitespaceBack(line, start);
            if (index <= 0 || line.charAt(index - 1) != '.') {
                break;
            }
            index -= 1;
            if (index > 0) {
                char before = line.charAt(index - 1);
                if (before == '?' || before == '*') {
                    index -= 1;
                }
            }
            index = skipWhitespaceBack(line, index);
        }
        return parts;
    }

    /** Exclusive UTF-16 end of the receiver expression, before the trigger dot. */
    private static int receiverEndOffset(final String line, final int caret) {
        int end = skipWhitespaceBack(line, caret);
        end = skipIdentifierBack(line, end);
        end = skipWhitespaceBack(line, end);
        if (end <= 0 || line.charAt(end - 1) != '.') {
            return end;
        }
        end -= 1;
        if (end > 0) {
            char before = line.charAt(end - 1);
            if (before == '?' || before == '*') {
                end -= 1;
            }
        }
        return skipWhitespaceBack(line, end);
    }

    private static ClassNode expressionAt(final ModuleNode module, final int line, final int column) {
        for (ASTNode node : AstQuery.containing(module, line, column)) {
            ClassNode type = expressionType(node, module);
            if (usable(type)) {
                return type;
            }
        }
        return null;
    }

    private static ClassNode expressionType(final ASTNode node, final ModuleNode module) {
        if (node instanceof VariableExpression variable) {
            return TypeInference.of(variable, module);
        }
        if (node instanceof ConstructorCallExpression constructor) {
            return constructor.getType();
        }
        if (node instanceof MethodCallExpression call) {
            return call.getType();
        }
        if (node instanceof ClassExpression classExpression) {
            return classExpression.getType();
        }
        if (node instanceof ConstantExpression constant) {
            return constant.getType();
        }
        if (node instanceof PropertyExpression property) {
            ClassNode type = property.getType();
            return usable(type) ? type : expressionType(property.getObjectExpression(), module);
        }
        return null;
    }

    private static ClassNode declaredType(final ModuleNode module, final String name, final int line, final int column) {
        if (module == null || name == null || name.isEmpty()) {
            return null;
        }
        ClassNode[] best = new ClassNode[1];
        int[] rank = new int[] {Integer.MIN_VALUE};
        int caretRank = line * 1_000_000 + Math.max(column, 0);
        AstQuery.walk(module, (node, ctx) -> {
            if (node instanceof DeclarationExpression declaration) {
                offerDeclaration(declaration, module, name, caretRank, best, rank);
            } else if (node instanceof BinaryExpression binary) {
                offerAssignment(binary, name, caretRank, best, rank);
            } else if (node instanceof Parameter parameter && name.equals(parameter.getName())) {
                offer(parameter.getLineNumber(), parameter.getColumnNumber(), TypeInference.of(parameter),
                        caretRank, best, rank);
            } else if (node instanceof FieldNode field && name.equals(field.getName())) {
                offer(field.getLineNumber(), field.getColumnNumber(), TypeInference.of(field), caretRank, best, rank);
            }
        });
        return best[0];
    }

    private static void offerDeclaration(final DeclarationExpression declaration, final ModuleNode module,
                                         final String name, final int caretRank,
                                         final ClassNode[] best, final int[] rank) {
        if (declaration.isMultipleAssignmentDeclaration()) {
            return;
        }
        VariableExpression left = declaration.getVariableExpression();
        if (left == null || !name.equals(left.getName())) {
            return;
        }
        offer(left.getLineNumber(), left.getColumnNumber(), TypeInference.of(left, module), caretRank, best, rank);
    }

    private static void offerAssignment(final BinaryExpression binary, final String name, final int caretRank,
                                        final ClassNode[] best, final int[] rank) {
        if (binary instanceof DeclarationExpression || binary.getOperation() == null
                || binary.getOperation().getType() != Types.ASSIGN
                || !(binary.getLeftExpression() instanceof VariableExpression variable)
                || !name.equals(variable.getName())) {
            return;
        }
        ClassNode type = binary.getRightExpression() == null ? null : binary.getRightExpression().getType();
        offer(variable.getLineNumber(), variable.getColumnNumber(), type, caretRank, best, rank);
    }

    private static void offer(final int nodeLine, final int nodeColumn, final ClassNode type,
                              final int caretRank, final ClassNode[] best, final int[] rank) {
        if (!usable(type) || nodeLine <= 0) {
            return;
        }
        int score = nodeLine * 1_000_000 + Math.max(nodeColumn, 0);
        if (score > caretRank || score < rank[0]) {
            return;
        }
        rank[0] = score;
        best[0] = type;
    }

    private static ClassNode memberType(final ClassNode owner, final String name) {
        if (owner == null || name == null) {
            return null;
        }
        ClassNode[] found = new ClassNode[1];
        eachType(owner, current -> {
            if (found[0] != null) {
                return;
            }
            found[0] = fieldOrPropertyType(current, name);
            if (found[0] == null) {
                found[0] = zeroArgReturn(current, name);
            }
        });
        return found[0];
    }

    private static ClassNode fieldOrPropertyType(final ClassNode owner, final String name) {
        for (FieldNode field : owner.getFields()) {
            if (name.equals(field.getName()) && field.isPublic()) {
                return field.getType();
            }
        }
        for (PropertyNode property : owner.getProperties()) {
            if (name.equals(property.getName()) && !Modifier.isPrivate(property.getModifiers())) {
                return property.getType();
            }
        }
        return null;
    }

    private static ClassNode zeroArgReturn(final ClassNode owner, final String name) {
        String suffix = name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (MethodNode method : owner.getMethods()) {
            if (method.getParameters().length != 0 || !method.isPublic() || method.getName() == null) {
                continue;
            }
            String methodName = method.getName();
            if (name.equals(methodName) || ("get" + suffix).equals(methodName) || ("is" + suffix).equals(methodName)) {
                return method.getReturnType();
            }
        }
        return null;
    }

    private static void addVisibleMembers(final List<CompletionItem> items, final ClassNode receiver,
                                          final String prefix, final boolean staticAccess) {
        ClassNode type = receiver == null ? ClassHelper.OBJECT_TYPE : receiver;
        if (!staticAccess && ClassHelper.isPrimitiveType(type)) {
            type = ClassHelper.getWrapper(type);
        }
        Set<String> seen = new LinkedHashSet<>();
        if (type.isArray() && !staticAccess && prefixMatches("length", prefix)) {
            CompletionItem length = new CompletionItem("length");
            length.setKind(CompletionItemKind.Field);
            length.setDetail("int");
            length.setSortText("2length");
            items.add(length);
            seen.add("length");
        }
        eachType(type, current -> {
            for (MethodNode method : current.getMethods()) {
                if (method.isSynthetic() || !method.isPublic() || method.getName() == null
                        || method.getName().startsWith("<") || method.getName().indexOf('$') >= 0) {
                    continue;
                }
                addMethod(items, seen, method, prefix);
            }
            addFieldsAndProperties(items, seen, current, prefix);
        });
    }

    private static void addFieldsAndProperties(final List<CompletionItem> items, final Set<String> seen,
                                               final ClassNode owner, final String prefix) {
        for (FieldNode field : owner.getFields()) {
            if (field.isSynthetic() || !field.isPublic() || field.getName() == null
                    || !prefixMatches(field.getName(), prefix) || !seen.add(field.getName())) {
                continue;
            }
            CompletionItem item = new CompletionItem(field.getName());
            item.setKind(CompletionItemKind.Field);
            item.setDetail(typeName(field.getType()));
            item.setSortText("2" + field.getName());
            items.add(item);
        }
        for (PropertyNode property : owner.getProperties()) {
            if (property.isSynthetic() || property.getName() == null || Modifier.isPrivate(property.getModifiers())
                    || !prefixMatches(property.getName(), prefix) || !seen.add(property.getName())) {
                continue;
            }
            CompletionItem item = new CompletionItem(property.getName());
            item.setKind(CompletionItemKind.Property);
            item.setSortText("2" + property.getName());
            items.add(item);
        }
    }

    private static void eachType(final ClassNode start, final Consumer<ClassNode> action) {
        ArrayDeque<ClassNode> pending = new ArrayDeque<>();
        pending.add(start);
        Set<String> seen = new LinkedHashSet<>();
        while (!pending.isEmpty()) {
            ClassNode current = pending.removeFirst();
            if (current == null) {
                continue;
            }
            ClassNode redirected = current.redirect();
            String key = redirected.getName();
            if (key != null && !seen.add(key)) {
                continue;
            }
            action.accept(redirected);
            ClassNode superClass = redirected.getSuperClass();
            if (superClass != null) {
                pending.add(superClass);
            }
            ClassNode[] interfaces = redirected.getInterfaces();
            if (interfaces != null) {
                for (ClassNode iface : interfaces) {
                    if (iface != null) {
                        pending.add(iface);
                    }
                }
            }
        }
    }

    /**
     * Compiles a parsable stand-in for a member access the real buffer rejected.
     * The caret line becomes {@code <receiver>.lsp}, and a leading {@code def}
     * on a bare name is dropped so {@code def a.} still sees the earlier {@code a}.
     */
    private static ModuleNode repairedModule(final TextDocument document, final Position position,
                                             final ClassLoader parentLoader,
                                             final CompilerConfiguration hostConfiguration) {
        if (document == null || position == null) {
            return null;
        }
        String repaired = repairMemberAccess(document.getText(), position.getLine(), position.getCharacter());
        if (repaired == null || repaired.equals(document.getText())) {
            return null;
        }
        CompilerConfiguration configuration = repairConfiguration(hostConfiguration);
        ClassLoader parent = parentLoader == null ? CompletionService.class.getClassLoader() : parentLoader;
        GroovyClassLoader loader = new GroovyClassLoader(parent, configuration);
        try {
            CompilationUnit unit = new CompilationUnit(configuration, null, loader);
            unit.addSource("repair.groovy", repaired);
            try {
                unit.compile(Phases.SEMANTIC_ANALYSIS);
            } catch (CompilationFailedException ignored) {
                // the stand-in can still carry the declarations above the caret
            }
            ModuleNode found = null;
            var sources = unit.iterator();
            while (sources.hasNext()) {
                SourceUnit source = sources.next();
                if (source.getAST() != null) {
                    found = source.getAST();
                }
            }
            return found;
        } finally {
            try {
                loader.close();
            } catch (IOException ignored) {
                // the module already holds the resolved JDK types
            }
        }
    }

    private static CompilerConfiguration repairConfiguration(final CompilerConfiguration host) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTolerance(100);
        if (host == null) {
            return configuration;
        }
        if (host.getScriptBaseClass() != null) {
            configuration.setScriptBaseClass(host.getScriptBaseClass());
        }
        List<CompilationCustomizer> customizers = host.getCompilationCustomizers();
        if (customizers != null && !customizers.isEmpty()) {
            configuration.addCompilationCustomizers(customizers.toArray(CompilationCustomizer[]::new));
        }
        return configuration;
    }

    private static String repairMemberAccess(final String text, final int line, final int character) {
        if (text == null) {
            return null;
        }
        int[] range = lineRange(text, Math.max(line, 0));
        String sourceLine = text.substring(range[0], range[1]);
        int caret = Math.min(Math.max(character, 0), sourceLine.length());
        int end = receiverEndOffset(sourceLine, caret);
        String receiver = sourceLine.substring(0, end).trim();
        if (receiver.startsWith("def") && receiver.length() > 3 && Character.isWhitespace(receiver.charAt(3))) {
            String rest = receiver.substring(3).trim();
            if (isIdentifier(rest)) {
                receiver = rest;
            }
        }
        if (receiver.isEmpty()) {
            return text;
        }
        return text.substring(0, range[0]) + receiver + ".lsp" + text.substring(range[1]);
    }

    private static boolean isIdentifier(final String text) {
        if (text == null || text.isEmpty() || !Character.isJavaIdentifierStart(text.codePointAt(0))) {
            return false;
        }
        int index = Character.charCount(text.codePointAt(0));
        while (index < text.length()) {
            int cp = text.codePointAt(index);
            if (!Character.isJavaIdentifierPart(cp) && cp != '$') {
                return false;
            }
            index += Character.charCount(cp);
        }
        return true;
    }

    private static int[] lineRange(final String text, final int line) {
        int current = 0;
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '\n' && c != '\r') {
                continue;
            }
            if (current == line) {
                return new int[] {start, i};
            }
            if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                i += 1;
            }
            current += 1;
            start = i + 1;
        }
        if (current == line) {
            return new int[] {start, text.length()};
        }
        return new int[] {text.length(), text.length()};
    }

    private static ClassNode concretize(final ClassNode type, final ModuleNode module, final SourceUnit unit) {
        if (type == null) {
            return null;
        }
        ClassNode current = type.redirect();
        if (current.isResolved() || ClassHelper.isPrimitiveType(current)) {
            return current;
        }
        String name = current.getName();
        if (name == null || name.isEmpty()) {
            return current;
        }
        if (name.indexOf('.') >= 0) {
            ClassNode loaded = loadClass(name, unit);
            if (loaded != null) {
                return loaded;
            }
            ClassNode source = sourceClass(module, name);
            return source == null ? current : source;
        }
        ClassNode simple = resolveSimpleName(name, module, unit);
        return simple == null ? current : simple;
    }

    private static ClassNode resolveSimpleName(final String name, final ModuleNode module, final SourceUnit unit) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if ("BigDecimal".equals(name)) {
            return ClassHelper.BigDecimal_TYPE;
        }
        if ("BigInteger".equals(name)) {
            return ClassHelper.BigInteger_TYPE;
        }
        ClassNode source = sourceClass(module, name);
        if (source != null) {
            return source;
        }
        if (module != null) {
            ClassNode imported = module.getImportType(name);
            if (imported != null) {
                ClassNode loaded = loadClass(imported.getName(), unit);
                if (loaded != null) {
                    return loaded;
                }
                return imported;
            }
            for (ImportNode star : module.getStarImports()) {
                ClassNode loaded = loadClass(qualify(star.getPackageName(), name), unit);
                if (loaded != null) {
                    return loaded;
                }
            }
        }
        for (String prefix : ResolveVisitor.DEFAULT_IMPORTS) {
            ClassNode loaded = loadClass(prefix + name, unit);
            if (loaded != null) {
                return loaded;
            }
        }
        return null;
    }

    private static ClassNode sourceClass(final ModuleNode module, final String name) {
        if (module == null || name == null) {
            return null;
        }
        for (ClassNode classNode : module.getClasses()) {
            if (classNode.isScript()) {
                continue;
            }
            if (name.equals(classNode.getName()) || name.equals(classNode.getNameWithoutPackage())) {
                return classNode;
            }
        }
        return null;
    }

    private static String qualify(final String packageName, final String simple) {
        if (packageName == null || packageName.isEmpty() || simple == null) {
            return null;
        }
        return packageName.endsWith(".") ? packageName + simple : packageName + "." + simple;
    }

    private static ClassNode loadClass(final String name, final SourceUnit unit) {
        if (name == null || name.isEmpty() || name.endsWith(".")) {
            return null;
        }
        ClassLoader loader = unit == null ? null : unit.getClassLoader();
        if (loader == null) {
            loader = CompletionService.class.getClassLoader();
        }
        try {
            return ClassHelper.make(Class.forName(name, false, loader));
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean usable(final ClassNode type) {
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

    private static void addMethod(final List<CompletionItem> items, final Set<String> seen, final MethodNode method,
                                  final String prefix) {
        if (method.isSynthetic() || !seen.add(method.getName() + "/" + method.getParameters().length)
                || !prefixMatches(method.getName(), prefix)) {
            return;
        }
        CompletionItem item = new CompletionItem(method.getName());
        // IntelliJ has no Function kind. Static methods are methods.
        item.setKind(CompletionItemKind.Method);
        item.setDetail(signature(method));
        item.setSortText("2" + method.getName());
        item.setInsertText(methodSnippet(method));
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        String owner = method.getDeclaringClass() == null ? "" : method.getDeclaringClass().getName();
        String descriptor = method.getTypeDescriptor() == null ? "" : method.getTypeDescriptor();
        item.setData(Map.of("k", "m", "n", method.getName(), "o", owner, "d", descriptor));
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
