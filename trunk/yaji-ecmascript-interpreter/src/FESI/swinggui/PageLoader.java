// PageLoader.java
// From the book Up to Speed with Swing

package FESI.swinggui;

import java.awt.Container;
import java.awt.Cursor;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

class PageLoader implements Runnable {
    private HelpWindow helpWindow; // Null if no save of history because going
                                   // backward
    private JEditorPane html;
    private URL url;
    private Cursor cursor;

    PageLoader(HelpWindow helpWindow, JEditorPane html, URL url, Cursor cursor) {
        this.helpWindow = helpWindow;
        this.html = html;
        this.url = url;
        this.cursor = cursor;
    }

    public void run() {
        if (url == null) {
            // restore the original cursor
            html.setCursor(cursor);

            // PENDING(prinz) remove this hack when
            // automatic validation is activated.
            Container parent = html.getParent();
            parent.repaint();
        } else {
            Document doc = html.getDocument();
            try {
                html.setPage(url);

                // add to history
                if (helpWindow != null) {
                    helpWindow.addHistory(url);
                }
            } catch (IOException ioe) {
                System.err.println("IOException load url " + url + ", " + ioe);
                html.setDocument(doc);
            } finally {
                // schedule the cursor to revert after
                // the paint has happended.
                url = null;
                SwingUtilities.invokeLater(this);
            }
        }
    }
}