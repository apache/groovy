
class GroovyTest3Class
{
	void doSomething()
	{
		org.codehaus.groovy.ant.GroovyTest.FLAG = "from groovytest3.GroovyTest3Class.doSomething()"
	}

	void doSomethingWithArgs(args)
	{
		org.codehaus.groovy.ant.GroovyTest.FLAG = "from groovytest3.GroovyTest3Class.doSomethingWithArgs() " + args.join(" ")
	}
}
