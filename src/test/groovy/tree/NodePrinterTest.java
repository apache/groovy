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
package groovy.tree;

import groovy.lang.GroovyObject;
import org.codehaus.groovy.classgen.TestSupport;

import java.util.logging.Logger;

public class NodePrinterTest extends TestSupport {

    public void testTree() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/TreeTest.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testVerboseTree() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/VerboseTreeTest.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testSmallTree() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/SmallTreeTest.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testLittleClosure() throws Exception {
        GroovyObject object = compile("src/test/groovy/LittleClosureTest.groovy");
        object.invokeMethod("testClosure", null);
    }

    public void testNestedClosureBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/NestedClosureBugTest.groovy");
        object.invokeMethod("testNestedClosureBug", null);
    }

    public void testClosureClassLoaderBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/ClosureClassLoaderBug.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testLogging() {
        Logger log = Logger.getLogger(getClass().getName());
        log.info("Logging using JDK 1.4 logging");
    }
}
