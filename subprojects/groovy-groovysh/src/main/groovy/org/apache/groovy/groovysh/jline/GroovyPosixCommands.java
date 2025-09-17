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
 *
 * This file has some code copied from its parent's class: PosixCommands from JLine3.
 * This is in some cases due to bug fixes not yet merged or released in JLine3,
 * and in other cases where existing extensibility points were insufficient.
 * Additional PRs related to extensibility might still be needed.
 * The intent would be to delete all copied code (and maybe most of this file)
 * if/when the relevant PRs have been merged into JLine3.
 * The copied code is available under the BSD License.
 */
package org.apache.groovy.groovysh.jline;

import org.codehaus.groovy.runtime.ArrayGroovyMethods;
import org.jline.builtins.Less;
import org.jline.builtins.Options;
import org.jline.builtins.PosixCommands;
import org.jline.builtins.Source;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.InputStreamReader;
import org.jline.utils.OSUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// The following file is expected to be deleted if/when the following issues have been merged in JLine3:
// https://github.com/jline/jline3/pull/1400
// https://github.com/jline/jline3/pull/1398
// https://github.com/jline/jline3/pull/1390
public class GroovyPosixCommands extends PosixCommands {

    public static void cat(Context context, Object[] argv) throws Exception {
        final String[] usage = {
            "/cat - concatenate and print FILES or VARIABLES",
            "Usage: /cat [OPTIONS] [FILES] [VARIABLES]",
            "  -? --help                show help",
            "  -n                       number the output lines, starting at 1"
        };
        Options opt = parseOptions(context, usage, argv);

        List<String> args = opt.args();
        if (args.isEmpty()) {
            args = Collections.singletonList("-");
        }
        List<NamedInputStream> sources = getSources(context, argv, args);
        for (NamedInputStream nis : sources) {
            InputStream is = nis.getInputStream();
            doCat(context, new BufferedReader(new InputStreamReader(is)), opt.isSet("n"));
        }
    }

    private static void doCat(Context context, BufferedReader reader, boolean numbered) throws IOException {
        String line;
        int lineno = 1;
        try {
            while ((line = reader.readLine()) != null) {
                if (numbered) {
                    context.out().printf("%6d\t%s%n", lineno++, line);
                } else {
                    context.out().println(line);
                }
            }
        } finally {
            reader.close();
        }
    }

    public static void wc(Context context, Object[] argv) throws Exception {
        final String[] usage = {
            "/wc - word, line, character, and byte count",
            "Usage: /wc [OPTIONS] [FILES]",
            "  -? --help                    Show help",
            "  -l --lines                   Print line counts",
            "  -c --bytes                   Print byte counts",
            "  -m --chars                   Print character counts",
            "  -w --words                   Print word counts",
            "     --total=WHEN              Print total counts, WHEN=auto|always|never|only",
        };
        Options opt = parseOptions(context, usage, argv);

        List<String> args = opt.args();
        if (args.isEmpty()) {
            args = Collections.singletonList("-");
        }
        List<NamedInputStream> sources = getSources(context, argv, args);

        boolean showLines = opt.isSet("lines");
        boolean showWords = opt.isSet("words");
        boolean showChars = opt.isSet("chars");
        boolean showBytes = opt.isSet("bytes");
        boolean only = false;
        boolean total = sources.size() > 1;
        String totalOpt = opt.isSet("total") ? opt.get("total") : "auto";
        switch (totalOpt) {
            case "always":
            case "yes":
            case "force":
                total = true;
                break;
            case "never":
            case "no":
            case "none":
                total = false;
                break;
            case "only":
                only = true;
                break;
            case "auto":
            case "tty":
            case "if-tty":
                break;
            default:
                throw new IllegalArgumentException("invalid argument '" + totalOpt + "' for '--total'");
        }

        // If no options specified, show all
        if (!showLines && !showWords && !showChars && !showBytes) {
            showLines = showWords = showBytes = true;
        }

        long totalLines = 0, totalWords = 0, totalChars = 0, totalBytes = 0;

        for (NamedInputStream source : sources) {
            long lines = 0, words = 0, chars = 0, bytes = 0;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(source.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines++;
                    chars += line.length() + 1; // +1 for newline
                    bytes += line.getBytes().length + 1; // +1 for newline

                    // Count words
                    String[] wordArray = line.trim().split("\\s+");
                    if (wordArray.length == 1 && wordArray[0].isEmpty()) {
                        // Empty line
                    } else {
                        words += wordArray.length;
                    }
                }
            }

            totalLines += lines;
            totalWords += words;
            totalChars += chars;
            totalBytes += bytes;

            if (only) continue;
            // Print results for this file
            StringBuilder result = new StringBuilder();
            if (showLines) result.append(String.format("%8d", lines));
            if (showWords) result.append(String.format("%8d", words));
            if (showChars) result.append(String.format("%8d", chars));
            if (showBytes) result.append(String.format("%8d", bytes));
            result.append(" ").append(source.getName());

            context.out().println(result);
            context.out().flush();
        }

        // Print totals if multiple files
        if (total) {
            StringBuilder result = new StringBuilder();
            if (showLines) result.append(String.format("%8d", totalLines));
            if (showWords) result.append(String.format("%8d", totalWords));
            if (showChars) result.append(String.format("%8d", totalChars));
            if (showBytes) result.append(String.format("%8d", totalBytes));
            result.append(" total");

            context.out().println(result);
        }
    }

    public static void head(Context context, Object[] argv) throws Exception {
        final String[] usage = {
            "/head - display first lines of files or variables",
            "Usage: /head [-n lines | -c bytes] [-q | -v] [file|variable ...]",
            "  -? --help                    Show help",
            "  -n --lines=LINES             Print line counts",
            "  -c --bytes=BYTES             Print byte counts",
            "  -q --quiet                   Never output filename headers",
            "  -v --verbose                 Always output filename headers",
        };
        Options opt = parseOptions(context, usage, argv);

        if (opt.isSet("lines") && opt.isSet("bytes")) {
            throw new IllegalArgumentException("usage: /head [-n # | -c #] [-q | -v] [file|variable ...]");
        }

        if (opt.isSet("quiet") && opt.isSet("verbose")) {
            throw new IllegalArgumentException("usage: /head [-n # | -c #] [-q | -v] [file|variable ...]");
        }

        int nbLines = Integer.MAX_VALUE;
        int nbBytes = Integer.MAX_VALUE;
        if (opt.isSet("lines")) {
            nbLines = opt.getNumber("lines");
        } else if (opt.isSet("bytes")) {
            nbBytes = opt.getNumber("bytes");
        } else {
            nbLines = 10; // default
        }

        List<String> args = opt.args();
        if (args.isEmpty()) {
            args = Collections.singletonList("-");
        }

        boolean first = true;
        List<NamedInputStream> sources = getSources(context, argv, args);
        for (NamedInputStream nis : sources) {
            boolean filenameHeader = sources.size() > 1;
            if (opt.isSet("verbose")) {
                filenameHeader = true;
            } else if (opt.isSet("quiet")) {
                filenameHeader = false;
            }
            if (filenameHeader) {
                if (!first) {
                    context.out().println();
                }
                context.out().println("==> " + nis.getName() + " <==");
            }
            doHead(context, nis.getInputStream(), nbLines, nbBytes);
            first = false;
        }
    }

    private static void doHead(Context context, InputStream is, final int nbLines, final int nbBytes) throws IOException {
        if (nbLines != Integer.MAX_VALUE) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < nbLines) {
                    context.out().println(line);
                    count++;
                }
            }
        } else {
            byte[] buffer = new byte[nbBytes];
            int bytesRead = is.read(buffer);
            if (bytesRead > 0) {
                context.out().write(buffer, 0, bytesRead);
            }
            is.close();
        }
    }

    public static void tail(Context context, Object[] argv) throws Exception {
        final String[] usage = {
            "/tail - display last lines of files or variables",
            "Usage: /tail [-n lines | -c bytes] [-q | -v] [file|variable ...]",
            "  -? --help                    Show help",
            "  -n --lines=LINES             Number of lines to print",
            "  -c --bytes=BYTES             Number of bytes to print",
            "  -q --quiet                   Never output filename headers",
            "  -v --verbose                 Always output filename headers",
        };
        Options opt = parseOptions(context, usage, argv);

        if (opt.isSet("lines") && opt.isSet("bytes")) {
            throw new IllegalArgumentException("usage: /tail [-c # | -n #] [-q | -v] [file|variable ...]");
        }

        if (opt.isSet("quiet") && opt.isSet("verbose")) {
            throw new IllegalArgumentException("usage: /tail [-c # | -n #] [-q | -v] [file|variable ...]");
        }

        int lines = opt.isSet("lines") ? opt.getNumber("lines") : 10;
        int bytes = opt.isSet("bytes") ? opt.getNumber("bytes") : -1;

        List<String> args = opt.args();
        if (args.isEmpty()) {
            args = Collections.singletonList("-");
        }

        List<NamedInputStream> sources = getSources(context, argv, args);
        boolean filenameHeader = sources.size() > 1;
        if (opt.isSet("verbose")) {
            filenameHeader = true;
        } else if (opt.isSet("quiet")) {
            filenameHeader = false;
        }
        for (NamedInputStream nis : sources) {
            if (filenameHeader) {
                context.out().println("==> " + nis.getName() + " <==");
            }
            tailInputStream(context, nis.getInputStream(), lines, bytes);
        }
    }

    private static void tailInputStream(Context context, InputStream is, int lines, int bytes) throws IOException {
        if (bytes > 0) {
            // Read all and keep last bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = is.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            byte[] data = baos.toByteArray();
            int start = Math.max(0, data.length - bytes);
            context.out().write(data, start, data.length - start);
        } else {
            // Read all and keep last lines
            List<String> allLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    allLines.add(line);
                }
            }
            int start = Math.max(0, allLines.size() - lines);
            for (int i = start; i < allLines.size(); i++) {
                context.out().println(allLines.get(i));
            }
        }
    }

    public static void ls(Context context, Object[] argv) throws Exception {
        final String[] usage = {
            "/ls - list files",
            "Usage: /ls [OPTIONS] [PATTERNS...]",
            "  -? --help                show help",
            "  -1                       list one entry per line",
            "  -C                       multi-column output",
            "     --color=WHEN          colorize the output, may be `always', `never' or `auto'",
            "  -a                       list entries starting with .",
            "  -F                       append file type indicators",
            "  -m                       comma separated",
            "  -l                       long listing",
            "  -S                       sort by size",
            "  -f                       output is not sorted",
            "  -r                       reverse sort order",
            "  -t                       sort by modification time",
            "  -x                       sort horizontally",
            "  -L                       list referenced file for links",
            "  -h                       print sizes in human readable form"
        };
        Options opt = parseOptions(context, usage, argv);

        Map<String, String> colorMap = getLsColorMap(context);

        String color = opt.isSet("color") ? opt.get("color") : "auto";
        boolean colored;
        switch (color) {
            case "always":
            case "yes":
            case "force":
                colored = true;
                break;
            case "never":
            case "no":
            case "none":
                colored = false;
                break;
            case "auto":
            case "tty":
            case "if-tty":
                colored = context.isTty();
                break;
            default:
                throw new IllegalArgumentException("invalid argument '" + color + "' for '--color'");
        }
        Map<String, String> colors =
            colored ? (colorMap != null ? colorMap : getLsColorMap(DEFAULT_LS_COLORS)) : Collections.emptyMap();

        class PathEntry implements Comparable<PathEntry> {
            final Path abs;
            final Path path;
            final Map<String, Object> attributes;

            public PathEntry(Path abs, Path root) {
                this.abs = abs;
                this.path = getPath(abs, root);
                this.attributes = readAttributes(abs);
            }

            private Path getPath(Path abs, Path root) {
                try {
                    return Files.isSameFile(abs, root)
                        ? Paths.get(".")
                        : abs.startsWith(root) ? root.relativize(abs) : abs;
                } catch (IOException ignore) {
                    return abs;
                }
            }

            @Override
            public int compareTo(PathEntry o) {
                int c = doCompare(o);
                return opt.isSet("r") ? -c : c;
            }

            private int doCompare(PathEntry o) {
                if (opt.isSet("f")) {
                    return -1;
                }
                if (opt.isSet("S")) {
                    long s0 = attributes.get("size") != null ? ((Number) attributes.get("size")).longValue() : 0L;
                    long s1 = o.attributes.get("size") != null ? ((Number) o.attributes.get("size")).longValue() : 0L;
                    return s0 > s1 ? -1 : s0 < s1 ? 1 : path.toString().compareTo(o.path.toString());
                }
                if (opt.isSet("t")) {
                    long t0 = attributes.get("lastModifiedTime") != null
                        ? ((FileTime) attributes.get("lastModifiedTime")).toMillis()
                        : 0L;
                    long t1 = o.attributes.get("lastModifiedTime") != null
                        ? ((FileTime) o.attributes.get("lastModifiedTime")).toMillis()
                        : 0L;
                    return t0 > t1 ? -1 : t0 < t1 ? 1 : path.toString().compareTo(o.path.toString());
                }
                return path.toString().compareTo(o.path.toString());
            }

            boolean isNotDirectory() {
                return is("isRegularFile") || is("isSymbolicLink") || is("isOther");
            }

            boolean isDirectory() {
                return is("isDirectory");
            }

            private boolean is(String attr) {
                Object d = attributes.get(attr);
                return d instanceof Boolean && (Boolean) d;
            }

            String display() {
                String type;
                String suffix;
                String link = "";
                if (is("isSymbolicLink")) {
                    type = "sl";
                    suffix = "@";
                    try {
                        Path l = Files.readSymbolicLink(abs);
                        link = " -> " + l.toString();
                    } catch (IOException e) {
                        // ignore
                    }
                } else if (is("isDirectory")) {
                    type = "dr";
                    suffix = "/";
                } else if (is("isExecutable")) {
                    type = "ex";
                    suffix = "*";
                } else if (is("isOther")) {
                    type = "ot";
                    suffix = "";
                } else {
                    type = "";
                    suffix = "";
                }
                boolean addSuffix = opt.isSet("F");
                return applyStyle(path.toString(), colors, type) + (addSuffix ? suffix : "") + link;
            }

            String longDisplay() {
                StringBuilder username;
                if (attributes.containsKey("owner")) {
                    username = new StringBuilder(Objects.toString(attributes.get("owner"), null));
                } else {
                    username = new StringBuilder("owner");
                }
                if (username.length() > 8) {
                    username = new StringBuilder(username.substring(0, 8));
                } else {
                    for (int i = username.length(); i < 8; i++) {
                        username.append(" ");
                    }
                }
                StringBuilder group;
                if (attributes.containsKey("group")) {
                    group = new StringBuilder(Objects.toString(attributes.get("group"), null));
                } else {
                    group = new StringBuilder("group");
                }
                if (group.length() > 8) {
                    group = new StringBuilder(group.substring(0, 8));
                } else {
                    for (int i = group.length(); i < 8; i++) {
                        group.append(" ");
                    }
                }
                Number length = (Number) attributes.get("size");
                if (length == null) {
                    length = 0L;
                }
                String lengthString;
                if (opt.isSet("h")) {
                    double l = length.longValue();
                    String unit = "B";
                    if (l >= 1000) {
                        l /= 1024;
                        unit = "K";
                        if (l >= 1000) {
                            l /= 1024;
                            unit = "M";
                            if (l >= 1000) {
                                l /= 1024;
                                unit = "T";
                            }
                        }
                    }
                    if (l < 10 && length.longValue() > 1000) {
                        lengthString = String.format("%.1f", l) + unit;
                    } else {
                        lengthString = String.format("%3.0f", l) + unit;
                    }
                } else {
                    lengthString = String.format("%1$8s", length);
                }
                @SuppressWarnings("unchecked")
                Set<PosixFilePermission> perms = (Set<PosixFilePermission>) attributes.get("permissions");
                if (perms == null) {
                    perms = EnumSet.noneOf(PosixFilePermission.class);
                }
                // TODO: all fields should be padded to align
                return (is("isDirectory") ? "d" : (is("isSymbolicLink") ? "l" : (is("isOther") ? "o" : "-")))
                    + PosixFilePermissions.toString(perms) + " "
                    + String.format(
                    "%3s",
                    (attributes.containsKey("nlink")
                        ? attributes.get("nlink").toString()
                        : "1"))
                    + " " + username + " " + group + " " + lengthString + " "
                    + toString((FileTime) attributes.get("lastModifiedTime"))
                    + " " + display();
            }

            protected String toString(FileTime time) {
                long millis = (time != null) ? time.toMillis() : -1L;
                if (millis < 0L) {
                    return "------------";
                }
                ZonedDateTime dt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault());
                // Less than six months
                if (System.currentTimeMillis() - millis < 183L * 24L * 60L * 60L * 1000L) {
                    return DateTimeFormatter.ofPattern("MMM ppd HH:mm").format(dt);
                }
                // Older than six months
                else {
                    return DateTimeFormatter.ofPattern("MMM ppd  yyyy").format(dt);
                }
            }

            protected Map<String, Object> readAttributes(Path path) {
                Map<String, Object> attrs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                for (String view : path.getFileSystem().supportedFileAttributeViews()) {
                    try {
                        Map<String, Object> ta =
                            Files.readAttributes(path, view + ":*", getLinkOptions(opt.isSet("L")));
                        ta.forEach(attrs::putIfAbsent);
                    } catch (IOException e) {
                        // Ignore
                    }
                }
                attrs.computeIfAbsent("isExecutable", s -> Files.isExecutable(path));
                attrs.computeIfAbsent("permissions", s -> getPermissionsFromFile(path));
                return attrs;
            }
        }

        Path currentDir = context.currentDir();
        // Listing
        List<Path> expanded = new ArrayList<>();
        if (opt.args().isEmpty()) {
            expanded.add(currentDir);
        } else {
            opt.args().stream().flatMap(s -> maybeExpandGlob(context, s)).forEach(expanded::add);
        }
        boolean listAll = opt.isSet("a");
        Predicate<Path> filter = p -> listAll
            || p.getFileName() == null // root
            || p.getFileName().toString().equals(".")
            || p.getFileName().toString().equals("..")
            || !p.getFileName().toString().startsWith(".");
        List<PathEntry> all = expanded.stream()
            .filter(filter)
            .map(p -> new PathEntry(p, currentDir))
            .sorted()
            .collect(Collectors.toList());
        // Print files first
        List<PathEntry> files = all.stream().filter(PathEntry::isNotDirectory).collect(Collectors.toList());
        PrintStream out = context.out();
        Consumer<Stream<PathEntry>> display = s -> {
            boolean optLine = opt.isSet("1");
            boolean optComma = opt.isSet("m");
            boolean optLong = opt.isSet("l");
            boolean optCol = opt.isSet("C");
            if (!optLine && !optComma && !optLong && !optCol) {
                if (context.isTty()) {
                    optCol = true;
                } else {
                    optLine = true;
                }
            }
            // One entry per line
            if (optLine) {
                s.map(PathEntry::display).forEach(out::println);
            }
            // Comma separated list
            else if (optComma) {
                out.println(s.map(PathEntry::display).collect(Collectors.joining(", ")));
            }
            // Long listing
            else if (optLong) {
                s.map(PathEntry::longDisplay).forEach(out::println);
            }
            // Column listing
            else if (optCol) {
                toColumn(context, out, s.map(PathEntry::display), opt.isSet("x"));
            }
        };
        boolean space = false;
        if (!files.isEmpty()) {
            display.accept(files.stream());
            space = true;
        }
        // Print directories
        List<PathEntry> directories =
            all.stream().filter(PathEntry::isDirectory).collect(Collectors.toList());
        for (PathEntry entry : directories) {
            if (space) {
                out.println();
            }
            space = true;
            Path path = currentDir.resolve(entry.path);
            if (expanded.size() > 1) {
                out.println(currentDir.relativize(path).toString() + ":");
            }
            try (Stream<Path> pathStream = Files.list(path)) {
                display.accept(Stream.concat(Stream.of(".", "..").map(path::resolve), pathStream)
                    .filter(filter)
                    .map(p -> new PathEntry(p, path))
                    .sorted());
            }
        }
    }

    private static void toColumn(Context context, PrintStream out, Stream<String> ansi, boolean horizontal) {
        Terminal terminal = context.terminal();
        int width = context.isTty() ? terminal.getWidth() : 80;
        List<AttributedString> strings = ansi.map(AttributedString::fromAnsi).collect(Collectors.toList());
        if (!strings.isEmpty()) {
            int max = strings.stream()
                .mapToInt(AttributedString::columnLength)
                .max()
                .getAsInt();
            int c = Math.max(1, width / max);
            while (c > 1 && c * max + (c - 1) >= width) {
                c--;
            }
            int columns = c;
            int lines = (strings.size() + columns - 1) / columns;
            IntBinaryOperator index;
            if (horizontal) {
                index = (i, j) -> i * columns + j;
            } else {
                index = (i, j) -> j * lines + i;
            }
            AttributedStringBuilder sb = new AttributedStringBuilder();
            for (int i = 0; i < lines; i++) {
                for (int j = 0; j < columns; j++) {
                    int idx = index.applyAsInt(i, j);
                    if (idx < strings.size()) {
                        AttributedString str = strings.get(idx);
                        boolean hasRightItem = j < columns - 1 && index.applyAsInt(i, j + 1) < strings.size();
                        sb.append(str);
                        if (hasRightItem) {
                            for (int k = 0; k <= max - str.length(); k++) {
                                sb.append(' ');
                            }
                        }
                    }
                }
                sb.append('\n');
            }
            out.print(sb.toAnsi(terminal));
        }
    }

    public static void grep(Context context, Object[] argv) throws Exception {
        final String[] usage = {
            "/grep -  search for PATTERN in each FILE or VARIABLE or standard input.",
            "Usage: /grep [OPTIONS] PATTERN [FILE|VARIABLE ...]",
            "  -? --help                Show help",
            "  -i --ignore-case         Ignore case distinctions",
            "  -n --line-number         Prefix each line with line number within its input file",
            "  -q --quiet, --silent     Suppress all normal output",
            "  -v --invert-match        Select non-matching lines",
            "  -w --word-regexp         Select only whole words",
            "  -x --line-regexp         Select only whole lines",
            "  -c --count               Only print a count of matching lines per file",
            "  -z --zero                When used with -c, don't print a count of zero",
            "  -H --with-filename       Display filename header for each file (defaults to true if multiple files are given)",
            "  -h --no-filename         Do not display filename header",
            "     --color=WHEN          Use markers to distinguish the matching string, may be `always', `never' or `auto'",
            "  -B --before-context=NUM  Print NUM lines of leading context before matching lines",
            "  -A --after-context=NUM   Print NUM lines of trailing context after matching lines",
            "  -C --context=NUM         Print NUM lines of output context",
            "     --pad-lines           Pad line numbers"
        };
        Options opt = parseOptions(context, usage, argv);

        Map<String, String> colorMap = getColorMap(context, "GREP", DEFAULT_GREP_COLORS);

        List<String> args = opt.args();
        if (args.isEmpty()) {
            throw new IllegalArgumentException("no pattern supplied");
        }

        String regex = args.remove(0);
        String regexp = regex;
        if (opt.isSet("word-regexp")) {
            regexp = "\\b" + regexp + "\\b";
        }
        if (opt.isSet("line-regexp")) {
            regexp = "^" + regexp + "$";
        } else {
            regexp = ".*" + regexp + ".*";
        }
        Pattern p;
        Pattern p2;
        if (opt.isSet("ignore-case")) {
            p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            p2 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } else {
            p = Pattern.compile(regexp);
            p2 = Pattern.compile(regex);
        }
        int after = opt.isSet("after-context") ? opt.getNumber("after-context") : -1;
        int before = opt.isSet("before-context") ? opt.getNumber("before-context") : -1;
        int contextLines = opt.isSet("context") ? opt.getNumber("context") : 0;
        String lineFmt = opt.isSet("pad-lines") ? "%6d" : "%d";
        if (after < 0) {
            after = contextLines;
        }
        if (before < 0) {
            before = contextLines;
        }
        boolean count = opt.isSet("count");
        boolean zero = opt.isSet("zero");
        boolean quiet = opt.isSet("quiet");
        boolean invert = opt.isSet("invert-match");
        boolean lineNumber = opt.isSet("line-number");
        String color = opt.isSet("color") ? opt.get("color") : "auto";
        boolean colored;
        switch (color) {
            case "always":
            case "yes":
            case "force":
                colored = true;
                break;
            case "never":
            case "no":
            case "none":
                colored = false;
                break;
            case "auto":
            case "tty":
            case "if-tty":
                colored = context.isTty();
                break;
            default:
                throw new IllegalArgumentException("invalid argument '" + color + "' for '--color'");
        }
        Map<String, String> colors =
            colored ? (colorMap != null ? colorMap : getColorMap(DEFAULT_GREP_COLORS)) : Collections.emptyMap();

        if (args.isEmpty()) {
            args.add("-");
        }
        List<NamedInputStream> sources = getSources(context, argv, args);
        boolean filenameHeader = sources.size() > 1;
        if (opt.isSet("with-filename")) {
            filenameHeader = true;
        } else if (opt.isSet("no-filename")) {
            filenameHeader = false;
        }
        boolean match = false;
        for (NamedInputStream src : sources) {
            List<String> lines = new ArrayList<>();
            boolean firstPrint = true;
            int nb = 0;
            try (InputStream is = src.getInputStream()) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    int lineno = 1;
                    int lineMatch = 0;
                    while ((line = r.readLine()) != null) {
                        boolean matches = p.matcher(line).matches();
                        if (invert) {
                            matches = !matches;
                        }
                        AttributedStringBuilder sbl = new AttributedStringBuilder();
                        if (matches) {
                            nb++;
                            if (!count && !quiet) {
                                if (filenameHeader) {
                                    if (colored) {
                                        applyStyle(sbl, colors, "fn");
                                    }
                                    sbl.append(src.getName());
                                    if (colored) {
                                        applyStyle(sbl, colors, "se");
                                    }
                                    sbl.append(":");
                                }
                                if (lineNumber) {
                                    if (colored) {
                                        applyStyle(sbl, colors, "ln");
                                    }
                                    sbl.append(String.format(lineFmt, lineno));
                                    if (colored) {
                                        applyStyle(sbl, colors, "se");
                                    }
                                    sbl.append(":");
                                }
                                if (colored) {
                                    Matcher matcher2 = p2.matcher(line);
                                    int cur = 0;
                                    while (matcher2.find()) {
                                        applyStyle(sbl, colors, "se");
                                        sbl.append(line, cur, matcher2.start());
                                        applyStyle(sbl, colors, "ms");
                                        sbl.append(line, matcher2.start(), matcher2.end());
                                        applyStyle(sbl, colors, "se");
                                        cur = matcher2.end();
                                    }
                                    sbl.append(line, cur, line.length());
                                } else {
                                    sbl.append(line);
                                }
                                while (lineMatch > after && !lines.isEmpty()) {
                                    context.out().println(lines.remove(0));
                                    lineMatch--;
                                }
                                lineMatch = Math.min(before, lines.size()) + after + 1;
                            }
                        } else if (lineMatch > 0) {
                            context.out().println(lines.remove(0));
                            lineMatch--;
                            if (filenameHeader) {
                                if (colored) {
                                    applyStyle(sbl, colors, "fn");
                                }
                                sbl.append(src.getName());
                                if (colored) {
                                    applyStyle(sbl, colors, "se");
                                }
                                sbl.append("-");
                            }
                            if (lineNumber) {
                                if (colored) {
                                    applyStyle(sbl, colors, "ln");
                                }
                                sbl.append(String.format(lineFmt, lineno));
                                if (colored) {
                                    applyStyle(sbl, colors, "se");
                                }
                                sbl.append("-");
                            }
                            if (colored) {
                                applyStyle(sbl, colors, "se");
                            }
                            sbl.append(line);
                        } else {
                            if (filenameHeader) {
                                if (colored) {
                                    applyStyle(sbl, colors, "fn");
                                }
                                sbl.append(src.getName());
                                if (colored) {
                                    applyStyle(sbl, colors, "se");
                                }
                                sbl.append("-");
                            }
                            if (lineNumber) {
                                if (colored) {
                                    applyStyle(sbl, colors, "ln");
                                }
                                sbl.append(String.format(lineFmt, lineno));
                                if (colored) {
                                    applyStyle(sbl, colors, "se");
                                }
                                sbl.append("-");
                            }
                            if (colored) {
                                applyStyle(sbl, colors, "se");
                            }
                            sbl.append(line);
                            while (lines.size() > before) {
                                lines.remove(0);
                            }
                            lineMatch = 0;
                        }
                        lines.add(sbl.toAnsi(context.terminal()));
                        while (lineMatch == 0 && lines.size() > before) {
                            lines.remove(0);
                        }
                        lineno++;
                    }
                    if (!count && lineMatch > 0) {
                        if (!firstPrint && before + after > 0) {
                            AttributedStringBuilder sbl2 = new AttributedStringBuilder();
                            if (colored) {
                                applyStyle(sbl2, colors, "se");
                            }
                            sbl2.append("--");
                            context.out().println(sbl2.toAnsi(context.terminal()));
                        } else {
                            firstPrint = false;
                        }
                        for (int i = 0; i < lineMatch && i < lines.size(); i++) {
                            context.out().println(lines.get(i));
                        }
                    }
                    if (count) {
                        if (nb != 0 || !zero) {
                            AttributedStringBuilder sbl = new AttributedStringBuilder();
                            if (filenameHeader) {
                                if (colored) {
                                    applyStyle(sbl, colors, "fn");
                                }
                                sbl.append(src.getName());
                                if (colored) {
                                    applyStyle(sbl, colors, "se");
                                }
                                sbl.append(":");
                            }
                            sbl.append(Integer.toString(nb));
                            context.out().println(sbl.toAnsi(context.terminal()));
                        }
                    }
                    match |= nb > 0;
                }
                context.out().flush();
            }
        }
    }

    public static void sort(Context context, Object[] argv) throws Exception {
        final String[] usage = {
            "/sort -  writes sorted standard input to standard output.",
            "Usage: /sort [OPTIONS] [FILES]",
            "  -? --help                    show help",
            "  -f --ignore-case             fold lower case to upper case characters",
            "  -r --reverse                 reverse the result of comparisons",
            "  -u --unique                  output only the first of an equal run",
            "  -t --field-separator=SEP     use SEP instead of non-blank to blank transition",
            "  -b --ignore-leading-blanks   ignore leading blanks",
            "     --numeric-sort            compare according to string numerical value",
            "  -k --key=KEY                 fields to use for sorting separated by whitespaces"
        };

        Options opt = parseOptions(context, usage, argv);

        List<String> args = opt.args();
        if (args.isEmpty()) {
            args = Collections.singletonList("-");
        }
        List<NamedInputStream> sources = getSources(context, argv, args);
        List<String> lines = new ArrayList<>();
        for (NamedInputStream s : sources) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                readLines(reader, lines);
            }
        }

        String separator = opt.get("field-separator");
        boolean caseInsensitive = opt.isSet("ignore-case");
        boolean reverse = opt.isSet("reverse");
        boolean ignoreBlanks = opt.isSet("ignore-leading-blanks");
        boolean numeric = opt.isSet("numeric-sort");
        boolean unique = opt.isSet("unique");
        List<String> sortFields = opt.getList("key");

        char sep = (separator == null || separator.length() == 0) ? '\0' : separator.charAt(0);
        lines.sort(new SortComparator(caseInsensitive, reverse, ignoreBlanks, numeric, sep, sortFields));
        String last = null;
        for (String s : lines) {
            if (!unique || last == null || !s.equals(last)) {
                context.out().println(s);
            }
            last = s;
        }
    }

    public static void less(Context context, String[] argv) throws Exception {
        Options opt = parseOptions(context, Less.usage(), argv);

        List<Source> sources = new ArrayList<>();
        if (opt.args().isEmpty()) {
            opt.args().add("-");
        }
        for (String arg : opt.args()) {
            if ("-".equals(arg)) {
                sources.add(new Source.StdInSource(context.in()));
            } else {
                maybeExpandGlob(context, arg)
                    .map(p -> {
                        try {
                            return new Source.URLSource(p.toUri().toURL(), p.toString());
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(sources::add);
            }
        }

        if (!context.isTty()) {
            // Non-interactive mode - just cat the files
            for (Source source : sources) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(source.read()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        context.out().println(line);
                    }
                }
            }
            return;
        }

        Less less = new Less(context.terminal(), context.currentDir(), opt);
        less.run(sources);
    }

    private static class NamedInputStream implements Closeable {
        private InputStream inputStream;
        private final Path path;
        private final String name;
        private final boolean close;

        public NamedInputStream(InputStream inputStream, String name, boolean close) {
            this.inputStream = inputStream;
            this.path = null;
            this.name = name;
            this.close = close;
        }

        public NamedInputStream(InputStream inputStream, String name) {
            this(inputStream, name, true);
        }
        public NamedInputStream(Path path, String name) {
            this.inputStream = null;
            this.path = path;
            this.name = name;
            this.close = false;
        }

        public InputStream getInputStream() throws IOException {
            if (inputStream == null) {
                inputStream = path.toUri().toURL().openStream();
            }
            return inputStream;
        }

        public String getName() {
            return name;
        }

        @Override
        public void close() throws IOException {
            if (inputStream != null && close) {
                inputStream.close();
            }
        }
    }

    private static List<NamedInputStream> getSources(Context context, Object[] argv, List<String> args) {
        List<NamedInputStream> sources = new ArrayList<>();
        for (String arg : args) {
            if ("-".equals(arg)) {
                sources.add(new NamedInputStream(context.in(), "(standard input)", false));
            } else if (arg.startsWith("[Ljava.lang.String;@")) {
                sources.add(new NamedInputStream(variableInputStream(argv, arg), arg));
            } else {
                sources.addAll(maybeExpandGlob(context, arg)
                    .map(p -> new NamedInputStream(p, p.toString()))
                    .collect(Collectors.toList()));
            }
        }
        return sources;
    }

    private static ByteArrayInputStream variableInputStream(Object[] argv, String arg) {
        String[] found = (String[]) Arrays.stream(argv).filter(v -> v.toString().equals(arg)).findFirst().get();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ArrayGroovyMethods.join(found, "\n").getBytes(StandardCharsets.UTF_8));
        return inputStream;
    }

    private static LinkOption[] getLinkOptions(boolean followLinks) {
        if (followLinks) {
            return EMPTY_LINK_OPTIONS;
        } else { // return a clone that modifications to the array will not affect others
            return NO_FOLLOW_OPTIONS.clone();
        }
    }

    private static final LinkOption[] NO_FOLLOW_OPTIONS = new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
    private static final LinkOption[] EMPTY_LINK_OPTIONS = new LinkOption[0];
    private static final List<String> WINDOWS_EXECUTABLE_EXTENSIONS =
        Collections.unmodifiableList(Arrays.asList(".bat", ".exe", ".cmd"));

    private static boolean isWindowsExecutable(String fileName) {
        if ((fileName == null) || (fileName.length() <= 0)) {
            return false;
        }
        for (String suffix : WINDOWS_EXECUTABLE_EXTENSIONS) {
            if (fileName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static Set<PosixFilePermission> getPermissionsFromFile(Path f) {
        Set<PosixFilePermission> perms = new HashSet<>();
        try {
            perms = Files.getPosixFilePermissions(f);
        } catch (IOException | UnsupportedOperationException ignore) {
        }
        if (OSUtils.IS_WINDOWS && isWindowsExecutable(f.getFileName().toString())) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return perms;
    }

    private static void readLines(BufferedReader reader, List<String> lines) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
    }

    private static Stream<Path> maybeExpandGlob(Context context, String s) {
        if (s.contains("*") || s.contains("?")) {
            return expandGlob(context, s).stream();
        }
        return Stream.of(context.currentDir().resolve(s));
    }

    private static List<Path> expandGlob(Context context, String pattern) {
        // try Ant for globbing if on classpath
        try {
            Class<?> directoryScannerClass = Class.forName("org.apache.tools.ant.DirectoryScanner");
            Object scanner = directoryScannerClass.getDeclaredConstructor().newInstance();
            List<Path> result = new ArrayList<>();

            // Use reflection to invoke methods on the DirectoryScanner
            Method setBasedirMethod = directoryScannerClass.getMethod("setBasedir", File.class);
            setBasedirMethod.invoke(scanner, context.currentDir().toFile());

            Method setIncludesMethod = directoryScannerClass.getMethod("setIncludes", String[].class);
            setIncludesMethod.invoke(scanner, (Object) new String[]{pattern});

            Method scanMethod = directoryScannerClass.getMethod("scan");
            scanMethod.invoke(scanner);

            Method getIncludedFilesMethod = directoryScannerClass.getMethod("getIncludedFiles");
            String[] includedFiles = (String[]) getIncludedFilesMethod.invoke(scanner);

            for (String file : includedFiles) {
                result.add(Path.of(file));
            }

            Method getIncludedDirsMethod = directoryScannerClass.getMethod("getIncludedDirectories");
            String[] includedDirs = (String[]) getIncludedDirsMethod.invoke(scanner);

            for (String dir : includedDirs) {
                result.add(Path.of(dir));
            }
            return result;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ignore) {
        }

        Path path = Path.of(pattern);

        Path base;
        String globPart;

        if (path.isAbsolute()) {
            base = path.getParent();
            globPart = path.getFileName().toString();
        } else {
            base = context.currentDir();
            globPart = pattern;
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPart);

        try {
            return Files.list(base)
                .filter(p -> matcher.matches(p.getFileName()))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
