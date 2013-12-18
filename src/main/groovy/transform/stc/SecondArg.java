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

public class SecondArg extends PickAnyArgumentHint {
    public SecondArg() {
        super(1,-1);
    }

    public static class FirstGenericType extends PickAnyArgumentHint {
        public FirstGenericType() {
            super(1,0);
        }
    }
    public static class SecondGenericType extends PickAnyArgumentHint {
        public SecondGenericType() {
            super(1,1);
        }
    }
    public static class ThirdGenericType extends PickAnyArgumentHint {
        public ThirdGenericType() {
            super(1,2);
        }
    }

    public static class Component extends SecondArg {
        @Override
        public ClassNode[] getParameterTypes(final MethodNode node, final String[] options) {
            final ClassNode[] parameterTypes = super.getParameterTypes(node, options);
            parameterTypes[0] = parameterTypes[0].getComponentType();
            return parameterTypes;
        }
    }
}
