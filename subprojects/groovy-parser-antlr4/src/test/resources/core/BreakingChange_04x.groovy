package core

class B<T> {}
B b
b = new B()
assert b instanceof B

B<String> b2
b2 = new B<String>()
assert b2 instanceof B

B<String>[] b3
b3 = new B<String>[0]
assert b3 instanceof B[]

core.B b4
b4 = new B()
assert b4 instanceof B

core.B<String> b5
b5 = new B<String>()
assert b5 instanceof B

core.B<String>[] b6
b6 = new B<String>[0]
assert b6 instanceof B[]