println "Hello"
println classes.length

persistentClasses = classes.findAll { it.getAnnotation("hibernate.class") != null }

println "Found ${persistentClasses.size()} instances out of ${classes.size()}"

persistentClasses.each { println it.simpleName }