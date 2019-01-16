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
package groovy.bugs.groovy7812

assert new Outer()
assert new Outer.Inner()
assert new groovy.bugs.groovy7812.Outer.Inner()
assert new Outer.Inner.Innest()
assert new groovy.bugs.groovy7812.Outer.Inner.Innest()
assert "2" == new Outer.Inner2().innerName
assert "3" == new Outer.Inner3().innerName
assert "1.Innest" == new Outer.Inner.Innest().name
assert "3.Innest" == new Outer.Inner3.Innest().name
