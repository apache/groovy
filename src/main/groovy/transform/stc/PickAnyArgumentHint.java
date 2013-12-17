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

public class PickAnyArgumentHint extends AbstractSingleClosureSignatureHint {
    private final int parameterIndex;
    private final int genericTypeIndex;

    public PickAnyArgumentHint() {
        this(0,-1);
    }

    public PickAnyArgumentHint(final int parameterIndex, final int genericTypeIndex) {
        this.parameterIndex = parameterIndex;
        this.genericTypeIndex = genericTypeIndex;
    }

    @Override
    public ClassNode[] getParameterTypes(final MethodNode node, final String[] options) {
        ClassNode type = node.getParameters()[parameterIndex].getOriginType();
        if (genericTypeIndex>=0) {
            type = pickGenericType(type, genericTypeIndex);
        }
        return new ClassNode[]{type};
    }
}
