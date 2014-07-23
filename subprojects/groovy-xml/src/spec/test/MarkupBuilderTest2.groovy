/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.xml
import groovy.util.GroovyTestCase
import org.junit.Test
import groovy.xml.MarkupBuilder

/**
 * Tests for MarkupBuilder. The tests directly in this file are specific
 * to MarkupBuilder. Functionality in common with other Builders
 * is tested in the parent class.
 *
 * @author Groovy Documentation Community
 *
 * Additional Tests for MarkupBuilder. 
 *
 * This test set contain several of the original tests from groovy-core/subprojects/groovy-xml/src/test/groovy/groovy/xml/MarkupBuilderTest.groovy
 *
 * Some additional test senarios are added here to increase code coverage and implementation robustness.
 *  
 * The tests directly in this file are specific to MarkupBuilder. Other builders may extend this builder.     
 * Tests for other implemenattions would reside in their respective directories.
 *
 */
class MarkupBuilderTest2 extends GroovyTestCase {
    private StringWriter writer
    private MarkupBuilder xml

    protected void setUp() {
        writer = new StringWriter()
        xml = new MarkupBuilder(writer)
    }

    private assertExpectedXmlDefault(expectedXml) {
        checkXml expectedXml, writer
    }

    // assert both strings are the same using the GroovyTestCase junit method
    private checkXml(String expectedXml, Object writer) {
        assertEquals(expectedXml, writer.toString())
    }


    /**
     * Main test method. Checks that well-formed XML is generated
     * and that the appropriate characters are escaped with the
     * correct entities.
     */
    void testBuilder() {
        String expectedXml = '''\
<chars>
  <ampersand a='&amp;'>&amp;</ampersand>
  <quote attr='"'>"</quote>
  <apostrophe attr='&apos;'>'</apostrophe>
  <lessthan attr='value'>chars: &amp; &lt; &gt; '</lessthan>
  <element attr='value 1 &amp; 2'>chars: &amp; &lt; &gt; " in middle</element>
  <greaterthan>&gt;</greaterthan>
  <emptyElement />
  <null />
  <nullAttribute t1='' />
  <emptyWithAttributes attr1='set' />
  <emptyAttribute t1='' />
  <parent key='value'>
    <label for='usernameId'>Username: </label>
    <input name='test' id='1' />
  </parent>
</chars>'''

        // Generate the markup.
        xml.chars {
            ampersand(a: "&", "&")
            quote(attr: "\"", "\"")
            apostrophe(attr: "'", "'")
            lessthan(attr: "value", "chars: & < > '")
            element(attr: "value 1 & 2", "chars: & < > \" in middle")
            greaterthan(">")
            emptyElement()
            'null'(null)
            nullAttribute(t1:null)
            emptyWithAttributes(attr1:'set')
            emptyAttribute(t1:'')
            parent(key:'value'){
                label(for:'usernameId', 'Username: ')
                input(name:'test', id:1)
            }
        }

        assertEquals expectedXml, fixEOLs(writer.toString())
    }

    void testOmitAttributeSettingsKeepBothDefaultCase() {
        xml.element(att1:null, att2:'')
        assertExpectedXmlDefault "<element att1='' att2='' />"
    }

    void testOmitAttributeSettingsOmitNullKeepEmpty() {
        xml.omitNullAttributes = true
        xml.element(att1:null, att2:'')
        assertExpectedXmlDefault "<element att2='' />"
    }

    void testDelegateOnlyToSkipInternalClosureMethods() {
        def items = {
          pizza(price: 8.5)
          curry(price: 8)
          burger(price: 7.5)
        }
        items.resolveStrategy = Closure.DELEGATE_ONLY
        xml.menu(items)

        assertExpectedXmlDefault '''<menu>
  <pizza price='8.5' />
  <curry price='8' />
  <burger price='7.5' />
</menu>'''
    }


    protected assertExpectedXml(Closure markup, String expectedXml) {
        assertExpectedXml markup, null, expectedXml
    }

    protected assertExpectedXml(Closure markup, Closure configureBuilder, String expectedXml) {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)
        if (configureBuilder) configureBuilder(builder)
        markup.delegate = builder
        markup()
        checkXml(expectedXml, writer)
    }
    
    void testObjectNotDefined() {
// tag::markupbuilder_nullobject1[]
    MarkupBuilder markupbuilder;
    
    // markupbuilder should be null when not initialized
    assert markupbuilder == null;
// end::markupbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::markupbuilder_nullobject2[]
    MarkupBuilder markupbuilder = null;
    
    // markupbuilder should be null when initialized to null
    assert markupbuilder == null;
// end::markupbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::markupbuilder_object_exists1[]
    MarkupBuilder markupbuilder = new MarkupBuilder();
    
    // markupbuilder should not be null after construction
    assert markupbuilder != null;
// end::markupbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::markupbuilder_object_exists2[]
    MarkupBuilder markupbuilder = new MarkupBuilder();
    
    // markupbuilder should be an instance of correct MarkupBuilder class
    assert markupbuilder instanceof MarkupBuilder, 'default MarkupBuilder constructor did not build a version of MarkupBuilder'
// end::markupbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::markupbuilder_object_exists3[]
    shouldFail 
    {
        // markupbuilder should throw an exception when null parm used in constructor
        MarkupBuilder markupbuilder = new MarkupBuilder(null);
    }
// end::markupbuilder_object_exists3[]
    } // end of method

}
