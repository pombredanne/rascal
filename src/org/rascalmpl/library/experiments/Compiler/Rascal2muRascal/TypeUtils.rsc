@bootstrapParser
module experiments::Compiler::Rascal2muRascal::TypeUtils

import Prelude;

import lang::rascal::\syntax::Rascal;

import lang::rascal::types::TestChecker;
import lang::rascal::types::CheckTypes;
import lang::rascal::types::AbstractName;
import experiments::Compiler::Rascal2muRascal::RascalType;

import experiments::Compiler::muRascal::AST;

public Configuration config = newConfiguration();

/*
* uid2addr maps now uids returned by the type checker to qualified function names and positions
*/
public map[int uid,tuple[str fuid,int pos] fuid2pos] uid2addr = ();
public map[loc \loc,int uid] loc2uid = ();

public set[int] modules = {};
public set[int] functions = {};
public set[int] constructors = {};
public set[int] variables = {};
public set[int] ofunctions = {};

public set[str] moduleNames = {};

@doc{Map from uid to simple names, used to recursively compute qualified names}
public map[int uid,str name] uid2name = (); 
@doc{Counters for different scopes}
public map[int uid,map[str,int] name2n] cases = (); // number of functions with the same type within a scope
public map[int uid,int n] blocks = ();              // number of blocks within a scope
public map[int uid,int n] closures = ();            // number of closures within a scope
public map[int uid,int n] bscopes = ();             // number of boolean scopes within a scope

@doc{Handling nesting}
public rel[int,int] declares = {};
public rel[int,int] containment = {};

@doc{Enables mapping of qualified names back to uids}
public map[int,str] fuid2str = ();
@doc{We need to perform more precise overloading resolution than provided by the type checker}
public map[int,Symbol] fuid2type = ();

public map[str,int] overloadingResolver = ();
public list[set[int]] overloadedFunctions = [];

public void resetScopeExtraction() {
	uid2addr = ();
	loc2uid = ();
	
	modules = {};
	functions = {};
	constructors = {};
	variables = {};
	ofunctions = {};
	
	uid2name = ();
	
	cases = ();
	blocks = ();
	closures = ();
	bscopes = ();
	declares = {};
	containment = {};
	
	fuid2str = ();
	fuid2type = ();
	
	overloadingResolver = ();
	overloadedFunctions = [];
}

// Get the type of an expression
Symbol getType(loc l) = config.locationTypes[l];

str getType(e) = "<getType(e@\loc)>";

// Get the outermost type constructor of an expression
str getOuterType(e) {
 tp = "<getName(getType(e@\loc))>";
// if(tp in {"int", "real", "rat"})
// 	tp = "num";
 return tp;
}

/* 
* CHANGED: 
* Getting a function type by name is problematic in case of nested functions,
* given that 'fcvEnv' does not contain nested functions;
* Additionally, it does not allow getting types of functions that are part of an overloaded function;
* Alternatively, the type of a function can be looked up by @loc;   
*/
Symbol getFunctionType(loc l) { 
   int uid = loc2uid[l];
   fun = config.store[uid];
   if(function(_,Symbol rtype,_,_,_,_) := fun) {
       return rtype;
   } else {
       throw "Looked up a function, but got: <fun> instead";
   }
}

Symbol getClosureType(loc l) {
   int uid = loc2uid[l];
   cls = config.store[uid];
   if(closure(Symbol rtype,_,_) := cls) {
       return rtype;
   } else {
       throw "Looked up a closure, but got: <cls> instead";
   }
}

// Compute the scope size, excluding declared nested functions and closures
int getScopeSize(str fuid) = size({ pos | int pos <- range(uid2addr)[fuid], pos != -1 })
								// r2mu translation of functions introduces variables in place of formal parameter patterns
								// and uses patterns to match these variables 
								+ size(fuid2type[invertUnique(fuid2str)[fuid]].parameters);

@doc{Make a call to a library function given its name, module's name and a number of its formal parameters}
public MuExp mkCallToLibFun(str modName, str fname, int nformals)
	= muFun("<modName>/<fname>(<nformals>)");

MuExp mkVar(str name, loc l) {
  
  int uid = loc2uid[l];
  
  // Pass all the functions through the overloading resolution
  if(uid in functions || uid in ofunctions) {
    // Get the function uids of an overloaded function
    set[int] ofuids = (uid in functions) ? { uid } : config.store[loc2uid[l]].items;
    // Generate a unique name for an overloaded function resolved for this specific use
    str ofuid = uid2str(config.usedIn[l]) + "/use:" + name;
    
    bool exists = ofuids in overloadedFunctions;
    int i = size(overloadedFunctions);
    if(!exists) {
    	overloadedFunctions += ofuids;
    } else {
    	i = indexOf(overloadedFunctions, ofuids);
    }   
    overloadingResolver[ofuid] = i;
    // TODO: New insight to the overloading and scoping semantics enables static resolution with respect to the 'scopeIn'
    // ***Note: r2mu translation does not care of whether the function is nested or not (no 'scopeIn' argument);
    //          now runtime system is responsible for this
  	return muOFun(ofuid);
  }
  
  tuple[str fuid,int pos] addr = uid2addr[uid];
  
  if(uid in constructors) {
  	return muConstr(name);
  }
  
  return muVar(name, addr.fuid, addr.pos);
}

tuple[str fuid,int pos] getVariableScope(str name, loc l) {
  tuple[str fuid,int pos] addr = uid2addr[loc2uid[l]];
  println("Get var scope: <name>: <addr.fuid>::<addr.pos>");
  return addr;
}


MuExp mkAssign(str name, loc l, MuExp exp) {
  //println("mkAssign: <name>");
  tuple[str fuid, int pos] addr = uid2addr[loc2uid[l]];
  res = "<name>::<addr.fuid>::<addr.pos>";
  //println("mkVar: <name> =\> <res>");
  return muAssign(name, addr.fuid, addr.pos, exp);
}

void extractScopes(){
   for(uid <- config.store){
      item = config.store[uid];
      switch(item){
        case function(rname,rtype,_,
        			  inScope,_,src):      { 
        							         functions += {uid};
                                             declares += {<inScope, uid>}; 
                                             loc2uid[src] = uid;
                                             for(l <- config.uses[uid]) {
                                                 loc2uid[l] = uid;
                                             }
                                             // Fill in uid2name
                                             name = getFUID(getSimpleName(rname),rtype);
                                             if(cases[inScope]?) {
                                                 if(cases[inScope][name]?) {
                                                     cases[inScope][name] = cases[inScope][name] + 1;
                                                 } else {
                                                    cases[inScope] = cases[inScope] + (name:0);
                                                 }
                                             } else {
                                                cases[inScope] = (name:0);
                                             }
                                             name = getFUID(getSimpleName(rname),rtype,cases[inScope][name]);
                                             uid2name[uid] = name;
                                             // Fill in fuid2type to enable more precise overloading resolution
                                             fuid2type[uid] = rtype;
                                           }
        case overload(_,_):                {
        								     ofunctions += {uid};
        								     for(l <- config.uses[uid]) {
        								     	loc2uid[l] = uid;
        								     } 
        								   }
        case variable(_,_,_,inScope,src):  { 
        									 variables += {uid};
        									 declares += {<inScope, uid>}; 
        									 loc2uid[src] = uid;
                                             for(l <- config.uses[uid]) {
                                                 loc2uid[l] = uid;
                                             }
                                           }
        case constructor(rname,rtype,
                         inScope,src):     { 
        									 constructors += {uid};
        									 declares += {<inScope, uid>};
        									 loc2uid[src] = uid;
        									 for(l <- config.uses[uid]) {
        									     loc2uid[l] = uid;
        									 }
        									 // Fill in uid2name
        									 // name = getFUID(getSimpleName(rname),rtype);
        								     uid2name[uid] = getSimpleName(rname);
        								   }
        case blockScope(inScope,src):      { 
        								     containment += {<inScope, uid>};
        									 loc2uid[src] = uid;
        									 // Fill in uid2name
        									 if(blocks[inScope]?) {
        									  	blocks[inScope] = blocks[inScope] + 1;
        									 } else {
        									  	blocks[inScope] = 0;
        									 }
        									 uid2name[uid] = "blk#<blocks[inScope]>";
        								   }
        case booleanScope(inScope,src):    { 
        								     containment += {<inScope, uid>}; 
        									 loc2uid[src] = uid;
        									 // Fill in uid2name
        									 if(bscopes[uid]?) {
        									    bscopes[uid] = bscopes[uid] + 1;
        									 } else {
        									    bscopes[uid] = 0;
        									 }
        									 uid2name[uid] = "bscope#<bscopes[uid]>";
        								   }
        case closure(rtype,inScope,src):       {
                                             functions += {uid};
                                             declares += {<inScope, uid>};
        									 loc2uid[src] = uid;
        									 // Fill in uid2name
        									 if(closures[uid]?) {
        									    closures[uid] = closures[uid] + 1;
        									 } else {
        									    closures[uid] = 0;
        									 }
        									 uid2name[uid] = "closure#<closures[uid]>";
        									 fuid2type[uid] = rtype;
        								   }
        case \module(RName rname, loc at):  {
        									 modules += uid;
        									 moduleNames += prettyPrintName(rname);
        									 // Fill in uid2name
        									 uid2name[uid] = prettyPrintName(rname);
        								   }
      }
    }
    
    containmentPlus = containment+;
    
    for(muid <- modules) {
    	// First, fill in variables to get their positions right
    	// Sort variable declarations to ensure that formal parameters get first positions preserving their order 
    	topdecls = sort([ uid | uid <- declares[muid], variable(_,_,_,_,_) := config.store[uid] ]);
    	for(i <- index(topdecls)) {
            uid2addr[topdecls[i]] = <getFUID(uid2str(muid),"#module_init_main",Symbol::func(Symbol::\value(),[Symbol::\list(\value())]),0), i + 1>;
    	}
    	// Then, functions
    	topdecls = [ uid | uid <- declares[muid], function(_,_,_,_,_,_) := config.store[uid] ||
    											  closure(_,_,_)        := config.store[uid] ||
    											  constructor(_,_,_,_)  := config.store[uid] ];
    	for(i <- index(topdecls)) {
    		// functions and closures are identified by their qualified names, and they do not have a position in their scope
    		// only the qualified name of their enclosing module or function is significant 
    		uid2addr[topdecls[i]] = <uid2str(muid), -1>;
    	}
    }

	// Fill in mapping of function uids to qualified names (enables invert mapping)
	for(int uid <- functions) {
		fuid2str[uid] = uid2str(uid);
	}
	
    for(int fuid <- functions) {
    	nformals = size(fuid2type[fuid].parameters);
        innerScopes = {fuid} + containmentPlus[fuid];
        // First, fill in variables to get their positions right
        // Sort variable declarations to ensure that formal parameters get first positions preserving their order
        decls = sort([ uid | uid <- declares[innerScopes], variable(_,_,_,_,_) := config.store[uid] ]);
        for(i <- index(decls)) {
        	// Note: we need to reserve positions for variables that will replace formal parameter patterns
        	uid2addr[decls[i]] = <fuid2str[fuid], i + nformals>;
        }
        // Then, functions
        decls = [ uid | uid <- declares[innerScopes], function(_,_,_,_,_,_) := config.store[uid] ||
        											  closure(_,_,_) := config.store[uid] ];
        for(i <- index(decls)) {
        	uid2addr[decls[i]] = <fuid2str[fuid], -1>;
        }
    }

}

str getFUID(str fname, Symbol \type) = "<fname>(<for(p<-\type.parameters){><p>;<}>)";
str getFUID(str fname, Symbol \type, int case_num) = "<fname>(<for(p<-\type.parameters){><p>;<}>)#<case_num>";
str getFUID(str modName, str fname, Symbol \type, int case_num) = "<modName>/<fname>(<for(p<-\type.parameters){><p>;<}>)#<case_num>";

str uid2str(int uid, str name) = uid2str(uid) + "/" + name;
str uid2str(int uid, str name, int \case) = uid2str(uid) + "/" + name + "#<\case>";

str uid2str(int uid) {
	if(!uid2name[uid]?) {
		throw "uid2str is not applicable!";
	}
	name = uid2name[uid];
	declaredIn = toMapUnique(invert(declares));
	containedIn = toMapUnique(invert(containment));
	if(containedIn[uid]?) {
		name = uid2str(containedIn[uid]) + "/" + name;
	} 
	else if(declaredIn[uid]?) {
		name = uid2str(declaredIn[uid]) + "/" + name;
	}
	return name;
}

public bool isDataType(AbstractValue::datatype(_,_,_,_)) = true;
public default bool isDataType(AbstractValue _) = false;

public bool isNonTerminalType(AbstractValue::sorttype(_,_,_,_)) = true;
public default bool isNonTerminalType(AbstractValue _) = false;

public bool isAlias(AbstractValue::\alias(_,_,_,_)) = true;
public default bool isAlias(AbstractValue _) = false;