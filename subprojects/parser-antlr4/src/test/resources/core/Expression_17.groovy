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
a.m(x: 1, y: 2) {
    println('named arguments');
}

a.m(x: 1, y: 2, z: 3) {
    println('named arguments');
} {
    println('named arguments');
}


//a.m(x: 1, y: 2, z: 3)
//
//{
//    println('named arguments');
//}
//
//{
//    println('named arguments');
//}



a.m(1, 2) {
    println('normal arguments');
}

a.m(1, 2, 3) {
    println('normal arguments');
} {
    println('normal arguments');
}

//a.m(1, 2, 3)
//
//{
//    println('normal arguments');
//}
//
//
//{
//    println('normal arguments');
//}




m {
    println('closure arguments');
}

m {
    println('closure arguments');
} {
    println('closure arguments');
}

m {
    println('closure arguments');
} {
    println('closure arguments');
} {
    println('closure arguments');
}


//m
//
//{
//    println('closure arguments');
//}
//
//{
//    println('closure arguments');
//}
//
//{
//    println('closure arguments');
//}

'm' {
    println('closure arguments');
}


1 {

}
1.1 {

}

-1 {

}

-1.1 {

}

1()
1.1()
1(1, 2, 3)
1.1(1, 2, 3)
-1()
-1.1()
-1(1, 2, 3)
-1.1(1, 2, 3)

1(1, 2) {

}

1.1(1, 2) {

}

-1(1, 2) {

}

-1.1(1, 2) {

}

hello(x: 1, y: 2, z: 3)
hello('a', 'b')
hello(x: 1, 'a', y: 2, 'b', z: 3)
hello('c', x: 1, 'a', y: 2, 'b', z: 3)


A[x: 1, y: 2]
A[*: someMap]
A[*: someMap, z: 3]
A[w: 0, *: someMap]
A[*: [x: 1, y: 2]]
A[*: [x: 1, y: 2], z: 3]
A[w: 0, *: [x: 1, y: 2]]

SomeMethod(a, b)

