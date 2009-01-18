/*
 * Copyright 2003-2007 the original author or authors.
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

import groovy.lang.GroovyClassLoader;

import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Represents the entire contents of a compilation step which consists of one or more
 * {@link ModuleNode} instances. There's one instance of this that's shared by all modules and
 * classes compiled during a single invocation of the compiler.
 * 
 * It's attached to MethodNodes and ClassNodes and is used to find fully qualified names of classes,
 * resolve imports, and that sort of thing.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan </a>
 * @version $Revision$
 */
public class CompileUnit {

    private final List modules = new ArrayList();
    private Map classes = new HashMap();
    private CompilerConfiguration config;
    private GroovyClassLoader classLoader;
    private CodeSource codeSource;
    private Map classesToCompile = new HashMap();
    private Map classNameToSource = new HashMap();
    
    public CompileUnit(GroovyClassLoader classLoader, CompilerConfiguration config) {
    	this(classLoader, null, config);
    }
    
    public CompileUnit(GroovyClassLoader classLoader, CodeSource codeSource, CompilerConfiguration config) {
        this.classLoader = classLoader;
        this.config = config;
        this.codeSource = codeSource;
    }

    public List getModules() {
        return modules;
    }

    public void addModule(ModuleNode node) {
        // node==null means a compilation error prevented
        // groovy from building an ast
        if (node==null) return;
        modules.add(node);
        node.setUnit(this);
        addClasses(node.getClasses());
    }

    /**
     * @return the ClassNode for the given qualified name or returns null if
     *         the name does not exist in the current compilation unit
     *         (ignoring the .class files on the classpath)
     */
    public ClassNode getClass(String name) {
        ClassNode cn = (ClassNode) classes.get(name);
        if (cn!=null) return cn;
        return (ClassNode) classesToCompile.get(name);
    }

    /**
     * @return a list of all the classes in each module in the compilation unit
     */
    public List getClasses() {
        List answer = new ArrayList();
        for (Iterator iter = modules.iterator(); iter.hasNext();) {
            ModuleNode module = (ModuleNode) iter.next();
            answer.addAll(module.getClasses());
        }
        return answer;
    }

    public CompilerConfiguration getConfig() {
        return config;
    }

    public GroovyClassLoader getClassLoader() {
        return classLoader;
    }
    
    public CodeSource getCodeSource() {
    	return codeSource;
    }

    /**
     * Appends all of the fully qualified class names in this
     * module into the given map
     */
    void addClasses(List classList) {
        for (Iterator iter = classList.iterator(); iter.hasNext();) {
            addClass((ClassNode) iter.next());
        }
    }
    
    /**
     *  Adds a class to the unit.
     */
    public void addClass(ClassNode node) {
    	node = node.redirect();
        String name = node.getName();
        ClassNode stored = (ClassNode) classes.get(name);
        if (stored != null && stored != node) {
            // we have a duplicate class!
            // One possibility for this is, that we delcared a script and a 
            // class in the same file and named the class like the file
            SourceUnit nodeSource = node.getModule().getContext();
            SourceUnit storedSource = stored.getModule().getContext();
            String txt = "Invalid duplicate class definition of class "+node.getName()+" : ";
            if (nodeSource==storedSource) {
                // same class in same source
                txt += "The source "+nodeSource.getName()+" contains at least two defintions of the class "+node.getName()+".\n";
                if (node.isScriptBody() || stored.isScriptBody()) {
                    txt += "One of the classes is a explicit generated class using the class statement, the other is a class generated from"+
                           " the script body based on the file name. Solutions are to change the file name or to change the class name.\n";
                }
            } else {
                txt += "The sources "+nodeSource.getName()+" and "+storedSource.getName()+" are containing both a class of the name "+node.getName()+".\n";
            }
            nodeSource.getErrorCollector().addErrorAndContinue(
                    new SyntaxErrorMessage(new SyntaxException(txt, node.getLineNumber(), node.getColumnNumber()), nodeSource)
            );
        }
        classes.put(name, node);
        
        if (classesToCompile.containsKey(name)) {
            ClassNode cn = (ClassNode) classesToCompile.get(name);
            cn.setRedirect(node);
            classesToCompile.remove(name);
        }        
    }
     
    /**
     * this emthod actually does not compile a class. It's only
     * a marker that this type has to be compiled by the CompilationUnit
     * at the end of a parse step no node should be be left.
     */
    public void addClassNodeToCompile(ClassNode node, SourceUnit location) {
        classesToCompile.put(node.getName(),node);
        classNameToSource.put(node.getName(),location);
    }
    
    public SourceUnit getScriptSourceLocation(String className) {
        return (SourceUnit) classNameToSource.get(className);
    }

    public boolean hasClassNodeToCompile(){
        return !classesToCompile.isEmpty();
    }
    
    public Iterator iterateClassNodeToCompile(){
        return classesToCompile.keySet().iterator();
    }
}
