/*
 * StructuredSyntaxHandler.java
 *
 * Copyright (c) 2004, 2007 Evan A Slatis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.ui.text;

import java.awt.Font;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author  hippy
 */
public class StructuredSyntaxHandler extends DefaultHandler {
    
    //StyleConstants.
    public static final String REGEXP = "regexp";
    public static final String STYLE = "style";
    
    public static final String ALIGN_CENTER = "ALIGN_CENTER";
    public static final String ALIGN_JUSTIFIED = "ALIGN_JUSTIFIED";
    public static final String ALIGN_LEFT = "ALIGN_LEFT";
    public static final String ALIGN_RIGHT = "ALIGN_RIGHT";

    public static final String alignment = "alignment";
    public static final String background = "background";
    public static final String bidiLevel = "bidiLevel";
    public static final String bold = "bold";
    public static final String componentAttribute = "componentAttribute";
    public static final String componentElementName = "componentElementName";
    public static final String composedTextAttribute = "composedTextAttribute";
    public static final String firstLineIndent = "firstLineIndent";
    public static final String fontFamily = "fontFamily";
    public static final String fontSize = "fontSize";
    public static final String foreground = "foreground";
    public static final String iconAttribute = "iconAttribute";
    public static final String iconElementName = "iconElementName";
    public static final String italic = "italic";
    public static final String leftIndent = "leftIndent";
    public static final String lineSpacing = "lineSpacing";
    public static final String modelAttribute = "modelAttribute";
    public static final String nameAttribute = "nameAttribute";
    public static final String orientation = "orientation";
    public static final String resolveAttribute = "resolveAttribute";
    public static final String rightIndent = "rightIndent";
    public static final String spaceAbove = "spaceAbove";
    public static final String spaceBelow = "spaceBelow";
    public static final String strikeThrough = "strikeThrough";
    public static final String subscript = "subscript";
    public static final String superscript = "superscript";
    public static final String tabSet = "tabSet";
    public static final String underline = "underline";
    
    private StructuredSyntaxDocumentFilter.LexerNode currentNode;
    private StructuredSyntaxDocumentFilter.LexerNode parentNode;
    
    private final StructuredSyntaxDocumentFilter filter;
    
    private Font font;

    /**
     * Creates a new instance of MasterFrameHandler
     */
    public StructuredSyntaxHandler(StructuredSyntaxDocumentFilter filter) {
        this.filter = filter;
    }
    
    /**
     * @param ch
     * @param start
     * @param length
     */    
    public void characters(char[] ch, int start, int length) {
    }
    
    /**
     * @throws SAXException
     */    
    public void endDocument() throws SAXException {
        super.endDocument();
    }
    
    /**
     * @param uri
     * @param localName
     * @param qName
     * @throws SAXException
     */    
    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
    }
    
    /**
     * @param e
     * @throws SAXException
     */    
    public void	error(SAXParseException e) throws SAXException {
        throw new SAXException("Line: " + e.getLineNumber() + " message: " + e.getMessage());
    }
    
    /**
     * @throws SAXException
     */    
    public void startDocument() throws SAXException {
        super.startDocument();
        currentNode = filter.getRootNode();
    }
    
    /**
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     */    
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
    }
}
