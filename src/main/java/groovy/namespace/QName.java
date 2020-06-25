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
package groovy.namespace;

import java.io.Serializable;

/**
 * <code>QName</code> class represents the value of a qualified name
 * as specified in <a href="http://www.w3.org/TR/xmlschema-2/#QName">XML
 * Schema Part2: Datatypes specification</a>.
 * <p>
 * The value of a QName contains a <b>namespaceURI</b>, a <b>localPart</b> and a <b>prefix</b>.
 * The localPart provides the local part of the qualified name. The
 * namespaceURI is a URI reference identifying the namespace.
 */
public class QName implements Serializable {
    private static final long serialVersionUID = -9029109610006696081L;

    /** comment/shared empty string */
    private static final String EMPTY_STRING = "";

    /** Field namespaceURI */
    private final String namespaceURI;

    /** Field localPart */
    private final String localPart;

    /** Field prefix */
    private final String prefix;

    /**
     * Constructor for the QName.
     *
     * @param localPart Local part of the QName
     */
    public QName(String localPart) {
        this(EMPTY_STRING, localPart, EMPTY_STRING);
    }

    /**
     * Constructor for the QName.
     *
     * @param namespaceURI Namespace URI for the QName
     * @param localPart Local part of the QName.
     */
    public QName(String namespaceURI, String localPart) {
        this(namespaceURI, localPart, EMPTY_STRING);
    }

    /**
     * Constructor for the QName.
     *
     * @param namespaceURI Namespace URI for the QName
     * @param localPart Local part of the QName.
     * @param prefix Prefix of the QName.
     */
    public QName(String namespaceURI, String localPart, String prefix) {
        this.namespaceURI = (namespaceURI == null)
                ? EMPTY_STRING
                : namespaceURI;
        if (localPart == null) {
            throw new IllegalArgumentException("invalid QName local part");
        } else {
            this.localPart = localPart;
        }

        if (prefix == null) {
            throw new IllegalArgumentException("invalid QName prefix");
        } else {
            this.prefix = prefix;
        }
    }

    /**
     * Gets the Namespace URI for this QName
     *
     * @return Namespace URI
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Gets the Local part for this QName
     *
     * @return Local part
     */
    public String getLocalPart() {
        return localPart;
    }

    /**
     * Gets the Prefix for this QName
     *
     * @return Prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the fully qualified name of this QName
     *
     * @return  a string representation of the QName
     */
    public String getQualifiedName() {
        return ((prefix.equals(EMPTY_STRING))
                ? localPart
                : prefix + ':' + localPart);
    }

    /**
     * Returns a string representation of this QName
     *
     * @return  a string representation of the QName
     */
    public String toString() {
        return ((namespaceURI.equals(EMPTY_STRING))
                ? localPart
                : '{' + namespaceURI + '}' + localPart);
    }

    /**
     * Tests this QName for equality with another object.
     * <p>
     * If the given object is not a QName or String equivalent or is null then this method
     * returns <tt>false</tt>.
     * <p>
     * For two QNames to be considered equal requires that both
     * localPart and namespaceURI must be equal. This method uses
     * <code>String.equals</code> to check equality of localPart
     * and namespaceURI. Any class that extends QName is required
     * to satisfy this equality contract.
     *
     * If the supplied object is a String, then it is split in two on the last colon
     * and the first half is compared against the prefix || namespaceURI
     * and the second half is compared against the localPart
     *
     * i.e.&#160;assert new QName("namespace","localPart").equals("namespace:localPart")
     *
     * Intended Usage: for gpath accessors, e.g.&#160;root.'urn:mynamespace:node'
     *
     * Warning: this equivalence is not commutative,
     * i.e.&#160;qname.equals(string) may be true/false  but string.equals(qname) is always false
     *
     * <p>
     * This method satisfies the general contract of the <code>Object.equals</code> method.
     *
     * @param o the reference object with which to compare
     *
     * @return <code>true</code> if the given object is identical to this
     *      QName: <code>false</code> otherwise.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof QName) {
            final QName qName = (QName) o;
            if (!namespaceURI.equals(qName.namespaceURI)) return false;
            return localPart.equals(qName.localPart);

        } else if (o instanceof String) {
            final String string = (String)o;
            if (string.length() == 0) return false;
            int lastColonIndex = string.lastIndexOf(':');
            if (lastColonIndex < 0 || lastColonIndex == string.length() - 1) return false;
            final String stringPrefix = string.substring(0,lastColonIndex);
            final String stringLocalPart = string.substring(lastColonIndex + 1);
            if (stringPrefix.equals(prefix) || stringPrefix.equals(namespaceURI)) {
                return localPart.equals(stringLocalPart);
            }
            return false;
        }
        return false;
    }

    /**
     * Tests if this QName matches another object.
     * <p>
     * If the given object is not a QName or String equivalent or is null then this method
     * returns <tt>false</tt>.
     * <p>
     * For two QNames to be considered matching requires that both
     * localPart and namespaceURI must be equal or one of them is a wildcard.
     *
     * If the supplied object is a String, then it is split in two on the last colon
     * and the first half is matched against the prefix || namespaceURI
     * and the second half is matched against the localPart
     *
     * @param o the reference object with which to compare
     *
     * @return <code>true</code> if the given object matches
     * this QName: <code>false</code> otherwise.
     */
    public boolean matches(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof QName) {
            final QName qName = (QName) o;
            if (!namespaceURI.equals(qName.namespaceURI) && !namespaceURI.equals("*") && !qName.namespaceURI.equals("*")) return false;
            return localPart.equals(qName.localPart) || localPart.equals("*") || qName.localPart.equals("*");
        } else if (o instanceof String) {
            final String string = (String)o;
            if (string.length() == 0) return false;
            // try matching against 'prefix:localname'
            int lastColonIndex = string.lastIndexOf(':');
            if (lastColonIndex < 0 && prefix.length() == 0) return string.equals(localPart);
            if (lastColonIndex < 0 || lastColonIndex == string.length() - 1) return false;
            final String stringPrefix = string.substring(0,lastColonIndex);
            final String stringLocalPart = string.substring(lastColonIndex + 1);
            if (stringPrefix.equals(prefix) || stringPrefix.equals(namespaceURI) || stringPrefix.equals("*")) {
                return localPart.equals(stringLocalPart) || stringLocalPart.equals("*");
            }
        }
        return false;
    }

    /**
     * Returns a QName holding the value of the specified String.
     * <p>
     * The string must be in the form returned by the QName.toString()
     * method, i.e. "{namespaceURI}localPart", with the "{namespaceURI}"
     * part being optional.
     * <p>
     * This method doesn't do a full validation of the resulting QName.
     * In particular, it doesn't check that the resulting namespace URI
     * is a legal URI (per RFC 2396 and RFC 2732), nor that the resulting
     * local part is a legal NCName per the XML Namespaces specification.
     *
     * @param s the string to be parsed
     * @throws java.lang.IllegalArgumentException If the specified String cannot be parsed as a QName
     * @return QName corresponding to the given String
     */
    public static QName valueOf(String s) {

        if ((s == null) || s.isEmpty()) {
            throw new IllegalArgumentException("invalid QName literal");
        }

        if (s.charAt(0) == '{') {
            int i = s.indexOf('}');

            if (i == -1) {
                throw new IllegalArgumentException("invalid QName literal");
            }

            if (i == s.length() - 1) {
                throw new IllegalArgumentException("invalid QName literal");
            } else {
                return new QName(s.substring(1, i), s.substring(i + 1));
            }
        } else {
            return new QName(s);
        }
    }

    /**
     * Returns a hash code value for this QName object. The hash code
     * is based on both the localPart and namespaceURI parts of the
     * QName. This method satisfies the  general contract of the
     * <code>Object.hashCode</code> method.
     *
     * @return a hash code value for this Qname object
     */
    public int hashCode() {
        int result;
        result = namespaceURI.hashCode();
        result = 29 * result + localPart.hashCode();
        return result;
    }
} 