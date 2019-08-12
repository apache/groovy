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

import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;

import java.util.function.Function;

public class ParameterUtils {
    public static boolean parametersEqual(Parameter[] a, Parameter[] b) {
        return parametersEqual(a, b, false);
    }

    public static boolean parametersEqualWithWrapperType(Parameter[] a, Parameter[] b) {
        return parametersEqual(a, b, true);
    }

    /**
     * check whether parameters type are compatible
     *
     * each parameter should match the following condition:
     * {@code targetParameter.getType().getTypeClass().isAssignableFrom(sourceParameter.getType().getTypeClass())}
     *
     * @param source source parameters
     * @param target target parameters
     * @return the check result
     * @since 3.0.0
     */
    public static boolean parametersCompatible(Parameter[] source, Parameter[] target) {
        return parametersMatch(source, target,
                t -> ClassHelper.getWrapper(t.getV2()).getTypeClass()
                        .isAssignableFrom(ClassHelper.getWrapper(t.getV1()).getTypeClass())
        );
    }

    private static boolean parametersEqual(Parameter[] a, Parameter[] b, final boolean wrapType) {
        return parametersMatch(a, b, t -> {
            ClassNode v1 = t.getV1();
            ClassNode v2 = t.getV2();

            if (wrapType) {
                v1 = ClassHelper.getWrapper(v1);
                v2 = ClassHelper.getWrapper(v2);
            }

            return v1.equals(v2);
        });
    }

    private static boolean parametersMatch(Parameter[] a, Parameter[] b, Function<Tuple2<ClassNode, ClassNode>, Boolean> typeChecker) {
        if (a.length == b.length) {
            boolean answer = true;
            for (int i = 0; i < a.length; i++) {
                ClassNode aType = a[i].getType();
                ClassNode bType = b[i].getType();

                if (!typeChecker.apply(Tuple.tuple(aType, bType))) {
                    answer = false;
                    break;
                }
            }
            return answer;
        }
        return false;
    }
}
