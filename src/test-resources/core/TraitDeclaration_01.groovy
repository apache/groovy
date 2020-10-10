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

trait A {}
trait B<T> {}
trait C<T extends A> {}
trait D<T extends A & B> {}
trait E<T extends A & B & C> {}
trait F<T extends A & B & C> extends A {}
trait F2 extends A<T> {}
trait G1<T extends A & B & C> extends A implements X {}
trait G2<T extends A & B & C> extends A<T> implements X<T> {}
trait G3                      extends A<T> implements X<T> {}
trait G4                      extends A    implements X<T> {}
trait G5                      extends A    implements X    {}
trait G2<T extends A & B & C> extends A<T> implements X<T> {}
trait H<T extends A & B & C> extends A implements X, Y {}
trait I<T extends A & B & C> extends A implements X, Y, Z {}
public trait J<T extends A & B & C> extends A implements X, Y, Z {}
@Test2 public trait K<T extends A & B & C> extends A implements X, Y, Z {}
@Test2 @Test3 public trait L<T extends A & B & C> extends A implements X, Y, Z {}

@Test2
@Test3
public
trait M
<
        T extends
        A &
        B &
        C
>
extends
A
implements
X,
Y,
Z
        {

        }

trait t {}