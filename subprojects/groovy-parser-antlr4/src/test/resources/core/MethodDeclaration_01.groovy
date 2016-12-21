int plus(int a, int b) {
        return a + b;
}

int plus2(int a,
          int b)
{
        return a + b;
}

int plus3(int a,
          int b)
throws
        Exception1,
        Exception2
{
        return a + b;
}

def <T> T someMethod() {}
def <T extends List> T someMethod2() {}
def <T extends A & B> T someMethod3() {}

static m(a) {}
static m2(a, b) {}
static m3(a, b, c) {}
static Object m4(a, b, c) {}

private String relativePath() { '' }
def foo() {}


