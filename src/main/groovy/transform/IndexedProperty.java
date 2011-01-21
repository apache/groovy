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

package groovy.transform;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation used with properties to provide an indexed getter and setter for the property.
 * Groovy provides nice GPath syntax support for accessing indexed properties but Java tools
 * or frameworks may expect the JavaBean style setters and getters.
 * <p>
 * <em>Example usage:</em> suppose you have a class with the following properties:
 * <pre>
 * {@code @IndexedProperty} FieldType[] someField
 * {@code @IndexedProperty} List<FieldType> otherField
 * {@code @IndexedProperty} List furtherField
 * </pre>
 * will add the following methods to the class containing the properties:
 * <pre>
 * FieldType getSomeField(int index) {
 *     someField[index]
 * }
 * FieldType getOtherField(int index) {
 *     otherField[index]
 * }
 * Object getFurtherField(int index) {
 *     furtherField[index]
 * }
 * void setSomeField(int index, FieldType val) {
 *     someField[index] = val
 * }
 * void setOtherField(int index, FieldType val) {
 *     otherField[index] = val
 * }
 * void setFurtherField(int index, Object val) {
 *     furtherField[index] = val
 * }
 * </pre>
 * Normal Groovy visibility rules for properties apply
 * (i.e. no <code>public</code>, <code>private</code> or <code>package</code>
 * visibility can be specified) or you will receive a compile-time error message.
 * The normal Groovy property getters and setters will also be created.
 * <p>
 *
 * @author Paul King
 * @since 1.7.3
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.IndexedPropertyASTTransformation")
public @interface IndexedProperty {
}
