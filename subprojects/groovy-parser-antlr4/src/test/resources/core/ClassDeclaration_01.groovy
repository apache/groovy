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