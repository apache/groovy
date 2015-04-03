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
package groovy

class NamedParameterTest extends GroovyTestCase {

    void testPassingNamedParametersToMethod() {
        someMethod(name:"gromit", eating:"nice cheese", times:2)
    }
    
    protected void someMethod(args) {
        assert args.name == "gromit"
        assert args.eating == "nice cheese"
        assert args.times == 2
        assert args.size() == 3
    }

    void testNamedParameterSpreadOnSeveralLines() {
        someMethod( name:
                    "gromit",
            eating:
                    "nice cheese",
            times:
                    2)
    }

    void testNamedParameterSpreadOnSeveralLinesWithCommandExpressions() {
        someMethod name:
                    "gromit",
            eating:
                    "nice cheese",
            times:
                    2
    }
}
