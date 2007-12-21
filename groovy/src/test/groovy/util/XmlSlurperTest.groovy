package groovy.util

import groovy.xml.TraversalTestSupport
import groovy.xml.GpathSyntaxTestSupport
import groovy.xml.MixedMarkupTestSupport
import groovy.xml.StreamingMarkupBuilder

class XmlSlurperTest extends GroovyTestCase {

    def getRoot = { xml -> new XmlSlurper().parseText(xml) }

    void testWsdl() {
        def wsdl = '''
            <definitions name="AgencyManagementService"
                         xmlns:ns1="http://www.example.org/NS1"
                         xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                         xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                         xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
                         xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
                         xmlns="http://schemas.xmlsoap.org/wsdl/">                                              
                <message name="SomeRequest">                                                          
                    <part name="parameters" element="ns1:SomeReq" />                                  
                </message>                                                                            
                <message name="SomeResponse">                                                         
                    <part name="result" element="ns1:SomeRsp" />                                      
                </message>                                                                            
            </definitions>                                                                            
            '''
        def xml = new XmlSlurper().parseText(wsdl)
        assert xml.message.part.@element.findAll {it =~ /.Req$/}.size() == 1
        assert xml.message.part.findAll { true }.size() == 2
        assert xml.message.part.find { it.name() == 'part' }.name() == 'part'
        assert xml.message.findAll { true }.size() == 2
        assert xml.message.part.lookupNamespace("ns1") == "http://www.example.org/NS1"
        assert xml.message.part.lookupNamespace("") == "http://schemas.xmlsoap.org/wsdl/"
        assert xml.message.part.lookupNamespace("undefinedPrefix") == null
        xml.message.findAll { true }.each { assert it.name() == "message"}
    }

    void testElement() {
        GpathSyntaxTestSupport.checkElement(getRoot)
        GpathSyntaxTestSupport.checkFindElement(getRoot)
        GpathSyntaxTestSupport.checkElementTypes(getRoot)
        GpathSyntaxTestSupport.checkElementClosureInteraction(getRoot)
    }

    void testAttribute() {
        GpathSyntaxTestSupport.checkAttribute(getRoot)
        GpathSyntaxTestSupport.checkAttributes(getRoot)
    }

    void testNavigation() {
        GpathSyntaxTestSupport.checkChildren(getRoot)
        GpathSyntaxTestSupport.checkParent(getRoot)
        GpathSyntaxTestSupport.checkNestedSizeExpressions(getRoot)
    }

    void testTraversal() {
        TraversalTestSupport.checkDepthFirst(getRoot)
        TraversalTestSupport.checkBreadthFirst(getRoot)
    }

    void testMixedMarkup() {
        MixedMarkupTestSupport.checkMixedMarkup(getRoot)
    }
    
    void testReplace() {
        def input = "<doc><sec>Hello<p>World</p></sec></doc>"
        def replaceSlurper = new XmlSlurper().parseText(input) 
        
        replaceSlurper.sec.replaceNode{ node ->
          t(){ delegate.mkp.yield node.getBody() }
        }
        
        def outputSlurper = new StreamingMarkupBuilder()
        String output = outputSlurper.bind{ mkp.yield replaceSlurper }
        assert output == "<doc><t>Hello<p>World</p></t></doc>"
    }
}
