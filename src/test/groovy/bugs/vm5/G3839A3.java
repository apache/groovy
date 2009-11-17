package groovy.bugs.vm5;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

@GroovyASTTransformationClass() // does not specify ast transform class names or classes
public @interface G3839A3 {}
