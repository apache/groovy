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
package core

interface A1 {}
interface A2<T> {}
interface A3<T extends A> {}
interface A4<T extends A & B> {}
interface A5<T extends A & B & C> {}
interface A6<T extends A & B & C> extends A {}
interface A62 extends A<T> {}
interface A7<T extends A & B & C> extends A, B {}
interface A8<T extends A & B & C> extends A, B, C {}
@Test2 interface A9<T extends A & B & C> extends A, B, C {}
@Test2 @Test3 public interface A10<T extends A & B & C> extends A, B, C {}

@Test2
@Test3
@Test4
public
interface A11
<
        T extends
A &
B &
C
>
extends
A,
B,
C
        {

        }

interface Iterator extends java.util.Iterator {}

interface i {}