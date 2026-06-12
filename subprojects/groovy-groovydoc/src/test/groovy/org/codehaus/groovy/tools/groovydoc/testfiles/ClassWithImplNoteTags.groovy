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
package org.codehaus.groovy.tools.groovydoc.testfiles

/**
 * GROOVY-11945 fixture: a method documented with the three Javadoc "note"
 * tags standardised since JEP 172, alongside ordinary block tags.
 */
class ClassWithImplNoteTags {
    /**
     * Compute something.
     *
     * @apiNote Callers should null-check the result before use.
     * @implSpec Overriders must preserve the non-negative contract.
     * @implNote This implementation uses a sieve for efficiency.
     * @param n the input value
     * @return a non-negative integer
     */
    int compute(int n) {
        return Math.abs(n)
    }
}
