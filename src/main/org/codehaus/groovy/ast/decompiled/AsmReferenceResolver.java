/*
 * Copyright 2003-2014 the original author or authors.
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

package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.ClassNodeResolver;
import org.codehaus.groovy.control.CompilationUnit;
import org.objectweb.asm.Type;

/**
 * @author Peter Gromov
 */
public class AsmReferenceResolver {
    private final ClassNodeResolver resolver;
    private final CompilationUnit unit;

    public AsmReferenceResolver(ClassNodeResolver resolver, CompilationUnit unit) {
        this.resolver = resolver;
        this.unit = unit;
    }

    public ClassNode resolveClass(String className) {
        ClassNodeResolver.LookupResult lookupResult = resolver.resolveName(className, unit);
        if (lookupResult == null || lookupResult.getClassNode() == null) {
            throw new NoClassDefFoundError(className);
        }

        return lookupResult.getClassNode();

    }

    public ClassNode resolveType(Type type) {
        if (type.getSort() == Type.ARRAY) {
            ClassNode result = resolveNonArrayType(type.getElementType());
            for (int i = 0; i < type.getDimensions(); i++) {
                result = result.makeArray();
            }
            return result;
        }

        return resolveNonArrayType(type);
    }

    private ClassNode resolveNonArrayType(Type type) {
        String className = type.getClassName();
        if (type.getSort() != Type.OBJECT) {
            return ClassHelper.make(className);
        }

        return resolveClass(className);
    }

}
