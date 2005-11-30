package org.codehaus.groovy.grails.commons;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyResourceLoader;
import junit.framework.TestCase;

public class ClassReloadingTests extends TestCase {

	public void testReloadingClasses() throws Exception {
		final GroovyClassLoader cl = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
		cl.setResourceLoader( new GroovyResourceLoader() {
			public URL loadGroovySource(String filename) throws MalformedURLException {
				filename = filename.replace('.', '/') + ".groovy";
				return cl.getResource(filename);
			}				
		}
	);		
		
		File file = new File(cl.getResource("org/codehaus/groovy/grails/commons/TestReload.groovy").getFile());
		
		FileWriter fw = new FileWriter(file);
		try {
			fw.write(  "package org.codehaus.groovy.grails.commons;\n" +
						"class TestReload { \n" +
							"@Property hello = \"hello\"\n" +
						"}");		
			fw.close();
			
			Class groovyClass = cl.loadClass("org.codehaus.groovy.grails.commons.TestReload",true,false);

			
			
			GroovyObject go = (GroovyObject)groovyClass.newInstance();
			
			assertEquals("hello", go.getProperty("hello"));
			
			// change class
			fw = new FileWriter(file);
			fw.write(  "package org.codehaus.groovy.grails.commons;\n" +
					"class TestReload { \n" +
						"@Property hello = \"goodbye\"\n" +
					"}");		
		
			// reload		
			groovyClass = cl.loadClass("org.codehaus.groovy.grails.commons.TestReload",true,false);			
			go = (GroovyObject)groovyClass.newInstance();			
			assertEquals("goodbye", go.getProperty("hello"));			
		}
		finally {
			fw.close();
		}
	}
}
