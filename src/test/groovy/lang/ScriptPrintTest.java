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
package groovy.lang;

import org.codehaus.groovy.classgen.TestSupport;

public class ScriptPrintTest extends TestSupport {

    public void testScriptWithCustomPrintln() throws Exception {
        assertScript(
                "out = new groovy.lang.MockWriter(); println(); assert out.output == 'println()', 'value of output is: ' + out.output\n"
                        + "print('hey'); assert out.output == 'print(hey)' , 'value is: ' + out.output\n"
                        + "println('hey'); assert out.output == 'println(hey)', 'value is: ' + out.output\n");
    }

}
