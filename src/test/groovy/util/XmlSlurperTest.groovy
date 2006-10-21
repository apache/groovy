package groovy.util

import groovy.xml.TraversalTestSupport
import groovy.xml.GpathSyntaxTestSupport

class XmlSlurperTest extends GroovyTestCase {

    def getRoot = { xml -> new XmlSlurper().parseText(xml) }

    void testWsdl() {
        def wsdl = '''
            <definitions name="AgencyManagementService">                                              
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
    }

    void testTraversal() {
        TraversalTestSupport.checkDepthFirst(getRoot)
        TraversalTestSupport.checkBreadthFirst(getRoot)
    }

    void testMixedMarkup() {
        //GpathSyntaxTestSupport.checkMixedMarkup(getRoot)
    }
}
