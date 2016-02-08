package groovy.xml.jaxb

import groovy.transform.CompileStatic

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

@CompileStatic
class MarshallingCategory {

    static <T> String marshal(Marshaller marshaller, T o) {
        StringWriter sw = new StringWriter()
        marshaller.marshal(o, sw)
        return sw.toString()
    }

    static <T> String marshal(JAXBContext jaxbContext, T o) {
        StringWriter sw = new StringWriter()
        jaxbContext.createMarshaller().marshal(o, sw)
        return sw.toString()
    }

    static <T> T unmarshal(Unmarshaller unmarshaller, String xml, Class<T> dest) {
        StringReader sr = new StringReader(xml)
        return dest.cast(unmarshaller.unmarshal(sr))
    }

    static <T> T unmarshal(JAXBContext jaxbContext, String xml, Class<T> dest) {
        StringReader sr = new StringReader(xml)
        return dest.cast(jaxbContext.createUnmarshaller().unmarshal(sr))
    }
}
