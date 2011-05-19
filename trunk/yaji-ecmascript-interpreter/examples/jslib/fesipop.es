// fesipop.es - jm lugrin, 1998
//
// Example using FesiPop
// To use this example, you must compile the FesiPop extension
// and add it to the classpath of the interpreter. Adapt
// the server name (beware, the server name may not be the
// same as your internet home address, check with your
// system administrator or internet provided. You need the
// POP3 server name!). Adapt the user name. You can then
// load this file. for a test. It just get the list of
// messages and print the first one if any.
// With some extension you could do various thinks, like
// taking some automatic action depending on the subject.
// For example forwarding some mails to another address,
// or checking if there is some mail labelled URGENT, etc...


if (loadExtension('FesiPop')) {
   writeln("FesiPop loaded");
} else {
   writeln("FesiPop NOT loaded");
}
server = "your.server.here"; // Your mail server here
user = "yournamehere";       // Your user name here
p = new POP3(server,user,prompt('password'));
p.onError = new Function('e','alert(e); exit(1);');
p.connect();
writeln("POP3 object connected: " + p);

m = p.messageList;
writeln(m.length + " messages in input box");
// Write first message if there is at leat one
// We could do more clever stuff, like deleting all
// messages with "money" in the header...
if (m.length>0) {
   msg = m[0].getMessageText();
   writeln(msg);
}
p.disconnect();
