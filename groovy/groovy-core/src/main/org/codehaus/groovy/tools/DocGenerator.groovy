package org.codehaus.groovy.tools

import java.io.File

import com.thoughtworks.qdox.JavaDocBuilder
import com.thoughtworks.qdox.model.JavaSource
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMethod
import com.thoughtworks.qdox.model.JavaParameter
import com.thoughtworks.qdox.model.Type

/**
 * Generate documentation about the methods provided by the Groovy Development Kit
 * enhancing the standard JDK classes.
 *
 * @author Guillaume Laforge
 */
class DocGenerator
{
	File           file
	File           outputFile
	JavaDocBuilder builder

	DocGenerator(File fileToParse, File outputFile)
	{
		this.file       = fileToParse
		this.outputFile = outputFile
	}

	/**
	 * Parse the DefaultGroovyMethods class to build a graph representing the structure of the class,
	 * with its methods, javadoc comments and tags.
	 */
	private void parse()
	{
		reader  = file.newReader()
		builder = new JavaDocBuilder()

		builder.addSource(reader)
	}

	/**
	 * Builds an HTML page from the structure of DefaultGroovyMethods.
	 */
	void generate()
	{
		parse()

		sources = builder.getSources()
		firstSource = sources[0]
		classes = firstSource.getClasses()
		groovyMeth = classes[0]

		// categorize all groovy methods per core JDK class to which it applies
		jdkEnhancedClasses = [:]
		methods = groovyMeth.getMethods()
		for (method in methods)
		{
			if (method.isPublic() && method.isStatic())
			{
				parameters = method.getParameters()
				jdkClass = parameters[0].type.toString()
				if (jdkEnhancedClasses.containsKey(jdkClass))
					jdkEnhancedClasses[jdkClass].add(method)
				else
					jdkEnhancedClasses[jdkClass] = [method]
			}
		}

		writer = outputFile.newPrintWriter()
		writer.withWriter{writer |
			writer.println("<html>")
			writer.println(" <head>")
			writer.println("  <title>GDK : Groovy methods</title>")
			writer.println(' <style type="text/css">')
			writer.println('   @import url("./style/maven-base.css");')
			writer.println(' </style>')
			writer.println(' <style type="text/css">')
			writer.println('   @import url("http://codehaus.org/codehaus-style.css");')
			writer.println(' </style>')
			//writer.println(" <style>")
			//writer.println("   body, h1, h2, h3, p, div, span { font-family: sans-serif; font-size: 10pt }")
			//writer.println("   h1 { padding: 6px; font-size: 24pt; color: #008800; font-weight: bold; font-size: 24pt}")
			//writer.println("   h2 { padding: 4px; font-size: 14pt; color: #008800; padding: 4px; border: 1px solid #aaaaaa; font-size: larger; background-color: #eeeeee;}")
			//writer.println("   h3 { margin: 10px }")
			//writer.println("   p { margin: 20px }")
			//writer.println(" </style>")
			writer.println(" </head>")
			writer.println(" <body>")
			writer.println(" <h1>Groovy JDK methods</h1>")
			writer.println(" <p>New methods added to the JDK to make it more groovy.</p>")

			writer2 = writer
            sb = new StringBuffer()
            counter = 0

			// lets iterate in sorted class name order
			sortedClasses = [] + jdkEnhancedClasses.keySet()
			for (className in sortedClasses.sort()) {

				sb.append("<h2>${getObjectType(className)}</h2>")
				writer2.println("<b>${className}</b>")

				listOfMethods = jdkEnhancedClasses[className]
				listOfMethods.sort { it.name }
				
				writer2.println("<table width='100%'>")
				for (meth in listOfMethods) {
                    counter++
                    anchor = "meth${counter}"
                    writer2.println("<tr><td width='30%'>${getReturnType(meth)}</td> <td width='70%'><a href='#${anchor}'>${meth.getName()}</a>(${getParametersDecl(meth)})</td></tr>")

                    sb.append("  <a name='${anchor}'></a>")
				    sb.append("  <p><b>${getReturnType(meth)} ${meth.getName()}(${getParametersDecl(meth)})</b></p>")
					sb.append("  <ul>${getComment(meth)}")
					sb.append("  ${getParametersReturnAndEx(meth)}</ul></p>")
				}
				writer2.println("</table>")
			}
            writer.println(sb.toString())

			writer.println(" </body>")
			writer.println("</html>")
		}
	}

	/**
	 * Retrives a String representing the return type of the method passed as parameter.
	 *
	 * @param method a method
	 * @return the return type of the method
	 */
	private String getReturn(method)
	{
		s = ""
		returnType = method.getReturns()
		if (returnType != null)
		{
		    type = getObjectType(returnType)
		    if (type != "void")
		    {
                s += "<li><b>returns</b>: ${type}"
                returnTag = method.getTagByName("return")
                if (returnTag != null)
                    s += " - <i>${returnTag.getValue()}</i>"
                s += "</li>"
            }
		}
		return s
	}

	/**
	 * Retrieves a String representing the return type
	 */
	private String getReturnType(method)
	{
	    s = ""
	    returnType = method.getReturns()
	    if (returnType != null) {
	        s += "${getObjectType(returnType)}"
	    }
	    return s
	}

	/**
	 * Retrieves a String representing the Exceptions thrown by method passed as parameter.
	 *
	 * @param method a method
	 * @return the exceptions thrown by the method
	 */
	private String getExceptions(method)
	{
		s = ""
		exceptions = method.getExceptions()
		for (ex in exceptions)
		{
			if (ex != null)
			{
				s += "<li><b>throws</b>: ${getObjectType(ex)}"
				exMsg = method.getTagByName("throws")
				if (exMsg != null)
					s += " - <i>${exMsg.getValue()}</i>"
				s += "</li>"
			}
		}
		return s
	}

	/**
	 * Retrieve a String representing the list of the parameters of the method passed as parameter.
	 *
	 * @param method a method
	 * @retrun a HTML list of parameters of the method
	 */
	private String getParametersReturnAndEx(method)
	{
		s = "<ul>"
		params = method.getTags()
		counter = 0
		if (params != null)
		{
			params.each{param |
				if (counter != 0 && param.getName() != "throws" && param.getName() != "return")
					s += "<li>${param.getName()} ${param.getValue()}</li>"
				counter++
			}
		}
		s += "${getReturn(method)} ${getExceptions(method)}</ul>"
		return s
	}

	/**
	 * Retrieve a String representing the declaration of the parameters of the method passed as parameter.
	 *
	 * @param method a method
	 * @return the declaration of the method (long version)
	 */
	private String getParametersDecl(method)
	{
		parms = getParameters(method).map{ "${getObjectType(it.getType())} ${it.getName()}" }.join(", ")
	}

	/**
	 * Retrieve the parameters of the method.
	 *
	 * @param method a method
	 * @return a list of parameters without the first one
	 */
	protected getParameters(method)
	{
		return method.getParameters().toList()[1..-1]
	}

	/**
	 * Retrieve the JavaDoc comment associated with the method passed as parameter.
	 *
	 * @param method a method
	 * @return the JavaDoc comment associated with this method
	 */
	private String getComment(method)
	{
		if (method.getComment() == null)
			return ""
		return method.getComment()
	}

	/**
	 * Retrieve the object type corresponding to the object passed as parameter.
	 *
	 * @param type a Type
	 * @return the String representing the type of that parameter
	 */
	private String getObjectType(type)
    {
	    return type.toString()
    }

    /**
     * Main entry point.
     */
    static void main(args)
    {
        defaultGroovyMethodSource =
            //new File("D:/cvs-groovy/groovy/groovy-core/src/main/org/codehaus/groovy/runtime/DefaultGroovyMethods.java")
            new File("src/main/org/codehaus/groovy/runtime/DefaultGroovyMethods.java")
        outFileName =
            //new File("D:/cvs-groovy/groovy/groovy-core/target/html/groovy-jdk.html")
            new File("target/html/groovy-jdk.html")

        docGen = new DocGenerator(defaultGroovyMethodSource, outFileName)
        docGen.generate()
    }
}