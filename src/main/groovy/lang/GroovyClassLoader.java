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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
 * @author Guillaume Laforge
 * @author Steve Goetze
 * @version $Revision$
 */
public class GroovyClassLoader extends SecureClassLoader {

    private Map cache = new HashMap();
    private class PARSING {};
    private CompilerConfig config;
    private String[] paths;

    public GroovyClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public GroovyClassLoader(ClassLoader loader) {
        this(loader, new CompilerConfig());
    }

    public GroovyClassLoader(GroovyClassLoader parent) {
        this(parent, parent.config);
    }

    public GroovyClassLoader(ClassLoader loader, CompilerConfig config) {
        super(loader);
        this.config = config;
    }

    /**
     * Loads the given class node returning the implementation Class
     *
     * @param classNode
     * @return
     */
    public Class defineClass(ClassNode classNode, String file) {
    	return defineClass(classNode, file, "/groovy/defineClass");
    }

    /**
     * Loads the given class node returning the implementation Class
     *
     * @param classNode
     * @return
     */
    public Class defineClass(ClassNode classNode, String file, String newCodeBase) {
    	CodeSource codeSource = null;
    	try {
    		codeSource = new CodeSource(new URL("file", "", newCodeBase), null);
    	} catch (MalformedURLException e) {
    		//swallow
    	}
        CompileUnit unit = new CompileUnit(getParent(), codeSource, config);
        ClassCollector compiler = createCollector(unit);
        compiler.generateClass(new GeneratorContext(unit), classNode, file);
        return compiler.generatedClass;
    }

    /**
     * Parses the given file into a Java class capable of being run
     *
     * @param file the file name to parse
     * @return the main class defined in the given script
     */
    public Class parseClass(File file) throws SyntaxException, IOException {
    	return parseClass(new GroovyCodeSource(file));
    }

    /**
     * Parses the given text into a Java class capable of being run
     *
     * @param text the text of the script/class to parse
     * @param fileName the file name to use as the name of the class
     * @return the main class defined in the given script
     */
    public Class parseClass(String text, String fileName) throws SyntaxException, IOException {
        return parseClass(new ByteArrayInputStream(text.getBytes()), fileName);
    }

    /**
     * Parses the given text into a Java class capable of being run
     *
     * @param text the text of the script/class to parse
     * @return the main class defined in the given script
     */
    public Class parseClass(String text) throws SyntaxException, IOException {
        return parseClass(new ByteArrayInputStream(text.getBytes()), "script" + System.currentTimeMillis() + ".groovy");
    }

    /**
     * Parses the given character stream into a Java class capable of being run
     *
     * @param in an InputStream
     * @return the main class defined in the given script
     */
    public Class parseClass(InputStream in) throws SyntaxException, IOException {
        return parseClass(in, "script" + System.currentTimeMillis() + ".groovy");
    }

    public Class parseClass(final InputStream in, final String fileName) throws SyntaxException, IOException {
    	//For generic input streams, provide a catch-all codebase of GroovyScript
    	//Security for these classes can be administered via policy grants with a codebase
    	//of file:groovy.script
    	GroovyCodeSource gcs = (GroovyCodeSource) AccessController.doPrivileged(new PrivilegedAction() {
    		public Object run() {
    			return new GroovyCodeSource(in, fileName, "/groovy/script");
    		}
    	});
    	return parseClass(gcs);
    }
    
    /**
	 * Parses the given character stream into a Java class capable of being run
	 *
	 * @param in an InputStream
	 * @param fileName the file name to use as the name of the class
	 * @return the main class defined in the given script
	 */
	public Class parseClass(GroovyCodeSource codeSource) throws SyntaxException, IOException {
		String name = codeSource.getName();
		Class answer = null;
		//ASTBuilder.resolveName can call this recursively -- for example when resolving a Constructor
		//invocation for a class that is currently being compiled.  
		synchronized (cache) {
			answer = (Class) cache.get(name);
			if (answer != null) {
				return (answer==PARSING.class ? null : answer);
			} else {
				cache.put(name, PARSING.class);
			}
		}
		//Was neither already loaded nor compiling, so compile and add to cache.
	   	try {
	   		CompileUnit unit = new CompileUnit(this, codeSource.getCodeSource(), config);
	   		ClassCollector compiler = createCollector(unit);
	   		compiler.parseClass(codeSource.getInputStream(), name);
	   		answer = compiler.generatedClass;
	   	} finally {
	   		synchronized (cache) {
	   			if (answer == null) {
	   				cache.remove(name);
	   			} else {
	   				cache.put(name, answer);
	   			}
	   		}
	   	}        		
	    return answer;
	}
    
    /**
     * Using this classloader you can load groovy classes from the system classpath as though they were already compiled.
     * Note that .groovy classes found with this mechanism need to conform to the standard java naming convention - i.e.
     * the public class inside the file must match the filename and the file must be located in a directory structure
     * that matches the package structure.
     */
    protected Class findClass(final String name) throws ClassNotFoundException {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String className = name.replace('/', '.');
			int i = className.lastIndexOf('.');
			if (i != -1) {
				sm.checkPackageDefinition(className.substring(0, i));
			}
		}
        try {
        	return (Class) AccessController.doPrivileged(new PrivilegedExceptionAction() {
        		public Object run() throws ClassNotFoundException {
        			return findGroovyClass(name);
        		}
        	});
        } catch (PrivilegedActionException pae) {
        	throw (ClassNotFoundException) pae.getException();
        }
    }
    
    protected Class findGroovyClass(String name) throws ClassNotFoundException {
        //Use a forward slash here for the path separator.  It will work as a separator
    	//for the File class on all platforms, AND it is required as a jar file entry separator.
    	String filename = name.replace('.', '/') + ".groovy";
        String[] paths = getClassPath();
        for (int i = 0; i < paths.length; i++) {
            String pathName = paths[i];
            File path = new File(pathName);
            if (path.exists()) {
                if (path.isDirectory()) {
                    File file = new File(path, filename);
                    if (file.exists()) {
                        try {
                            return parseClass(file);
                        } catch (SyntaxException e) {
                            e.printStackTrace();
                            throw new ClassNotFoundException(
                                    "Syntax error in groovy file: " + filename,
                                    e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new ClassNotFoundException(
                                    "Error reading groovy file: " + filename, e);
                        }
                    }
                } else {
                    try {
                        JarFile jarFile = new JarFile(path);
                        JarEntry entry = jarFile.getJarEntry(filename);
                        if (entry != null) {
                        	byte[] bytes = extractBytes(jarFile, entry);
                            Certificate[] certs = entry.getCertificates();
                            try {
                                return parseClass(new GroovyCodeSource(new ByteArrayInputStream(bytes), filename, path, certs));
                            } catch (SyntaxException e1) {
                                e1.printStackTrace();
                                throw new ClassNotFoundException(
                                        "Syntax error in groovy file: " + filename,
                                        e1);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                throw new ClassNotFoundException(
                                        "Error reading groovy file: " + filename, e1);
                            }
                        }

                    } catch (IOException e) {
                        // Bad jar in classpath, ignore
                    }
                }
            }
        }
    	throw new ClassNotFoundException(name);
    }

    //Read the bytes from a non-null JarEntry.  This is done here because the entry must be read completely 
    //in order to get verified certificates, which can only be obtained after a full read.
    private byte[] extractBytes(JarFile jarFile, JarEntry entry) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        try {
            BufferedInputStream bis = new BufferedInputStream(jarFile.getInputStream(entry));
        	while ((b = bis.read()) != -1) {
        		baos.write(b);
        	}
        } catch (IOException ioe) {
        	throw new GroovyRuntimeException("Could not read the jar bytes for " + entry.getName());
        }
        return baos.toByteArray();
    }

    /**
     * @return
     */
    private String[] getClassPath() {
        if (paths == null) {
            List pathList = new ArrayList();
            String classpath = System.getProperty("java.class.path", ".");
            expandClassPath(pathList, null, classpath);
            paths = new String[pathList.size()];
            paths = (String[]) pathList.toArray(paths);
        }
        return paths;
    }

    /**
     * @param pathList
     * @param classpath
     */
    private void expandClassPath(List pathList, String base, String classpath) {
        paths = classpath.split(File.pathSeparator);
        for (int i = 0; i < paths.length; i++) {
            File path = null;

            if ("".equals(base)) {
                path = new File(paths[i]);
            } else {
                path = new File(base, paths[i]);
            }

            if (path.exists()) {
                if (!path.isDirectory()) {
                    try {
                        // Get the manifest classpath entry from the jar
                        JarFile jar = new JarFile(path);
                        pathList.add(paths[i]);
                        Manifest manifest = jar.getManifest();
                        Attributes classPathAttributes = manifest.getMainAttributes();
                        String manifestClassPath = classPathAttributes.getValue("Class-Path");

                        if (manifestClassPath != null)
                            expandClassPath(pathList, paths[i], manifestClassPath);

                    } catch (IOException e) {
                        // Bad jar, ignore
                        continue;
                    }
                } else {
                    pathList.add(paths[i]);
                }
            }
        }
    } 
 
    /**
     * A helper method to allow bytecode to be loaded.
     * spg changed name to defineClass to make it more consistent with other ClassLoader methods 
     */
    protected Class defineClass(String name, byte[] bytecode, ProtectionDomain domain) {
        return defineClass(name, bytecode, 0, bytecode.length, domain);
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

            Class theClass = cl.defineClass(classNode.getName(), code, 0, code.length, getCompileUnit().getCodeSource());

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
        String outputDir = config.getOutputDir();
        if (outputDir != null) {
            String filename = classNode.getName().replace('.', File.separatorChar) + ".class";
            int index = filename.lastIndexOf(File.separator);
            String dirname;
            if (index != -1) {
                dirname = filename.substring(0, index);
            }
            else {
                dirname = "";
            }
            File outputFile = new File(new File(outputDir), filename);
            System.err.println("Writing: " + outputFile);
            try {
                new File(new File(outputDir), dirname).mkdirs();
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(code, 0, code.length);
                fos.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 * Implemented here to check package access prior to returning an already loaded class.
	 */
	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String className = name.replace('/', '.');
			int i = className.lastIndexOf('.');
			if (i != -1) {
				sm.checkPackageAccess(className.substring(0, i));
			}
		}
		return super.loadClass(name, resolve);
	}
}
