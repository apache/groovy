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

import java.awt.AWTPermission;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

/**
 * Contains all the basic resources and values used by the utility framework.
 */
public final class StructuredSyntaxResources {

    // ==================================================
    // ClipBoard
    // ==================================================

    /**
     * Shared clipboard used by the console text components.
     */
    public static final Clipboard SYSTEM_CLIPBOARD = getSystemOrAppLevelClipboard();

    private static Clipboard getSystemOrAppLevelClipboard() {
        Clipboard systemClipboard = getSystemClipboard();
        if (systemClipboard == null) {
            systemClipboard = new Clipboard("UIResourceMgr");
        }
        return systemClipboard;
    }

    @SuppressWarnings("removal") // TODO a future Groovy version should skip clipboard security check
    private static Clipboard getSystemClipboard() {
        try {
            // if we don't have access to the system clipboard, will throw a security exception
            SecurityManager mgr = System.getSecurityManager();
            if (mgr != null) {
                mgr.checkPermission(new AWTPermission("accessClipboard"));
            }
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        catch (Exception e) {
            // means we can't get to system clipboard
            return null;
        }
    }

    // =====================================================
    // STANDARD FONTS
    // =====================================================

    /**
     * Large UI font used by legacy console components.
     */
    public static final Font LARGE_FONT = Font.decode("Arial-24");
    /**
     * Medium UI font used by legacy console components.
     */
    public static final Font MEDIUM_FONT = Font.decode("Arial-18");
    /**
     * Small UI font used by legacy console components.
     */
    public static final Font SMALL_FONT = Font.decode("Arial-12");

    /**
     * Default monospaced font for editors.
     */
    public static final Font EDITOR_FONT = Font.decode("Monospaced-12");

    // =====================================================
    // UNDO/REDO NAMES
    // =====================================================

    /**
     * Presentation name for undo actions.
     */
    public static final String UNDO = "Undo";
    /**
     * Presentation name for redo actions.
     */
    public static final String REDO = "Redo";
    /**
     * Presentation name for print actions.
     */
    public static final String PRINT = "Print";
    /**
     * Presentation name for find actions.
     */
    public static final String FIND = "Find";
    /**
     * Presentation name for find-next actions.
     */
    public static final String FIND_NEXT = "Find Next";
    /**
     * Presentation name for replace actions.
     */
    public static final String REPLACE = "Replace";

    // singleton
    private StructuredSyntaxResources() {
    }
}
