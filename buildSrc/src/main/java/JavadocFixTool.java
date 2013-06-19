/*
 * Copyright (c) 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * License Agreement
 * 
 * PLEASE READ THE FOLLOWING LICENSE TERMS CAREFULLY BEFORE USING THE 
 * ACCOMPANYING PROGRAM. THESE TERMS CONSTITUTE A LEGAL AGREEMENT BETWEEN 
 * YOU AND US.
 * 
 * "Oracle" refers to Oracle America, Inc., for and on behalf of itself and its 
 * subsidiaries and affiliates under common control.  "We," "us," and "our" 
 * refers to Oracle and any Program contributors. "You" and "your" refers to 
 * the individual or entity that wishes to use the Program. "Program" refers to
 * the Java API Documentation Updater Tool, Copyright (c) 2013, Oracle America,
 * Inc., and updates or error corrections provided by Oracle or contributors.
 * 
 * WARNING: 
 * The Program will analyze directory information on your computer 
 * system and may modify software components on such computer system.  You 
 * should only use the Program on computer systems that you maintain sufficient
 * rights to update software components.
 * 
 * If your computer system is owned by a person or entity other than you, 
 * you should check with such person or entity before using the Program. 
 * 
 * It is possible that you may lose some software functionality, and make 
 * Java API Documentation pages unusable on your computer system after you use
 * the Program to update software components.
 * 
 * License Rights and Obligations 
 * We grant you a perpetual, nonexclusive, limited license to use, modify and 
 * distribute the Program in binary and/or source code form, only for the
 * purpose of analyzing the directory structure of your computer system and
 * updating Java API Documentation files.  If you distribute the Program, in
 * either or both binary or source form, including as modified by you, you
 * shall include this License Agreement ("Agreement") with your distribution.
 * 
 * All rights not expressly granted above are hereby reserved. If you want to
 * use the Program for any purpose other than as permitted under this
 * Agreement, you must obtain a valid license permitting such use from Oracle.
 * Neither the name of Oracle nor the names of any Program contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * Ownership and Restrictions 
 * We retain all ownership and intellectual property rights in the Program as
 * provided by us. You retain all ownership and intellectual property rights
 * in your modifications.
 * 
 * Export
 * You agree to comply fully with export laws and regulations of the United 
 * States and any other applicable export laws ("Export Laws") to assure that
 * neither the Program nor any direct products thereof are:  (1) exported,
 * directly or indirectly, in violation of this Agreement or Export Laws; or
 * (2) used for any purposes prohibited by the Export Laws, including, without
 * limitation, nuclear, chemical, or biological weapons proliferation, or
 * development of missile technology. 
 * 
 * Disclaimer of Warranty and Limitation of Liability 
 * THE PROGRAM IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. USE AT YOUR
 * OWN RISK.  WE FURTHER DISCLAIM ALL WARRANTIES, EXPRESS AND IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT. 
 * 
 * IN NO EVENT SHALL WE BE LIABLE FOR ANY INDIRECT, DIRECT, INCIDENTAL,
 * SPECIAL, PUNITIVE OR CONSEQUENTIAL DAMAGES, OR DAMAGES FOR LOSS OF PROFITS,
 * REVENUE, DATA OR DATA USE, INCURRED BY YOU OR ANY THIRD PARTY, WHETHER IN AN
 * ACTION IN CONTRACT OR TORT, EVEN IF WE HAVE BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.  ORACLE SHALL HAVE NO LIABILITY FOR MODIFICATIONS MADE BY
 * YOU OR ANY THIRD PARTY.
 * 
 * Entire Agreement
 * You agree that this Agreement is the complete agreement for the Program, and
 * this Agreement supersedes all prior or contemporaneous agreements or
 * representations. If any term of this Agreement is found to be invalid or
 * unenforceable, the remaining provisions will remain effective. This
 * Agreement is governed by the substantive and procedural laws of California.
 * You and Oracle agree to submit to the exclusive jurisdiction of, and venue
 * in, the courts of San Francisco or Santa Clara counties in California in
 * any dispute between you and Oracle arising out of or relating to this
 * Agreement. 
 * 
 * Last updated: 14 June 2013
 */
import java.io.*;

/*
 * Tool for finding and addressing files related to CVE-2013-1571.
 * See README file for details.
 */
public class JavadocFixTool {
    // Usual suspects
    private final static String[] fileNames = {"index.html",
                                         "index.htm",
                                         "toc.html",
                                         "toc.htm"};

    // If we locate this function but not validURL - we are in trouble
    private final String patchString = "function loadFrames() {";
    // Main fix - should be inserted before the loadFrames() function alongside
    // the code that calls this function
    private final static String[] patchData =
            {"    if (targetPage != \"\" && !validURL(targetPage))",
             "        targetPage = \"undefined\";",
             "    function validURL(url) {",
             "        var pos = url.indexOf(\".html\");",
             "        if (pos == -1 || pos != url.length - 5)",
             "            return false;",
             "        var allowNumber = false;",
             "        var allowSep = false;",
             "        var seenDot = false;",
             "        for (var i = 0; i < url.length - 5; i++) {",
             "            var ch = url.charAt(i);",
             "            if ('a' <= ch && ch <= 'z' ||",
             "                    'A' <= ch && ch <= 'Z' ||",
             "                    ch == '$' ||",
             "                    ch == '_') {",
             "                allowNumber = true;",
             "                allowSep = true;",
             "            } else if ('0' <= ch && ch <= '9'",
             "                    || ch == '-') {",
             "                if (!allowNumber)",
             "                     return false;",
             "            } else if (ch == '/' || ch == '.') {",
             "                if (!allowSep)",
             "                    return false;",
             "                allowNumber = false;",
             "                allowSep = false;",
             "                if (ch == '.')",
             "                     seenDot = true;",
             "                if (ch == '/' && seenDot)",
             "                     return false;",
             "            } else {",
             "                return false;",
             "            }",
             "        }",
             "        return true;",
             "    }",
             "    function loadFrames() {"};

    private final String quickFixString = "if (!(url.indexOf(\".html\") == url.length - 5))";
    private final String[] quickFix = {"        var pos = url.indexOf(\".html\");",
                                       "        if (pos == -1 || pos != url.length - 5)"};
    private static String readme = null;
    private static String version = "Java Documentation Updater Tool version 1.2 06/14/2013\n";

    private static boolean doPatch = true; // By default patch file
    private static boolean recursive = false; // By default only look in the folder in parameter

    public static void main(String[] args) {
        System.out.println(version);

        if (args.length < 1) {
            // No arguments - lazily initialize readme, print readme and usage
            initReadme();
            if (readme != null) {
                System.out.println(readme);
            }
            printUsage(System.out);
            return;
        }

        // Last argument should be a path to the document root
        String name = args[args.length-1];

        // Analyze the rest of parameters
        for (int i = 0 ; i < args.length -1; i++) {
            if ("-R".equalsIgnoreCase(args[i])) {
                recursive = true;
            } else if ("-C".equalsIgnoreCase(args[i])) {
                doPatch = false;
            } else {
                System.err.println("Unknown option passed: "+args[i]);
                printUsage(System.err);
                return;
            }
        }
        new JavadocFixTool().proceed(name);
    }

    /*
     * Print usage information into the provided PrintStream
     * @param out PrintStream to write usage information
     */
    public static void printUsage(PrintStream out) {
        out.println("Usage: java -jar JavadocPatchTool.jar [-R] [-C] <Path to Javadoc root>");
        out.println("    -R : Proceed recursively starting from given folder");
        out.println("    -C : Check only - program will find vulnerable files and print their full paths");
    }

    /*
     * Lazily initialize the readme document, reading it from README file inside the jar
     */
    public static void initReadme() {
        try {
            InputStream readmeStream = JavadocFixTool.class.getResourceAsStream("/README");
            if (readmeStream != null) {
                BufferedReader readmeReader = new BufferedReader(new InputStreamReader(readmeStream));
                StringBuilder readmeBuilder = new StringBuilder();
                String s;
                while ((s = readmeReader.readLine()) != null) {
                    readmeBuilder.append(s);
                    readmeBuilder.append("\n");
                }
                readme = readmeBuilder.toString();
            }
        } catch (IOException ignore) {} // Ignore exception - readme not initialized
    }

    /*
     * Main procedure - proceed with the searching and/or fixing depending on
     * the command line parameters
     * @param name Path to the document root
     */
    public void proceed(String name) {
        try {
            File folder = new File(name);
            if (folder.exists() && folder.isDirectory() && folder.canRead()) {
                searchAndPatch(folder);
            } else {
                System.err.println("Invalid folder in parameter \""+name+"\"");
                printUsage(System.err);
            }
        } catch (Exception ignored) {} // Die silently
    }

    /*
     * Find all the files that match the list given in the fileNames array.
     * If file found attempt to patch it.
     * If global parameter recursive is set to true attempt to go into the enclosed subfolders
     * otherwise only patch said files in the folder directly pointed in parameter.
     */
    public void searchAndPatch(File folder) {
        if (folder == null || !folder.isDirectory() || folder.list() == null) {
            // Silently return
            return;
        }

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                if(recursive) {
                    searchAndPatch(file);
                }
                continue;
            }
            String name = file.getName();
            for (String s : fileNames) {
                if (s.equalsIgnoreCase(name)) {
                    try {
                        applyPatch(file, folder);
                    } catch (Exception ex) {
                        String filePath;
                        try {
                            filePath = file.getCanonicalPath();
                        } catch (IOException ioe) {
                            System.err.println("Can not resolve path to "+file.getName()+" in folder "+folder.getName());
                            continue;
                        }
                        System.err.println("Patch failed on: "+filePath+" due to the "+ex);
                    }
                }
            }
        }
    }

    /*
     * Try to apply patch to the single file in the specific folder
     * If global parameter doPatch is false we should only print the location of the vulnerable html file
     * and return
     */
    public void applyPatch(File file, File currentFolder) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        String failedString = patchString;
        String[] patch = patchData;
        // Attempt to look if file is vulnerable
        for (int i = 0 ; i < 80 ; i++) { // Check first 80 lines - if there is no signature it is not our file
            line = br.readLine();
            if (line == null) {
                // File less than 80 lines long, no signature encountered
                return;
            }
            if (line.trim().equals("function validURL(url) {")) { // Already patched
                failedString = null;
                patch = null;
                continue;
            }
            if (line.trim().equals(quickFixString)) { // The patch had famous 2-letter bug, update it
                failedString = quickFixString;
                patch = quickFix;
                continue;
            }
            if (line.trim().equals("function loadFrames() {")) {
                fis.close(); // It should not interfere with the file renaming process
                if (failedString != null) {
                    // Vulnerable file
                    if (!doPatch) { // Report and return
                        System.out.println("Vulnerable file found: "+file.getCanonicalPath());
                    } else {
                        replaceStringInFile(currentFolder, file, failedString, patch);
                    }
                }
                return;
            }
        }
    }

    /*
     * Replace one line in the given file in the given folder with the lines given
     * @param folder Folder in which file should be created
     * @param file Original file to patch
     * @param template Trimmed String with the pattern we are have to find
     * @param replacement Array of String that has to be written in the place of first line matching the template
     */
    public void replaceStringInFile(File folder, File file, String template, String[] replacement)
            throws IOException {
        System.out.println("Patching file: "+file.getCanonicalPath());
        String name = file.getName();
        File origFile = new File(folder, name+".orig");
        file.renameTo(origFile);
        File temporaryFile = new File(folder, name+".tmp");
        if (temporaryFile.exists()) {
            temporaryFile.delete();
        }
        temporaryFile.createNewFile();
        String line;
        FileInputStream fis = new FileInputStream(origFile);
        PrintWriter pw = new PrintWriter(temporaryFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        while ((line = br.readLine()) != null) {
            if (line.trim().equals(template)) {
                for (String s : replacement) {
                    pw.println(s);
                }
            } else {
                pw.println(line);
            }
        }
        pw.flush();
        pw.close();
        if (!temporaryFile.renameTo(new File(folder, name))) {
            throw new IOException("Unable to rename file in folder "+folder.getName()+
                    " from \""+temporaryFile.getName()+"\" into \""+name +
                    "\n Original file saved as "+origFile.getName());
        }
        origFile.delete();
    }
}
