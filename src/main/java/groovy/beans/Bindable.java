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
package groovy.beans;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a groovy property or a class.
 *
 * When annotating a property it indicates that the property should be a
 * bound property according to the JavaBeans spec, announcing to listeners
 * that the value has changed.
 * <p>
 * When annotating a class it indicates that all groovy properties in that
 * class should be bound as though each property had the annotation (even
 * if it already has it explicitly).
 * <p>
 * It is a compilation error to place this annotation on a field (that is
 * not a property, i.e. has scope visibility modifiers).
 * <p>
 * If a property with a user defined setter method is annotated the code
 * block is wrapped with the needed code to fire off the event.
 * <p>
 * The following example shows how you can use this annotation on fields
 * of a class: 
 * <pre>
 * class Person {
 *    &#064;groovy.beans.Bindable
 *    String firstName
 *
 *    &#064;groovy.beans.Bindable
 *    def zipCode
 * }
 * </pre>
 * The above example will generate code that is similar to the next snippet. 
 * Notice the difference between a String property and a def/Object property: 
 * <pre>
 * public class Person { 
 *     &#064;groovy.beans.Bindable
 *     private java.lang.String firstName 
 *     &#064;groovy.beans.Bindable
 *     private java.lang.Object zipCode 
 *     final private java.beans.PropertyChangeSupport this$propertyChangeSupport 
 * 
 *     public Person() {
 *         this$propertyChangeSupport = new java.beans.PropertyChangeSupport(this)
 *     }
 * 
 *     public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
 *         this$propertyChangeSupport.addPropertyChangeListener(listener)
 *     }
 * 
 *     public void addPropertyChangeListener(java.lang.String name, java.beans.PropertyChangeListener listener) {
 *         this$propertyChangeSupport.addPropertyChangeListener(name, listener)
 *     }
 * 
 *     public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
 *         this$propertyChangeSupport.removePropertyChangeListener(listener)
 *     }
 * 
 *     public void removePropertyChangeListener(java.lang.String name, java.beans.PropertyChangeListener listener) {
 *         this$propertyChangeSupport.removePropertyChangeListener(name, listener)
 *     }
 * 
 *     public void firePropertyChange(java.lang.String name, java.lang.Object oldValue, java.lang.Object newValue) {
 *         this$propertyChangeSupport.firePropertyChange(name, oldValue, newValue)
 *     }
 * 
 *     public java.beans.PropertyChangeListener[] getPropertyChangeListeners() {
 *         return this$propertyChangeSupport.getPropertyChangeListeners()
 *     }
 * 
 *     public java.beans.PropertyChangeListener[] getPropertyChangeListeners(java.lang.String name) {
 *         return this$propertyChangeSupport.getPropertyChangeListeners(name)
 *     }
 * 
 *     public void setFirstName(java.lang.String value) {
 *         this.firePropertyChange('firstName', firstName, firstName = value )
 *     }
 * 
 *     public void setZipCode(java.lang.Object value) {
 *         this.firePropertyChange('zipCode', zipCode, zipCode = value )
 *     }
 * }
 * </pre>
 *
 * @see BindableASTTransformation
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.TYPE})
@GroovyASTTransformationClass("groovy.beans.BindableASTTransformation")
public @interface Bindable {
}
