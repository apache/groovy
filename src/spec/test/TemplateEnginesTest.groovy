import gls.CompilableTestSupport

class TemplateEnginesTest extends CompilableTestSupport {

    void testSimpleTemplateEngine1() {
        // tag::simple_template_engine1[]
        def text = 'Dear "$firstname $lastname",\nSo nice to meet you in <% print city %>.\nSee you in ${month},\n${signed}'

        def binding = ["firstname":"Sam", "lastname":"Pullara", "city":"San Francisco", "month":"December", "signed":"Groovy-Dev"]

        def engine = new groovy.text.SimpleTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def result = 'Dear "Sam Pullara",\nSo nice to meet you in San Francisco.\nSee you in December,\nGroovy-Dev'

        assert result == template.toString()
        // end::simple_template_engine1[]
    }

    void testSimpleTemplateEngineOther() {
        def binding = [firstname: 'andrey']
        def engine = new groovy.text.SimpleTemplateEngine()
        def text = '''\
            // tag::simple_template_engine2[]
            $firstname
            // end::simple_template_engine2[]
        '''
        def template = engine.createTemplate(text).make(binding)
        def result = template.toString()
        assert result.readLines()[1].trim() == 'andrey'

        text = '''\
            // tag::simple_template_engine3[]
            ${firstname.capitalize()}
            // end::simple_template_engine3[]
        '''
        template = engine.createTemplate(text).make(binding)
        result = template.toString()
        assert result.readLines()[1].trim() == 'Andrey'

        binding = [city: 'Moscow']
        text = '''\
            // tag::simple_template_engine4[]
            <% print city %>
            // end::simple_template_engine4[]
        '''
        template = engine.createTemplate(text).make(binding)
        result = template.toString()
        assert result.readLines()[1].trim() == 'Moscow'

        binding = [city: 'New York']
        text = '''\
            // tag::simple_template_engine5[]
            <% print city == "New York" ? "The Big Apple" : city %>
            // end::simple_template_engine5[]
        '''
        template = engine.createTemplate(text).make(binding)
        result = template.toString()
        assert result.readLines()[1].trim() == 'The Big Apple'

        text = '''\
            // tag::simple_template_engine6[]
            <% print city == "New York" ? "\\"The Big Apple\\"" : city %>
            // end::simple_template_engine6[]
        '''
        template = engine.createTemplate(text).make(binding)
        result = template.toString()
        assert result.readLines()[1].trim() == '"The Big Apple"'

        text = '''\
            // tag::simple_template_engine7[]
            \\n
            // end::simple_template_engine7[]
        '''
        template = engine.createTemplate(text).make()
        result = template.toString()
        assert result.readLines().size() == 5

        text = '''\
            // tag::simple_template_engine8[]
            \\\\
            // end::simple_template_engine8[]
        '''
        template = engine.createTemplate(text).make()
        result = template.toString()
        assert result.readLines()[1].trim() == '\\'
    }

    void testGStringTemplateEngine() {
        def binding = [firstname: 'Sam', lastname: 'Pullara', city: 'New York', month: 'December', signed: 'Groovy-Dev']
        def engine = new groovy.text.GStringTemplateEngine()
        def text = '''\
            // tag::gstring_template_engine1[]
            Dear "$firstname $lastname",
            So nice to meet you in <% out << (city == "New York" ? "\\"The Big Apple\\"" : city) %>.
            See you in ${month},
            ${signed}
            // end::gstring_template_engine1[]
        '''
        def template = engine.createTemplate(text).make(binding)
        List result = template.toString().readLines()
        result.remove(0); result.remove(result.size() - 2)
        assert result.join('\n') == '''\
            Dear "Sam Pullara",
            So nice to meet you in "The Big Apple".
            See you in December,
            Groovy-Dev
        '''

        shouldCompile '''
            // tag::gstring_template_engine2[]
            def f = new File('test.template')
            def engine = new groovy.text.GStringTemplateEngine()
            def template = engine.createTemplate(f).make(binding)
            println template.toString()
            // end::gstring_template_engine2[]
        '''
    }

    void testXmlTemplateEngine() {
        // tag::xml_template_engine[]
        def binding = [firstname: 'Jochen', lastname: 'Theodorou', nickname: 'blackdrag', salutation: 'Dear']
        def engine = new groovy.text.XmlTemplateEngine()
        def text = '''\
            <document xmlns:gsp='http://groovy.codehaus.org/2005/gsp' xmlns:foo='baz' type='letter'>
                <gsp:scriptlet>def greeting = "${salutation}est"</gsp:scriptlet>
                <gsp:expression>greeting</gsp:expression>
                <foo:to>$firstname "$nickname" $lastname</foo:to>
                How are you today?
            </document>
        '''
        def template = engine.createTemplate(text).make(binding)
        println template.toString()
        // end::xml_template_engine[]
        
        assert template.toString() == '''\
<document type='letter'>
  Dearest
  <foo:to xmlns:foo='baz'>
    Jochen &quot;blackdrag&quot; Theodorou
  </foo:to>
  How are you today?
</document>
'''
    }
}