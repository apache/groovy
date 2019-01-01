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
package groovy.mock.interceptor

/**
 * Helper class for testing.
 */
class Caller {
    int collaborateOne() {
        return new Collaborator().one()
    }

    int collaborateOne(int arg) {
        return new Collaborator().one(arg)
    }

    int collaborateOne(int one, two) {
        return new Collaborator().one(one, two)
    }

    int collaborateTwo() {
        return new Collaborator().two()
    }

    String collaborateJava() {
        return 'whatever'.toString()
    }

    String callFoo1() {
        return new Collaborator().foo
    }

    String callFoo2() {
        return new Collaborator().foo
    }

    void setBar1() {
        new Collaborator().bar = "bar1"
        return
    }

    void setBar2() {
        new Collaborator().setBar("bar2")
        return
    }

}