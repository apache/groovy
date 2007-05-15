package org.codehaus.groovy.groovydoc;

public interface GroovyPackageDoc extends GroovyDoc {
	public GroovyClassDoc[] allClasses();
	public GroovyClassDoc[] allClasses(boolean arg0);
//	public GroovyAnnotationTypeDoc[] annotationTypes();
//	public GroovyAnnotationDesc[] annotations();
	public GroovyClassDoc[] enums();
	public GroovyClassDoc[] errors();
	public GroovyClassDoc[] exceptions();
	public GroovyClassDoc findClass(String arg0);
	public GroovyClassDoc[] interfaces();
	public GroovyClassDoc[] ordinaryClasses();
	
	public String nameWithDots(); // not in JavaDoc API
}
