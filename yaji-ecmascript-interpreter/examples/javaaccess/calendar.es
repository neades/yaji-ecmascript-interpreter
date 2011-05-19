// calendar.es
//
// FESI examples - JM Lugrin - 1998
//
// This example demonstrates how to use FESI to play
// with a java package. An environment is setup by this
// file, that the user should load as an initialization
// file for the interpreter.  Abreviations are defined,
// as well as a few useful functions. A help() function
// list the available abreviations and functions, and a
// sample() function demonstrate some tests.

// Requires: BasicIO, JavaAccess (standard in the interpreter)
// Used as:  fesiiw calendar.es  (for a windowed interpreter)
//           fesii calendar.es  (for a console interpreter)

// This file is used as a load file to setup the playground

// Abreviate some common classes for interactive use

GC = GregorianCalendar = java.util.GregorianCalendar
C = Calendar = java.util.Calendar
D = Date = java.util.Date
TZ = TimeZone = java.util.TimeZone
TF = Format = java.text.Format;
DF = DateFormat = java.text.DateFormat;
L = Locale = java.util.Locale;



// A function to list the locales known the the Calendar

function locales() {
  var ls = C.getAvailableLocales();
  var i,j,k;
  for (i=1; i<ls.length; i+=4) {
     j = Math.min(i+4, ls.length);
	 for (k = i; k<j; k++) {
         write (ls[k] + "\t");
     }
	 writeln();
  }
}


// A function to list the ids known the the TimeZone

function tzids() {
  var ls = TZ.getAvailableIDs();
  var i,j,k;
  for (i=1; i<ls.length; i+=4) {
     j = Math.min(i+4, ls.length);
	 for (k = i; k<j; k++) {
         write (ls[k] + "\t");
     }
	 writeln();
  }
}


// So that it is easy to remember which abreviation have
// been defined by the load file

function help () {
  writeln ("  GC: GregorianCalendar, C: Calendar");
  writeln ("  D: Date,  TZ: TimeZone, STZ: SimpleTimeZone");
  writeln ("  TF: text.Format, DF: text.DateFormat");
  writeln ("  L: Locale");
  writeln ("  locales() = list available locales");
  writeln ("  tzids() = list available time zone ids");
}


// An utility routine to evaluate and display strings

function e (what) {
   write("Eval: '" + what + "' result: '");
   var result = eval(what);
   writeln(result +"'");
 }
 
 
 // Show how it could be used interactively
 // The short abreviations are used as would be the
 // case by a lazy programmer.
 // The results are left in the global name space for the
 // user to play with
 
 function sample() {
    writeln();
    e ("TZ.getDefault().getID()");
    e ("TZ.getDefault().getRawOffset()");
	e ("cgmt = new GC(TZ.getTimeZone('GMT'))");
	e ("fmt = DF.getDateTimeInstance(DF.LONG, DF.LONG); fmt.setCalendar(cgmt); fmt");
	e ("fmt.format(new D())");
	writeln();
	e ("cect = new GC(TZ.getTimeZone('ECT'))");
	e ("lde = new Locale('de','DE')");
	e ("fmtde = DF.getDateTimeInstance(DF.LONG, DF.LONG, lde); fmtde.setCalendar(cect); fmtde");
	e ("fmtde.format(new D())");
	writeln();
	writeln("Type help() for list of variables and commands");
	writeln("Type test() for various tests");
	writeln("Type sample() for theses examples");
 }
 
 
 
// A more complete test example - longer abvreviation of package
// are used for clarity

function test () {
  writeln();
  
  // We avoid poluting the global name space
  var loc, ect, gmt, here, there, nongmt, dfHere, dfThere, lde, dfHere, dfThere;
  
  // Get local time zone information
  loc = TimeZone.getDefault();
  writeln("Default timezone ('loc = TimeZone.getDefault(), loc.getID()'): " + loc.getID() + 
  		" at offset ('loc.getRawOffset()'): " + loc.getRawOffset() +
		" (" + (loc.getRawOffset()/3600000) + " hrs)");
		
  // Get ECT and GMT time zone information
  ect = TimeZone.getTimeZone("ECT");
  writeln("ECT timezone ('ect = TimeZone.getDefault(), ect.getID()'): " + ect.getID() + 
                " at offset ('ect.getRawOffset()'): " + ect.getRawOffset() +
  				" (" + (ect.getRawOffset()/3600000) + " hrs)");
  gmt = TimeZone.getTimeZone("GMT");
  writeln("GMT timezone('gmt = TimeZone.getDefault(), gmt.getID()'): " + gmt.getID() + 
  				" at offset ('gmt.getRawOffset()'): " + gmt.getRawOffset() +
  				" (" + (gmt.getRawOffset()/3600000) + " hrs)");
  writeln();
  
  // Select an HERE and a THERE, one of them being GMT
  if (loc.getID() == gmt.getID()) {
    here = loc;
	there = ect;
	nongmt = ect;
  } else {
    here = loc;
	there = gmt;
	nongmt = loc;
  }
  
  now = new Date();
  cHere = new GregorianCalendar(here); cThere = new GregorianCalendar(there);
  
  lde = new Locale("de","DE");
  dfHere = DateFormat.getDateTimeInstance(DF.LONG, DF.LONG); dfHere.setCalendar(cHere); 
  dfThere = DateFormat.getDateTimeInstance(DF.LONG, DF.LONG, lde ); dfThere.setCalendar(cThere); 
  
  //cHere.setTime(new D()); cThere.setTime(new D());
  writeln("Two calendars and date formatter setup: here and there");
  writeln("here: " + dfHere.format(now) + ", there: " + dfThere.format(now));
  
  
}




help();
sample();
  



