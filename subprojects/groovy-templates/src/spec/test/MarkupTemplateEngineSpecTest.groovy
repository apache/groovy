/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import java.nio.charset.Charset

class MarkupTemplateEngineSpecTest extends GroovyTestCase {
    private MarkupTemplateEngine engine
    private TemplateConfiguration config
    private Map model
    private Map modelTypes
    private String templateContents
    private String expectedRendered

    @Override
    void setUp() {
        super.setUp();
        config = new TemplateConfiguration()
        model = null
        modelTypes = null
    }

    @Override
    void tearDown() {
        super.tearDown();
        config = null
        engine = null
    }

    private void assertRendered() {
        engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = modelTypes ?
                engine.createTypeCheckedModelTemplate(templateContents, modelTypes) :
                engine.createTemplate(templateContents);
        def ready = template.make(model)
        def sw = new StringWriter()
        ready.writeTo(sw)
        assert sw.toString() == expectedRendered
    }

    private void assertError(String error) {
        def msg = shouldFail {
            assertRendered()
        }
        assert msg.contains(error)
    }

    private String stripAsciidocMarkup(String string) {
        boolean inside = false
        StringBuilder sb = new StringBuilder()
        new StringReader(string).eachLine { line ->
            if (line =~ 'tag::') {
                inside = true
            } else if (line =~ 'end::') {
                inside = false
            } else if (inside) {
                if (sb.length() > 0) {
                    sb.append(config.newLineString)
                }
                sb.append(line)
            }
        }
        sb
    }

    void testSimpleTemplate() {
        templateContents = '''
// tag::example1_template[]
xmlDeclaration()
cars {
   cars.each {
       car(make: it.make, model: it.model)
   }
}
// end::example1_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::example1_expected[]
<?xml version='1.0'?>
<cars><car make='Peugeot' model='508'/><car make='Toyota' model='Prius'/></cars>
// end::example1_expected[]
'''
        // tag::example1_model[]
        model = [cars: [new Car(make: 'Peugeot', model: '508'), new Car(make: 'Toyota', model: 'Prius')]]
        // end::example1_model[]
        assertRendered()

/*
// tag::example1_template_with_bullets[]
xmlDeclaration()                                <1>
cars {                                          <2>
   cars.each {                                  <3>
       car(make: it.make, model: it.model)      <4>
   }                                            <5>
}
// end::example1_template_with_bullets[]
 */

    }

    void testSimpleHtmlTemplate() {
        templateContents = '''
// tag::example2_template[]
yieldUnescaped '<!DOCTYPE html>'                                                    // <1>
html(lang:'en') {                                                                   // <2>
    head {                                                                          // <3>
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')      // <4>
        title('My page')                                                            // <5>
    }                                                                               // <6>
    body {                                                                          // <7>
        p('This is an example of HTML contents')                                    // <8>
    }                                                                               // <9>
}                                                                                   // <10>
// end::example2_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::example2_expected[]
<!DOCTYPE html><html lang='en'><head><meta http-equiv='"Content-Type" content="text/html; charset=utf-8"'/><title>My page</title></head><body><p>This is an example of HTML contents</p></body></html>
// end::example2_expected[]
'''
        assertRendered()
    }

    void testYield() {
        templateContents = '''
// tag::yield[]
yield 'Some text with <angle brackets>'
// end::yield[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::yield_expected[]
Some text with &lt;angle brackets&gt;
// end::yield_expected[]
'''
        assertRendered()
    }

    void testYieldUnescaped() {
        templateContents = '''
// tag::yieldUnescaped[]
yieldUnescaped 'Some text with <angle brackets>'
// end::yieldUnescaped[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::yieldUnescaped_expected[]
Some text with <angle brackets>
// end::yieldUnescaped_expected[]
'''
        assertRendered()
    }

    void testXMLDeclaration() {
        config.newLineString = ''
        templateContents = '''
// tag::xmlDeclaration[]
xmlDeclaration()
// end::xmlDeclaration[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::xmlDeclaration_expected[]
<?xml version='1.0'?>
// end::xmlDeclaration_expected[]
'''
        assertRendered()
    }

    void testXMLDeclarationWithEncoding() {
        config.newLineString = ''
        config.declarationEncoding = 'UTF-8'
        templateContents = '''
// tag::xmlDeclaration_encoding[]
xmlDeclaration()
// end::xmlDeclaration_encoding[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::xmlDeclaration_encoding_expected[]
<?xml version='1.0' encoding='UTF-8'?>
// end::xmlDeclaration_encoding_expected[]
'''
        assertRendered()
    }

    void testComment() {
        templateContents = '''
// tag::comment[]
comment 'This is <a href="foo.html">commented out</a>'
// end::comment[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::comment_expected[]
<!--This is <a href="foo.html">commented out</a>-->
// end::comment_expected[]
'''
        assertRendered()
    }

    void testNewLine() {
        config.newLineString = '\n'
        templateContents = '''
// tag::newline[]
p('text')
newLine()
p('text on new line')
// end::newline[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::newline_expected[]
<p>text</p>
<p>text on new line</p>
// end::newline_expected[]
'''
        assertRendered()
    }

    void testPI() {
        config.newLineString = ''
        templateContents = '''
// tag::pi[]
pi("xml-stylesheet":[href:"mystyle.css", type:"text/css"])
// end::pi[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::pi_expected[]
<?xml-stylesheet href='mystyle.css' type='text/css'?>
// end::pi_expected[]
'''
        assertRendered()
    }

    void testTryEscape() {
        templateContents = '''
// tag::tryEscape[]
yieldUnescaped tryEscape('Some text with <angle brackets>')
// end::tryEscape[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::tryEscape_expected[]
Some text with &lt;angle brackets&gt;
// end::tryEscape_expected[]
'''
        assertRendered()
    }

    void testIncludeTemplate() {
        templateContents = '''
// tag::include_template[]
include template: 'other_template.tpl'
// end::include_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::include_template_expected[]
included as a template
// end::include_template_expected[]
'''
        assertRendered()
    }

    void testIncludeRaw() {
        templateContents = '''
// tag::include_raw[]
include unescaped: 'raw.txt'
// end::include_raw[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::include_raw_expected[]
This is raw contents that may include <tags>.
// end::include_raw_expected[]
'''
        assertRendered()
    }

    void testIncludeEscaped() {
        templateContents = '''
// tag::include_escaped[]
include escaped: 'to_be_escaped.txt'
// end::include_escaped[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::include_escaped_expected[]
This is contents that may include &lt;tags&gt;.
// end::iinclude_escaped_expected[]
'''
        assertRendered()
    }

    void testExpandEmptyElements() {
        templateContents = '''
// tag::expandEmptyElements[]
p()
// end::expandEmptyElements[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::expandEmptyElements_false[]
<p/>
// end::expandEmptyElements_false[]
'''
        assertRendered()
        config.expandEmptyElements = true
        expectedRendered = stripAsciidocMarkup '''
// tag::expandEmptyElements_true[]
<p></p>
// end::expandEmptyElements_true[]
'''
        assertRendered()
    }

    void testUseDoubleQuotes() {
        templateContents = '''
// tag::useDoubleQuotes[]
tag(attr:'value')
// end::useDoubleQuotes[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::useDoubleQuotes_false[]
<tag attr='value'/>
// end::useDoubleQuotes_false[]
'''
        assertRendered()
        config.useDoubleQuotes = true
        expectedRendered = stripAsciidocMarkup '''
// tag::useDoubleQuotes_true[]
<tag attr="value"/>
// end::useDoubleQuotes_true[]
'''
        assertRendered()
    }

    void testNewLineString() {
        config.newLineString = 'BAR'
        templateContents = '''
// tag::newLineString[]
p('foo')
newLine()
p('baz')
// end::newLineString[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::newLineString_expected[]
<p>foo</p>BAR<p>baz</p>
// end::newLineString_expected[]
'''
        assertRendered()
    }

    void testAutoformat() {
        config.newLineString = '\n'

        // do NOT change the Java-style of the code below! Used in documentation!
        // tag::autoformat_setup[]
        config.setAutoNewLine(true);
        config.setAutoIndent(true);
        // end::autoformat_setup[]

        templateContents = '''
// tag::autoformat_template[]
html {
    head {
        title('Title')
    }
}
// end::autoformat_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::autoformat_template_expected[]
<html>
    <head>
        <title>Title</title>
    </head>
</html>
// end::autoformat_template_expected[]
'''
        assertRendered()

        templateContents = '''
// tag::autoformat_template2[]
html {
    head { title('Title')
    }
}
// end::autoformat_template2[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::autoformat_template2_expected[]
<html>
    <head><title>Title</title>
    </head>
</html>
// end::autoformat_template2_expected[]
'''
        assertRendered()

        templateContents = '''
// tag::autoformat_template3[]
html {
    head {
        meta(attr:'value')          // <1>
        title('Title')              // <2>
        newLine()                   // <3>
        meta(attr:'value2')         // <4>
    }
}
// end::autoformat_template3[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::autoformat_template3_expected[]
<html>
    <head>
        <meta attr='value'/><title>Title</title>
        <meta attr='value2'/>
    </head>
</html>
// end::autoformat_template3_expected[]
'''
        assertRendered()
    }

    void testAutoEscape() {
        config.newLineString = '\n'

        // do NOT change the Java-style of the code below! Used in documentation!
        // tag::autoescape_setup[]
        config.setAutoEscape(false);
        model = new HashMap<String,Object>();
        model.put("unsafeContents", "I am an <html> hacker.");
        // end::autoescape_setup[]

        templateContents = '''
// tag::autoescape_template[]
html {
    body {
        div(unsafeContents)
    }
}
// end::autoescape_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::autoescape_template_expected[]
<html><body><div>I am an <html> hacker.</div></body></html>
// end::autoescape_template_expected[]
'''
        assertRendered()

        // tag::autoescape_setup_fixed[]
        config.setAutoEscape(true);
        // end::autoescape_setup_fixed[]

        expectedRendered = stripAsciidocMarkup '''
// tag::autoescape_template_fixed_expected[]
<html><body><div>I am an &lt;html&gt; hacker.</div></body></html>
// end::autoescape_template_fixed_expected[]
'''
        assertRendered()

        templateContents = '''
// tag::autoescape_template_unescaped[]
html {
    body {
        div(unescaped.unsafeContents)
    }
}
// end::autoescape_template_unescaped[]
'''

        expectedRendered = stripAsciidocMarkup '''
// tag::autoescape_template_expected2[]
<html><body><div>I am an <html> hacker.</div></body></html>
// end::autoescape_template_expected2[]
'''
        assertRendered()

    }

    void testLocale() {
        // TODO shouldn't this (or a better) test pass in all environments??
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            config.locale = Locale.ENGLISH
            templateContents = '''
// tag::locale_explicit_import[]
include template: 'locale_include_fr_FR.tpl'
// end::locale_explicit_import[]
'''
            expectedRendered = stripAsciidocMarkup '''
// tag::locale_explicit_import_expected[]
Texte en français
// end::locale_explicit_import_expected[]
'''
            assertRendered()

            templateContents = '''
// tag::locale_implicit_import[]
include template: 'locale_include.tpl'
// end::locale_implicit_import[]
'''

            expectedRendered = stripAsciidocMarkup '''
// tag::locale_implicit_import_expected[]
Default text
// end::locale_implicit_import_expected[]
'''
            assertRendered()

            config.locale = Locale.FRANCE
            expectedRendered = stripAsciidocMarkup '''
// tag::locale_implicit_import_expected2[]
Texte en français
// end::locale_implicit_import_expected2[]
'''
            assertRendered()

        }
    }

    void testRenderingSetup() {
        // NOTE: please keep the Java style syntax here, it is used in the docs!
        def writer = new StringWriter()
        // tag::rendering_setup[]
        TemplateConfiguration config = new TemplateConfiguration();         // <1>
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config);     // <2>
        Template template = engine.createTemplate("p('test template')");    // <3>
        Map<String, Object> model = new HashMap<>();                        // <4>
        Writable output = template.make(model);                             // <5>
        output.writeTo(writer);                                             // <6>
        // end::rendering_setup[]
        assert writer.toString() == '<p>test template</p>'
    }

    void testRenderingByTemplateName() {
        // NOTE: please keep the Java style syntax here, it is used in the docs!
        def writer = new StringWriter()
        TemplateConfiguration config = new TemplateConfiguration();
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config);
        Map<String, Object> model = new HashMap<>();
        // tag::rendering_by_name[]
        Template template = engine.createTemplateByPath("main.tpl");
        Writable output = template.make(model);
        output.writeTo(writer);
        // end::rendering_by_name[]
        assert writer.toString() == '<p>test template</p>'
    }

    public static class Car {
        String make
        String model
    }

    void testCustomSuperClass() {
        // NOTE: please keep the Java style syntax here, it is used in the docs!
        // tag::custombase_config[]
        config.setBaseTemplateClass(MyTemplate.class);
        // end::custombase_config[]

        templateContents = '''
// tag::custombase_template[]
if (hasModule('foo')) {
    p 'Found module [foo]'
} else {
    p 'Module [foo] not found'
}
// end::custombase_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::custombase_expected[]
<p>Module [foo] not found</p>
// end::custombase_expected[]
'''
        assertRendered()
    }

    void testTypeCheckedModel() {
        // NOTE: please keep the Java style syntax here, it is used in the docs!
        // tag::typechecked_setup_no_stc[]
        Page p = new Page();
        p.setTitle("Sample page");
        p.setBody("Page body");
        List<Page> pages = new LinkedList<>();
        pages.add(p);
        model = new HashMap<String,Object>();
        model.put("pages", pages);
        // end::typechecked_setup_no_stc[]

        templateContents = '''
// tag::typechecked_template[]
pages.each { page ->                    // <1>
    p("Page title: $page.title")        // <2>
    p(page.text)                        // <3>
}
// end::typechecked_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::typechecked_template_expected[]
<p>Page title: Sample page</p><p>Page body</p>
// end::typechecked_template_expected[]
'''
        assertError 'No such property: text'


        modelTypes = [pages: 'List<MarkupTemplateEngineSpecTest.Page>']
        /*
        // tag::typechecked_setup_fixed[]
        modelTypes = new HashMap<String,String>();                                          // <1>
        modelTypes.put("pages", "List<Page>");                                              // <2>
        Template template = engine.createTypeCheckedModelTemplate("main.tpl", modelTypes)   // <3>
        // end::typechecked_setup_fixed[]
         */

        assertError 'No such property: text for class: MarkupTemplateEngineSpecTest$Page'
    }

    void testInlinedTypeCheckedModel() {
        // NOTE: please keep the Java style syntax here, it is used in the docs!
        // tag::typechecked_inlined_setup[]
        Page p = new Page();
        p.setTitle("Sample page");
        p.setBody("Page body");
        List<Page> pages = new LinkedList<>();
        pages.add(p);
        model = new HashMap<String,Object>();
        model.put("pages", pages);
        // end::typechecked_inlined_setup[]

        templateContents = '''import MarkupTemplateEngineSpecTest.Page as Page
// tag::typechecked_inlined_template[]
modelTypes = {                          // <1>
    List<Page> pages                    // <2>
}

pages.each { page ->
    p("Page title: $page.title")
    p(page.text)
}
// end::typechecked_inlined_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::typechecked_inlined_template_expected[]
<p>Page title: Sample page</p><p>Page body</p>
// end::typechecked_inlined_template_expected[]
'''

        assertError 'No such property: text for class: MarkupTemplateEngineSpecTest$Page'
    }

    void testFragment() {
        model = new HashMap<String,Object>();
        model.put("pages", Arrays.asList("Page 1", "Page 2"));
        templateContents = '''
// tag::fragment_template[]
ul {
    pages.each {
        fragment "li(line)", line:it
    }
}
// end::fragment_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::fragment_expected[]
<ul><li>Page 1</li><li>Page 2</li></ul>
// end::fragment_expected[]
'''
        assertRendered()
    }

    void testLayout() {
        templateContents = '''
// tag::layout_template[]
layout 'layout-main.tpl',                                   // <1>
    title: 'Layout example',                                // <2>
    bodyContents: contents { p('This is the body') }        // <3>
// end::layout_template[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::layout_expected[]
<html><head><title>Layout example</title></head><body><p>This is the body</p></body></html>
// end::layout_expected[]
'''
        assertRendered()
    }

    void testLayoutWithInheritedModel() {
        // tag::layout_template_inherit_model[]
        model = new HashMap<String,Object>();
        model.put('title','Title from main model');
        // end::layout_template_inherit_model[]

        templateContents = '''
// tag::layout_template_inherit[]
layout 'layout-main.tpl', true,                             // <1>
    bodyContents: contents { p('This is the body') }
// end::layout_template_inherit[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::layout_expected_2[]
<html><head><title>Title from main model</title></head><body><p>This is the body</p></body></html>
// end::layout_expected_2[]
'''
        assertRendered()

        templateContents = '''
// tag::layout_template_inherit_override[]
layout 'layout-main.tpl', true,                             // <1>
    title: 'overridden title',                               // <2>
    bodyContents: contents { p('This is the body') }
// end::layout_template_inherit_override[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::layout_expected_3[]
<html><head><title>overridden title</title></head><body><p>This is the body</p></body></html>
// end::layout_expected_3[]
'''
        assertRendered()
    }

    void testStringMarkupGotcha() {
        templateContents = '''
// tag::gotcha_strings_longversion[]
p {
    yield "This is a "
    a(href:'target.html', "link")
    yield " to another page"
}
// end::gotcha_strings_longversion[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::gotcha_strings_longversion_expected[]
<p>This is a <a href='target.html'>link</a> to another page</p>
// end::gotcha_strings_longversion_expected[]
'''
        assertRendered()

        templateContents = '''
// tag::gotcha_strings_naive_fail[]
p {
    yield "This is a ${a(href:'target.html', "link")} to another page"
}
// end::gotcha_strings_naive_fail[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::gotcha_strings_naive_fail_expected[]
<p><a href='target.html'>link</a>This is a  to another page</p>
// end::gotcha_strings_naive_fail_expected[]
'''
        assertRendered()

        templateContents = '''
// tag::gotcha_strings_stringof[]
p("This is a ${stringOf {a(href:'target.html', "link")}} to another page")
// end::gotcha_strings_stringof[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::gotcha_strings_stringof_expected[]
<p>This is a <a href='target.html'>link</a> to another page</p>
// end::gotcha_strings_stringof_expected[]
'''
        assertRendered()

        templateContents = '''
// tag::gotcha_strings_stringof_dollar[]
p("This is a ${$a(href:'target.html', "link")} to another page")
// end::gotcha_strings_stringof_dollar[]
'''
        expectedRendered = stripAsciidocMarkup '''
// tag::gotcha_strings_stringof_dollar_expected[]
<p>This is a <a href='target.html'>link</a> to another page</p>
// end::gotcha_strings_stringof_dollar_expected[]
'''
        assertRendered()

    }

    // tag::page_class[]
    public class Page {

        Long id
        String title
        String body
    }
    // end::page_class[]
}
