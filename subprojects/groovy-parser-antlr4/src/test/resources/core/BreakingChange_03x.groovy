package core

class a {}
a[] b
b = new a[0]
assert 0 == b.length

a[] b2 = new a[0]
assert 0 == b2.length

core.a[] b3
b3 = new a[0]
assert 0 == b3.length

core.a[] b4 = new a[0]
assert 0 == b4.length