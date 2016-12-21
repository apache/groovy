def shell = new GroovyShell()
assert shell.evaluate("x = String.&toUpperCase; x('abc')") == "ABC"
assert shell.evaluate("x = 'abc'.&toUpperCase; x()") == "ABC"
assert shell.evaluate("x = Integer.&parseInt; x('123')") == 123
assert shell.evaluate("x = 3.&parseInt; x('123')") == 123