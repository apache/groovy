package core
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
int[][][] a1 = {}
assert 0 == a1.length

int[][][] a2 = {{}}
assert 1 == a2.length
assert 0 == a2[0].length

int[][][] a3 = {{{}}}
assert 1 == a3.length
assert 1 == a3[0].length
assert 0 == a3[0][0].length

int[][][] a4 = {{}, {{1, 2}}}
assert 2 == a4.length
assert 0 == a4[0].length
assert 1 == a4[1].length
assert 2 == a4[1][0].length
assert 1 == a4[1][0][0]
assert 2 == a4[1][0][1]
