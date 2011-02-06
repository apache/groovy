/*
 * Copyright 2003-2011 the original author or authors.
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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;

import java.awt.HeadlessException;

/**
 * Tests dynamically compiling and running a new class
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class RunGroovyTest extends TestSupport {

    public void testArrayBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/ToArrayBugTest.groovy");
        object.invokeMethod("testToArrayBug", null);
    }


    public void testPostfix() throws Exception {
        GroovyObject object = compile("src/test/groovy/PostfixTest.groovy");
        object.invokeMethod("testIntegerPostfix", null);
    }

    public void testMap() throws Exception {
        GroovyObject object = compile("src/test/groovy/MapTest.groovy");
        object.invokeMethod("testMap", null);
    }

    public void testClosure() throws Exception {
        GroovyObject object = compile("src/test/groovy/ClosureMethodTest.groovy");
        object.invokeMethod("testListCollect", null);
    }

    public void testClosureWithDefaultParam() throws Exception {
        GroovyObject object = compile("src/test/groovy/ClosureWithDefaultParamTest.groovy");
        object.invokeMethod("methodWithDefaultParam", null);
    }

    public void testOptionalReturn() throws Exception {
        GroovyObject object = compile("src/test/groovy/OptionalReturnTest.groovy");
        object.invokeMethod("testSingleExpression", null);
        object.invokeMethod("testLastExpressionIsSimple", null);
    }

    public void testConsole() throws Exception {
        try {
            GroovyObject object = compile("src/main/groovy/ui/Console.groovy");
        } catch (MissingMethodException mme) {
            // ignore due to missing ivy dependencies in class loader
        } catch (HeadlessException he) {
            // ignore to deal with headless environments
        }
    }
}
