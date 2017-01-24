enum Outer {
    A, B
    enum Inner{X, Y}
}
assert Outer.A instanceof Outer
assert Outer.B instanceof Outer
assert Outer.Inner.X instanceof Outer.Inner
assert Outer.Inner.Y instanceof Outer.Inner