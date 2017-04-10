assert 11 == (1.plus 2 plus 3) + (2.plus 3) * (1.multiply 1) - ((1 - 1).intdiv 1 multiply 1) / (2.power 10)
assert (Long)(1.plus 2 plus 3) instanceof Long
assert [1, 2, 3] == [(1.plus 0), (1.plus 1), (1.plus 1 plus 1)]

def m(a, b) {
    return a + b
}
assert m((1.plus 2 plus 3), (1.multiply 1)) == 7
