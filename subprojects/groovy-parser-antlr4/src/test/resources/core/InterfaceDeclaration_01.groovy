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