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
/**
 * Refer to pleac.sourceforge.net if wanting accurate comparisons with PERL.
 * Original author has included tweaked examples here solely for the purposes
 * of exercising the Groovy compiler.
 * In some instances, examples have been modified to avoid additional
 * dependencies or for dependencies not in common repos.
 */

// @@PLEAC@@_5.0
//----------------------------------------------------------------------------------
// quotes are optional around the key
age = [ Nat:24, Jules:25, Josh:17 ]

assert age['Nat']  == 24
// alternate syntax
assert age."Jules" == 25

foodColor = [
    Apple:  'red',
    Banana: 'yellow',
    Lemon:  'yellow',
    Carrot: 'orange'
]
assert foodColor.size() == 4
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.1
//----------------------------------------------------------------------------------
foodColor['Lemon'] = 'green'
assert foodColor.size() == 4
assert foodColor['Lemon'] == 'green'
foodColor['Raspberry'] = 'pink'
assert foodColor.size() == 5
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.2
//----------------------------------------------------------------------------------
assert ['Banana', 'Martini'].collect{ foodColor.containsKey(it)?'food':'drink' } == [ 'food', 'drink' ]

age = [Toddler:3, Unborn:0, Phantasm:null]
['Toddler', 'Unborn', 'Phantasm', 'Relic'].each{ key ->
    print "$key: "
    if (age.containsKey(key)) print 'has key '
    if (age.containsKey(key) && age[key]!=null) print 'non-null '
    if (age.containsKey(key) && age[key]) print 'true '
    println ''
}
// =>
// Toddler: has key non-null true
// Unborn: has key non-null
// Phantasm: has key
// Relic:
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.3
//----------------------------------------------------------------------------------
assert foodColor.size() == 5
foodColor.remove('Banana')
assert foodColor.size() == 4
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.4
//----------------------------------------------------------------------------------
hash = [:]
hash.each { key, value ->
    // do something with key and value
}

hash.each { entry ->
    // do something with entry
}

hash.keySet().each { key ->
    // do something with key
}

sb = new StringBuffer()
foodColor.each { food, color ->
    sb << "$food is $color\n"
}
assert '\n' + sb.toString() == '''
Lemon is green
Carrot is orange
Apple is red
Raspberry is pink
'''

foodColor.each { entry ->
    assert entry.key.size() > 4 && entry.value.size() > 2
}

foodColorsSortedByFood = []
foodColor.keySet().sort().each { k -> foodColorsSortedByFood << foodColor[k] }
assert foodColorsSortedByFood == ["red", "orange", "green", "pink"]

fakedInput = '''
From: someone@somewhere.com
From: someone@spam.com
From: someone@somewhere.com
'''

from = [:]
fakedInput.split('\n').each{
    matcher = (it =~ /^From:\s+([^\s>]*)/)
    if (matcher.matches()) {
        sender = matcher[0][1]
        if (from.containsKey(sender)) from[sender] += 1
        else from[sender] = 1
    }
}

// More useful to sort by number of received mail by person
from.entrySet().sort { a,b -> b.value<=>a.value}.each { e->
    println "${e.key}: ${e.value}"
}
// =>
// someone@somewhere.com: 2
// someone@spam.com: 1
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.5
//----------------------------------------------------------------------------------
hash = [a:1, b:2, c:3]
// Map#toString already produce a pretty decent output:
println hash
// => ["b":2, "a":1, "c":3]

// Or do it by longhand for customised formatting
hash.each { k,v -> println "$k => $v" }
// =>
// b => 2
// a => 1
// c => 3
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.6
//----------------------------------------------------------------------------------
// java.util.LinkedHashMap "maintains a doubly-linked list running through all of its entries.
// This linked list defines the iteration ordering, which is normally the order in which keys
// were inserted into the map (insertion-order)".
foodColor = new LinkedHashMap()
foodColor['Banana'] = 'Yellow'
foodColor['Apple'] = 'Green'
foodColor['Lemon'] = 'Yellow'

foodColor.keySet().each{ key -> println key }
// =>
// Banana
// Apple
// Lemon
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.7
//----------------------------------------------------------------------------------
foodsOfColor = [ Yellow:['Banana', 'Lemon'], Green:['Apple'] ]
foodsOfColor['Green'] += 'Melon'
assert foodsOfColor == ["Green":["Apple", "Melon"], "Yellow":["Banana", "Lemon"]]
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.8
//----------------------------------------------------------------------------------
surname = [Mickey: 'Mantle', Babe: 'Ruth']
assert surname.findAll{ it.value == 'Mantle' }.collect{ it.key } == ["Mickey"]

firstname = [:]
surname.each{ entry -> firstname[entry.value] = entry.key }
assert firstname == ["Ruth":"Babe", "Mantle":"Mickey"]

// foodfindScript:
//#!/usr/bin/groovy
// usage: foodfind food_or_color"
color = [Apple:'red', Banana:'yellow', Lemon:'yellow', Carrot:'orange']
given = args[0]
if (color.containsKey(given))
    println "$given is a food with color ${color[given]}."
if (color.containsValue(given)) {
    // could use commify() here - see 4.2
    foods = color.findAll{it.value == given}.collect{it.key}
    join = foods.size() == 1 ? 'is a food' : 'are foods'
    println "${foods.join(', ')} $join with color ${given}."
}
// foodfind red
// => Apple is a food with color red.
// foodfind yellow
// => Lemon, Banana are foods with color yellow.
// foodfind Carrot
// => Carrot is a food with color orange.
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.9
//----------------------------------------------------------------------------------
foodColor = [Apple:'red', Carrot:'orange', Banana:'yellow', Cherry:'black']

// Sorted by keys
assert foodColor.keySet().sort() == ["Apple", "Banana", "Carrot", "Cherry"]
// you could now iterate through the hash with the sorted keys
assert foodColor.values().sort() == ["black", "orange", "red", "yellow"]
assert foodColor.values().sort{it.size()} == ["red", "black", "orange", "yellow"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.10
//----------------------------------------------------------------------------------
//merged = a.clone.update(b)        # because Hash#update changes object in place

drinkColor = [Galliano:'yellow', 'Mai Tai':'blue']
ingestedColor = [:]
ingestedColor.putAll(drinkColor)
// overrides any common keys
ingestedColor.putAll(foodColor)

totalColors = ingestedColor.values().sort().unique()
assert totalColors == ["black", "blue", "orange", "red", "yellow"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.11
//----------------------------------------------------------------------------------
foodColor['Lemon']='yellow'
citrusColor = [Lemon:'yellow', Orange:'orange', Lime:'green']
println foodColor
println citrusColor
common = foodColor.keySet().intersect(citrusColor.keySet())
assert common == ["Lemon"]

foodButNotCitrus = foodColor.keySet().toList() - citrusColor.keySet().toList()
assert foodButNotCitrus == ["Carrot", "Apple", "Banana", "Cherry"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.12
//----------------------------------------------------------------------------------
// no problem here, Groovy handles any kind of object for key-ing
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.13
//----------------------------------------------------------------------------------
// Groovy uses Java implementations for storing hashes and these
// support setting an initial capacity and load factor (which determines
// at what point the hash will be resized if needed)
hash = [:]                              // Groovy shorthand gets defaults
hash = new HashMap()                    // default capacity and load factor
println hash.capacity()
// => 16
('A'..'Z').each{ hash[it] = it }
println hash.capacity()
// => 64
hash = new HashMap(100)                 // initial capacity of 100 and default load factor
hash = new HashMap(100, 0.8f)    // initial capacity of 100 and 0.8 load factor
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.14
//----------------------------------------------------------------------------------
count = [:]
letters = []
foodColor.values().each{ letters.addAll((it as String[]).toList()) }
letters.each{ if (count.containsKey(it)) count[it] += 1 else count[it] = 1 }
assert count == ["o":3, "d":1, "k":1, "w":2, "r":2, "c":1, "l":5, "g":1, "b":1, "a":2, "y":2, "n":1, "e":4]
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.15
//----------------------------------------------------------------------------------
father = [
    Cain:'Adam',
    Abel:'Adam',
    Seth:'Adam',
    Enoch:'Cain',
    Irad:'Enoch',
    Mehujael:'Irad',
    Methusael:'Mehujael',
    Lamech:'Methusael',
    Jabal:'Lamech',
    Jubal:'Lamech',
    Tubalcain:'Lamech',
    Enos:'Seth'
]

def upline(person) {
    while (father.containsKey(person)) {
        print person + ' '
        person = father[person]
    }
    println person
}

upline('Irad')
// => Irad Enoch Cain Adam

children = [:]
father.each { k,v ->
    if (!children.containsKey(v)) children[v] = []
    children[v] += k
}
def downline(person) {
    println "$person begat ${children.containsKey(person)?children[person].join(', '):'Nobody'}.\n"
}
downline('Tubalcain')
// => Tubalcain begat Nobody.
downline('Adam')
// => Adam begat Abel, Seth, Cain.

// This one doesn't recurse through subdirectories (as a simplification)
// scriptToFindIncludeFilesWhichContainNoIncludesScript:
dir = '<path_to_usr/include>'
includes = [:]
new File(dir).eachFile{ file ->
    if (file.directory) return
    file.eachLine{ line ->
        matcher = (line =~ '^\\s*#\\s*include\\s*<([^>]+)>')
        if (matcher.matches()) {
            if (!includes.containsKey(file.name)) includes[file.name] = []
            includes[file.name] += matcher[0][1]
        }
    }
}
// find referenced files which have no includes; assumes all files
// were processed and none are missing
println includes.values().sort().flatten().unique() - includes.keySet()
//----------------------------------------------------------------------------------

// @@PLEAC@@_5.16
//----------------------------------------------------------------------------------
// dutree - print sorted indented rendition of du output
// obtaining this input is not shown, it is similar to other examples
// on some unix systems it will be: duProcessFakedInput = "du options".process().text
duProcessFakedInput = '''
11732   groovysoap/lib
68      groovysoap/src/main/groovy/net/soap
71      groovysoap/src/main/groovy/net
74      groovysoap/src/main/groovy
77      groovysoap/src/main
9       groovysoap/src/examples
8       groovysoap/src/examples/groovy
102     groovysoap/src/test
202     groovysoap/src
11966   groovysoap
'''

// The DuNode class collects all information about a directory,
class DuNode {
    def name
    def size
    def kids = []

    // support for sorting nodes with side
    def compareTo(node2) { size <=> node2.size }

    def getBasename() {
        name.replaceAll(/.*\//, '')
    }

    // returns substring before last "/", otherwise null
    def getParent() {
        def p = name.replaceAll(/\/[^\/]+$/,'')
        return (p == name) ? null : p
    }
}

// The DuTree does the actual work of
// getting the input, parsing it, building up a tree
// and formatting it for output
class DuTree {
    def input
    def topdir
    def nodes = [:]
    def dirsizes = [:]
    def kids = [:]

    // get a node by name, create it if it does not exist yet
    def getOrCreateNode(name) {
        if (!nodes.containsKey(name))
            nodes[name] = new DuNode(name:name)
        return nodes[name]
    }

    // figure out how much is taken in each directory
    // that isn't stored in the subdirectories. Add a new
    // fake kid called "." containing that much.
    def getDots(node) {
        def cursize = node.size
        for (kid in node.kids) {
            cursize -=  kid.size
            getDots(kid)
        }
        if (node.size != cursize) {
            def newnode = getOrCreateNode(node.name + "/.")
            newnode.size = cursize
            node.kids += newnode
        }
    }

    def processInput() {
        def name = ''
        input.split('\n').findAll{it.trim()}.each{ line ->
            def tokens = line.tokenize()
            def size = tokens[0]
            name = tokens[1]
            def node = getOrCreateNode(name)
            node.size = size.toInteger()
            nodes[name] = node
            def parent = node.parent
            if (parent)
                getOrCreateNode(parent).kids << node
        }
        topdir = nodes[name]
    }

    // recursively output everything
    // passing padding and number width as well
    // on recursive calls
    def output(node, prefix='', width=0) {
        def line = node.size.toString().padRight(width) + ' ' + node.basename
        println (prefix + line)
        prefix += line.replaceAll(/\d /, '| ')
        prefix = prefix.replaceAll(/[^|]/, ' ')
        if (node.kids.size() > 0) {    // not a bachelor node
            kids = node.kids
            kids.sort{ a,b -> b.compareTo(a) }
            width = kids[0].size.toString().size()
            for (kid in kids) output(kid, prefix, width)
        }
    }
}

tree = new DuTree(input:duProcessFakedInput)
tree.processInput()
tree.getDots(tree.topdir)
tree.output(tree.topdir)
// =>
// 11966 groovysoap
//     |           11732 lib
//     |           202   src
//     |             |      102 test
//     |             |      77  main
//     |             |       |      74 groovy
//     |             |       |       |       71 net
//     |             |       |       |        |    68 soap
//     |             |       |       |        |    3  .
//     |             |       |       |       3  .
//     |             |       |      3  .
//     |             |      14  .
//     |             |      9   examples
//     |             |      |           8 groovy
//     |             |      |           1 .
//     |           32    .
//----------------------------------------------------------------------------------


// @@PLEAC@@_6.0
//----------------------------------------------------------------------------------
// Groovy has built-in language support for Regular Expressions:
// *  Strings quoted with '/' characters have special escaping
//    rules for backslashes and the like.
// *  ~string (regex pattern operator)
// *  m =~ /pattern/ (regex find operator)
// *  m ==~/pattern/ (regex match operator)
// *  patterns can be used in case expressions in a switch statement
// *  string.replaceAll can take a closure expression as the second argument
// In addition, Groovy can make use of Java's Pattern, Matcher and Scanner classes
// directly. (The sugar coating metnioed above sits on top of these anyway).
// There are also additional open source Java regex libraries which can be used.

meadow1 = 'cow grass butterflies Ovine'
meadow2 = 'goat sheep flowers dog'
// pattern strings can benefit from 'slashy' quotes
partial = /sheep/
full = /.*sheep.*/

// find operator
assert !(meadow1 =~ partial)
assert meadow2 =~ partial
finder = (meadow2 =~ partial)
// underneath Groovy sugar coating is Java implementation
assert finder instanceof java.util.regex.Matcher

// match operator
assert !(meadow1 ==~ full)
assert meadow2 ==~ full
matcher = (meadow2 ==~ full)
// under the covers is just a boolean
assert matcher instanceof Boolean

assert meadow1 =~ /(?i)\bovines?\b/ // (?i) == case flag

string = 'good food'
println string.replaceFirst(/o*/, 'e')
// => egood food
println string.replaceAll(/o*/, 'e')
// => egeede efeede (global)
// beware this one is just textual replacement
println string.replace(/o*/, 'e')
// => good food
println 'o*o*'.replace(/o*/, 'e')
// => ee

// groovy -e "m = args[0] =~ /(a|ba|b)+(a|ac)+/; if (m.matches()) println m[0][0]" ababacaca
// => ababa

digits = "123456789"
nonlap = digits =~ /\d\d\d/
assert nonlap.count == 3
print 'Non-overlapping:  '
(0..<nonlap.count).each{ print nonlap[it] + ' ' }; print '\n'
print 'Overlapping:      '
yeslap = (digits =~ /(?=(\d\d\d))/)
assert yeslap.count == 7
(0..<yeslap.count).each{ print yeslap[it][1] + ' ' }; print '\n'
// Non-overlapping:  123 456 789
// Overlapping:      123 234 345 456 567 678 789

string = 'And little lambs eat ivy'
// Greedy version
parts = string =~ /(.*)(l[^s]*s)(.*)/
(1..parts.groupCount()).each{ print "(${parts[0][it]}) " }; print '\n'
// (And little ) (lambs) ( eat ivy)

// Reluctant version
parts = string =~ /(.*?)(l[^s]*s)(.*)/
(1..parts.groupCount()).each{ print "(${parts[0][it]}) " }; print '\n'
// (And ) (little lambs) ( eat ivy)
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.1
//----------------------------------------------------------------------------------
// Groovy splits src and dest to avoid this problem
src = 'Go this way'
dst = src.replaceFirst('this', 'that')
assert dst == 'Go that way'

// extract basename
src = 'c:/some/path/file.ext'
dst = src.replaceFirst('^.*/', '')
assert dst == 'file.ext'

// Make All Words Title-Cased (not that you would do it this way)
//  The preprocessing operations \X where X is one of l, u, L, and U are not supported
// in the sun regex library but other Java regex libraries may support this. Instead:
src = 'make all words title-cased'
dst = src
('a'..'z').each{ dst = dst.replaceAll(/([^a-zA-Z])/+it+/|\A/+it, /$1/+it.toUpperCase()) }
assert dst == 'Make All Words Title-Cased'

// rename list of dirs
bindirs = '/usr/bin /bin /usr/local/bin'.split(' ').toList()
expected = '/usr/lib /lib /usr/local/lib'.split(' ').toList()
libdirs = bindirs.collect { dir -> dir.replaceFirst('bin', 'lib') }
assert libdirs == expected
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.2
//----------------------------------------------------------------------------------
// Groovy uses Java regex (other Java regex packages would also be possible)
// It doesn't support Locale-based settings but you can roll your own to some
// extent, you can use any Unicode characters as per below and you can use
// \p{Punct}    Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
// or the other special character classes
words = '''
silly
fa�ade
co�perate
ni�o
Ren�e
Moli�re
h�moglobin
na�ve
tsch��
random!stuff#here\u0948
'''
results = ''
greekAlpha = '\u0391'
special = '���������?' + greekAlpha
// flag as either Y (alphabetic) or N (not)
words.split('\n').findAll{it.trim()}.each{ results += it ==~ /^[\w/+special+/]+$/ ?'Y':'N' }
assert results == 'YYYYYYYYYN'
results = ''
words.split('\n').findAll{it.trim()}.each{ results += it ==~ /^[^\p{Punct}]+$/ ?'Y':'N' }
assert results == 'YYYYYYYYYN'
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.3
//----------------------------------------------------------------------------------
// as many non-whitespace bytes as possible
finder = 'abczqz z' =~ /a\S+z/
assert finder[0] == 'abczqz'

// as many letters, apostrophes, and hyphens
finder = "aAzZ'z-z0z" =~ /a[A-Za-z'-]+z/          //'
assert finder[0] == "aAzZ'z-z"

// selecting words
finder = '23rd Psalm' =~ /\b([A-Za-z]+)\b/   // usually best
println finder[0][0]
// => Psalm (23rd is not matched)
finder = '23rd Psalm' =~ /\s([A-Za-z]+)\s/   // fails at ends or w/ punctuation
println finder.matches()
// => false (no whitespaces at ends)
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.4
//----------------------------------------------------------------------------------
str = 'groovy-lang.org and www.aboutgroovy.com'
re = '''(?x)          # to enable whitespace and comments
      (               # capture the hostname in $1
        (?:           # these parens for grouping only
          (?! [-_] )  # lookahead for neither underscore nor dash
          [\\w-] +    # hostname component
          \\.         # and the domain dot
        ) +           # now repeat that whole thing a bunch of times
        [A-Za-z]      # next must be a letter
        [\\w-] +      # now trailing domain part
      )               # end of $1 capture
     '''

finder = str =~ re
out = str
(0..<finder.count).each{
    adr = finder[it][0]
    out = out.replaceAll(adr, "$adr [${InetAddress.getByName(adr).hostAddress}]")
}
println out
// => groovy-lang.org [63.246.7.187] and www.aboutgroovy.com [63.246.7.76]

// to match whitespace or #-characters in an extended re you need to escape them.
foo = 42
str = 'blah #foo# blah'
re = '''(?x)         # to enable whitespace and comments
              \\#    # a pound sign
              (\\w+) # the variable name
              \\#    # another pound sign
     '''
finder = str =~ re
found = finder[0]
out = str.replaceAll(found[0], evaluate(found[1]).toString())
assert out == 'blah 42 blah'
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.5
//----------------------------------------------------------------------------------
fish = 'One fish two fish red fish blue fish'
expected = 'The third fish is a red one.'
thirdFish = /(?:\w+\s+fish\s+){2}(\w+)\s+fish.*/
assert expected == (fish.replaceAll(thirdFish, 'The third fish is a $1 one.'))

anyFish = /(\w+)\s+fish\b/
finder = fish =~ anyFish
// finder contains an array of matched groups
// 2 = third one (index start at 0), 1 = matched word in group
out = "The third fish is a ${finder[2][1]} one."
assert out == expected

evens = []
(0..<finder.count).findAll{it%2!=0}.each{ evens += finder[it][1] }
println "Even numbered fish are ${evens.join(' ')}."
// => Even numbered fish are two blue.

// one of several ways to do this
pond = fish + ' in the pond'
fishInPond = (/(\w+)(\s+fish\b\s*)/) * 4 + /(.*)/
found = (pond =~ fishInPond)[0]
println ((found[1..6] + 'sushi' + found[8..9]).join())
// => One fish two fish red fish sushi fish in the pond

// find last fish
expected = 'Last fish is blue'
pond = 'One fish two fish red fish blue fish swim here.'
finder = (pond =~ anyFish)
assert expected == "Last fish is ${finder[finder.count-1][1]}"
// => Last fish is blue

// greedy match version of above
finder = (pond =~ /.*\b/ + anyFish)
assert expected == "Last fish is ${finder[0][1]}"

// last fish match version of above
finder = (pond =~ /\b(\w+)\s+fish\b(?!.*\bfish\b)/)
assert expected == "Last fish is ${finder[0][1]}"
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.6
//----------------------------------------------------------------------------------
// Html Stripper
// get this using: fakedfile = new File('path_to_file.htm').text
fakedFile = '''
<html>
<head><title>Chapter 1 Title</title></head>
<body>
<h1>Chapter 1: Some Heading</h1>
A paragraph.
</body>
</html>
'''

stripExpectations = '''
Chapter 1 Title

Chapter 1: Some Heading
A paragraph.
'''.trim()

stripped = fakedFile.replaceAll(/(?m)<.*?>/,'').trim()
assert stripExpectations == stripped

pattern = '''(?x)
      (                    # capture in $1
          Chapter          # text string
          \\s+             # mandatory whitespace
          \\d+             # decimal number
          \\s*             # optional whitespace
          :                # a real colon
          . *              # anything not a newline till end of line
      )
'''

headerfyExpectations = '''
Chapter 1 Title

<H1>Chapter 1: Some Heading</H1>
A paragraph.
'''.trim()

headerfied = stripped.replaceAll(pattern, '<H1>$1</H1>')
assert headerfyExpectations == headerfied

// one liner equivalent which prints to stdout
//% groovy -p -e "line.replaceAll(/^(Chapter\s+\d+\s*:.*)/,'<H1>$1</H1>')"

// one liner equivalent which modifies file in place and creates *.bak original file
//% groovy -pi .bak -e "line.replaceAll(/^(Chapter\s+\d+\s*:.*)/,'<H1>$1</H1>')"

// use: realFileInput = new File(path_to_file).text
fakeFileInput = '''
0
START
1
2
END
3
4
5
START
6
END
'''

chunkyPattern = /(?ms)^START(.*?)^END/
finder = fakeFileInput =~ chunkyPattern
(0..<finder.count).each {
    println "Chunk #$it contains ${new StringTokenizer(finder[it][1],'\n').countTokens()} lines."
}
// =>
// Chunk #0 contains 2 lines.
// Chunk #1 contains 1 lines.
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.7
//----------------------------------------------------------------------------------
// general pattern is:
//file = new File("datafile").text.split(/pattern/)
// .Ch, .Se and .Ss divide chunks of input text
fakedFiletext = '''
.Ch
abc
.Se
def
.Ss
ghi
.Se
jkl
.Se
mno
.Ss
pqr
.Ch
stu
.Ch
vwx
.Se
yz!
'''
chunks = fakedFiletext.split(/(?m)^\.(Ch|Se|Ss)$/)
println "I read ${chunks.size()} chunks."
// => I read 10 chunks.
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.8
//----------------------------------------------------------------------------------
// Groovy doesn't support the ~/BEGIN/ .. ~/END/ notation
// you have to emulate it as shown in the example below
// The from line number to line number processing is supported
// from the command line but not within a script, e.g.
// command-line to print lines 15 through 17 inclusive (see below)
// > groovy -p -e "if (count in 15..17) return line" datafile
// Within a script itself, you emulate the count by keeping state

htmlContent = '''
<h1>A Heading</h1>
Here is <XMP>inline AAA</XMP>.
And the bigger Example 2:
<XMP>
line BBB
line CCC
</XMP>
Done.
'''.trim()

examplePattern = /(?ms)<XMP>(.*?)<\/XMP>/
finder = htmlContent =~ examplePattern
(0..<finder.count).each {
    println "Example ${it+1}:"
    println finder[it][1]
}
// =>
// Example 1:
// inline AAA
// Example 2:
//
// line BBB
// line CCC
//

htmlContent.split('\n').eachWithIndex{ line, count ->
    if (count in 4..5) println line
}
// =>
// line BBB
// line CCC

// You would probably use a mail Api for this in Groovy
fakedMailInput = '''
From: A Person <someone@somewhere.com>
To: <pleac-discuss@lists.sourceforge.net>
Date: Sun, 31 Dec 2006 02:14:57 +1100

From: noone@nowhere.com
To: <pleac-discuss@lists.sourceforge.net>
Date: Sun, 31 Dec 2006 02:14:58 +1100

From: someone@somewhere.com
To: <pleac-discuss@lists.sourceforge.net>
Date: Sun, 31 Dec 2006 02:14:59 +1100
'''.trim()+'\n'

seen = [:]
fakedMailInput.split('\n').each{ line ->
    m = (line =~ /^From:?\s(.*)/)
    if (m) {
        addr = m[0][1] =~ /([^<>(),;\s]+\@[^<>(),;\s]+)/
        x = addr[0][1]
        if (seen.containsKey(x)) seen[x] += 1 else seen[x] = 1
    }
}
seen.each{ k,v -> println "Address $k seen $v time${v==1?'':'s'}." }
// =>
// Address noone@nowhere.com seen 1 time.
// Address someone@somewhere.com seen 2 times.
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.9
//----------------------------------------------------------------------------------
import java.util.regex.Pattern

names = '''
myFile.txt
oldFile.tex
myPicture.jpg
'''

def glob2pat(globstr) {
    def patmap = [ '*':'.*', '?':'.', '[':'[', ']':']' ]
    def result = '(?m)^'
    '^' + globstr.replaceAll(/(.)/) { all, c ->
        result += (patmap.containsKey(c) ? patmap[c] : Pattern.quote(c))
    }
     result + '$'
}

def checkNumMatches(pat, count) {
    assert (names =~ glob2pat(pat)).count == count
}

checkNumMatches('*.*', 3)
checkNumMatches('my*.*', 2)
checkNumMatches('*.t*', 2)
checkNumMatches('*File.*', 2)
checkNumMatches('*Rabbit*.*', 0)
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.10
//----------------------------------------------------------------------------------
// version 1: simple obvious way
states = 'CO ON MI WI MN'.split(' ').toList()

def popgrep1(file) {
    file.eachLine{ line ->
        if (states.any{ line =~ /\b$it\b/ }) println line
    }
}
// popgrep1(new File('path_to_file'))

// version 2: eval strings; fast but hard to quote (SLOW)
def popgrep2(file) {
    def code = 'def found = false\n'
    states.each{
        code += "if (!found && line =~ /\\b$it\\b/) found = true\n"
    }
    code += "if (found) println line\n"
    file.eachLine{ line = it; evaluate(code) }
}
// popgrep2(new File('path_to_file'))

// version 2b: eval using switch/case (not in Perl cookbook) (SLOW)
def popgrep2b(file) {
    def code = 'switch(line) {\n'
    states.each{
        code += "case ~/.*\\b$it\\b.*/:\nprintln line;break\n"
    }
    code += "default:break\n}\n"
    file.eachLine{ line = it; evaluate(code) }
}
// popgrep2b(new File('path_to_file'))

// version3: build a match_any function as a GString
def popgrep3(file) {
    def code = states.collect{ "line =~ /\\b$it\\b/" }.join('||')
    file.eachLine{ line = it; if (evaluate(code)) println line }
}
// popgrep3(new File('path_to_file'))

// version4: pretty fast, but simple: compile all re's first:
patterns = states.collect{ ~/\b$it\b/ }
def popgrep4(file) {
    file.eachLine{ line ->
        if (patterns.any{ it.matcher(line)}) println line
    }
}
// popgrep4(new File('path_to_file'))

// version5: faster
str = states.collect{ /\b$it\b/ }.join('|')
def popgrep5(file) {
    file.eachLine{ line ->
        if (line =~ str) println line
    }
}
// popgrep5(new File('path_to_file'))

// version5b: faster (like 5 but compiled outside loop)
pattern = ~states.collect{ /\b$it\b/ }.join('|')
def popgrep5b(file) {
    file.eachLine{ line ->
        if (pattern.matcher(line)) println line
    }
}
// popgrep5b(new File('path_to_file'))

// speeds trials ON the current source file (~1200 lines)
// popgrep1   =>  0.39s
// popgrep2   => 25.08s
// popgrep2b  => 23.86s
// popgrep3   => 22.42s
// popgrep4   =>  0.12s
// popgrep5   =>  0.05s
// popgrep5b  =>  0.05s
// Groovy's built-in support is the way to go in terms of
// both speed and simplicity of understanding. Avoid using
// evaluate() unless you absolutely need it

// generic matching functions
input = '''
both cat and dog
neither
just a cat
just a dog
'''.split('\n').findAll{it.trim()}

def matchAny(line, patterns) { patterns.any{ line =~ it } }
def matchAll(line, patterns) { patterns.every{ line =~ it } }

assert input.findAll{ matchAny(it, ['cat','dog']) }.size() == 3
assert input.findAll{ matchAny(it, ['cat$','^n.*']) }.size() == 2
assert input.findAll{ matchAll(it, ['cat','dog']) }.size() == 1
assert input.findAll{ matchAll(it, ['cat$','^n.*']) }.size() == 0
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.11
//----------------------------------------------------------------------------------
// patternCheckingScript:
prompt = '\n> '
print 'Enter patterns to check:' + prompt
new BufferedReader(new InputStreamReader(System.in)).eachLine{ line ->
    try {
        Pattern.compile(line)
        print 'Valid' + prompt
    } catch (java.util.regex.PatternSyntaxException ex) {
        print 'Invalid pattern: ' + ex.message + prompt
    }
}
// =>
// Enter patterns to check:
// > ab*.c
// Valid
// > ^\s+[^a-z]*$
// Valid
// > **
// Invalid pattern: Dangling meta character '*' near index 0
// **
// ^
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.12
//----------------------------------------------------------------------------------
src = 'dierk k�nig'
// simplistic with locale issue
dst = src
('a'..'z').each{ dst = dst.replaceAll(/(?<=[^a-zA-Z])/+it+/|\A/+it, it.toUpperCase()) }
println dst
// => Dierk K�Nig
// locale avoidance
dst = src
('a'..'z').each{ dst = dst.replaceAll(/(?<=\A|\b)/+it, it.toUpperCase()) }
println dst
// => Dierk K�nig
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.13
//----------------------------------------------------------------------------------
// Several libraries exist, e.g.
// http://secondstring.sourceforge.net/
// http://sourceforge.net/projects/simmetrics/
// both support numerous algorithms. Using the second as an example:
class Levenshtein{}
class MongeElkan{}
class JaroWinkler{}
class Soundex{}
//import uk.ac.shef.wit.simmetrics.similaritymetrics.*
target = 'balast'
candidates = '''
quick
brown
fox
jumped
over
the
lazy
dog
ballast
ballasts
balustrade
balustrades
blast
blasted
blaster
blasters
blasting
blasts
'''.split('\n').findAll{it.trim()}
metrics = [new Levenshtein(), new MongeElkan(), new JaroWinkler(), new Soundex()]
def out(name, results) {
    print name.padLeft(14) + '  '; results.each{print(it.padRight(16))}; println()
}
def outr(name, results){out(name, results.collect{''+((int)(it*100))/100})}
out ('Word/Metric', metrics.collect{it.shortDescriptionString} )
candidates.each{ w -> outr(w, metrics.collect{ m -> m.getSimilarity(target, w)} )}
// =>
//   Word/Metric  Levenshtein     MongeElkan      JaroWinkler     Soundex
//         quick  0               0.11            0               0.66
//         brown  0.16            0.23            0.5             0.73
//           fox  0               0.2             0               0.66
//        jumped  0               0.2             0               0.66
//          over  0               0.44            0               0.55
//           the  0               0.33            0               0.55
//          lazy  0.33            0.5             0.44            0.66
//           dog  0               0.2             0               0.66
//       ballast  0.85            0.83            0.96            1
//      ballasts  0.75            0.83            0.94            0.94
//    balustrade  0.5             0.93            0.3             0.94
//   balustrades  0.45            0.93            0.3             0.94
//         blast  0.83            0.8             0.88            1
//       blasted  0.57            0.66            0.8             0.94
//       blaster  0.57            0.66            0.8             0.94
//      blasters  0.5             0.66            0.77            0.94
//      blasting  0.5             0.66            0.77            0.94
//        blasts  0.66            0.66            0.84            0.94
// to implement the example, iterate through /usr/dict/words selecting words
// where one or a combination of metrics are greater than some threshold
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.14
//----------------------------------------------------------------------------------
n = "   49 here"
println n.replaceAll(/\G /,'0')
// => 00049 here

str = "3,4,5,9,120"
print 'Found numbers:'
str.eachMatch(/\G,?(\d+)/){ print ' ' + it[1] }
println()
// => Found numbers: 3 4 5 9 120

// Groovy doesn't have the String.pos or a /c re modifier like Perl
// But it does have similar functionality. Matcher has start() and
// end() for find the position and Matcher's usePattern() allows
// you to swap patterns without changing the buffer position
text = 'the year 1752 lost 10 days on the 3rd of September'
p = ~/(?<=\D)(\d+)/
m = p.matcher(text)
while (m.find()) {
    println 'Found ' + m.group() + ' starting at pos ' + m.start() +
            ' and ending at pos ' + m.end()
}
// now reset pos back to between 1st and 2nd numbers
if (m.find(16)) { println 'Found ' + m.group() }
// =>
// Found 1752 starting at pos 9 and ending at pos 13
// Found 10 starting at pos 19 and ending at pos 21
// Found 3 starting at pos 34 and ending at pos 35
// Found 10

// Alternatively you can use Scanner in Java 5-7+:
p1 = ~/(?<=\D)(\d+)/
p2 = ~/\S+/
s = new Scanner(text)
while ((f = s.findInLine(p1))) { println 'Found: ' + f }
if ((f = s.findInLine(p2))) { println "Found $f after the last number." }
// =>
// Found: 1752
// Found: 10
// Found: 3
// Found rd after the last number.
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.15
//----------------------------------------------------------------------------------
html = '<b><i>this</i> and <i>that</i> are important</b> Oh, <b><i>me too!</i></b>'

greedyHtmlStripPattern = ~/(?m)<.*>/       // not good
nonGreedyHtmlStripPattern = ~/(?m)<.*?>/   // not great
simpleNested = ~/(?mx)<b><i>(.*?)<\/i><\/b>/
// match BEGIN, then not BEGIN, then END
generalPattern = ~/BEGIN((?:(?!BEGIN).)*)END/
betterButInefficient1 = ~/(?mx)<b><i>(  (?: (?!<\/b>|<\/i>). )*  ) <\/i><\/b>/
betterButInefficient2 = ~/(?mx)<b><i>(  (?: (?!<\/[ib]>). )*  ) <\/i><\/b>/

efficientPattern = '''(?mx)
    <b><i>
    [^<]*  # stuff not possibly bad, and not possibly the end.
    (?:
 # at this point, we can have '<' if not part of something bad
     (?!  </?[ib]>  )   # what we can't have
     <                  # okay, so match the '<'
     [^<]*              # and continue with more safe stuff
    ) *
    </i></b>
'''                   //'
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.16
//----------------------------------------------------------------------------------
input = 'This is a test\nTest of the duplicate word finder.\n'
dupWordPattern = '''(?ix)
       \\b    # start at word boundary
      (\\S+)  # find chunk of non-whitespace
       \\b    # until a word boundary
      (
       \\s+   # followed by whitespace
       \\1    # and that same chunk again
       \\b    # and a word boundary
      ) +     # one or more times
'''
finder = input =~ dupWordPattern
println 'Found duplicate word: ' + finder[0][1]
// => Found duplicate word: test

astr = 'nobody'
bstr = 'bodysnatcher'
m = "$astr $bstr" =~ /^(\w+)(\w+) \2(\w+)$/
actual = "${m[0][2]} overlaps in ${m[0][1]}-${m[0][2]}-${m[0][3]}"
assert actual == 'body overlaps in no-body-snatcher'

cap = 'o' * 180
while (m = (cap =~ /^(oo+?)\1+$/)) {
    p1 = m[0][1]
    print p1.size() + ' '
    cap = cap.replaceAll(p1,'o')
}
println cap.size()
// => 2 2 3 3 5

// diophantine
// solve for 12x + 15y + 16z = 281, maximizing x
if ((m = ('o' * 281) =~ /^(o*)\1{11}(o*)\2{14}(o*)\3{15}$/)) {
    x=m[0][1].size(); y=m[0][2].size(); z=m[0][3].size()
    println "One solution is: x=$x; y=$y; z=$z"
} else println "No solution."
// => One solution is: x=17; y=3; z=2

// using different quantifiers:
// /^(o+)\1{11}(o+)\2{14}(o+)\3{15}$/
// => One solution is: x=17; y=3; z=2

// /^(o*?)\1{11}(o*)\2{14}(o*)\3{15}$/
// => One solution is: x=0; y=7; z=11

// /^(o+?)\1{11}(o*)\2{14}(o*)\3{15}$/
// => One solution is: x=1; y=3; z=14
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.17
//----------------------------------------------------------------------------------
// Groovy doesn't currently support x!~y so you must use the !(x=~y) style

// alpha OR beta
assert 'alpha' ==~ /alpha|beta/
assert 'beta' ==~ /alpha|beta/
assert 'betalpha' =~ /alpha/ || 'betalpha' =~ /beta/

// alpha AND beta
assert !('alpha' =~ /(?=.*alpha)(?=.*beta)/)
assert 'alphabeta' =~ /(?=.*alpha)(?=.*beta)/
assert 'betalpha' =~ /(?=.*alpha)(?=.*beta)/
assert 'betalpha' =~ /alpha/ && 'betalpha' =~ /beta/

// alpha AND beta,  no overlap
assert 'alphabeta' =~ /alpha.*beta|beta.*alpha/
assert !('betalpha' =~ /alpha.*beta|beta.*alpha/)

// NOT beta
assert 'alpha gamma' =~ /^(?:(?!beta).)*$/
assert !('alpha beta gamma' =~ /^(?:(?!beta).)*$/)

// NOT bad BUT good
assert !('GOOD and BAD' =~ /(?=(?:(?!BAD).)*$)GOOD/)
assert !('BAD' =~ /(?=(?:(?!BAD).)*$)GOOD/)
assert !('WORSE' =~ /(?=(?:(?!BAD).)*$)GOOD/)
assert 'GOOD' =~ /(?=(?:(?!BAD).)*$)GOOD/

// minigrep could be done as a one-liner as follows
// groovy -p -e "if (line =~ /pat/) return line" datafile

string = 'labelled'
assert string =~ /^(?=.*bell)(?=.*lab)/
assert string =~ /bell/ && string =~ 'lab'
fakeAddress = "blah bell blah "
murrayHillRegex = '''(?x)
             ^              # start of string
            (?=             # zero-width lookahead
                .*          # any amount of intervening stuff
                bell        # the desired bell string
            )               # rewind, since we were only looking
            (?=             # and do the same thing
                .*          # any amount of intervening stuff
                lab         # and the lab part
            )
'''
assert string =~ murrayHillRegex
assert !(fakeAddress =~ murrayHillRegex)

// eliminate overlapping
assert !(string =~ /(?:^.*bell.*lab)|(?:^.*lab.*bell)/)

brandRegex = '''(?x)
            (?:                 # non-capturing grouper
                ^ .*?           # any amount of stuff at the front
                  bell          # look for a bell
                  .*?           # followed by any amount of anything
                  lab           # look for a lab
              )                 # end grouper
        |                       # otherwise, try the other direction
            (?:                 # non-capturing grouper
                ^ .*?           # any amount of stuff at the front
                  lab           # look for a lab
                  .*?           # followed by any amount of anything
                  bell          # followed by a bell
              )                 # end grouper
'''
assert !(string =~ brandRegex)

map = 'the great baldo'

assert map =~ /^(?:(?!waldo).)*$/
noWaldoRegex = '''(?x)
        ^                   # start of string
        (?:                 # non-capturing grouper
            (?!             # look ahead negation
                waldo       # is he ahead of us now?
            )               # is so, the negation failed
            .               # any character (cuzza /s)
        ) *                 # repeat that grouping 0 or more
        $                   # through the end of the string
'''
assert map =~ noWaldoRegex

// on unix systems use: realFakedInput = 'w'.process().text
fakedInput = '''
 7:15am  up 206 days, 13:30,  4 users,  load average: 1.04, 1.07, 1.04
USER     TTY      FROM              LOGIN@  IDLE   JCPU   PCPU  WHAT
tchrist  tty1                       5:16pm 36days 24:43   0.03s  xinit
tchrist  tty2                       5:19pm  6days  0.43s  0.43s  -tcsh
tchrist  ttyp0    chthon            7:58am  3days 23.44s  0.44s  -tcsh
gnat     ttyS4    coprolith         2:01pm 13:36m  0.30s  0.30s  -tcsh
'''.trim() + '\n'

def miniGrepMethod(input) {
    input.split('\n').findAll{it =~ '^(?!.*ttyp).*tchrist'}
}
assert miniGrepMethod(fakedInput).size() == 2

findUserRegex = '''(?xm)
    ^                       # anchored to the start
    (?!                     # zero-width look-ahead assertion
        .*                  # any amount of anything (faster than .*?)
        ttyp                # the string you don't want to find
    )                       # end look-ahead negation; rewind to start
    .*                      # any amount of anything (faster than .*?)
    tchrist                 # now try to find Tom
'''
assert (fakedInput =~ findUserRegex).count == 2
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.18
//----------------------------------------------------------------------------------
// Groovy uses Unicode character encoding
// special care needs to be taken when using unicode because of the different
// byte lengths, e.g. � can be encoded as two bytes \u0061\u0300 and is also
// supported in legacy character sets by a single character \u00E0.  To Match
// this character, you can't use any of /./, /../, /a/, /\u00E0/, /\u0061/\u0300
// or /\pL/. The correct way is to use /X (not currently supported) or one
// of /\pL/\pM*/ to ensure that it is a letter or /\PM\pM*/ when you just want
// to combine multicharacter sequences and don't care whether it is a letter
def checkUnicode(s) {
    println s + ' is of size ' + s.size()
    println 'Exactly matches /./   ' + (s ==~ /./)
    println 'Exactly matches /../  ' + (s ==~ /../)
    println 'Exactly matches /a/   ' + (s ==~ /a/)
    println 'Exactly matches /\\u00E0/       '  + (s ==~ /\u00E0/)
    println 'Exactly matches /\\u0061\\u0300/ ' + (s ==~ /\u0061\u0300/)
    println 'Exactly matches /\\pL/          '  + (s ==~ /\pL/)
    println 'Exactly matches /\\pL\\pM*/      ' + (s ==~ /\pL\pM*/)
    println 'Exactly matches /\\PM\\pM*/      ' + (s ==~ /\PM\pM*/)
}
checkUnicode('�')
checkUnicode('\u0061\u0300')
checkUnicode('\u00E0')
// =>
// � is of size 1
// Exactly matches /./   true
// Exactly matches /../  false
// Exactly matches /a/   false
// Exactly matches /\u00E0/       true
// Exactly matches /\u0061\u0300/ false
// Exactly matches /\pL/          true
// Exactly matches /\pL\pM*/      true
// Exactly matches /\PM\pM*/      true
// a? is of size 2
// Exactly matches /./   false
// Exactly matches /../  true
// Exactly matches /a/   false
// Exactly matches /\u00E0/       false
// Exactly matches /\u0061\u0300/ true
// Exactly matches /\pL/          false
// Exactly matches /\pL\pM*/      true
// Exactly matches /\PM\pM*/      true
// � is of size 1
// Exactly matches /./   true
// Exactly matches /../  false
// Exactly matches /a/   false
// Exactly matches /\u00E0/       true
// Exactly matches /\u0061\u0300/ false
// Exactly matches /\pL/          true
// Exactly matches /\pL\pM*/      true
// Exactly matches /\PM\pM*/      true
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.19
//----------------------------------------------------------------------------------
// The Perl Cookbook categorizes this as a hard problem ... mostly for
// reasons not related to the actual regex - but with a 60-line regex
// perhaps there are some issues with that too. Further details:
// http://www.perl.com/CPAN/authors/Tom_Christiansen/scripts/ckaddr.gz

simpleCommentStripper = /\([^()]*\)/
println 'Book Publishing <marketing@books.com> (We will spam you)'.replaceAll(simpleCommentStripper, '')
// => Book Publishing <marketing@books.com>

// inspired by the fact that domain names can contain any foreign character these days
modern = /^.+@[^\.].*\.[a-z]{2,}>?$/

// .Net 
lenient = /\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/

// a little more checking
strict = /^[_a-zA-Z0-9- <]+(\.[_a-zA-Z0-9- <]+)*@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\./ +
         /(([0-9]{1,3})|([a-zA-Z]{2,3})|(aero|coop|info|museum|name))>?$/

addresses = ['someuser@somehost.com',
             'Book Publishing <marketing@books.com>']
addresses.each{
    assert it =~ lenient
    assert it =~ strict
    assert it =~ modern
}

//----------------------------------------------------------------------------------

// @@PLEAC@@_6.20
//----------------------------------------------------------------------------------
def findAction(ans) {
    def re = '(?i)^' + Pattern.quote(ans)
    if      ("SEND"  =~ re) println "Action is send"
    else if ("STOP"  =~ re) println "Action is stop"
    else if ("ABORT" =~ re) println "Action is abort"
    else if ("EDIT"  =~ re) println "Action is edit"
    else println 'No Match'
}
findAction('edit something')
// => No Match
findAction('edit')
// => Action is edit
findAction('se')
// => Action is send
findAction('e')
// => Action is edit

def buildAbbrev(words) {
    def table = new TreeMap()
    words.each{ w ->
        (0..<w.size()).each { n ->
            if (!(words - w).any{
                it.size() >= n+1 && it[0..n] == w[0..n]
            }) table[w[0..n]] = w
        }
    }
    table
}
println buildAbbrev('send stop abort edit'.split(' ').toList())
// => ["a":"abort", "ab":"abort", "abo":"abort", "abor":"abort", "abort":"abort",
//     "e":"edit", "ed":"edit", "edi":"edit", "edit":"edit", "se":"send", "sen":"send",
//     "send":"send", "st":"stop", "sto":"stop", "stop":"stop"]

// miniShellScript:
// dummy methods
def invokeEditor() { println "invoking editor" }
def deliverMessage() { println "delivering message at " + new Date() }
actions = [
    edit:    this.&invokeEditor,
    send:    this.&deliverMessage,
    list:    { println Runtime.runtime.freeMemory() },
    abort:   { System.exit(0) },
    unknown: { println "Unknown Command"}
]

table = buildAbbrev(actions.keySet().toList())
prompt = '\n> '
print 'Enter Commands: edit send list abort' + prompt
new BufferedReader(new InputStreamReader(System.in)).eachLine{ line ->
    def idx = (table.containsKey(line)) ? table[line] : 'unknown'
    actions[idx]()
    print prompt
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_6.21
//----------------------------------------------------------------------------------
//% gunzip -c ~/mail/archive.gz | urlify > archive.urlified
//% urlify ~/mail/*.inbox > ~/allmail.urlified

urls = '(https?|telnet|gopher|file|wais|ftp|mail)'
ltrs = /\w/
gunk = /\#\/~:.?+=&%@!\-/
punc = /.:?\-/
doll = /$/
all  = /$ltrs$gunk$punc/

findUrls = """(?ix)
        \\b                   # start at word boundary
        (                     # begin group 1  {
         $urls   :            # need resource and a colon
         [$all] +?            # followed by on or more of any valid
                              #  character, but be conservative and
                              #  take only what you need to...
        )                     # end   group 1  }
        (?=                   # look-ahead non-consumptive assertion
         [$punc]*             # either 0 or more punctuation
         [^$all]              #   followed by a non-url character
         |                    # or else
         $doll                #   then end of the string
        )
"""

input = '''
If you find a typo on http://groovy.codehaus.org please
send an email to mail:spelling.pedant@codehaus.org
'''

println input.replaceAll(findUrls,'<a href="$1">$1</a>')
// =>
// If you find a typo on <a href="http://groovy.codehaus.org">http://groovy.codehaus.org</a> please
// send an email to <a href="mail:spelling.pedant@codehaus.org">mail:spelling.pedant@codehaus.org</a>

// urlifyScript:
//#!/usr/bin/groovy
// urlify - wrap HTML links around URL-like constructs
// definitions from above
args.each{ file ->
    new File(file).eachLine{ line ->
        println line.replaceAll(findUrls,'<a href="$1">$1</a>')
    }
}

//----------------------------------------------------------------------------------

// @@PLEAC@@_6.22
//----------------------------------------------------------------------------------
// @@INCOMPLETE@@
// @@INCOMPLETE@@

//----------------------------------------------------------------------------------

// @@PLEAC@@_6.23
//----------------------------------------------------------------------------------
romans = /(?i)^m*(d?c{0,3}|c[dm])(l?x{0,3}|x[lc])(v?i{0,3}|i[vx])$/
assert 'cmxvi' =~ romans
// can't have tens before 1000s (M) or 100s (C) after 5s (V)
assert !('xmvci' =~ romans)

// swap first two words
assert 'the words'.replaceAll(/(\S+)(\s+)(\S+)/, '$3$2$1') == 'words the'

// extract keyword and value
m = 'k=v' =~ /(\w+)\s*=\s*(.*)\s*$/
assert m.matches()
assert m[0][1] == 'k'
assert m[0][2] == 'v'

hasAtLeastSize = { n -> /.{$n,}/ }
assert 'abcdefghijklmnopqrstuvwxyz' =~ hasAtLeastSize(20)

// MM/DD/YY HH:MM:SS (lenient - doesn't check HH > 23 etc)
d = /\d+/
datetime = "($d)/($d)/($d) ($d):($d):($d)"
assert '04/05/2006 10:26:59' =~ datetime

orig = '/usr/bin/vi'
expected = '/usr/local/bin/vi'
orig.replaceAll('/usr/bin','/usr/local/bin') == expected

escapeSequenceRegex = /%([0-9A-Fa-f][0-9A-Fa-f])/
convertEscapeToChar = { Object[] chr -> new Character((char)Integer.parseInt(chr[1],16)) }
assert 'abc%3cdef'.replaceAll(escapeSequenceRegex, convertEscapeToChar) == 'abc<def'

commentStripper = '''(?xms)
    /\\*        # Match the opening delimiter
    .*          # Match a minimal number of characters */
    \\*/        # Match the closing delimiter
'''

input = '''
a line
/*
some comment
*/
another line
'''
expected = '''
a line

another line
'''

assert input.replaceAll(commentStripper,'') == expected

// emulate s.trim()
assert '  x  y  '.replaceAll(/^\s+/, '').replaceAll(/\s+$/, '') == 'x  y'

// convert \\n into \n
assert (/a\nb/.replaceAll(/\\n/,"\n") == 'a\nb')

// remove package symbol (Groovy/Java doesn't use this as package symbol)
assert 'A::B'.replaceAll(/^.*::/, '') == 'B'

// match IP Address (requires leading 0's)
ipregex = /^([01]?\d\d|2[0-4]\d|25[0-5])\.([01]?\d\d|2[0-4]\d|25[0-5])\./ +
    /([01]?\d\d|2[0-4]\d|25[0-5])\.([01]?\d\d|2[0-4]\d|25[0-5])$/
assert !('123.456.789' =~ ipregex)
assert '192.168.000.001' =~ ipregex

// extract basename
assert 'c:/usr/temp.txt'.replaceAll(/^.*\/{1}/, '') == 'temp.txt'

termcap = ':co#80:li#24:'
m = (termcap =~ /:co\#(\d+):/)
assert m.count == 1
assert m[0][1] == '80'

assert 'cmd c:/tmp/junk.txt'.replaceAll(/ \S+\/{1}/, ' ') == 'cmd junk.txt'

os = System.getProperty('os.name')
println 'Is Linux? ' + (os ==~ /(?i)linux.*/)
println 'Is Windows? ' + (os ==~ /(?i)windows.*/)
println 'Is Mac? ' + (os ==~ /(?i)mac.*/)

// join multiline sting
multi = '''
This is
    a test
'''.trim()
assert multi.replaceAll(/(?m)\n\s+/, ' ') == 'This is a test'

// nums in string
string = 'The 5th test was won today by 10 wickets after 10.5 overs'
nums = string =~ /(\d+\.?\d*|\.\d+)/
assert (0..<nums.count).collect{ nums[it][1] }.join(' ') == '5 10 10.5'

// capitalize words
words = 'the Capital words ARE hiding'
capwords = words =~ /(\b\p{Upper}+\b)/
assert (0..<capwords.count).collect{ capwords[it][1] }.join(' ') == 'ARE'

lowords = words =~ /(\b\p{Lower}+\b)/
assert (0..<lowords.count).collect{ lowords[it][1] }.join(' ') == 'the words hiding'

capWords = words =~ /(\b\p{Upper}\p{Lower}*\b)/
assert (0..<capWords.count).collect{ capWords[it][1] }.join(' ') == 'Capital'

input = '''
If you find a typo on <a href="http://groovy.codehaus.org">http://groovy.codehaus.org</a> please
send an email to <a href="mail:spelling.pedant@codehaus.org">mail:spelling.pedant@codehaus.org</a>
'''

linkRegex = /(?im)<A[^>]+?HREF\s*=\s*["']?([^'" >]+?)[ '"]?>/          //'
links = input =~ linkRegex
(0..<links.count).each{ println links[it][1] }
// =>
// http://groovy.codehaus.org
// mail:spelling.pedant@codehaus.org

// find middle initial if any
m = 'Lee Harvey Oswald' =~ /^\S+\s+(\S)\S*\s+\S/
initial = m.count ? m[0][1] : ""
assert initial == 'H'

// inch marks to quotes
println 'I said "Hello" to you.'.replaceAll(/"([^"]*)"/, /``$1''/)     //"
// => I said ``Hello'' to you.

// extract sentences (2 spaces or newline after punctuation)
input = '''
Is this a sentence?
Yes!  And so
is this.  And the fourth.
'''
sentences = []
strip = input.replaceAll(/(\p{Punct})\n/, '$1  ').replaceAll(/\n/, ' ').replaceAll(/ {3,}/,'  ')
m = strip =~ /(\S.*?\p{Punct})(?=  |\Z)/
(0..<m.count).each{ sentences += m[it][1] }
assert sentences == ["Is this a sentence?", "Yes!", "And so is this.", "And the fourth."]

// YYYY-MM-DD
m = '2007-2-28' =~ /(\d{4})-(\d\d?)-(\d\d?)/
assert m.matches()
assert ['2007', '2', '28'] == [m[0][1], m[0][2], m[0][3]]

usPhoneRegex = /^[01]?[- .]?(\([2-9]\d{2}\)|[2-9]\d{2})[- .]?\d{3}[- .]?\d{4}$/
numbers = '''
(425) 555-0123
425-555-0123
425 555 0123
1-425-555-0123
'''.trim().split('\n').toList()
assert numbers.every{ it ==~ usPhoneRegex }

exclaimRegex = /(?i)\boh\s+my\s+gh?o(d(dess(es)?|s?)|odness|sh)\b/
assert 'Oh my Goodness!' =~ exclaimRegex
assert !('Golly gosh' =~ exclaimRegex)

input = 'line 1\rline 2\nline\r\nline 3\n\rline 4'
m = input =~ /(?m)^([^\012\015]*)(\012\015?|\015\012?)/
assert m.count == 4


// @@PLEAC@@_6.22
// not an exact equivalent to original cookbook but has
// a reasonable subset of mostly similar functionality
// instead of -r recursion option, use Ant fileset wildcards
// e.g. **/*.c.  You can also specify an excludes pattern
// e.g. **/*.* -X **/*.h will process all but header files
// (currently not optimised and with minimal error checking)
// uses jopt-simple (jopt-simple.sf.net)

op = new joptsimple.OptionParser()
NOCASE  = 'i';  op.accepts( NOCASE,  "case insensitive" )
WITHN   = 'n';  op.accepts( WITHN,   "display line/para with line/para number" )
WITHF   = 'H';  op.accepts( WITHF,   "display line/para with filename" )
NONAME  = 'h';  op.accepts( NONAME,  "hide filenames" )
COUNT   = 'c';  op.accepts( COUNT,   "give count of lines/paras matching" )
TCOUNT  = 'C';  op.accepts( TCOUNT,  "give count of total matches (multiple per line/para)" )
WORD    = 'w';  op.accepts( WORD,    "word boundaries only" )
EXACT   = 'x';  op.accepts( EXACT,   "exact matches only" )
INVERT  = 'v';  op.accepts( INVERT,  "invert search sense (lines that DON'T match)" )
EXCLUDE = 'X';  op.accepts( EXCLUDE, "exclude files matching pattern [default is '**/*.bak']" ).
                    withRequiredArg().describedAs('path_pattern')
MATCH   = 'l';  op.accepts( MATCH,   "list names of files with matches" )
NOMATCH = 'L';  op.accepts( NOMATCH, "list names of files with no match" )
PARA    = 'p';  op.accepts( PARA,    "para mode (.* matches newlines)" ).
                    withOptionalArg().describedAs('para_pattern')
EXPR    = 'e';  op.accepts( EXPR,    "expression (when pattern begins with '-')" ).
                    withRequiredArg().describedAs('pattern')
FILE    = 'f';  op.accepts( FILE,    "file containing pattern" ).
                    withRequiredArg().describedAs('filename')
HELP = 'help';  op.accepts( HELP,    "display this message" )

options = op.parse(args)
params = options.nonOptionArguments()
if (options.wasDetected( HELP )) {
    op.printHelpOn( System.out )
} else if (params.size() == 0) {
    println "Usage: grep [OPTION]... PATTERN [FILE]...\nTry 'grep --$HELP' for more information."
} else {
    modifiers = []
    paraPattern = ''
    o_withn   = options.wasDetected( WITHN )
    o_withf   = options.wasDetected( WITHF )
    o_noname  = options.wasDetected( NONAME )
    o_count   = options.wasDetected( COUNT )
    o_tcount  = options.wasDetected( TCOUNT )
    o_invert  = options.wasDetected( INVERT )
    o_match   = options.wasDetected( MATCH )
    o_nomatch = options.wasDetected( NOMATCH )
    if (options.wasDetected( EXPR )) {
        pattern = options.valueOf( EXPR )
    } else if (options.wasDetected( FILE )) {
        pattern = new File(options.valueOf( FILE )).text.trim()
    } else {
        pattern = params[0]
        params = params[1..-1]
    }
    if (options.wasDetected( EXCLUDE )) excludes = options.valueOf( EXCLUDE )
    else excludes = ['**/*.bak']
    if (options.wasDetected( EXACT )) pattern = '^' + pattern + '$'
    else if (options.wasDetected( WORD )) pattern = /\b$pattern\b/
    if (options.wasDetected( NOCASE )) modifiers += 'i'
    if (options.wasDetected( PARA )) {
        if (options.hasArgument( PARA )) paraPattern = options.valueOf( PARA )
        else paraPattern = '^$'
        paraPattern = '(?sm)' + paraPattern
        modifiers += 'sm'
    }
    if (modifiers) pattern = "(?${modifiers.join()})" + pattern

    if (params.size() == 0) grepStream(System.in, '<stdin>')
    else {
        scanner = new AntBuilder().fileScanner {
            fileset(dir:'.', includes:params.join(','), excludes:excludes)
        }
        for (f in scanner) {
            grepStream(new FileInputStream(f), f)
        }
    }
}

def grepStream(s, name) {
    def count = 0
    def tcount = 0
    def pieces
    if (paraPattern) pieces = s.text.split(paraPattern)
    else pieces = s.readLines()
    def fileMode = o_match || o_nomatch || o_count || o_tcount
    pieces.eachWithIndex{line, index ->
        def m = line =~ pattern
        boolean found = m.count
        if (found != o_invert) {
            count++
            tcount += m.count
            if (!fileMode) {
                linefields = []
                if (o_withf) linefields += name
                if (o_withn) linefields += index + 1
                linefields += line
                println linefields.join(':')
            }
        }
    }
    def display = true
    if ((o_match && count == 0) || (o_nomatch && count != 0)) display = false
    if (fileMode && display) {
        filefields = []
        if (!o_noname) filefields += name
        if (o_tcount) filefields += tcount
        else if (o_count) filefields += count
        println filefields.join(':')
    }
}
//----------------------------------------------------------------------------------
