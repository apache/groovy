package org.codehaus.groovy.tools

import static org.codehaus.groovy.tools.StringHelper.*

class StringHelperTest extends GroovyTestCase {

  void testTokenize() {
    assert tokenizeUnquoted("a b") == ["a","b"]
    assert tokenizeUnquoted("a 'b a'") == ["a","'b a'"]
    assert tokenizeUnquoted(" a 'b a'") == ["a","'b a'"]
    assert tokenizeUnquoted(" a 'b a'" ) == ["a","'b a'"]
    assert tokenizeUnquoted('a "b a"') == ["a",'"b a"']
    assert tokenizeUnquoted("a 'b \"a c\"'") == ["a","'b \"a c\"'"]
    assert tokenizeUnquoted("a \"b 'a c'\"") == ["a","\"b 'a c'\""]
    assert tokenizeUnquoted("'a ") == ["'a "]
    assert tokenizeUnquoted("\"a 'b'") == ["\"a 'b'"]
  }
}