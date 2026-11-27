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
package org.apache.groovy.bench

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.InvokerHelper

import java.util.function.Function

/**
 * Helper class for benchmarking call syntax on functional values in dynamic code.
 */
class FunctionalValueCall {

    @CompileStatic
    static Function<Integer, Integer> lambda() {
        Function<Integer, Integer> f = (Integer i) -> i + 1
        f
    }

    static Function<Integer, Integer> proxy() {
        { Integer i -> i + 1 } as Function<Integer, Integer>
    }

    static Closure<Integer> closure() {
        { Integer i -> i + 1 }
    }

    /** Dynamic {@code f(x)}: a closure call, or call syntax on a functional value. */
    static callSyntax(f, x) {
        f(x)
    }

    /** Dynamic call of the abstract method by its name. */
    static apply(f, x) {
        f.apply(x)
    }

    /** What {@code f(x)} costs a functional value when the call site is not bound to its method. */
    static callThroughMetaClass(f, x) {
        InvokerHelper.invokeMethod(f, 'call', x)
    }
}
