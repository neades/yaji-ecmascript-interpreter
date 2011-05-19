// SwingPromptBox.java
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

import FESI.gui.PromptBox;

/**
 * Implementation of the prompt box
 */
public class SwingPromptBox implements PromptBox {

    private String response = null;

    /**
     * Create a PromptBox with default response
     * 
     * @param title
     *            The window title
     * @param prompt
     *            The prompt string
     * @param defaultResponse
     *            The default response (will be "" if null)
     */
    public SwingPromptBox(String title, String prompt, String defaultResponse) {

        response = JOptionPane.showInputDialog(null, prompt, title,
                JOptionPane.PLAIN_MESSAGE);
        if (response == null) {
            response = defaultResponse == null ? "" : defaultResponse;
        }
    }

    /**
     * Wait that the user returned a response
     * 
     * @return the string response (may be "" but not null)
     */
    public synchronized String waitResponse() {
        return response;
    }

}