class AA {
    {
        println 123
    }
}

class BB {
    static {
        println '123'
    }
}

class CC {
    static
    {
        println '123'
    }
}

class DD {
    static {
        println '123'
    }

    {
        println 'abc'
    }

    static {
        println '234'
    }

    {
        println 'bcd'
    }
}

class EE {{}}
class FF {static {}}
class GG {static {};{}}

class Iterator implements java.util.Iterator {}