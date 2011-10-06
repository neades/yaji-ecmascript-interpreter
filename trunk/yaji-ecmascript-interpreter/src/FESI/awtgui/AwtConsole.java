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

package FESI.awtgui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import FESI.Exceptions.ProgrammingError;
import FESI.gui.Console;

// Inspired by JConsole v0.9 of Henrik Bengtsson

public class AwtConsole extends Frame implements Console {

    private static final long serialVersionUID = -6107086842254399466L;

    private static String eol = System.getProperty("line.separator", "\n");

    /** @serial The text area used to represent the console text */
    private TextArea theTextArea;
    private transient InputStream consoleIn;
    private PrintStream consoleOut;
    private FESI.gui.InterpreterCommands itrp = null;

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
     * Create a new console interface - this is limited by the AWT usage of
     * TextEdit.
     * 
     * @param itrpParam
     *            The interpreter which will receive the exit command whel EOF
     *            is received.
     * @param title
     *            The window title string
     * @rows Starting number of rows
     * @columns Starring number of columnes
     */
    public AwtConsole(FESI.gui.InterpreterCommands itrpParam, String title,
            int rows, int columns) {
        super(title);

        itrp = itrpParam;

        theTextArea = new TextArea(rows, columns);
        theTextArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        theTextArea.setBackground(Color.lightGray);
        this.add(theTextArea);
        theTextArea.setVisible(true);
        this.pack();
        int scrwidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int scrheight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int conwidth = getSize().width;
        int conheight = getSize().height;

        setLocation((scrwidth - conwidth) / 2, (scrheight - conheight) / 2);

        consoleIn = new LineInputStream(new TextAreaInputStream(theTextArea));
        consoleOut = new PrintStream(new TextAreaOutputStream(theTextArea),
                true);

        // Nice attempt - but does not always work...
        System.setOut(consoleOut);
        System.setIn(consoleIn);
        System.setErr(consoleOut);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                itrp.exit();
                // System.exit(0);
            }
        });

        this.setVisible(true);
    }

    /**
     * Clear the console content
     */
    synchronized public void clear() {
        theTextArea.setText("");
    }

    /**
     * Inform if editing is supported
     */
    public boolean supportsEditing() {
        return false;
    }

    /**
     * Create a new editing window for the specified file
     */
    public void createEditor(String fileName) {
        throw new ProgrammingError(
                "Create editor called in non supported environment");
    }

    /**
     * Display the about information
     */
    public void displayAbout() {
        itrp.displayAboutText();
    }

    /**
     * Display the @help text information
     */
    public void displayHelpText() {
        itrp.displayHelpText();
    }

    /**
     * Display the help window
     */
    public void displayHelpWindow() {
        // ignored in AWT
    }

    static boolean initialized = false;
    static boolean needsAppend = false; // For a workaround against an ms bug

    static boolean needsAppend() {
        if (!initialized) {
            initialized = true;
            try {
                String tk = System.getProperty("awt.toolkit", "");
                needsAppend = (tk == null) || (tk.indexOf("com.ms") < 0);
            } catch (SecurityException ignore) {
                ignore.printStackTrace();
                System.err.println("Exception ignored");
            }
        }
        return needsAppend;
    }

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

        @Override
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
            return -1;
        }

        @Override
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

        @Override
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
        TextArea theTextArea;
        String buffer;

        /**
         * Connect the stream to a TextArea.
         */
        public TextAreaOutputStream(TextArea textArea) {
            buffer = "";
            theTextArea = textArea;
        }

        /**
         * Add the contents in the internal buffer to the TextArea and delete
         * the buffer.
         */
        @Override
        synchronized public void flush() {
            theTextArea.append(buffer);
            String text = theTextArea.getText();
            theTextArea.setCaretPosition(text.length());
            buffer = "";
        }

        /**
         * Write to the internal buffer.
         */
        @Override
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

    public class TextAreaInputStream extends PipedInputStream implements
            KeyListener {
        TextArea theTextArea;
        OutputStream out;
        int nbrOfKeyTyped;

        public TextAreaInputStream(TextArea newTextArea) {
            try {
                theTextArea = newTextArea;
                theTextArea.addKeyListener(this);
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
                    if (needsAppend())
                        theTextArea.append(eol);
                    // out.write(13); out.write(10);
                    byte[] beol = eol.getBytes();
                    out.write(beol, 0, beol.length);
                    out.flush();
                    nbrOfKeyTyped = 0;
                } else if (ch >= 32 && ch < 256) {
                    if (needsAppend())
                        theTextArea.append(String.valueOf(ch));
                    out.write(ch);
                    nbrOfKeyTyped++;
                } else if (ch != 13) {
                    theTextArea.append("?");
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

        synchronized public void keyTyped(KeyEvent keyEvent) {
            boolean consume = true;
            try {
                char ch = keyEvent.getKeyChar();
                // System.err.println("Keychar: " + int( ch ) + ": " +
                // keyEvent.toString());

                if (ch == 8) { // Backspace
                    if (nbrOfKeyTyped > 0) {
                        // String text = theTextArea.getText();
                        theTextArea.append(""); // Used for placing the cursor
                        // at the end.
                        // int caret = theTextArea.getCaretPosition();
                        // theTextArea.setText(text.substring(0, text.length() -
                        // 1));
                        // theTextArea.append(""); // Used for placing the
                        // cursor at the end.
                        // theTextArea.setCaretPosition(caret-1); // Used for
                        // placing the cursor at the end.
                        out.write(ch);
                        nbrOfKeyTyped--;
                        consume = false;
                    }
                } else if (ch == 13 || ch == 10) { // return

                    send((char) 10); // LF

                } else if (ch == 21) { // ^U
                    if (nbrOfKeyTyped > 0) {
                        String text = theTextArea.getText();
                        // int caret = theTextArea.getCaretPosition();
                        theTextArea.setText(text.substring(0, text.length()
                                - nbrOfKeyTyped));
                        theTextArea.append(""); // Used for placing the cursor
                        // at the end.
                        // theTextArea.setCaretPosition(caret
                        // +1000);//-nbrOfKeyTyped); // Used for placing the
                        // cursor at the end.
                        out.write(21);
                    }
                } else if (ch >= 32 && ch < 256 - 31) {

                    send(ch);

                } else {

                    if (ch == 3) { // ^c

                        consume = false;

                    } else if (ch == 26) { // ^z

                        itrp.exit();

                    } else if (ch == 22) { // ^v

                        Clipboard c = Toolkit.getDefaultToolkit()
                                .getSystemClipboard();
                        Transferable t = c.getContents(this);
                        String s = "";
                        try {
                            s = (String) t
                                    .getTransferData(DataFlavor.stringFlavor);
                            send(s);
                        } catch (UnsupportedFlavorException e) {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                        // System.err.println("ch == '"+ch+"' ["+(int)ch+"]");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (consume)
                    keyEvent.consume();
            }
        }

        public void keyPressed(KeyEvent e) {
            // ignored
        }

        public void keyReleased(KeyEvent e) {
            // ignored
        }
    }
    /*
     * static public void main(String [] args) throws Exception { Console c =
     * new Console(new FESI.gui.InterpreterCommands() {public void
     * exit(){System.exit(0);}}, "Test",25,80); DataInputStream din = new
     * DataInputStream(System.in); String line; while ((line = din.readLine())
     * != null) { System.out.println("'"+line+"'"); } }
     */
}
