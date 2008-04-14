package groovy.bugs

t = new Thread() { println "Groovy" }
t.start()
t.join()
