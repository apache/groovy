package org.codehaus.groovy.runtime

class GroovyCategoryTest extends GroovyTestCase {
    void testUseWithVarArg() {
        // Try out the single class case
        use(Category1) {
            assert "HeLlO".upper() == "HELLO"
        }

        // Try out the list case
        use([Category1, Category2]) {
            assert "HeLlO".upper() == "HELLO"
            assert "HeLlO".lower() == "hello"
        }

        // Try out the vararg version
        use(Category1, Category2) {
            assert "HeLlO".upper() == "HELLO"
            assert "HeLlO".lower() == "hello"
        }

        // This should fail
        try {
            use(Category1)
            fail()
        } catch (IllegalArgumentException e) {
        }

        // And so should this
        try {
            use(Category1, Category2)
            fail()
        } catch (IllegalArgumentException e) {
        }
    }
}

class Category1 {
    static String upper(String message) {return message.toUpperCase()}
}

class Category2 {
    static String lower(String message) {return message.toLowerCase()}
}