package groovy.bugs.vm5;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

// add ast transforms both by class names and classes, which should result in an error 
@GroovyASTTransformationClass(value="groovy.bugs.vm5.G3839Transform2", classes=G3839Transform3.class)
public @interface G3839A4 {}
