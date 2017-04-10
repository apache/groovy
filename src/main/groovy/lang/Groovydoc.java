package groovy.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to hold the groovydoc for the annotated element at runtime, we can it "Runtime Groovydoc".
 * Runtime Groovydoc is a bit like Python's Documentation Strings and will be useful for IDE and developers who set a high value on documentations.
 *
 * The usage is very simple, just place @Groovydoc at the beginning of the content of groovydoc, then the new parser Parrot will attach the annotation Groovydoc automatically
 *
 * @since 3.0.0
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Groovydoc {
    String value();
}
