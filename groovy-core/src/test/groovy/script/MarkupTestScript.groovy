import groovy.xml.MarkupBuilder;

class Bean {
	String b
};

t = new Bean()
t.b = "hello"
println t.b
println "test: ${t.b}"

xml = new MarkupBuilder()
root = xml.foo {
	bar {
		// works
		baz("test")
		// fails
		baz(t.b)
		// fails
		baz("${t.b}")
	}
} 
