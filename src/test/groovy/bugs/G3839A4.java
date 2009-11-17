package groovy.bugs;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

// add ast transforms both by class names and classes, which should result in an error 
@GroovyASTTransformationClass(value="groovy.bugs.G3839Transform2", classes=G3839Transform3.class)
public @interface G3839A4 {}
