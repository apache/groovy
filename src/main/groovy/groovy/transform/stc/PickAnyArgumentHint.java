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
 * <p>Base class for hints which use the type of a parameter of the annotated method as the signature.
 * This can optionally use a generic type of the selected parameter as the hint. For example, imagine the following
 * method:</p>
 * <code>void foo(A firstArg, B secondArg, Closure c) {...}</code>
 * <p>If the <i>c</i> closure should be <code>{ B it -> ...}</code>, then we can see that the parameter type
 * should be picked from the second parameter of the foo method, which is what {@link groovy.transform.stc.PickAnyArgumentHint}
 * lets you do.</p>
 * <p>Alternatively, the method may look like this:</p>
 * <code>void &lt;T&gt; foo(A&lt;T&gt; firstArg, B secondArg, Closure c) {...}</code>
 * <p>in which case if you want to express the fact that <i>c</i> should accept a &lt;T&gt; then you can use the
 * {@link #genericTypeIndex} value.</p>
 * <p></p>
 * <p>This class is extended by several hint providers that make it easier to use as annotation values.</p>
 *
 * @since 2.3.0
 */
public class PickAnyArgumentHint extends SingleSignatureClosureHint {
    private final int parameterIndex;
    private final int genericTypeIndex;

    /**
     * Creates the an argument picker which extracts the type of the first parameter.
     */
    public PickAnyArgumentHint() {
        this(0,-1);
    }

    /**
     * Creates a picker which will extract the parameterIndex-th parameter type, or its
     * genericTypeIndex-th generic type genericTypeIndex is &gt;=0.
     * @param parameterIndex the index of the parameter from which to extract the type
     * @param genericTypeIndex if &gt;=0, then returns the corresponding generic type instead of the parameter type.
     */
    public PickAnyArgumentHint(final int parameterIndex, final int genericTypeIndex) {
        this.parameterIndex = parameterIndex;
        this.genericTypeIndex = genericTypeIndex;
    }

    @Override
    public ClassNode[] getParameterTypes(final MethodNode node, final String[] options, final SourceUnit sourceUnit, final CompilationUnit unit, final ASTNode usage) {
        ClassNode type = node.getParameters()[parameterIndex].getOriginType();
        if (genericTypeIndex>=0) {
            type = pickGenericType(type, genericTypeIndex);
        }
        return new ClassNode[]{type};
    }
}
