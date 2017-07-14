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

class A {}
class B<T> {}
class C<T extends A> {}
class D<T extends A & B> {}
class E<T extends A & B & C> {}
class F<T extends A & B & C> extends A {}
class F2 extends A<T> {}
class G1<T extends A & B & C> extends A implements X {}
class G2<T extends A & B & C> extends A<T> implements X<T> {}
class G3                      extends A<T> implements X<T> {}
class G4                      extends A    implements X<T> {}
class G5                      extends A    implements X    {}
class H<T extends A & B & C> extends A implements X, Y {}
class I<T extends A & B & C> extends A implements X, Y, Z {}
public class J<T extends A & B & C> extends A implements X, Y, Z {}
@Test2 public class K<T extends A & B & C> extends A implements X, Y, Z {}
@Test2 @Test3 public class L<T extends A & B & C> extends A implements X, Y, Z {}

@Test2
@Test3
@Test4(value={
        def a = someMethod()
        assert a.result() == 'abc'
})
@Test5(b=2, a=1)
@Test6(a=2, b=1)
public
class M
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

class a {}

class OutputTransforms {
        final  static localTransforms = loadOutputTransforms()
}

class OutputTransforms2 {
        @Lazy  static localTransforms = loadOutputTransforms()
}

class XX {
        def x
        private x = 1
}
