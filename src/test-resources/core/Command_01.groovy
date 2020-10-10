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
a 1
a 1, 2
a x: 1, y: 2
a.b 1, 2
z.a b(1), c(2)
a.b x: 1, y: 2
z.a x: b(1), y: c(2)
a b(1), c(2)
a x: b(1), y: c(2)
z.a x: b(1, 3), y: c(2, 4)
a b(1, 3), c(2, 4)
obj.<Integer, Double>a b(1, 3), c(2, 4), d(3, 5)
println a
// println a = 1  // breaking change
println a == 1

result.addAll allElements()
task someTask() {}