package examples.astbuilder

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.ElementType
import java.lang.annotation.Target
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention

/**
 * Marker interface to mark a method as something that should be invokable
 * as a main() method. An AST transformation will later wire this together. 
 *
 * @author Hamlet D'Arcy
 */

@Retention (RetentionPolicy.SOURCE)
@Target ([ElementType.METHOD])
@GroovyASTTransformationClass (["examples.astbuilder.MainTransformation"])
public @interface Main { 
}
