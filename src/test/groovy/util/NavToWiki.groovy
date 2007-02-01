package groovy.util

import groovy.util.XmlParser

if (args.size() < 1) {
    println "Usage: NavToWiki fileName"
}
else {
    file = args[0]
    println "About to parse ${file}"
    doc = new XmlParser().parse(file)

    println """
QuickLinks page
-------------------------------


"""
    links = doc.body.links.item
    println links.collect {
        return "{link:" + it['@name'] + "|" + it['@href'] + "}"
    }.join(" | ")

    println """



Navigation page
-------------------------------


"""
    menus = doc.body.menu
    menus.each {
        println "h3:${it['@name']}"

        it.item.each {
            println "- {link:" + it['@name'] + "|" + it['@href'] + "}"
        }
        println ""
    }
}

