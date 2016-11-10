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
package org.codehaus.groovy.classgen.asm.sc.bugs.support;

public class Groovy7363Support {
    public interface A<T, U extends B<T>> {
        U getB();
    }

    public static class ABC implements A<C, BC> {
        public void setB(BC b) {}

        @Override
        public BC getB() {
            return new BC();
        }
    }

    public interface B<T> {
        T getObject();
    }

    public static class BC implements B<C> {
        @Override
        public C getObject() {
            return new C();
        }
    }

    public static class C {
        public long getValue() {
            return 42;
        }
    }
}
