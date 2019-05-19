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
 * <p>
 * When annotating a property it indicates that the property should be a
 * constrained property according to the JavaBeans spec, subject to
 * listeners vetoing the property change.
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
 * Here is a simple example of how to annotate a class with Vetoable: 
 * <pre>
 * &#064;groovy.beans.Vetoable
 * class Person {
 *     String firstName
 *     def zipCode
 * }
 * </pre>
 * This code is transformed by the compiler into something resembling the following
 * snippet. Notice the difference between a String and a def/Object property: 
 * <pre>
 * public class Person implements groovy.lang.GroovyObject { 
 *     private java.lang.String firstName
 *     private java.lang.Object zipCode 
 *     final private java.beans.VetoableChangeSupport this$vetoableChangeSupport 
 * 
 *     public Person() {
 *         this$vetoableChangeSupport = new java.beans.VetoableChangeSupport(this)
 *     }
 * 
 *     public void addVetoableChangeListener(java.beans.VetoableChangeListener listener) {
 *         this$vetoableChangeSupport.addVetoableChangeListener(listener)
 *     }
 * 
 *     public void addVetoableChangeListener(java.lang.String name, java.beans.VetoableChangeListener listener) {
 *         this$vetoableChangeSupport.addVetoableChangeListener(name, listener)
 *     }
 * 
 *     public void removeVetoableChangeListener(java.beans.VetoableChangeListener listener) {
 *         this$vetoableChangeSupport.removeVetoableChangeListener(listener)
 *     }
 * 
 *     public void removeVetoableChangeListener(java.lang.String name, java.beans.VetoableChangeListener listener) {
 *         this$vetoableChangeSupport.removeVetoableChangeListener(name, listener)
 *     }
 * 
 *     public void fireVetoableChange(java.lang.String name, java.lang.Object oldValue, java.lang.Object newValue) throws java.beans.PropertyVetoException {
 *         this$vetoableChangeSupport.fireVetoableChange(name, oldValue, newValue)
 *     }
 * 
 *     public java.beans.VetoableChangeListener[] getVetoableChangeListeners() {
 *         return this$vetoableChangeSupport.getVetoableChangeListeners()
 *     }
 * 
 *     public java.beans.VetoableChangeListener[] getVetoableChangeListeners(java.lang.String name) {
 *         return this$vetoableChangeSupport.getVetoableChangeListeners(name)
 *     }
 * 
 *     public void setFirstName(java.lang.String value) throws java.beans.PropertyVetoException {
 *         this.fireVetoableChange('firstName', firstName, value)
 *         firstName = value 
 *     }
 * 
 *     public void setZipCode(java.lang.Object value) throws java.beans.PropertyVetoException {
 *         this.fireVetoableChange('zipCode', zipCode, value)
 *         zipCode = value 
 *     }
 * }
 * </pre>
 *
 * @see VetoableASTTransformation
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.TYPE})
@GroovyASTTransformationClass("groovy.beans.VetoableASTTransformation")
public @interface Vetoable {
}
