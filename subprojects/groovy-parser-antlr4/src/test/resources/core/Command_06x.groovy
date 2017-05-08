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
assert 11 == (1.plus 2 plus 3) + (2.plus 3) * (1.multiply 1) - ((1 - 1).intdiv 1 multiply 1) / (2.power 10)
assert (Long)(1.plus 2 plus 3) instanceof Long
assert [1, 2, 3] == [(1.plus 0), (1.plus 1), (1.plus 1 plus 1)]

def m(a, b) {
    return a + b
}
assert m((1.plus 2 plus 3), (1.multiply 1)) == 7
