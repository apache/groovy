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
package org.codehaus.groovy.tools.stubgenerator

/**
 * A synthetic static method $getStaticMetaClass() was part of the stubs generated,
 * which shouldn't be the case, as this method should never be used directly.
 */
class NoStaticGetMetaClassSyntheticMethodInStubsTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'SimplePogo.groovy': 'class SimplePogo {}',
                'PogoUser.java': 'class PogoUser { static void main(String[] args) { new SimplePogo(); } }'
        ]
    }

    void verifyStubs() {
        // the synthetic method should not be present
        assert !classes['SimplePogo'].methods['$getStaticMetaClass']
    }
}
