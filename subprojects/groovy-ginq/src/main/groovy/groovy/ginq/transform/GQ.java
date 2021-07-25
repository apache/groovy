package groovy.ginq.transform;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation to make a method call returning GINQ result
 *
 * @since 4.0.0
 */
@Incubating
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@GroovyASTTransformationClass("org.apache.groovy.ginq.transform.GinqASTTransformation")
public @interface GQ {
    boolean optimize() default true;
    boolean parallel() default false;
    String astWalker() default "org.apache.groovy.ginq.provider.collection.GinqAstWalker";
}
