persistentClasses = classes.findAll { it.getAnnotation("hibernate.class") != null }

println "Found ${persistentClasses.size()} instances out of ${classes.size()}"

persistentClasses.each { c |
    println c.simpleName

    for (p in c.properties) {
        println "  property: ${p.simpleName}"
    }
}