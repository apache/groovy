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

    /**
     * @return the module's methods
     */
    public List<MethodNode> getMethods() {
        return methods;
    }

    /**
     * @return a copy of the module's imports
     */
    public List<ImportNode> getImports() {
        return new ArrayList<>(imports);
    }

    /**
     * @return the module's star imports
     */
    public List<ImportNode> getStarImports() {
        return starImports;
    }

    /**
     * @return the module's static imports
     */
    public Map<String, ImportNode> getStaticImports() {
        return staticImports;
    }

    /**
     * @return the module's static star imports
     */
    public Map<String, ImportNode> getStaticStarImports() {
        return staticStarImports;
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
        Map<String, ImportNode> aliases = getNodeMetaData("import.aliases", x ->
            imports.stream().collect(Collectors.toMap(ImportNode::getAlias, n -> n, (n, m) -> m)));
        return aliases.get(alias);
    }

    public void addImport(final String name, final ClassNode type) {
        addImport(name, type, Collections.emptyList());
    }

    public void addImport(final String name, final ClassNode type, final List<AnnotationNode> annotations) {
        checkUsage(name, type); // GROOVY-8254

        ImportNode importNode = new ImportNode(type, name);
        importNode.addAnnotations(annotations);
        imports.add(importNode);

        removeNodeMetaData("import.aliases");
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

    public void addStaticImport(final ClassNode type, final String memberName, final String simpleName) {
        addStaticImport(type, memberName, simpleName, Collections.emptyList());
    }

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
            if (!(statement instanceof ExpressionStatement)) {
                hasUncontainedStatements = true;
                break;
            }
            ExpressionStatement es = (ExpressionStatement) statement;
            Expression expression = es.getExpression();
            if (!(expression instanceof DeclarationExpression)) {
                hasUncontainedStatements = true;
                break;
            }
            DeclarationExpression de = (DeclarationExpression) expression;
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
            if (node.getName().equals("run") && node.getParameters().length == 0) {
                return node;
            }
        }
        return null;
    }

    /*
     * We retain the 'main' method if a compatible one is found.
     * A compatible one has no parameters or 1 (Object or String[]) parameter.
     * The return type must be void or Object.
     */
    private MethodNode handleMainMethodIfPresent(final List<MethodNode> methods) {
        boolean foundInstance = false;
        boolean foundStatic = false;
        MethodNode result = null;
        for (Iterator<MethodNode> iter = methods.iterator(); iter.hasNext(); ) {
            MethodNode node = iter.next();
            if (node.getName().equals("main") && !node.isPrivate()) {
                int numParams = node.getParameters().length;
                if (numParams < 2) {
                    ClassNode argType = numParams > 0 ? node.getParameters()[0].getType() : null;
                    ClassNode retType = node.getReturnType();

                    boolean argTypeMatches = argType == null || argType.getNameWithoutPackage().equals("Object") || (argType.isArray() && argType.getComponentType().getNameWithoutPackage().equals("String"));
                    boolean retTypeMatches = ClassHelper.isPrimitiveVoid(retType) || retType.getNameWithoutPackage().equals("Object");
                    if (retTypeMatches && argTypeMatches) {
                        if (node.isStatic() ? foundStatic : foundInstance) {
                            throw new RuntimeException("Repetitive main method found.");
                        }
                        if (!foundStatic) { // static trumps instance
                            result = node;
                        }

                        if (node.isStatic()) foundStatic = true;
                        else foundInstance = true;
                    }
                }
            }
        }
        return result;
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
