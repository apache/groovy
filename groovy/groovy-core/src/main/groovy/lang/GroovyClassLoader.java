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
package groovy.lang;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.classgen.CompilerFacade;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.ClassWriter;

/**
 * A ClassLoader which can load Groovy classes
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyClassLoader extends ClassLoader {

	private String outputDir;
    private Map cache = new HashMap();

    public GroovyClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public GroovyClassLoader(ClassLoader loader) {
        super(loader);
		outputDir = System.getProperty("groovy.output.dir");
    }

    /**
	 * Loads the given class node returning the implementation Class
	 * 
	 * @param classNode
	 * @return
	 */
    public Class defineClass(ClassNode classNode, String file) {
        CompileUnit unit = new CompileUnit();
        ClassCollector compiler = createCollector(unit);
        compiler.generateClass(new GeneratorContext(unit), classNode, file);
        return compiler.generatedClass;
    }

    /**
     * Parses the given file name into a Java class capable of being run
     * 
     * @param file the file name to parse
     * @return the main class defined in the given script
     */
    public Class parseClass(String file) throws SyntaxException, IOException {
        return parseClass(new FileInputStream(file), file);
    }

    /**
     * Parses the given text into a Java class capable of being run
     * 
     * @param text the text of the script/class to parse
     * @param file the file name to use as the name of the class
     * @return the main class defined in the given script
     */
    public Class parseClass(String text, String file) throws SyntaxException, IOException {
        return parseClass(new ByteArrayInputStream(text.getBytes()), file);
    }

    /**
	 * Parses the given character stream into a Java class capable of being run
	 * 
	 * @param charStream
	 * @return the main class defined in the given script
	 */
    public Class parseClass(InputStream in, String file) throws SyntaxException, IOException {
        Class answer = (Class) cache.get(file);
        if (answer == null) {
            CompileUnit unit = new CompileUnit();
            ClassCollector compiler = createCollector(unit);
            compiler.parseClass(in, file);
            answer = compiler.generatedClass;
            cache.put(file, answer);
        }
        return answer;
    }

    /**
     * A helper method to allow bytecode to be loaded
     */
    protected Class loadClass(String name, byte[] bytecode) {
        return defineClass(name, bytecode, 0, bytecode.length);
    }
    
    protected ClassCollector createCollector(CompileUnit unit) {
        return new ClassCollector(this, unit);
    }

    
    protected static class ClassCollector extends CompilerFacade {
    	
    	private Class generatedClass;
    	private GroovyClassLoader cl;
    	
    	
    	protected ClassCollector(GroovyClassLoader cl, CompileUnit unit) {
    		super(cl, unit);
    		this.cl = cl;
    	}
    	
    	protected Class onClassNode(ClassWriter classWriter, ClassNode classNode) {
    		byte[] code = classWriter.toByteArray();

    		cl.debugWriteClassfile(classNode, code);

    		Class theClass = cl.defineClass(classNode.getName(), code, 0, code.length);

    		if (generatedClass == null) {
    			generatedClass = theClass;
    		}
    		
    		return theClass;
    	}
    	
    	protected void onClass(ClassWriter classWriter, ClassNode classNode) {
    		onClassNode(classWriter, classNode);
    	}
    	
    }

    private void debugWriteClassfile(ClassNode classNode, byte[] code) {
		if (outputDir != null) {
			String filename = classNode.getName().replace('.', File.separatorChar) + ".class";
			int index = filename.lastIndexOf(File.separator);
			String dirname;
			if (index != -1) {
				dirname = filename.substring(0, index);
			} else {
				dirname = "";
			}
			File outputFile = new File(new File(outputDir), filename);
			System.err.println("Writing: " + outputFile);
			try {
				new File(new File(outputDir), dirname).mkdirs();
				FileOutputStream fos = new FileOutputStream(outputFile);
				fos.write(code, 0, code.length);
				fos.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
