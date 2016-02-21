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
package groovy.xml.jaxb

import groovy.transform.CompileStatic

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

/**
 * This class defines methods which simplifies marshalling object to
 * {@link String} containing xml and unmarshalling xml in {@link String} to object.
 *
 * @author Dominik Przybysz
 */
@CompileStatic
class JaxbGroovyMethods {

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
