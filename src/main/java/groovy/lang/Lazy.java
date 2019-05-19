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
 * Field annotation to simplify lazy initialization.
 * <p>
 * Example usage without any special modifiers just defers initialization until the first call but is not thread-safe:
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
 *       $x = new T()
 *       return $x
 *    }
 * }
 * </pre>
 *
 * If the field is declared volatile then initialization will be synchronized using
 * the <a href="http://en.wikipedia.org/wiki/Double-checked_locking">double-checked locking</a> pattern as shown here:
 *
 * <pre>
 * {@code @Lazy} volatile T x
 * </pre>
 * becomes
 * <pre>
 * private volatile T $x
 *
 * T getX() {
 *    T $x_local = $x
 *    if ($x_local != null)
 *       return $x_local
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
 * {@code @Lazy} T x = { [1, 2, 3] } ()
 * </pre>
 * becomes
 * <pre>
 * private T $x
 *
 * T getX() {
 *    T $x_local = $x
 *    if ($x_local != null)
 *       return $x_local
 *    else {
 *       synchronized(this) {
 *          if ($x == null) {
 *             $x = { [1, 2, 3] } ()
 *          }
 *          return $x
 *       }
 *    }
 * }
 * </pre>
 * <p>
 * <code>@Lazy(soft=true)</code> will use a soft reference instead of the field and use the above rules each time re-initialization is required.
 * <p>
 * If the <code>soft</code> flag for the annotation is not set but the field is static, then
 * the <a href="http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom">initialization on demand holder idiom</a> is
 * used as follows:
 * <pre>
 * {@code @Lazy} static FieldType field
 * {@code @Lazy} static Date date1
 * {@code @Lazy} static Date date2 = { new Date().copyWith(year: 2000) }()
 * {@code @Lazy} static Date date3 = new GregorianCalendar(2009, Calendar.JANUARY, 1).time
 * </pre>
 * becomes these methods and inners classes within the class containing the above definitions:
 * <pre>
 * private static class FieldTypeHolder_field {
 *     private static final FieldType INSTANCE = new FieldType()
 * }
 *
 * private static class DateHolder_date1 {
 *     private static final Date INSTANCE = new Date()
 * }
 *
 * private static class DateHolder_date2 {
 *     private static final Date INSTANCE = { new Date().copyWith(year: 2000) }()
 * }
 *
 * private static class DateHolder_date3 {
 *     private static final Date INSTANCE = new GregorianCalendar(2009, Calendar.JANUARY, 1).time
 * }
 *
 * static FieldType getField() {
 *     return FieldTypeHolder_field.INSTANCE
 * }
 *
 * static Date getDate1() {
 *     return DateHolder_date1.INSTANCE
 * }
 *
 * static Date getDate2() {
 *     return DateHolder_date2.INSTANCE
 * }
 *
 * static Date getDate3() {
 *     return DateHolder_date3.INSTANCE
 * }
 * </pre>
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.LazyASTTransformation")
public @interface Lazy {
    /**
     * @return if field should be soft referenced instead of hard referenced
     */
    boolean soft () default false;
}
