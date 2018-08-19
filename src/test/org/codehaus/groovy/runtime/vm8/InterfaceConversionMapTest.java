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
package org.codehaus.groovy.runtime.vm8;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class InterfaceConversionMapTest {

    private final GroovyShell shell;

    public InterfaceConversionMapTest() {
        final CompilerConfiguration cc = new CompilerConfiguration();
        cc.setTargetBytecode(CompilerConfiguration.JDK8);
        shell = new GroovyShell(cc);
    }

    // GROOVY-7330
    @Test
    public void testMapToProxy() {
        final Map map = (Map) shell.evaluate("[x: {10}, y: {20}]");
        final SomeInterface si = DefaultGroovyMethods.asType(map, SomeInterface.class);
        Assert.assertEquals(20, si.y());
        Assert.assertEquals(10, si.x());
    }

    public interface SomeInterface {
        default int x() {
            return 1;
        }

        int y();
    }
}
