// juggler.esw
//
// FESI examples - JM Lugrin - 1998
//
// This example demonstrates how the a bean can be loaded

// Requires: JavaAccess (standard in the interpeter)
// Used as:  fesiw juggler.esw

TT = Beans("D:/java11/bdk/jars/misc.jar")
tt = new TT.sunw.demo.misc.TickTock;

// Use Bean field
tt.interval = 3; 

tt.onPropertyChange = "writeln(event)";
writeln("Ticking every " + tt.interval + " seconds, type ^C to terminate");
