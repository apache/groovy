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
package groovy.transform.stc;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

/**
 * <p>A hint used to instruct the type checker to pick the first parameter type. For example:</p>
 * <code>public &lt;T&gt; def doWith(T src, @ClosureParams(FirstParam.class) Closure c) { c.call(src); }</code>
 *
 * <p>This class has several inner classes that also helps picking generic argument types instead of the parameter type.</p>
 *
 * @since 2.3.0
 */
public class FirstParam extends PickAnyArgumentHint {
    public FirstParam() {
        super(0,-1);
    }

    /**
     * <p>A hint used to instruct the type checker to pick the first generic type of the first parameter type. For example:</p>
     * <code>void &lt;T&gt; doWithElements(List&lt;T&gt; src, @ClosureParams(FirstParam.FirstGenericType.class) Closure c) { src.each { c.call(it) } }</code>
     *
     * @since 2.3.0
     */
    public static class FirstGenericType extends PickAnyArgumentHint {
        public FirstGenericType() {
            super(0,0);
        }
    }

    /**
     * <p>A hint used to instruct the type checker to pick the second generic type of the first parameter type. For example:</p>
     * <code>void &lt;T,U&gt; doWithElements(Tuple&lt;T,U&gt; src, @ClosureParams(FirstParam.SecondGenericType.class) Closure c) { src.each { c.call(it) } }</code>
     *
     * @since 2.3.0
     */
    public static class SecondGenericType extends PickAnyArgumentHint {
        public SecondGenericType() {
            super(0,1);
        }
    }

    /**
     * <p>A hint used to instruct the type checker to pick the third generic type of the first parameter type. For example:</p>
     * <code>void &lt;T,U,V&gt; doWithElements(Triple&lt;T,U,V&gt; src, @ClosureParams(FirstParam.ThirdGenericType.class) Closure c) { src.each { c.call(it) } }</code>
     *
     * @since 2.3.0
     */
    public static class ThirdGenericType extends PickAnyArgumentHint {
        public ThirdGenericType() {
            super(0,2);
        }
    }

    /**
     * <p>A hint used to instruct the type checker to pick the type of the component of the first parameter type, which is therefore
     * expected to be an array, like in this example:</p>
     * <code>void &lt;T&gt; doWithArray(T[] array, @ClosureParams(FirstParam.Component.class) Closure c) { array.each { c.call(it)} }</code>
     */
    public static class Component extends FirstParam {
        @Override
        public ClassNode[] getParameterTypes(final MethodNode node, final String[] options, final SourceUnit sourceUnit, final CompilationUnit unit, final ASTNode usage) {
            final ClassNode[] parameterTypes = super.getParameterTypes(node, options, sourceUnit, unit, usage);
            parameterTypes[0] = parameterTypes[0].getComponentType();
            return parameterTypes;
        }
    }
}
