package org.rascalmpl.interpreter.utils;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.ast.Expression;
import org.rascalmpl.ast.NullASTVisitor;
import org.rascalmpl.ast.StringConstant;
import org.rascalmpl.ast.Expression.Anti;
import org.rascalmpl.ast.Expression.CallOrTree;
import org.rascalmpl.ast.Expression.List;
import org.rascalmpl.ast.Expression.MultiVariable;
import org.rascalmpl.ast.Expression.QualifiedName;
import org.rascalmpl.ast.Expression.TypedVariable;
import org.rascalmpl.ast.Expression.TypedVariableBecomes;
import org.rascalmpl.ast.QualifiedName.Default;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.values.uptr.Factory;

public class IUPTRAstToSymbolConstructor extends NullASTVisitor<IConstructor> {
	
	static public class NonGroundSymbolException extends RuntimeException {
		private static final long serialVersionUID = 2430739406856140650L;
	}

	private IValueFactory vf;

	public IUPTRAstToSymbolConstructor(IValueFactory vf) {
		this.vf = vf;
	}
	
	@Override
	public IConstructor visitQualifiedNameDefault(Default x) {
		throw new NonGroundSymbolException();
	}
	
	@Override
	public IConstructor visitExpressionMultiVariable(MultiVariable x) {
		throw new NonGroundSymbolException();
	}
	
	@Override
	public IConstructor visitExpressionQualifiedName(QualifiedName x) {
		throw new NonGroundSymbolException();
	}
	
	@Override
	public IConstructor visitExpressionTypedVariable(TypedVariable x) {
		throw new NonGroundSymbolException();
	}
	
	@Override
	public IConstructor visitExpressionTypedVariableBecomes(
			TypedVariableBecomes x) {
		throw new NonGroundSymbolException();
	}

	@Override
	public IConstructor visitExpressionAnti(Anti x) {
		throw new NonGroundSymbolException();
	}
	
	@Override
	public IConstructor visitExpressionCallOrTree(CallOrTree x) {
		Expression namePart = x.getExpression();
		if (!namePart.isQualifiedName()) {
			throw new ImplementationError("weird AST");
		}
		String name = Names.name(Names.lastName(namePart.getQualifiedName()));
		
		if (name.equals("lit")) {
			StringConstant.Lexical arg = 
				(org.rascalmpl.ast.StringConstant.Lexical) x.getArguments().get(0).getLiteral().getStringLiteral().getConstant();
			// TODO: escaping etc.
			String str = arg.getString();
			str = str.substring(1, str.length() - 1);
			return vf.constructor(Factory.Symbol_Lit, vf.string(str));
		}
		
		if (name.equals("cilit")) {
			StringConstant.Lexical arg = (org.rascalmpl.ast.StringConstant.Lexical) x.getArguments().get(0).getLiteral().getStringLiteral().getConstant();
			// TODO: escaping etc.
			String str = arg.getString();
			str = str.substring(1, str.length() - 1);
			return vf.constructor(Factory.Symbol_CiLit, vf.string(str));
		}
		
		if (name.equals("cf")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			return vf.constructor(Factory.Symbol_Cf, arg);
		}
		
		if (name.equals("lex")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			return vf.constructor(Factory.Symbol_Lex, arg);
		}
		
		if (name.equals("empty")) {
			return vf.constructor(Factory.Symbol_Empty);	
		}
		
		if (name.equals("seq")) {
			IList list = vf.list(Factory.Symbol);
			Expression.List arg = (List) x.getArguments().get(0);
			for (Expression y: arg.getElements()) {
				list = list.append(y.accept(this));
			}
			return vf.constructor(Factory.Symbol_Seq, list);
			
		}
		
		if (name.equals("opt")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			return vf.constructor(Factory.Symbol_Opt, arg);
		}
		
		if (name.equals("alt")) {
			IConstructor arg1 = x.getArguments().get(0).accept(this);
			IConstructor arg2 = x.getArguments().get(1).accept(this);
			return vf.constructor(Factory.Symbol_Alt, arg1, arg2);
		}
		
		if (name.equals("tuple")) {
			java.util.List<Expression> args = x.getArguments();
			IConstructor head = args.get(0).accept(this); 
			IList rest = vf.list(Factory.Symbol);
			for (Expression arg: ((Expression.List)args.get(1)).getElements()) {
				rest = rest.append(arg.accept(this));
			}
			return vf.constructor(Factory.Symbol_Tuple, head, rest);
		}
		
		if (name.equals("sort")) {
			StringConstant.Lexical arg = (org.rascalmpl.ast.StringConstant.Lexical) 
				x.getArguments().get(0).getLiteral().getStringLiteral().getConstant();
			String str = arg.getString();
			str = str.substring(1, str.length() - 1);
			return vf.constructor(Factory.Symbol_Sort, vf.string(str));
		}
		

		if (name.equals("iter")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			return vf.constructor(Factory.Symbol_IterPlus, arg);
		}
		
		if (name.equals("iter-star")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			return vf.constructor(Factory.Symbol_IterStar, arg);
		}
		
		if (name.equals("iter-sep")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			IConstructor sep = x.getArguments().get(1).accept(this);
			return vf.constructor(Factory.Symbol_IterPlusSep, arg, sep);
		}
		
		if (name.equals("iter-star-sep")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			IConstructor sep = x.getArguments().get(1).accept(this);
			return vf.constructor(Factory.Symbol_IterStarSep, arg, sep);
		}
		
		if (name.equals("iter-n")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			org.rascalmpl.ast.DecimalIntegerLiteral.Lexical arg1 = (org.rascalmpl.ast.DecimalIntegerLiteral.Lexical) x.getArguments().get(1).getLiteral().getIntegerLiteral().getDecimal();
			IInteger num = vf.integer(java.lang.Integer.parseInt(arg1.getString())); 
			return vf.constructor(Factory.Symbol_IterN, arg, num);
		}
		
		if (name.equals("iter-n-sep")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			org.rascalmpl.ast.DecimalIntegerLiteral.Lexical arg1 = (org.rascalmpl.ast.DecimalIntegerLiteral.Lexical) x.getArguments().get(1).getLiteral().getIntegerLiteral().getDecimal();
			IInteger num = vf.integer(java.lang.Integer.parseInt(arg1.getString())); 
			return vf.constructor(Factory.Symbol_IterN, arg, num);
		}
		
		if (name.equals("func")) {
			java.util.List<Expression> args = x.getArguments();
			IList formals = vf.list(Factory.Symbol);
			for (Expression arg: ((Expression.List)args.get(0)).getElements()) {
				formals = formals.append(arg.accept(this));
			}
			IConstructor result = args.get(1).accept(this); 
			return vf.constructor(Factory.Symbol_Func, result, formals);
			
		}
		
		if (name.equals("parameterized-sort")) {
			java.util.List<Expression> args = x.getArguments();
			StringConstant.Lexical sort = (org.rascalmpl.ast.StringConstant.Lexical) 
				x.getArguments().get(0).getLiteral().getStringLiteral().getConstant();
			IList rest = vf.list(Factory.Symbol);
			for (Expression arg: ((Expression.List)args.get(1)).getElements()) {
				rest = rest.append(arg.accept(this));
			}
			return vf.constructor(Factory.Symbol_ParameterizedSort, vf.string(sort.getString()), rest);
			
		}
		
		if (name.equals("strategy")) {
			IConstructor arg = x.getArguments().get(0).accept(this);
			IConstructor sep = x.getArguments().get(1).accept(this);
			return vf.constructor(Factory.Symbol_Strategy, arg, sep);
		}
		
		if (name.equals("lit")) {
			StringConstant.Lexical arg = (org.rascalmpl.ast.StringConstant.Lexical) 
				x.getArguments().get(0).getLiteral().getStringLiteral().getConstant();
			// TODO: escaping etc.
			return vf.constructor(Factory.Symbol_Lit, vf.string(arg.getString()));
		}
		
		if (name.equals("layout")) {
			return vf.constructor(Factory.Symbol_Layout);
		}
		
		if (name.equals("char-class")) {
			java.util.List<Expression> args = x.getArguments();
			IList ranges = vf.list(Factory.CharRange);
			for (Expression arg: ((Expression.List)args.get(0)).getElements()) {
				ranges = ranges.append(arg.accept(this));
			}
			return vf.constructor(Factory.Symbol_CharClass, ranges);
		}
		
		if (name.equals("single")) {
			org.rascalmpl.ast.DecimalIntegerLiteral.Lexical arg1 = (org.rascalmpl.ast.DecimalIntegerLiteral.Lexical) x.getArguments().get(0).getLiteral().getIntegerLiteral().getDecimal();
			IInteger num = vf.integer(java.lang.Integer.parseInt(arg1.getString())); 
			return vf.constructor(Factory.CharRange_Single, num);
		}
		
		if (name.equals("range")) {
			org.rascalmpl.ast.DecimalIntegerLiteral.Lexical arg1 = (org.rascalmpl.ast.DecimalIntegerLiteral.Lexical) x.getArguments().get(0).getLiteral().getIntegerLiteral().getDecimal();
			IInteger num1 = vf.integer(java.lang.Integer.parseInt(arg1.getString())); 
			org.rascalmpl.ast.DecimalIntegerLiteral.Lexical arg2 = (org.rascalmpl.ast.DecimalIntegerLiteral.Lexical) x.getArguments().get(1).getLiteral().getIntegerLiteral().getDecimal();
			IInteger num2 = vf.integer(java.lang.Integer.parseInt(arg2.getString())); 
			return vf.constructor(Factory.CharRange_Range, num1, num2);
		}

		throw new ImplementationError("Non-IUPTR AST expression: " + x);
	}
}
