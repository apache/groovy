def a
    =
        1 + 2
assert 3 == a

a
    +=
        2
assert 5 == a

int b
    =
        1,
    c
        =
            2
assert 1 == b
assert 2 == c

def (int x, int y)
    =
        [1, 2]
assert 1 == x
assert 2 == y
(x)
    =
        [3]
assert 3 == x

@SuppressWarnings(value
        =
        "all")
def m(p1
        =
            1,
      p2
        =
            2,
      int... p3
                =
                    [3]) {
    return p1 + p2 + p3[0]
}
assert 6 == m()

def w
    =
        1
            <<
                2
assert 4 == w
assert 'a'
            instanceof
                        String
assert 1
            <
                2

assert 1
            ==
                1

assert 'a'
            ==~
                /a/
assert true
            &
                true
assert true
            ^
                false
assert true
            |
                true

assert true
            &&
                true

assert true
            ||
                true

