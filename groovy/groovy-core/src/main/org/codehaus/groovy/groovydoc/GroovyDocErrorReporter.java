package org.codehaus.groovy.groovydoc;

public interface GroovyDocErrorReporter{
	public void printError(String arg0);
//	public void printError(GroovySourcePosition arg0, String arg1);
	public void printNotice(String arg0);
//	public void printNotice(GroovySourcePosition arg0, String arg1);
	public void printWarning(String arg0);
//	public void printWarning(GroovySourcePosition arg0, String arg1);
}
