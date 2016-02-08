package groovy.xml.jaxb

import groovy.transform.EqualsAndHashCode

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement

@EqualsAndHashCode
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Person {
    String name
    int age
}
