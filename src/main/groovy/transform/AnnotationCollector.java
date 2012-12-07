/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.AnnotationCollectorTransform;


/**
 * The AnnotationCollector can be used to define aliases for groups of 
 * annotations. The Alias needs to be an annotation annotated with 
 * AnnotationCollector, otherwise nothing is required. The alias will be 
 * replaced on the AST level and will never appear in later. Any annotation 
 * attribute definitions of the alias will be ignored, but could be used by a 
 * custom processor. Annotation arguments are mapped to the aliased annoations
 * if existing. Should the default processor not be able to map one of the
 * arguments and error will be given. Is this not wished or if you want a 
 * different mapping a custom processor has to be used<pre>
 *          import groovy.transform.*
 *          &#64;AnnotationCollector([ToString, EqualsAndHashCode, Immutable])
 *          &#64;interface Alias {}

 *          &#64;Alias(excludes=["a"])
 *          class Foo {
 *              Integer a, b
 *          }
 *          assert Foo.class.annotations.size()==3 
 *          assert new Foo(1,2).toString() == "Foo(2)"
 * </pre>In the example above we have Alias as the alias annotation and an argument 
 * excludes which will be mapped to ToString and EqualsAndHashCode. Immutable 
 * doesn't have excludes, thus nothing will be done.
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @see AnnotationCollectorTransform
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface AnnotationCollector {
    /**
     * Processor used for computing custom logic or the list of annotations, or 
     * both. The default is org.codehaus.groovy.transform.AnnotationCollectorTransform.
     * Custom processors need to extend that class. 
     */
    String processor() default "org.codehaus.groovy.transform.AnnotationCollectorTransform";
    /**
     * List of aliased annotations.
     */
    Class[] value() default {};
}
