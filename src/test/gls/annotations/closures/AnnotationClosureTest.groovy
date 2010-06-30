package gls.annotations.closures

import gls.CompilableTestSupport
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention

class AnnotationClosureTest extends CompilableTestSupport {
	def answer = new Object() {
		def answer() { 42 }
	}

	void testAllowedAsValueForAnnotationElementOfTypeClass() {
		shouldCompile """
import gls.annotations.closures.AnnWithClassElement

@AnnWithClassElement(elem = { 1 + 2 })
class Foo {}
		"""
	}

	// TODO: two compile errors instead of one, odd error message
	void testNotAllowedAsValueForAnnotationElementOfOtherType() {
		shouldNotCompile """
import gls.annotations.closures.AnnWithStringElement

@AnnWithStringElement(elem = { 1 + 2 })
class Foo {}
		"""
	}

	void testWorksForAnnotationTypeDeclaredInGroovy() {
		def closureClass = ClassWithAnnClosure.class.getAnnotation(AnnWithClassElement).elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call() == 3
	}

	void testWorksForAnnotationTypeDeclaredInJava() {
 		def closureClass = ClassWithJavaAnnClosure.class.getAnnotation(JavaAnnotationWithClassElement).elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call() == 3
	}

	void testCanBeUsedAsDefaultValue() {
		def closureClass = ClosureAsDefaultValue.class.getAnnotation(AnnWithDefaultValue).elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call() == 3
	}

	void testCanBeNested() {
		def closureClass = NestedClosure.class.getAnnotation(AnnWithClassElement).elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call(9) == 9
	}

	void testWorksOnInnerClass() {
		def closureClass = ClassWithAnnClosure.InnerClassWithAnnClosure.class.getAnnotation(AnnWithClassElement).elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call() == 3
	}

	void testWorksOnNestedClass() {
		def closureClass = ClassWithAnnClosure.NestedClassWithAnnClosure.class.getAnnotation(AnnWithClassElement).elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call() == 3
	}

	void testWorksOnNestedAnnotation() {
 		def closureClass = NestedAnnotation.class.getAnnotation(AnnWithNestedAnn).elem().elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call() == 3
	}

	void testWorksOnNestedAnnotationWithDefaultValue() {
		def closureClass = NestedAnnotationWithDefault.class.getAnnotation(AnnWithNestedAnnWithDefault).elem().elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call() == 3
	}

	void testMayContainGString() {
		def closureClass = ClosureWithGString.class.getAnnotation(AnnWithClassElement).elem()
		def closure = closureClass.newInstance(null, null)

		assert closure.call([1,2,3]) == "list has 3 elements"		
	}

	void testDoesNoHarmOnAnnotationWithSourceRetention() {
		shouldCompile """
import java.lang.annotation.*

@Retention(RetentionPolicy.SOURCE)
@interface AnnWithSourceRetention {
	Class elem()
}

@AnnWithSourceRetention(elem = { 1 + 2 })
class Foo {}
		"""
	}

	void testDoesNoHarmOnAnnotationWithClassRetention() {
		shouldCompile """
import java.lang.annotation.*

@Retention(RetentionPolicy.CLASS)
@interface AnnWithClassRetention {
	Class elem()
}

@AnnWithClassRetention(elem = { 1 + 2 })
class Foo {}
		"""
	}
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithClassElement {
	Class elem()
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithStringElement {
	String elem()
}

@AnnWithClassElement(elem = { 1 + 2 })
class ClassWithAnnClosure {
	@AnnWithClassElement(elem = { 1 + 2 })
	class InnerClassWithAnnClosure {}

	@AnnWithClassElement(elem = { 1 + 2 })
	static class NestedClassWithAnnClosure {}
}

@JavaAnnotationWithClassElement(elem = { 1 + 2 })
class ClassWithJavaAnnClosure {}

@AnnWithClassElement(elem = { def nested = { it }; nested(it) })
class NestedClosure {}

@AnnWithDefaultValue
class ClosureAsDefaultValue {}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithDefaultValue {
	Class elem() default { 1 + 2 }
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithNestedAnn {
	AnnWithClassElement elem()	
}

@AnnWithNestedAnn(elem = @AnnWithClassElement(elem = { 1 + 2 }))
class NestedAnnotation {}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithNestedAnnWithDefault {
	AnnWithDefaultValue elem()
}

@AnnWithNestedAnnWithDefault(elem = @AnnWithDefaultValue())
class NestedAnnotationWithDefault {}

@AnnWithClassElement(elem = { list -> "list has ${list.size()} elements" })
class ClosureWithGString {}
