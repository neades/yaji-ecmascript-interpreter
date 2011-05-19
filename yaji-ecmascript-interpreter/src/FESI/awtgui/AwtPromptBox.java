// PromptBox.java
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

package FESI.awtgui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import FESI.gui.PromptBox;

/**
 * Implementation of the prompt box
 */
public class AwtPromptBox implements PromptBox {

    private boolean waiting = true;
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
    public AwtPromptBox(String title, String prompt, String defaultResponse) {

        response = defaultResponse == null ? "" : defaultResponse;
        final Frame frame = new Frame(title);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setBackground(Color.lightGray);
        MultiLineLabel mll = new MultiLineLabel(prompt, 15, 15);
        frame.add("Center", mll);
        Panel panel = new Panel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        frame.add("South", panel);
        final TextField tf = new TextField(response, 20);
        if (!response.equals("")) {
            tf.setSelectionStart(0);
            tf.setSelectionEnd(response.length());
        }
        panel.add(tf);
        Button bOK = new Button("OK");
        panel.add(bOK);

        frame.pack();
        Dimension dimScreen = frame.getToolkit().getScreenSize();
        Dimension dimWindow = frame.getSize();
        frame.setLocation(dimScreen.width / 2 - dimWindow.width / 2, // Center
                                                                     // screen
                dimScreen.height / 2 - dimWindow.height / 2);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                response = "";
                frame.setVisible(false);
                frame.dispose();
                completed();
            }
        });
        bOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = tf.getText();
                frame.setVisible(false);
                frame.dispose();
                completed();
            }
        });
        tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = tf.getText();
                frame.setVisible(false);
                frame.dispose();
                completed();
            }
        });
        frame.setVisible(true);
    }

    private synchronized void completed() {
        waiting = false;
        notifyAll();
    }

    /**
     * Wait that the user returned a response
     * 
     * @return the string response (may be "" but not null)
     */
    public synchronized String waitResponse() {
        while (waiting) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        return response;
    }

}

// This example is from _Java Examples in a Nutshell_. (http://www.oreilly.com)
// Copyright (c) 1997 by David Flanagan
// This example is provided WITHOUT ANY WARRANTY either expressed or implied.
// You may study, use, modify, and distribute it for non-commercial purposes.
// For any commercial use, see http://www.davidflanagan.com/javaexamples