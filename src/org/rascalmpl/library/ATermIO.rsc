module ATermIO

/*
 * Read/writing of values in ATerm format:
 * - readTextATermFile
 * - readTextATermFile
 * - writeTextATermFile
 *
 * ATerms are the ubiquitous exchange format used by ToolBus,
 * ASF+SDF Meta-Environment, and others to exchange data.
 */

@doc{read an ATerm from a text file}
@javaClass{org.rascalmpl.library.ATermIO}
@reflect{Uses URI Resolver Registry}
public &T java readTextATermFile(type[&T] start, loc location);

public value readTextATermFile(loc location) {
  return readTextATermFile(#value, location);
}

@doc{write an ATerm to a text file}
@javaClass{org.rascalmpl.library.ATermIO}
@reflect{Uses URI Resolver Registry}
public void java writeTextATermFile(loc location, value v);
