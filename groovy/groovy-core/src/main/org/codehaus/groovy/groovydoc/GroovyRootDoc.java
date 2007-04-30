package org.codehaus.groovy.groovydoc;

public interface GroovyRootDoc extends GroovyDoc, GroovyDocErrorReporter {
	public GroovyClassDoc classNamed(String arg0);
	public GroovyClassDoc[] classes();
	public String[][] options();
	public GroovyPackageDoc packageNamed(String arg0);
	public GroovyClassDoc[] specifiedClasses();
	public GroovyPackageDoc[] specifiedPackages();
}
