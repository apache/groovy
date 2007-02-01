package groovy

import java.util.HashMap as Goober;

class Cheddar extends Goober implements Runnable
{
    Goober theMap;
    protected def cheesier;
    public static def cheesiest;

    static void main(args) {
        def f = new Cheddar()
        println f
    }

    def cheeseIt() {  }

    String getStringCheese() { }
    String getOtherCheese(foo,bar) { }

    void run() { cheeseIt() }

    static Goober mutateGoober(Goober theGoober) { }
   
}

class Provolone
{

}
