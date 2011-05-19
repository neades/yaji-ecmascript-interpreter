// event.es
//
// FESI examples - JM Lugrin - 1998
//
// This example demonstrates event handling.

// Requires: JavaAccess (standard in the interpeter)
// Used as:  fesi event.es

Frame = java.awt.Frame;
Button = java.awt.Button;

f = new Frame("Event");
f.setSize(200,200)

b = new Button("Click or type");
f.add("Center",b)
f.pack();
f.show()

// The first eventhandler  will be overriden
b.onAction = "writeln ('First handler');";
// Only this handler will get events
b.onAction = b.onAction = new Function("writeln ('Second handler')");

// This handler use the event parameter
b.onKeyTyped = "writeln('KEY: ' + event);";

// Demonstrate an event routine defined as a function
function dispose(event) {
  writeln();
  writeln("Event received: " + event);
  writeln("By: " + this);
  this.dispose();
  exit();
}
f.onWindowClosing = dispose;
writeln("ready");



