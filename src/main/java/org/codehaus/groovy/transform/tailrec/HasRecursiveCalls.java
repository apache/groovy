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
package org.codehaus.groovy.transform.tailrec;

import org.apache.groovy.util.Maps;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.query.AstQuery;

/**
 * Check if there are any recursive calls in a method
 */
public class HasRecursiveCalls {

    /**
     * Tests whether the supplied method contains at least one recursive call.
     *
     * @param method the method to inspect
     * @return {@code true} if the method contains a recursive call; {@code false} otherwise
     */
    public boolean test(MethodNode method) {
        RecursivenessTester tester = new RecursivenessTester();
        return AstQuery.from(method.getCode())
                .descendants(MethodCallExpression.class, StaticMethodCallExpression.class)
                .where(call -> tester.isRecursive(Maps.of("method", method, "call", call)))
                .any();
    }
}
