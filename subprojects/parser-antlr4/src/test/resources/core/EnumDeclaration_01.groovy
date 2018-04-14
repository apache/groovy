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
enum AAAA {}
public enum AAAA2 {}
public enum AAAA3 implements A {}
@Test2
public enum AAAA4 implements A,B {}
enum BBBB {A}
enum CCCC {A, B}
enum CCCC2 {A, B
}
enum CCCC3 {
    A, B
}
enum DDDD {A, B,}
enum EEEE {
    A,
    B}
enum FFFF {
    A,
    B,}
enum GGGG
{A,
B,}

enum E1 {
    A(111,'aa'), B(222,'bb')
}

@Test2
enum E2 {
    A2,
    B2,C2,
    D2
    private void a() {}
    def c;
}

enum E3 {
    A(1), B(2)

    public static final String SOME_CONSTANT = '123';
    private String name;
    private int age = 2;
    String title = "title"
    public E9(int x) {}
}



enum e {}

enum Enum1 {
    A, B;
}
enum Enum2 {
    A, B;}
enum Enum3 {A, B;}
