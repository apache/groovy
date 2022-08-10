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

// @@PLEAC@@_11.0
//----------------------------------------------------------------------------------
// In Groovy, most usages of names are references (there are some special
// rules for the map shorthand notation and builders).
// Objects are inherently anonymous, they don't know what names refer to them.
ref = 3       // points ref to an Integer object with value 3.
println ref   // prints the value that the name ref refers to.

myList = [3, 4, 5]       // myList is a name for this list
anotherRef = myList
myMap = ["How": "Now", "Brown": "Cow"] // myMap is a name for this map

anArray = [1, 2, 3] as int[] // creates an array of three references to Integer objects

list = [[]]  // a list containing an empty list
list[2] = 'Cat'
println list // => [[], null, "Cat"]
list[0][2] = 'Dog'
println list // => [[null, null, "Dog"], null, "Cat"]

a = [2, 1]
b = a  // b is a reference to the same thing as a
a.sort()
println b // => [1, 2]

nat = [ Name: "Leonhard Euler",
        Address: "1729 Ramanujan Lane\nMathworld, PI 31416",
        Birthday: 0x5bb5580
]
println nat
// =>["Address":"1729 Ramanujan Lane\nMathworld, PI 31416", "Name":"Leonhard Euler", "Birthday":96163200]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.1
//----------------------------------------------------------------------------------
aref = myList
anonList = [1, 3, 5, 7, 9]
anonCopy = anonList
implicitCreation = [2, 4, 6, 8, 10]

anonList += 11
println anonList  // => [1, 3, 5, 7, 9, 11]

two = implicitCreation[0]
assert two == 2

//  To get the last index of a list, you can use size()
// but you never would
lastIdx = aref.size() - 1

// Normally, though, you'd use an index of -1 for the last
// element, -2 for the second last, etc.
println implicitCreation[-1]
//=> 10

// And if you were looping through (and not using a list closure operator)
(0..<aref.size()).each{ /* do something */ }

numItems = aref.size()

assert anArray instanceof int[]
assert anArray.class.isArray()
println anArray

myList.sort() // sort is in place.
myList += "an item" // append item

def createList() { return [] }
aref1 = createList()
aref2 = createList()
// aref1 and aref2 point to different lists.

println anonList[4] // refers to the 4th item in the list_ref list.

// The following two statements are equivalent and return up to 3 elements
// at indices 3, 4, and 5 (if they exist).
x = anonList[3..5]
x = anonList[(3..5).step(1)]

//   This will insert 3 elements, overwriting elements at indices 3,4, or 5 - if they exist.
anonList[3..5] = ["blackberry", "blueberry", "pumpkin"]

// non index-based looping
for (item in anonList) println item
anonList.each{ println it }

// index-based looping
(0..<anonList.size()).each{ idx -> println anonList[idx] }
for (idx in 0..<anonList.size()) println anonList[idx]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.2
//----------------------------------------------------------------------------------
// Making Hashes of Arrays
hash = [:] // empty map
hash["KEYNAME"] = "new value"

hash.each{ key, value -> println key + ' ' + value }

hash["a key"] = [3, 4, 5]
values = hash["a key"]

hash["a key"] += 6
println hash
// => ["KEYNAME":"new value", "a key":[3, 4, 5, 6]]

// attempting to access a value for a key not in the map yields null
assert hash['unknown key'] == null
assert hash.get('unknown key', 45) == 45
println hash
// => ["unknown key":45, "KEYNAME":"new value", "a key":[3, 4, 5, 6]]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.3
//----------------------------------------------------------------------------------
// Hashes are no different to other objects
myHash = [ key1:100, key2:200 ]
myHashCopy = myHash.clone()

value = myHash['key1']
value = myHash.'key1'
slice = myHash[1..3]
keys = myHash.keySet()

assert myHash instanceof Map

[myHash, hash].each{ m ->
    m.each{ k, v -> println "$k => $v"}
}
// =>
// key1 => 100
// key2 => 200
// unknown key => 45
// KEYNAME => new value
// a key => [3, 4, 5, 6]

values = ['key1','key2'].collect{ myHash[it] }
println values  // => [100, 200]

for (key in ["key1", "key2"]) {
    myHash[key] += 7
}
println myHash  // => ["key1":107, "key2":207]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.4
//----------------------------------------------------------------------------------
// you can use closures or the &method notation
def joy() { println 'joy' }
def sullen() { println 'sullen' }
angry = { println 'angry' }
commands = [happy: this.&joy,
            sad:   this.&sullen,
            done:  { System.exit(0) },
            mad:   angry
]

print "How are you?"
cmd = System.in.readLine()
if (cmd in commands.keySet()) commands[cmd]()
else println "No such command: $cmd"


// a counter of the type referred to in the original cookbook
// would be implemented using a class
def counterMaker(){
    def start = 0
    return { -> start++; start-1 }
}

counter = counterMaker()
5.times{ print "${counter()} " }; println()

counter1 = counterMaker()
counter2 = counterMaker()

5.times{ println "${counter1()} " }
println "${counter1()} ${counter2()}"
//=> 0
//=> 1
//=> 2
//=> 3
//=> 4
//=> 5 0


def timestamp() {
    def start = System.currentTimeMillis()
    return { (System.currentTimeMillis() - start).intdiv(1000) }
}
early = timestamp()
//sleep(10000)
later = timestamp()
sleep(2000)
println "It's been ${early()} seconds since early."
println "It's been ${later()} seconds since later."
//=> It's been 12 seconds since early.
//=> It's been 2 seconds since later.
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.5
//----------------------------------------------------------------------------------
// All variables in Groovy are objects including primitives. Some objects
// are immutable. Some operations on objects change mutable objects.
// Some operations produce new objects.

// 15 is an Integer which is an immutable object.
// passing 15 to a method passes a reference to the Integer object.
void print(n) { println "${n.toString()}" }
print(15)  // no need to create any kind of explicit reference

// even though Integers are immutable, references to them are not
x = 1
y = x
println "$x $y"  // => 1 1
x += 1    // "x" now refers to a different object than y
println "$x $y"  // => 2 1
y = 4     // "y" now refers to a different object than it did before
println "$x $y"  // => 2 4

// Some objects (including ints and strings) are immutable, however, which
// can give the illusion of a by-value/by-reference distinction:
list = [[1], 1, 's']
list.each{ it += 1 } // plus operator doesn't operate inplace
print list //=> [[1] 1 s]
list = list.collect{ it + 1 }
print list //=> [[1, 1], 2, s1]

list = [['Z', 'Y', 'X'], ['C', 'B', 'A'], [5, 3, 1]]
list.each{ it.sort() } // sort operation operates inline
println list // => [["X", "Y", "Z"], ["A", "B", "C"], [1, 3, 5]]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.6
//----------------------------------------------------------------------------------
// As indicated by the previous section, everything is referenced, so
// just create a list as normal, and beware that augmented assignment
// works differently with immutable objects to mutable ones and depends
// on the semantics of the particular operation invoked:
mylist = [1, "s", [1]]
print mylist
//=> [1, s, [1]]

mylist.each{ it *= 2 }
print mylist
//=> [1, s, [1,1]]

mylist[0] *= 2
mylist[-1] *= 2
print mylist
//=> [2, s, [1, 1]]

// If you need to modify every value in a list, you use collect
// which does NOT modify inplace but rather returns a new collection:
mylist = 1..4
println mylist.collect{ it**3 * 4/3 * Math.PI }
// => [4.188790204681671, 33.510321638395844, 113.09733552923255, 268.0825731062243]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.7
//----------------------------------------------------------------------------------
def mkcounter(count) {
    def start = count
    def bundle = [:]
    bundle.'NEXT' = { count += 1 }
    bundle.'PREV' = { count -= 1 }
    bundle.'RESET' = { count = start }
    bundle["LAST"] = bundle["PREV"]
    return bundle
}

c1 = mkcounter(20)
c2 = mkcounter(77)

println "next c1: ${c1["NEXT"]()}"  // 21
println "next c2: ${c2["NEXT"]()}"  // 78
println "next c1: ${c1["NEXT"]()}"  // 22
println "last c1: ${c1["PREV"]()}"  // 21
println "last c1: ${c1["LAST"]()}"  // 20
println "old  c2: ${c2["RESET"]()}" // 77
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.8
//----------------------------------------------------------------------------------
def addAndMultiply(a, b) {
    println "${a+b} ${a*b}"
}
methRef = this.&addAndMultiply
// or use direct closure
multiplyAndAdd = { a,b -> println "${a*b} ${a+b}" }
// later ...
methRef(2,3)                   // => 5 6
multiplyAndAdd(2,3)            // => 6 5
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.9
//----------------------------------------------------------------------------------
record = [
    "name": "Jason",
    "empno": 132,
    "title": "deputy peon",
    "age": 23,
    "salary": 37000,
    "pals": ["Norbert", "Rhys", "Phineas"],
]
println "I am ${record.'name'}, and my pals are ${record.'pals'.join(', ')}."
// => I am Jason, and my pals are Norbert, Rhys, Phineas.

byname = [:]
byname[record["name"]] = record

rp = byname.get("Aron")
if (rp) println "Aron is employee ${rp["empno"]}."

byname["Jason"]["pals"] += "Theodore"
println "Jason now has ${byname['Jason']['pals'].size()} pals."

byname.each{ name2, record ->
    println "$name2 is employee number ${record['empno']}."
}

employees = [:]
employees[record["empno"]] = record

// lookup by id
rp = employees[132]
if (rp) println "Employee number 132 is ${rp.'name'}."

byname["Jason"]["salary"] *= 1.035
println record
// => ["pals":["Norbert", "Rhys", "Phineas", "Theodore"], "age":23,
//      "title":"deputy peon", "name":"Jason", "salary":38295.000, "empno":132]

peons = employees.findAll{ k, v -> v.'title' =~ /(?i)peon/ }
assert peons.size() == 1
tsevens = employees.findAll{ k, v -> v.'age' == 27 }
assert tsevens.size() == 0

// Go through all records
println 'Names are: ' + employees.values().collect{r->r.'name'}.join(', ')

byAge = {a,b-> a.value().'age' <=> b.value().'age'}
employees.values().sort{byAge}.each{ r->
    println "${r.'name'} is ${r.'age'}"
}

// byage, a hash: age => list of records
byage = [:]
byage[record["age"]] = byage.get(record["age"], []) + [record]

byage.each{ age, list ->
    println "Age $age: ${list.collect{it.'name'}.join(', ')}"
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.10
//----------------------------------------------------------------------------------
// if you are using a Properties (see 8.16) then just use load
// and store (or storeToXML)
// variation to original cookbook as Groovy can use Java's object serialization
map = [1:'Jan', 2:'Feb', 3:'Mar']
// write
new File('months.dat').withObjectOutputStream{ oos ->
    oos.writeObject(map)
}
// reset
map = null
// read
new File('months.dat').withObjectInputStream{ ois ->
    map = ois.readObject()
}
println map // => [1:"Jan", 2:"Feb", 3:"Mar"]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.11
//----------------------------------------------------------------------------------
// Groovy automatically does pretty printing for some of the key types, e.g.
mylist = [[1,2,3], [4, [5,6,7], 8,9, [0,3,5]], 7, 8]
println mylist
// => [[1, 2, 3], [4, [5, 6, 7], 8, 9, [0, 3, 5]], 7, 8]

mydict = ["abc": "def", "ghi":[1,2,3]]
println mydict
// => ["abc":"def", "ghi":[1, 2, 3]]

// if you have another type of object you can use the built-in dump() method
class PetLover {
    def name
    def age
    def pets
}
p = new PetLover(name:'Jason', age:23, pets:[dog:'Rover',cat:'Garfield'])
println p
// => PetLover@b957ea
println p.dump()
// => <PetLover@b957ea name=Jason age=23 pets=["cat":"Garfield", "dog":"Rover"]>

// If that isn't good enough, you can use Boost (http://tara-indigo.org/daisy/geekscape/g2/128)
// or Jakarta Commons Lang *ToStringBuilders (jakarta.apache.org/commons)
// Here's an example of Boost, just extend the supplied Primordial class
//import au.net.netstorm.boost.primordial.Primordial
class Primordial{}
class PetLover2 extends Primordial { def name, age, pets }
println new PetLover2(name:'Jason', age:23, pets:[dog:'Rover',cat:'Garfield'])
// =>
// PetLover2[
//     name=Jason
//     age=23
//     pets={cat=Garfield, dog=Rover}
//     metaClass=groovy.lang.MetaClassImpl@1d8d39f[class PetLover2]
// ]

// using Commons Lang ReflectionToStringBuilder (equivalent to dump())
import org.apache.commons.lang.builder.*
class PetLover3 {
    def name, age, pets
    String toString() {
        ReflectionToStringBuilder.toString(this)
    }
}
println new PetLover3(name:'Jason', age:23, pets:[dog:'Rover',cat:'Garfield'])
// => PetLover3@196e136[name=Jason,age=23,pets={cat=Garfield, dog=Rover}]

// using Commons Lang ToStringBuilder if you want a custom format
class PetLover4 {
    def name, dob, pets
    String toString() {
        def d1 = dob.time; def d2 = (new Date()).time
        int age = (d2 - d1)/1000/60/60/24/365 // close approx good enough here
        return new ToStringBuilder(this).
            append("Pet Lover's name", name).
            append('Pets', pets).
            append('Age', age)
    }
}
println new PetLover4(name:'Jason', dob:new Date(83,03,04), pets:[dog:'Rover',cat:'Garfield'])
// => PetLover4@fdfc58[Pet Lover's name=Jason,Pets={cat=Garfield, dog=Rover},Age=23]
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.12
//----------------------------------------------------------------------------------
oldlist = [1, 2, 3]
newlist = new ArrayList(oldlist) // shallow copy
newlist = oldlist.clone() // shallow copy

oldmap = [a:1, b:2, c:3]
newmap = new HashMap(oldmap) // shallow copy
newmap = oldmap.clone() // shallow copy

oldarray = [1, 2, 3] as int[]
newarray = oldarray.clone()

// shallow copies copy a data structure, but don't copy the items in those
// data structures so if there are nested data structures, both copy and
// original will refer to the same object
mylist = ["1", "2", "3"]
newlist = mylist.clone()
mylist[0] = "0"
println "$mylist $newlist"
//=> ["0", "2", "3"] ["1", "2", "3"]

mylist = [["1", "2", "3"], 4]
newlist = mylist.clone()
mylist[0][0] = "0"
println "$mylist $newlist"
//=> [["0", "2", "3"], 4] [["0", "2", "3"], 4]

// standard deep copy implementation
def deepcopy(orig) {
     bos = new ByteArrayOutputStream()
     oos = new ObjectOutputStream(bos)
     oos.writeObject(orig); oos.flush()
     bin = new ByteArrayInputStream(bos.toByteArray())
     ois = new ObjectInputStream(bin)
     return ois.readObject()
}

newlist = deepcopy(oldlist) // deep copy
newmap  = deepcopy(oldmap)  // deep copy

mylist = [["1", "2", "3"], 4]
newlist = deepcopy(mylist)
mylist[0][0] = "0"
println "$mylist $newlist"
//=> [["0", "2", "3"], 4] [["1", "2", "3"], 4]

// See also:
// http://javatechniques.com/public/java/docs/basics/low-memory-deep-copy.html
// http://javatechniques.com/public/java/docs/basics/faster-deep-copy.html
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.13
//----------------------------------------------------------------------------------
// use Java's serialization capabilities as per 11.10
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.14
//----------------------------------------------------------------------------------
// There are numerous mechanisms for persisting objects to disk
// using Groovy and Java mechanisms. Some are completely transparent,
// some require some initialization only, others make the persistence
// mechanisms visible. Here is a site that lists over 20 options:
// http://www.java-source.net/open-source/persistence
// (This list doesn't include EJB offerings which typically
// require an application server or XML-based options)

// We'll just consider one possibility from prevayler.sf.net.
// This package doesn't make changes to persistent data transparent;
// instead requiring an explicit call via a transaction object.
// It saves all such transaction objects in a journal file so
// that it can rollback the system any number of times (or if
// you make use of the timestamp feature) to a particular point
// in time. It can also be set up to create snapshots which
// consolidate all changes made up to a certain point. The
// journaling will begin again from that point.
import org.prevayler.*
class ImportantHash implements Serializable {
    private map = [:]
    def putAt(key, value) { map[key] = value }
    def getAt(key) { map[key] }
}
class StoreTransaction implements Transaction {
    private val
    StoreTransaction(val) { this.val = val }
    void executeOn(prevayler, Date ignored) { prevayler.putAt(val,val*2) }
}
def save(n){ store.execute(new StoreTransaction(n)) }
store = PrevaylerFactory.createPrevayler(new ImportantHash(), "pleac11")
hash = store.prevalentSystem()
for (i in 0..1000) {
    save(i)
}
println hash[750] // => 1500

store = null; hash = null // *** could shutdown here

store = PrevaylerFactory.createPrevayler(new ImportantHash(), "pleac11")
hash = store.prevalentSystem()
println hash[750] // => 1500
//----------------------------------------------------------------------------------


// @@PLEAC@@_11.15
//----------------------------------------------------------------------------------
// bintree - binary tree demo program
class BinaryTree {
    def value, left, right
    BinaryTree(val) {
        value = val
        left = null
        right = null
    }

    // insert given value into proper point of
    // provided tree.  If no tree provided,
    // use implicit pass by reference aspect of @_
    // to fill one in for our caller.
    def insert(val) {
        if (val < value) {
            if (left) left.insert(val)
            else left = new BinaryTree(val)
        } else if (val > value) {
            if (right) right.insert(val)
            else right = new BinaryTree(val)
        } else println "double" // ignore double values
    }

    // recurse on left child,
    // then show current value,
    // then recurse on right child.
    def inOrder() {
        if (left) left.inOrder()
        print value + ' '
        if (right) right.inOrder()
    }

    // show current value,
    // then recurse on left child,
    // then recurse on right child.
    def preOrder() {
        print value + ' '
        if (left) left.preOrder()
        if (right) right.preOrder()
    }

    // show current value,
    // then recurse on left child,
    // then recurse on right child.
    def dumpOrder() {
        print this.dump() + ' '
        if (left) left.dumpOrder()
        if (right) right.dumpOrder()
    }

    // recurse on left child,
    // then recurse on right child,
    // then show current value.
    def postOrder() {
        if (left) left.postOrder()
        if (right) right.postOrder()
        print value + ' '
    }

    // find out whether provided value is in the tree.
    // if so, return the node at which the value was found.
    // cut down search time by only looking in the correct
    // branch, based on current value.
    def search(val) {
        if (val == value) {
            return this.dump()
        } else if (val < value) {
            return left ? left.search(val) : null
        } else {
            return right ? right.search(val) : null
        }
    }
}

// first generate 20 random inserts
test = new BinaryTree(500)
rand = new Random()
20.times{
    test.insert(rand.nextInt(1000))
}

// now dump out the tree all three ways
print "Pre order:  "; test.preOrder();  println ""
print "In order:   "; test.inOrder();   println ""
print "Post order: "; test.postOrder(); println ""

println "\nSearch?"
while ((item = System.in.readLine()?.trim()) != null) {
    println test.search(item.toInteger())
    println "\nSearch?"
}
// Randomly produces a tree such as:
//           -------- 500 ------
//         /                     \
//       181                     847
//     /    \                    /  \
//   3       204              814   970
//    \       /  \            /
//    126  196  414        800
//             /   \       /
//          353   438   621
//                /     /  \
//             423    604   776
//                   /     /
//                 517   765
//                      /
//                    646
//                   /
//                 630
// Pre order:
// 500 181 3 126 204 196 414 353 438 423 847 814 800 621 604 517 776 765 646 630 970
// In order:
// 3 126 181 196 204 353 414 423 438 500 517 604 621 630 646 765 776 800 814 847 970
// Post order:
// 126 3 196 353 423 438 414 204 181 517 604 630 646 765 776 621 800 814 970 847 500
//
// Search?
// 125
// null
//
// Search?
// 126
// <BinaryTree@ae97c4 value=126 left=null right=null>
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.0
//----------------------------------------------------------------------------------
// Groovy adopts many of the Java structuring conventions and terminology
// and adds some concepts of its own.
// Code-reuse can occur at the script, class, library, component or framework level.
// Source code including class file source and scripts are organised into packages.
// These can be thought of as like hierarchical folders or directories. Two class
// with the same name can be distinguished by having different packages. Compiled
// byte code and sometimes source code including scripts can be packaged up into
// jar files. Various conventions exist for packaging classes and resources in
// such a way to allow them to be easily reused. Some of these conventions allow
// reusable code to be placed within repositories for easy use by third parties.
// One such repository is the maven repository, e.g.: ibiblio.org/maven2
// When reusing classes, it is possible to compartmentalise knowledge of
// particular packages using multiple (potentially hierarchical) classloaders.
// By convention, package names are all lowercase. Class names are capitalized.
// Naming examples:
// package my.package1.name     // at most one per source file - at top of file
// class MyClass ...            // actually defines my.package1.name.MyClass
// import my.package1.name.MyClass  // allows package to be dropped within current file
// import my.package2.name.MyClass  // if class basenames are the same, can't
//                                  // import both, leave one fully qualified
// import my.package.name.*         // all classes in package can drop package prefix
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.1
//----------------------------------------------------------------------------------
// No equivalent export process exists for Groovy.

// If you have some Groovy functionality that you would like others to use
// you either make the source code available or compile it into class files
// and package those up in a jar file. Some subset of your class files will
// define the OO interface to your functionality, e.g. public methods,
// interfaces, etc. Depending on the circumstances, various conventions are
// used to indicate this functionality including Manifest files, javadocs,
// deployment descriptors, project metadata and dependency management files.
// See 12.18 for an example.
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.2
//----------------------------------------------------------------------------------
// Groovy supports both static and dynamic (strong) typing. When trying to
// compile or run files using static typing, the required classes referenced
// must be available. Classes used in more dynamic ways may be loaded (or
// created) at runtime. Errors in loading such dynamic cases are handled
// using the normal exception handling mechanisms.

// attempt to load an unknown resource or script:
try {
    evaluate(new File('doesnotexist.groovy'))
} catch (Exception FileNotFoundException) {
    println 'File not found, skipping ...'
}
// => File not found, skipping ...

// attempt to load an unknown class:
try {
    Class.forName('org.happytimes.LottoNumberGenerator')
} catch (ClassNotFoundException ex) {
    println 'Class not found, skipping ...'
}
// -> Class not found, skipping ...

// dynamicallly look for a database driver (slight variation to original cookbook)
// Note: this hypothetical example ignores certain issues e.g. different url
// formats for configuration when establishing a connection with the driver
candidates = [
    'oracle.jdbc.OracleDriver',
    'com.ibm.db2.jcc.DB2Driver',
    'com.microsoft.jdbc.sqlserver.SQLServerDriver',
    'net.sourceforge.jtds.jdbc.Driver',
    'com.sybase.jdbc3.jdbc.SybDriver',
    'com.informix.jdbc.IfxDriver',
    'com.mysql.jdbc.Driver',
    'org.postgresql.Driver',
    'com.sap.dbtech.jdbc.DriverSapDB',
    'org.hsqldb.jdbcDriver',
    'com.pointbase.jdbc.jdbcUniversalDriver',
    'org.apache.derby.jdbc.ClientDriver',
    'com.mckoi.JDBCDriver',
    'org.firebirdsql.jdbc.FBDriver',
    'sun.jdbc.odbc.JdbcOdbcDriver'
]
loaded = null
for (driver in candidates) {
    try {
        loaded = Class.forName(driver).newInstance()
        break
    } catch (Exception ex) { /* ignore */ }
}
println loaded?.class?.name // => sun.jdbc.odbc.JdbcOdbcDriver
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.3
//----------------------------------------------------------------------------------
// In Groovy (like Java), any static reference to an external class within
// your class will cause the external class to be loaded from the classpath.
// You can dynamically add to the classpath using:
// this.class.rootLoader.addURL(url)
// To delay loading of external classes, use Class.forName() or evaluate()
// the script separately as shown in 12.2.

// For the specific case of initialization code, here is another example:
// (The code within the anonymous { ... } block is called whenever the
// class is loaded.)
class DbHelper {
    def driver
    {
        if (System.properties.'driver' == 'oracle')
            driver = Class.forName('oracle.jdbc.OracleDriver')
        else
            driver = Class.forName('sun.jdbc.odbc.JdbcOdbcDriver')
    }
}
println new DbHelper().driver.name // => sun.jdbc.odbc.JdbcOdbcDriver
// call program with -Ddriver=oracle to swap to other driver

// A slightly related feature: If you want to load a script (typically in a
// server environment) whenever the source file changes, use GroovyScriptEngine()
// instead of GroovyShell() when embedding groovy.
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.4
//----------------------------------------------------------------------------------
// class variables are private unless access functions are defined
class Alpha {
    def x = 10
    private y = 12
}

println new Alpha().x    // => 10
println new Alpha().y    // => 12 when referenced inside source file, error outside
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.5
//----------------------------------------------------------------------------------
// You can examine the stacktrace to determine the calling class: see 10.4
// When executing a script from a groovy source file, you can either:
println getClass().classLoader.resourceLoader.loadGroovySource(getClass().name)
// => file:/C:/Projects/GroovyExamples/Pleac/classes/pleac12.groovy
// or for the initially started script when started using the standard .bat/.sh files
println System.properties.'script.name'
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.6
//----------------------------------------------------------------------------------
// For code which executes at class startup, see the initialization code block
// mechanism mentioned in 12.3. For code which should execute during shutdown
// see the finalize() method discussed (including limitations) in 13.2.
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.7
//----------------------------------------------------------------------------------
// Each JVM process may have its own classpath (and indeed its own version of Java
// runtime and libraries). You "simply" supply a classpath pointing to different
// locations to obtain different modules.
// Groovy augments the JVM behaviour by allowing individuals to have a ~/.groovy/lib
// directory with additional libraries (and potentially other resources).
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.8
//----------------------------------------------------------------------------------
// To make your code available to others could involve any of the following:
// (1) make your source code available
// (2) if you are creating a standard class, use the jar tool to package the
//     compiled code into a jar - this is then added to the classpath to use
// (3) if the jar relies on additional jars, this is sometimes specified in
//     a special manifest file within the jar
// (4) if the code is designed to run within a container environment, there
//     might be additional packaging, e.g. servlets might be packaged in a war
//     file - essentially a jar file with extra metadata in xml format.
// (5) you might also supply your package to a well known repository such as the
//     maven repository - and you will add dependency information in xml format
// (6) you may use platform specific installers to produce easily installable
//     components (e.g. windows .exe files or linux rpm's)
// (7) you may spackage up your components as a plugin (e.g. as an eclipse plugin)
//     this is also typically in jar/zip like format with additional metadata
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.9
//----------------------------------------------------------------------------------
// Groovy has no SelfLoader. Class loading can be delayed using external scripts
// and by using the Class.forName() approach discussed in 12.2/12.3. If you have
// critical performance issues, you can use these techniques and keep your class
// size small to maximise the ability to defer loading. There are other kinds of
// performance tradeoffs you can make too. Alot of work has been done with JIT
// (just in time) compilers for bytecode. You can pre-compile Groovy source files
// into bytecode using the groovy compiler (groovyc). You can also do this on
// the fly for scripts you know you are going to need shortly.
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.10
//----------------------------------------------------------------------------------
// Groovy has no AutoLoader. See the discussion in 12.9 for some techniques to
// impact program performance. There are many techniques available to speed up
// program performance (and in particular load speed). Some of these utilise
// techniques similar in nature to the technique used by the AutoLoader.
// As already mentioned, when you load a class into the JVM, any statically
// referenced class is also loaded. If you reference interfaces rather than
// concrete implementations, only the interface need be loaded. If you must
// reference a concrete implementation you can use either a Proxy class or
// classloader tricks to delay the loading of a full class (e.g. you supply a
// Proxy class with just one method implemented or a lazy-loading Proxy which
// loads the real class only when absolutely required)
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.11
//----------------------------------------------------------------------------------
// You can use Categories to override Groovy and Java base functionality.
println new Date().time // => 1169019557140

class DateCategory2 {  // the class name by convention ends with category
    // we can add new functionality
    static float getFloatTime(Date self) {
        return (float) self.getTime()
    }
    // we can override existing functionality (now seconds since 1970 not millis)
    static long asSeconds(Date self) {
        return (long) (self.getTime()/1000)
    }
}

use (DateCategory2) {
    println new Date().floatTime    // => 1.1690195E12
    println new Date().asSeconds()  // => 1169019557
}

// We can also use the 'as' keyword
class MathLib {
    def triple(n) { n * 4 }
    def twice(n) { n * 2 }
}
def m = new MathLib()
println m.twice(10)     // => 20
println m.triple(10)    // => 40 (Intentional Bug!)
// we might want to make use of some funtionality in the math
// library but want to later some of its features slightly or fix
// some bugs, we can simply import the original using a different name
import MathLib as BuggyMathLib
// now we could define our own MathLib which extended or had a delegate
// of the BuggyMathLib class
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.12
//----------------------------------------------------------------------------------
// Many Java and Groovy programs emit a stacktrace when an error occurs.
// This shows both the calling and called programs (with line numbers if
// supplied). Groovy pretties up stacktraces to show less noise. You can use -d
// or --debug on the commandline to force it to always produce full stacktraces.
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.13
//----------------------------------------------------------------------------------
// already have log10, how to create log11 to log100
(11..100).each { int base ->
    binding."log$base" = { int n -> Math.log(n) / Math.log(base) }
}
println log20(400)  // => 2.0
println log100(1000000)  // => 3.0 (displays 2.9999999999999996 using doubles)

// same thing again use currying
def logAnyBase = { base, n -> Math.log(n) / Math.log(base) }
(11..100).each { int base ->
    binding."log$base" = logAnyBase.curry(base)
}
println log20(400)  // => 2.0
println log100(1000000)  // => 3.0 (displays 2.9999999999999996 using doubles)
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.14
//----------------------------------------------------------------------------------
// Groovy intefaces with C in the same way as Java: using JNI
// For this discussion we will ignoring platform specific options and CORBA.
// This tutorial here describes how to allow Java (and hence Groovy) to
// call a C program which generates UUIDs:
// http://ringlord.com/publications/jni-howto/
// Here's another useful reference:
// http://weblogs.java.net/blog/kellyohair/archive/2006/01/compilation_of_1.html
// And of course, Sun's tutorial:
// http://java.sun.com/developer/onlineTraining/Programming/JDCBook/jni.html

// You might also want to consider SWIG which simplifies connecting
// C/C++ to many scripting languages including Java (and hence Groovy)
// More details: http://www.swig.org/
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.15
//----------------------------------------------------------------------------------
// See discussion for 12.14
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.16
//----------------------------------------------------------------------------------
// The standard documentation system for Java is JavaDoc.
// Documentation for JavaDoc is part of a Java installation.
// Groovy has a GroovyDoc tool planned which expands upon the JavaDoc tool
// but work on the tool hasn't progressed much as yet.
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.17
//----------------------------------------------------------------------------------
// Most libraries for Java (and hence Groovy) come precompiled. You simply download
// the jar and place it somewhere on your CLASSPATH.

// If only source code is available, you need to download the source and follow any
// instuctions which came with the source. Most projects use one of a handful of
// build tools to compile, test and package their artifacts. Typical ones are Ant
// and Maven which you need to install according to their respective instructions.

// If using Ant, you need to unpack the source files then type 'ant'.

// If using Maven, you need to unpack the source files then type 'maven'.

// If you are using Maven or Ivy for dependency management you can add
// the following lines to your project description file.
/*
    <dependency>
      <groupId>commons-collections</groupId>
      <artifactId>commons-collections</artifactId>
      <version>3.2</version>
    </dependency>
*/
// This will automatically download the particular version of the referenced
// library file and also provide hooks so that you can make this automatically
// available in your classpath.
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.18
//----------------------------------------------------------------------------------
// example groovy file for a "module"
//import org.apache.commons.lang.WordUtils
class WordUtils{}
class GroovyTestCase{}

class Greeter {
    def name
    Greeter(who) { name = WordUtils.capitalize(who) }
    def salute() { "Hello $name!" }
}

// test class
class GreeterTest extends GroovyTestCase {
    def testGreeting() {
        assert new Greeter('world').salute()
    }
}

// Typical Ant build file (could be in Groovy instead of XML):
/*
<?xml version="1.0"?>
<project name="sample" default="jar" basedir=".">
    <property name="src" value="src"/>
    <property name="build" value="build"/>

    <target name="init">
        <mkdir dir="${build}"/>
    </target>

    <target name="compile" depends="init">
        <mkdir dir="${build}/classes"/>
        <groovyc srcdir="${src}" destdir="${build}/classes"/>
    </target>

    <target name="test" depends="compile">
        <groovy src="${src}/GreeterTest.groovy">
    </target>

    <target name="jar" depends="compile,test">
        <mkdir dir="${build}/jar"/>
        <jar destfile="${build}/jar/Greeter.jar" basedir="${build}/classes">
            <manifest>
                <attribute name="Main-Class" value="Greeter"/>
            </manifest>
        </jar>
    </target>
</project>

*/

// Typical dependency management file
/*
<?xml version="1.0" encoding="UTF-8"?>
<project
  xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
          http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>groovy</groupId>
  <artifactId>module</artifactId>
  <name>Greeter</name>
  <version>1.0</version>
  <packaging>jar</packaging>
  <description>Greeter Module/description>
  <dependencies>
    <dependency>
      <groupId>commons-lang</groupId>
      <artifactId>commons-lang</artifactId>
      <version>2.2</version>
    </dependency>
  </dependencies>
</project>
*/
//----------------------------------------------------------------------------------


// @@PLEAC@@_12.19
//----------------------------------------------------------------------------------
// Searching available modules in repositories:
// You can browse the repositories online, e.g. ibiblio.org/maven2 or various
// plugins are available for IDEs which do this for you, e.g. JarJuggler for IntelliJ.

// Searching currently "installed" modules:
// Browse your install directory, view your maven POM file, look in your ~/.groovy/lib
// directory, turn on debug modes and watch classloader messages ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.0
//----------------------------------------------------------------------------------
// Classes and objects in Groovy are rather straigthforward
class Person2 {
    // Class variables (also called static attributes) are prefixed by the keyword static
    static personCounter=0
    def age, name               // this creates setter and getter methods
    private alive

    // object constructor
    Person2(age, name, alive = true) {            // Default arg like in C++
        this.age = age
        this.name = name
        this.alive = alive
        personCounter += 1
        // There is a '++' operator in Groovy but using += is often clearer.
    }

    def die() {
        alive = false
        println "$name has died at the age of $age."
        alive
    }

    def kill(anotherPerson) {
        println "$name is killing $anotherPerson.name."
        anotherPerson.die()
    }

    // methods used as queries generally start with is, are, will or can
    // usually have the '?' suffix
    def isStillAlive() {
        alive
    }

    def getYearOfBirth() {
        new Date().year - age
    }

    // Class method (also called static method)
    static getNumberOfPeople() { // accessors often start with get
                                 // in which case you can call it like
                                 // it was a field (without the get)
        personCounter
    }
}

// Using the class:
// Create objects of class Person
lecter = new Person2(47, 'Hannibal')
starling = new Person2(29, 'Clarice', true)
pazzi = new Person2(40, 'Rinaldo', true)

// Calling a class method
println "There are $Person2.numberOfPeople Person objects."

println "$pazzi.name is ${pazzi.alive ? 'alive' : 'dead'}."
lecter.kill(pazzi)
println "$pazzi.name is ${pazzi.isStillAlive() ? 'alive' : 'dead'}."

println "$starling.name was born in $starling.yearOfBirth."
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.1
//----------------------------------------------------------------------------------
// Classes may have no constructor.
class MyClass { }

aValidButNotVeryUsefulObject = new MyClass()

// If no explicit constructor is given a default implicit
// one which supports named parameters is provided.
class MyClass2 {
    def start = new Date()
    def age = 0
}
println new MyClass2(age:4).age // => 4

// One or more explicit constructors may also be provided
class MyClass3 {
    def start
    def age
    MyClass3(date, age) {
        start = date
        this.age = age
    }
}
println new MyClass3(new Date(), 20).age // => 20
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.2
//----------------------------------------------------------------------------------
// Objects are destroyed by the JVM garbage collector.
// The time of destroying is not predicated but left up to the JVM.
// There is no direct support for destructor. There is a courtesy
// method called finalize() which the JVM may call when disposing
// an object. If you need to free resources for an object, like
// closing a socket or killing a spawned subprocess, you should do
// it explicitly - perhaps by supporting your own lifecycle methods
// on your class, e.g. close().

class MyClass4{
    void finalize() {
        println "Object [internal id=${hashCode()}] is dying at ${new Date()}"
    }
}

// test code
50.times {
    new MyClass4()
}
20.times {
    System.gc()
}
// => (between 0 and 50 lines similar to below)
// Object [internal id=10884088] is dying at Wed Jan 10 16:33:33 EST 2007
// Object [internal id=6131844] is dying at Wed Jan 10 16:33:33 EST 2007
// Object [internal id=12245160] is dying at Wed Jan 10 16:33:33 EST 2007
// ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.3
//----------------------------------------------------------------------------------
// You can write getter and setter methods explicitly as shown below.
// One convention is to use set and get at the start of method names.
class Person2B {
    private name
    def getName() { name }
    def setName(name) { this.name = name }
}

// You can also just use def which auto defines default getters and setters.
class Person3 {
    def age, name
}

// Any variables marked as final will only have a default getter.
// You can also write an explicit getter. For a write-only variable
// just write only a setter.
class Person4 {
    final age      // getter only
    def name       // getter and setter
    private color  // private
    def setColor() { this.color = color } // setter only
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.4
//----------------------------------------------------------------------------------
class Person5 {
    // Class variables (also called static attributes) are prefixed by the keyword static
    static personCounter = 0

    static getPopulation() {
        personCounter
    }
    Person5() {
        personCounter += 1
    }
    void finalize() {
        personCounter -= 1
    }
}
people = []
10.times {
    people += new Person5()
}
println "There are ${Person5.population} people alive"
// => There are 10 people alive

alpha = new FixedArray()
println "Bound on alpha is $alpha.maxBounds"

beta = new FixedArray()
beta.maxBounds = 50
println "Bound on alpha is $alpha.maxBounds"

class FixedArray {
    static maxBounds = 100

    def getMaxBounds() {
        maxBounds
    }
    def setMaxBounds(value) {
        maxBounds = value
    }
}
// =>
// Bound on alpha is 100
// Bound on alpha is 50
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.5
//----------------------------------------------------------------------------------
// The fields of this struct-like class are dynamically typed
class DynamicPerson { def name, age, peers }
p = new DynamicPerson()
p.name = "Jason Smythe"
p.age = 13
p.peers = ["Wilbur", "Ralph", "Fred"]
p.setPeers(["Wilbur", "Ralph", "Fred"])     // alternative using implicit setter
p["peers"] = ["Wilbur", "Ralph", "Fred"]    // alternative access using name of field
println "At age $p.age, $p.name's first friend is ${p.peers[0]}"
// => At age 13, Jason Smythe's first friend is Wilbur

// The fields of this struct-like class are statically typed
class StaticPerson { String name; int age; List peers }
p = new StaticPerson(name:'Jason', age:14, peers:['Fred','Wilbur','Ralph'])
println "At age $p.age, $p.name's first friend is ${p.peers[0]}"
// => At age 14, Jason's first friend is Fred


class Family { def head, address, members }
folks = new Family(head:new DynamicPerson(name:'John',age:34))

// supply of own accessor method for the struct for error checking
class ValidatingPerson {
    private age
    def printAge() { println 'Age=' + age }
    def setAge(value) {
        if (!(value instanceof Integer))
            throw new IllegalArgumentException("Argument '${value}' isn't an Integer")
        if (value > 150)
            throw new IllegalArgumentException("Age ${value} is unreasonable")
        age = value
    }
}

// test ValidatingPerson
def tryCreate(arg) {
    try {
        new ValidatingPerson(age:arg).printAge()
    } catch (Exception ex) {
        println ex.message
    }
}

tryCreate(20)
tryCreate('Youngish')
tryCreate(200)
// =>
// Age=20
// Argument 'Youngish' isn't an Integer
// Age 200 is unreasonable
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.6
//----------------------------------------------------------------------------------
// Groovy objects are (loosely speaking) extended Java objects.
// Java's Object class provides a clone() method. The conventions of
// clone() are that if I say a = b.clone() then a and b should be
// different objects with the same type and value. Java doesn't
// enforce a class to implement a clone() method at all let alone
// require that one has to meet these conventions. Classes which
// do support clone() should implement the Cloneable interface and
// implement an equals() method.
// Groovy follows Java's conventions for clone().

class A implements Cloneable {
    def name
    boolean equals(Object other) {
        other instanceof A && this.name == other.name
    }
}
ob1 = new A(name:'My named thing')

ob2 = ob1.clone()
assert !ob1.is(ob2)
assert ob1.class == ob2.class
assert ob2.name == ob1.name
assert ob1 == ob2
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.7
//----------------------------------------------------------------------------------
class CanFlicker {
    def flicker(arg) { return arg * 2 }
}
methname = 'flicker'
assert new CanFlicker().invokeMethod(methname, 10) == 20
assert new CanFlicker()."$methname"(10) == 20

class NumberEcho {
    def one() { 1 }
    def two() { 2 }
    def three() { 3 }
}
obj = new NumberEcho()
// call methods on the object, by name
assert ['one', 'two', 'three', 'two', 'one'].collect{ obj."$it"() }.join() == '12321'
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.8
//----------------------------------------------------------------------------------
// Groovy can work with Groovy objects which inherit from a common base
// class called GroovyObject or Java objects which inherit from Object.

// the class of the object
assert 'a string'.class == java.lang.String

// Groovy classes are actually objects of class Class and they
// respond to methods defined in the Class class as well
assert 'a string'.class.class == java.lang.Class
assert !'a string'.class.isArray()

// ask an object whether it is an instance of particular class
n = 4.7f
println (n instanceof Integer)          // false
println (n instanceof Float)            // true
println (n instanceof Double)           // false
println (n instanceof String)           // false
println (n instanceof StaticPerson)     // false

// ask if a class or interface is either the same as, or is a
// superclass or superinterface of another class
println n.class.isAssignableFrom(Float.class)       // true
println n.class.isAssignableFrom(String.class)      // false

// can a Groovy object respond to a particular method?
assert new CanFlicker().metaClass.methods*.name.contains('flicker')

class POGO{}
println (obj.metaClass.methods*.name - new POGO().metaClass.methods*.name)
// => ["one", "two", "three"]
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.9
//----------------------------------------------------------------------------------
// Most classes in Groovy are inheritable
class Person6{ def age, name }
dude = new Person6(name:'Jason', age:23)
println "$dude.name is age $dude.age."

// Inheriting from Person
class Employee2B extends Person6 {
    def salary
}
empl = new Employee2B(name:'Jason', age:23, salary:200)
println "$empl.name is age $empl.age and has salary $empl.salary."

// Many built-in class can be inherited the same way
class WierdList extends ArrayList {
    int size() {  // size method in this class is overridden
        super.size() * 2
    }
}
a = new WierdList()
a.add('dog')
a.add('cat')
println a.size() // => 4
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.10
//----------------------------------------------------------------------------------
class Person7 { def firstname, surname; def getName(){ firstname + ' ' + surname } }
class Employee2 extends Person7 {
    def employeeId
    def getName(){ 'Employee Number ' + employeeId }
    def getRealName(){ super.getName() }
}
p = new Person7(firstname:'Jason', surname:'Smythe')
println p.name
// =>
// Jason Smythe
e = new Employee2(firstname:'Jason', surname:'Smythe', employeeId:12349876)
println e.name
println e.realName
// =>
// Employee Number 12349876
// Jason Smythe
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.11
//----------------------------------------------------------------------------------
// Groovy's built in constructor and auto getter/setter features
 // give you the required functionalty already but you could also
 // override invokeMethod() for trickier scenarios.
class Person8 {
    def name, age, peers, parent
    def newChild(args) { new Person8(parent:this, *:args) }
}

dad = new Person8(name:'Jason', age:23)
kid = dad.newChild(name:'Rachel', age:2)
println "Kid's parent is ${kid.parent.name}"
// => Kid's parent is Jason

// additional fields ...
class Employee3 extends Person8 { def salary, boss }
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.12
//----------------------------------------------------------------------------------
// Fields marked as private in Groovy can't be trampled by another class in
// the class hierarchy
class Parent2 {
    private name // my child's name
    def setChildName(value) { name = value }
    def getChildName() { name }
}
class GrandParent extends Parent2 {
    private name // my grandchild's name
    def setgrandChildName(value) { name = value }
    def getGrandChildName() { name }
}
g = new GrandParent()
g.childName = 'Jason'
g.grandChildName = 'Rachel'
println g.childName       // => Jason
println g.grandChildName  // => Rachel
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.13
//----------------------------------------------------------------------------------
// The JVM garbage collector copes with circular structures.
// You can test it with this code:
class Person9 {
    def friend
    void finalize() {
        println "Object [internal id=${hashCode()}] is dying at ${new Date()}"
    }
}

def makeSomeFriends() {
    def first = new Person9()
    def second = new Person9(friend:first)
    def third = new Person9(friend:second)
    def fourth = new Person9(friend:third)
    def fifth = new Person9(friend:fourth)
    first.friend = fifth
}

makeSomeFriends()
100.times{
    System.gc()
}
// =>
// Object [internal id=24478976] is dying at Tue Jan 09 22:24:31 EST 2007
// Object [internal id=32853087] is dying at Tue Jan 09 22:24:31 EST 2007
// Object [internal id=23664622] is dying at Tue Jan 09 22:24:31 EST 2007
// Object [internal id=10630672] is dying at Tue Jan 09 22:24:31 EST 2007
// Object [internal id=25921812] is dying at Tue Jan 09 22:24:31 EST 2007
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.14
//----------------------------------------------------------------------------------
// Groovy provides numerous methods which are automatically associated with
// symbol operators, e.g. here is '<=>' which is associated with compareTo()
// Suppose we have a class with a compareTo operator, such as:
class Person10 implements Comparable {
    def firstname, initial, surname
    Person10(f,i,s) { firstname = f; initial = i; surname = s }
    int compareTo(other) { firstname <=> other.firstname }
}
a = new Person10('James', 'T', 'Kirk')
b = new Person10('Samuel', 'L', 'Jackson')
println a <=> b
// => -1

// we can override the existing Person10's <=> operator as below
// so that now comparisons are made using the middle initial
// instead of the fisrtname:
class Person11 extends Person10 {
    Person11(f,i,s) { super(f,i,s) }
    int compareTo(other) { initial <=> other.initial }
}

a = new Person11('James', 'T', 'Kirk')
b = new Person11('Samuel', 'L', 'Jackson')
println a <=> b
// => 1

// we could also in general use Groovy's categories to extend class functionality.

// There is no way to directly overload the '""' (stringify)
// operator in Groovy.  However, by convention, classes which
// can reasonably be converted to a String will define a
// 'toString()' method as in the TimeNumber class defined below.
// The 'println' method will automatcally call an object's
// 'toString()' method as is demonstrated below. Furthermore,
// an object of that class can be used most any place where the
// interpreter is looking for a String value.

//---------------------------------------
// NOTE: Groovy has various built-in Time/Date/Calendar classes
// which would usually be used to manipulate time objects, the
// following is supplied for educational purposes to demonstrate
// operator overloading.
class TimeNumber {
    def h, m, s
    TimeNumber(hour, min, sec) { h = hour; m = min; s = sec }

    def toDigits(s) { s.toString().padLeft(2, '0') }
    String toString() {
        return toDigits(h) + ':' + toDigits(m) + ':' + toDigits(s)
    }

    def plus(other) {
        s = s + other.s
        m = m + other.m
        h = h + other.h
        if (s >= 60) {
            s %= 60
            m += 1
        }
        if (m >= 60) {
            m %= 60
            h += 1
        }
        return new TimeNumber(h, m, s)
    }

}

t1 = new TimeNumber(0, 58, 59)
sec = new TimeNumber(0, 0, 1)
min = new TimeNumber(0, 1, 0)
println t1 + sec + min + min

//-----------------------------
// StrNum class example: Groovy's builtin String class already has the
// capabilities outlined in StrNum Perl example, however the '*' operator
// on Groovy's String class acts differently: It creates a string which
// is the original string repeated N times.
//
// Using Groovy's String class as is in this example:
x = "Red"; y = "Black"
z = x+y
r = z*3 // r is "RedBlackRedBlackRedBlack"
println "values are $x, $y, $z, and $r"
println "$x is ${x < y ? 'LT' : 'GE'} $y"
// prints:
// values are Red, Black, RedBlack, and RedBlackRedBlackRedBlack
// Red is GE Black

//-----------------------------
class FixNum {
    def REGEX = /(\.\d*)/
    static final DEFAULT_PLACES = 0
    def float value
    def int places
    FixNum(value) {
        initValue(value)
        def m = value.toString() =~ REGEX
        if (m) places = m[0][1].size() - 1
        else places = DEFAULT_PLACES
    }
    FixNum(value, places) {
        initValue(value)
        this.places = places
    }
    private initValue(value) {
        this.value = value
    }

    def plus(other) {
        new FixNum(value + other.value, [places, other.places].max())
    }

    def multiply(other) {
        new FixNum(value * other.value, [places, other.places].max())
    }

    def div(other) {
        println "DEUG: Divide = ${value/other.value}"
        def result = new FixNum(value/other.value)
        result.places = [places,other.places].max()
        result
    }

    String toString() {
        //m = value.toString() =~ /(\d)/ + REGEX
        String.format("STR%s: %.${places}f", [this.class.name, value as float] as Object[])
    }
}

x = new FixNum(40)
y = new FixNum(12, 0)

println "sum of $x and $y is ${x+y}"
println "product of $x and $y is ${x*y}"

z = x/y
println "$z has $z.places places"
z.places = 2
println "$z now has $z.places places"

println "div of $x by $y is $z"
println "square of that is ${z*z}"
// =>
// sum of STRFixNum: 40 and STRFixNum: 12 is STRFixNum: 52
// product of STRFixNum: 40 and STRFixNum: 12 is STRFixNum: 480
// DEUG: Divide = 3.3333333333333335
// STRFixNum: 3 has 0 places
// STRFixNum: 3.33 now has 2 places
// div of STRFixNum: 40 by STRFixNum: 12 is STRFixNum: 3.33
// square of that is STRFixNum: 11.11
//----------------------------------------------------------------------------------


// @@PLEAC@@_13.15
//----------------------------------------------------------------------------------
// Groovy doesn't use the tie terminology but you can achieve
// similar results with Groovy's metaprogramming facilities
class ValueRing {
    private values
    def add(value) { values.add(0, value) }
    def next() {
        def head = values[0]
        values = values[1..-1] + head
        return head
    }
}
ring = new ValueRing(values:['red', 'blue'])
def getColor() { ring.next() }
void setProperty(String n, v) {
    if (n == 'color') { ring.add(v); return }
    super.setProperty(n,v)
}

println "$color $color $color $color $color $color"
// => red blue red blue red blue

color = 'green'
println "$color $color $color $color $color $color"
// => green red blue green red blue

// Groovy doesn't have the $_ implicit variable so we can't show an
// example that gets rid of it. We can however show an example of how
// you could add in a simplified version of that facility into Groovy.
// We use Groovy's metaProgramming facilities. We execute our script
// in a new GroovyShell so that we don't affect subsequent examples.
// script:
x = 3
println "$_"
y = 'cat' * x
println "$_"

// metaUnderscore:
//void setProperty(String n, v) {
//    super.setProperty('_',v)
//    super.setProperty(n,v)
//}

new GroovyShell().evaluate(metaUnderscore + script)
// =>
// 3
// catcatcat

// We can get a little bit fancier by making an UnderscoreAware class
// that wraps up some of this functionality. This is not recommended
// as good Groovy style but mimicks the $_ behaviour in a sinple way.
class UnderscoreAware implements GroovyInterceptable {
    private _saved
    void setProperty(String n, v) {
        _saved = v
        this.metaClass.setProperty(this, n, v)
    }
    def getProperty(String n) {
        if (n == '_') return _saved
        this.metaClass.getProperty(this, n)
    }
    def invokeMethod(String name, Object args) {
        if (name.startsWith('print') && args.size() == 0)
            args = [_saved] as Object[]
        this.metaClass.invokeMethod(this, name, args)
    }
}

class PerlishClass extends UnderscoreAware {
    private _age
    def setAge(age){ _age = age }
    def getAge(){ _age }
    def test() {
        age = 25
        println "$_"   // explicit $_ supported
        age++
        println()      // implicit $_ will be injected
    }
}

def x = new PerlishClass()
x.test()
// =>
// 25
// 26

// Autoappending hash:
class AutoMap extends HashMap {
    void setProperty(String name, v) {
        if (containsKey(name)) {
            put(name, get(name) + v)
        } else {
            put(name, [v])
        }
    }
}
m = new AutoMap()
m.beer = 'guinness'
m.food = 'potatoes'
m.food = 'peas'
println m
// => ["food":["potatoes", "peas"], "beer":["guinness"]]

// Case-Insensitive Hash:
class FoldedMap extends HashMap {
    void setProperty(String name, v) {
        put(name.toLowerCase(), v)
    }
    def getProperty(String name) {
        get(name.toLowerCase())
    }
}
tab = new FoldedMap()
tab.VILLAIN = 'big '
tab.herOine = 'red riding hood'
tab.villain += 'bad wolf'
println tab
// => ["heroine":"red riding hood", "villain":"big bad wolf"]

// Hash That "Allows Look-Ups by Key or Value":
class RevMap extends HashMap {
    void setProperty(String n, v) { put(n,v); put(v,n) }
    def remove(n) { super.remove(get(n)); super.remove(n) }
}
rev = new RevMap()
rev.Rojo = 'Red'
rev.Azul = 'Blue'
rev.Verde = 'Green'
rev.EVIL = [ "No way!", "Way!!" ]
rev.remove('Red')
rev.remove('Azul')
println rev
// =>
// [["No way!", "Way!!"]:"EVIL", "EVIL":["No way!", "Way!!"], "Verde":"Green", "Green":"Verde"]

// Infinite loop scenario:
// def x(n) { x(++n) }; x(0)
// => Caught: java.lang.StackOverflowError

// Multiple Streams scenario:
class MultiStream2 extends PrintStream {
    def streams
    MultiStream2(List streams) {
        super(streams[0])
        this.streams = streams
    }
    void println(String x) {
        streams.each{ it.println(x) }
    }
}
tee = new MultiStream2([System.out, System.err])
tee.println ('This goes two places')
// =>
// This goes two places
// This goes two places
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.0
//----------------------------------------------------------------------------------
//As discussed in 14.1, many database options exist, one of which is JDBC.
//Over 200 JDBC drivers are listed at the following URL:
//http://developers.sun.com/product/jdbc/drivers/browse_all.jsp
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.1
//----------------------------------------------------------------------------------
// Groovy can make use of various Java persistence libraries and has special
// support built-in (e.g. datasets) for interacting wth RDBMS systems.
// Some of the options include:
//   object serialization (built in to Java)
//   pbeans: pbeans.sf.net
//   prevayler: http://www.prevayler.org
//   Berkeley DB Java edition: http://www.oracle.com/database/berkeley-db/je/
//   JDBC: Over 200 drivers are listed at http://developers.sun.com/product/jdbc/drivers
//   Datasets (special Groovy support)
//   XML via e.g. xstream or JAXB or XmlBeans or ...
//   ORM: over 20 are listed at http://java-source.net/open-source/persistence
//   JNI: can be used directly on a platform that supports e.g. DBM or via
//     a cross platform API such as Apache APR which includes DBM routines:
//     http://apr.apache.org/docs/apr-util/0.9/group__APR__Util__DBM.html
//   jmork: used for Firefox/Thunderbird databases, e.g. address books, history files
// JDBC or Datasets would normally be most common for all examples in this chapter.


// Example shown using berkeley db Java edition - not quite as transparent as
// cookbook example as Berkeley DB Java addition makes transactions visible.
import com.sleepycat.je.*
tx = null
envHome = new File("D:/Projects/GroovyExamples/Pleac/data/db")

myEnvConfig = new EnvironmentConfig()
myEnvConfig.setAllowCreate(true)
myEnv = new Environment(envHome, myEnvConfig)

myDbConfig = new DatabaseConfig()
myDbConfig.setAllowCreate(true)
myDb = myEnv.openDatabase(tx, "vendorDB", myDbConfig)

theKey = new DatabaseEntry("key".getBytes("UTF-8"))
theData = new DatabaseEntry("data".getBytes("UTF-8"))
myDb.put(tx, theKey, theData)
if (myDb.get(tx, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
    key = new String(theKey.data, "UTF-8")
    foundData = new String(theData.data, "UTF-8")
    println "For key: '$key' found data: '$foundData'."
}
myDb.delete(tx, theKey)
myDb.close()
myEnv.close()


// userstats using pbeans
//import net.sourceforge.pbeans.*
interface Persistent{}
class Store{}
// on *nix use: whotext = "who".execute().text
whotext = '''
gnat ttyp1 May 29 15:39 (coprolith.frii.com)
bill ttyp1 May 28 15:38 (hilary.com)
gnit ttyp1 May 27 15:37 (somewhere.org)
'''

class LoginInfo implements Persistent {
    LoginInfo() {}
    LoginInfo(name) { this.name = name; loginCount = 1 }
    String name
    int loginCount
}

def printAllUsers(store) {
    printUsers(store, store.select(LoginInfo.class).collect{it.name}.sort())
}

def printUsers(store, list) {
    list.each{
        println "$it  ${store.selectSingle(LoginInfo.class, 'name', it).loginCount}"
    }
}

def addUsers(store) {
    whotext.trim().split('\n').each{
        m = it =~ /^(\S+)/
        name = m[0][1]
        item = store.selectSingle(LoginInfo.class, 'name', name)
        if (item) {
            item.loginCount++
            store.save(item)
        } else {
            store.insert(new LoginInfo(name))
        }
    }
}

//def ds = new org.hsqldb.jdbc.jdbcDataSource()
def ds = new jdbcDataSource()
ds.database = 'jdbc:hsqldb:hsql://localhost/mydb'
ds.user = 'sa'
ds.password = ''
store = new Store(ds)
if (args.size() == 0) {
    addUsers(store)
} else if (args == ['ALL']) {
    printAllUsers(store)
} else {
    printUsers(store, args)
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.2
//----------------------------------------------------------------------------------
// Groovy would normally use JDBC here (see 14.1 for details)
//import com.sleepycat.je.*
tx = null
envHome = new File("D:/Projects/GroovyExamples/Pleac/data/db")

myEnvConfig = new EnvironmentConfig()
myEnvConfig.setAllowCreate(true)
myEnv = new Environment(envHome, myEnvConfig)

myDbConfig = new DatabaseConfig()
myDbConfig.setAllowCreate(true)
myDb = myEnv.openDatabase(tx, "vendorDB", myDbConfig)

theKey = new DatabaseEntry("key".getBytes("UTF-8"))
theData = new DatabaseEntry("data".getBytes("UTF-8"))
myDb.put(tx, theKey, theData)
myDb.close()
// clear out database
returnCount = true
println myEnv.truncateDatabase(tx, "vendorDB", returnCount) + ' records deleted'
// remove database
myEnv.removeDatabase(tx, "vendorDB")
myEnv.close()
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.3
//----------------------------------------------------------------------------------
// Original cookbook example not likely in Groovy.
// Here is a more realistic example, copying pbeans -> jdbc
// Creation of pbeans database not strictly needed but shown for completion

class jdbcDataSource {}
//import net.sourceforge.pbeans.*
import groovy.sql.Sql

//ds = new org.hsqldb.jdbc.jdbcDataSource()
ds = new jdbcDataSource()
ds.database = 'jdbc:hsqldb:hsql://localhost/mydb'
ds.user = 'sa'
ds.password = ''
store = new Store(ds)

class PersonX implements Persistent {
    String name
    String does
    String email
}

// populate with test data
store.insert(new PersonX(name:'Tom Christiansen', does:'book author', email:'tchrist@perl.com'))
store.insert(new PersonX(name:'Tom Boutell', does:'Poet Programmer', email:'boutell@boutell.com'))

people = store.select(PersonX.class)

db = new Sql(ds)

db.execute 'CREATE TABLE people ( name VARCHAR, does VARCHAR, email VARCHAR );'
people.each{ p ->
    db.execute "INSERT INTO people ( name, does, email ) VALUES ($p.name,$p.does,$p.email);"
}
db.eachRow("SELECT * FROM people where does like 'book%'"){
    println "$it.name, $it.does, $it.email"
}
db.execute 'DROP TABLE people;'
// => Tom Christiansen, book author, tchrist@perl.com
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.4
//----------------------------------------------------------------------------------
// Groovy would normally use JDBC here (see 14.1 for details)
//import com.sleepycat.je.*

def copyEntries(indb, outdb) {
    cursor = indb1.openCursor(null, null)
    while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS)
        outdb.out(tx, foundKey, foundData)
    cursor.close()
}

tx = null
envHome = new File("D:/Projects/GroovyExamples/Pleac/data/db")

myEnvConfig = new EnvironmentConfig()
myEnvConfig.setAllowCreate(true)
myEnv = new Environment(envHome, myEnvConfig)

myDbConfig = new DatabaseConfig()
myDbConfig.setAllowCreate(true)
indb1 = myEnv.openDatabase(tx, "db1", myDbConfig)
indb2 = myEnv.openDatabase(tx, "db2", myDbConfig)
outdb = myEnv.openDatabase(tx, "db3", myDbConfig)
foundKey = new DatabaseEntry()
foundData = new DatabaseEntry()
copyEntries(indb1, outdb)
copyEntries(indb2, outdb)
cursor = indb2.openCursor(null, null)
while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS)
    outdb.out(tx, foundKey, foundData)
cursor.close()
indb1.close()
indb2.close()
outdb.close()
myEnv.close()
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.5
//----------------------------------------------------------------------------------
// If you are using a single file based persistence mechanism you can
// use the file locking mechanisms mentioned in 7.11 otherwise the
// database itself or the ORM layer will provide locking mechanisms.
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.6
//----------------------------------------------------------------------------------
// N/A for most Java/Groovy persistent technologies.
// Use indexes for RDBMS systems.
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.7
//----------------------------------------------------------------------------------
 // We can write a category that allows the ArrayList class
 // to be persisted as required.
 class ArrayListCategory {
     static file = new File('/temp.txt')
     static void save(ArrayList self) {
         def LS = System.getProperty('line.separator')
         file.withWriter{ w ->
             self.each{ w.write(it + LS)  }
         }
     }
 }

 lines = '''
 zero
 one
 two
 three
 four
 '''.trim().split('\n') as ArrayList

 use(ArrayListCategory) {
     println "ORIGINAL"
     for (i in 0..<lines.size())
         println "${i}: ${lines[i]}"

     a = lines[-1]
     lines[-1] = "last"
     println "The last line was [$a]"

     a = lines[0]
     lines = ["first"] + lines[1..-1]
     println "The first line was [$a]"

     lines.add(3, 'Newbie')
     lines.add(1, 'New One')

     lines.remove(3)

     println "REVERSE"
     (lines.size() - 1).downto(0){ i ->
         println "${i}: ${lines[i]}"
     }
     lines.save()
 }
 // =>
 // ORIGINAL
 // 0: zero
 // 1: one
 // 2: two
 // 3: three
 // 4: four
 // The last line was [four]
 // The first line was [zero]
 // REVERSE
 // 5: last
 // 4: three
 // 3: Newbie
 // 2: one
 // 1: New One
 // 0: first
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.8
//----------------------------------------------------------------------------------
// example using pbeans
//import net.sourceforge.pbeans.*
//ds = new org.hsqldb.jdbc.jdbcDataSource()
ds = new jdbcDataSource()
ds.database = 'jdbc:hsqldb:hsql://localhost/mydb'
ds.user = 'sa'
ds.password = ''
store = new Store(ds)

class PersonY implements Persistent {
    String name
    String does
    String email
}

name1 = 'Tom Christiansen'
name2 = 'Tom Boutell'

store.insert(new PersonY(name:name1, does:'book author', email:'tchrist@perl.com'))
store.insert(new PersonY(name:name2, does:'shareware author', email:'boutell@boutell.com'))

tom1 = store.selectSingle(PersonY.class, 'name', name1)
tom2 = store.selectSingle(PersonY.class, 'name', name2)

println "Two Toming: $tom1 $tom2"

if (tom1.name == tom2.name && tom1.does == tom2.does && tom1.email == tom2.email)
    println "You're having runtime fun with one Tom made two."
else
    println "No two Toms are ever alike"

tom2.does = 'Poet Programmer'
store.save(tom2)
// =>
// Two Toming: Person@12884e0 Person@8ab708
// No two Toms are ever alike
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.9
//----------------------------------------------------------------------------------
// Use one of the mechanisms mentioned in 14.1 to load variables at the start
// of the script and save them at the end. You can save the binding, individual
// variables, maps of variables or composite objects.
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.10
//----------------------------------------------------------------------------------
import groovy.sql.Sql

users = ['20':'Joe Bloggs', '40':'Bill Clinton', '60':'Ben Franklin']

//def source = new org.hsqldb.jdbc.jdbcDataSource()
def source = new jdbcDataSource()
source.database = 'jdbc:hsqldb:mem:PLEAC'
source.user = 'sa'
source.password = ''
db = new Sql(source)

db.execute 'CREATE TABLE users ( uid INT, login CHAR(8) );'
users.each{ uid, login ->
    db.execute "INSERT INTO users ( uid, login ) VALUES ($uid,$login);"
}
db.eachRow('SELECT uid, login FROM users WHERE uid < 50'){
    println "$it.uid $it.login"
}
db.execute 'DROP TABLE users;'
// =>
// 20 Joe Bloggs
// 40 Bill Clinton
//----------------------------------------------------------------------------------


// @@PLEAC@@_14.11
//----------------------------------------------------------------------------------
// variation to cookbook: uses Firefox instead of Netscape, always assumes
// argument is a regex, has some others args, retains no args to list all

// uses jmork mork dbm reading library:
//     http://www.smartwerkz.com/projects/jmork/index.html
//import mork.*
class MorkDocument{}
class CliBuilder{}
def cli = new CliBuilder()
cli.h(longOpt: 'help', 'print this message')
cli.e(longOpt: 'exclude', 'exclude hidden history entries (js, css, ads and images)')
cli.c(longOpt: 'clean', 'clean off url query string when reporting urls')
cli.v(longOpt: 'verbose', 'show referrer and first visit date')
def options = cli.parse(args)
if (options.h) { cli.usage(); System.exit(0) }
regex = options.arguments()
if (regex) regex = regex[0]
reader = new FileReader('Pleac/data/history.dat')
morkDocument = new MorkDocument(reader)
tables = morkDocument.tables
tables.each{ table ->
    table.rows.each { row ->
        url = row.getValue('URL')
        if (options.c) url = url.tokenize('?')[0]
        if (!regex || url =~ regex) {
            if (!options.e || row.getValue('Hidden') != '1') {
                println "$url\n    Last Visited: ${date(row,'LastVisitDate')}"
                if (options.v) {
                    println "    First Visited: ${date(row,'FirstVisitDate')}"
                    println "    Referrer: ${row.getValue('Referrer')}"
                }
            }
        }
    }
}
def date(row, key) {
    return new Date((long)(row.getValue(key).toLong()/1000))
}
// $ groovy gfh -ev oracle' =>
// http://www.oracle.com/technology/products/jdev/index.html
//     Last Visited: Thu Feb 15 20:20:36 EST 2007
//     First Visited: Thu Feb 15 20:20:36 EST 2007
//     Referrer: http://docs.codehaus.org/display/GROOVY/Oracle+JDeveloper+Plugin
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.1
//----------------------------------------------------------------------------------
// The are several Java options builder packages available. Some popular ones:
//   Apache Jakarta Commons CLI: http://jakarta.apache.org/commons/cli/
//   jopt-simple: http://jopt-simple.sourceforge.net
//   args4j: https://args4j.dev.java.net/ (requires Java 5 with annotations)
//   jargs: http://jargs.sourceforge.net/
//   te-code: http://te-code.sourceforge.net/article-20041121-cli.html
// Most of these can be used from Groovy with some Groovy code benefits.
// Groovy also has the CliBuilder built right in.


// CliBuilder example
cli = new CliBuilder()
cli.v(longOpt: 'verbose', 'verbose mode')
cli.D(longOpt: 'Debug', 'display debug info')
cli.o(longOpt: 'output', 'use/specify output file')
options = cli.parse(args)
if (options.v) // ...
if (options.D) println 'Debugging info available'
if (options.o) {
    println 'Output file flag was specified'
    println "Output file is ${options.o}"
}
// ...

// jopt-simple example 1 (short form)
cli = new joptsimple.OptionParser("vDo::")
options = cli.parse(args)
if (options.wasDetected('o')) {
    println 'Output file flag was specified.'
    println "Output file is ${options.argumentsOf('o')}"
}
// ...


// jopt-simple example 2 (declarative form)
op = new joptsimple.OptionParser()
VERBOSE = 'v';  op.accepts( VERBOSE,  "verbose mode" )
DEBUG   = 'D';  op.accepts( DEBUG,    "display debug info" )
OUTPUT  = 'o';  op.accepts( OUTPUT,   "use/specify output file" ).withOptionalArg().
    describedAs( "file" ).ofType( File.class )
options = op.parse(args)
params = options.nonOptionArguments()
if (options.wasDetected( DEBUG )) println 'Debugging info available'
// ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.2
//----------------------------------------------------------------------------------
// Groovy like Java can be run in a variety of scenarios, not just interactive vs
// non-interative, e.g. within a servlet container. Sometimes InputStreams and other
// mechanisms are used to hide away differences between the different containers
// in which code is run; other times, code needs to be written purpose-built for
// the container in which it is running. In most situations where the latter applies
// the container will have specific lifecycle mechanisms to allow the code to
// access specific needs, e.g. javax.servlet.ServletRequest.getInputStream()
// rather than System.in
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.3
//----------------------------------------------------------------------------------
// Idiomatically Groovy encourages GUI over text-based applications where a rich
// interface is desirable. Libraries for richer text-based interfaces include:
// jline: http://jline.sourceforge.net
// jcurses: http://sourceforge.net/projects/javacurses/
// java-readline: http://java-readline.sourceforge.net
// enigma console: http://sourceforge.net/projects/enigma-shell/
// Note: Run examples using these libraries from command line not inside an IDE.

// If you are using a terminal/console that understands ANSI codes
// (excludes WinNT derivatives) you can just print the ANSI codes
print ((char)27 + '[2J')

// jline has constants for ANSI codes
//import jline.ANSIBuffer
//print ANSIBuffer.ANSICodes.clrscr()
// Also available through ConsoleReader.clearScreen()
println new jline.console.ConsoleReader().clearScreen()

// Using jcurses
import jcurses.system.*
bg = CharColor.BLACK
fg = CharColor.WHITE
screenColors = new CharColor(bg, fg)
Toolkit.clearScreen(screenColors)
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.4
//----------------------------------------------------------------------------------
// Not idiomatic for Groovy to use text-based applications here.

// Using jcurses: http://sourceforge.net/projects/javacurses/
// use Toolkit.screenWidth and Toolkit.screenHeight

// 'barchart' example
import jcurses.system.Toolkit
numCols = Toolkit.screenWidth
rand = new Random()
if (numCols < 20) throw new RuntimeException("You must have at least 20 characters")
values = (1..5).collect { rand.nextInt(20) }  // generate rand values
max = values.max()
ratio = (numCols - 12)/max
values.each{ i ->
    printf('%8.1f %s\n', [i as double, "*" * ratio * i])
}

// gives, for example:
//   15.0 *******************************
//   10.0 *********************
//    5.0 **********
//   14.0 *****************************
//   18.0 **************************************
// Run from command line not inside an IDE which may give false width/height values.
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.5
//----------------------------------------------------------------------------------
// Idiomatically Groovy encourages GUI over text-based applications where a rich
// interface is desirable. See 15.3 for richer text-based interface libraries.
// Note: Run examples using these libraries from command line not inside an IDE.

// If you are using a terminal/console that understands ANSI codes
// (excludes WinNT derivatives) you can just print the ANSI codes
ESC = "${(char)27}"
redOnBlack = ESC + '[31;40m'
reset = ESC + '[0m'
println (redOnBlack + 'Danger, Will Robinson!' + reset)

// jline has constants for ANSI codes
//import jline.ANSIBuffer
class ANSIBuffer{}

redOnBlack = ANSIBuffer.ANSICodes.attrib(31) + ANSIBuffer.ANSICodes.attrib(40)
reset = ANSIBuffer.ANSICodes.attrib(0)
println redOnBlack + 'Danger, Will Robinson!' + reset

// Using JavaCurses
//import jcurses.system.*
//import jcurses.widgets.*
whiteOnBlack = new CharColor(CharColor.BLACK, CharColor.WHITE)
Toolkit.clearScreen(whiteOnBlack)
redOnBlack = new CharColor(CharColor.BLACK, CharColor.RED)
Toolkit.printString("Danger, Will Robinson!", 0, 0, redOnBlack)
Toolkit.printString("This is just normal text.", 0, 1, whiteOnBlack)
// Blink not supported by JavaCurses

// Using jline constants for Blink
blink = ANSIBuffer.ANSICodes.attrib(5)
reset = ANSIBuffer.ANSICodes.attrib(0)
println (blink + 'Do you hurt yet?' + reset)

// Using jline constants for Coral snake rhyme
def ansi(code) { ANSIBuffer.ANSICodes.attrib(code) }
redOnBlack = ansi(31) + ansi(40)
redOnYellow = ansi(31) + ansi(43)
greenOnCyanBlink = ansi(32) + ansi(46) + ansi(5)
reset = ansi(0)
println redOnBlack + "venom lack"
println redOnYellow + "kill that fellow"
println greenOnCyanBlink + "garish!" + reset
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.6
//----------------------------------------------------------------------------------
// Default Java libraries buffer System.in by default.

// Using JavaCurses:
//import jcurses.system.Toolkit

print 'Press a key: '
println "\nYou pressed the '${Toolkit.readCharacter().character}' key"

// Also works for special keys:
//import jcurses.system.InputChar

print "Press the 'End' key to finish: "
ch = Toolkit.readCharacter()
assert ch.isSpecialCode()
assert ch.code == InputChar.KEY_END

// See also jline Terminal#readCharacter() and Terminal#readVirtualKey()
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.7
//----------------------------------------------------------------------------------
print "${(char)7}"

// Using jline constant
print "${jline.ConsoleOperations.KEYBOARD_BELL}"
// Also available through ConsoleReader.beep()

// Using JavaCurses (Works only with terminals that support 'beeps')
//import jcurses.system.Toolkit
Toolkit.beep()
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.8
//----------------------------------------------------------------------------------
// I think you would need to resort to platform specific calls here,
// E.g. on *nix systems call 'stty' using execute().
// Some things can be set through the packages mentioned in 15.3, e.g.
// echo can be turned on and off, but others like setting the kill character
// didn't appear to be supported (presumably because it doesn't make
// sense for a cross-platform toolkit).
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.9
//----------------------------------------------------------------------------------
// Consider using Java's PushbackInputStream or PushbackReader
// Different functionality to original cookbook but can be used
// as an alternative for some scenarios.
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.10
//----------------------------------------------------------------------------------
// If using Java 6, use Console.readPassword()
// Otherwise use jline (use 0 instead of mask character '*' for no echo):
password = new jline.console.ConsoleReader().readLine(new Character('*'))
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.11
//----------------------------------------------------------------------------------
// In Groovy (like Java) normal input is buffered so you can normally make
// edits before hitting 'Enter'. For more control over editing (including completion
// and history etc.) use one of the packages mentioned in 15.3, e.g. jline.
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.12
//----------------------------------------------------------------------------------
// Use javacurses or jline (see 15.3) for low level screen management.
// Java/Groovy would normally use a GUI for such functionality.

// Here is a slight variation to cookbook example. This repeatedly calls
// the command feedin on the command line, e.g. "cmd /c dir" on windows
// or 'ps -aux' on Linux. Whenever a line changes, the old line is "faded
// out" using font colors from white through to black. Then the new line
// is faded in using the reverse process.
//import jcurses.system.*
class CharColor{}
color = new CharColor(CharColor.BLACK, CharColor.WHITE)
Toolkit.clearScreen(color)
maxcol = Toolkit.screenWidth
maxrow = Toolkit.screenHeight
colors = [CharColor.WHITE, CharColor.CYAN, CharColor.YELLOW, CharColor.GREEN,
          CharColor.RED, CharColor.BLUE, CharColor.MAGENTA, CharColor.BLACK]
done = false
refresh = false
waittime = 8000
oldlines = []
def fade(line, row, colorList) {
    for (i in 0..<colorList.size()) {
        Toolkit.printString(line, 0, row, new CharColor(CharColor.BLACK, colorList[i]))
        sleep 10
    }
}
while(!done) {
    if (waittime > 9999 || refresh) {
        proc = args[0].execute()
        lines = proc.text.split('\n')
        for (r in 0..<maxrow) {
            if (r >= lines.size() || r > oldlines.size() || lines[r] != oldlines[r]) {
                if (oldlines != [])
                    fade(r < oldlines.size() ? oldlines[r] : ' ' * maxcol, r, colors)
                fade(r < lines.size() ? lines[r] : ' ' * maxcol, r, colors.reverse())
            }
        }
        oldlines = lines
        refresh = false
        waittime = 0
    }
    waittime += 200
    sleep 200
}

// Keyboard handling would be similar to 15.6.
// Something like below but need to synchronize as we are in different threads.
Thread.start{
    while(!done) {
        ch = Toolkit.readCharacter()
        if (ch.isSpecialCode() || ch.character == 'q') done = true
        else refresh = true
    }
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.13
//----------------------------------------------------------------------------------
// These examples uses expectj, a pure Java Expect-like module.
// http://expectj.sourceforge.net/
defaultTimeout = -1 // infinite
expect = new expectj.ExpectJ("logfile.log", defaultTimeout)
command = expect.spawn("program to run")
command.expect('Password', 10)
// expectj doesn't support regular expressions, but see readUntil
// in recipe 18.6 for how to manually code this
command.expect('invalid')
command.send('Hello, world\r')
// kill spawned process
command.stop()

// expecting multiple choices
// expectj doesn't support multiple choices, but see readUntil
// in recipe 18.6 for how to manually code this
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.14
//----------------------------------------------------------------------------------
// Methods not shown for the edit menu items, they would be the same as for the
// file menu items.
//import groovy.swing.SwingBuilder
class SwingBuilder{}
def print() {}
def save() {}
frame = new SwingBuilder().frame(title:'Demo') {
    menuBar {
        menu(mnemonic:'F', 'File') {
            menuItem (actionPerformed:this.&print, 'Print')
            separator()
            menuItem (actionPerformed:this.&save, 'Save')
            menuItem (actionPerformed:{System.exit(0)}, 'Quit immediately')
        }
        menu(mnemonic:'O', 'Options') {
            checkBoxMenuItem ('Create Debugging Info', state:true)
        }
        menu(mnemonic:'D', 'Debug') {
            group = buttonGroup()
            radioButtonMenuItem ('Log Level 1', buttonGroup:group, selected:true)
            radioButtonMenuItem ('Log Level 2', buttonGroup:group)
            radioButtonMenuItem ('Log Level 3', buttonGroup:group)
        }
        menu(mnemonic:'F', 'Format') {
            menu('Font') {
                group = buttonGroup()
                radioButtonMenuItem ('Times Roman', buttonGroup:group, selected:true)
                radioButtonMenuItem ('Courier', buttonGroup:group)
            }
        }
        menu(mnemonic:'E', 'Edit') {
            menuItem (actionPerformed:{}, 'Copy')
            menuItem (actionPerformed:{}, 'Cut')
            menuItem (actionPerformed:{}, 'Paste')
            menuItem (actionPerformed:{}, 'Delete')
            separator()
            menu('Object ...') {
                menuItem (actionPerformed:{}, 'Circle')
                menuItem (actionPerformed:{}, 'Square')
                menuItem (actionPerformed:{}, 'Point')
            }
        }
    }
}
frame.pack()
frame.show()
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.15
//----------------------------------------------------------------------------------
// Registration Example
import groovy.swing.SwingBuilder
def cancel(event) {
    println 'Sorry you decided not to register.'
    dialog.dispose()
}
def register(event) {
    if (swing.name?.text) {
        println "Welcome to the fold $swing.name.text"
        dialog.dispose()
    } else println "You didn't give me your name!"
}
def dialog(event) {
    dialog = swing.createDialog(title:'Entry')
    def panel = swing.panel {
        vbox {
            hbox {
                label(text:'Name')
                textField(columns:20, id:'name')
            }
            hbox {
                button('Register', actionPerformed:this.&register)
                button('Cancel', actionPerformed:this.&cancel)
            }
        }
    }
    dialog.getContentPane().add(panel)
    dialog.pack()
    dialog.show()
}
swing = new SwingBuilder()
frame = swing.frame(title:'Registration Example') {
    panel {
        button(actionPerformed:this.&dialog, 'Click Here For Registration Form')
        glue()
        button(actionPerformed:{System.exit(0)}, 'Quit')
    }
}
frame.pack()
frame.show()


// Error Example, slight variation to original cookbook
import groovy.swing.SwingBuilder
import javax.swing.WindowConstants as WC
import javax.swing.JOptionPane
def calculate(event) {
    try {
        swing.result.text = evaluate(swing.expr.text)
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(frame, ex.message)
    }
}
swing = new SwingBuilder()
frame = swing.frame(title:'Calculator Example',
    defaultCloseOperation:WC.EXIT_ON_CLOSE) {
    panel {
        vbox {
            hbox {
                label(text:'Expression')
                hstrut()
                textField(columns:12, id:'expr')
            }
            hbox {
                label(text:'Result')
                glue()
                label(id:'result')
            }
            hbox {
                button('Calculate', actionPerformed:this.&calculate)
                button('Quit', actionPerformed:{System.exit(0)})
            }
        }
    }
}
frame.pack()
frame.show()
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.16
//----------------------------------------------------------------------------------
// Resizing in Groovy follows Java rules, i.e. is dependent on the layout manager.
// You can set preferred, minimum and maximum sizes (may be ignored by some layout managers).
// You can setResizable(false) for some components.
// You can specify a weight value for some layout managers, e.g. GridBagLayout
// which control the degree of scaling which occurs during resizing.
// Some layout managers, e.g. GridLayout, automaticaly resize their contained widgets.
// You can capture resize events and do everything manually yourself.
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.17
//----------------------------------------------------------------------------------
// Removing DOS console on Windows:
// If you are using java.exe to start your Groovy script, use javaw.exe instead.
// If you are using groovy.exe to start your Groovy script, use groovyw.exe instead.
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.18
//----------------------------------------------------------------------------------
// additions to original cookbook:
// random starting position
// color changes after each bounce
//import jcurses.system.*
color = new CharColor(CharColor.BLACK, CharColor.WHITE)
Toolkit.clearScreen(color)
rand = new Random()
maxrow = Toolkit.screenWidth
maxcol = Toolkit.screenHeight
rowinc = 1
colinc = 1
row = rand.nextInt(maxrow)
col = rand.nextInt(maxcol)
chars = '*-/|\\_'
colors = [CharColor.RED, CharColor.BLUE, CharColor.YELLOW,
          CharColor.GREEN, CharColor.CYAN, CharColor.MAGENTA]
delay = 20
ch = null
def nextChar(){
    ch = chars[0]
    chars = chars[1..-1] + chars[0]
    color = new CharColor(CharColor.BLACK, colors[0])
    colors = colors[1..-1] + colors[0]
}
nextChar()
while(true) {
    Toolkit.printString(ch, row, col, color)
    sleep delay
    row = row + rowinc
    col = col + colinc
    if (row in [0, maxrow]) { nextChar(); rowinc = -rowinc }
    if (col in [0, maxcol]) { nextChar(); colinc = -colinc }
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_15.19
//----------------------------------------------------------------------------------
// Variation to cookbook. Let's you reshuffle lines in a multi-line string
// by drag-n-drop.
import java.awt.*
import java.awt.datatransfer.*
import java.awt.dnd.*
import javax.swing.*
import javax.swing.ScrollPaneConstants as SPC

class DragDropList extends JList implements
        DragSourceListener, DropTargetListener, DragGestureListener {
    def dragSource
    DropTarget dropTarget
    def dropTargetCell
    int draggedIndex = -1
    def localDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType)
    def supportedFlavors = [localDataFlavor] as DataFlavor[]

    DragDropList(model) {
        super()
        setModel(model)
        setCellRenderer(new DragDropCellRenderer(this))
        dragSource = new DragSource()
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this)
        dropTarget = new DropTarget(this, this)
    }

    void dragGestureRecognized(DragGestureEvent dge) {
        int index = locationToIndex(dge.dragOrigin)
        if (index == -1 || index == model.size() - 1) return
        def trans = new CustomTransferable(model.getElementAt(index), this)
        draggedIndex = index
        dragSource.startDrag(dge, Cursor.defaultCursor, trans, this)
    }

    void dragDropEnd(DragSourceDropEvent dsde) {
        dropTargetCell = null
        draggedIndex = -1
        repaint()
    }

    void dragEnter(DragSourceDragEvent dsde) { }

    void dragExit(DragSourceEvent dse) { }

    void dragOver(DragSourceDragEvent dsde) { }

    void dropActionChanged(DragSourceDragEvent dsde) { }

    void dropActionChanged(DropTargetDragEvent dtde) { }

    void dragExit(DropTargetEvent dte) { }

    void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.source != dropTarget) dtde.rejectDrag()
        else dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE)
    }

    void dragOver(DropTargetDragEvent dtde) {
        if (dtde.source != dropTarget) dtde.rejectDrag()
        int index = locationToIndex(dtde.location)
        if (index == -1 || index == draggedIndex + 1) dropTargetCell = null
        else dropTargetCell = model.getElementAt(index)
        repaint()
    }

    void drop(DropTargetDropEvent dtde) {
        if (dtde.source != dropTarget) {
            dtde.rejectDrop()
            return
        }
        int index = locationToIndex(dtde.location)
        if (index == -1 || index == draggedIndex) {
            dtde.rejectDrop()
            return
        }
        dtde.acceptDrop(DnDConstants.ACTION_MOVE)
        def dragged = dtde.transferable.getTransferData(localDataFlavor)
        boolean sourceBeforeTarget = (draggedIndex < index)
        model.remove(draggedIndex)
        model.add((sourceBeforeTarget ? index - 1 : index), dragged)
        dtde.dropComplete(true)
    }
}

class CustomTransferable implements Transferable {
    def object
    def ddlist

    CustomTransferable(object, ddlist) {
        this.object = object
        this.ddlist = ddlist
    }

    Object getTransferData(DataFlavor df) {
        if (isDataFlavorSupported(df)) return object
    }

    boolean isDataFlavorSupported(DataFlavor df) {
        return df.equals(ddlist.localDataFlavor)
    }

    DataFlavor[] getTransferDataFlavors() {
        return ddlist.supportedFlavors
    }
}

class DragDropCellRenderer extends DefaultListCellRenderer {
    boolean isTargetCell
    def ddlist

    DragDropCellRenderer(ddlist) {
        super()
        this.ddlist = ddlist
    }

    Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) {
        isTargetCell = (value == ddlist.dropTargetCell)
        boolean showSelected = isSelected && !isTargetCell
        return super.getListCellRendererComponent(list, value, index, showSelected, hasFocus)
    }

    void paintComponent(Graphics g) {
        super.paintComponent(g)
        if (isTargetCell) {
            g.setColor(Color.black)
            g.drawLine(0, 0, size.width.intValue(), 0)
        }
    }
}

lines = '''
This is line 1
This is line 2
This is line 3
This is line 4
'''.trim().split('\n')
def listModel = new DefaultListModel()
lines.each{ listModel.addElement(it) }
listModel.addElement(' ') // dummy
def list = new DragDropList(listModel)
def sp = new JScrollPane(list, SPC.VERTICAL_SCROLLBAR_ALWAYS, SPC.HORIZONTAL_SCROLLBAR_NEVER)
def frame = new JFrame('Line Shuffle Example')
frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
frame.contentPane.add(sp)
frame.pack()
frame.setVisible(true)
//----------------------------------------------------------------------------------
