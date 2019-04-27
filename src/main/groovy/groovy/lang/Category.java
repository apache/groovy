/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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
 * <p>
 * Groovy categories are the original mechanism used
 * by Groovy when augmenting classes with new methods. Writing categories required
 * using a class writing style where all methods were static and an additional
 * self parameter was defined. The self parameter and static nature of the methods
 * disappeared once applied by Groovy's metaclass framework but some regarded
 * the writing style as a little noisy. This transformation allows you to write
 * your categories without the "apparent noise" but adds it back in during
 * compilation so that the classes appear as normal categories.
 * <p>
 * It might seem strange writing your class/object enhancements using a succinct
 * notation, then having "noise" added, then having the noise removed during
 * category application. If this worries you, then you may also like to consider
 * using Groovy's {@code ExpandoMetaClass} mechanism which avoids
 * the category definition altogether. If you already have an investment in
 * categories or like some of the other features which categories currently give you,
 * then read on.
 * <p>
 * The mechanics: during compilation, all methods are transformed to static ones with an additional
 * self parameter of the type you supply as the annotation parameter (the default type
 * for the self parameters is {@code Object} which might be more broad reaching than
 * you like so it is usually wise to specify a type).
 * Properties invoked using 'this' references are transformed so that
 * they are instead invoked on the additional self parameter and not on
 * the Category instance. (Remember that once the category is applied, the reverse
 * will occur and we will be back to conceptually having methods on the {@code this}
 * references again!)
 * <p>
 * Classes conforming to the conventional Groovy category conventions can be used
 * within {@code use} statements or mixed in at runtime with the {@code mixin} method on classes.
 * <p>
 * An example showing a {@code use} statement (allowing fine-grained application of
 * the category methods):
 * <pre class="groovyTestCase">
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
 * Or, "mixing in" your methods at runtime:
 * <pre class="groovyTestCase">
 * {@code @Category}(List)
 * class Shuffler {
 *     def shuffle() {
 *         def result = new ArrayList(this)
 *         Collections.shuffle(result)
 *         result
 *     }
 * }
 *
 * class Sentence extends ArrayList {
 *     Sentence(Collection initial) { super(initial) }
 * }
 * Sentence.mixin Shuffler
 *
 * def words = ["The", "quick", "brown", "fox"]
 * println new Sentence(words).shuffle()
 * // {@code =>} [quick, fox, The, brown]       (order will vary)
 * </pre>
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.CategoryASTTransformation")
public @interface Category {
    Class value () default Object.class;
}
