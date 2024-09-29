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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy7324Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testInferenceOfListDotOperator() {
        assertScript '''
            class Account {
                String id
            }
            class Accounts {
                List<Account> accountList
            }
            class User {
                List<Accounts> accountsList
            }

            def accounts = (1..10).collect { new Account(id: "Id $it") }
            def user1 = new User(accountsList: [new Accounts(accountList: accounts[0..2]), new Accounts(accountList: accounts[3..4])])
            def user2 = new User(accountsList: [new Accounts(accountList: accounts[5..7]), new Accounts(accountList: accounts[8..9])])
            def users = [user1, user2]
            def ids = (List<String>) users.accountsList.accountList.id.flatten()
            assert ids.size() == 10
        '''
    }

    void testInferenceOfSpreadDotOperator() {
        assertScript '''
            class Account {
                String id
            }
            class Accounts {
                List<Account> accountList
            }
            class User {
                List<Accounts> accountsList
            }

            def accounts = (1..5).collect { new Account(id: "Id $it") }
            def user = new User(accountsList: [new Accounts(accountList: accounts[0..2]), new Accounts(accountList: accounts[3..4])])
            // "user.accountsList*.accountList" produces List<List<Account>> so "that*.id" includes spread-dot and list-dot operation
            def ids = (List<String>) user.accountsList*.accountList*.id.flatten()
            assert ids.size() == 5
        '''
    }
}
