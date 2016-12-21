interface A {
    default String hello() {
        return 'hello'
    }
    default String a() {
        return 'a'
    }
    String b();
}

interface B extends A {
    public String world();
}

class C implements B {
    public static String haha() { return 'haha' }

    public String world() {
        return 'world'
    }

    public String name() {
        return 'c'
    }

    public String a() {
        return 'a1';
    }

    public String b() {
        return 'b'
    }
}

def c = new C()
assert 'hello, world, c, a1, b, haha' == "${c.hello()}, ${c.world()}, ${c.name()}, ${c.a()}, ${c.b()}, ${C.haha()}"