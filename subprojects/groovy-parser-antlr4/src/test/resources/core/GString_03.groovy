def a = """hello${a}
world
"""

a = """
hello
${a}
world
"""

a =~ $/(${123}hello) \/
            ${a}world\1
 \u9fa5 \r
/$
a =~ $/\
    x  $
    $$
    $/
/$
a =~ $/\
    $x  $
    $$
    $/
/$
a = $/
            Hello name,
            today we're date.

            $ dollar sign
            $$ escaped dollar sign
            \ backslash
            / forward slash
            $/ escaped forward slash
            $/$ escaped dollar slashy string delimiter
        /$

a = $/
            Hello $name,
            today we're ${date}.

            $ dollar sign
            $$ escaped dollar sign
            \ backslash
            / forward slash
            $/ escaped forward slash
            $/$ escaped dollar slashy string delimiter
        /$
a = $/$$VAR/$
a = $/$$ $VAR/$
