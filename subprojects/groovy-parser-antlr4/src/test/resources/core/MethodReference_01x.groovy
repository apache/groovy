import java.util.stream.Collectors

// class::staticMethod
assert ['1', '2', '3'] == [1, 2, 3].stream().map(Integer::toString).collect(Collectors.toList())

// class::instanceMethod
assert ['A', 'B', 'C'] == ['a', 'b', 'c'].stream().map(String::toUpperCase).collect(Collectors.toList())



def robot = new Robot();

// instance::instanceMethod
assert ['Hi, Jochen', 'Hi, Paul', 'Hi, Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(robot::greet).collect(Collectors.toList())

// class::staticMethod
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(Person::getText).collect(Collectors.toList())
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(BasePerson::getText).collect(Collectors.toList())

// instance::staticMethod
assert ['J', 'P', 'D'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(robot::firstCharOfName).collect(Collectors.toList())

// class::instanceMethod
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(Person::getName).collect(Collectors.toList())


// ----------------------------------
class BasePerson {
    public static String getText(Person p) {
        return p.name;
    }
}

class Person extends BasePerson {
    private String name;

    public Person(String name) {
        this.name = name
    }

    public String getName() {
        return this.name;
    }

}
class Robot {
    public String greet(Person p) {
        return "Hi, ${p.name}"
    }

    public static char firstCharOfName(Person p) {
        return p.getName().charAt(0);
    }
}

def mr = String::toUpperCase
assert 'ABC' == mr('abc')
assert 'ABC' == String::toUpperCase('abc')

assert new HashSet() == HashSet::new()
assert new String() == String::new()
assert 1 == Integer::new(1)
assert new String[0] == String[]::new(0)
assert new String[0] == String[]::new('0')
assert new String[1][2] == String[][]::new(1, 2)
assert new String[1][2][3] == String[][][]::new(1, 2, 3)

assert [new String[1], new String[2], new String[3]] == [1, 2, 3].stream().map(String[]::new).collect(Collectors.toList())
assert [1, 2, 3] as String[] == [1, 2, 3].stream().map(String::valueOf).toArray(String[]::new)


def a = String[][]::new(1, 2)
def b = new String[1][2]
assert a.class == b.class && a == b

a = String[][][]::new(1, 2)
b = new String[1][2][]
assert a.class == b.class && a == b

a = String[][][][]::new(1, 2)
b = new String[1][2][][]
assert a.class == b.class && a == b



