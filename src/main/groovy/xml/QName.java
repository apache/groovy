/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation ( http://www.apache.org/ )."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org .
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * < http://www.apache.org/ >.
 */
package groovy.xml;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * <code>QName</code> class represents the value of a qualified name
 * as specified in <a href=" http://www.w3.org/TR/xmlschema-2/#QName ">XML
 * Schema Part2: Datatypes specification</a>.
 * <p>
 * The value of a QName contains a <b>namespaceURI</b>, a <b>localPart</b> and a <b>prefix</b>.
 * The localPart provides the local part of the qualified name. The
 * namespaceURI is a URI reference identifying the namespace.
 *
 * @version 1.1
 */
public class QName implements Serializable {

    /** comment/shared empty string */
    private static final String emptyString = "".intern();

    /** Field namespaceURI */
    private String namespaceURI;

    /** Field localPart */
    private String localPart;

    /** Field prefix */
    private String prefix;

    /**
     * Constructor for the QName.
     *
     * @param localPart Local part of the QName
     */
    public QName(String localPart) {
        this(emptyString, localPart, emptyString);
    }

    /**
     * Constructor for the QName.
     *
     * @param namespaceURI Namespace URI for the QName
     * @param localPart Local part of the QName.
     */
    public QName(String namespaceURI, String localPart) {
        this(namespaceURI, localPart, emptyString);
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
                ? emptyString
                : namespaceURI.intern();
        if (localPart == null) {
            throw new IllegalArgumentException("invalid QName local part");
        } else {
            this.localPart = localPart.intern();
        }

        if (prefix == null) {
            throw new IllegalArgumentException("invalid QName prefix");
        } else {
            this.prefix = prefix.intern();
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

        return ((prefix == emptyString)
                ? localPart
                : prefix + ':' + localPart);
    }

    /**
     * Returns a string representation of this QName
     *
     * @return  a string representation of the QName
     */
    public String toString() {

        return ((namespaceURI == emptyString)
                ? localPart
                : '{' + namespaceURI + '}' + localPart);
    }

    /**
     * Tests this QName for equality with another object.
     * <p>
     * If the given object is not a QName or is null then this method
     * returns <tt>false</tt>.
     * <p>
     * For two QNames to be considered equal requires that both
     * localPart and namespaceURI must be equal. This method uses
     * <code>String.equals</code> to check equality of localPart
     * and namespaceURI. Any class that extends QName is required
     * to satisfy this equality contract.
     * <p>
     * This method satisfies the general contract of the <code>Object.equals</code> method.
     *
     * @param obj the reference object with which to compare
     *
     * @return <code>true</code> if the given object is identical to this
     *      QName: <code>false</code> otherwise.
     */
    public final boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof QName)) {
            return false;
        }

        if ((namespaceURI == ((QName) obj).namespaceURI)
                && (localPart == ((QName) obj).localPart)) {
            return true;
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

        if ((s == null) || s.equals("")) {
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
    public final int hashCode() {
        return namespaceURI.hashCode() ^ localPart.hashCode();
    }

    /**
     * Ensure that deserialization properly interns the results.
     * @param in the ObjectInputStream to be read
     */
    private void readObject(ObjectInputStream in) throws
            IOException, ClassNotFoundException {
        in.defaultReadObject();

        namespaceURI = namespaceURI.intern();
        localPart = localPart.intern();
        prefix = prefix.intern();
    }
} 