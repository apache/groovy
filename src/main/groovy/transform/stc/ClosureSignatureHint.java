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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;

import java.util.List;

public abstract class ClosureSignatureHint {

    public static ClassNode pickGenericType(ClassNode type, int gtIndex) {
        final GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes==null || genericsTypes.length<gtIndex) {
            return ClassHelper.OBJECT_TYPE;
        }
        return genericsTypes[gtIndex].getType();
    }

    public static ClassNode pickGenericType(MethodNode node, int parameterIndex, int gtIndex) {
        final Parameter[] parameters = node.getParameters();
        final ClassNode type = parameters[parameterIndex].getOriginType();
        return pickGenericType(type, gtIndex);
    }

    public abstract List<ClassNode[]> getClosureSignatures(MethodNode node, final String[] options);
}
