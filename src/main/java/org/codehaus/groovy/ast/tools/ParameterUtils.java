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
package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.function.BiPredicate;

public class ParameterUtils {

    public static boolean parametersEqual(final Parameter[] a, final Parameter[] b) {
        return parametersEqual(a, b, false);
    }

    public static boolean parametersEqualWithWrapperType(final Parameter[] a, final Parameter[] b) {
        return parametersEqual(a, b, true);
    }

    /**
     * Checks compatibility of parameter arrays. Each parameter should match the
     * following condition: {@code sourceType.isAssignableTo(targetType)}
     *
     * @since 3.0.0
     */
    public static boolean parametersCompatible(final Parameter[] source, final Parameter[] target) {
        return parametersMatch(source, target, StaticTypeCheckingSupport::isAssignableTo);
    }

    //--------------------------------------------------------------------------

    private static boolean parametersEqual(final Parameter[] a, final Parameter[] b, final boolean wrapType) {
        return parametersMatch(a, b, (aType, bType) -> {
            if (wrapType) {
                aType = ClassHelper.getWrapper(aType);
                bType = ClassHelper.getWrapper(bType);
            }
            return aType.equals(bType);
        });
    }

    private static boolean parametersMatch(final Parameter[] a, final Parameter[] b, final BiPredicate<ClassNode, ClassNode> typeChecker) {
        if (a.length == b.length) {
            for (int i = 0, n = a.length; i < n; i += 1) {
                ClassNode aType = a[i].getType();
                ClassNode bType = b[i].getType();

                if (!typeChecker.test(aType, bType)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
