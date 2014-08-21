new HashMap(a: a)
new HashMap(a: a, b:b)
new HashMap("$a": a, b:b)
new HashMap("as ${ 2 + 1 }": a, b:b)
new HashMap((a): a, b:b, c, {-> })
new HashMap(b, a: a, b:b, c, {-> })

a(a: a)
a(a: a, b:b)
a("$a": a, b:b)
a("as ${ 2 + 1 }": a, b:b)
a((a): a, {}, b:b, c, {-> })
a(b, a: a, b:b, c, {-> })

a a: a
a a: a, b:b
a "$a": a, b:b
a "as ${ 2 + 1 }": a, b:b
a a: a, b:b, c, {-> }
a b, a: a, b:b, c, {-> }

