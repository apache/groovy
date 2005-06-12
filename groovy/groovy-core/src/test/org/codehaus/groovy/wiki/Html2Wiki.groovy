import groovy.util.XmlParser

import java.io.File

import org.cyberneko.html.parsers.SAXParser

class Html2Wiki {
    
    protected out
    
    static void main(args) {
        def gen = new Html2Wiki()
        for (arg in args) {
            gen.createWiki(arg)
        }
    }
    
    void createWiki(fileName) {
        def htmlParser = new SAXParser()
        htmlParser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower")
        htmlParser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower")
        def parser = new XmlParser(htmlParser)
        println "Parsing ${fileName}"
        def node = parser.parse(fileName)
        
        def outputName = getOutputName(fileName)
        new File(outputName).eachPrintWriter { out = it; makeWikiPage(node) }
    }

    def getOutputName(fileName) {
	    def lastIdx = fileName.lastIndexOf(".")
	    if (lastIdx > 0) {
	        def fileName = fileName.substring(0, lastIdx)
	    }
	    return fileName + ".wiki"
	}
    
    void makeWikiPage(node) {
        def body = node.html.body
        if (body == null) {
            println "Warning empty document, no <html><body> section"
        }
        else {
            applyTemplatesForChildren(node)
        }
    }
    
    void applyTemplates(node) {
        switch (node.name()) {
            case "h1":
                out.println "1 " + node.text() 
                out.println()
                break
            case "h2":
                out.println "1.1 " + node.text()
                out.println()
                break
            case "h3":
                out.println "1.1.1 " + node.text()
                out.println()
                break
            case "h4":
                out.println "1.1.1.1 " + node.text()
                out.println()
                break
            case "a":
                out.print "{link:${node.text()}${node.attribute('href')}} "
                break
            case "b":
                out.print "__${node.text()}__ "
                break
            case "i":
                out.print "~~${node.text()}~~ "
                break
            case "source":
                out.println """{code:groovysh}
${node.text()}
{code}
"""
               break
            case "li":
                out.print "* "
                applyTemplatesForChildren(node)
                out.println()
                break
            case "p":
            default:
                applyTemplatesForChildren(node)
                out.println()
                out.println()
        }
    }
    
    void applyTemplatesForChildren(node) {
        for (c in node.children()) {
            if (c instanceof String) {
                out.print c + " "
            }
            else {
                applyTemplates(c)
            }
        }
    }
}
