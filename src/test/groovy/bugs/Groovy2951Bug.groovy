class Groovy2951Bug extends GroovyTestCase{
    def void testInstanceLevelMissingMethodWithRegularClosure1() {
        Groovy2951BugClass1.metaClass.methodMissing = {
            method, args ->
            return method
        }
        def result = new Groovy2951BugClass1().test1("arg1", "arg2")
        assert result == "test1"
    }

    def void testInstanceLevelMissingMethodWithRegularClosure2() {
        Groovy2951BugClass2.metaClass.methodMissing << { method, args ->
            return method
        }
        def result = new Groovy2951BugClass2().test2("arg1", "arg2")
        assert result == "test2"
    }

    def void testInstanceLevelMissingMethodWithMethodClosure() {
        Groovy2951BugClass3.metaClass.methodMissing = Groovy2951BugClass3.&mm

        def result = new Groovy2951BugClass3().test3("arg3", "arg4")
        assert result == "test3"
    }
}

class Groovy2951BugClass1 {}

class Groovy2951BugClass2 {}

class Groovy2951BugClass3 {
    static def mm(method, args) {
        return method
    }	
}
