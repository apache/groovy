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
package org.codehaus.groovy.transform.stc.acc;

/**
 * Java fixtures whose getter modifiers Groovy source cannot spell precisely.
 * A subclass property can then be checked against each replacement rule.
 */
public class AccessorBases {

    public static class PublicFinal {
        public final boolean getFlag() {
            return true;
        }
    }

    public static class PublicOpen {
        public boolean getFlag() {
            return true;
        }
    }

    public static class PackageFinal {
        final boolean getFlag() {
            return true;
        }
    }

    public static class PrivateFinal {
        private final boolean getFlag() {
            return true;
        }
    }
}
