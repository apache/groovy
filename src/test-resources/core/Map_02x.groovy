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
def map = [
        private: 1, public: 2, protected: 3, static: 4,
        transient: 5, final: 6, abstract: 7, native: 8,
        threadsafe: 9, synchronized: 10, volatile: 11, strictfp: 12
]
assert 1..12 == [
        map.private, map.public, map.protected, map.static,
        map.transient, map.final, map.abstract, map.native,
        map.threadsafe, map.synchronized, map.volatile, map.strictfp
]