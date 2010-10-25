package org.rascalmpl.interpreter.matching;

import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.env.Environment;

/**
 * The equivalence boolean operator backtracks for both the lhs and the rhs. This means
 * that if the lhs or rhs have multiple ways of assigning a value to a variable,
 * this and operator will be evaluated as many times.
 */
public class EquivalenceResult extends AbstractBooleanResult {
	private final IBooleanResult left;
	private final IBooleanResult right;
	private boolean firstMatch = true;
	private boolean leftResult;

	public EquivalenceResult(IEvaluatorContext ctx, IBooleanResult left, IBooleanResult right) {
		super(ctx);
		this.left = left;
		this.right = right;
	}

	public void init() {
		left.init();
		right.init();
		firstMatch = true;
	}

	public boolean hasNext() {
		if (firstMatch) {
			return left.hasNext() && right.hasNext();
		}
		
		return right.hasNext() || left.hasNext();
	}
	
	@Override
	public boolean next() {
		if (firstMatch) {
			firstMatch = false;
			Environment old = ctx.getCurrentEnvt();
			boolean rightResult;
			try {
				ctx.pushEnv();
				leftResult = left.next();
			}
			finally {
				ctx.unwind(old);
			}
			try {
				ctx.pushEnv();
				rightResult = right.next();
			}
			finally {
				ctx.unwind(old);
			}
			
			return leftResult == rightResult;
		}
		
		if (right.hasNext()) {
			Environment old = ctx.getCurrentEnvt();
			try {
				ctx.pushEnv();
				return leftResult == right.next();
			}
			finally {
				ctx.unwind(old);
			}
		}
		
		Environment old = ctx.getCurrentEnvt();
		try {
			ctx.pushEnv();
			leftResult = left.next();
		}
		finally {
			ctx.unwind(old);
		}
		
		try {
			right.init();
			return leftResult == right.next();
		}
		finally {
			ctx.unwind(old);
		}
	}
}
