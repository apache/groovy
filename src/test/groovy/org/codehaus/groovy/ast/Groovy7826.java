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
package org.codehaus.groovy.ast;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.Test;

public final class Groovy7826 {

    @Test
    public void testComplexTypeArguments() throws Exception {
        String script = "def f(" + getClass().getName() + ".C1 c1) { }";

        CompilerConfiguration config = new CompilerConfiguration();
        config.getOptimizationOptions().put("asmResolving", false);

        GroovyShell shell = new GroovyShell(config);
        shell.evaluate(script, "bug7826.groovy");
    }

    public static class C1<T2 extends C2<T2, T1>, T1 extends C1<T2, T1>> {
    }

    public static class C2<T2 extends C2<T2, T1>, T1 extends C1<T2, T1>> {
    }
}
