package org.rascalmpl.library.experiments.Compiler.RVM.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Call;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.CallConstr;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.CallDyn;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.CallPrim;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Create;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.CreateDyn;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Halt;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.HasNext;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Init;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Instruction;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Jmp;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.JmpFalse;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.JmpTrue;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Label;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadCon;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadConstr;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadFun;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadLoc;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadLocAsRef;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadLocRef;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadNestedFun;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadVar;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadVarAsRef;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.LoadVarRef;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Next0;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Next1;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Opcode;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Pop;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Println;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Return0;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Return1;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.StoreLoc;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.StoreLocRef;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.StoreVar;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.StoreVarRef;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Yield0;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions.Yield1;

public class CodeBlock {

	private final IValueFactory vf;
	int pc;
	
	private final ArrayList<Instruction> insList;
	
	private final HashMap<String,Integer> labels;
	private final ArrayList<String> labelList;
	
	private final Map<IValue, Integer> constantMap;
	private final ArrayList<IValue> constantStore;
	private IValue[] finalConstantStore;
	
	private Map<String, Integer> functionMap;
	private Map<String, Integer> constructorMap;
	
	public int[] finalCode;
	
	public CodeBlock(IValueFactory factory){
		labels = new HashMap<String,Integer>();
		labelList = new ArrayList<String>();
		insList = new ArrayList<Instruction>();
		new ArrayList<Integer>();
		pc = 0;
		this.vf = factory;
		constantMap = new HashMap<IValue, Integer>();
		this.constantStore = new ArrayList<IValue>();
	}
	
	public void defLabel(String label){
		int idx = labelList.indexOf(label);
		if(idx < 0){
			labelList.add(label);
		}
		labels.put(label, pc);
	}
	
	protected int useLabel(String label){
		int idx = labelList.indexOf(label);
		if(idx < 0){
			idx = labelList.size();
			labelList.add(label);
		}
		return idx;
	}
	
	public int getLabelIndex(String label){
		Integer n = labels.get(label);
		if(n == null){
			throw new RuntimeException("PANIC: undefined label " + label);
		}
		return n;
	}
	
	public IValue getConstantValue(int n){
		for(IValue constant : constantMap.keySet()){
			if(constantMap.get(constant) == n){
				return constant;
			}
		}
		throw new RuntimeException("PANIC: undefined constant index " + n);
	}
	
	private int getConstantIndex(IValue v){
		Integer n = constantMap.get(v);
		if(n == null){
			n = constantStore.size();
			constantStore.add(v);
			constantMap.put(v,  n);
		}
		return n;
	}
	
	public String getFunctionName(int n){
		for(String fname : functionMap.keySet()){
			if(functionMap.get(fname) == n){
				return fname;
			}
		}
		throw new RuntimeException("PANIC: undefined function index " + n);
	}
	
	public int getFunctionIndex(String name){
		Integer n = functionMap.get(name);
		if(n == null){
			throw new RuntimeException("PANIC: undefined function name " + name);
		}
		return n;
	}
	
	public String getConstructorName(int n) {
		for(String cname : constructorMap.keySet()) {
			if(constructorMap.get(cname) == n)
				return cname;
		}
		throw new RuntimeException("PANIC: undefined constructor index " + n);
	}
	
	public int getConstructorIndex(String name) {
		Integer n = constructorMap.get(name);
		if(n == null)
			throw new RuntimeException("PANIC: undefined constructor name " + name);
		return n;
	}
	
	CodeBlock add(Instruction ins){
		insList.add(ins);
		pc += ins.pcIncrement();
		return this;
	}
	
	public void addCode(int c){
		finalCode[pc++] = c;
	}
	
	public CodeBlock POP(){
		return add(new Pop(this));
	}
	
	public  CodeBlock HALT(){
		return add(new Halt(this));
	}
	
	public CodeBlock RETURN0() {
		return add(new Return0(this));
	}
	
	public CodeBlock RETURN1(){
		return add(new Return1(this));
	}
	
	public CodeBlock LABEL(String arg){
		return add(new Label(this, arg));
	}
	
	public CodeBlock LOADCON(boolean arg){
		return add(new LoadCon(this, getConstantIndex(vf.bool(arg))));
	}
	
	public CodeBlock LOADCON(int arg){
		return add(new LoadCon(this, getConstantIndex(vf.integer(arg))));
	}
	
	public CodeBlock LOADCON(String arg){
		return add(new LoadCon(this, getConstantIndex(vf.string(arg))));
	}
	
	public CodeBlock LOADCON(IValue val){
		return add(new LoadCon(this, getConstantIndex(val)));
	}
	
	public CodeBlock CALL(String arg){
		return add(new Call(this, arg));
	}
	
	public CodeBlock JMP(String arg){
		return add(new Jmp(this, arg));
	}
	
	public CodeBlock JMPTRUE(String arg){
		return add(new JmpTrue(this, arg));
	}
	
	public CodeBlock JMPFALSE(String arg){
		return add(new JmpFalse(this, arg));
	}
	
	public CodeBlock LOADLOC (int pos){
		return add(new LoadLoc(this, pos));
	}
	
	public CodeBlock STORELOC (int pos){
		return add(new StoreLoc(this, pos));
	}
	
	public CodeBlock LOADVAR (int scope, int pos){
		return add(new LoadVar(this, scope, pos));
	}
	
	public CodeBlock STOREVAR (int scope, int pos){
		return add(new StoreVar(this, scope, pos));
	}
	
	public CodeBlock CALLPRIM (Primitive prim){
		return add(new CallPrim(this, prim));
	}
	
	public CodeBlock LOADFUN (String name){
		return add(new LoadFun(this, name));
	}
	
	public CodeBlock CALLDYN(){
		return add(new CallDyn(this));
	}
	
	public CodeBlock INIT() {
		return add(new Init(this));
	}
	
	public CodeBlock CREATE(String name) {
		return add(new Create(this, name));
	}
	
	public CodeBlock NEXT0() {
		return add(new Next0(this));
	}
	
	public CodeBlock NEXT1() {
		return add(new Next1(this));
	}
	
	public CodeBlock YIELD0() {
		return add(new Yield0(this));
	}
	
	public CodeBlock YIELD1() {
		return add(new Yield1(this));
	}
	
	public CodeBlock CREATEDYN() {
		return add(new CreateDyn(this));
	}
	
	public CodeBlock HASNEXT() {
		return add(new HasNext(this));
	}
	
	public CodeBlock PRINTLN(){
		return add(new Println(this));
	}
    
	public CodeBlock LOADLOCASREF(int pos) {
		return add(new LoadLocAsRef(this, pos));
	}
	
	public CodeBlock LOADVARASREF(int scope, int pos) {
		return add(new LoadVarAsRef(this, scope, pos));
	}
	
	public CodeBlock LOADLOCREF(int pos) {
		return add(new LoadLocRef(this, pos));
	}
	
	public CodeBlock LOADVARREF(int scope, int pos) {
		return add(new LoadVarRef(this, scope, pos));
	}
	
	public CodeBlock STORELOCREF(int pos) {
		return add(new StoreLocRef(this, pos));
	}
	
	public CodeBlock STOREVARREF(int scope, int pos) {
		return add(new StoreVarRef(this, scope, pos));
	}
	
	public CodeBlock LOADCONSTR(String name) {
		return add(new LoadConstr(this, name));
	}
	
	public CodeBlock CALLCONSTR(String name) {
		return add(new CallConstr(this, name));
	}
	
	public CodeBlock LOADNESTEDFUN(String name, int scope) {
		return add(new LoadNestedFun(this, name, scope));
	}
	
	public CodeBlock done(String fname, Map<String, Integer> codeMap, Map<String, Integer> constructorMap, boolean listing){
		this.functionMap = codeMap;
		this.constructorMap = constructorMap;
		int codeSize = pc;
		pc = 0;
		finalCode = new int[codeSize];
		for(Instruction ins : insList){
			ins.generate();
		}
		finalConstantStore = new IValue[constantStore.size()];
		for(int i = 0; i < constantStore.size(); i++ ){
			finalConstantStore[i] = constantStore.get(i);
		}
		if(listing){
			listing(fname);
		}
    	return this;
    }
    
    public int[] getInstructions(){
    	return finalCode;
    }
    
    public IValue[] getConstants(){
    	return finalConstantStore;
    }
    
    void listing(String fname){
    	int pc = 0;
    	while(pc < finalCode.length){
    		Opcode opc = Opcode.fromInteger(finalCode[pc]);
    		System.out.println(fname + "[" + pc +"]: " + Opcode.toString(this, opc, pc));
    		pc += opc.getIncrement();
    	}
    	System.out.println();
    }
    
    public String toString(int n){
    	Opcode opc = Opcode.fromInteger(finalCode[n]);
    	return Opcode.toString(this, opc, n);
    }
}