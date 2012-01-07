package org.codehaus.groovy.ant
// should produce an error message
def f = {
    t.notExisting()
}

f()
