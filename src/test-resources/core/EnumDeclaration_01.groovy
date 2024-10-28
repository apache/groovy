import H.I

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

enum e1{}
enum e2 { }
public enum E3 {}
public enum E4 implements A {}
@Test2
public enum E5 implements A,B {}
enum E6 implements I<T> {}

enum E10 {A}
enum E11 {A, B}
enum E12 {A, B
}
enum E13 {
    A, B
}
enum E14 {A, B,}
enum E15 {
    A,
    B}
enum E16 {
    A,
    B,}
enum E17
{A,
B,}
enum E18
{
A
,
B
,
}
enum E19
{
A
,
B
,
;
}

enum E20 {
    @Test2
    A
}

enum E21 {
    A(111,'aa'), B(222,'bb')
}
