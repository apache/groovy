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
package groovy.xml

/**
 * Represents a named or numeric XML entity reference that can be written unescaped by Groovy XML builders.
 * <p>
 * The predefined constants cover the standard XHTML-compatible entity names for markup generation.
 */
class Entity implements Buildable {
    /*
     * XML-compatible ISO Latin 1 Character Entity Set for XHTML
     */
    /** Entity reference {@code &nbsp;}. */
    public static final Entity nbsp = new Entity('nbsp')        // no-break space = non-breaking space, U+00A0 ISOnum
    /** Entity reference {@code &iexcl;}. */
    public static final Entity iexcl = new Entity('iexcl')      // inverted exclamation mark, U+00A1 ISOnum
    /** Entity reference {@code &cent;}. */
    public static final Entity cent = new Entity('cent')        // cent sign, U+00A2 ISOnum
    /** Entity reference {@code &pound;}. */
    public static final Entity pound = new Entity('pound')      // pound sign, U+00A3 ISOnum
    /** Entity reference {@code &curren;}. */
    public static final Entity curren = new Entity('curren')    // currency sign, U+00A4 ISOnum
    /** Entity reference {@code &yen;}. */
    public static final Entity yen = new Entity('yen')          // yen sign = yuan sign, U+00A5 ISOnum
    /** Entity reference {@code &brvbar;}. */
    public static final Entity brvbar = new Entity('brvbar')    // broken bar = broken vertical bar, U+00A6 ISOnum
    /** Entity reference {@code &sect;}. */
    public static final Entity sect = new Entity('sect')        // section sign, U+00A7 ISOnum
    /** Entity reference {@code &uml;}. */
    public static final Entity uml = new Entity('uml')          // diaeresis = spacing diaeresis, U+00A8 ISOdia
    /** Entity reference {@code &copy;}. */
    public static final Entity copy = new Entity('copy')        // copyright sign, U+00A9 ISOnum
    /** Entity reference {@code &ordf;}. */
    public static final Entity ordf = new Entity('ordf')        // feminine ordinal indicator, U+00AA ISOnum
    /** Entity reference {@code &laquo;}. */
    public static final Entity laquo = new Entity('laquo')      // left-pointing double angle quotation mark = left pointing guillemet, U+00AB ISOnum
    /** Entity reference {@code &not;}. */
    public static final Entity not = new Entity('not')          // not sign, U+00AC ISOnum
    /** Entity reference {@code &shy;}. */
    public static final Entity shy = new Entity('shy')          // soft hyphen = discretionary hyphen, U+00AD ISOnum
    /** Entity reference {@code &reg;}. */
    public static final Entity reg = new Entity('reg')          // registered sign = registered trade mark sign, U+00AE ISOnum
    /** Entity reference {@code &macr;}. */
    public static final Entity macr = new Entity('macr')        // macron = spacing macron = overline = APL overbar, U+00AF ISOdia
    /** Entity reference {@code &deg;}. */
    public static final Entity deg = new Entity('deg')          // degree sign, U+00B0 ISOnum
    /** Entity reference {@code &plusmn;}. */
    public static final Entity plusmn = new Entity('plusmn')    // plus-minus sign = plus-or-minus sign, U+00B1 ISOnum
    /** Entity reference {@code &sup2;}. */
    public static final Entity sup2 = new Entity('sup2')        // superscript two = superscript digit two = squared, U+00B2 ISOnum
    /** Entity reference {@code &sup3;}. */
    public static final Entity sup3 = new Entity('sup3')        // superscript three = superscript digit three = cubed, U+00B3 ISOnum
    /** Entity reference {@code &acute;}. */
    public static final Entity acute = new Entity('acute')      // acute accent = spacing acute, U+00B4 ISOdia
    /** Entity reference {@code &micro;}. */
    public static final Entity micro = new Entity('micro')      // micro sign, U+00B5 ISOnum
    /** Entity reference {@code &para;}. */
    public static final Entity para = new Entity('para')        // pilcrow sign = paragraph sign, U+00B6 ISOnum
    /** Entity reference {@code &middot;}. */
    public static final Entity middot = new Entity('middot')    // middle dot = Georgian comma = Greek middle dot, U+00B7 ISOnum
    /** Entity reference {@code &cedil;}. */
    public static final Entity cedil = new Entity('cedil')      // cedilla = spacing cedilla, U+00B8 ISOdia
    /** Entity reference {@code &sup1;}. */
    public static final Entity sup1 = new Entity('sup1')        // superscript one = superscript digit one, U+00B9 ISOnum
    /** Entity reference {@code &ordm;}. */
    public static final Entity ordm = new Entity('ordm')        // masculine ordinal indicator, U+00BA ISOnum
    /** Entity reference {@code &raquo;}. */
    public static final Entity raquo = new Entity('raquo')      // right-pointing double angle quotation mark = right pointing guillemet, U+00BB ISOnum
    /** Entity reference {@code &frac14;}. */
    public static final Entity frac14 = new Entity('frac14')    // vulgar fraction one quarter = fraction one quarter, U+00BC ISOnum
    /** Entity reference {@code &frac12;}. */
    public static final Entity frac12 = new Entity('frac12')    // vulgar fraction one half = fraction one half, U+00BD ISOnum
    /** Entity reference {@code &frac34;}. */
    public static final Entity frac34 = new Entity('frac34')    // vulgar fraction three quarters = fraction three quarters, U+00BE ISOnum
    /** Entity reference {@code &iquest;}. */
    public static final Entity iquest = new Entity('iquest')    // inverted question mark = turned question mark, U+00BF ISOnum
    /** Entity reference {@code &Agrave;}. */
    public static final Entity Agrave = new Entity('Agrave')    // latin capital A with grave = latin capital A grave, U+00C0 ISOlat1
    /** Entity reference {@code &Aacute;}. */
    public static final Entity Aacute = new Entity('Aacute')    // latin capital A with acute, U+00C1 ISOlat1
    /** Entity reference {@code &Acirc;}. */
    public static final Entity Acirc = new Entity('Acirc')      // latin capital A with circumflex, U+00C2 ISOlat1
    /** Entity reference {@code &Atilde;}. */
    public static final Entity Atilde = new Entity('Atilde')    // latin capital A with tilde, U+00C3 ISOlat1
    /** Entity reference {@code &Auml;}. */
    public static final Entity Auml = new Entity('Auml')        // latin capital A with diaeresis, U+00C4 ISOlat1
    /** Entity reference {@code &Aring;}. */
    public static final Entity Aring = new Entity('Aring')      // latin capital A with ring above = latin capital A ring, U+00C5 ISOlat1
    /** Entity reference {@code &AElig;}. */
    public static final Entity AElig = new Entity('AElig')      // latin capital AE = latin capital ligature AE, U+00C6 ISOlat1
    /** Entity reference {@code &Ccedil;}. */
    public static final Entity Ccedil = new Entity('Ccedil')    // latin capital C with cedilla, U+00C7 ISOlat1
    /** Entity reference {@code &Egrave;}. */
    public static final Entity Egrave = new Entity('Egrave')    // latin capital E with grave, U+00C8 ISOlat1
    /** Entity reference {@code &Eacute;}. */
    public static final Entity Eacute = new Entity('Eacute')    // latin capital E with acute, U+00C9 ISOlat1
    /** Entity reference {@code &Ecirc;}. */
    public static final Entity Ecirc = new Entity('Ecirc')      // latin capital E with circumflex, U+00CA ISOlat1
    /** Entity reference {@code &Euml;}. */
    public static final Entity Euml = new Entity('Euml')        // latin capital E with diaeresis, U+00CB ISOlat1
    /** Entity reference {@code &Igrave;}. */
    public static final Entity Igrave = new Entity('Igrave')    // latin capital I with grave, U+00CC ISOlat1
    /** Entity reference {@code &Iacute;}. */
    public static final Entity Iacute = new Entity('Iacute')    // latin capital I with acute, U+00CD ISOlat1
    /** Entity reference {@code &Icirc;}. */
    public static final Entity Icirc = new Entity('Icirc')      // latin capital I with circumflex, U+00CE ISOlat1
    /** Entity reference {@code &Iuml;}. */
    public static final Entity Iuml = new Entity('Iuml')        // latin capital I with diaeresis, U+00CF ISOlat1
    /** Entity reference {@code &ETH;}. */
    public static final Entity ETH = new Entity('ETH')          // latin capital ETH, U+00D0 ISOlat1
    /** Entity reference {@code &Ntilde;}. */
    public static final Entity Ntilde = new Entity('Ntilde')    // latin capital N with tilde, U+00D1 ISOlat1
    /** Entity reference {@code &Ograve;}. */
    public static final Entity Ograve = new Entity('Ograve')    // latin capital O with grave, U+00D2 ISOlat1
    /** Entity reference {@code &Oacute;}. */
    public static final Entity Oacute = new Entity('Oacute')    // latin capital O with acute, U+00D3 ISOlat1
    /** Entity reference {@code &Ocirc;}. */
    public static final Entity Ocirc = new Entity('Ocirc')      // latin capital O with circumflex, U+00D4 ISOlat1
    /** Entity reference {@code &Otilde;}. */
    public static final Entity Otilde = new Entity('Otilde')    // latin capital O with tilde, U+00D5 ISOlat1
    /** Entity reference {@code &Ouml;}. */
    public static final Entity Ouml = new Entity('Ouml')        // latin capital O with diaeresis, U+00D6 ISOlat1
    /** Entity reference {@code &times;}. */
    public static final Entity times = new Entity('times')      // multiplication sign, U+00D7 ISOnum
    /** Entity reference {@code &Oslash;}. */
    public static final Entity Oslash = new Entity('Oslash')    // latin capital O with stroke = latin capital O slash, U+00D8 ISOlat1
    /** Entity reference {@code &Ugrave;}. */
    public static final Entity Ugrave = new Entity('Ugrave')    // latin capital U with grave, U+00D9 ISOlat1
    /** Entity reference {@code &Uacute;}. */
    public static final Entity Uacute = new Entity('Uacute')    // latin capital U with acute, U+00DA ISOlat1
    /** Entity reference {@code &Ucirc;}. */
    public static final Entity Ucirc = new Entity('Ucirc')      // latin capital U with circumflex, U+00DB ISOlat1
    /** Entity reference {@code &Uuml;}. */
    public static final Entity Uuml = new Entity('Uuml')        // latin capital U with diaeresis, U+00DC ISOlat1
    /** Entity reference {@code &Yacute;}. */
    public static final Entity Yacute = new Entity('Yacute')    // latin capital Y with acute, U+00DD ISOlat1
    /** Entity reference {@code &THORN;}. */
    public static final Entity THORN = new Entity('THORN')      // latin capital THORN, U+00DE ISOlat1
    /** Entity reference {@code &szlig;}. */
    public static final Entity szlig = new Entity('szlig')      // latin small sharp s = ess-zed, U+00DF ISOlat1
    /** Entity reference {@code &agrave;}. */
    public static final Entity agrave = new Entity('agrave')    // latin small a with grave = latin small a grave, U+00E0 ISOlat1
    /** Entity reference {@code &aacute;}. */
    public static final Entity aacute = new Entity('aacute')    // latin small a with acute, U+00E1 ISOlat1
    /** Entity reference {@code &acirc;}. */
    public static final Entity acirc = new Entity('acirc')      // latin small a with circumflex, U+00E2 ISOlat1
    /** Entity reference {@code &atilde;}. */
    public static final Entity atilde = new Entity('atilde')    // latin small a with tilde, U+00E3 ISOlat1
    /** Entity reference {@code &auml;}. */
    public static final Entity auml = new Entity('auml')        // latin small a with diaeresis, U+00E4 ISOlat1
    /** Entity reference {@code &aring;}. */
    public static final Entity aring = new Entity('aring')      // latin small a with ring above = latin small a ring, U+00E5 ISOlat1
    /** Entity reference {@code &aelig;}. */
    public static final Entity aelig = new Entity('aelig')      // latin small ae = latin small ligature ae, U+00E6 ISOlat1
    /** Entity reference {@code &ccedil;}. */
    public static final Entity ccedil = new Entity('ccedil')    // latin small c with cedilla, U+00E7 ISOlat1
    /** Entity reference {@code &egrave;}. */
    public static final Entity egrave = new Entity('egrave')    // latin small e with grave, U+00E8 ISOlat1
    /** Entity reference {@code &eacute;}. */
    public static final Entity eacute = new Entity('eacute')    // latin small e with acute, U+00E9 ISOlat1
    /** Entity reference {@code &ecirc;}. */
    public static final Entity ecirc = new Entity('ecirc')      // latin small e with circumflex, U+00EA ISOlat1
    /** Entity reference {@code &euml;}. */
    public static final Entity euml = new Entity('euml')        // latin small e with diaeresis, U+00EB ISOlat1
    /** Entity reference {@code &igrave;}. */
    public static final Entity igrave = new Entity('igrave')    // latin small i with grave, U+00EC ISOlat1
    /** Entity reference {@code &iacute;}. */
    public static final Entity iacute = new Entity('iacute')    // latin small i with acute, U+00ED ISOlat1
    /** Entity reference {@code &icirc;}. */
    public static final Entity icirc = new Entity('icirc')      // latin small i with circumflex, U+00EE ISOlat1
    /** Entity reference {@code &iuml;}. */
    public static final Entity iuml = new Entity('iuml')        // latin small i with diaeresis, U+00EF ISOlat1
    /** Entity reference {@code &eth;}. */
    public static final Entity eth = new Entity('eth')          // latin small eth, U+00F0 ISOlat1
    /** Entity reference {@code &ntilde;}. */
    public static final Entity ntilde = new Entity('ntilde')    // latin small n with tilde, U+00F1 ISOlat1
    /** Entity reference {@code &ograve;}. */
    public static final Entity ograve = new Entity('ograve')    // latin small o with grave, U+00F2 ISOlat1
    /** Entity reference {@code &oacute;}. */
    public static final Entity oacute = new Entity('oacute')    // latin small o with acute, U+00F3 ISOlat1
    /** Entity reference {@code &ocirc;}. */
    public static final Entity ocirc = new Entity('ocirc')      // latin small o with circumflex, U+00F4 ISOlat1
    /** Entity reference {@code &otilde;}. */
    public static final Entity otilde = new Entity('otilde')    // latin small o with tilde, U+00F5 ISOlat1
    /** Entity reference {@code &ouml;}. */
    public static final Entity ouml = new Entity('ouml')        // latin small o with diaeresis, U+00F6 ISOlat1
    /** Entity reference {@code &divide;}. */
    public static final Entity divide = new Entity('divide')    // division sign, U+00F7 ISOnum
    /** Entity reference {@code &oslash;}. */
    public static final Entity oslash = new Entity('oslash')    // latin small o with stroke, = latin small o slash, U+00F8 ISOlat1
    /** Entity reference {@code &ugrave;}. */
    public static final Entity ugrave = new Entity('ugrave')    // latin small u with grave, U+00F9 ISOlat1
    /** Entity reference {@code &uacute;}. */
    public static final Entity uacute = new Entity('uacute')    // latin small u with acute, U+00FA ISOlat1
    /** Entity reference {@code &ucirc;}. */
    public static final Entity ucirc = new Entity('ucirc')      // latin small u with circumflex, U+00FB ISOlat1
    /** Entity reference {@code &uuml;}. */
    public static final Entity uuml = new Entity('uuml')        // latin small u with diaeresis, U+00FC ISOlat1
    /** Entity reference {@code &yacute;}. */
    public static final Entity yacute = new Entity('yacute')    // latin small y with acute, U+00FD ISOlat1
    /** Entity reference {@code &thorn;}. */
    public static final Entity thorn = new Entity('thorn')      // latin small thorn with, U+00FE ISOlat1
    /** Entity reference {@code &yuml;}. */
    public static final Entity yuml = new Entity('yuml')        // latin small y with diaeresis, U+00FF ISOlat1

    /*
     * XML-compatible ISO Special Character Entity Set for XHTML
     */

    // C0 Controls and Basic Latin
    /** Entity reference {@code &lt;}. */
    public static final Entity lt = new Entity('lt')            //less-than sign, U+003C ISOnum
    /** Entity reference {@code &gt;}. */
    public static final Entity gt = new Entity('gt')            //greater-than sign, U+003E ISOnum
    /** Entity reference {@code &amp;}. */
    public static final Entity amp = new Entity('amp')          //ampersand, U+0026 ISOnum
    /** Entity reference {@code &apos;}. */
    public static final Entity apos = new Entity('apos')        //The Apostrophe (Apostrophe Quote, APL Quote), U+0027 ISOnum
    /** Entity reference {@code &quot;}. */
    public static final Entity quot = new Entity('quot')        //quotation mark (Quote Double), U+0022 ISOnum

    // Latin Extended-A
    /** Entity reference {@code &OElig;}. */
    public static final Entity OElig = new Entity('OElig')      //latin capital ligature OE, U+0152 ISOlat2
    /** Entity reference {@code &oelig;}. */
    public static final Entity oelig = new Entity('oelig')      //latin small ligature oe, U+0153 ISOlat2

    // ligature is a misnomer, this is a separate character in some languages
    /** Entity reference {@code &Scaron;}. */
    public static final Entity Scaron = new Entity('Scaron')    //latin capital letter S with caron, U+0160 ISOlat2
    /** Entity reference {@code &scaron;}. */
    public static final Entity scaron = new Entity('scaron')    //latin small letter s with caron, U+0161 ISOlat2
    /** Entity reference {@code &Yuml;}. */
    public static final Entity Yuml = new Entity('Yuml')        //latin capital letter Y with diaeresis, U+0178 ISOlat2

    // Spacing Modifier Letters
    /** Entity reference {@code &circ;}. */
    public static final Entity circ = new Entity('circ')        //modifier letter circumflex accent, U+02C6 ISOpub
    /** Entity reference {@code &tilde;}. */
    public static final Entity tilde = new Entity('tilde')      //small tilde, U+02DC ISOdia

    // General Punctuation
    /** Entity reference {@code &ensp;}. */
    public static final Entity ensp = new Entity('ensp')        //en space, U+2002 ISOpub
    /** Entity reference {@code &emsp;}. */
    public static final Entity emsp = new Entity('emsp')        //em space, U+2003 ISOpub
    /** Entity reference {@code &thinsp;}. */
    public static final Entity thinsp = new Entity('thinsp')    //thin space, U+2009 ISOpub
    /** Entity reference {@code &zwnj;}. */
    public static final Entity zwnj = new Entity('zwnj')        //zero width non-joiner, U+200C NEW RFC 2070
    /** Entity reference {@code &zwj;}. */
    public static final Entity zwj = new Entity('zwj')          //zero width joiner, U+200D NEW RFC 2070
    /** Entity reference {@code &lrm;}. */
    public static final Entity lrm = new Entity('lrm')          //left-to-right mark, U+200E NEW RFC 2070
    /** Entity reference {@code &rlm;}. */
    public static final Entity rlm = new Entity('rlm')          //right-to-left mark, U+200F NEW RFC 2070
    /** Entity reference {@code &ndash;}. */
    public static final Entity ndash = new Entity('ndash')      //en dash, U+2013 ISOpub
    /** Entity reference {@code &mdash;}. */
    public static final Entity mdash = new Entity('mdash')      //em dash, U+2014 ISOpub
    /** Entity reference {@code &lsquo;}. */
    public static final Entity lsquo = new Entity('lsquo')      //left single quotation mark, U+2018 ISOnum
    /** Entity reference {@code &rsquo;}. */
    public static final Entity rsquo = new Entity('rsquo')      //right single quotation mark, U+2019 ISOnum
    /** Entity reference {@code &sbquo;}. */
    public static final Entity sbquo = new Entity('sbquo')      //single low-9 quotation mark, U+201A NEW
    /** Entity reference {@code &ldquo;}. */
    public static final Entity ldquo = new Entity('ldquo')      //left double quotation mark, U+201C ISOnum
    /** Entity reference {@code &rdquo;}. */
    public static final Entity rdquo = new Entity('rdquo')      //right double quotation mark, U+201D ISOnum
    /** Entity reference {@code &bdquo;}. */
    public static final Entity bdquo = new Entity('bdquo')      //double low-9 quotation mark, U+201E NEW
    /** Entity reference {@code &dagger;}. */
    public static final Entity dagger = new Entity('dagger')    //dagger, U+2020 ISOpub
    /** Entity reference {@code &Dagger;}. */
    public static final Entity Dagger = new Entity('Dagger')    //double dagger, U+2021 ISOpub
    /** Entity reference {@code &permil;}. */
    public static final Entity permil = new Entity('permil')    //per mille sign, U+2030 ISOtech

    // lsaquo is proposed but not yet ISO standardized
    /** Entity reference {@code &lsaquo;}. */
    public static final Entity lsaquo = new Entity('lsaquo')    //single left-pointing angle quotation mark, U+2039 ISO proposed

    // rsaquo is proposed but not yet ISO standardized
    /** Entity reference {@code &rsaquo;}. */
    public static final Entity rsaquo = new Entity('rsaquo')    //single right-pointing angle quotation mark, U+203A ISO proposed
    /** Entity reference {@code &euro;}. */
    public static final Entity euro = new Entity('euro')        //euro sign, U+20AC NEW

    private final entity

    /**
     * Creates a named entity reference such as {@code &amp;}.
     *
     * @param name entity name without the leading ampersand or trailing semicolon
     */
    Entity(String name) {
        this.entity = "&$name;"
    }

    /**
     * Creates a numeric entity reference such as {@code &#160;}.
     *
     * @param name numeric entity value
     */
    Entity(int name) {
        this.entity = "&#$name;"
    }

    /**
     * Writes this entity reference to the target builder without escaping.
     *
     * @param builder builder exposing an {@code unescaped} output channel
     */
    @SuppressWarnings('BuilderMethodWithSideEffects')
    void build(GroovyObject builder) {
        builder.unescaped << entity
    }
}
