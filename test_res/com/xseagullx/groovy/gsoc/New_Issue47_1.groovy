class A {
    {
        new A()
        new A<B>()
        new A<B, C<D>>()
        new A<B, C<D>>(1, 2)
        new A<B, C<D>>(1, 2, [])
        new int[1]
        new int[1][2]
        new int[1][2][3]
    }
}
