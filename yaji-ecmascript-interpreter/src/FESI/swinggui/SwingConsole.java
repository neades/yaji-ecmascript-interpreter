// SwingConsole.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import FESI.gui.InterpreterCommands;

// Inspired by JConsole v0.9 of Henrik Bengtsson
// Rewritten for Swing by JMLugrin

public class SwingConsole extends JFrame implements ExtendedConsole {

    private static final long serialVersionUID = 2992136937357172311L;

    private static String eol = System.getProperty("line.separator", "\n");

    private static int newWindowCounter = 0;
    private Vector<Editor> editorWindows = new Vector<Editor>();

    private JConsoleArea theConsoleArea;
    private JScrollPane theScrollPane;

    private HelpWindow helpWindow = null;

    // There should be only one console in the system
    private transient InputStream consoleIn;
    private PrintStream consoleOut;
    private FESI.gui.InterpreterCommands itrp = null;

    TextAreaInputStream textAreaInputStream = null;

    /**
     * A window signals that it is closed
     */
    private void editorWindowClosed(Editor editor) {
        editorWindows.removeElement(editor);
    }

    /**
     * Get the stream consisting of characters typed by the user
     */
    public InputStream getConsoleIn() {
        return consoleIn;
    }

    /**
     * Get the stream to which to print characters to be displayed on the
     * console
     */
    public PrintStream getConsoleOut() {
        return consoleOut;
    }

    /**
     * Create a new console interface
     * 
     * @param itrpParam
     *            The interpreter which will receive the exit command when EOF
     *            is received.
     * @param title
     *            The window title string
     * @rows Starting number of rows
     * @columns Starring number of columnes
     */
    public SwingConsole(InterpreterCommands itrpParam, String title, int rows,
            int columns) {
        super(title);

        itrp = itrpParam;

        this.getContentPane().setLayout(new BorderLayout());

        theConsoleArea = new JConsoleArea(this, rows, columns);
        theConsoleArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        theConsoleArea.setBackground(Color.lightGray);
        // this.getContentPane().add(theConsoleArea);
        theScrollPane = new JScrollPane(theConsoleArea);
        theScrollPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.getContentPane().add("Center", theScrollPane);

        theConsoleArea.setVisible(true);
        theConsoleArea.setBackground(Color.white);

        // Define the MENUBAR
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        // New
        JMenuItem newItem = new JMenuItem("New  Ctrl+N");
        newItem.setMnemonic('N');
        newItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.commandNew();
            }
        });
        fileMenu.add(newItem);
        // Shortcut ^N
        theConsoleArea.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.commandNew();
            }
        }, KeyStroke.getKeyStroke('N', Event.CTRL_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Load a file - use AWT file dialog as the Swing one is still not
        // defined !
        JMenuItem loadItem = new JMenuItem("Load...  Ctrl+L");
        loadItem.setMnemonic('L');
        loadItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.commandLoad();
            }
        });
        fileMenu.add(loadItem);
        // Shortcut ^L
        theConsoleArea.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.commandLoad();
            }
        }, KeyStroke.getKeyStroke('L', Event.CTRL_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // open a file for editing - use AWT file dialog as the Swing one is
        // still not defined !
        JMenuItem openItem = new JMenuItem("Open...  Ctrl+O");
        openItem.setMnemonic('O');
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.commandOpen();
            }
        });
        fileMenu.add(openItem);
        // Shortcut ^O
        theConsoleArea.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.commandOpen();
            }
        }, KeyStroke.getKeyStroke('O', Event.CTRL_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Exit
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                commandExit();
            }
        });
        fileMenu.add(exitItem);

        // Add menu bar
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        // Clear
        JMenuItem clearItem = new JMenuItem("Clear all");
        clearItem.setMnemonic('a');
        clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.clear();
            }
        });
        editMenu.add(clearItem);
        editMenu.addSeparator();

        // Cut
        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setMnemonic('t');
        cutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.theConsoleArea.cut();
            }
        });
        editMenu.add(cutItem);

        // Copy
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setMnemonic('C');
        copyItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.theConsoleArea.copy();
            }
        });
        editMenu.add(copyItem);

        // Paste at end
        JMenuItem pasteItem = new JMenuItem("Paste at end");
        pasteItem.setMnemonic('P');
        pasteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.theConsoleArea.paste();
            }
        });
        editMenu.add(pasteItem);

        // Add menu to menu bar
        menuBar.add(editMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        // Help content
        JMenuItem contentItem = new JMenuItem("Content  F1");
        contentItem.setMnemonic('C');
        contentItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.displayHelpWindow();
            }
        });
        helpMenu.add(contentItem);
        // Shortcut F1
        theConsoleArea.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.displayHelpWindow();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Help commands
        JMenuItem commandsItem = new JMenuItem("Commands");
        commandsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.displayHelpText();
            }
        });
        helpMenu.add(commandsItem);

        // About
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                SwingConsole.this.displayAbout();
            }
        });
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        // Add menu to menu bar
        menuBar.add(helpMenu);

        // Add menu bar
        this.getContentPane().add("North", menuBar);

        this.pack();

        // Put the console in the middle of the screen (nicer for Windows 95)
        int scrwidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int scrheight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int conwidth = getSize().width;
        int conheight = getSize().height;

        setLocation((scrwidth - conwidth) / 2, (scrheight - conheight) / 2);

        // Create the stream for I/O in the consoles
        textAreaInputStream = new TextAreaInputStream(theConsoleArea);
        consoleIn = new LineInputStream(textAreaInputStream);
        // USE DEPRECATED ROUTINE AS DEFAULT IO STREAMS USE DEPRECATED STREAMS
        consoleOut = new PrintStream(new TextAreaOutputStream(theConsoleArea),
                true);

        // Nice attempt - but does not always work...
        System.setOut(consoleOut);
        System.setIn(consoleIn);
        System.setErr(consoleOut);

        // Support controlled closing
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                boolean abort = false;
                int nWindows = editorWindows.size();
                for (int i = 0; i < nWindows && !abort; i++) {
                    Editor editor = editorWindows.elementAt(i);
                    abort = editor.checkDirty();
                    if (!abort) {
                        Frame editorFrame = editor.getFrame();
                        editorFrame.dispose();
                    }
                }
                if (!abort)
                    dispose(); // will call exit
            }

            public void windowClosed(WindowEvent ev) {
                itrp.exit();
            }
        });

        setVisible(true);
        theConsoleArea.requestFocus();
    }

    /**
     * Process the exit command, first saving any dirty buffer if required
     */
    public void commandExit() {
        boolean abort = false;
        int nWindows = editorWindows.size();
        for (int i = 0; i < nWindows && !abort; i++) {
            Editor editor = editorWindows.elementAt(i);
            abort = editor.checkDirty();
        }
        if (!abort) {
            if (helpWindow != null) {
                helpWindow.dispose();
                helpWindow = null;
            }
            itrp.exit();
        }
    }

    /**
     * Load a file based on file dialog
     */
    public void commandLoad() {
        FileDialog fd = new FileDialog(this, "File to load");
        fd.setVisible(true);
        String directoryName = fd.getDirectory();
        String fileName = fd.getFile();
        // System.out.println("D: " + directoryName + ", f: " + fileName);
        if (fileName != null && directoryName != null) {
            itrp.loadFile(directoryName, fileName);
        }
    }

    /**
     * Create a new editor
     */
    public void commandNew() {
        newWindowCounter++;
        final JFrame frame = new JFrame("FESI Editor " + newWindowCounter);
        frame.getContentPane().setLayout(new BorderLayout());
        final Editor editor = new Editor(this);
        editorWindows.addElement(editor);
        frame.getContentPane().add("Center", editor);
        frame.pack();
        frame.setSize(500, 600);
        frame.setVisible(true);
        // Support controlled closing
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                if (!editor.checkDirty()) {
                    frame.dispose();
                    SwingConsole.this.toFront();
                }
            }

            public void windowClosed(WindowEvent ev) {
                SwingConsole.this.editorWindowClosed(editor);
            }
        });
    }

    /**
     * Open a file based on file dialog
     */
    public void commandOpen() {
        FileDialog fd = new FileDialog(this, "File to open");
        fd.setVisible(true);
        String directoryName = fd.getDirectory();
        String fileName = fd.getFile();
        // System.out.println("D: " + directoryName + ", f: " + fileName);
        if (fileName != null && directoryName != null) {
            File directory = new File(directoryName);
            File file = new File(directory, fileName);
            if (file.exists()) {
                JFrame frame = new JFrame("FESI Editor"); // Will be replaced by
                                                          // file name during
                                                          // load
                Editor ed = new Editor(this);
                frame.getContentPane().setLayout(new BorderLayout());
                frame.getContentPane().add("Center", ed);
                frame.pack();
                frame.setSize(500, 600);
                frame.setVisible(true);
                ed.loadFile(file);
            }
        }
    }

    /**
     * Execute a string with a specified source.
     * 
     * @returns 0 if success, line of error otherwise, -1 if line unknown and in
     *          error
     */
    int executeString(String text, String source) {
        int status = itrp.executeString(text, source);
        theConsoleArea.atEnd(); // Make results visible
        if (status <= 0)
            toFront(); // show results
        return status;
    }

    /**
     * Send data to console input
     */
    synchronized public void send(String text) {
        // byte [] btext = text.getBytes();
        // pipedStream.write(btext, 0, btext.length);
        textAreaInputStream.send(text);
    }

    /**
     * Clear the console content
     */
    synchronized public void clear() {
        theConsoleArea.setText("");
        theConsoleArea.resetFirstInputLocation();
    }

    /**
     * Inform if editing is supported
     */
    public boolean supportsEditing() {
        return true;
    }

    /**
     * Create a new editing window for the specified file
     */
    public void createEditor(String fileName) {
        File file = new File(fileName);
        JFrame frame = new JFrame("FESI Editor"); // Will be replaced by file
                                                  // name during load
        Editor ed = new Editor(this);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add("Center", ed);
        frame.pack();
        frame.setSize(500, 600);
        frame.setVisible(true);
        if (file.exists()) {
            ed.loadFile(file);
        }
    }

    /**
     * Display the about information
     */
    public void displayAbout() {
        itrp.displayAboutText();
        toFront();
    }

    /**
     * Display the @help text information
     */
    public void displayHelpText() {
        itrp.displayHelpText();
        toFront();
    }

    /**
     * Display the help window
     */
    public void displayHelpWindow() {
        if (helpWindow == null) {
            String helpLocation = System.getProperty("FESI.help");
            if (helpLocation == null) {
                System.err
                        .println("[[Property FESI.help not defined, should be help url]]");
                return;
            }
            Cursor cursor = getCursor();
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            setCursor(waitCursor);
            helpWindow = new HelpWindow(helpLocation);
            setCursor(cursor);
        } else {
            helpWindow.setVisible(true);
            helpWindow.toFront();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * FilterInputStream that buffers input until newline occurs. Then
     * everything is passed along. If backspace (ch = 8) is received then the
     * last character in the buffer is removed.
     */
    public static class LineInputStream extends FilterInputStream {
        // OutputStream out;
        byte byteArray[];
        int arrayOffset;
        int arrayLength;

        public LineInputStream(InputStream in) {
            super(in);
        }

        // public LineInputStream(InputStream in, OutputStream out) {
        // super(in);
        // this.out = out;
        // }

        synchronized public int read() throws IOException {
            // If there are data in buffer the return the first character
            // in buffer.
            if (byteArray != null && arrayOffset < arrayLength)
                return byteArray[arrayOffset++];

            // if buffer is empty, fill buffer...
            byteArray = readLine();
            arrayOffset = 0;
            arrayLength = byteArray.length;

            // If there are data in buffer the return the first character
            // in buffer.
            if (byteArray != null && arrayOffset < arrayLength)
                return byteArray[arrayOffset++];
            else
                return -1;
        }

        synchronized public int read(byte bytes[], int offset, int length)
                throws IOException {
            if (byteArray != null && arrayOffset < arrayLength) {
                int available = available();
                if (length > available)
                    length = available;

                System.arraycopy(byteArray, arrayOffset, bytes, offset, length);
                arrayOffset += length;

                return length;
            }

            byteArray = readLine();
            arrayOffset = 0;
            arrayLength = byteArray.length;
            if (byteArray == null || arrayOffset >= arrayLength)
                return -1;

            int available = available();
            if (length > available)
                length = available;

            System.arraycopy(byteArray, arrayOffset, bytes, offset, length);
            arrayOffset += length;

            return length;
        }

        synchronized public int available() throws IOException {
            return arrayLength - arrayOffset + super.available();
        }

        synchronized public byte[] readLine() throws IOException {
            ByteArrayOutputStream bytesOut;
            byte bytes[];
            int ch;

            bytesOut = new ByteArrayOutputStream();

            boolean ready = false;
            while (!ready) {
                ch = this.in.read();

                if (ch == -1) {
                    // EOF
                    ready = true;

                } else if (ch == 8) {
                    // Backspace: Remove last character in buffer.
                    bytes = bytesOut.toByteArray();
                    bytesOut.reset();
                    int length = bytes.length - 1;
                    if (length > 0)
                        bytesOut.write(bytes, 0, length);

                } else if (ch == 21) {
                    // ^U: Remove all character in buffer.
                    bytesOut.reset();

                } else if (ch == 10) {
                    bytesOut.write(ch);
                    // NewLine: Return current buffer.
                    ready = true;

                } else {
                    // Other: Add to buffer.
                    bytesOut.write(ch);

                    // out.write(ch);
                    // out.flush();
                }
            } // while

            return bytesOut.toByteArray();
        }
    }

    /**
     * The final output stream that send the output to the associated TextArea.
     * The output is *appended* to the text in the TextArea, because it will
     * only be used like a console output.
     */
    public static class TextAreaOutputStream extends OutputStream {
        JConsoleArea theConsoleArea;
        String buffer;

        /**
         * Connect the stream to a TextArea.
         */
        public TextAreaOutputStream(JConsoleArea textArea) {
            buffer = "";
            theConsoleArea = textArea;
        }

        /**
         * Add the contents in the internal buffer to the TextArea and delete
         * the buffer.
         */
        synchronized public void flush() {
            theConsoleArea.append(buffer);
            buffer = "";
        }

        /**
         * Write to the internal buffer.
         */
        synchronized public void write(int b) {
            // if (b == 13) {
            // buffer += eol;
            // } else if (b != 10) { // ignore LF
            if (b < 0)
                b += 256;
            buffer += (char) b;
            // }
        }
    }

    public static class TextAreaInputStream extends PipedInputStream {
        OutputStream out;
        int nbrOfKeyTyped;

        public TextAreaInputStream(JConsoleArea newTextArea) {
            try {
                out = new PipedOutputStream(this);
                nbrOfKeyTyped = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Process the end of line (as received from paste) but no other
        private void send(char ch) {
            try {
                if (ch == 10) { // LF
                    // theConsoleArea.append(eol);
                    // out.write(13); out.write(10);
                    byte[] beol = eol.getBytes();
                    out.write(beol, 0, beol.length);
                    out.flush();
                    nbrOfKeyTyped = 0;
                } else if (ch >= 32 && ch < 256) {
                    // theConsoleArea.append(String.valueOf(ch));
                    out.write(ch);
                    nbrOfKeyTyped++;
                } else if (ch == 13) {
                    ; // ignore RETURN
                } else {
                    // theConsoleArea.append("?");
                    out.write('?');
                    nbrOfKeyTyped++;
                    // System.err.println("paste ch == '"+ch+"' ["+(int)ch+"]");
                }
            } catch (IOException e) {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        private void send(String s) {
            for (int i = 0; i < s.length(); i++) {
                // char ch = s.charAt(i);
                // System.err.println("send == '"+ch+"' ["+(int)ch+"]");
                send(s.charAt(i));
            }
        }

    }

    /*
     * // Small test program static public void main(String [] args) throws
     * Exception { Console c = new Console(new FESI.gui.InterpreterCommands()
     * {public void exit(){System.exit(0);}}, "Test",25,80); DataInputStream din
     * = new DataInputStream(System.in); String line; while ((line =
     * din.readLine()) != null) { System.out.println("'"+line+"'"); } }
     */
}
