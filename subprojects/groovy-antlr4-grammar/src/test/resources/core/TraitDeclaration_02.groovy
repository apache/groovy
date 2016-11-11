trait AA {
    {
        println 123
    }
}

trait BB {
    static {
        println '123'
    }
}

trait CC {
    static
    {
        println '123'
    }
}

trait DD {
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

trait EE {{}}
trait FF {static {}}
trait GG {static {};{}}