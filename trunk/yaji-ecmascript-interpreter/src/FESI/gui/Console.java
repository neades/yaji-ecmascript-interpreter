// Console.java
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

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Define the interface of the console interface
 */
public interface Console {

    /**
     * Get the stream consisting of characters typed by the user
     */
    InputStream getConsoleIn();

    /**
     * Get the stream to which to print characters to be displayed on the
     * console
     */
    PrintStream getConsoleOut();

    /**
     * Clear the console content
     */
    void clear();

    /**
     * Release the console resources
     */
    void dispose();

    /**
     * Inform if editing is supported
     */
    boolean supportsEditing();

    /**
     * Create a new editing window for the specified file
     */
    void createEditor(String fileName);

    /**
     * Display the about information
     */
    void displayAbout();

    /**
     * Display the @help text information
     */
    void displayHelpText();

    /**
     * Display the help window
     */
    void displayHelpWindow();

}
