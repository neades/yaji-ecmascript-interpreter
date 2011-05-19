// InterpreterCommands.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package FESI.gui;

/**
 * Define the interface to the commands of the interpreter which can be called
 * by the gui
 */
public interface InterpreterCommands {

    /**
     * Exit the interpreter (and EOF was detetected)
     */
    void exit();

    /**
     * Load and execute a file
     */
    void loadFile(String directoryName, String fileName);

    /**
     * Execute a string - return the line number of any error or 0
     * 
     * @param text
     *            The text to execute
     * @param source
     *            The identification of the source
     * @return the line number of any error if possible or 0
     */
    int executeString(String text, String source);

    /**
     * Display the @about text information
     */
    void displayAboutText();

    /**
     * Display the @help text information
     */
    void displayHelpText();

}