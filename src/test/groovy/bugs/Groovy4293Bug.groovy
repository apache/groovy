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
package groovy.bugs

class Groovy4293Bug extends GroovyTestCase {
    void testNoBUGMessageForNullScriptPassedToCompiler() {
        try {
            assertScript """
                def params = new Expando()
                params.newValue = 23
                def trendMap = ['worsening': (-2), 'slightly worsening':(-1), 'neutral':0 ,'slightly improving': 1,'improving':2]
                
                def obj = []
                def el = [obj]
                if (!el[1]) el[1] = "1"
                
                Eval.x(el[0],"x[1]="+ el[2] ? trendMap[params.newValue.toInteger()] : params.newValue.toInteger() )
            """
            fail('Eval call should have failed as null script is passed to it')
        } catch(IllegalArgumentException ex) {
            assert ex.message.contains('Script text to compile cannot be null!')
        }
    }
}
