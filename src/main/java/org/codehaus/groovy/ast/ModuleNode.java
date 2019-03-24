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
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a module, which consists typically of a class declaration
 * but could include some imports, some statements and multiple classes
 * intermixed with statements like scripts in Python or Ruby
 */
public class ModuleNode extends ASTNode implements Opcodes {

    private final BlockStatement statementBlock = new BlockStatement();
    List<ClassNode> classes = new LinkedList<ClassNode>();
    private final List<MethodNode> methods = new ArrayList<MethodNode>();
    private final Map<String, ImportNode> imports = new HashMap<String, ImportNode>();
    private final List<ImportNode> starImports = new ArrayList<ImportNode>();
    private final Map<String, ImportNode> staticImports = new LinkedHashMap<String, ImportNode>();
    private final Map<String, ImportNode> staticStarImports = new LinkedHashMap<String, ImportNode>();
    private CompileUnit unit;
    private PackageNode packageNode;
    private String description;
    private boolean createClassForStatements = true;
    private transient SourceUnit context;
    private boolean importsResolved = false;
    private ClassNode scriptDummy;
    private String mainClassName = null;
    private final Parameter[] SCRIPT_CONTEXT_CTOR = {new Parameter(ClassHelper.BINDING_TYPE, "context")};

    public ModuleNode (SourceUnit context ) {
        this.context = context;
    }

    public ModuleNode (CompileUnit unit) {
        this.unit = unit;
    }

    public BlockStatement getStatementBlock() {
        return statementBlock;
    }

    public List<MethodNode> getMethods() {
        return methods;
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
        return classes;
    }

    private boolean isPackageInfo() {
        return context != null && context.getName() != null && context.getName().endsWith("package-info.groovy");
    }

    public List<ImportNode> getImports() {
        return new ArrayList<ImportNode>(imports.values());
    }

    public List<ImportNode> getStarImports() {
        return starImports;
    }

    /**
     * @param alias the name of interest
     * @return the class node for the given alias or null if none is available
     */
    public ClassNode getImportType(String alias) {
        ImportNode importNode = imports.get(alias);
        return importNode == null ? null : importNode.getType();
    }

    /**
     * @param alias the name of interest
     * @return the import node for the given alias or null if none is available
     */
    public ImportNode getImport(String alias) {
        return imports.get(alias);
    }

    public void addImport(String alias, ClassNode type) {
        addImport(alias, type, new ArrayList<AnnotationNode>());
    }

    public void addImport(String alias, ClassNode type, List<AnnotationNode> annotations) {
        ImportNode importNode = new ImportNode(type, alias);
        imports.put(alias, importNode);
        importNode.addAnnotations(annotations);
        storeLastAddedImportNode(importNode);
    }

    public void addStarImport(String packageName) {
        addStarImport(packageName, new ArrayList<AnnotationNode>());
    }

    public void addStarImport(String packageName, List<AnnotationNode> annotations) {
        ImportNode importNode = new ImportNode(packageName);
        importNode.addAnnotations(annotations);
        starImports.add(importNode);
        storeLastAddedImportNode(importNode);
    }

    public void addStatement(Statement node) {
        statementBlock.addStatement(node);
    }

    public void addClass(ClassNode node) {
        if(classes.isEmpty()) mainClassName = node.getName();
        classes.add(node);
        node.setModule(this);
        addToCompileUnit(node);
    }

    private void addToCompileUnit(ClassNode node) {
        // register the new class with the compile unit
        if (unit != null) {
            unit.addClass(node);
        }
    }

    public void addMethod(MethodNode node) {
        methods.add(node);
    }

    public void accept(GroovyCodeVisitor visitor) {
    }

    public String getPackageName() {
        return packageNode == null ? null : packageNode.getName();
    }

    public PackageNode getPackage() {
        return packageNode;
    }

    // TODO don't allow override?
    public void setPackage(PackageNode packageNode) {
        this.packageNode = packageNode;
    }

    // TODO don't allow override?
    public void setPackageName(String packageName) {
        this.packageNode = new PackageNode(packageName);
    }

    public boolean hasPackageName(){
        return packageNode != null && packageNode.getName() != null;
    }

    public boolean hasPackage(){
        return this.packageNode != null;
    }

    public SourceUnit getContext() {
        return context;
    }

    /**
     * @return the underlying character stream description
     */
    public String getDescription() {
        if( context != null )
        {
            return context.getName();
        }
        else
        {
            return this.description;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CompileUnit getUnit() {
        return unit;
    }

    void setUnit(CompileUnit unit) {
        this.unit = unit;
    }

    public ClassNode getScriptClassDummy() {
        if (scriptDummy!=null) {
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

    private void setScriptBaseClassFromConfig(ClassNode cn) {
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
                new Parameter[] { new Parameter(ClassHelper.STRING_TYPE.makeArray(), "args")},
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                    new MethodCallExpression(
                        new ClassExpression(ClassHelper.make(InvokerHelper.class)),
                        "runScript",
                        new ArgumentListExpression(
                                new ClassExpression(classNode),
                                new VariableExpression("args"))))));

        MethodNode methodNode = new MethodNode("run", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statementBlock);
        methodNode.setIsScriptBody();
        classNode.addMethod(methodNode);

        classNode.addConstructor(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());

        Statement stmt;
        // A script's contextual constructor should call it's super class' contextual constructor, if it has one.
        // In practice this will always be true because currently this visitor is run before the AST transformations
        // (like @BaseScript) that could change this.  But this is cautious and anticipates possible compiler changes.
        if (classNode.getSuperClass().getDeclaredConstructor(SCRIPT_CONTEXT_CTOR) != null) {
            stmt = new ExpressionStatement(
                    new ConstructorCallExpression(ClassNode.SUPER,
                            new ArgumentListExpression(
                                    new VariableExpression("context"))));
        } else {
            // Fallback for non-standard base "script" classes with no context (Binding) constructor.
            stmt = new ExpressionStatement(
                    new MethodCallExpression(
                            new VariableExpression("super"),
                            "setBinding",
                            new ArgumentListExpression(
                                    new VariableExpression("context"))));
        }

        classNode.addConstructor(
            ACC_PUBLIC,
            new Parameter[] { new Parameter(ClassHelper.make(Binding.class), "context")},
            ClassNode.EMPTY_ARRAY,
            stmt);

        for (MethodNode node : methods) {
            int modifiers = node.getModifiers();
            if ((modifiers & ACC_ABSTRACT) != 0) {
                throw new RuntimeException(
                    "Cannot use abstract methods in a script, they are only available inside classes. Method: "
                        + node.getName());
            }
            // br: the old logic seems to add static to all def f().... in a script, which makes enclosing
            // inner classes (including closures) in a def function difficult. Comment it out.
            node.setModifiers(modifiers /*| ACC_STATIC*/);

            classNode.addMethod(node);
        }
        return classNode;
    }

    /*
     * If a main method is provided by user, account for it under run() as scripts generate their own 'main' so they can run.  
     */
    private void handleMainMethodIfPresent(List methods) {
        boolean found = false;
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode node = (MethodNode) iter.next();
            if(node.getName().equals("main")) {
                if (node.isStatic() && node.getParameters().length == 1) {
                    boolean retTypeMatches, argTypeMatches;
                    ClassNode argType = node.getParameters()[0].getType();
                    ClassNode retType = node.getReturnType();

                    argTypeMatches = (argType.equals(ClassHelper.OBJECT_TYPE) || argType.getName().contains("String[]"));
                    retTypeMatches = (retType == ClassHelper.VOID_TYPE || retType == ClassHelper.OBJECT_TYPE);
                    
                    if(retTypeMatches && argTypeMatches) {
                        if(found) {
                            throw new RuntimeException("Repetitive main method found.");
                        } else {
                            found = true;
                        }
                        // if script has both loose statements as well as main(), then main() is ignored
                        if(statementBlock.isEmpty()) {
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
            if (path!=null) {
                answer = path;
            } else if (schemeSpecific!=null) {
                answer = schemeSpecific;
            }
        } catch (URISyntaxException e) {}
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
    
    public void sortClasses(){
        if (isEmpty()) return;
        List<ClassNode> classes = getClasses();
        LinkedList<ClassNode> sorted = new LinkedList<ClassNode>();
        int level=1;
        while (!classes.isEmpty()) {
            for (Iterator<ClassNode> cni = classes.iterator(); cni.hasNext();) {
                ClassNode cn = cni.next();
                ClassNode sn = cn;
                for (int i=0; sn!=null && i<level; i++) sn = sn.getSuperClass();
                if (sn!=null && sn.isPrimaryClassNode()) continue;
                cni.remove();
                sorted.addLast(cn);
            }
            level++;
        }
        this.classes = sorted;
    }

    public boolean hasImportsResolved() {
        return importsResolved;
    }

    public void setImportsResolved(boolean importsResolved) {
        this.importsResolved = importsResolved;
    }

    public Map<String, ImportNode> getStaticImports() {
        return staticImports;
    }

    public Map<String, ImportNode> getStaticStarImports() {
        return staticStarImports;
    }

    public void addStaticImport(ClassNode type, String fieldName, String alias) {
        addStaticImport(type, fieldName, alias, new ArrayList<AnnotationNode>());
    }

    public void addStaticImport(ClassNode type, String fieldName, String alias, List<AnnotationNode> annotations) {
        ImportNode node = new ImportNode(type, fieldName, alias);
        node.addAnnotations(annotations);
        staticImports.put(alias, node);
        storeLastAddedImportNode(node);
    }

    public void addStaticStarImport(String name, ClassNode type) {
        addStaticStarImport(name, type, new ArrayList<AnnotationNode>());
    }

    public void addStaticStarImport(String name, ClassNode type, List<AnnotationNode> annotations) {
        ImportNode node = new ImportNode(type);
        node.addAnnotations(annotations);
        staticStarImports.put(name, node);
        storeLastAddedImportNode(node);
    }

    // This method only exists as a workaround for GROOVY-6094
    // In order to keep binary compatibility
    private void storeLastAddedImportNode(final ImportNode node) {
        if (getNodeMetaData(ImportNode.class)==ImportNode.class) {
            putNodeMetaData(ImportNode.class, node);
        }
    }

    public String getMainClassName() {
        return mainClassName;
    }
}
