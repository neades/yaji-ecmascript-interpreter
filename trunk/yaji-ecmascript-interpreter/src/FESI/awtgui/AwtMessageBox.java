// AwtMessageBox.java
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import FESI.gui.MessageBox;

/**
 * Implementation of message box function
 */
public class AwtMessageBox implements MessageBox {

    private boolean waiting = true;

    /**
     * Create a message box
     * 
     * @param title
     *            The window title of the message
     * @param message
     *            The message string
     */
    public AwtMessageBox(String title, String message) {

        final Frame frame = new Frame(title);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setBackground(Color.lightGray);
        // TextArea textArea = new TextArea(message);
        // textArea.setEditable(false);
        MultiLineLabel mll = new MultiLineLabel(message, 15, 15);
        frame.add("Center", mll);
        Panel panel = new Panel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        frame.add("South", panel);
        Button bOK = new Button("  OK  ");
        panel.add(bOK);

        frame.pack();
        Dimension dimScreen = frame.getToolkit().getScreenSize();
        Dimension dimWindow = frame.getSize();
        frame.setLocation(dimScreen.width / 2 - dimWindow.width / 2, // Center
                                                                     // screen
                dimScreen.height / 2 - dimWindow.height / 2);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
                completed();
            }
        });
        bOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
     * Wait that the user confirmed reception of the message
     */
    public synchronized void waitOK() {
        while (waiting) {
            try {
                wait();
            } catch (Exception e) {
                System.err.println("Exception ignored "+e.getMessage());
            }
        }
    }

}