// TODO: 

package groovy.bugs.priv

class StaticMethodPrivate {
    private static String sayHello(x) {
        println "[[private hello]]"
        return "private hello"
    }

    private static String say() {
        println "((private say hello))"
        return "private say hello"
    }
}
