package org.rascalmpl.interpreter.types;

import org.eclipse.imp.pdb.facts.type.DefaultTypeVisitor;
import org.eclipse.imp.pdb.facts.type.Type;

public abstract class DefaultRascalTypeVisitor<T, E extends Throwable> extends DefaultTypeVisitor<T, E> implements IRascalTypeVisitor<T, E> {

  public DefaultRascalTypeVisitor(T def) {
    super(def);
  }

  @Override
  public T visitExternal(Type type) throws E {
    assert type instanceof RascalType;
    return ((RascalType) type).accept(this);
  }
  
  @Override
  public T visitFunction(RascalType type) throws E {
    return def;
  }

  @Override
  public T visitOverloadedFunction(RascalType type) throws E {
    return def;
  }

  @Override
  public T visitReified(RascalType type) throws E {
    return def;
  }

  @Override
  public T visitNonTerminal(RascalType type) throws E {
    return def;
  }
}
