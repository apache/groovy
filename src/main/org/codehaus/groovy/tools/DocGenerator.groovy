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
				jdkClass = parameters[0]
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
			writer.println('@import url("./style/maven-base.css");')
			writer.println(' </style>')
			writer.println(' <style type="text/css">')
			writer.println('@import url("./style/codehaus-style.css");')
			writer.println(' </style>')
			writer.println(" </head>")
			writer.println(" <body>")
			writer.println(" <h1>Groovy JDK methods</h1>")
			writer.println(" <p>New methods added to the JDK to make it more groovy.</p>")

			writer2 = writer
			jdkEnhancedClasses.each{e |
				writer2.print("<h2>${getObjectType(e.key.getType())}</h2>")

				listOfMethods = e.value
				for (meth in listOfMethods)
				{
				    //writer2.println("<p><b>${e.key.getName()}.${meth.getName()}(${getParametersDecl(meth)})</b></p>")
				    writer2.println("<p><b>${getReturnType(meth)} ${meth.getName()}(${getParametersDecl(meth)})</b></p>")
					writer2.println("<p>${getComment(meth)}</p>")
					writer2.println("${getExceptions(meth)}</p>")
				}
			}

			writer.println(" </body>")
			writer.println("</html>")
		}
	}

	/**
	 * Retrives a String representing the return type of the method passed as parameter.
	 */
	private String getReturn(method)
	{
		s = ""
		returnType = method.getReturns()
		if (returnType != null)
		{
			s += "<li><b>returns</b>: ${getObjectType(returnType)}"
			returnTag = method.getTagByName("return")
			if (returnTag != null)
				s += " - <i>${returnTag.getValue()}</i>"
			s += "</li>"
		}
		return s
	}

	/**
	 * Retrives a String representing the return type
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
	 * Retrives a String representing the Exceptions thrown by method passed as parameter.
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
	 */
	private String getParametersReturnAndEx(method)
	{
		//s = "<ul><li>returns ${getObjectType(method.getReturns())}</li>"
		s = "<ul>${getReturn(method)} ${getExceptions(method)}"
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
		s += "</ul>"
		return s
	}
	
	/**
	 * Retrieve a String representing the declaration of the parameters of the method passed as parameter.
	 */
	private String getParametersDecl(method)
	{
		parms = getParameters(method).map{ "${getObjectType(it.getType())} ${it.getName()}" }.join(", ")
	}

	protected getParameters(method) 
	{
		return method.getParameters().toList()[1..-1]		    
	}
	
	/**
	 * Retrieve the JavaDoc comment associated with the method passed as parameter.
	 */
	private String getComment(method)
	{
		if (method.getComment() == null)
			return ""
		return method.getComment()
	}

	/**
	 * Retrieve the object type corresponding to the object passed as parameter.
	 */
	private String getObjectType(type)
    {
	    return type.toString()
	    /*
	    s = type.getValue()
        for (i in 1..type.getDimensions())
            s += "[]"
        return s
        */
    }

    /**
     * Main entry point.
     */
    static void main(args)
    {
        defaultGroovyMethodSource =
            new File("src/main/org/codehaus/groovy/runtime/DefaultGroovyMethods.java")
        outFileName =
            new File("target/html/groovy-jdk.html")

        docGen = new DocGenerator(defaultGroovyMethodSource, outFileName)
        docGen.generate()
    }
}