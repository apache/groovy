/*
 * Copyright 2008-2010 the original author or authors.
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
 * Field annotation to simplify lazy initialization.
 * <p>
 * Example usage:
 * <pre>
 * {@code @Lazy} T x
 * </pre>
 * becomes
 * <pre>
 * private T $x
 *
 * T getX() {
 *    if ($x != null)
 *       return $x
 *    else {
 *        $x = new T()
 *        return $x
 *    }
 * }
 * </pre>
 *
 * If the field is declared volatile then initialization will be synchronized.
 *
 * <pre>
 * {@code @Lazy} volatile T x
 * </pre>
 * becomes
 * <pre>
 * private volatile T $x
 *
 * T getX() {
 *    if ($x != null)
 *       return $x
 *    else {
 *       synchronized(this) {
 *          if ($x == null) {
 *             $x = new T()
 *          }
 *          return $x
 *       }
 *    }
 * }
 * </pre>
 *
 * By default a field will be initialized by calling its default constructor.
 *
 * If the field has an initial value expression then this expression will be used instead of calling the default constructor.
 * In particular, it is possible to use closure <code>{ ... } ()</code> syntax as follows:
 *
 * <pre>
 * {@code @Lazy} T x = { [1,2,3] } ()
 * </pre>
 * becomes
 * <pre>
 * private T $x
 *
 * T getX() {
 *    if ($x != null)
 *       return $x
 *    else {
 *       synchronized(this) {
 *          if ($x == null) {
 *             $x = { [1,2,3] } ()
 *          }
 *          return $x
 *       }
 *    }
 * }
 * </pre>
 *
 * <code>@Lazy(soft=true)</code> will use a soft reference instead of the field and use the above rules each time re-initialization is required.
 *
 * @author Alex Tkachman
 * @author Paul King
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