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
package groovy.transform.stc


/**
 * Unit tests for static type checking : categories.
 */
class CategoriesSTCTest extends StaticTypeCheckingTestCase {

    void testShouldNotAllowCategory() {
        shouldFailWithMessages '''import groovy.time.TimeCategory
            use(TimeCategory) {
                1.day
            }
        ''', 'Due to their dynamic nature, usage of categories is not possible with static type checking active', 'No such property: day for class: int'
    }

}

