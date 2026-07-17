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
package groovy.util.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A node in the tree of balanced groups extracted by {@link #find},
 * also exposed on {@link CharSequence} via
 * {@link org.codehaus.groovy.runtime.StringGroovyMethods#findBalancedGroups(CharSequence, String, String)}.
 * <p>
 * Java's {@link Pattern} has no .NET-style balancing groups
 * ({@code (?<name1-name2>…)}). This type is both the structured result and the
 * entry point for Groovy's stack-based equivalent: each node is one successfully
 * closed span, with immediately nested spans as children (richer than .NET's flat
 * {@code CaptureCollection} on a single group).
 * </p>
 * <p>
 * Offsets mirror .NET {@code Capture.Index} / {@code Length}: {@link #getStart()}
 * and {@link #getEnd()} describe the range of {@link #getMatchedString()} in the
 * original input (half-open {@code [start, end)}). {@link #getFullStart()} /
 * {@link #getFullEnd()} always cover the complete pair including delimiters,
 * even when {@link MatchOptions#includeEdges()} is {@code false} (comparable to
 * knowing both the balancing capture and the open/close match positions).
 * </p>
 * <p>
 * Parent links and nesting depth are wired when a node is attached as a child
 * of another node during construction; root nodes have a {@code null} parent
 * and depth {@code 0}. Prefer {@link #find} (or the GDK methods on
 * {@link CharSequence}) as the public entry point — constructors are
 * package-private because they perform one-shot parent wiring.
 * </p>
 *
 * @since 6.0.0
 */
public final class BalancedGroup {

    /**
     * Bounded LRU cache for compiled tokenizer patterns. Hot call sites often
     * reuse the same open/close/ignore triple; recompiling those on every call
     * dominates cost for short inputs.
     */
    private static final int TOKENIZER_CACHE_LIMIT = 64;

    private static final Map<String, Pattern> TOKENIZER_CACHE =
        new LinkedHashMap<>(TOKENIZER_CACHE_LIMIT, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
                return size() > TOKENIZER_CACHE_LIMIT;
            }
        };

    private final String matchedString;
    private final int start;
    private final int end;
    private final int fullStart;
    private final int fullEnd;
    private final List<BalancedGroup> children;
    private BalancedGroup parent;
    /** Nesting depth; set once when this node is attached as a child (O(1) {@link #getDepth()}). */
    private int depth;

    /**
     * Package-private constructor: builds a node with offsets relative to
     * {@code matchedString} ({@code start = 0}, {@code end = matchedString.length()},
     * and the same for the full span). Prefer {@link #find} for public use.
     * Wires each child's parent link (one-shot: a child may not already have a parent).
     *
     * @param matchedString the text captured for this group (never {@code null})
     * @param children      immediate nested groups, or {@code null}/empty for a leaf
     * @throws NullPointerException     if {@code matchedString} is {@code null}, or
     *                                  {@code children} contains {@code null}
     * @throws IllegalArgumentException if any child already has a parent
     */
    BalancedGroup(String matchedString, List<BalancedGroup> children) {
        this(matchedString, 0, lengthOf(matchedString), 0, lengthOf(matchedString), children);
    }

    /**
     * Package-private constructor: builds a node with absolute source offsets and children.
     * Prefer {@link #find} for public use. Wires each child's parent link and depth
     * (one-shot: a child may not already have a parent).
     *
     * @param matchedString the text captured for this group (never {@code null});
     *                      may include or exclude boundary delimiters depending on
     *                      {@link MatchOptions#includeEdges()}
     * @param start         start index of {@code matchedString} in the source (inclusive)
     * @param end           end index of {@code matchedString} in the source (exclusive)
     * @param fullStart     start index of the full pair including open delimiter
     * @param fullEnd       end index of the full pair including close delimiter (exclusive)
     * @param children      immediate nested groups, or {@code null}/empty for a leaf
     * @throws NullPointerException     if {@code matchedString} is {@code null}, or
     *                                  {@code children} contains {@code null}
     * @throws IllegalArgumentException if ranges are invalid or a child already has a parent
     */
    BalancedGroup(String matchedString, int start, int end, int fullStart, int fullEnd,
                  List<BalancedGroup> children) {
        this.matchedString = Objects.requireNonNull(matchedString, "matchedString");
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("invalid match range: [" + start + ", " + end + ")");
        }
        if (fullStart < 0 || fullEnd < fullStart) {
            throw new IllegalArgumentException("invalid full range: [" + fullStart + ", " + fullEnd + ")");
        }
        if (end - start != matchedString.length()) {
            throw new IllegalArgumentException(
                "matchedString length " + matchedString.length() + " != range length " + (end - start));
        }
        this.start = start;
        this.end = end;
        this.fullStart = fullStart;
        this.fullEnd = fullEnd;
        if (children == null || children.isEmpty()) {
            this.children = List.of();
        } else {
            this.children = List.copyOf(children);
            for (BalancedGroup child : this.children) {
                if (child.parent != null) {
                    throw new IllegalArgumentException("child BalancedGroup already has a parent");
                }
                child.parent = this;
                // Bottom-up assembly: this node may later be attached under a grandparent,
                // so depth is assigned (and cascaded) here and again when we are attached.
                child.assignDepth(this.depth + 1);
            }
        }
    }

    /**
     * Sets this node's depth and cascades to descendants. Invoked only during the
     * one-shot parent-wiring phase of construction (tree is not yet published).
     */
    private void assignDepth(int newDepth) {
        this.depth = newDepth;
        for (BalancedGroup child : children) {
            child.assignDepth(newDepth + 1);
        }
    }

    private static int lengthOf(String s) {
        return s == null ? 0 : s.length();
    }

    /**
     * Finds balanced groups using {@link MatchOptions#defaults()}.
     *
     * @param text       text to scan
     * @param openRegex  regex for an opening delimiter (must not be empty)
     * @param closeRegex regex for a closing delimiter (must not be empty)
     * @return unmodifiable list of outermost groups (empty if none)
     * @see #find(CharSequence, String, String, MatchOptions)
     * @since 6.0.0
     */
    public static List<BalancedGroup> find(CharSequence text, String openRegex, String closeRegex) {
        return find(text, openRegex, closeRegex, MatchOptions.defaults());
    }

    /**
     * Finds balanced (nested) groups in {@code text} and returns them as a forest of
     * {@link BalancedGroup} nodes.
     * <p>
     * <b>Relation to .NET balancing groups:</b> .NET keeps a capture stack per
     * named group, pushes with {@code (?&lt;Open&gt;…)}, pops with
     * {@code (?&lt;-Open&gt;…)} or {@code (?&lt;Between-Open&gt;…)}, and can require a
     * fully empty stack via {@code (?(Open)(?!))}. This method uses the same
     * push/pop idea on an explicit stack, but returns a hierarchical tree
     * (children + parent) rather than a flat {@code CaptureCollection}, and
     * always extracts every completed span in one left-to-right scan.
     * </p>
     * <p>
     * <b>Algorithm:</b> matches of a combined tokenizer
     * ({@code IGNORE}? / {@code OPEN} / {@code CLOSE}) are consumed in order.
     * Nesting is resolved by the stack, not by recursive regex, so balancing
     * itself does not introduce ReDoS; cost is proportional to the number of
     * tokenizer hits times the cost of the user-supplied patterns. Compiled
     * tokenizers are cached (bounded LRU).
     * </p>
     * <p>
     * <b>Fault tolerance</b> (differs from a strict .NET
     * {@code (?(Open)(?!))} pattern): unmatched closers are ignored; unclosed
     * openers are dropped at end-of-input, but groups already completed inside
     * them are promoted outward ("orphan rescue").
     * </p>
     * <p>
     * Named capturing groups {@code OPEN}, {@code CLOSE}, and {@code IGNORE} are
     * reserved for the tokenizer; do not use those names inside
     * {@code openRegex}, {@code closeRegex}, or {@link MatchOptions#ignoreRegex()}.
     * </p>
     *
     * @param text       text to scan
     * @param openRegex  regex for an opening delimiter (must not be empty)
     * @param closeRegex regex for a closing delimiter (must not be empty)
     * @param options    match options; {@code null} means {@link MatchOptions#defaults()}
     * @return unmodifiable list of outermost groups (empty if none)
     * @throws NullPointerException     if {@code text}, {@code openRegex}, or {@code closeRegex} is {@code null}
     * @throws IllegalArgumentException if {@code openRegex} or {@code closeRegex} is empty
     * @throws java.util.regex.PatternSyntaxException if a supplied pattern is not a valid Java regex
     * @since 6.0.0
     */
    public static List<BalancedGroup> find(CharSequence text, String openRegex, String closeRegex,
                                           MatchOptions options) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(openRegex, "openRegex must not be null");
        Objects.requireNonNull(closeRegex, "closeRegex must not be null");
        if (openRegex.isEmpty()) {
            throw new IllegalArgumentException("openRegex must not be empty");
        }
        if (closeRegex.isEmpty()) {
            throw new IllegalArgumentException("closeRegex must not be empty");
        }

        MatchOptions actualOptions = options != null ? options : MatchOptions.defaults();
        String ignoreRegex = actualOptions.ignoreRegex();
        boolean hasIgnoreRule = ignoreRegex != null && !ignoreRegex.isBlank();
        boolean includeEdges = actualOptions.includeEdges();

        // Single String source: Matcher indices and substring share the same coordinate space;
        // avoids repeated CharSequence.subSequence materialization for non-String inputs.
        final String source = text instanceof String ? (String) text : text.toString();

        Matcher matcher = tokenizer(openRegex, closeRegex, hasIgnoreRule ? ignoreRegex : null).matcher(source);

        // Virtual super-root so top-level groups need no empty-stack special case.
        Deque<PendingGroup> stack = new ArrayDeque<>();
        stack.push(new PendingGroup(-1, -1));

        while (matcher.find()) {
            if (hasIgnoreRule && matcher.start("IGNORE") >= 0) {
                continue;
            }

            if (matcher.start("OPEN") >= 0) {
                stack.push(new PendingGroup(matcher.start(), matcher.end()));
            } else if (matcher.start("CLOSE") >= 0 && stack.size() > 1) {
                PendingGroup openGroup = stack.pop();
                int openStart = openGroup.openStart;
                int openEnd = openGroup.openEnd;
                int closeStart = matcher.start();
                int closeEnd = matcher.end();

                int matchStart = includeEdges ? openStart : openEnd;
                int matchEnd = includeEdges ? closeEnd : closeStart;
                // Pathological open/close patterns can invert the interior range.
                if (matchStart > matchEnd) {
                    matchStart = matchEnd;
                }
                String matchStr = source.substring(matchStart, matchEnd);

                // Constructor wires child → parent links.
                BalancedGroup completedNode = new BalancedGroup(
                    matchStr, matchStart, matchEnd, openStart, closeEnd, openGroup.children());
                stack.peek().addChild(completedNode);
            }
        }

        // Promote completed children out of any still-open (dangling) frames.
        while (stack.size() > 1) {
            PendingGroup dangling = stack.pop();
            stack.peek().addAllChildren(dangling);
        }

        return stack.peek().snapshotChildren();
    }

    /**
     * Builds or reuses the tokenizer pattern. Alternation order is significant:
     * IGNORE wins over OPEN, OPEN over CLOSE (same idea as putting more specific
     * alternatives first in a hand-written .NET balancing pattern).
     */
    private static Pattern tokenizer(String openRegex, String closeRegex, String ignoreRegex) {
        String key;
        String pattern;
        if (ignoreRegex != null) {
            key = ignoreRegex + "\0" + openRegex + "\0" + closeRegex;
            pattern = "(?<IGNORE>" + ignoreRegex + ")|(?<OPEN>" + openRegex + ")|(?<CLOSE>" + closeRegex + ")";
        } else {
            key = openRegex + "\0" + closeRegex;
            pattern = "(?<OPEN>" + openRegex + ")|(?<CLOSE>" + closeRegex + ")";
        }
        synchronized (TOKENIZER_CACHE) {
            Pattern cached = TOKENIZER_CACHE.get(key);
            if (cached != null) {
                return cached;
            }
            Pattern compiled = Pattern.compile(pattern);
            TOKENIZER_CACHE.put(key, compiled);
            return compiled;
        }
    }

    /**
     * Returns the text captured for this group.
     *
     * @return the matched text (never {@code null}); may include or exclude boundary
     *         delimiters depending on {@link MatchOptions#includeEdges()}
     */
    public String getMatchedString() {
        return matchedString;
    }

    /**
     * Start index of {@link #getMatchedString()} in the original input (inclusive).
     * Comparable to .NET {@code Capture.Index}.
     *
     * @return the start offset
     */
    public int getStart() {
        return start;
    }

    /**
     * End index of {@link #getMatchedString()} in the original input (exclusive).
     * Length is {@code getEnd() - getStart()}, like .NET {@code Capture.Length}.
     *
     * @return the end offset
     */
    public int getEnd() {
        return end;
    }

    /**
     * Length of {@link #getMatchedString()} ({@code end - start}).
     *
     * @return the length
     */
    public int getLength() {
        return end - start;
    }

    /**
     * Start index of the full balanced pair, always including the opening delimiter.
     *
     * @return the full-span start offset
     */
    public int getFullStart() {
        return fullStart;
    }

    /**
     * End index of the full balanced pair, always including the closing delimiter (exclusive).
     *
     * @return the full-span end offset
     */
    public int getFullEnd() {
        return fullEnd;
    }

    /**
     * Returns the immediately nested balanced groups.
     *
     * @return an unmodifiable list of children; empty (never {@code null}) for a leaf
     */
    public List<BalancedGroup> getChildren() {
        return children;
    }

    /**
     * Returns the enclosing group, if any.
     *
     * @return the parent node, or {@code null} if this is a root
     */
    public BalancedGroup getParent() {
        return parent;
    }

    /**
     * Nesting depth of this node (0 for a root).
     * Computed once when the parent link is wired; O(1).
     *
     * @return the number of ancestors
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the matched text of this group.
     *
     * @return the same value as {@link #getMatchedString()}
     */
    @Override
    public String toString() {
        return matchedString;
    }

    /**
     * Frame for an as-yet unmatched opening delimiter during the scan.
     * Child lists are allocated lazily: most frames are leaves.
     */
    private static final class PendingGroup {
        final int openStart;
        final int openEnd;
        private ArrayList<BalancedGroup> completedChildren;

        PendingGroup(int openStart, int openEnd) {
            this.openStart = openStart;
            this.openEnd = openEnd;
        }

        void addChild(BalancedGroup child) {
            if (completedChildren == null) {
                completedChildren = new ArrayList<>(2);
            }
            completedChildren.add(child);
        }

        void addAllChildren(PendingGroup other) {
            if (other.completedChildren == null || other.completedChildren.isEmpty()) {
                return;
            }
            if (completedChildren == null) {
                completedChildren = other.completedChildren;
                other.completedChildren = null;
            } else {
                completedChildren.addAll(other.completedChildren);
            }
        }

        List<BalancedGroup> children() {
            return completedChildren == null ? List.of() : completedChildren;
        }

        List<BalancedGroup> snapshotChildren() {
            if (completedChildren == null || completedChildren.isEmpty()) {
                return List.of();
            }
            return List.copyOf(completedChildren);
        }
    }

    /**
     * Options controlling balanced-group matching.
     * <p>
     * Instances are immutable; use the {@code with*} methods to derive variants.
     * </p>
     *
     * @param ignoreRegex  regex for spans to skip while scanning (e.g. string literals
     *                     or escape sequences); {@code null} or blank means ignore nothing.
     *                     This is the practical counterpart to writing “neither open nor
     *                     close” character classes in a hand-built .NET balancing pattern.
     * @param includeEdges {@code true} to keep opening/closing delimiters in
     *                     {@link BalancedGroup#getMatchedString()}; {@code false} to strip
     *                     them (like .NET's balancing capture of the interval between
     *                     the subtracted open capture and the close match)
     *
     * @since 6.0.0
     */
    public record MatchOptions(String ignoreRegex, boolean includeEdges) {

        /**
         * Default options: nothing ignored, delimiters included.
         *
         * @return the default options
         */
        public static MatchOptions defaults() {
            return new MatchOptions(null, true);
        }

        /**
         * Returns a copy with the given ignore regex.
         *
         * @param ignoreRegex regex of content to skip, or {@code null}/blank for none
         * @return a new options instance
         */
        public MatchOptions withIgnoreRegex(String ignoreRegex) {
            return new MatchOptions(ignoreRegex, this.includeEdges);
        }

        /**
         * Returns a copy with the given edge-inclusion policy.
         *
         * @param includeEdges whether matched text keeps opening/closing delimiters
         * @return a new options instance
         */
        public MatchOptions withIncludeEdges(boolean includeEdges) {
            return new MatchOptions(this.ignoreRegex, includeEdges);
        }
    }
}
