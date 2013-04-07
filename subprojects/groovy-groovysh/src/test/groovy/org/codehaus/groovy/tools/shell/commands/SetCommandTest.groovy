/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.commands

import groovy.mock.interceptor.MockFor
import org.codehaus.groovy.tools.shell.util.Preferences
import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * Tests for the {@link SetCommand} class.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class SetCommandTest
    extends CommandTestSupport
{
    void testSet() {
        shell << 'set'
    }

    void testComplete() {

        MockFor preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.keys(1) { [:] }
        preferencesMocker.demand.getVERBOSITY_KEY(1) { 'k1' }
        preferencesMocker.demand.getEDITOR_KEY(1) { 'k2' }
        preferencesMocker.demand.getPARSER_FLAVOR_KEY(1) { 'k3' }
        preferencesMocker.demand.getSANITIZE_STACK_TRACE_KEY(1) { 'k4' }
        preferencesMocker.demand.getSHOW_LAST_RESULT_KEY(1) { 'k5' }
        preferencesMocker.use {
            SetCommand command = new SetCommand(shell)
            ArrayList<SimpleCompletor> completors = command.createCompletors()
            assertEquals(2, completors.size())
            List<String> candidates = []
            assertEquals(0, completors[0].complete("", 0, candidates))
            assertEquals(["k1", "k2", "k3", "k4", "k5"], candidates)
        }
    }
}
