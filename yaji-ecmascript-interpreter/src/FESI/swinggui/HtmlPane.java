// HtmlPane.java
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

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class HtmlPane extends JEditorPane {

    private static final long serialVersionUID = 4108928093410972316L;
    private HelpWindow helpWindow;
    private JPopupMenu popupMenu;

    public HtmlPane(HelpWindow helpWindow, URL url) throws IOException {
        super(url);
        this.helpWindow = helpWindow;

        // Add popup menu
        popupMenu = new JPopupMenu("Help");
        JMenuItem menuContent = new JMenuItem("Content");
        menuContent.setMnemonic('C');
        menuContent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                HtmlPane.this.helpWindow.gotoContentPage();
            }
        });
        popupMenu.add(menuContent);
        JMenuItem menuBack = new JMenuItem("Back");
        menuBack.setMnemonic('B');
        menuBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                HtmlPane.this.helpWindow.backHistory();
            }
        });
        popupMenu.add(menuBack);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    @Override
    public void processMouseEvent(MouseEvent event) {
        if (event.isPopupTrigger()) {
            popupMenu.show(event.getComponent(), event.getX(), event.getY());
        } else {
            super.processMouseEvent(event);
        }
    }

}
