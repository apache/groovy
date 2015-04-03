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
package org.codehaus.groovy.ast.source


class Groovy3051Test extends SourceBaseTestCase {
    def script = '''
        for(Object item in [1:1,2:2]) {}
        for(Object item in [1,2]) {}
    '''        
    

    void testLine2() {
        def statements = statements()
        
        // for(Object item in [1:1,2:2]) {}
        def forStatement = statements[0] 
        assert sourceInfo(forStatement) == [2,9, 2,41]
        
        // Object item
        def variable = forStatement.variable
        assert sourceInfo(variable) == [2,20, 2,24]
        assert sourceInfo(variable.type) == [2,13, 2,19]
        
        // [1:1,2:2]
        def map = forStatement.collectionExpression
        assert sourceInfo(map) == [2,28, 2,37]
        
        def entries = map.mapEntryExpressions
        
        // 1:1
        assert sourceInfo(entries[0].keyExpression) == [2,29, 2,30]
        assert sourceInfo(entries[0].valueExpression) == [2,31, 2,32]
        
        // 2:2
        assert sourceInfo(entries[1].keyExpression) == [2,33, 2,34]
        assert sourceInfo(entries[1].valueExpression) == [2,35, 2,36]
        
        // {}
        assert sourceInfo(forStatement.loopBlock) == [2,39, 2,41]
   }

   void testLine3() {
        def statements = statements()
        
        // for(Object item in [1,2]) {}
        def forStatement = statements[1] 
        assert sourceInfo(forStatement) == [3,9, 3,37]
        
        // Object item
        def variable = forStatement.variable
        assert sourceInfo(variable) == [3,20, 3,24]
        assert sourceInfo(variable.type) == [3,13, 3,19]
        
        // [1,2]
        def list = forStatement.collectionExpression
        assert sourceInfo(list) == [3,28, 3,33]
        
        // 1
        assert sourceInfo(list.expressions[0]) == [3,29, 3,30]
        
        // 2
        assert sourceInfo(list.expressions[1]) == [3,31, 3,32]
        
        // {}
        assert sourceInfo(forStatement.loopBlock) == [3,35, 3,37]
   }
}