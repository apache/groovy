/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;

/**
 * Utility methods to deal with generic types.
 *
 * @author Cedric Champeau
 */
public class GenericsUtils {
    public static final GenericsType[] EMPTY_GENERICS_ARRAY = new GenericsType[0];

    /**
     * Given a parameterized type and a generic type information, aligns actual type parameters. For example, if a
     * class uses generic type <pre>&lt;T,U,V&gt;</pre> (redirectGenericTypes), is used with actual type parameters
     * <pre>&lt;java.lang.String, U,V&gt;</pre>, then a class or interface using generic types <pre>&lt;T,V&gt;</pre>
     * will be aligned to <pre>&lt;java.lang.String,V&gt;</pre>
     * @param redirectGenericTypes the type arguments or the redirect class node
     * @param parameterizedTypes the actual type arguments used on this class node
     * @param alignmentTarget the generic type arguments to which we want to align to
     * @return aligned type arguments
     */
    public static GenericsType[] alignGenericTypes(final GenericsType[] redirectGenericTypes, final GenericsType[] parameterizedTypes, final GenericsType[] alignmentTarget) {
        if (alignmentTarget==null) return EMPTY_GENERICS_ARRAY;
        if (parameterizedTypes==null || parameterizedTypes.length==0) return alignmentTarget;
        GenericsType[] generics = new GenericsType[alignmentTarget.length];
        for (int i = 0, scgtLength = alignmentTarget.length; i < scgtLength; i++) {
            final GenericsType superGenericType = alignmentTarget[i];
            GenericsType match = null;
            if (redirectGenericTypes!=null) {
                for (int j = 0; j < redirectGenericTypes.length && match == null; j++) {
                    GenericsType redirectGenericType = redirectGenericTypes[j];
                    if (redirectGenericType.isCompatibleWith(superGenericType.getType())) {
                        match = parameterizedTypes[j];
                    }
                }
            }
            if (match == null) {
                match = superGenericType;
            }
            generics[i]=match;
        }
        return generics;
    }

    /**
     * Generates a wildcard generic type in order to be used for checks against class nodes.
     * See {@link GenericsType#isCompatibleWith(org.codehaus.groovy.ast.ClassNode)}.
     * @param type the type to be used as the wildcard upper bound
     * @return a wildcard generics type
     */
    public static GenericsType buildWildcardType(final ClassNode type) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        GenericsType gt = new GenericsType(base, new ClassNode[]{type}, null);
        gt.setWildcard(true);
        return gt;
    }
}
