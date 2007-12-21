package groovy.bugs

class Groovy2348Bug extends GroovyTestCase{
  void test () {
     assertEquals( ['1.0', '2.0'], Foo.test(['1.0-vers', '2.0-subvers']))
  }
}

class Foo {

    private static test(tokens) {
        tokens.collect {
            trimTag(it) 
        }
    }

    private static trimTag(pluginVersion) {
        int i = pluginVersion.indexOf('-')
        if(i > 0) {
            pluginVersion = pluginVersion[0..i-1]
        }
        pluginVersion
    }
}
