FESI - Free EcmaScript Interpreter
==================================

FESI Copyright (c) Jean-Marc Lugrin, 1996-2003

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

To install Fesi, double click on the installation jar, or 
execute it using a command like 

	java -jar install-fesi.jar

A proper java version must be installed on your machine 
(tested with 1.4 only).

This start an installer that will prompt you for location and
modules to install. The sources are included in the distribution
kit.




Notes:

There is currently no specific help files, so the help points to the
FESI home page available locally. Not all link work properly, due to
limitation in the Swing HTML component.


    
Release 1.0 - 18 October 1998
    Initial public release with sources (after extensive testing). 
  
Release 1.0.1
    Use FESI_HOME in batch files (from Matt Humphrey).
    Corrected bug on Date(date) constructor (from Hannes Wallnoefer).
    getWelcomeText made public on evaluator.
  
Release 1.0.2 - 16 Janvier 1999
    tryEval now return the evaluated value (rather than the first 
    parameter), as is documented and useful (from Kurt Westerfeld).
    Added getVersion and getWelcomeText to the JSUtil package.
    Corrected bug in @listAll of java arrays (from Kurt Westerfeld.
    MathObject initialization moved around to help JBuilder users.
    Corrected bug in EcmaScriptException printing (from Kurt Westerfeld).
    Routine doWork in Interpreter made public (to ease embedding the
      interpreter in user code).
    Enhancement in routine lookup in inside class of interfaces (based
      on an idea of Rich Kadel)
    
Release 1.1 - 7 March 1999
    WARNING: Use Swing 1.1 (javax.swing) in the swinggui !
    A new option -e allows to load extensions from the command line.
    Option documentation upgraded.
    The Interpreter does not requires the extensions to be present
    during compilation (allow compilation and load without the Swing
    package or ORO libraries).
    Various changes to ease subclassing the Interpreter.
    Validated under Java 1.2.
    Enhanced selection of method overloaded with multiple 
    primitive types (int, long, ...).
    Added access to CORBA properties (without set/get prefix)
  From Kurt Westerfeld:
    Fix around Console to support the MS environment 
    Bug correction in ESObject, avoiding an endless loop if an object
    without a valid default string value was converted to string.
    Added getMetaData() to connection and rowset.
    
Release 1.1.1 - 15 March 1999
    Minor code cleanup
    Corrected many bad link in html documentation

Release 1.1.2 - 4 May 1999
    Avoid converting BigInteger to builtin numbers (thanks to Denis Bohm)
    Add support to sort function in Array.sort (thanks to Hannes Wallnoefer)
    Corrected bug in normalize value to support arrays (thanks to Kurt Westerfeld)
    Corrected bug in array enumeration when non indexed properties were present.
    Transformed parameters of JSFunction to JSObject when possible.

NOTES:
	The JIT on Windows is usually buggy (including in version 1.2.1), so 
	before reporting any bug, please try the nojit variant.
	Especially 
		java.lang.NullPointerException
		at FESI.Parser.ASCII_CharStream.readChar(Compiled Code)
	which was corrected by a patched jit for 1.2 re-appear with 1.2.1
	The hotspot 1.0.1 (on 1.2.2) is very buggy too.

Release 1.1.3 - 25 Decembre 1999
    CHANGED LICENSES CONDITIONS TO LGPL
    Use javaCC 1.1.
    Corrected bug in getting path separator (for Linux).
    Pass call to functions to the prototype even if prototype is a
       native object.
    Add extensions of initialized objects (from Christophe Marton).
    new Date() correctly initialize to current time.
    Corrected bug in parseInt of large numbers.
    Corrected bug when RegExp matched but one group did not match.

    The limitation on calling public methods of non public classes
	is documented (thanks to Jason Mathews).
    The LINK.HTML page has been updated.

Release 1.1.4 - 30 Janvier 2000
    Documented the SETUP process for Windows
    GNU regexp are supported (Thanks to Mike Dillon)

Release 1.1.5 - 29 July 2000
    Corrected bug in ESArguments which, in case a parameter was
    	missing in a function call, made the routine looking for 
    	the global variable instead of an undefined local one 
    	(thanks to Rob Noble and Sergey Borisov).    
    Use ESUndefined when assigning from an empty statement, a
    	missing else part (thanks to Rob Noble) and most other cases
    	where a null pointer exception was generated. This can be
    	changed in EcmaScriptEvaluateVisitor (routine acceptNull)
    	to generate an error instead).
    Hack (adding a new line) to work around a parsing limitation
     	which generated an error when a single line program was 
        terminated by a // comment.

Release 1.1.6 - 4 Aug 2003
    Avoid a useless backtrack in lexical parsing.
    Corrected a bug in scope chain and prototype, added test
    	in validation suite (thanks to Michael Schneider).
    Corrected a bug in 'this' for called functions (thanks to 
    Michael Schneider and a few others).
    Corrected a bug in getTimezoneOffset (thank to Wendell T. Hicken)
    Added substr from Wendell T. Hicken 
    Added command @module to load module, use @load to load files
    Normalized import statements (using Eclipse)
    Use latest version of ORO and GNU regexpt libraries, removed 
		deprecated methods used for OROlib.
    Use latest version (3.1, open source, renamed the reader) of JavaCC
    Use IzPack installer (http://www.izforge.com/izpack/)
    Combine source,executable and examples in one kit.
    Use ANT to control the code and site generation.
    Currently removed the FesiFTP example (lack of time to integrate).

Release 1.1.7 - 31 Aug 2003
    Some enhancements in installatation script and documentation
    Support of BSF, version 2.2 (see Bean Scripting Framwork), allowing to 
		call FESI from a common interface used by many tools.
    Added example for swing (examples/swing/swingev.esw) that demonstrate use of events.
    Added getWrappedObject and getWrappedBean to the js package.
    Upgraded to latest JavaCC and IzPack (no user visible change).


Release 1.1.8 - 29 September 2003
    Support of BSF migrated to version 2.3 (see Bean Scripting Framwork),
		that is maintained by apache.
    Upgraded to latest IzPack (no user visible change).
    Include a precompiled jar for execution on JDK 1.1.8, with accompanying
	command files (JDK 1.4 has a different class file format).





    
