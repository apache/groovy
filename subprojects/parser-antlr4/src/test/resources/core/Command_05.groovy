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
((int) 1 / 2)(1, 2) {} {} (2, 3, 4) {}
(((int) 1 / 2))(1, 2) {} {} (2, 3, 4) {}
(m())()
((Integer)m())()

((int) 1 / 2) 1, 2 {} {} (2, 3, 4) {}

'm'() + /aa/() + $/bb/$() + "$m"() + /a${'x'}a/() + $/b${'x'}b/$() + 1.2('b') + 1('a') + 2() + null() + true() + false() + a() + {a,b->}(1, 2) + [1, 2]() + [a:1, b:2]() + new int[0]() + new Integer(1)()

// cast expressions
(int)(1 / 2)
(Integer)(1 / 2)
(java.lang.Integer)(1 / 2)



1 + 1.("a" + "1")()
1 + 1.(m())()
1.("a" + "1")()
1.("a" + "1")(123)
1.("a" + "1") 123
1.(m())()
1.(m())(123)
1.(m()) 123
(1+1).("a"+1)()
(1+1).("a"+1) a b c
1+1.("a"+1)()

x = a(1, 2)(3, 4) {} {} (5, 6) {} (7, 8)
x = a(1, 2) {} (3, 4) {} {} (5, 6) {} (7, 8) {} {}
x = obj.a(1, 2)(3, 4) {} {} (5, 6) {} (7, 8)
x = obj.a(1, 2) {} (3, 4) {} {} (5, 6) {} (7, 8) {} {}
x = {a, b -> }(1, 2)(3, 4) {} {} (5, 6) {} (7, 8)
x = {a, b -> }(1, 2) {} (3, 4) {} {} (5, 6) {} (7, 8) {}
x = {a, b -> }(1, 2) {} {} (3, 4) {} {} (5, 6) {} (7, 8) {} {}



m 1, 2
"m" 1, 2
"$m" 1, 2

("m") 1, 2
("$m") 1, 2

find x:
        1 confirm right


find x:
        1,
        y:
                2 confirm right

a b 1 2


(obj.m1(1, 2)) m2(3, 4)
obj.m1(1, 2) m2(3, 4)

a {} 1 {}
a {} 1.2 {}
a {} "1" {}
a {} true {}
a {} false {}
