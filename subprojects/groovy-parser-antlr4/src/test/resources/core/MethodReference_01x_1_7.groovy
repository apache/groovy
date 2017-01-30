
def robot = new Robot();

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


def a = String[][]::new(1, 2)
def b = new String[1][2]
assert a.class == b.class && a == b

a = String[][][]::new(1, 2)
b = new String[1][2][]
assert a.class == b.class && a == b

a = String[][][][]::new(1, 2)
b = new String[1][2][][]
assert a.class == b.class && a == b

