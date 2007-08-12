/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package groovy.util

//
// Borrowed and augmented from GShell ( http://svn.apache.org/repos/asf/geronimo/sandbox/gshell/trunk/gshell-core/src/main/java/org/apache/geronimo/gshell/command/MessageSourceImpl.java )
//

/**
 * Unit tests for the {@link MessageSource} class.
 *
 * @version $Id$
 */
class MessageSourceTest
    extends GroovyTestCase
{
    void testLoadAndGetMessage() {
        def messages = new MessageSource(this.class)

        String a = messages.getMessage('a')
        assert '1' == a

        String b = messages.getMessage('b')
        assert '2' == b

        String c = messages.getMessage('c')
        assert '3' == c

        String f = messages.getMessage('f', a, b, c)
        assert '1 2 3' == f
    }
}