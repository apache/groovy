class A {
    private run() {
//        String a
//        String[] b
//        A<String[]> c

        A<String[], B<int[]>> d
    }
}


/*
   public java.lang.Object run() {
        a = {
            this.println(it)
        }
        b = 'Hello'
        this.a(b)
        A c =


a = { println(it) }
b = "Hello"
a b
helloWorld c // MethodCallExpression
HelloWorld d // DeclarationExpression

// Am I right, that style convention defines, how we should parse and resolve that ambiguity?
    }
* */
