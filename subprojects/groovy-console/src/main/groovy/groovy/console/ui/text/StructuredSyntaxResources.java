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

import java.awt.*;
import java.awt.datatransfer.Clipboard;

/**
 * Contains all the basic resources and values used by the utility frame work
 * framework.
 */
public final class StructuredSyntaxResources {

    // ==================================================
    // ClipBoard
    // ==================================================
    
    public static final Clipboard SYSTEM_CLIPBOARD;
    static {
        Clipboard systemClipboard = null;
        try {
            // if we don't have access to the system clipboard, will throw
            // a security exception
            SecurityManager mgr = System.getSecurityManager();
            if (mgr != null) {
                mgr.checkPermission(new AWTPermission("accessClipboard"));
            }
            systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        catch (SecurityException e) {
            // means we can't get to system clipboard, so create app level one
            systemClipboard = new Clipboard("UIResourceMgr");
        }
        catch (Exception e) {
            e.printStackTrace();               
        }
        SYSTEM_CLIPBOARD = systemClipboard;
    }

    // =====================================================
    // STANDARD FONTS
    // =====================================================

    public static final Font LARGE_FONT = Font.decode("Arial-24");
    public static final Font MEDIUM_FONT = Font.decode("Arial-18");
    public static final Font SMALL_FONT = Font.decode("Arial-12");
    
    public static final Font EDITOR_FONT = Font.decode("Monospaced-12");

    // =====================================================
    // UNDO/REDO NAMES
    // =====================================================

    public static final String UNDO = "Undo";
    public static final String REDO = "Redo";
    public static final String PRINT = "Print";
    public static final String FIND = "Find";
    public static final String FIND_NEXT = "Find Next";
    public static final String REPLACE = "Replace";
        
    // singleton
    private StructuredSyntaxResources() {
    }
}
