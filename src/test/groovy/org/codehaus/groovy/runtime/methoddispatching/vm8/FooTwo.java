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
package org.codehaus.groovy.runtime.methoddispatching.vm8;

import org.codehaus.groovy.runtime.metaclass.MetaMethodIndex;

/**
 * To test the case when we call a static method on a class and {@link MetaMethodIndex.Cache}
 * contains more than one method from interface already
 */
interface FooTwo {
    static String foo() {
        return "FooTwo.foo()";
    }

    static String foo(int a) {
        return String.format("FooTwo.foo(%1$d)", a);
    }
}

class BarTwo implements FooTwo {
    static String foo() {
        return "BarTwo.foo()";
    }

    static String foo(int a) {
        return String.format("BarTwo.foo(%1$d)", a);
    }
}
