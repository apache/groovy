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
package org.codehaus.groovy.ast;

import groovy.lang.Binding;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.BaseScriptASTTransformation;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Represents a module, which consists typically of a class declaration
 * but could include some imports, some statements and multiple classes
 * intermixed with statements like scripts in Python or Ruby
 */
public class ModuleNode extends ASTNode implements Opcodes {

    private List<ClassNode> classes = new LinkedList<>();
    private final List<MethodNode> methods = new ArrayList<>();
    private final Map<String, ImportNode> imports = new HashMap<>();
    private final List<ImportNode> starImports = new ArrayList<>();
    private final Map<String, ImportNode> staticImports = new LinkedHashMap<>();
    private final Map<String, ImportNode> staticStarImports = new LinkedHashMap<>();
    private CompileUnit unit;
    private PackageNode packageNode;
    private String description;
    private boolean createClassForStatements = true;
    private transient SourceUnit context;
    private boolean importsResolved;
    private ClassNode scriptDummy;
    private String mainClassName;
    private final BlockStatement statementBlock = new BlockStatement();

    public ModuleNode(final SourceUnit context) {
        this.context = context;
    }

    public ModuleNode(final CompileUnit unit) {
        this.unit = unit;
    }

    public List<ClassNode> getClasses() {
        if (createClassForStatements && (!statementBlock.isEmpty() || !methods.isEmpty() || isPackageInfo())) {
            ClassNode mainClass = createStatementsClass();
            mainClassName = mainClass.getName();
            createClassForStatements = false;
            classes.add(0, mainClass);
            mainClass.setModule(this);
            addToCompileUnit(mainClass);
        }
        return /*Collections.unmodifiableList(*/classes/*)*/; // modified by MacroClassTransform
    }

    public List<MethodNode> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public List<ImportNode> getImports() {
        return new ArrayList<>(imports.values());
    }

    public List<ImportNode> getStarImports() {
        return Collections.unmodifiableList(starImports);
    }

    public Map<String, ImportNode> getStaticImports() {
        return Collections.unmodifiableMap(staticImports);
    }

    public Map<String, ImportNode> getStaticStarImports() {
        return Collections.unmodifiableMap(staticStarImports);
    }

    /**
     * @param alias the name of interest
     * @return the import type for the given alias or null if none is available
     */
    public ClassNode getImportType(final String alias) {
        ImportNode node = getImport(alias);
        return (node != null ? node.getType() : null);
    }

    /**
     * @param alias the name of interest
     * @return the import node for the given alias or null if none is available
     */
    public ImportNode getImport(final String alias) {
        return imports.get(alias);
    }

    public void addImport(final String alias, final ClassNode type) {
        addImport(alias, type, Collections.emptyList());
    }

    public void addImport(final String alias, final ClassNode type, final List<AnnotationNode> annotations) {
        ImportNode importNode = new ImportNode(type, alias);
        importNode.addAnnotations(annotations);
        imports.put(alias, importNode);

        storeLastAddedImportNode(importNode);
    }

    public void addStarImport(final String packageName) {
        addStarImport(packageName, Collections.emptyList());
    }

    public void addStarImport(final String packageName, final List<AnnotationNode> annotations) {
        ImportNode importNode = new ImportNode(packageName);
        importNode.addAnnotations(annotations);
        starImports.add(importNode);

        storeLastAddedImportNode(importNode);
    }

    public void addStaticImport(final ClassNode type, final String fieldName, final String alias) {
        addStaticImport(type, fieldName, alias, Collections.emptyList());
    }

    public void addStaticImport(final ClassNode type, final String fieldName, final String alias, final List<AnnotationNode> annotations) {
        ImportNode node = new ImportNode(type, fieldName, alias);
        node.addAnnotations(annotations);
        ImportNode prev = staticImports.put(alias, node);
        if (prev != null) {
            staticImports.put(prev.toString(), prev);
            staticImports.put(alias, staticImports.remove(alias));
        }

        storeLastAddedImportNode(node);
    }

    public void addStaticStarImport(final String name, final ClassNode type) {
        addStaticStarImport(name, type, Collections.emptyList());
    }

    public void addStaticStarImport(final String name, final ClassNode type, final List<AnnotationNode> annotations) {
        ImportNode node = new ImportNode(type);
        node.addAnnotations(annotations);
        staticStarImports.put(name, node);

        storeLastAddedImportNode(node);
    }

    public void addStatement(final Statement node) {
        statementBlock.addStatement(node);
    }

    public void addClass(final ClassNode node) {
        if (classes.isEmpty())
            mainClassName = node.getName();
        classes.add(node);
        node.setModule(this);
        addToCompileUnit(node);
    }

    private void addToCompileUnit(final ClassNode node) {
        // register the new class with the compile unit
        if (unit != null) {
            unit.addClass(node);
        }
    }

    public void addMethod(final MethodNode node) {
        methods.add(node);
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
    }

    public String getPackageName() {
        return packageNode == null ? null : packageNode.getName();
    }

    public PackageNode getPackage() {
        return packageNode;
    }

    public boolean hasPackage() {
        return (packageNode != null);
    }

    public void setPackage(final PackageNode packageNode) {
        this.packageNode = packageNode;
    }

    public boolean hasPackageName() {
        return (packageNode != null && packageNode.getName() != null);
    }

    public void setPackageName(final String packageName) {
        setPackage(new PackageNode(packageName));
    }

    /**
     * @return the underlying character stream description
     */
    public String getDescription() {
        if (context != null) {
            return context.getName();
        } else {
            return description;
        }
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public CompileUnit getUnit() {
        return unit;
    }

    void setUnit(final CompileUnit unit) {
        this.unit = unit;
    }

    public SourceUnit getContext() {
        return context;
    }

    private boolean isPackageInfo() {
        return context != null && context.getName() != null && context.getName().endsWith("package-info.groovy");
    }

    public ClassNode getScriptClassDummy() {
        if (scriptDummy != null) {
            setScriptBaseClassFromConfig(scriptDummy);
            return scriptDummy;
        }

        String name = getPackageName();
        if (name == null) {
            name = "";
        }
        // now let's use the file name to determine the class name
        if (getDescription() == null) {
            throw new RuntimeException("Cannot generate main(String[]) class for statements when we have no file description");
        }
        name += GeneratorContext.encodeAsValidClassName(extractClassFromFileDescription());

        ClassNode classNode;
        if (isPackageInfo()) {
            classNode = new ClassNode(name, ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        } else {
            classNode = new ClassNode(name, ACC_PUBLIC, ClassHelper.SCRIPT_TYPE);
            setScriptBaseClassFromConfig(classNode);
            classNode.setScript(true);
            classNode.setScriptBody(true);
        }

        scriptDummy = classNode;
        return classNode;
    }

    private void setScriptBaseClassFromConfig(final ClassNode cn) {
        String baseClassName = null;
        if (unit != null) {
            baseClassName = unit.getConfig().getScriptBaseClass();
        } else if (context != null) {
            baseClassName = context.getConfiguration().getScriptBaseClass();
        }
        if (baseClassName != null) {
            if (!cn.getSuperClass().getName().equals(baseClassName)) {
                cn.setSuperClass(ClassHelper.make(baseClassName));
                AnnotationNode annotationNode = new AnnotationNode(BaseScriptASTTransformation.MY_TYPE);
                cn.addAnnotation(annotationNode);
            }
        }
    }

    protected ClassNode createStatementsClass() {
        ClassNode classNode = getScriptClassDummy();
        if (classNode.getName().endsWith("package-info")) {
            return classNode;
        }

        handleMainMethodIfPresent(methods);

        // return new Foo(new ShellContext(args)).run()
        classNode.addMethod(
            new MethodNode(
                "main",
                ACC_PUBLIC | ACC_STATIC,
                ClassHelper.VOID_TYPE,
                params(param(ClassHelper.STRING_TYPE.makeArray(), "args")),
                ClassNode.EMPTY_ARRAY,
                stmt(
                    callX(
                        classX(ClassHelper.make(InvokerHelper.class)),
                        "runScript",
                        args(classX(classNode), varX("args"))
                    )
                )
            )
        );

        MethodNode methodNode = new MethodNode("run", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statementBlock);
        methodNode.setIsScriptBody();
        classNode.addMethod(methodNode);

        classNode.addConstructor(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());

        Statement stmt;
        // A script's contextual constructor should call it's super class' contextual constructor, if it has one.
        // In practice this will always be true because currently this visitor is run before the AST transformations
        // (like @BaseScript) that could change this.  But this is cautious and anticipates possible compiler changes.
        if (classNode.getSuperClass().getDeclaredConstructor(params(param(ClassHelper.BINDING_TYPE, "context"))) != null) {
            stmt = stmt(ctorX(ClassNode.SUPER, args(varX("context"))));
        } else {
            // Fallback for non-standard base "script" classes with no context (Binding) constructor.
            stmt = stmt(callX(varX("super"), "setBinding", args(varX("context"))));
        }

        classNode.addConstructor(
            ACC_PUBLIC,
            params(param(ClassHelper.make(Binding.class), "context")),
            ClassNode.EMPTY_ARRAY,
            stmt);

        for (MethodNode method : methods) {
            if (method.isAbstract()) {
                throw new RuntimeException("Cannot use abstract methods in a script" +
                    ", they are only available inside classes. Method: " + method.getName());
            }
            classNode.addMethod(method);
        }
        return classNode;
    }

    /*
     * If a main method is provided by user, account for it under run() as scripts generate their own 'main' so they can run.
     */
    private void handleMainMethodIfPresent(final List<MethodNode> methods) {
        boolean found = false;
        for (Iterator<MethodNode> iter = methods.iterator(); iter.hasNext();) {
            MethodNode node = iter.next();
            if (node.getName().equals("main")) {
                if (node.isStatic() && node.getParameters().length == 1) {
                    boolean retTypeMatches, argTypeMatches;
                    ClassNode argType = node.getParameters()[0].getType();
                    ClassNode retType = node.getReturnType();

                    argTypeMatches = (argType.equals(ClassHelper.OBJECT_TYPE) || argType.getName().contains("String[]"));
                    retTypeMatches = (retType == ClassHelper.VOID_TYPE || retType == ClassHelper.OBJECT_TYPE);

                    if (retTypeMatches && argTypeMatches) {
                        if (found) {
                            throw new RuntimeException("Repetitive main method found.");
                        } else {
                            found = true;
                        }
                        // if script has both loose statements as well as main(), then main() is ignored
                        if (statementBlock.isEmpty()) {
                            addStatement(node.getCode());
                        }
                        iter.remove();
                    }
                }
            }
        }
    }

    protected String extractClassFromFileDescription() {
        String answer = getDescription();
        try {
            URI uri = new URI(answer);
            String path = uri.getPath();
            String schemeSpecific = uri.getSchemeSpecificPart();
            if (path != null && !path.isEmpty()) {
                answer = path;
            } else if (schemeSpecific != null && !schemeSpecific.isEmpty()) {
                answer = schemeSpecific;
            }
        } catch (URISyntaxException ignore) {}
        // let's strip off everything after the last '.'
        int slashIdx = answer.lastIndexOf('/');
        int separatorIdx = answer.lastIndexOf(File.separatorChar);
        int dotIdx = answer.lastIndexOf('.');
        if (dotIdx > 0 && dotIdx > Math.max(slashIdx, separatorIdx)) {
            answer = answer.substring(0, dotIdx);
        }
        // new let's strip everything up to and including the path separators
        if (slashIdx >= 0) {
            answer = answer.substring(slashIdx + 1);
        }
        // recalculate in case we have already done some stripping
        separatorIdx = answer.lastIndexOf(File.separatorChar);
        if (separatorIdx >= 0) {
            answer = answer.substring(separatorIdx + 1);
        }
        return answer;
    }

    public boolean isEmpty() {
        return classes.isEmpty() && statementBlock.getStatements().isEmpty();
    }

    public void sortClasses() {
        if (isEmpty()) return;
        List<ClassNode> sorted = new LinkedList<>(), todo = getClasses();
        int level = 1;
        while (!todo.isEmpty()) {
            for (Iterator<ClassNode> it = todo.iterator(); it.hasNext(); ) {
                ClassNode cn = it.next(), sc = cn;

                for (int i = 0; sc != null && i < level; i += 1) sc = sc.getSuperClass();
                if (sc != null && sc.isPrimaryClassNode()) continue;
                sorted.add(cn);
                it.remove();
            }
            level += 1;
        }
        this.classes = sorted;
    }

    public boolean hasImportsResolved() {
        return importsResolved;
    }

    public void setImportsResolved(final boolean importsResolved) {
        this.importsResolved = importsResolved;
    }

    // This method only exists as a workaround for GROOVY-6094
    // In order to keep binary compatibility
    private void storeLastAddedImportNode(final ImportNode node) {
        if (getNodeMetaData(ImportNode.class) == ImportNode.class) {
            putNodeMetaData(ImportNode.class, node);
        }
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public BlockStatement getStatementBlock() {
        return statementBlock;
    }
}
