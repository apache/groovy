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

import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.BaseScriptASTTransformation;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Represents a module, which consists typically of a class declaration
 * but could include some imports, some statements and multiple classes
 * intermixed with statements like scripts in Python or Ruby
 */
public class ModuleNode extends ASTNode {

    private List<ClassNode> classes = new LinkedList<>();
    private final List<MethodNode> methods = new ArrayList<>();
    private final List<ImportNode> imports = new ArrayList<>();
    private final List<ImportNode> starImports = new ArrayList<>();
    private final List<ImportNode> moduleStarImports = new ArrayList<>();
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

    /**
     * Creates a module node for the specified compilation context.
     * Used when compiling a single Groovy file with full source location tracking.
     *
     * @param context the source unit providing file path, line mapping, and error context
     */
    public ModuleNode(final SourceUnit context) {
        this.context = context;
    }

    /**
     * Creates a module node for batch compilation.
     * Used when compiling multiple classes in a single compilation batch.
     *
     * @param unit the compile unit managing this module's classes
     */
    public ModuleNode(final CompileUnit unit) {
        this.unit = unit;
    }

    /**
     * Returns the module's class definitions.
     * If {@code createClassForStatements} is true and there are module-level statements,
     * methods, or this is a package-info script, a synthetic class is created to wrap them.
     * This class is automatically inserted at the beginning of the list.
     * The flag is then reset to prevent future synthesis.
     *
     * @return list of {@link ClassNode} definitions (may include synthetic wrapper for statements)
     */
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

    /**
     * Returns the module's top-level method definitions.
     * These are module-level methods typically found in scripts or trait interfaces.
     *
     * @return list of {@link MethodNode} definitions
     */
    public List<MethodNode> getMethods() {
        return methods;
    }

    /**
     * Returns a copy of the module's regular import declarations.
     * Includes both simple class imports (e.g., {@code import java.util.List})
     * and aliased imports. Does not include star imports or static imports.
     *
     * @return a copy of the {@link ImportNode} list for regular imports
     */
    public List<ImportNode> getImports() {
        return new ArrayList<>(imports);
    }

    /**
     * Returns wildcard (star) import declarations (e.g., {@code import java.util.*}).
     * These imports make all public classes in a package available without qualification.
     *
     * @return list of {@link ImportNode} for wildcard imports
     */
    public List<ImportNode> getStarImports() {
        return starImports;
    }

    /**
     * Returns static import declarations (e.g., {@code import static java.lang.Math.PI}).
     * Maps simple names to {@link ImportNode} objects for static member imports.
     *
     * @return map from simple name to {@link ImportNode} for static imports
     */
    public Map<String, ImportNode> getStaticImports() {
        return staticImports;
    }

    /**
     * Returns static wildcard import declarations (e.g., {@code import static java.lang.Math.*}).
     * Maps package names to {@link ImportNode} objects for static star imports.
     *
     * @return map from package name to {@link ImportNode} for static star imports
     */
    public Map<String, ImportNode> getStaticStarImports() {
        return staticStarImports;
    }

    /**
     * Looks up the class type for a regular import by its alias name.
     * Resolves simple names to the actual class they reference.
     *
     * @param alias the imported name to look up
     * @return the {@link ClassNode} for that import, or null if not found
     */
    public ClassNode getImportType(final String alias) {
        ImportNode node = getImport(alias);
        return (node != null ? node.getType() : null);
    }

    /**
     * Looks up an import node by its alias name.
     * Searches regular imports and returns the matching node.
     * Caches import alias mappings as node metadata for performance.
     *
     * @param alias the imported name to look up
     * @return the {@link ImportNode} for that import, or null if not found
     */
    public ImportNode getImport(final String alias) {
        Map<String, ImportNode> aliases = getNodeMetaData("import.aliases", x ->
            imports.stream().collect(Collectors.toMap(ImportNode::getAlias, n -> n, (n, m) -> m)));
        return aliases.get(alias);
    }

    /**
     * Registers a regular import (e.g., {@code import java.util.List}).
     * The alias name will be used to reference the imported class.
     *
     * @param name the alias name for this import
     * @param type the {@link ClassNode} to import
     * @throws SyntaxException if the name conflicts with an existing declaration
     */
    public void addImport(final String name, final ClassNode type) {
        addImport(name, type, Collections.emptyList());
    }

    /**
     * Registers a regular import with optional annotations.
     * The alias name will be used to reference the imported class.
     *
     * @param name the alias name for this import
     * @param type the {@link ClassNode} to import
     * @param annotations annotations to attach to this import
     * @throws SyntaxException if the name conflicts with an existing declaration
     */
    public void addImport(final String name, final ClassNode type, final List<AnnotationNode> annotations) {
        checkUsage(name, type); // GROOVY-8254

        ImportNode importNode = new ImportNode(type, name);
        importNode.addAnnotations(annotations);
        imports.add(importNode);

        removeNodeMetaData("import.aliases");
        storeLastAddedImportNode(importNode);
    }

    /**
     * Registers a wildcard import (e.g., {@code import java.util.*}).
     * All public classes in the package become available without qualification.
     *
     * @param packageName the package name (e.g., "java.util")
     */
    public void addStarImport(final String packageName) {
        addStarImport(packageName, Collections.emptyList());
    }

    /**
     * Registers a wildcard import with optional annotations.
     * All public classes in the package become available without qualification.
     *
     * @param packageName the package name (e.g., "java.util")
     * @param annotations annotations to attach to this import
     */
    public void addStarImport(final String packageName, final List<AnnotationNode> annotations) {
        ImportNode importNode = new ImportNode(packageName);
        importNode.addAnnotations(annotations);
        starImports.add(importNode);

        storeLastAddedImportNode(importNode);
    }

    /**
     * Registers a module-level star import (e.g., {@code import module java.base}).
     * Returns module star imports, which are separate from regular wildcard imports
     * for proper JLS 6.4.1 shadowing resolution (type-import-on-demand shadowing).
     *
     * @return star imports from {@code import module} declarations
     * @since 6.0.0
     */
    public List<ImportNode> getModuleStarImports() {
        return moduleStarImports;
    }

    /**
     * Adds a module-level star import with optional annotations.
     * These are tracked separately from regular wildcard imports for import resolution.
     *
     * @param packageName the module package name
     * @param annotations annotations to attach to this import
     *
     * @since 6.0.0
     */
    public void addModuleStarImport(final String packageName, final List<AnnotationNode> annotations) {
        ImportNode importNode = new ImportNode(packageName);
        importNode.addAnnotations(annotations);
        moduleStarImports.add(importNode);

        storeLastAddedImportNode(importNode);
    }

    /**
     * Registers a static import (e.g., {@code import static java.lang.Math.PI}).
     * A specific static member from a class becomes available without qualification.
     *
     * @param type the {@link ClassNode} containing the static member
     * @param memberName the name of the static member (field or method)
     * @param simpleName the alias name by which this member is imported
     * @throws SyntaxException if the simpleName conflicts with existing declarations
     */
    public void addStaticImport(final ClassNode type, final String memberName, final String simpleName) {
        addStaticImport(type, memberName, simpleName, Collections.emptyList());
    }

    /**
     * Registers a static import with optional annotations.
     * A specific static member from a class becomes available without qualification.
     *
     * @param type the {@link ClassNode} containing the static member
     * @param memberName the name of the static member (field or method)
     * @param simpleName the alias name by which this member is imported
     * @param annotations annotations to attach to this import
     * @throws SyntaxException if the simpleName conflicts with existing declarations
     */
    public void addStaticImport(final ClassNode type, final String memberName, final String simpleName, final List<AnnotationNode> annotations) {
        ClassNode memberType = new ClassNode(type.getName() + '.' + memberName, 0, null) {
            @Override public ClassNode getOuterClass() { return type; }
        };
        memberType.setSourcePosition(type);
        checkUsage(simpleName, memberType);

        ImportNode node = new ImportNode(type, memberName, simpleName);
        node.addAnnotations(annotations);
        ImportNode prev = staticImports.put(simpleName, node);
        if (prev != null) {
            staticImports.put(prev.toString(), prev);
            staticImports.put(simpleName, staticImports.remove(simpleName));
        }

        storeLastAddedImportNode(node);
    }

    /**
     * Registers a static wildcard import (e.g., {@code import static java.lang.Math.*}).
     * All static members from a class become available without qualification.
     *
     * @param name the package/class name for this static star import
     * @param type the {@link ClassNode} from which to import static members
     */
    public void addStaticStarImport(final String name, final ClassNode type) {
        addStaticStarImport(name, type, Collections.emptyList());
    }

    /**
     * Registers a static wildcard import with optional annotations.
     * All static members from a class become available without qualification.
     *
     * @param name the package/class name for this static star import
     * @param type the {@link ClassNode} from which to import static members
     * @param annotations annotations to attach to this import
     */
    public void addStaticStarImport(final String name, final ClassNode type, final List<AnnotationNode> annotations) {
        ImportNode node = new ImportNode(type);
        node.addAnnotations(annotations);
        staticStarImports.put(name, node);

        storeLastAddedImportNode(node);
    }

    /**
     * Adds a statement to the module's statement block.
     * Module-level statements are collected and later wrapped in a synthetic class
     * during compilation (controlled by {@link #createClassForStatements}).
     *
     * @param node the {@link Statement} to add
     */
    public void addStatement(final Statement node) {
        statementBlock.addStatement(node);
    }

    /**
     * Adds a class definition to this module.
     * If this is the first class added, its name is recorded as the main class name.
     * The class's containing module is set to this module, and it's registered
     * with the compile unit if present.
     *
     * @param node the {@link ClassNode} to add
     * @throws SyntaxException if a class with the same name already exists in this module
     */
    public void addClass(final ClassNode node) {
        if (classes.isEmpty())
            mainClassName = node.getName();
        classes.add(node);
        node.setModule(this);
        addToCompileUnit(node);
        checkUsage(node.getNameWithoutPackage(), node);
    }

    private void addToCompileUnit(final ClassNode node) {
        // register the new class with the compile unit
        if (unit != null) {
            unit.addClass(node);
        }
    }

    private void checkUsage(final String name, final ClassNode type) {
        for (ClassNode node : classes) {
            if (node.getNameWithoutPackage().equals(name) && !node.equals(type)) {
                getContext().addErrorAndContinue(new SyntaxException("The name " + name + " is already declared", type));
                return;
            }
        }

        for (ImportNode node : imports) {
            if (node.getAlias().equals(name) && !node.getType().equals(type)) {
                getContext().addErrorAndContinue(new SyntaxException("The name " + name + " is already declared", type));
                return;
            }
        }

        {
            ImportNode node = staticImports.get(name);
            if (node != null && !node.getType().equals(type.getOuterClass())) {
                getContext().addErrorAndContinue(new SyntaxException("The name " + name + " is already declared", type));
            }
        }
    }

    /**
     * Adds a module-level method definition.
     * Module-level methods are typically found in scripts or in trait interface definitions.
     *
     * @param node the {@link MethodNode} to add
     */
    public void addMethod(final MethodNode node) {
        methods.add(node);
    }

    /**
     * Accepts a code visitor for AST traversal and processing.
     * Implementation is empty; module-level visitation typically proceeds directly
     * to classes and methods contained within.
     *
     * @param visitor the {@link GroovyCodeVisitor} to process this node
     */
    @Override
    public void visit(final GroovyCodeVisitor visitor) {
    }

    /**
     * Returns the module's package name.
     * Returns null if no package is declared (uses the default package).
     *
     * @return the fully qualified package name, or null for the default package
     */
    public String getPackageName() {
        return packageNode == null ? null : packageNode.getName();
    }

    /**
     * Returns the package node for this module.
     * Contains the package declaration and associated annotations.
     *
     * @return the {@link PackageNode}, or null if none is declared
     */
    public PackageNode getPackage() {
        return packageNode;
    }

    /**
     * Checks whether this module declares a package.
     *
     * @return true if a package is declared, false for the default package
     */
    public boolean hasPackage() {
        return (packageNode != null);
    }

    /**
     * Sets the package declaration for this module.
     *
     * @param packageNode the {@link PackageNode} for this module
     */
    public void setPackage(final PackageNode packageNode) {
        this.packageNode = packageNode;
    }

    /**
     * Checks whether this module has a named package (not default package).
     *
     * @return true if a non-null package name is declared
     */
    public boolean hasPackageName() {
        return (packageNode != null && packageNode.getName() != null);
    }

    /**
     * Sets the package for this module by name.
     * Creates a new {@link PackageNode} with the given package name.
     *
     * @param packageName the fully qualified package name
     */
    public void setPackageName(final String packageName) {
        setPackage(new PackageNode(packageName));
    }

    /**
     * Returns a description of this module.
     * Typically the source file name if available (via context), otherwise a user-supplied description.
     *
     * @return the module description (usually the file path), or null if not set
     */
    public String getDescription() {
        if (context != null) {
            return context.getName();
        } else {
            return description;
        }
    }

    /**
     * Sets a description for this module (typically the source file name).
     *
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the compile unit managing this module (batch compilation context).
     * Null if this module was compiled individually.
     *
     * @return the {@link CompileUnit}, or null for single-file compilation
     */
    public CompileUnit getUnit() {
        return unit;
    }

    void setUnit(final CompileUnit unit) {
        this.unit = unit;
    }

    /**
     * Returns the source unit for this module (single-file compilation context).
     * Provides access to source code, line mappings, and error reporting.
     *
     * @return the {@link SourceUnit}, or null if compiled as part of a batch
     */
    public SourceUnit getContext() {
        return context;
    }

    private boolean isPackageInfo() {
        return context != null && context.getName() != null && context.getName().endsWith("package-info.groovy");
    }

    /**
     * Returns a synthetic class wrapping this module's statements and methods.
     * Used for scripts: module-level code is collected and placed in a generated class
     * with a {@code main(String[])} method. Configures the base class based on compiler config.
     *
     * @return a {@link ClassNode} for script execution
     * @throws RuntimeException if module description is not set
     */
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
        ClassLoader bcLoader = null;
        if (unit != null) {
            bcLoader = unit.getClassLoader();
            baseClassName = unit.getConfig().getScriptBaseClass();
        } else if (context != null) {
            bcLoader = context.getClassLoader();
            baseClassName = context.getConfiguration().getScriptBaseClass();
        }
        if (baseClassName != null && !cn.getSuperClass().getName().equals(baseClassName)) {
            cn.addAnnotation(new AnnotationNode(BaseScriptASTTransformation.MY_TYPE));
            try { // GROOVY-8096
                cn.setSuperClass(ClassHelper.make(bcLoader.loadClass(baseClassName)));
            } catch (ReflectiveOperationException | RuntimeException e) {
                cn.setSuperClass(ClassHelper.make(baseClassName));
            }
        }
    }

    private static Parameter[] finalParam(final ClassNode type, final String name) {
        Parameter parameter = param(type, name);
        parameter.setModifiers(ACC_FINAL);
        return params(parameter);
    }

    /**
     * Creates a synthetic class wrapping this module's statement and method definitions.
     * Generates a {@code main(String[])} method for script execution and a {@code run()} method for statement execution.
     * Only invoked when the module contains module-level code (scripts or statements).
     *
     * @return a {@link ClassNode} representing the synthetic wrapper class with generated methods
     */
    protected ClassNode createStatementsClass() {
        ClassNode classNode = getScriptClassDummy();
        if (classNode.getName().endsWith("package-info")) {
            return classNode;
        }

        MethodNode existingMain = handleMainMethodIfPresent(methods);

        boolean hasUncontainedStatements = false;
        List<FieldNode> fields = new ArrayList<>();
        // check for uncontained statements (excluding decl statements)
        for (Statement statement : statementBlock.getStatements()) {
            if (!(statement instanceof ExpressionStatement es)) {
                hasUncontainedStatements = true;
                break;
            }
            Expression expression = es.getExpression();
            if (!(expression instanceof DeclarationExpression de)) {
                hasUncontainedStatements = true;
                break;
            }
            if (de.isMultipleAssignmentDeclaration()) {
                List<Expression> variables = de.getTupleExpression().getExpressions();
                if (!(de.getRightExpression() instanceof ListExpression)) break;
                List<Expression> values = ((ListExpression)de.getRightExpression()).getExpressions();
                for (int i = 0; i < variables.size(); i++) {
                    VariableExpression var = (VariableExpression) variables.get(i);
                    Expression val = i >= values.size() ? null : values.get(i);
                    fields.add(new FieldNode(var.getName(), var.getModifiers(), var.getType(), null, val));
                }
            } else {
                VariableExpression ve = de.getVariableExpression();
                fields.add(new FieldNode(ve.getName(), ve.getModifiers(), ve.getType(), null, de.getRightExpression()));
            }
        }

        if (existingMain != null && !hasUncontainedStatements) { // JEP 445 main
            ClassNode result = new ClassNode(classNode.getName(), 0, ClassHelper.OBJECT_TYPE);
            result.addAnnotations(existingMain.getAnnotations());
            result.setScriptBody(false);
            result.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
            existingMain.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
            methods.forEach(result::addMethod);
            fields.forEach(result::addField);
            return result;
        }

        MethodNode main = new MethodNode(
            "main",
            ACC_PUBLIC | ACC_STATIC,
            ClassHelper.VOID_TYPE,
            finalParam(ClassHelper.STRING_TYPE.makeArray(), "args"),
            ClassNode.EMPTY_ARRAY,
            stmt(
                callX(
                    ClassHelper.make(InvokerHelper.class),
                    "runScript",
                    args(classX(classNode), varX("args"))
                )
            )
        );
        main.setIsScriptBody();
        ClassNodeUtils.addGeneratedMethod(classNode, main, true);

        // we add the run method unless we find a no-arg instance run method
        // and there are no uncontained statements
        MethodNode existingRun = hasUncontainedStatements ? null : findRun();
        if (existingRun == null) {
            MethodNode methodNode = new MethodNode("run", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statementBlock);
            methodNode.setIsScriptBody();
            if (existingMain != null) {
                methodNode.addAnnotations(existingMain.getAnnotations());
            }
            ClassNodeUtils.addGeneratedMethod(classNode, methodNode, true);

            new CodeVisitorSupport() {
                @Override
                public void visitConstructorCallExpression(final ConstructorCallExpression cce) {
                    if (cce.isUsingAnonymousInnerClass()) { // GROOVY-11846
                        cce.getType().setEnclosingMethod(methodNode);
                    }
                    super.visitConstructorCallExpression(cce);
                }
            }
            .visit(statementBlock);
        } else {
            fields.forEach(classNode::addField);
            classNode.addAnnotations(existingRun.getAnnotations());
            classNode.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
            existingRun.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
        }

        classNode.addConstructor(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());

        Statement stmt;
        // A script's contextual constructor should call its super class' contextual constructor, if it has one.
        // In practice this will always be true because currently this visitor is run before the AST transformations
        // (like @BaseScript) that could change this.  But this is cautious and anticipates possible compiler changes.
        if (classNode.getSuperClass().getDeclaredConstructor(params(param(ClassHelper.BINDING_TYPE, "context"))) != null) {
            stmt = stmt(ctorSuperX(args(varX("context"))));
        } else {
            // Fallback for non-standard base "script" classes with no context (Binding) constructor.
            stmt = stmt(callX(varX("super"), "setBinding", args(varX("context"))));
        }

        classNode.addConstructor(ACC_PUBLIC, finalParam(ClassHelper.BINDING_TYPE, "context"), ClassNode.EMPTY_ARRAY, stmt);

        for (MethodNode method : methods) {
            if (method.isAbstract()) {
                throw new RuntimeException("Cannot use abstract methods in a script" +
                    ", they are only available inside classes. Method: " + method.getName());
            }
            classNode.addMethod(method);
        }
        return classNode;
    }

    private MethodNode findRun() {
        for (MethodNode node : methods) {
            if ("run".equals(node.getName()) && node.getParameters().length == 0) {
                return node;
            }
        }
        return null;
    }

    /*
     * We retain the 'main' method if a compatible one is found.
     * A compatible one has no parameters or 1 (Object or String[]) parameter.
     * The return type must be void or Object.
     * When multiple valid main methods exist, a warning is issued for those
     * that would not be reachable from the command-line runner.
     * Priority follows JEP-512: static before instance, args before no-args.
     */
    private MethodNode handleMainMethodIfPresent(final List<MethodNode> methods) {
        boolean foundInstance = false;
        boolean foundStatic = false;
        List<MethodNode> validMains = new ArrayList<>();
        for (MethodNode node : methods) {
            if ("main".equals(node.getName()) && !node.isPrivate()) {
                int numParams = node.getParameters().length;
                if (numParams < 2) {
                    ClassNode argType = numParams > 0 ? node.getParameters()[0].getType() : null;
                    ClassNode retType = node.getReturnType();

                    boolean argTypeMatches = argType == null || "Object".equals(argType.getNameWithoutPackage()) || (argType.isArray() && "String".equals(argType.getComponentType().getNameWithoutPackage()));
                    boolean retTypeMatches = ClassHelper.isPrimitiveVoid(retType) || "Object".equals(retType.getNameWithoutPackage());
                    if (retTypeMatches && argTypeMatches) {
                        if (node.isStatic() ? foundStatic : foundInstance) {
                            throw new RuntimeException("Repetitive main method found.");
                        }
                        validMains.add(node);

                        if (node.isStatic()) foundStatic = true;
                        else foundInstance = true;
                    }
                }
            }
        }

        if (validMains.isEmpty()) return null;

        // Select winner using JEP-512 priority: static before instance, args before no-args
        validMains.sort((a, b) -> {
            if (a.isStatic() != b.isStatic()) return a.isStatic() ? -1 : 1;
            return Integer.compare(b.getParameters().length, a.getParameters().length);
        });
        MethodNode result = validMains.get(0);

        // Warn about unreachable main methods
        for (int i = 1; i < validMains.size(); i++) {
            MethodNode unreachable = validMains.get(i);
            getContext().addWarning("Method '" + unreachable.getText()
                    + "' is not reachable from the Groovy runner"
                    + " because a higher-priority main method '"
                    + result.getText() + "' exists", unreachable);
        }

        return result;
    }

    /**
     * Extracts the class name from the module's description (typically a file path).
     * Strips file extensions, path separators, and URI schemes to derive a valid class name.
     * Used when generating synthetic script wrapper classes.
     *
     * @return a valid class name derived from the description
     */
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

    /**
     * Checks whether this module is empty (no classes or statements).
     * An empty module compiles to no bytecode output.
     *
     * @return true if both class list and statement block are empty
     */
    public boolean isEmpty() {
        return classes.isEmpty() && statementBlock.getStatements().isEmpty();
    }

    /**
     * Sorts classes in dependency order based on class hierarchy.
     * Inner classes and dependent classes are ordered after their dependencies.
     * This ensures that base classes are defined before derived classes during compilation.
     * Does nothing if the module is empty or contains only one class.
     */
    public void sortClasses() {
        if (isEmpty()) return;
        List<ClassNode> classes = getClasses();
        if (classes.size() == 1) return;
        List<ClassNode> ordered = new LinkedList<>();
        int level = 1;
        while (!classes.isEmpty()) {
            for (Iterator<ClassNode> it = classes.iterator(); it.hasNext(); ) {
                ClassNode cn = it.next(), sc = cn.getSuperClass();

                for (int i = 1; i < level && sc != null; i += 1) sc = sc.getSuperClass();
                if (sc != null && sc.isPrimaryClassNode()) continue;
                ordered.add(cn);
                it.remove();
            }
            level += 1;
        }
        this.classes = ordered;
    }

    /**
     * Checks whether imports have been resolved for this module.
     * Import resolution converts import aliases to fully qualified class names and
     * processes import conflicts. This flag tracks whether that phase is complete.
     *
     * @return true if import resolution has been performed
     */
    public boolean hasImportsResolved() {
        return importsResolved;
    }

    /**
     * Marks whether imports have been resolved for this module.
     * Set to true after the import resolution phase completes.
     *
     * @param importsResolved true to mark imports as resolved
     */
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

    /**
     * Returns the simple name of the main class for this module.
     * For scripts, this is the synthetic wrapper class name.
     * For modules with classes, this is typically the first class added.
     *
     * @return the main class name, or null if not set
     */
    public String getMainClassName() {
        return mainClassName;
    }

    /**
     * Returns the block of statements defined at module scope.
     * These statements become part of the script's run() method (if module is a script)
     * or remain at module level (if module contains only classes).
     *
     * @return the {@link BlockStatement} containing module-level code
     */
    public BlockStatement getStatementBlock() {
        return statementBlock;
    }
}
