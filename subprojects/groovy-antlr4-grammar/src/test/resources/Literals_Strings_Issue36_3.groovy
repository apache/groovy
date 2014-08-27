
class Strings {
    {
        'quoted string'

        "double quoted string"

        '''quoted
        multiline

        string'''

        """double quoted

        multiline
        string"""

        ''
        ""

        ' nested "double quotes" '
        " nested 'quotes' "

        ' \6 1 digit is escaped'
        ' \665 2 digits are escaped, \'5\' is a character.'
        ' \3666 3 digits are escaped'
        ' \166 '
        " \166 "
        ' \u1234 '
        " \u1234 "

        '\t'
        "\n"
        "\\ "
    }
}
