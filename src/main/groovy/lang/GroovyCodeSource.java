package groovy.lang;

import groovy.security.GroovyCodeSourcePermission;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
	public GroovyCodeSource(String script, String name, String codeBase) {
		this(new ByteArrayInputStream(script.getBytes()), name, codeBase);
	}
	
	/**
	 * Construct a GroovyCodeSource for an inputStream of groovyCode that has an
	 * unknown provenance -- meaning it didn't come from a File or a URL (e.g. a String).
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
			this.codeSource = new CodeSource(new URL("file", "", codeBase), null);
		} catch (MalformedURLException murle) {
			throw new RuntimeException("A CodeSource file URL cannot be constructed from the supplied codeBase: " + codeBase);
		}
	}

	/** 
	 * Package private constructor called by GroovyClassLoader for signed jar entries
	 */
	GroovyCodeSource(InputStream inputStream, String name, final File path, final Certificate[] certs) {
		this.inputStream = inputStream;
		this.name = name;
		try {
			this.codeSource = (CodeSource) AccessController.doPrivileged( new PrivilegedExceptionAction() {
				public Object run() throws MalformedURLException {
					//toURI().toURL() will encode, but toURL() will not.
					return new CodeSource(path.toURI().toURL(), certs);
				}
			});
		} catch (PrivilegedActionException pae) {
			//shouldn't happen
			throw new RuntimeException("Could not construct a URL from: " + path);
		}
	}
	
	public GroovyCodeSource(final File file) throws FileNotFoundException {
		this.inputStream = new FileInputStream(file);
		this.name = file.getName();
		//toURI() below requires access to user.dir - allow here since getCodeSource() is package private and is
		//used only by the GroovyClassLoader.
		try {
			this.codeSource = (CodeSource) AccessController.doPrivileged( new PrivilegedExceptionAction() {
				public Object run() throws MalformedURLException {
					//toURI().toURL() will encode, but toURL() will not.
					return new CodeSource(file.toURI().toURL(), null);
				}
			});
		} catch (PrivilegedActionException pae) {
			throw new RuntimeException("Could not construct a URL from: " + file);
		}
	}
	
	public GroovyCodeSource(URL url) {
		this.codeSource = new CodeSource(url, null);
	}
	
	CodeSource getCodeSource() {
		return codeSource;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getName() {
		return name;
	}
}
