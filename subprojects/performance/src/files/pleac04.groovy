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

// @@PLEAC@@_4.0
//----------------------------------------------------------------------------------
simple = [ "this", "that", "the", "other" ]
nested = [ "this", "that", [ "the", "other" ] ]
assert nested.size() == 3
assert nested[2].size() == 2

flattenNestedToSimple = [ "this", "that", [ "the", "other" ] ].flatten()
assert flattenNestedToSimple.size() == 4
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.1
//----------------------------------------------------------------------------------
a = [ "quick", "brown", "fox" ]
assert a.size() == 3
a = 'Why are you teasing me?'.split(' ')
assert a == ["Why", "are", "you", "teasing", "me?"]

removeLeadingSpaces = { it.trim() }
nonBlankLines = { it }
lines = '''
    The boy stood on the burning deck,
    It was as hot as glass.
'''.split('\n').collect(removeLeadingSpaces).findAll(nonBlankLines)

assert lines == ["The boy stood on the burning deck,",
                 "It was as hot as glass."]

// initialiseListFromFileScript:
lines = new File('mydata.txt').readLines()

// processFileScript:
new File('mydata.txt').eachLine{
    // dosomething
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.2
//----------------------------------------------------------------------------------
marbleColors = ['red', 'green', 'yellow']
assert marbleColors.join(', ') == 'red, green, yellow'

def commify(items) {
    if (!items) return items
    def sepchar = items.find{ it =~ /,/ } ? '; ' : ', '
    switch (items.size()) {
        case 1: return items[0]
        case 2: return items.join(' and ')
    }
    items[0..-2].join(sepchar) + sepchar + 'and ' + items[-1]
}

assert commify(marbleColors) == 'red, green, and yellow'

lists = [
    [ 'just one thing' ],
    [ 'Mutt', 'Jeff' ],
    'Peter Paul Mary'.split(' '),
    [ 'To our parents', 'Mother Theresa', 'God' ],
    [ 'pastrami', 'ham and cheese', 'peanut butter and jelly', 'tuna' ],
    [ 'recycle tired, old phrases', 'ponder big, happy thoughts' ],
    [ 'recycle tired, old phrases',
      'ponder big, happy thoughts',
      'sleep and dream peacefully' ],
]

expected = '''
just one thing
Mutt and Jeff
Peter, Paul, and Mary
To our parents, Mother Theresa, and God
pastrami, ham and cheese, peanut butter and jelly, and tuna
recycle tired, old phrases and ponder big, happy thoughts
recycle tired, old phrases; ponder big, happy thoughts; and sleep and dream peacefully
'''

assert expected == '\n' + lists.collect{commify(it)}.join('\n') + '\n'
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.3
//----------------------------------------------------------------------------------
// In Groovy, lists and arrays are more or less interchangeable
// here is the example using lists
people = ['Crosby', 'Stills', 'Nash']
assert people.size() == 3
people[3] = 'Young'
assert people.size() == 4
assert people == ['Crosby', 'Stills', 'Nash', 'Young']
// to use arrays simply do 'people = peopleArray.toList()' at the start
// and 'peopleArray = people as String[]' at the end
// if you attempt to do extension on a Java array you will get an
// ArrayIndexOutOfBoundsException - which is why Java has ArrayList et al
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.4
//----------------------------------------------------------------------------------
// list to process
people == ['Crosby', 'Stills', 'Nash', 'Young']
// helper
startsWithCapital = { word -> word[0] in 'A'..'Z' }

// various styles are possible for processing lists
// closure style
people.each { person -> assert startsWithCapital(person) }
// for loop style
for (person in people) { assert startsWithCapital(person) }

// unixScriptToFindAllUsersStartingWithLetterA:
all = 'who'.execute().text.replaceAll('\r', '').split('\n')
all.grep(~/^a.*/).each{ println it }

// printFileWithWordsReversedScript:
new File('Pleac/src/SlowCat.groovy').eachLine{ line ->
     line.split(' ').each{ print it.reverse() }
}

a = [0.5, 3]; b = [0, 1]
assert [a, b].flatten().collect{ it * 7 } == [3.5, 21, 0, 7]
// above doesn't modify original arrays
// instead use a = a.collect{ ... }
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.5
//----------------------------------------------------------------------------------
// not relevant in Groovy since we have always references
items = []
for (item in items) {
    // do something with item
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.6
//----------------------------------------------------------------------------------
assert [ 1, 1, 2, 2, 3, 3, 3, 5 ].unique() == [ 1, 2, 3, 5 ]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.7
//----------------------------------------------------------------------------------
assert [ 1, 1, 2, 2, 3, 3, 3, 4, 5 ] - [ 1, 2, 4 ]  ==  [3, 3, 3, 5]
assert [ 1, 1, 2, 2, 3, 3, 3, 4, 5 ].unique() - [ 1, 2, 4 ]  ==  [3, 5]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.8
//----------------------------------------------------------------------------------
a = [1, 3, 5, 6, 7, 8]
b = [2, 3, 5, 7, 9]
// intersection
assert a.intersect(b) == [3, 5, 7]
// union
assert (a + b).unique().sort() == [1, 2, 3, 5, 6, 7, 8, 9]
// difference
assert (a - b) == [1, 6, 8]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.9
//----------------------------------------------------------------------------------
members = [ "Time", "Flies" ]
initiates =  [ "An", "Arrow" ]
members += initiates
assert members == ["Time", "Flies", "An", "Arrow"]

members.add(2, "Like")
assert members == ["Time", "Flies", "Like", "An", "Arrow"]

members[0] = "Fruit"
members[3..4] = ["A", "Banana"]
assert members == ["Fruit", "Flies", "Like", "A", "Banana"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.10
//----------------------------------------------------------------------------------
items = ["the", "quick", "brown", "fox"]
assert items.reverse() == ["fox", "brown", "quick", "the"]

firstLetters = []
items.reverseEach{ firstLetters += it[0] }
assert firstLetters.join() == 'fbqt'

descending = items.sort().reverse()
assert descending == ["the", "quick", "fox", "brown"]
descendingBySecondLastLetter = items.sort { a,b -> b[-2] <=> a[-2] }
assert descendingBySecondLastLetter == ["brown", "fox", "the", "quick"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.11
//----------------------------------------------------------------------------------
// warning: not an exact equivalent, idiomatic use would return copies
def shift2 = {one = friends[0]; two = friends[1]; 2.times{friends.remove(0)}}
friends = 'Peter Paul Mary Jim Tim'.split(' ').toList()
shift2()
assert one == 'Peter'
assert two == 'Paul'
assert friends == ["Mary", "Jim", "Tim"]

def pop2(items) { items[0..1] }
beverages = 'Dew Jolt Cola Sprite Fresca'.split(' ').toList()
pair = pop2(beverages)
assert pair == ["Dew", "Jolt"]
//----------------------------------------------------------------------------------


// @@PLEAC@@_4.12
//----------------------------------------------------------------------------------
class Employee {
    def name
    def position
    def salary
}
staff = [new Employee(name:'Jim',position:'Manager',salary:26000),
         new Employee(name:'Jill',position:'Engineer',salary:24000),
         new Employee(name:'Jack',position:'Engineer',salary:22000)]
highestEngineer = staff.find { emp -> emp.position == 'Engineer' }
assert highestEngineer.salary == 24000
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.13
//----------------------------------------------------------------------------------
engineers = staff.findAll { e -> e.position == 'Engineer' }
assert engineers.size() == 2

highPaid = staff.findAll { e -> e.salary > 23000 }
assert highPaid*.name == ["Jim", "Jill"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.14
//----------------------------------------------------------------------------------
// sort works for numbers
assert [100, 3, 20].sort() == [3, 20, 100]
// strings representing numbers will be sorted alphabetically
assert ['100', '3', '20'].sort() == ["100", "20", "3"]
// closure style sorting allows arbitrary expressions for the comparison
assert ['100', '3', '20'].sort{ a,b -> a.toLong() <=> b.toLong()} == ["3", "20", "100"]

// obtain the following on unix systems using: 'ps ux'.execute().text
processInput = '''
      PID    PPID    PGID     WINPID  TTY  UID    STIME COMMAND
     3868       1    3868       3868  con 1005 06:23:34 /usr/bin/bash
     3456    3868    3456       3528  con 1005 06:23:39 /usr/bin/ps
'''
nonEmptyLines = {it.trim()}
lines = processInput.split("\n").findAll(nonEmptyLines)[1..-1]
def col(n, s) { s.tokenize()[n] }
commandIdx = 7
pidIdx = 0
ppidIdx = 1
linesByPid = lines.sort{ col(pidIdx,it).toLong() }
assert col(commandIdx, linesByPid[0]) == '/usr/bin/ps'
linesByPpid = lines.sort{ col(ppidIdx,it).toLong() }
assert col(commandIdx, linesByPpid[0]) == '/usr/bin/bash'
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.15
//----------------------------------------------------------------------------------
// sort staff from 4.12 by name
assert staff.sort { a,b -> a.name <=> b.name }*.name == ["Jack", "Jill", "Jim"]
// sort by first two characters of name and if equal by descending salary
assert staff.sort { a,b ->
    astart = a.name[0..1]
    bstart = b.name[0..1]
    if (astart == bstart) return b.salary <=> a.salary
    return astart <=> bstart
}*.name == ["Jack", "Jim", "Jill"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.16
//----------------------------------------------------------------------------------
items = [1, 2, 3, 4, 5]
processed = []
10.times{
    processed << items[0]
    items = items[1..-1] + items[0]
}
assert processed == [1, 2, 3, 4, 5, 1, 2, 3, 4, 5]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.17
//----------------------------------------------------------------------------------
import java.text.DateFormatSymbols as Symbols
items = new Symbols().shortWeekdays.toList()[1..7]
assert items == ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
// not as random as you might expect
println items.sort{ Math.random() }
// => ["Sat", "Tue", "Sun", "Wed", "Mon", "Thu", "Fri"]
// better to use the built-in method for this purpose
Collections.shuffle(items)
println items
// => ["Wed", "Tue", "Fri", "Sun", "Sat", "Thu", "Mon"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.18
//----------------------------------------------------------------------------------
symbols = new Symbols()
words = symbols.weekdays.toList()[1..7] +
    symbols.months.toList()[0..11] +
    symbols.eras.toList() +
    symbols.amPmStrings.toList()

expected = //
'AD        August    February  July      May       October   September Tuesday   \n' +
'AM        BC        Friday    June      Monday    PM        Sunday    Wednesday \n' +
'April     December  January   March     November  Saturday  Thursday  \n'

class WordFormatter {
    def cols

    def process(list) {
        def sb = new StringBuffer()
        def colWidth = list.max{it.size()}.size() + 1
        int columns = [cols/colWidth, 1].max()
        def numWords = list.size()
        int rows = (numWords + columns - 1) / columns
        for (row in 0..<rows) {
            for (col in 0..<columns) {
                def target = row + col * rows
                if (target < numWords)
                    sb << list[target].padRight(colWidth)
            }
            sb << '\n'
        }
        return sb.toString()
    }
}

// get nr of chars that fit in window or console, see PLEAC 15.4
// hard-coded here but several packages are available, e.g. in JLine
// use a concrete implementation of Terminal.getTerminalWidth()
def getWinCharWidth() { 80 }

// main script
actual = new WordFormatter(cols:getWinCharWidth()).process(words.sort())
assert actual == expected
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.19
//----------------------------------------------------------------------------------
// recursive version is simplest but can be inefficient
def fact(n) { (n == 1) ? 1 : n * fact(n-1)}
assert fact(10) == 3628800
// unwrapped version: note use of BigInteger
def factorial(n) {
    def result = 1G // 1 as BigInteger
    while (n > 0) {
        result *= n
        n -= 1
    }
    return result
}
expected = 93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000
assert expected == factorial(100)
// println factorial(10000)
// => 284625... (greater than 35,000 digits)

// simple version but less efficient
def simplePermute(items, perms) {
    if (items.size() == 0)
        println perms.join(' ')
    else
        for (i in items) {
            newitems = items.clone()
            newperms = perms.clone()
            newperms.add(i)
            newitems.remove(i)
            simplePermute(newitems, newperms)
        }
}
simplePermute(['dog', 'bites', 'man'], [])
// =>
//dog bites man
//dog man bites
//bites dog man
//bites man dog
//man dog bites
//man bites dog

// optimised version below
expected = '''
man bites dog
man dog bites
bites man dog
bites dog man
dog man bites
dog bites man
'''

// n2pat(n, len): produce the N-th pattern of length len
def n2pat(n, length) {
    def pat = []
    int i = 1
    while (i <= length) {
        pat << (n % i)
        n = n.intdiv(i)
        i += 1
    }
    pat
}

// pat2perm(pat): turn pattern returned by n2pat() into
// permutation of integers.
def pat2perm(pat) {
    def source = (0 ..< pat.size()).collect{ it/*.toString()*/ }
    def perm = []
    while (pat.size() > 0) {
        def next = pat.remove(pat.size()-1)
        perm << source[next]
        source.remove(next)
    }
    perm
}

def n2perm(n, len) {
    pat2perm(n2pat((int)n,len))
}

data = ['man', 'bites', 'dog']
sb = new StringBuffer()
numPermutations = fact(data.size())
for (j in 0..<numPermutations) {
    def permutation = n2perm(j, data.size()).collect { k -> data[k] }
    sb << permutation.join(' ') + '\n'
}
assert '\n' + sb.toString() == expected
//----------------------------------------------------------------------------------
