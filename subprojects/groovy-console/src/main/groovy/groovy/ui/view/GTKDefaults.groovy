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
package groovy.ui.view

import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import java.util.prefs.Preferences

build(Defaults)

def prefs = Preferences.userNodeForPackage(groovy.ui.Console)
def fontFamily = prefs.get("fontName", "DejaVu Sans Mono")

// change font to DejaVu Sans Mono, much clearer
styles.regular[StyleConstants.FontFamily] = fontFamily
styles[StyleContext.DEFAULT_STYLE][StyleConstants.FontFamily] = fontFamily

// some current distros (Ubuntu 7.10) have broken printing support :(
// detect it and disable it
try {
    pj = java.awt.print.PrinterJob.getPrinterJob()
    ps = pj.getPrintService()
    ps.getAttributes()
    docFlav  = (ps.getSupportedDocFlavors() as List).find {it.mimeType == 'application/vnd.cups-postscript' }
    attrset = ps.getAttributes()
    orient = attrset.get(javax.print.attribute.standard.OrientationRequested) ?: 
             ps.getDefaultAttributeValue(javax.print.attribute.standard.OrientationRequested)
    ps.isAttributeValueSupported(orient, docFlav, attrset)
} catch (NullPointerException npe) {
    //print will bomb out... replace with disabled print action
    printAction = action(
        name: 'Print...',
        closure: controller.&print,
        mnemonic: 'P',
        accelerator: shortcut('P'),
        shortDescription: 'Printing does not work in Java with this version of CUPS',
        enabled: false
    )
}



