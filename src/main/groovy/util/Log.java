package groovy.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * This local transform adds a logging ability to your program using
 * java.util.logging. Every method call on a unbound variable named <i>log</i> 
 * will be mapped to a call to the logger. For this a <i>log</i> field will be 
 * inserted in the class. If the field already exists the usage of this transform
 * will cause a compilation error. The method name will be used to determine
 * what to call on the logger.
 * <pre>
 * log.name(exp)
 * </pre>is mapped to
 * <pre>
 * if (log.isLoggable(Level.NAME) {
 *    log.name(exp)
 * }</pre>
 * Here name is a place holder for info, fine, finer, finest, config, warning, severe.
 * NAME is name tranformed to upper case. if anything else is used it will result in 
 * an exception at runtime. If the expression exp is a constant or only a variable access
 * the method call will not be transformed. But this will still cause a call on the injected 
 * logger.
 * 
 * @author Guillaume Laforge
 * @author Jochen Theodorou
 * @author Dinko Srkoc
 * @author Hamlet D'Arcy
 * @author Raffaele Cigni
 * @author Alberto Vilches Raton
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.LogASTTransformation")
public @interface Log {
	
}
