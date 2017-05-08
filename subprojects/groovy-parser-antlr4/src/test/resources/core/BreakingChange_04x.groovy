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

class B<T> {}
B b
b = new B()
assert b instanceof B

B<String> b2
b2 = new B<String>()
assert b2 instanceof B

B<String>[] b3
b3 = new B<String>[0]
assert b3 instanceof B[]

core.B b4
b4 = new B()
assert b4 instanceof B

core.B<String> b5
b5 = new B<String>()
assert b5 instanceof B

core.B<String>[] b6
b6 = new B<String>[0]
assert b6 instanceof B[]