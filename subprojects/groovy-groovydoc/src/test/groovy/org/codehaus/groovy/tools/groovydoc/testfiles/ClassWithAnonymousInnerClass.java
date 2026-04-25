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
package org.codehaus.groovy.tools.groovydoc.testfiles;

public class ClassWithAnonymousInnerClass {
    private final Supplier anonymous = new Supplier() {
        private final String hiddenField = "hidden";

        @Override
        public int getType() {
            return hiddenField.length();
        }

        @Override
        public Object getValue() {
            return hiddenField;
        }

        class HiddenType {
            Object reveal() {
                return hiddenField;
            }
        }
    };

    public Supplier inout(final Supplier supplier) {
        return new Supplier() {
            @Override
            public int getType() {
                return supplier.getType();
            }

            @Override
            public Object getValue() {
                return supplier.getValue();
            }
        };
    }

    public int visibleMethod() {
        return anonymous.getType();
    }

    public static class NamedNested {
        public String visibleNestedMethod() {
            return "visible";
        }
    }

    public interface Supplier {
        int getType();
        Object getValue();
    }
}
