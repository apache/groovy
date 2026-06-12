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
package groovy.console.ui.text;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX handler that reads structured syntax definitions into a document filter.
 */
public class StructuredSyntaxHandler extends DefaultHandler {

    //StyleConstants.
    /**
     * XML element name for regular expression entries.
     */
    public static final String REGEXP = "regexp";
    /**
     * XML element name for style entries.
     */
    public static final String STYLE = "style";

    /**
     * Alignment value name for centered text.
     */
    public static final String ALIGN_CENTER = "ALIGN_CENTER";
    /**
     * Alignment value name for justified text.
     */
    public static final String ALIGN_JUSTIFIED = "ALIGN_JUSTIFIED";
    /**
     * Alignment value name for left-aligned text.
     */
    public static final String ALIGN_LEFT = "ALIGN_LEFT";
    /**
     * Alignment value name for right-aligned text.
     */
    public static final String ALIGN_RIGHT = "ALIGN_RIGHT";

    /**
     * Style attribute name for alignment.
     */
    public static final String ALIGNMENT = "alignment";
    /**
     * Style attribute name for background color.
     */
    public static final String BACKGROUND = "background";
    /**
     * Style attribute name for bidirectional text level.
     */
    public static final String BIDI_LEVEL = "bidiLevel";
    /**
     * Style attribute name for bold text.
     */
    public static final String BOLD = "bold";
    /**
     * Style attribute name for embedded components.
     */
    public static final String COMPONENT_ATTRIBUTE = "componentAttribute";
    /**
     * Style attribute name for component element names.
     */
    public static final String COMPONENT_ELEMENT_NAME = "componentElementName";
    /**
     * Style attribute name for composed text.
     */
    public static final String COMPOSED_TEXT_ATTRIBUTE = "composedTextAttribute";
    /**
     * Style attribute name for first-line indentation.
     */
    public static final String FIRST_LINE_INDENT = "firstLineIndent";
    /**
     * Style attribute name for font family.
     */
    public static final String FONT_FAMILY = "fontFamily";
    /**
     * Style attribute name for font size.
     */
    public static final String FONT_SIZE = "fontSize";
    /**
     * Style attribute name for foreground color.
     */
    public static final String FOREGROUND = "foreground";
    /**
     * Style attribute name for embedded icons.
     */
    public static final String ICON_ATTRIBUTE = "iconAttribute";
    /**
     * Style attribute name for icon element names.
     */
    public static final String ICON_ELEMENT_NAME = "iconElementName";
    /**
     * Style attribute name for italic text.
     */
    public static final String ITALIC = "italic";
    /**
     * Style attribute name for left indentation.
     */
    public static final String LEFT_INDENT = "leftIndent";
    /**
     * Style attribute name for line spacing.
     */
    public static final String LINE_SPACING = "lineSpacing";
    /**
     * Style attribute name for model attributes.
     */
    public static final String MODEL_ATTRIBUTE = "modelAttribute";
    /**
     * Style attribute name for named attributes.
     */
    public static final String NAME_ATTRIBUTE = "nameAttribute";
    /**
     * Style attribute name for orientation.
     */
    public static final String ORIENTATION = "orientation";
    /**
     * Style attribute name for resolve attributes.
     */
    public static final String RESOLVE_ATTRIBUTE = "resolveAttribute";
    /**
     * Style attribute name for right indentation.
     */
    public static final String RIGHT_INDENT = "rightIndent";
    /**
     * Style attribute name for space above paragraphs.
     */
    public static final String SPACE_ABOVE = "spaceAbove";
    /**
     * Style attribute name for space below paragraphs.
     */
    public static final String SPACE_BELOW = "spaceBelow";
    /**
     * Style attribute name for strike-through text.
     */
    public static final String STRIKE_THROUGH = "strikeThrough";
    /**
     * Style attribute name for subscript text.
     */
    public static final String SUBSCRIPT = "subscript";
    /**
     * Style attribute name for superscript text.
     */
    public static final String SUPERSCRIPT = "superscript";
    /**
     * Style attribute name for tab sets.
     */
    public static final String TAB_SET = "tabSet";
    /**
     * Style attribute name for underlined text.
     */
    public static final String UNDERLINE = "underline";

    private StructuredSyntaxDocumentFilter.LexerNode currentNode;

    private final StructuredSyntaxDocumentFilter filter;

    /**
     * Creates a handler for the supplied syntax filter.
     *
     * @param filter the filter receiving parsed syntax definitions
     */
    public StructuredSyntaxHandler(StructuredSyntaxDocumentFilter filter) {
        this.filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
        throw new SAXException("Line: " + e.getLineNumber() + " message: " + e.getMessage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        currentNode = filter.getRootNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
    }
}
