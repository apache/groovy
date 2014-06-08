
class Numbers {
    {
        0x1234567890abcdef
        0X1234567890ABCDEF

        0x1234567890abcdefi
        0x1234567890abcdefl
        0x1234567890abcdefg

        0x1234567890abcdefI
        0x1234567890abcdefL
        0x1234567890abcdefG

        0xabcdef
        0xABCDEF

        -0xabcdef
        -0xABCDEF

        +0xabcdef
        +0xABCDEF
    }

    {
        01234567
        -01234567
        +01234567

        01234567i
        01234567l
        01234567g

        01234567I
        01234567L
        01234567G
    }

    {
        1__2
        2_12_3
        +2__12__3
        +2__12__3.1__2
        -2__12__3.1__2

        12e10
        12e-10

        -12e10
        -12e-10

        -12.12e10
        -12.12e-10
        -12.12e10
        -12.12e-10

        12.12e-10f
        12.12e-10d
        12.12e-10g

        12e-10F
        120e-10D
        12e-10G

        12.1__2e-10f
        12.1_2e-10d
        1__12_2.12e-10g
        'Note NO _ in power part.'

        +0xab__cdef
        +0xAB__CD_EF

        012__34567L
        'octal'

        'Note NO _ in power part.'
        if('Comments lead tests to fail because of broken old parser location handling') {
            //TODO add negative examples, and their hanling.
        }
    }
}
