package groovy.text

class SimpleTemplateTest extends GroovyTestCase {

    void testSimpleCallFromGroovyEmpty() {
        assertEquals('', simpleCall(''))
    }

    void testSimpleCallFromGroovyStatic() {
        def input = 'some static text'
        assertEquals(input, simpleCall(input))
    }

    void testExpressionAssign() {
        assertEquals('1',   simpleCall('<%=1%>'))
        assertEquals(' 1',  simpleCall(' <%=1%>'))
        assertEquals(' 1 ', simpleCall(' <%=1%> '))
        assertEquals(' 1 ', simpleCall(' <%= 1%> '))
        assertEquals(' 1 ', simpleCall(' <%= 1 %> '))
        assertEquals(' 1 ', simpleCall(" <%=\n 1 \n%> "))
        assertEquals(' 1', bindingCall([a:1],' <%=a%>'))
    }

    void testExpressionEval() {
        assertEquals('1', simpleCall('<%print(1)%>'))
        assertEquals('01', simpleCall('<%for(i in 0..1){print(i)}%>'))
    }

    void testWithMarkupBuilder(){
    def text = '''<%
        builder = new groovy.xml.MarkupBuilder(out)
        [1,2,3].each{ count ->
            out.print(1)
        }
    %>'''
    assertEquals('111', simpleCall(text))
    }

    void testWithMarkupBuilderWithSemicolons(){
    def text = '''<%
        builder = new groovy.xml.MarkupBuilder(out);
        [1,2,3].each{ count ->
            out.print(1);
        }
    %>'''
    assertEquals('111', simpleCall(text))
    }

    void testWithQuotesInScriplet(){
    def text = '''<%
        ['1',"2",/3/].each{ x ->
            out.print(x)
        }
    %>'''
    assertEquals('123', simpleCall(text))
    }

    String simpleCall(input){
        bindingCall([:], input)
    }

    String bindingCall(binding, input){
        def template = new SimpleTemplateEngine(true).createTemplate(input)
        return template.make(binding).toString()
    }

}