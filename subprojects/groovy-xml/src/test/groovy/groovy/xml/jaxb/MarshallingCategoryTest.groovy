package groovy.xml.jaxb

import javax.xml.bind.JAXBContext

class MarshallingCategoryTest extends GroovyTestCase {
    JAXBContext jaxbContext = JAXBContext.newInstance(Person)
    Person p = new Person(name: 'JT', age: 20)

    void testMarshallAndUnmarshallObjectUsingCategoryOnMarshallerAndUnmarshaller() {
        use(MarshallingCategory) {
            String xml = jaxbContext.createMarshaller().marshal(p)
            assert jaxbContext.createUnmarshaller().unmarshal(xml, Person) == p
        }
    }

    void testMarshallAndUnmarshallObjectUsingCategoryOnJaxbContext() {
        use(MarshallingCategory) {
            String xml = jaxbContext.marshal(p)
            assert jaxbContext.unmarshal(xml, Person) == p
        }
    }
}