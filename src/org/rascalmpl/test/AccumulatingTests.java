package org.rascalmpl.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.rascalmpl.interpreter.staticErrors.AppendWithoutLoop;


public class AccumulatingTests extends TestFramework {

	@Test(expected=AppendWithoutLoop.class)
	public void appendWithoutFor() {
		runTest("append 3;");
	}
	
	@Test
	public void testForWithAppend() {
		assertTrue(runTest("{ for (x <- [1,2,3]) append x; } == [1,2,3];"));
	}

	@Test
	public void testForWithAppendAfterSomethingElse() {
		assertTrue(runTestInSameEvaluator("{ for (x <- [1,2,3]) { x += 1; append x; }} == [2,3,4];"));
	}

	@Test
	public void testForWithAppendAndLabel() {
		assertTrue(runTestInSameEvaluator("{ y: for (x <- [1,2,3]) { append y: x; }} == [1,2,3];"));
	}

	@Test
	public void testForWithAppendAndLabelOuter() {
		assertTrue(runTestInSameEvaluator("{ y: for (x <- [1,2,3]) { for (i <- [1,2,3]) append y: i; }} == [1,2,3,1,2,3,1,2,3];"));
	}

	@Test
	public void testForWithAppendAndLabelOuterAndInner() {
		assertTrue(runTestInSameEvaluator("{ y: for (x <- [1,2,3]) { z: for (i <- [1,2,3]) append y: i; }} == [1,2,3,1,2,3,1,2,3];"));
	}

	@Test
	public void testNestedAppend() {
		assertTrue(runTestInSameEvaluator("{ for (x <- [1,2,3]) append for (i <- [1,2,3]) append i; } == [[1,2,3],[1,2,3],[1,2,3]];"));
	}

	
	@Test
	public void testSimpleNestedFor() {
		assertTrue(runTest("{ for (x <- [1,2,3]) append for (y <- [1,2,3]) append y; } " +
				" == [[1,2,3],[1,2,3],[1,2,3]];"));
	}
	
	@Test(expected=AppendWithoutLoop.class)
	public void testAppendHasLexicalScopingFunction() {
		prepare("public void f() { append 3; }");
		runTestInSameEvaluator("{ for (x <- [1,2,3]) f(); }");
	}
	
	@Test(expected=AppendWithoutLoop.class)
	public void testAppendHasLexicalScopingClosure() {
		assertTrue(runTest("{ f = () { append 3; }; for (x <- [1,2,3]) { f(); } }" +
			" == [3,3,3];"));
	}

	@Test
	public void escapingClosureAppendToDevNull() {
		prepare("int() f() { for (x <- [1,2,3]) { int g() { append x; return 4; } return g; } }");
		assertTrue(runTestInSameEvaluator("f()() == 4;"));
	}
	
	@Test
	public void testClosuresHaveAccessToLexicalScopeForAppend() {
		assertTrue(runTest("{ for (x <- [1,2,3]) { f = () { append x; }; f(); } }" +
			" == [1,2,3];"));
	}
		
	
	@Test
	public void testWhileWithNoAppend() {
		assertTrue(runTest("{  x = 3; while (x > 0) x -= 1; } == []"));
	}
	
	@Test
	public void testWhileWithAppend() {
		assertTrue(runTest("{ x = 3; while (x > 0) { append x; x -= 1; } } == [3,2,1]"));
	}

	
	@Test
	public void testDoWhileWithNoAppend() {
		assertTrue(runTest("{  x = 3; do { x -= 1; } while (x > 0); }== []"));
	}
	
	@Test
	public void testDoWhileWithAppend() {
		assertTrue(runTest("{ x = 3; do { append x; x -= 1; } while (x > 0); } == [3,2,1]"));
	}

	
	
	
}
