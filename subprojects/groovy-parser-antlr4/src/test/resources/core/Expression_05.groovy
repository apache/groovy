true ?: 'a'

true
?: 'a'

true
?:
'a'

true ? 'a' : 'b'

true ?
        'a'
        :
        'b'

true ?
        'a'
        :
        true ?: 'b'

true ?
        'a'
        :
        true ? 'b' : 'c'

true ?: true ?: 'a'

1 == 2 ?: 3
1 == 2 ? 3 : 4

1 == 2 || 1 != 3 && !(1 == 6)
    ? 2 > 3 && 3 >= 1
        ? 4 < 5 && 2 <= 9 ?: 6 ^ 8 | 9 & 10
        : 8 * 2 / (3 % 4 + 6) - 2
    : 9


bar = 0 ? "moo"         \
              : "cow"
