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

public class JavaNestedResolutionOuter {
    /** Same-package nested target. */
    public static class SamePackageHelper {
    }

    /** Explicit-import nested target. */
    public static class ImportedHelper {
    }

    /** Enclosing nested owner used to resolve sibling nested types. */
    public static class Enclosing {
        /** Nested sibling target. */
        public static class Sibling {
        }

        /** Nested consumer that references a sibling by its simple source name. */
        public static class Consumer {
            /**
             * Returns the sibling helper type.
             *
             * @return the sibling helper type
             */
            public Sibling sibling() {
                return null;
            }
        }
    }
}
