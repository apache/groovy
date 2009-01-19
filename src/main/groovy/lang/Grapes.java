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
 */
public @interface Grapes {
    Grab[] value();

    /**
     * This will be pushed into the child grab annotations if the value is not
     * set in the child annotaiton already.
     *
     * This results in an effective change in the default value, which each &#064;Grab 
     * can still override
     */
    boolean initClass() default true;
}
