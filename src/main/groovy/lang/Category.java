/*
 * Copyright 2008-2009 the original author or authors.
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

package groovy.lang;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transforms an instance-style Groovy class or interface to become a static-style
 * conventional Groovy category.
 * </p>
 * All methods are transformed to static ones with an additional self
 * parameter having the type supplied as the required annotation parameter.
 * Properties invoked using 'this' references are transformed so that
 * they are instead invoked on the additional self parameter and not on
 * the Category instance.
 * </p>
 * Classes conforming to the conventional Groovy category conventions can be used
 * within {@code use} statements or mixed in with the {@code Mixin} transformation.
 * An example showing a {@code use} statement:
 * <pre>
 * {@code @Category}(Integer)
 * class IntegerOps {
 *     def triple() {
 *         this * 3
 *     }
 * }
 *
 * use (IntegerOps) {
 *     assert 25.triple() == 75
 * }
 * </pre>
 * Or, using the {@code @Mixin} flavor:
 * <pre>
 * {@code @Category}(List)
 * class Shuffler {
 *     def shuffle() {
 *         def result = new ArrayList(this)
 *         Collections.shuffle(result)
 *         result
 *     }
 * }
 *
 * {@code @Mixin}(Shuffler)
 * class Sentence extends ArrayList {
 *     Sentence(Collection initial) { super(initial) }
 * }
 *
 * def words = ["The", "quick", "brown", "fox"]
 * println new Sentence(words).shuffle()
 * // => [quick, fox, The, brown]       (order will vary)
 * </pre>
 *
 * @author Alex Tkachman
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.CategoryASTTransformation")
public @interface Category {
    Class value () default Object.class;
}