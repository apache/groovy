package groovy.script

class MapFromList {
    void talk(a) {
        println("hello "+a)
    }

    void doit(args) {
        def i = 1
        def l = [:]
        args.each { 
	    talk(it)
	    l.put(it,i++)
	    }
        l.each {
           println(it)
        }
    }

    static void main(args) {
        def a = ['tom','dick','harry']
        def t = new MapFromList()
        t.doit(a)
    }
}
