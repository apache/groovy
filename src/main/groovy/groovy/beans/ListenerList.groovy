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
package groovy.beans

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * This annotation adds Java-style listener support to a class based on an annotated Collection-property.
 * <p>
 * For any given Collection property, several methods will be written into the enclosing class during the compile phase. These
 * changes are visible from Java or other languages. The List is intended to hold listeners of some sort, and the methods
 * addListener, removeListener, and getListeners are all added to the class. The actual methods names depend on the generic
 * type of the collection.
 * <p>
 * Given the following example:<br>
 * <pre>
 * class MyClass {
 *     &#064;groovy.beans.ListenerList
 *     List&lt;java.awt.event.ActionListener&gt; listeners
 * }
 * </pre>
 * The following code is generated:
 * <pre>
 * public class MyClass extends java.lang.Object {
 *     &#064;groovy.beans.ListenerList
 *     private java.util.List&lt;java.awt.event.ActionListener&gt; listeners
 *
 *     public void addActionListener(java.awt.event.ActionListener listener) {
 *         if ( listener == null) {
 *             return null
 *         }
 *         if ( listeners == null) {
 *             listeners = []
 *         }
 *         listeners.add(listener)
 *     }
 *
 *     public void removeActionListener(java.awt.event.ActionListener listener) {
 *         if ( listener == null) {
 *             return null
 *         }
 *         if ( listeners == null) {
 *             listeners = []
 *         }
 *         listeners.remove(listener)
 *     }
 *
 *     public java.awt.event.ActionListener[] getActionListeners() {
 *         java.lang.Object __result = []
 *         if ( listeners != null) {
 *             __result.addAll(listeners)
 *         }
 *         return (( __result ) as java.awt.event.ActionListener[])
 *     }
 *
 *     public void fireActionPerformed(java.awt.event.ActionEvent param0) {
 *         if ( listeners != null) {
 *             def __list = new java.util.ArrayList(listeners)
 *             for (java.lang.Object listener : __list ) {
 *                 listener.actionPerformed(param0)
 *             }
 *         }
 *     }
 * }
 * </pre>
 * A fire method is created for each public method in the target class. In this case, ActionListener only has one
 * method. For a four method interface, four fire methods would be created.
 * <p>
 * The annotation can take the following parameters:
 * <pre>
 * name        = a suffix for creating the add, remove, and get methods.
 *               Default: Name of the listener type
 *               In the above example, if name is set to MyListener, then the class will have an addMyListener,
 *               removeMyListener, and getMyListeners methods. 
 *
 * synchronize = Whether or not the methods created should be synchronized at the method level. 
 *               Default: false
 * </pre>
 * <p>
 * <strong>Compilation Errors</strong> - Using this annotation incorrectly results in compilation errors rather
 * than runtime errors. A list of potential problems includes:
 * <ul>
 * <li>This annotation can only be applied to a field of type Collection</li>
 * <li>The annotated Collection field must have a generic type</li>
 * <li>The annotated Collection field must not have a generic wildcard declared</li>
 * <li>The generated methods must not already exist</li>
 * </ul>
 *
 * @see ListenerListASTTransformation
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@GroovyASTTransformationClass('groovy.beans.ListenerListASTTransformation')
@interface ListenerList {
    /**
     * A suffix for creating the add, remove, and get methods
     * defaulting to the name of the listener type, e.g. if name is set to MyListener,
     * then the class will have addMyListener, removeMyListener, and getMyListeners methods.
     */
    String name() default ""

    /**
     * Whether or not the methods created should be synchronized at the method level.
     */
    boolean synchronize() default false
}
