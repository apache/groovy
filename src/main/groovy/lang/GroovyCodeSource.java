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

package groovy.lang;

import groovy.security.GroovyCodeSourcePermission;
import groovy.util.CharsetToolkit;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * CodeSource wrapper class that allows specific security policies to be associated with a class
 * compiled from groovy source.
 * 
 * @author Steve Goetze
 * @author Guillaume Laforge
 */
public class GroovyCodeSource {
	
	/** 
	 * The codeSource to be given the generated class.  This can be used by policy file
	 * grants to administer security.
	 */
	private CodeSource codeSource;

    /** The name given to the generated class */
	private String name;

    /** The groovy source to be compiled and turned into a class */
	private String scriptText;

    /** The certificates used to sign the items from the codesource */
	Certificate[] certs;

    private boolean cachable;

	private File file;

	public GroovyCodeSource(String script, String name, String codeBase) {
		this.name = name;
        this.scriptText = script;
        this.codeSource = createCodeSource(codeBase);
		this.cachable = true;
	}

    /**
	 * Construct a GroovyCodeSource for an inputStream of groovyCode that has an
	 * unknown provenance -- meaning it didn't come from a File or a URL (e.g.&nbsp;a String).
	 * The supplied codeBase will be used to construct a File URL that should match up
	 * with a java Policy entry that determines the grants to be associated with the
	 * class that will be built from the InputStream.
	 *
	 * The permission groovy.security.GroovyCodeSourcePermission will be used to determine if the given codeBase
	 * may be specified.  That is, the current Policy set must have a GroovyCodeSourcePermission that implies
	 * the codeBase, or an exception will be thrown.  This is to prevent callers from hijacking
	 * existing codeBase policy entries unless explicitly authorized by the user.
	 */
	public GroovyCodeSource(Reader reader, String name, String codeBase) {
		this.name = name;
		this.codeSource = createCodeSource(codeBase);

        try {
            this.scriptText = DefaultGroovyMethods.getText(reader);
        } catch (IOException e) {
            throw new RuntimeException("Impossible to read the text content from that reader, for script: " + name + " with codeBase: " + codeBase, e);
        }
    }

	/**
	 * Construct a GroovyCodeSource for an inputStream of groovyCode that has an
	 * unknown provenance -- meaning it didn't come from a File or a URL (e.g.&nbsp;a String).
	 * The supplied codeBase will be used to construct a File URL that should match up
	 * with a java Policy entry that determines the grants to be associated with the
	 * class that will be built from the InputStream.
	 * 
	 * The permission groovy.security.GroovyCodeSourcePermission will be used to determine if the given codeBase
	 * may be specified.  That is, the current Policy set must have a GroovyCodeSourcePermission that implies
	 * the codeBase, or an exception will be thrown.  This is to prevent callers from hijacking
	 * existing codeBase policy entries unless explicitly authorized by the user.
     *
     * @deprecated Prefer using methods taking a Reader rather than an InputStream to avoid wrong encoding issues.
	 */
    @Deprecated
	public GroovyCodeSource(InputStream inputStream, String name, String codeBase) {
		this.name = name;
		this.codeSource = createCodeSource(codeBase);
        try {
            this.scriptText = DefaultGroovyMethods.getText(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Impossible to read the text content from that input stream, for script: " + name + " with codeBase: " + codeBase, e);
        }
    }

    public GroovyCodeSource(final File infile, final String encoding) throws IOException {
        // avoid files which confuse us like ones with .. in path
        final File file = new File(infile.getCanonicalPath());
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString() + " (" + file.getAbsolutePath() + ")");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file.toString() + " (" + file.getAbsolutePath() + ") is a directory not a Groovy source file.");
        }
        try {
            if (!file.canRead())
                throw new RuntimeException(file.toString() + " can not be read. Check the read permission of the file \"" + file.toString() + "\" (" + file.getAbsolutePath() + ").");
        }
        catch (SecurityException e) {
            throw e;
        }

        this.file = file;
        this.cachable = true;
        //The calls below require access to user.dir - allow here since getName() and getCodeSource() are
        //package private and used only by the GroovyClassLoader.
        try {
            Object[] info = AccessController.doPrivileged(new PrivilegedExceptionAction<Object[]>() {
                public Object[] run() throws IOException {
                    // retrieve the content of the file using the provided encoding
                    if (encoding != null) {
                        scriptText = DefaultGroovyMethods.getText(infile, encoding);
                    } else {
                        scriptText = DefaultGroovyMethods.getText(infile);
                    }

                    Object[] info = new Object[2];
                    URL url = file.toURI().toURL();
                    info[0] = url.toExternalForm();
                    //toURI().toURL() will encode, but toURL() will not.
                    info[1] = new CodeSource(url, (Certificate[]) null);
                    return info;
                }
            });

            this.name = (String) info[0];
            this.codeSource = (CodeSource) info[1];
        } catch (PrivilegedActionException pae) {
            throw new RuntimeException("Could not construct a URL from: " + file);
        }
    }

    /**
     * @param infile the file to create a GroovyCodeSource for.
     *
     * @throws IOException if an issue arises opening and reading the file.
     */
    public GroovyCodeSource(final File infile) throws IOException {
        this(infile, CharsetToolkit.getDefaultSystemCharset().name());
    }

    public GroovyCodeSource(URL url) throws IOException {
		if (url == null) {
			throw new RuntimeException("Could not construct a GroovyCodeSource from a null URL");
		}
		this.name = url.toExternalForm();
		this.codeSource = new CodeSource(url, (java.security.cert.Certificate[])null);
	}
	
	CodeSource getCodeSource() {
		return codeSource;
	}
    
    /**
     * @deprecated Prefer using methods taking a Reader rather than an InputStream to avoid wrong encoding issues.
     */
    @Deprecated
	public InputStream getInputStream() {
        IOException ioe;
        if (file == null) {
            try {
                return new ByteArrayInputStream(scriptText.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                ioe = e;
            }
        } else {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                ioe = e;
            }
        }

        String errorMsg = "Impossible to read the bytes from the associated script: " + scriptText + " with name: " + name;
        if (ioe != null) {
            throw new RuntimeException(errorMsg, ioe);
        } else {
            throw new RuntimeException(errorMsg);
        }
    }

	public String getScriptText() {
        return scriptText;
    }

    public String getName() {
		return name;
	}
    
    public File getFile() {
        return file;
    }
    
    public void setCachable(boolean b) {
        cachable = b;
    }

    public boolean isCachable() {
        return cachable;
    }

    private static CodeSource createCodeSource(final String codeBase) {
        SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    sm.checkPermission(new GroovyCodeSourcePermission(codeBase));
		}
        try {
			return new CodeSource(new URL("file", "", codeBase), (java.security.cert.Certificate[])null);
		}
        catch (MalformedURLException e) {
			throw new RuntimeException("A CodeSource file URL cannot be constructed from the supplied codeBase: " + codeBase);
		}
    }
}
