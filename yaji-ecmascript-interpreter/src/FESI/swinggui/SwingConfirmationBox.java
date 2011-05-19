// SwingConfirmationBox.java
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

import javax.swing.JOptionPane;

import FESI.gui.ConfirmationBox;

/**
 * Implementation of a confirmation box
 */
public class SwingConfirmationBox implements ConfirmationBox {

    private boolean response = false;

    /**
     * Create a confirmation box
     * 
     * @param title
     *            The title string
     * @param message
     *            The message text
     */
    public SwingConfirmationBox(String title, String message) {
        response = (JOptionPane.showConfirmDialog(null, message, title,
                JOptionPane.YES_NO_OPTION)) == JOptionPane.YES_OPTION;
    }

    /**
     * Wait for the answer and return it
     * 
     * @return the answer as a boolean
     */
    public boolean waitYesOrNo() {
        return response;
    }

}

// This example is from _Java Examples in a Nutshell_. (http://www.oreilly.com)
// Copyright (c) 1997 by David Flanagan
// This example is provided WITHOUT ANY WARRANTY either expressed or implied.
// You may study, use, modify, and distribute it for non-commercial purposes.
// For any commercial use, see http://www.davidflanagan.com/javaexamples