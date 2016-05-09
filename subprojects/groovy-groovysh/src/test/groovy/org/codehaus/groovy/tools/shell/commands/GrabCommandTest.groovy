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
package org.codehaus.groovy.tools.shell.commands

/**
 * Tests for the {@link GrabCommand} class.
 *
 */
class GrabCommandTest
    extends CommandTestSupport
{
    void testDisplay() {
        shell.execute(GrabCommand.COMMAND_NAME)
    }

    void testBuildSpec() {
        GrabCommand grabCommand = new GrabCommand(shell)
        assert 'group:\'\', module:\'foo\', version:\'\'' == grabCommand.buildSpec(['foo'])
        assert 'group:\'foo\', module:\'bar\', version:\'\'' == grabCommand.buildSpec(['foo', 'bar'])
        assert 'group:\'foo\', module:\'bar\', version:\'baz\'' == grabCommand.buildSpec(['foo', 'bar', 'baz'])
        assert 'group:\'\', module:\'foo\', version:\'\'' == grabCommand.buildSpec([':foo'])
        assert 'group:\'\', module:\'foo\', version:\'\'' == grabCommand.buildSpec([':foo:'])
        assert 'group:\'foo\', module:\'bar\', version:\'\'' == grabCommand.buildSpec(['foo:bar'])
        assert 'group:\'foo\', module:\'bar\', version:\'baz\'' == grabCommand.buildSpec(['foo:bar:baz'])
    }
}
