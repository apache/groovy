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
package org.apache.groovy.jaxb.extensions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This class defines new groovy methods which appear on Jaxb-related JDK
 * classes ({@code JAXBContext}, {@code Marshaller}) inside the Groovy environment.
 * Static methods are used with the first parameter being the destination class.
 */
public class JaxbExtensions {

    /**
     * Marshall an object to a xml {@code String}.
     *
     * @param self a Marshaller which can marshall the type of the given object
     * @param jaxbElement object to marshall to a {@code String}
     * @return {@code String} representing the object as xml
     */
    public static <T> String marshal(Marshaller self, T jaxbElement) throws JAXBException {
        StringWriter sw = new StringWriter();
        self.marshal(jaxbElement, sw);
        return sw.toString();
    }

    /**
     * Marshall an object to a xml {@code String}.
     *
     * @param self a JaxbContext, which recognizes the type of the given object
     * @param jaxbElement object to marshall to a {@code String}
     * @return String representing the object as xml
     */
    public static <T> String marshal(JAXBContext self, T jaxbElement) throws JAXBException {
        return marshal(self.createMarshaller(), jaxbElement);
    }

    /**
     * Unmarshal xml data from the given {@code String} to object of the given type.
     *
     * @param self Unmarshaller, a Unmarshaller which can unmarshall the type of the given object
     * @param xml xml data as a {@link String}
     * @param declaredType appropriate JAXB mapped class to hold node's xml data
     * @return instance of destination class unmarshalled from xml
     */
    public static <T> T unmarshal(Unmarshaller self, String xml, Class<T> declaredType) throws JAXBException {
        StringReader sr = new StringReader(xml);
        return declaredType.cast(self.unmarshal(sr));
    }

    /**
     * Unmarshal xml data from the given {@code String} to object of the given type.
     *
     * @param self a JaxbContext, which recognizes the type of the given object
     * @param xml xml data as a {@link String}
     * @param declaredType appropriate JAXB mapped class to hold node's xml data
     * @return instance of destination class unmarshalled from xml
     */
    public static <T> T unmarshal(JAXBContext self, String xml, Class<T> declaredType) throws JAXBException {
        return unmarshal(self.createUnmarshaller(), xml, declaredType);
    }
}
