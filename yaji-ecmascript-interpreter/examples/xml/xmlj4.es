// xml4j.es
//
// Demonstrate the use of the IBM XML parser with the DOM
//
// Create the parser and read the document
fileName = "test.xml";
is = new java.io.FileInputStream(fileName);
parserPackage = Packages("d:/java11/xml4j/xml4j_1_0_9.jar").com.ibm.xml.parser;
parser = new parserPackage.Parser(fileName);
document = parser.readStream(is);

// Print some interesting properties of the document in an indented format
types = new Array("Document","Element","Attribute","PI","Comment","Text");
doTree(document,"");

function doTree(tree, offset) {
	var t = tree.getNodeType();
	write(offset + "Type: " + types[t-1]);
	if (t==1) { // document
	       writeln(", ROOT");
	if (t==2) { // element
	       writeln(", <" + tree.getTagName() + ">");
	} else if (t==6) { // text
	       writeln(', "' + tree.getData() + '"');
	} else {
	   writeln();
	}

	var node = tree.getFirstChild();
	while (node != null) {
	   doTree(node, offset + "  ");
	   node = node.getNextSibling();
	}
}

