/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package org.codehaus.groovy.ast;

import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.classgen.ClassGeneratorException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Represents the entire contents of a compilation step which consists of one
 * or more {@link ModuleNode}instances
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan </a>
 * @version $Revision$
 */
public class CompileUnit {

    private List modules = new ArrayList();
    private Map classes = new HashMap();
    private CompilerConfiguration config;
    private ClassLoader classLoader;
    private CodeSource codeSource;
    private Map cachedClasses = new HashMap();
    
    public static final Object NO_CLASS = new Object();
    

    public CompileUnit(ClassLoader classLoader, CompilerConfiguration config) {
    	this(classLoader, null, config);
    }
    
    public CompileUnit(ClassLoader classLoader, CodeSource codeSource, CompilerConfiguration config) {
        this.classLoader = classLoader;
        this.config = config;
        this.codeSource = codeSource;
    }

    public List getModules() {
        return modules;
    }

    public void addModule(ModuleNode node) {
        modules.add(node);
        node.setUnit(this);
        addClasses(node.classes);
    }

    /**
     * @return the ClassNode for the given qualified name or returns null if
     *         the name does not exist in the current compilation unit
     *         (ignoring the .class files on the classpath)
     */
    public ClassNode getClass(String name) {
        return (ClassNode) classes.get(name);
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

    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public CodeSource getCodeSource() {
    	return codeSource;
    }

    
    /**
     * Loads a class on the compile classpath so that it can be introspected
     * 
     * @param type
     * @return @throws
     *         ClassNotFoundException
     */
    public Class loadClass(String type) throws ClassNotFoundException {
    	Object obj = cachedClasses.get(type);
    	if ( obj == NO_CLASS ) {
    		throw new ClassNotFoundException(type);
    	}
    	if ( obj != null) {
    		return (Class)obj;
    	}
    	
    	Class answer = null;
    	ClassLoader lastLoader  = getClassLoader();
    	try {
    		answer = lastLoader.loadClass(type);
    	} catch (ClassNotFoundException e) {
            Throwable cause = e.getCause();
    		if (cause !=null && cause instanceof CompilationFailedException){
                throw new ClassGeneratorException("Error when compiling class: " + type + ". Reason: " + cause, cause);
            }
    	} catch (NoClassDefFoundError e) {
           // fall through   
        }
    	
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
        	if ( answer == null && loader != lastLoader /*&& loader != null*/) {
                lastLoader = loader;
        		answer = loader.loadClass(type);
        	}
        }
        catch (ClassNotFoundException e1) {
            Throwable cause = e1.getCause();
            if (cause !=null && cause instanceof CompilationFailedException){
                throw new ClassGeneratorException("Error when compiling class: " + type + ". Reason: " + cause, cause);
            }
        } catch (NoClassDefFoundError e) {
            // fall through   
        }
        
        // lets try our class loader
        try {
            ClassLoader loader = getClass().getClassLoader();
        	if ( answer == null && loader != lastLoader) {
                lastLoader = loader;
        		answer = loader.loadClass(type);
        	}
        }
        catch (ClassNotFoundException e2) {
            Throwable cause = e2.getCause();
            if (cause !=null && cause instanceof CompilationFailedException){
                throw new ClassGeneratorException("Error when compiling class: " + type + ". Reason: " + cause, cause);
            }        
        }
        catch (NoClassDefFoundError e) {
            // fall through   
         }
        
        try {
        	if (answer == null ) {
        		answer = Class.forName(type);
        	}
        }
        catch (ClassNotFoundException e2) {
            Throwable cause = e2.getCause();
            if (cause !=null && cause instanceof CompilationFailedException){
                throw new ClassGeneratorException("Error when compiling class: " + type + ". Reason: " + cause, cause);
            }
        }
        catch (NoClassDefFoundError e) {
            // fall through   
         }

        if ( answer == null ) {
        	cachedClasses.put(type,NO_CLASS);
        	throw new ClassNotFoundException(type);
        } else {
            if (!type.equals(answer.getName())) { // br case sensitive match
                cachedClasses.put(type,NO_CLASS);
                System.out.println("Mismatch: answer.getName() = " + answer.getName() + ", type = " + type);
                throw new ClassNotFoundException(type);
            }
            cachedClasses.put(type,answer);
        }
        
        return answer;
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
        String name = node.getName();
        if (classes.containsKey(name)) {
            throw new RuntimeException(
                "Error: duplicate class declaration for name: " + name + " and class: " + node);
        }
        classes.put(name, node);
    }

}
