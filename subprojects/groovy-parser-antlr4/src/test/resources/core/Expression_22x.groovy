int j = 0
++j++
assert j == 1

int i = 0
((i++)++)++
assert i == 1
++(++(++i))
assert i == 2
++(++(++i++)++)++
assert i == 3
