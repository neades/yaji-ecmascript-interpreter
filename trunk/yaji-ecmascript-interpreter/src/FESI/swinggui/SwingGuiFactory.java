// SwingGuiFactory.java
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

package FESI.swinggui;

import FESI.gui.Console;
import FESI.gui.GuiFactory;
import FESI.gui.InterpreterCommands;
import FESI.gui.MessageBox;

/**
 * Factory class to create the GUI elements, used to achieve independence of GUI
 * implementation at compile time.
 */
public class SwingGuiFactory extends GuiFactory {

    /**
     * Must have a null constructor so that it can be created by newInstance
     */
    public SwingGuiFactory() {
        super();
    }

    /**
     * Display an error message using the AWT message box
     */
    @Override
    public MessageBox displayMessageBox(String title, String msg) {

        return new SwingMessageBox(title, msg);
    }

    /**
     * Create aa new console
     */
    @Override
    public Console makeConsole(InterpreterCommands itrpParam, String title,
            int rows, int columns) {
        return new SwingConsole(itrpParam, title, rows, columns);
    }

}