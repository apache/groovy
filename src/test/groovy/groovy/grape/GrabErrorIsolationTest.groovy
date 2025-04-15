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
package groovy.grape

import org.junit.Test

/**
 * Test for GROOVY-3853.
 */
class GrabErrorIsolationTest {

    @Test
    void testConsecutiveGrabCallsWithASharedLoaderWhereFirstGrabFails() {
        def classLoader = new GroovyClassLoader()

        grabThatShouldFail(classLoader)

        // prior to fix this call failed because GrapeIvy maintained state from previous failure
        grabThatShouldGoThrough(classLoader)
    }

    def grabThatShouldFail(classLoader) {
        try {
            // just an unlikely grab, that's all
            classLoader.parseClass """
                @Grab(group="roshan", module="dawrani", version="0.0.7")
                class Foo3853V1 {}
            """
            fail('This @Grab usage should have failed')
        } catch (ex) {
            // fine if it failed
            assert ex.message.contains('unresolved dependency')
        }
    }

    def grabThatShouldGoThrough(classLoader) {
        classLoader.parseClass """
            @Grab(group="junit", module="junit", version="4.7")
            class Foo3853V2 {}
        """
    }
}
