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
assert 'a' instanceof String
assert 'a' !instanceof Integer
assert 1 !instanceof String
assert null !instanceof String
assert 1 in [1, 2]
assert 3 !in [1, 2]
assert 3 !in ['1', '2']
assert '3' !in [1, 2]
assert '3' !in ['1', '2']
assert null !in ['1', '2']
assert null !in [1, 2]

boolean interesting = false
assert !interesting

boolean instanceofboolean = false
assert !instanceofboolean

assert 1 !in[2, 3]
assert 1 !in([2, 3])
assert 1 !in{return [2, 3]}()
