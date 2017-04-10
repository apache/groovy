package bugs

int result = 1
for((i, j) = [0,0]; i < 3; {i++; j++}()){
    result = result * i + j
}
assert 4 == result

