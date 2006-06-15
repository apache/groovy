/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.ast;

import groovy.lang.Binding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.objectweb.asm.Opcodes;

/**
 * Represents a module, which consists typically of a class declaration
 * but could include some imports, some statements and multiple classes
 * intermixed with statements like scripts in Python or Ruby
 *
 * @author Jochen Theodorou
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ModuleNode extends ASTNode implements Opcodes {

    private BlockStatement statementBlock = new BlockStatement();
    List classes = new LinkedList();
    private List methods = new ArrayList();
    private List imports = new ArrayList();
    private List importPackages = new ArrayList();
    private Map importIndex = new HashMap();
    private CompileUnit unit;
    private String packageName;
    private String description;
    private boolean createClassForStatements = true;
    private transient SourceUnit context;
    private boolean importsResolved = false;


    public ModuleNode (SourceUnit context ) {
        this.context = context;
    }

    public ModuleNode (CompileUnit unit) {
        this.unit = unit;
    }

    public BlockStatement getStatementBlock() {
        return statementBlock;
    }

    public List getMethods() {
        return methods;
    }

    public List getClasses() {
        if (createClassForStatements && (!statementBlock.isEmpty() || !methods.isEmpty())) {
            ClassNode mainClass = createStatementsClass();
            createClassForStatements = false;
            classes.add(0, mainClass);
            mainClass.setModule(this);
            addToCompileUnit(mainClass);
        }
        return classes;
    }

    public List getImports() {
        return imports;
    }

    public List getImportPackages() {
        return importPackages;
    }

    /**
     * @return the class name for the given alias or null if none is available
     */
    public ClassNode getImport(String alias) {
        return (ClassNode) importIndex.get(alias);
    }

    public void addImport(String alias, ClassNode type) {
        imports.add(new ImportNode(type, alias));
        importIndex.put(alias, type);
    }

    public String[]  addImportPackage(String packageName) {
        importPackages.add(packageName);
        return new String[] { /* class names, not qualified */ };
    }

    public void addStatement(Statement node) {
        statementBlock.addStatement(node);
    }

    public void addClass(ClassNode node) {
        classes.add(node);
        node.setModule(this);
        addToCompileUnit(node);
    }

    /**
     * @param node
     */
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
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public boolean hasPackageName(){
        return this.packageName != null;
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
        this.description = description;
    }

    public CompileUnit getUnit() {
        return unit;
    }

    void setUnit(CompileUnit unit) {
        this.unit = unit;
    }

    protected ClassNode createStatementsClass() {
        String name = getPackageName();
        if (name == null) {
            name = "";
        }
        // now lets use the file name to determine the class name
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
        ClassNode classNode = new ClassNode(name, ACC_PUBLIC, baseClass);
        classNode.setScript(true);
        classNode.setScriptBody(true);

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
                            new Expression[] {
                                new ClassExpression(classNode),
                                new VariableExpression("args")})))));

        classNode.addMethod(
            new MethodNode("run", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statementBlock));

        classNode.addConstructor(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
        Statement stmt = new ExpressionStatement(
                        new MethodCallExpression(
                            new VariableExpression("super"),
            				"setBinding",
            				new ArgumentListExpression(
                                    new Expression[] {
                                        new VariableExpression("context")})));

        classNode.addConstructor(
            ACC_PUBLIC,
            new Parameter[] { new Parameter(ClassHelper.make(Binding.class), "context")},
			ClassNode.EMPTY_ARRAY,
            stmt);

        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode node = (MethodNode) iter.next();
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
        // lets strip off everything after the last .
        String answer = getDescription();
        int idx = answer.lastIndexOf('.');
        if (idx > 0) {
            answer = answer.substring(0, idx);
        }
        // new lets trip the path separators
        idx = answer.lastIndexOf('/');
        if (idx >= 0) {
            answer = answer.substring(idx + 1);
        }
        idx = answer.lastIndexOf(File.separatorChar);
        if (idx >= 0) {
            answer = answer.substring(idx + 1);
        }
        return answer;
    }

    public boolean isEmpty() {
        return classes.isEmpty() && statementBlock.getStatements().isEmpty();
    }
    
    public void sortClasses(){
    	if (isEmpty()) return;
    	List classes = getClasses();
    	LinkedList sorted = new LinkedList();
    	int level=1;
    	while (!classes.isEmpty()) {
	    	for (Iterator cni = classes.iterator(); cni.hasNext();) {
				ClassNode cn = (ClassNode) cni.next();
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

}
