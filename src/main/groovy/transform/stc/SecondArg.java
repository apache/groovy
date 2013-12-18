/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * <p>A hint used to instruct the type checker to pick the second parameter type. For example:</p>
 * <code>public &lt;T,U&gt; def doWith(T first, U second, @ClosureParams(SecondArg.class) Closure c) { c.call(src); }</code>
 *
 * <p>This class has several inner classes that also helps picking generic argument types instead of the parameter type.</p>
 *
 * @author Cédric Champeau
 * @since 2.3.0
 */
public class SecondArg extends PickAnyArgumentHint {
    public SecondArg() {
        super(1,-1);
    }

    /**
     * <p>A hint used to instruct the type checker to pick the first generic type of the second parameter type. For example:</p>
     * <code>void &lt;T&gt; doWithElements(String base, List&lt;T&gt; src, @ClosureParams(SecondArg.FirstGenericType.class) Closure c) { ... } }</code>
     *
     * @author Cédric Champeau
     * @since 2.3.0
     */
    public static class FirstGenericType extends PickAnyArgumentHint {
        public FirstGenericType() {
            super(1,0);
        }
    }

    /**
     * <p>A hint used to instruct the type checker to pick the second generic type of the second parameter type. For example:</p>
     * <code>void &lt;T,U&gt; doWithElements(String base, Tuple&lt;T,U&gt; src, @ClosureParams(SecondArg.SecondGenericType.class) Closure c) { ... }</code>
     *
     * @author Cédric Champeau
     * @since 2.3.0
     */
    public static class SecondGenericType extends PickAnyArgumentHint {
        public SecondGenericType() {
            super(1,1);
        }
    }

    /**
     * <p>A hint used to instruct the type checker to pick the second generic type of the second parameter type. For example:</p>
     * <code>void &lt;T,U,V&gt; doWithElements(String base, Triple&lt;T,U,V&gt; src, @ClosureParams(SecondArg.ThirdGenericType.class) Closure c) { ... }</code>
     *
     * @author Cédric Champeau
     * @since 2.3.0
     */
    public static class ThirdGenericType extends PickAnyArgumentHint {
        public ThirdGenericType() {
            super(1,2);
        }
    }

    /**
     * <p>A hint used to instruct the type checker to pick the type of the component of the second parameter type, which is therefore
     * expected to be an array, like in this example:</p>
     * <code>void &lt;T&gt; doWithArray(String first, T[] array, @ClosureParams(FirstArg.Component.class) Closure c) { ... }</code>
     */
    public static class Component extends SecondArg {
        @Override
        public ClassNode[] getParameterTypes(final MethodNode node, final String[] options) {
            final ClassNode[] parameterTypes = super.getParameterTypes(node, options);
            parameterTypes[0] = parameterTypes[0].getComponentType();
            return parameterTypes;
        }
    }
}
