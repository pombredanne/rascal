package org.meta_environment.rascal.ast;

import org.eclipse.imp.pdb.facts.ITree;

public abstract class FunctionType extends AbstractAST {
	static public class Ambiguity extends FunctionType {
		private final java.util.List<org.meta_environment.rascal.ast.FunctionType> alternatives;

		public Ambiguity(
				java.util.List<org.meta_environment.rascal.ast.FunctionType> alternatives) {
			this.alternatives = java.util.Collections
					.unmodifiableList(alternatives);
		}

		public java.util.List<org.meta_environment.rascal.ast.FunctionType> getAlternatives() {
			return alternatives;
		}
	}

	static public class TypeArguments extends FunctionType {
		private java.util.List<org.meta_environment.rascal.ast.TypeArg> arguments;
		private org.meta_environment.rascal.ast.Type type;

		/*
		 * type:Type "(" arguments:{TypeArg ","} ")" -> FunctionType
		 * {cons("TypeArguments")}
		 */
		private TypeArguments() {
		}

		/* package */TypeArguments(
				ITree tree,
				org.meta_environment.rascal.ast.Type type,
				java.util.List<org.meta_environment.rascal.ast.TypeArg> arguments) {
			this.tree = tree;
			this.type = type;
			this.arguments = arguments;
		}

		private void $setArguments(
				java.util.List<org.meta_environment.rascal.ast.TypeArg> x) {
			this.arguments = x;
		}

		private void $setType(org.meta_environment.rascal.ast.Type x) {
			this.type = x;
		}

		public IVisitable accept(IASTVisitor visitor) {
			return visitor.visitFunctionTypeTypeArguments(this);
		}

		@Override
		public java.util.List<org.meta_environment.rascal.ast.TypeArg> getArguments() {
			return arguments;
		}

		@Override
		public org.meta_environment.rascal.ast.Type getType() {
			return type;
		}

		public TypeArguments setArguments(
				java.util.List<org.meta_environment.rascal.ast.TypeArg> x) {
			TypeArguments z = new TypeArguments();
			z.$setArguments(x);
			return z;
		}

		public TypeArguments setType(org.meta_environment.rascal.ast.Type x) {
			TypeArguments z = new TypeArguments();
			z.$setType(x);
			return z;
		}
	}

	public java.util.List<org.meta_environment.rascal.ast.TypeArg> getArguments() {
		throw new UnsupportedOperationException();
	}

	public org.meta_environment.rascal.ast.Type getType() {
		throw new UnsupportedOperationException();
	}
}
