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
package groovy.bugs.groovy4287

import static groovy.bugs.groovy4287.Outer.Inner
import static groovy.bugs.groovy4287.Outer3.* // the static star import is used to test resolving, don't remove it
import static groovy.bugs.groovy4287.Outer2.*

assert "outer.inner" == new Inner().innerName
assert "outer2.inner2" == new Inner2().innerName
assert Inner2.class.name != null
