class MapFromList {
    void talk(a) {
        println("hello "+a)
    }

    void doit(args) {
        i = 1
        l = [:]
        args.each { 
	    talk(it)
	    l.put(it,i++)
	    }
        l.each {
           println(it)
        }
    }

    static void main(args) {
        a = ['tom','dick','harry']
        t = new MapFromList()
        t.doit(a)
    }
}
