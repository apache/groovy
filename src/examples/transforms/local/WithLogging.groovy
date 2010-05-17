package transforms.local
import java.lang.annotation.Retention
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

/**
* This is just a marker interface that will trigger a local transformation. 
* The 3rd Annotation down is the important one: @GroovyASTTransformationClass
* The parameter is the String form of a fully qualified class name. 
*
* @author Hamlet D'Arcy
*/ 
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass(["transforms.local.LoggingASTTransformation"])
public @interface WithLogging {
}