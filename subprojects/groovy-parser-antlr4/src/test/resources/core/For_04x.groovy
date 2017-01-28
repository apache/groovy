int result = 0
for ((i, j) = [0, 0]; i < 5 && j < 5; i = i + 2, j++) {
    result += i;
    result += j;
}
assert 9 == result
