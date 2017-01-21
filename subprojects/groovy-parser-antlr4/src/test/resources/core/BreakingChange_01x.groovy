package core

class a<T> {
    int x = 1;
}

a b = new a()
assert b.x == 1

a<String> b2 = new a<String>()
assert b2.x == 1

a<String>[] b3 = new a<String>[0]
assert b3.length == 0

core.a b4 = new a()
assert b4.x == 1

core.a<String> b5 = new a<String>()
assert b5.x == 1

core.a<String>[] b6 = new a<String>[0]
assert b6.length == 0

