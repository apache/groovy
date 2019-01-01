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
package groovy.transform;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be added on a trait to declare the list of types that a class
 * implementing that trait is supposed to extend. This is useful when you want to be
 * able to call methods from the class implementing the trait without having to declare
 * all of them as members of the trait.
 *
 * Self types are particularly useful in combination with {@link groovy.transform.CompileStatic},
 * if you know that a trait can only be applied to a specific type but that the trait cannot extend
 * that type itself. For example, imagine the following code:
 * <pre><code>
 *     class Component { void methodInComponent() }
 *     trait ComponentDecorator {
 *         void logAndCall() {
 *             println "Calling method in component"
 *             methodInComponent()
 *         }
 *         // other useful methods
 *     }
 *     class DecoratedComponent extends Component implements ComponentDecorator {}
 * </code></pre>
 *
 * This will work because the trait uses the dynamic backend, so there is no check at
 * compile time that the <i>methodInComponent</i> call in <i>logAndCall</i> is actually
 * defined. If you annotate the trait with {@link groovy.transform.CompileStatic}, compilation
 * will fail because the trait does not define the method. To declare that the trait can be
 * applied on something that will extend <i>Component</i>, you need to add the <i>SelfType</i>
 * annotation like this:
 * <pre><code>
 *     class Component { void methodInComponent() }
 *
 *     {@literal @}CompileStatic
 *     {@literal @}SelfType(Component)
 *     trait ComponentDecorator {
 *         void logAndCall() {
 *             println "Calling method in component"
 *             methodInComponent()
 *         }
 *         // other useful methods
 *     }
 *     class DecoratedComponent extends Component implements ComponentDecorator {}
 * </code></pre>
 *
 * This pattern can therefore be used to avoid explicit casts everywhere you need to call a method
 * that you know is defined in the class that will implement the trait but normally don't have access
 * to, which is often the case where a trait needs to be applied on a class provided by a third-party
 * library.
 *
 * @since 2.4.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SelfType {
    Class[] value();
}
