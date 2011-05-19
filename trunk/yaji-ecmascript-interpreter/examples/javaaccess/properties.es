// properties.es
//
// FESI examples - JM Lugrin - 1998
//
// This example demonstrates how the system properties
// can be simply listed with a short EcmaScript
//
// Requires: BasicIO, JavaAccess (standard in the interpreter)
// Used as:  fesi calendar.es  


// Get the properies

properties = java.lang.System.getProperties();

// List them, using a java enumerators

for (key in properties.keys()) {
   value = properties.get(key);
   writeln ("Property '" , key, " = '", value, "'");
}

