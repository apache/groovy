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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;

/**
 * CodeSource wrapper class that allows specific security policies to be associated with a class
 * compiled from groovy source.
 * 
 * @author Steve Goetze
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
	private InputStream inputStream;
	/** The certificates used to sign the items from the codesource */
	Certificate[] certs;
    private boolean cachable;
    
	private File file;
	
	public GroovyCodeSource(String script, String name, String codeBase) {
		this(new ByteArrayInputStream(script.getBytes()), name, codeBase);
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
	public GroovyCodeSource(InputStream inputStream, String name, String codeBase) {
		this.inputStream = inputStream;
		this.name = name;
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    sm.checkPermission(new GroovyCodeSourcePermission(codeBase));
		}
		try {
			this.codeSource = new CodeSource(new URL("file", "", codeBase), (java.security.cert.Certificate[])null);
		} catch (MalformedURLException murle) {
			throw new RuntimeException("A CodeSource file URL cannot be constructed from the supplied codeBase: " + codeBase);
		}
	}

    public GroovyCodeSource(final File infile) throws IOException {
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
                throw new RuntimeException(file.toString() + " can not be read. Check the read permisson of the file \"" + file.toString() + "\" (" + file.getAbsolutePath() + ").");
        }
        catch (SecurityException e) {
            throw e;
        }

        //this.inputStream = new FileInputStream(file);
        this.file = file;
        this.inputStream = null;
        this.cachable = true;
        //The calls below require access to user.dir - allow here since getName() and getCodeSource() are
        //package private and used only by the GroovyClassLoader.
        try {
            Object[] info = (Object[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws MalformedURLException {
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

    public GroovyCodeSource(URL url) throws IOException {
		if (url == null) {
			throw new RuntimeException("Could not construct a GroovyCodeSource from a null URL");
		}
		this.inputStream = url.openStream();
		this.name = url.toExternalForm();
		this.codeSource = new CodeSource(url, (java.security.cert.Certificate[])null);
	}
	
	CodeSource getCodeSource() {
		return codeSource;
	}

	public InputStream getInputStream() {
        if(this.inputStream != null) {
            return this.inputStream;
        }
        else {
            try {
                if (file!=null) return new FileInputStream(file);
            } catch (FileNotFoundException fnfe) {
                // IGNORE
            }
            return inputStream;            
        }
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
}
