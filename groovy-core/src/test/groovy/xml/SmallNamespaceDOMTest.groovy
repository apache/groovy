package groovy.xml

class SmallNamespaceDOMTest extends TestXmlSupport {
    
    void testTree() {
        def builder = DOMBuilder.newInstance()
        def xsd = NamespaceBuilder.newInstance(builder, 'http://www.w3.org/2001/XMLSchema', 'xsd')
        
        def root = xsd.schema() {
          element(name:'purchaseOrder', type:'PurchaseOrderType')
          element(name:'comment', type:'xsd:string')
          complexType(name:'PurchaseOrderType') {
            sequence {
              element(name:'shipTo', type:'USAddress')
              element(name:'billTo', type:'USAddress')
              element(minOccurs:'0', ref:'comment')
              element(name:'items', type:'Items')
            }
            attribute(name:'orderDate', type:'xsd:date')
          }
        }        
        assert root != null
        
        dump(root)
    }
}
