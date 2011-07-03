// HelpWindow.java
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

// NOTE: Largely copied from the book Up to Speed with Swing

package FESI.swinggui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Help window (HTML based)
 */

public class HelpWindow extends JFrame implements HyperlinkListener {

    private static final long serialVersionUID = -1803172875103338070L;

    private HtmlPane html;

    private ArrayList<URL> history = new ArrayList<URL>();
    private URL contentPageURL = null;

    // for use by package only
    HelpWindow(String helpLocation) {
        setTitle("FESI Help");
        setSize(600, 500);
        setBackground(Color.gray);
        getContentPane().setLayout(new BorderLayout());

        // Put the window in the middle of the screen (nicer for Windows 95)
        int scrwidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int scrheight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int conwidth = getSize().width;
        int conheight = getSize().height;

        setLocation((scrwidth - conwidth) / 2, (scrheight - conheight) / 2);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.CENTER);

        try {
            // Load the entry URL
            contentPageURL = new URL(helpLocation);

            // Create an HTML viewer to display the URL
            html = new HtmlPane(this, contentPageURL);
            html.setEditable(false);
            addHistory(contentPageURL); // Keep track of history

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.getViewport().add(html, BorderLayout.CENTER);

            topPanel.add(scrollPane, BorderLayout.CENTER);
            html.addHyperlinkListener(this);
        } catch (MalformedURLException e) {
            System.err.println("[[Error opening HELP url '" + helpLocation
                    + "': " + e + "]]");
        } catch (IOException e) {
            System.err.println("[[Error accessing HELP url '" + helpLocation
                    + "': " + e + "]]");
        }
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setVisible(true);

    }

    public void addHistory(URL url) {
        history.add(url);
    }

    public void gotoContentPage() {
        if (contentPageURL != null) {
            Cursor cursor = html.getCursor();
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            html.setCursor(waitCursor);
            SwingUtilities.invokeLater(new PageLoader(null, html,
                    contentPageURL, cursor));
        }
    }

    public void backHistory() {
        int last = history.size() - 2; // -1 to skip current, -1 for base 0
        if (last >= 0) {
            URL url = history.get(last);
            history.remove(last);
            Cursor cursor = html.getCursor();
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            html.setCursor(waitCursor);
            SwingUtilities.invokeLater(new PageLoader(null, html, url, cursor));
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            Cursor cursor = html.getCursor();
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            html.setCursor(waitCursor);
            SwingUtilities.invokeLater(new PageLoader(this, html, event
                    .getURL(), cursor));
        }
    }

}