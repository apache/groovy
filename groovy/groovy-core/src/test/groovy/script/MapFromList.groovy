class MapFromList {
    talk(a) {
        println("hello "+a)
    }

    doit(args) {
        i = 1
        l = [:]
        args.each { 
	    talk(it)
	    /** @todo fixme
	    l.put(it,i++)
	    */
	    l.put(it,i)
	    i = i + 1
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
