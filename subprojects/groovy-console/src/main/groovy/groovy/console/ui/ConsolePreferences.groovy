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
package groovy.console.ui

import groovy.beans.Bindable
import groovy.swing.SwingBuilder
import org.codehaus.groovy.tools.shell.util.MessageSource

import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.Dimension

/**
 * Manages the Groovy Console preferences dialog and persisted settings.
 */
class ConsolePreferences {

    /** Default maximum number of characters retained in the output area. */
    static int DEFAULT_MAX_OUTPUT_CHARS = 20000
    /** Default loop mode delay, in milliseconds. */
    static int DEFAULT_LOOP_MODE_DELAY_MILLIS = 1000

    /** Maximum number of characters retained in the output area. */
    @Bindable
    int maxOutputChars

    /** Delay between loop mode executions, in milliseconds. */
    @Bindable
    int loopModeDelay

    /** Selected icon size for console actions. */
    @Bindable
    int iconSize

    /** Whether action icons should track the current font size. */
    @Bindable
    boolean scaleIconsWithFont

    /** Optional custom light theme path. */
    @Bindable
    String customLightThemePath

    /** Optional custom dark theme path. */
    @Bindable
    String customDarkThemePath

    private final console
    private final MessageSource T

    private JDialog dialog
    /** Output file currently selected in the preferences dialog. */
    File outputFile

    /**
     * Creates a preferences controller for the supplied console.
     *
     * @param console console instance that owns the dialog
     */
    ConsolePreferences(console) {
        this.console = console
        T = new MessageSource(Console)

        maxOutputChars = console.loadMaxOutputChars()
        loopModeDelay = console.prefs.getInt('loopModeDelay', DEFAULT_LOOP_MODE_DELAY_MILLIS)
        iconSize = console.prefs.getInt('iconSize', Icons.SIZE_NORMAL)
        scaleIconsWithFont = console.prefs.getBoolean('scaleIconsWithFont', false)
        customLightThemePath = ThemeManager.customLightPath ?: ''
        customDarkThemePath  = ThemeManager.customDarkPath  ?: ''
        console.maxOutputChars = maxOutputChars
    }

    /**
     * Displays the preferences dialog.
     */
    void show() {
        console.swing.edt {
            if (!dialog) {
                buildDialog()
            }
            dialog.setLocationRelativeTo(console.frame)
            dialog.pack()
            dialog.getRootPane().setDefaultButton(console.swing.closePrefsButton)
            console.swing.doLater console.swing.closePrefsButton.&requestFocusInWindow
            dialog.setVisible(true)
        }
    }

    private void buildDialog() {
        dialog = console.swing.dialog(
                title: T['prefs.dialog.title'], owner: console.frame, modal: true
        ) {
            vbox(border: emptyBorder([12, 16, 12, 16])) {
                vbox(border: compoundBorder([
                        titledBorder(T['prefs.output.settings.title']),
                        emptyBorder([6, 8, 8, 8])])) {
                    hbox {
                        label "${T['prefs.max.characters.output']}:"
                        hstrut(6)
                        formattedTextField value: maxOutputChars, id: 'txtMaxOutputChars',
                                text:
                                        bind(target: this, targetProperty: 'maxOutputChars',
                                                validator: this.&isInteger, converter: Integer.&parseInt),
                                columns: 6
                        hglue()
                    }

                    vstrut(8)

                    hbox {
                        label "${T['prefs.loop.mode.title']}:"
                        hstrut(6)
                        formattedTextField value: loopModeDelay, id: 'txtLoopModeDelay',
                                text:
                        bind(target: this, targetProperty: 'loopModeDelay',
                                validator: this.&isInteger, converter: Integer.&parseInt),
                        columns: 8
                        hglue()
                    }

                    vstrut(8)

                    hbox {
                        checkBox T['prefs.output.file'], id: 'outputFileCheckBox', selected: false
                        hstrut(8)
                        label T['prefs.output.file.name'], id: 'outputFileName',
                                enabled: bind(source: outputFileCheckBox, sourceProperty: 'selected')
                        hglue()
                        button T['prefs.output.file.select'], id: 'outputFileNameButton',
                                enabled: bind(source: outputFileCheckBox, sourceProperty: 'selected'),
                                actionPerformed: this.&onChooseFile
                    }
                }

                vstrut(12)

                vbox(border: compoundBorder([
                        titledBorder(T['prefs.appearance.settings.title']),
                        emptyBorder([6, 8, 8, 8])])) {
                    hbox {
                        label "${T['prefs.icon.size']}:"
                        hstrut(8)
                        buttonGroup(id: 'iconSizeGroup')
                        radioButton(text: T['prefs.icon.size.small'],  id: 'iconSizeSmall',
                                buttonGroup: iconSizeGroup, selected: iconSize == Icons.SIZE_SMALL,
                                enabled: !scaleIconsWithFont,
                                actionPerformed: { iconSize = Icons.SIZE_SMALL })
                        hstrut(4)
                        radioButton(text: T['prefs.icon.size.normal'], id: 'iconSizeNormal',
                                buttonGroup: iconSizeGroup, selected: iconSize == Icons.SIZE_NORMAL,
                                enabled: !scaleIconsWithFont,
                                actionPerformed: { iconSize = Icons.SIZE_NORMAL })
                        hstrut(4)
                        radioButton(text: T['prefs.icon.size.large'],  id: 'iconSizeLarge',
                                buttonGroup: iconSizeGroup, selected: iconSize == Icons.SIZE_LARGE,
                                enabled: !scaleIconsWithFont,
                                actionPerformed: { iconSize = Icons.SIZE_LARGE })
                        hglue()
                    }

                    vstrut(4)

                    hbox {
                        checkBox(text: T['prefs.scale.icons.with.font'], id: 'scaleIconsCheckBox',
                                selected: scaleIconsWithFont,
                                actionPerformed: this.&onScaleWithFontToggled)
                        hglue()
                    }

                    vstrut(10)

                    hbox {
                        label "${T['prefs.custom.light.theme']}:"
                        hstrut(6)
                        label(id: 'customLightPathLabel',
                                text: customLightThemePath ?: T['prefs.no.file.selected'])
                        hglue()
                        button(text: T['prefs.select'], actionPerformed: this.&onSelectLightTheme)
                        hstrut(4)
                        button(text: T['prefs.clear'], id: 'clearLightButton',
                                enabled: customLightThemePath as boolean,
                                actionPerformed: this.&onClearLightTheme)
                    }

                    vstrut(4)

                    hbox {
                        label "${T['prefs.custom.dark.theme']}:"
                        hstrut(6)
                        label(id: 'customDarkPathLabel',
                                text: customDarkThemePath ?: T['prefs.no.file.selected'])
                        hglue()
                        button(text: T['prefs.select'], actionPerformed: this.&onSelectDarkTheme)
                        hstrut(4)
                        button(text: T['prefs.clear'], id: 'clearDarkButton',
                                enabled: customDarkThemePath as boolean,
                                actionPerformed: this.&onClearDarkTheme)
                    }

                    vstrut(8)

                    hbox {
                        hglue()
                        button(text: T['prefs.reload.themes'], actionPerformed: this.&onReloadThemes)
                    }
                }

                vstrut(12)

                hbox {
                    button T['prefs.reset.defaults'], id: 'resetPrefsButton', actionPerformed: this.&onReset
                    hglue()
                    button T['prefs.close'], id: 'closePrefsButton', actionPerformed: this.&onClose
                }

            }
        }

        console.swing.txtMaxOutputChars.maximumSize = new Dimension(Integer.MAX_VALUE, (int) console.swing.txtMaxOutputChars.preferredSize.height)

        def outputLogFileName = console.prefs.get('outputLogFileName', null)
        if (outputLogFileName != null) {
            console.swing.outputFileCheckBox.selected = true
            console.swing.outputFileName.text = outputLogFileName
        }
    }

    private boolean isInteger(value) {
        try {
            Integer.parseInt(value)
            return true
        } catch (NumberFormatException ignore) {
            return false
        }
    }

    private void onReset(EventObject event) {
        console.swing.txtMaxOutputChars.text = DEFAULT_MAX_OUTPUT_CHARS
        console.swing.txtLoopModeDelay.text = DEFAULT_LOOP_MODE_DELAY_MILLIS
        iconSize = Icons.SIZE_NORMAL
        scaleIconsWithFont = false
        console.swing.iconSizeNormal.selected = true
        console.swing.scaleIconsCheckBox.selected = false
        setIconSizeRadiosEnabled(true)
        updateLightPathLabel(null)
        updateDarkPathLabel(null)
    }

    private void onClose(EventObject event) {
        console.prefs.putInt('maxOutputChars', maxOutputChars)
        console.prefs.putInt('loopModeDelay', loopModeDelay)
        // For backwards compatibility 'maxOutputChars' remains defined in the Console class
        // and so we update the value to keep it in sync.
        if (maxOutputChars != console.maxOutputChars) {
            console.maxOutputChars = maxOutputChars
        }

        console.setOutputPreferences(console.swing.outputFileCheckBox.enabled, outputFile)

        // apply appearance settings
        boolean previousScale = console.prefs.getBoolean('scaleIconsWithFont', false)
        int previousIcon = console.prefs.getInt('iconSize', Icons.SIZE_NORMAL)
        if (scaleIconsWithFont != previousScale) {
            console.setScaleIconsWithFont(scaleIconsWithFont)
        } else if (!scaleIconsWithFont && iconSize != previousIcon) {
            console.applyIconSize(iconSize)
        }

        // apply custom theme paths — reapply theme only if something actually changed
        String previousLight = ThemeManager.customLightPath ?: ''
        String previousDark  = ThemeManager.customDarkPath  ?: ''
        String newLight = customLightThemePath ?: ''
        String newDark  = customDarkThemePath  ?: ''
        if (newLight != previousLight || newDark != previousDark) {
            ThemeManager.customLightPath = newLight ?: null
            ThemeManager.customDarkPath  = newDark  ?: null
            console.reloadThemes()
        }

        dialog.dispose()
    }

    private void onChooseFile(EventObject event) {
        JFileChooser fileChooser = console.swing.fileChooser()

        if (console.prefs.get('outputLogFileName', null) != null) {
            fileChooser.setSelectedFile(new File(console.prefs.get('outputLogFileName', null)))
        }

        if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
            outputFile = fileChooser.selectedFile
        }
        console.swing.outputFileName.text = outputFile.path
    }

    private void onScaleWithFontToggled(EventObject event) {
        scaleIconsWithFont = event.source.selected
        setIconSizeRadiosEnabled(!scaleIconsWithFont)
    }

    private void setIconSizeRadiosEnabled(boolean enabled) {
        console.swing.iconSizeSmall.enabled = enabled
        console.swing.iconSizeNormal.enabled = enabled
        console.swing.iconSizeLarge.enabled = enabled
    }

    private void onSelectLightTheme(EventObject event) { pickThemeFile { it -> updateLightPathLabel(it) } }
    private void onSelectDarkTheme (EventObject event) { pickThemeFile { it -> updateDarkPathLabel(it)  } }
    private void onClearLightTheme (EventObject event) { updateLightPathLabel(null) }
    private void onClearDarkTheme  (EventObject event) { updateDarkPathLabel(null)  }

    private void onReloadThemes(EventObject event) {
        // apply any staged path changes first so Reload uses current selections
        ThemeManager.customLightPath = customLightThemePath ?: null
        ThemeManager.customDarkPath  = customDarkThemePath  ?: null
        console.reloadThemes()
    }

    private void pickThemeFile(Closure onPicked) {
        JFileChooser fileChooser = new JFileChooser()
        fileChooser.fileFilter = new FileNameExtensionFilter('Groovy theme files (*.theme)', 'theme')
        def groovyDir = new File(System.getProperty('user.home'), '.groovy')
        fileChooser.currentDirectory = groovyDir.isDirectory() ? groovyDir : new File(System.getProperty('user.home'))
        if (fileChooser.showOpenDialog(dialog) != JFileChooser.APPROVE_OPTION) return
        File picked = fileChooser.selectedFile
        try {
            picked.withReader('UTF-8') { r -> ThemeManager.parseTheme(r) }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog,
                    "Could not parse theme file:\n${picked.absolutePath}\n\n${ex.message}",
                    'Invalid Theme File', JOptionPane.WARNING_MESSAGE)
            return
        }
        onPicked(picked.absolutePath)
    }

    private void updateLightPathLabel(String path) {
        customLightThemePath = path ?: ''
        console.swing.customLightPathLabel.text = path ?: T['prefs.no.file.selected']
        console.swing.clearLightButton.enabled = path as boolean
    }

    private void updateDarkPathLabel(String path) {
        customDarkThemePath = path ?: ''
        console.swing.customDarkPathLabel.text = path ?: T['prefs.no.file.selected']
        console.swing.clearDarkButton.enabled = path as boolean
    }

    // Useful for testing gui
    /**
     * Launches the preferences dialog in isolation for manual UI checks.
     *
     * @param args ignored command-line arguments
     */
    static void main(args) {
        javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())
        def c = new Expando().with {
            swing = new SwingBuilder()
            frame = swing.frame(title: 'foo', size: [800, 800])
            DEFAULT_MAX_OUTPUT_CHARS = 25000
            maxOutputChars = 25000
            loadMaxOutputChars = { 20000 }
            prefs = [putInt: { s, t -> }, getInt: { s, t -> t }]
            it
        }
        ConsolePreferences cp = new ConsolePreferences(c)
        cp.show()
        c.frame.dispose()
        println "maxOutputChars==${cp.maxOutputChars}"
        println "console.maxOutputChars==${c.maxOutputChars}"
        println 'done'
    }
}
