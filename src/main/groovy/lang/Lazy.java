/*
 * Copyright 2008 the original author or authors.
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
 * Field annotation to make field lazy initializable.
 *
 * If field declared volatile then initialization will be synchronized
 *
 * <pre>
 * @Lazy volatile T x
 *
 * becomes
 *
 * private volatile T $x
 *
 * T getX () {
 *   if ($x != null)
 *     return $x
 *   else {
 *     synchronized(this) {
 *        $x = new T ()
 *        return $x
 *     }
 *   }
 * }
 *
 * By default field will be initialized by call to default constructor
 *
 * If field has initial value expression then this expression will be used instead of default constructor.
 * In particularly it is possible to use closures <code>{ ... } ()</code> syntax
 *
 * <pre>
 * @Lazy T x = { [1,2,3] } ()
 *
 * becomes
 *
 * private T $x
 *
 * T getX () {
 *   if ($x != null)
 *     return $x
 *   else {
 *     $x = { [1, 2, 3] } ()
 *     return $x
 *   }
 * }
 *
 * </pre>
 *
 * <code>@Lazy(soft=true)</code> will use soft reference instead of the field and use above rules each time re-initialization required
 *
 * @author Alex Tkachman
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.LazyASTTransformation")
public @interface Lazy {
    /**
     * @return if field should be soft referenced instead of hard referenced
     */
    boolean soft () default false;
}