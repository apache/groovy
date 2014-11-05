package groovy.text

import spock.lang.*

import static StreamingTemplateEngineSpecification.EngineType.*

class StreamingTemplateEngineSpecification extends Specification {

  enum EngineType { 
    STREAMING('StreamingTemplateEngine'),
    SIMPLE('SimpleTemplateEngine'), 
    GSTRING('GStringTemplateEngine')
    String displayString
    
    EngineType(displayString) {
      this.displayString = displayString
    }
    
    String toString() {
      displayString      
    }
  }

  @Shared Map defaultBinding = [alice: 'Alice', rabbit: 'Rabbit', queen: 'Queen', desk: 'writing desk']

  private String template(EngineType type, String data, Map binding=null) {
    TemplateEngine engine
    switch (type) {
      case STREAMING:
        engine = new StreamingTemplateEngine()
        break
      case SIMPLE: 
        engine = new SimpleTemplateEngine()
        break
      case GSTRING: 
        engine = new GStringTemplateEngine()
        break
    }
    Template template = engine.createTemplate(data)

    Writable writable = (binding ? template.make(binding) : template.make())
    StringWriter sw = new StringWriter()
    writable.writeTo(sw)

    sw
  }

  @Unroll
  def "#testName - #engineType should evaluate '#data' to '#expectedResult' using binding '#binding'"() {
    expect: 
      template(engineType, data, binding) == expectedResult

    where:
      data                   | expectedResult      | engineType     | binding        | testName
      ''                     | ''                  | STREAMING      | null           | 'emptyStringNoBinding'
      ''                     | ''                  | STREAMING      | defaultBinding | 'emptyStringWithBinding'
      'bob'                  | 'bob'               | STREAMING      | null           | 'noExpressionsNoBinding'
      'bob'                  | 'bob'               | STREAMING      | defaultBinding | 'noExpressionsWithBinding'

      '\\Hello World'        | '\\Hello World'     | STREAMING      | null           | 'noExpressionsNoBindingEscapingAtStart'
      '\\Hello World'        | '\\Hello World'     | STREAMING      | defaultBinding | 'noExpressionsWithBindingEscapingAtStart'
      '\\\\Hello World'      | '\\\\Hello World'   | STREAMING      | null           | 'noExpressionsNoBindingDoubleEscapingAtStart'
      '\\\\Hello World'      | '\\\\Hello World'   | STREAMING      | defaultBinding | 'noExpressionsWithBindingDoubleEscapingAtStart'
      '\\\\\\Hello World'    | '\\\\\\Hello World' | STREAMING      | null           | 'noExpressionsNoBindingTripleEscapingAtStart'
      '\\\\\\Hello World'    | '\\\\\\Hello World' | STREAMING      | defaultBinding | 'noExpressionsWithBindingTripleEscapingAtStart'

      'Hello World\\'        | 'Hello World\\'     | STREAMING      | null           | 'noExpressionsNoBindingEscapingAtEnd'
      'Hello World\\'        | 'Hello World\\'     | STREAMING      | defaultBinding | 'noExpressionsWithBindingEscapingAtEnd'
      'Hello World\\\\'      | 'Hello World\\\\'   | STREAMING      | null           | 'noExpressionsNoBindingDoubleEscapingAtEnd'
      'Hello World\\\\'      | 'Hello World\\\\'   | STREAMING      | defaultBinding | 'noExpressionsWithBindingDoubleEscapingAtEnd'
      'Hello World\\\\\\'    | 'Hello World\\\\\\' | STREAMING      | null           | 'noExpressionsNoBindingTripleEscapingAtEnd'
      'Hello World\\\\\\'    | 'Hello World\\\\\\' | STREAMING      | defaultBinding | 'noExpressionsWithBindingTripleEscapingAtEnd'
  }

  def "should handle \\r\\n line feeds correctly"() {
    setup:
      String basic = '<%\r\n' +
                     'def var1 = "cookie"\r\n' +
                     'out << "This is var1: ${var1}"\r\n' +
                     '%>\r\n' +
                     'Style 1:\r\n' +
                     '<%= "Again, this is var1:${var1}" %>\r\n' +
                     '\r\n' +
                     'Style 2:\r\n' +
                     '${"var1:" + var1}'

    when:
      String gStringTemplate = new GStringTemplateEngine().createTemplate(basic).make().toString()
      String streamingTemplate = new StreamingTemplateEngine().createTemplate(basic).make().toString()

    then:
      gStringTemplate == streamingTemplate
  }

  def "should handle simple embedded if statements"() {
    setup:
      String templateText = 'before "<% if (false) { %>should not be included<% } else { %>should be included<% } %>" after'

    when:
      Template template = new StreamingTemplateEngine().createTemplate(templateText)
      String result  = template.make().toString()

    then:
      result == 'before "should be included" after'
  }

  def "should handle complex embedded if statements"() {
    setup:
      String templateText = 'first line text\n' +
                            '<%\n' +
                            'def var1 = "test"\n' +
                            'if (false) {%>\n' +
                            '  non executed text\n' +
                            '  <%\n' +
                            '  out << "more non-executed text"\n' +
                            '} else {%>\n' +
                            '  else stuff\n' +
                            '<% \n' +
                            '} %>\n' +
                            '\n' +
                            'last line text\n'

    when:
      Template template1 = new StreamingTemplateEngine().createTemplate(templateText)
      Template template2 = new GStringTemplateEngine().createTemplate(templateText)

      String streamingResult  = template1.make().toString()
      String gStringResult    = template2.make().toString()

    then:
      streamingResult == gStringResult
  }
}
