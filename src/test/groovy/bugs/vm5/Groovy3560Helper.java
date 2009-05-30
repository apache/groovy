package groovy.bugs.vm5;

public class Groovy3560Helper {
	public static int m1(IGroovy3560... ifcs) {
		return ifcs.length;
	}
	public static int m2(String x, String y, IGroovy3560... ifcs) {
		return ifcs.length;
	}
}

interface IGroovy3560 {}

class Groovy3560A implements IGroovy3560{}

class Groovy3560B implements IGroovy3560{}