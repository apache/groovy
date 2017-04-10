trait SwimmingAbility {
    def swim() {
        prt("swimming..")
    }

    abstract String prt(String msg);
}

trait FlyingAbility {
    def fly() {
        println "flying.."
    }
}

class Duck implements SwimmingAbility, FlyingAbility {
    String prt(String msg) {
        println msg + " happily";
    }
}

def duck = new Duck()
duck.swim()
duck.fly()
