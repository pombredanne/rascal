package org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions;

import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.CodeBlock;

public class StoreVarRef extends Instruction {
	
	final int scope;
	final int pos;
	
	public StoreVarRef(CodeBlock ins, int scope, int pos) {
		super(ins, Opcode.STOREVARREF);
		this.scope = scope;
		this.pos = pos;
	}

	public String toString() { return "STOREVARREF " + scope + ", " + pos; }
	
	public void generate(){
		codeblock.addCode(opcode.getOpcode());
		codeblock.addCode(scope);
		codeblock.addCode(pos);
	}

}