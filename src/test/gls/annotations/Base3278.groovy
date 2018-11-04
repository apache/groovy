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
class Base3278 {
    static final int CONST = 3278
    static final int CONST1 = 1024 * 2
    static final int CONST2 = CONST1 + 22
    static final String CONST4 = 'foobar'
    static final String CONST5 = CONST4 + 'baz'
    def ints() { getClass().annotations[0].ints() }
    def strings() { getClass().annotations[0].strings() }
}
