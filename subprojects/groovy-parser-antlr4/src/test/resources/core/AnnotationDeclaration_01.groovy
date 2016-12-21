import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Canonical(
        includes = ['a', 'b'], excludes = ['c']
)
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@GroovyASTTransformationClass('Lulz')
@interface FunnyAnnotation {
    public static final String SOME_CONSTANT2 = 'SOME_CONSTANT2';
    String SOME_CONSTANT = 'SOME_CONSTANT';

    /* This is a comment
    */
    String name() default ""

    /**
     * This has a default, too
     */
    boolean synchronize() default false

    boolean synchronize2() default
            false
}

@interface a {

}

@interface b {
    String name()
}