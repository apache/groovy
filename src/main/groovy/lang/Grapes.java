/*
 * Copyright 2003-2008 the original author or authors.
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

/**
 * Sometimes we will need more than one grab per class, but we can only add
 * one annotation type per annotatable node.  This class allows for multiple
 * grabs to be added.
 *
 * For example:
 *
 * <pre>
 * {@code @Grapes([@Grab(module='m1'), @Grab(module='m2')])}
 * class AnnotatedClass { ... }
 * </pre>
 *
 * You can override an implicit transitive dependency by providing an explicit one.
 * E.g. htmlunit 2.6 normally uses xerces 2.9.1 but you can get 2.9.0 as follows:
 * <pre>
 * {@code @Grapes}([
 *     {@code @Grab}('net.sourceforge.htmlunit:htmlunit:2.6'),
 *     {@code @Grab}('xerces#xercesImpl;2.9.0'),
 * ])
 * </pre>
 * Obviously, only do this if you understand the consequences.
 * <p/>
 * You can also remove a transitive dependency altogether (provided you know you don't need them) using
 * {@code @GrabExclude}.
 * For the same example as above, here is how we would not grab the xerces dependency (htmlunit won't
 * work without it but in other cases it is sometimes useful to do):
 * <pre>
 * {@code @Grapes}([
 *     {@code @Grab}('net.sourceforge.htmlunit:htmlunit:2.6'),
 *     {@code @GrabExclude}('xerces:xercesImpl'),
 * ])
 * </pre>
 * It is sometimes also useful to use {@code @GrabConfig} to further adjust how dependencies
 * are grabbed. See {@code @GrabConfig} for further information.
 */
public @interface Grapes {
    Grab[] value();

    /**
     * This will be pushed into the child grab annotations if the value is not
     * set in the child annotation already.
     *
     * This results in an effective change in the default value, which each &#064;Grab 
     * can still override
     */
    boolean initClass() default true;
}
