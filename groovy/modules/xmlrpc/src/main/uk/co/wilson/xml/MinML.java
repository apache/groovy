// Copyright (c) 2000, 2001 The Wilson Partnership.
// All Rights Reserved.
// @(#)MinML.java, 1.4, 26th October 2001
// Author: John Wilson - tug@wilson.co.uk

package uk.co.wilson.xml;

/*
Copyright (c) 2000, 2001 John Wilson (tug@wilson.co.uk).
All rights reserved.
Redistribution and use in source and binary forms,
with or without modification, are permitted provided
that the following conditions are met:

Redistributions of source code must retain the above
copyright notice, this list of conditions and the
following disclaimer.

Redistributions in binary form must reproduce the
above copyright notice, this list of conditions and
the following disclaimer in the documentation and/or
other materials provided with the distribution.

All advertising materials mentioning features or use
of this software must display the following acknowledgement:

This product includes software developed by John Wilson.
The name of John Wilson may not be used to endorse or promote
products derived from this software without specific prior
written permission.

THIS SOFTWARE IS PROVIDED BY JOHN WILSON ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JOHN WILSON
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE
*/

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.AttributeList;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.org.xml.sax.DocumentHandler;
import uk.org.xml.sax.Parser;

public class MinML implements Parser, Locator, DocumentHandler, ErrorHandler {
  public static final int endStartName = 0;
  public static final int emitStartElement = 1;
  public static final int emitEndElement = 2;
  public static final int emitCharacters = 3;
  public static final int saveAttributeName = 4;
  public static final int saveAttributeValue = 5;
  public static final int startComment = 6;
  public static final int endComment = 7;
  public static final int incLevel = 8;
  public static final int decLevel = 9;
  public static final int startCDATA = 10;
  public static final int endCDATA = 11;
  public static final int processCharRef = 12;
  public static final int writeCdata = 13;
  public static final int exitParser = 14;
  public static final int parseError = 15;
  public static final int discardAndChange = 16;
  public static final int discardSaveAndChange = 17;
  public static final int saveAndChange = 18;
  public static final int change = 19;

  public static final int inSkipping = 0;
  public static final int inSTag = 1;
  public static final int inPossiblyAttribute = 2;
  public static final int inNextAttribute = 3;
  public static final int inAttribute = 4;
  public static final int inAttribute1 = 5;
  public static final int inAttributeValue = 6;
  public static final int inAttributeQuoteValue = 7;
  public static final int inAttributeQuotesValue = 8;
  public static final int inETag = 9;
  public static final int inETag1 = 10;
  public static final int inMTTag = 11;
  public static final int inTag = 12;
  public static final int inPI = 13;
  public static final int inPI1 = 14;
  public static final int inPossiblySkipping = 15;
  public static final int inCharData = 16;
  public static final int inCDATA = 17;
  public static final int inCDATA1 = 18;
  public static final int inComment =19;
  public static final int inDTD = 20;

  public MinML(final int initialBufferSize, final int bufferIncrement) {
    this.initialBufferSize = initialBufferSize;
    this.bufferIncrement = bufferIncrement;
  }

  public MinML() {
    this(256, 128);
  }

  public void parse(final Reader in) throws SAXException, IOException {
  final Vector attributeNames = new Vector();
  final Vector attributeValues = new Vector();

  final AttributeList attrs = new AttributeList() {
    public int getLength() {
      return attributeNames.size();
    }

    public String getName(final int i) {
      return (String)attributeNames.elementAt(i);
    }

    public String getType(final int i) {
      return "CDATA";
    }

    public String getValue(final int i) {
      return (String)attributeValues.elementAt(i);
    }

    public String getType(final String name) {
      return "CDATA";
    }

    public String getValue(final String name) {
    final int index = attributeNames.indexOf(name);

      return (index == -1) ? null : (String)attributeValues.elementAt(index);
    }
  };

  final MinMLBuffer buffer = new MinMLBuffer(in);
  int currentChar = 0, charCount = 0;
  int level = 0;
  String elementName = null;
  String state = operands[inSkipping];

    this.lineNumber = 1;
    this.columnNumber = 0;

    try {
main: while(true) {
        charCount++;

        //
        // this is to try and make the loop a bit faster
        // currentChar = buffer.read(); is simpler but is a bit slower.
        //
        currentChar = (buffer.nextIn == buffer.lastIn) ? buffer.read() : buffer.chars[buffer.nextIn++];

        final int transition;

        if (currentChar > ']') {
          transition = state.charAt(14);
        } else {
        final int charClass = charClasses[currentChar + 1];

          if (charClass == -1) fatalError("Document contains illegal control character with value " + currentChar, this.lineNumber, this.columnNumber);

          if (charClass == 12) {
            if (currentChar == '\r') {
              currentChar = '\n';
              charCount = -1;
            }

            if (currentChar == '\n') {
              if (charCount == 0) continue;  // preceeded by '\r' so ignore

              if (charCount != -1) charCount = 0;

              this.lineNumber++;
              this.columnNumber = 0;
            }
          }

          transition = state.charAt(charClass);
       }

        this.columnNumber++;

        final String operand = operands[transition >>> 8];

        switch (transition & 0XFF) {
          case endStartName:
          // end of start element name
            elementName = buffer.getString();
            if (currentChar != '>' && currentChar != '/') break;  // change state to operand
            // drop through to emit start element (we have no attributes)

          case emitStartElement:
          // emit start element

          final Writer newWriter = this.extDocumentHandler.startElement(elementName, attrs,
                                                                   (this.tags.empty()) ?
                                                                     this.extDocumentHandler.startDocument(buffer)
                                                                   :
                                                                     buffer.getWriter());

            buffer.pushWriter(newWriter);
            this.tags.push(elementName);

            attributeValues.removeAllElements();
            attributeNames.removeAllElements();

            if (currentChar != '/') break;  // change state to operand

            // <element/> drop through

          case emitEndElement:
          // emit end element

            if (this.tags.empty())
              fatalError("end tag at begining of document", this.lineNumber, this.columnNumber);
              
            final String begin = (String)this.tags.pop();
            buffer.popWriter();
            elementName = buffer.getString();

            if (currentChar != '/' && !elementName.equals(begin)) {
              fatalError("end tag </" + elementName + "> does not match begin tag <" + begin + ">",
                         this.lineNumber, this.columnNumber);
            } else {
            	this.documentHandler.endElement(begin);

              if (this.tags.empty()) {
              	this.documentHandler.endDocument();
                return;
              }
            }
            break;  // change state to operand


          case emitCharacters:
          // emit characters

            buffer.flush();
            break;  // change state to operand

          case saveAttributeName:
          // save attribute name

            attributeNames.addElement(buffer.getString());
            break;  // change state to operand

          case saveAttributeValue:
          // save attribute value

            attributeValues.addElement(buffer.getString());
            break;  // change state to operand

          case startComment:
          // change state if we have found "<!--"

            if (buffer.read() != '-') continue; // not "<!--"

            break;  // change state to operand

          case endComment:
          // change state if we find "-->"

            if ((currentChar = buffer.read()) == '-') {
              // deal with the case where we might have "------->"
              while ((currentChar = buffer.read()) == '-');

              if (currentChar == '>') break;  // end of comment, change state to operand
            }

            continue;   // not end of comment, don't change state

          case incLevel:

            level++;

            break;

          case decLevel:

            if (level == 0) break; // outer level <> change state

            level--;

            continue; // in nested <>, don't change state

          case startCDATA:
          // change state if we have found "<![CDATA["

            if (buffer.read() != 'C') continue;   // don't change state
            if (buffer.read() != 'D') continue;   // don't change state
            if (buffer.read() != 'A') continue;   // don't change state
            if (buffer.read() != 'T') continue;   // don't change state
            if (buffer.read() != 'A') continue;   // don't change state
            if (buffer.read() != '[') continue;   // don't change state
            break;  // change state to operand

          case endCDATA:
          // change state if we find "]]>"

            if ((currentChar = buffer.read()) == ']') {
              // deal with the case where we might have "]]]]]]]>"
              while ((currentChar = buffer.read()) == ']') buffer.write(']');

              if (currentChar == '>') break;  // end of CDATA section, change state to operand

              buffer.write(']');
            }

            buffer.write(']');
            buffer.write(currentChar);
            continue;   // not end of CDATA section, don't change state

          case processCharRef:
          // process character entity

            int crefState = 0;

            currentChar = buffer.read();

            while (true) {
              if ("#amp;&pos;'quot;\"gt;>lt;<".charAt(crefState) == currentChar) {
                crefState++;

                if (currentChar == ';') {
                  buffer.write("#amp;&pos;'quot;\"gt;>lt;<".charAt(crefState));
                  continue main;

                } else if (currentChar == '#') {
                final int radix;

                  currentChar = buffer.read();

                  if (currentChar == 'x') {
                    radix = 16;
                    currentChar = buffer.read();
                  } else {
                    radix = 10;
                  }

                  int charRef = Character.digit((char)currentChar, radix);

                  while (true) {
                    currentChar = buffer.read();

                    final int digit = Character.digit((char)currentChar, radix);

                    if (digit == -1) break;

                    charRef = (char)((charRef * radix) + digit);
                  }

                  if (currentChar == ';' && charRef != -1) {
                    buffer.write(charRef);
                    continue main;
                  }

                  break;  // bad char reference
                } else {
                  currentChar = buffer.read();
                }
              } else {
                crefState = ("\u0001\u000b\u0006\u00ff\u00ff\u00ff\u00ff\u00ff\u00ff\u00ff\u00ff" +
//                               #     a     m     p     ;     &     p     o     s     ;     '
//                               0     1     2     3     4     5     6     7     8     9     a
                             "\u0011\u00ff\u00ff\u00ff\u00ff\u00ff\u0015\u00ff\u00ff\u00ff" +
//                               q     u     o     t     ;     "     g     t     ;     >
//                               b     b     d     e     f     10    11    12    13    14
                             "\u00ff\u00ff\u00ff").charAt(crefState);
//                               l     t     ;
//                               15    16    17

                if (crefState == 255) break;  // bad char reference
              }
            }
           // drop through to report error and exit

          case parseError:
          // report fatal error

            fatalError(operand, this.lineNumber, this.columnNumber);
            // drop through to exit parser

          case exitParser:
          // exit parser

            return;

          case writeCdata:
          // write character data
          // this will also write any skipped whitespace

            buffer.write(currentChar);
            break;  // change state to operand

          case discardAndChange:
          // throw saved characters away and change state

            buffer.reset();
            break;  // change state to operand

          case discardSaveAndChange:
          // throw saved characters away, save character and change state

            buffer.reset();
            // drop through to save character and change state

          case saveAndChange:
          // save character and change state

            buffer.saveChar((char)currentChar);
            break;  // change state to operand

          case change:
          // change state to operand

            break;  // change state to operand
        }

        state = operand;
      }
    }
    catch (final IOException e) {
      this.errorHandler.fatalError(new SAXParseException(e.toString(), null, null, this.lineNumber, this.columnNumber, e));
    }
    finally {
      this.errorHandler = this;
      this.documentHandler = this.extDocumentHandler = this;
      this.tags.removeAllElements();
    }
  }

  public void parse(final InputSource source) throws SAXException, IOException {
    if (source.getCharacterStream() != null)
      parse(source.getCharacterStream());
    else if (source.getByteStream() != null)
      parse(new InputStreamReader(source.getByteStream()));
    else
     parse(new InputStreamReader(new URL(source.getSystemId()).openStream()));
  }

  public void parse(final String systemId) throws SAXException, IOException {
    parse(new InputSource(systemId));
  }

  public void setLocale(final Locale locale) throws SAXException {
    throw new SAXException("Not supported");
  }

  public void setEntityResolver(final EntityResolver resolver) {
    // not supported
  }

  public void setDTDHandler(final DTDHandler handler) {
    // not supported
  }

  public void setDocumentHandler(final org.xml.sax.DocumentHandler handler) {
   this.documentHandler = (handler == null) ? this : handler;
   this.extDocumentHandler = this;
  }

  public void setDocumentHandler(final DocumentHandler handler) {
   this.documentHandler = this.extDocumentHandler = (handler == null) ? this : handler;
   this.documentHandler.setDocumentLocator(this);
  }

  public void setErrorHandler(final ErrorHandler handler) {
   this.errorHandler = (handler == null) ? this : handler;
  }

  public void setDocumentLocator(final Locator locator) {
  }

  public void startDocument() throws SAXException {
  }

  public Writer startDocument(final Writer writer) throws SAXException {
    this.documentHandler.startDocument();
    return writer;
  }

  public void endDocument() throws SAXException {
  }

  public void startElement(final String name, final AttributeList attributes) throws SAXException {
  }

  public Writer startElement(final String name, final AttributeList attributes, final Writer writer)
        throws SAXException
  {
    this.documentHandler.startElement(name, attributes);
    return writer;
  }

  public void endElement(final String name) throws SAXException {
  }

  public void characters(final char ch[], final int start, final int length) throws SAXException {
  }

  public void ignorableWhitespace(final char ch[], final int start, final int length) throws SAXException {
  }

  public void processingInstruction(final String target, final String data) throws SAXException {
  }

  public void warning(final SAXParseException e) throws SAXException {
  }

  public void error(final SAXParseException e) throws SAXException {
  }

  public void fatalError(final SAXParseException e) throws SAXException {
    throw e;
  }

  public String getPublicId() {
    return "";
  }


  public String getSystemId() {
    return "";
  }

  public int getLineNumber () {
    return this.lineNumber;
  }

  public int getColumnNumber () {
    return this.columnNumber;
  }

  private void fatalError(final String msg, final int lineNumber, final int columnNumber) throws SAXException {
  	final SAXParseException e = new SAXParseException(msg, null, null, lineNumber, columnNumber);
  	
    this.errorHandler.fatalError(e);
    
    throw e;
  }

  private class MinMLBuffer extends Writer {
    public MinMLBuffer(final Reader in) {
      this.in = in;
    }

    public void close() throws IOException {
      flush();
    }

    public void flush() throws IOException {
      try {
        _flush();
        if (this.writer != this) this.writer.flush();
      }
      finally {
        this.flushed = true;
      }
    }

    public void write(final int c) throws IOException {
      this.written = true;
      this.chars[this.count++] = (char)c;
    }

    public void write(final char[] cbuf, final int off, final int len) throws IOException {
      this.written = true;
      System.arraycopy(cbuf, off, this.chars, this.count, len);
      this.count += len;
    }

    public void saveChar(final char c) {
      this.written = false;
      this.chars[this.count++] = c;
    }

    public void pushWriter(final Writer writer) {
      MinML.this.tags.push(this.writer);

      this.writer = (writer == null) ? this : writer;

      this.flushed = this.written = false;
    }

    public Writer getWriter() {
      return this.writer;
    }

    public void popWriter() throws IOException {
      try {
        if (!this.flushed && this.writer != this) this.writer.flush();
      }
      finally {
        this.writer = (Writer)MinML.this.tags.pop();
        this.flushed = this.written = false;
      }
    }

    public String getString() {
    final String result = new String(this.chars, 0, this.count);

      this.count = 0;
      return result;
    }

    public void reset() {
      this.count = 0;
    }

    public int read() throws IOException {
      if (this.nextIn == this.lastIn) {
        if (this.count != 0) {
          if (this.written) {
            _flush();
          } else if (this.count >= (this.chars.length - MinML.this.bufferIncrement)) {
          final char[] newChars = new char[this.chars.length + MinML.this.bufferIncrement];

            System.arraycopy(this.chars, 0, newChars, 0, this.count);
            this.chars = newChars;
          }
        }

        final int numRead = this.in.read(this.chars, this.count, this.chars.length - this.count);

        if (numRead == -1) return -1;

        this.nextIn = this.count;
        this.lastIn = this.count + numRead;
      }

      return this.chars[this.nextIn++];
    }

    private void _flush() throws IOException {
      if (this.count != 0) {
        try {
          if (this.writer == this) {
            try {
              MinML.this.documentHandler.characters(this.chars, 0, this.count);
            }
            catch (final SAXException e) {
              throw new IOException(e.toString());
            }
          } else {
            this.writer.write(this.chars, 0, this.count);
          }
        }
        finally {
          this.count = 0;
        }
      }
    }

    private int nextIn = 0, lastIn = 0;
    private char[] chars = new char[MinML.this.initialBufferSize];
    private final Reader in;
    private int count = 0;
    private Writer writer = this;
    private boolean flushed = false;
    private boolean written = false;
  }

  private DocumentHandler extDocumentHandler = this;
  private org.xml.sax.DocumentHandler documentHandler = this;
  private ErrorHandler errorHandler = this;
  private final Stack tags = new Stack();
  private int lineNumber = 1;
  private int columnNumber = 0;
  private final int initialBufferSize;
  private final int bufferIncrement;

  private static final byte[] charClasses = {
  //  EOF
      13,
  //                                      \t  \n          \r
      -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, 12, -1, -1, 12, -1, -1,
  //
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
  //  SP   !   "   #   $   %   &   '   (   )   *   +   ,   -   .   /
      12,  8,  7, 14, 14, 14,  3,  6, 14, 14, 14, 14, 14, 11, 14,  2,
  //   0   1   2   3   4   5   6   7   8   9   :   ;   <   =   >   ?
      14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,  0,  5,  1,  4,
  //
      14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
  //                                               [   \   ]
      14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,  9, 14, 10
  };

  private static final String[] operands = {
    "\u0c13\u150f\u150f\u150f\u150f\u150f\u150f\u150f\u150f\u150f\u150f\u150f\u0013\u000e\u150f",
    "\u160f\u0f00\u0b00\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u0112\u0200\u170f\u0112",
    "\u160f\u0f01\u0b01\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u0213\u170f\u0412",
    "\u160f\u0f01\u0b01\u160f\u180f\u180f\u180f\u180f\u180f\u180f\u180f\u180f\u0313\u170f\u0412",
    "\u180f\u180f\u180f\u180f\u180f\u0604\u180f\u180f\u180f\u180f\u180f\u0412\u0513\u170f\u0412",
    "\u180f\u180f\u180f\u180f\u180f\u0604\u180f\u180f\u180f\u180f\u180f\u180f\u0513\u170f\u180f",
    "\u190f\u190f\u190f\u190f\u190f\u190f\u0713\u0813\u190f\u190f\u190f\u190f\u0613\u170f\u190f",
    "\u0712\u0712\u0712\u1a0c\u0712\u0712\u0305\u0712\u0712\u0712\u0712\u0712\u0712\u170f\u0712",
    "\u0812\u0812\u0812\u1a0c\u0812\u0812\u0812\u0305\u0812\u0812\u0812\u0812\u0812\u170f\u0812",
    "\u160f\u0002\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u0912\u0913\u170f\u0912",
    "\u1b0f\u1b0f\u0903\u1b0f\u1b0f\u1b0f\u1b0f\u1b0f\u1113\u1b0f\u1b0f\u1b0f\u1b0f\u170f\u1b0f",
    "\u160f\u0013\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u160f\u170f\u160f",
    "\u160f\u1c0f\u0913\u160f\u0d13\u160f\u160f\u160f\u1113\u160f\u160f\u160f\u160f\u170f\u0111",
    "\u0d13\u0d13\u0d13\u0d13\u0e13\u0d13\u0d13\u0d13\u0d13\u0d13\u0d13\u0d13\u0d13\u170f\u0d13",
    "\u0d13\u0013\u0d13\u0d13\u0e13\u0d13\u0d13\u0d13\u0d13\u0d13\u0d13\u0d13\u0d13\u170f\u0d13",
    "\u0c10\u100d\u100d\u1a0c\u100d\u100d\u100d\u100d\u100d\u100d\u100d\u100d\u0f12\u170f\u100d",
    "\u0a13\u100d\u100d\u1a0c\u100d\u100d\u100d\u100d\u100d\u100d\u100d\u100d\u100d\u170f\u100d",
    "\u1d0f\u1d0f\u1d0f\u1d0f\u1d0f\u1d0f\u1d0f\u1d0f\u1d0f\u120a\u1d0f\u1306\u1d0f\u170f\u1413",
    "\u120d\u120d\u120d\u120d\u120d\u120d\u120d\u120d\u120d\u120d\u100b\u120d\u120d\u170f\u120d",
    "\u1313\u1313\u1313\u1313\u1313\u1313\u1313\u1313\u1313\u1313\u1313\u0007\u1313\u170f\u1313",
    "\u1408\u0009\u1413\u1413\u1413\u1413\u1413\u1413\u1413\u1413\u1413\u1413\u1413\u170f\u1413",
    "expected Element",
    "unexpected character in tag",
    "unexpected end of file found",
    "attribute name not followed by '='",
    "invalid attribute value",
    "invalid Character Entity",
    "expecting end tag",
    "empty tag",
    "unexpected character after <!"
  };
}
