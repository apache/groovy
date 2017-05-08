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
package core

class a<T> {
    int x = 1;
}

a b = new a()
assert b.x == 1

a<String> b2 = new a<String>()
assert b2.x == 1

a<String>[] b3 = new a<String>[0]
assert b3.length == 0

core.a b4 = new a()
assert b4.x == 1

core.a<String> b5 = new a<String>()
assert b5.x == 1

core.a<String>[] b6 = new a<String>[0]
assert b6.length == 0

