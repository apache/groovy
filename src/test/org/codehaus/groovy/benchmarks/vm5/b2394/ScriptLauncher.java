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
package org.codehaus.groovy.benchmarks.vm5.b2394;

import groovy.lang.Binding;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.concurrent.CountDownLatch;

public class ScriptLauncher extends Thread
{
    Class scriptClass;

    Script script;

    int numIter;

    CountDownLatch latch;
    public final long[] tids;

    public ScriptLauncher(Class scriptClass, int numIter, CountDownLatch latch, long[] tids)
    {
        this.tids = tids;
        this.scriptClass = scriptClass;
        this.numIter = numIter;
        this.latch = latch;

    }

    public void run()
    {
        final long id = Thread.currentThread().getId();

        // run the script numIter times
        for (int i = 0; i < numIter; i++)
        {
            org.codehaus.groovy.benchmarks.vm5.b2394.Builder builder = new org.codehaus.groovy.benchmarks.vm5.b2394.Builder();

            Binding binding = new Binding();
            binding.setVariable("builder", builder);

            script = InvokerHelper.createScript(scriptClass, binding);

            script.run();
        }

        latch.countDown();
    }
}
