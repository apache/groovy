'123'
'abc'
'a1b2c3'
'a\tb\tc'
'a\nb\r\nc'
'$a$b$c'
'$1$2$3'
'$1$2\$3'
'\$1\$2\$3\
  hello world\
'
"\$1\$2\$3\
  hello world\
"
' nested "double quotes" '
" nested 'quotes' "
' \6 1 digit is escaped'
' \665 2 digits are escaped, \'5\' is a character.'
' \3666 3 digits are escaped'
' \166 '
" \166 "
' \u1234 '

'''abc'''
'''123'''
'''
            ''hello world''
            'hello'
            ''world'
            'hi''
            \
            \t\r\n
            $\$
            \u1234
            \123
'''

"""
            ''hello world''
            'hello'
            ''world'
            'hi''
            \
            \t\r\n
            \$
            \u1234
            \123
"""
