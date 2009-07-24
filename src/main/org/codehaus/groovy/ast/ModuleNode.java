/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast;

import groovy.lang.Binding;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.util.*;

/**
 * Represents a module, which consists typically of a class declaration
 * but could include some imports, some statements and multiple classes
 * intermixed with statements like scripts in Python or Ruby
 *
 * @author Jochen Theodorou
 * @author Paul King
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class ModuleNode extends ASTNode implements Opcodes {

    private BlockStatement statementBlock = new BlockStatement();
    List<ClassNode> classes = new LinkedList<ClassNode>();
    private List<MethodNode> methods = new ArrayList<MethodNode>();
    private Map<String, ImportNode> imports = new HashMap<String, ImportNode>();
    private List<ImportNode> starImports = new ArrayList<ImportNode>();
    private Map<String, ImportNode> staticImports = new LinkedHashMap<String, ImportNode>();
    private Map<String, ImportNode> staticStarImports = new LinkedHashMap<String, ImportNode>();
    private CompileUnit unit;
    private PackageNode packageNode;
    private String description;
    private boolean createClassForStatements = true;
    private transient SourceUnit context;
    private boolean importsResolved = false;
    private ClassNode scriptDummy;

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

    /**
     * @deprecated replaced by {@link #getStarImports()}
     */
    @Deprecated
    public List<String> getImportPackages() {
        List<String> result = new ArrayList<String>();
        for (ImportNode importStarNode : starImports) {
            result.add(importStarNode.getPackageName());
        }
        return result;
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
    }

    /**
     * @deprecated replaced by {@link #addStarImport(String)}
     */
    @Deprecated
    public String[] addImportPackage(String packageName) {
        addStarImport(packageName);
        return new String[]{};
    }

    public void addStarImport(String packageName) {
        addStarImport(packageName, new ArrayList<AnnotationNode>());
    }

    public void addStarImport(String packageName, List<AnnotationNode> annotations) {
        ImportNode importNode = new ImportNode(packageName);
        importNode.addAnnotations(annotations);
        starImports.add(importNode);
    }

    public void addStatement(Statement node) {
        statementBlock.addStatement(node);
    }

    public void addClass(ClassNode node) {
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

    public void visit(GroovyCodeVisitor visitor) {
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
        // DEPRECATED -- context.getName() is now sufficient
        // TODO add deprecated annotation or javadoc comment?
        this.description = description;
    }

    public CompileUnit getUnit() {
        return unit;
    }

    void setUnit(CompileUnit unit) {
        this.unit = unit;
    }

    public ClassNode getScriptClassDummy() {
        if (scriptDummy!=null) return scriptDummy;
        
        String name = getPackageName();
        if (name == null) {
            name = "";
        }
        // now let's use the file name to determine the class name
        if (getDescription() == null) {
            throw new RuntimeException("Cannot generate main(String[]) class for statements when we have no file description");
        }
        name += extractClassFromFileDescription();

        String baseClassName = null;
        if (unit != null) baseClassName = unit.getConfig().getScriptBaseClass();
        ClassNode baseClass = null;
        if (baseClassName!=null) {
            baseClass = ClassHelper.make(baseClassName);
        }
        if (baseClass == null) {
            baseClass = ClassHelper.SCRIPT_TYPE;
        }
        ClassNode classNode;
        if (isPackageInfo()) {
            classNode = new ClassNode(name, ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        } else {
            classNode = new ClassNode(name, ACC_PUBLIC, baseClass);
            classNode.setScript(true);
            classNode.setScriptBody(true);
        }

        scriptDummy = classNode;
        return classNode;
    }
    
    protected ClassNode createStatementsClass() {
        ClassNode classNode = getScriptClassDummy();
        if (classNode.getName().endsWith("package-info")) {
            return classNode;
        }
        
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

        classNode.addMethod(
            new MethodNode("run", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statementBlock));

        classNode.addConstructor(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
        Statement stmt = new ExpressionStatement(
                        new MethodCallExpression(
                            new VariableExpression("super"),
            				"setBinding",
            				new ArgumentListExpression(
                                        new VariableExpression("context"))));

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

    protected String extractClassFromFileDescription() {
        // let's strip off everything after the last '.'
        String answer = getDescription();
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

    /**
     * @deprecated replaced by {@link #getStaticImports()}
     */
    @Deprecated
    public Map<String, ClassNode> getStaticImportAliases() {
        Map<String, ClassNode> result = new HashMap<String, ClassNode>();
        for (Map.Entry<String, ImportNode> entry : staticImports.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getType());
        }
        return result;
    }

    /**
     * @deprecated replaced by {@link #getStaticStarImports()}
     */
    @Deprecated
    public Map<String, ClassNode> getStaticImportClasses() {
        Map<String, ClassNode> result = new HashMap<String, ClassNode>();
        for (Map.Entry<String, ImportNode> entry : staticStarImports.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getType());
        }
        return result;
    }

    /**
     * @deprecated replaced by {@link #getStaticImports()}
     */
    @Deprecated
    public Map<String, String> getStaticImportFields() {
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, ImportNode> entry : staticImports.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getFieldName());
        }
        return result;
    }

    public Map<String, ImportNode> getStaticImports() {
        return staticImports;
    }

    public Map<String, ImportNode> getStaticStarImports() {
        return staticStarImports;
    }

    /**
     * @deprecated replaced by {@link #addStaticImport(ClassNode, String, String)}
     */
    @Deprecated
    public void addStaticMethodOrField(ClassNode type, String fieldName, String alias) {
        addStaticImport(type, fieldName, alias);
    }

    public void addStaticImport(ClassNode type, String fieldName, String alias) {
        addStaticImport(type, fieldName, alias, new ArrayList<AnnotationNode>());
    }

    public void addStaticImport(ClassNode type, String fieldName, String alias, List<AnnotationNode> annotations) {
        ImportNode node = new ImportNode(type, fieldName, alias);
        node.addAnnotations(annotations);
        staticImports.put(alias, node);
    }

    /**
     * @deprecated replaced by {@link #addStaticStarImport(String, ClassNode)}
     */
    @Deprecated
    public void addStaticImportClass(String name, ClassNode type) {
        addStaticStarImport(name, type);
    }

    public void addStaticStarImport(String name, ClassNode type) {
        addStaticStarImport(name, type, new ArrayList<AnnotationNode>());
    }

    public void addStaticStarImport(String name, ClassNode type, List<AnnotationNode> annotations) {
        ImportNode node = new ImportNode(type);
        node.addAnnotations(annotations);
        staticStarImports.put(name, node);
    }
}
