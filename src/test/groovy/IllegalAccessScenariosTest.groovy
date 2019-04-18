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
package groovy

import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.apache.groovy.util.SystemUtil.getBooleanSafe

/**
 * Tests for permissive member access. Typically such access is only allowed in Java via means such
 * as reflection.
 *
 * In JDK versions < 9, Groovy supports permissive access and no warnings are given by the JDK.
 * In JDK versions >= 9, Groovy supports permissive access but the JDK gives illegal access warnings.
 * At some point, the JDK may further restrict permissive access and Groovy's support for this feature may be limited.
 */
class IllegalAccessScenariosTest extends GroovyTestCase {
    void testPrivateFieldAccess() {
        if (isAtLeastJdk('9.0') && !getBooleanSafe('groovy.force.illegal.access')) return
        def items = [1, 2, 3]
        // size is a private field in ArrayList
        assert items.size == 3
    }
} 
