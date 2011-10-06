// JConsoleArea.java
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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;

import FESI.Exceptions.ProgrammingError;

/**
 * Implements a console with interactive input/output (in collaboration with
 * Console). The stream I/O of Console should probably be moved here.
 */
public class JConsoleArea extends JTextArea {

    private static final long serialVersionUID = 8030283053424789256L;

    // Keep tracks of first character entered since the last time a return
    // sent the data to the interpreter. The new data from this point will be
    // sent on the next return.
    private int firstInputLocation = -1; // -1 means no data typed

    // Keep track of last output location, to circumvent a bug in jdk1.2
    private int lastOutputLocation = -1;

    private static Keymap fesiKeymap = null; // Keymap for ^C,^V,^X, retur
    protected ExtendedConsole mainConsole; // The calling console

    /**
     * Create a new console with the specified number of rows and columns
     * 
     * @param mainComsole
     *            The calling console program to send back data to
     * @param rows
     *            The number of rows
     * @param columns
     *            the number of columns
     */
    JConsoleArea(ExtendedConsole mainConsole, int rows, int columns) {
        super(rows, columns);
        this.mainConsole = mainConsole;
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        if (fesiKeymap == null) {
            Keymap defaultKeymap = JTextComponent
                    .getKeymap(JTextComponent.DEFAULT_KEYMAP);
            if (defaultKeymap == null) {
                throw new ProgrammingError("Could not find default keymap");
            }
            fesiKeymap = JTextComponent.addKeymap("FESIKeyMap", defaultKeymap);
            fesiKeymap.setDefaultAction(new DefaultKeyTypedAction(this));

            fesiKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, 0), new ReturnKeyTypedAction(this));
            fesiKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(
                    KeyEvent.VK_C, InputEvent.CTRL_MASK), new CopyAction());
            fesiKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(
                    KeyEvent.VK_X, InputEvent.CTRL_MASK), new CutAction());
            fesiKeymap
                    .addActionForKeyStroke(KeyStroke.getKeyStroke(
                            KeyEvent.VK_V, InputEvent.CTRL_MASK),
                            new PasteAction(this));

        }

        setKeymap(fesiKeymap);
    }

    /**
     * Keep track of first input location (only!)
     * 
     * @param the
     *            current input location
     */
    protected void setFirstInputLocation(int loc) {
        if (firstInputLocation == -1)
            firstInputLocation = loc;
    }

    /**
     * Reset the first input location to none
     */
    protected void resetFirstInputLocation() {
        firstInputLocation = -1;
    }

    /**
     * Return the first input location
     * 
     * @return location or -1 if none defined
     */
    protected int getFirstInputLocation() {
        if (firstInputLocation == -1 && lastOutputLocation != -1) {
            // May be first input location si not set as JDK 1.2
            // do not pass the default event
            return lastOutputLocation;
        }
        return firstInputLocation;
    }

    /**
     * Move caret at end (for example after some code was executed)
     */
    protected void atEnd() {
        Document doc = getDocument();
        int dot = doc.getLength();
        setCaretPosition(dot);
    }

    /**
     * Override the standard PASTE command with a PASTE AT END
     */
    @Override
    public void paste() {

        // Move at end
        Document doc = getDocument();
        int dot = doc.getLength();
        setCaretPosition(dot);

        // Save location if needed
        setFirstInputLocation(dot);
        // Paste all
        super.paste();
    }

    /**
     * Trape append (WHICH MUST BE THE ONLY WAY TO ADD TEXT) to keep track of
     * the last output character location.
     */
    @Override
    public void append(String str) {
        super.append(str);
        Document doc = getDocument();
        lastOutputLocation = doc.getLength();
        setCaretPosition(lastOutputLocation);
    }

    /**
     * The action that is executed by default if a <em>key typed event</em> is
     * received and there is no keymap entry. There is a variation across
     * different VM's in what gets sent as a <em>key typed</em> event, and this
     * action tries to filter out the undesired events. This filters the control
     * characters and those with the ALT modifier.
     * <p>
     * The character is added at the end of the buffer and the first input
     * location is updated if needed.
     * <p>
     * If the event doesn't get filtered, it will try to insert content into the
     * text editor. The content is fetched from the command string of the
     * ActionEvent. The text entry is done through the
     * <code>replaceSelection</code> method on the target text component. This
     * is the action that will be fired for most text entry tasks.
     * <p>
     * Warning: serialized objects of this class will not be compatible with
     * future swing releases. The current serialization support is appropriate
     * for short term storage or RMI between Swing1.0 applications. It will not
     * be possible to load serialized Swing1.0 objects with future releases of
     * Swing. The JDK1.2 release of Swing will be the compatibility baseline for
     * the serialized form of Swing objects.
     * 
     * @see DefaultEditorKit#defaultKeyTypedAction
     * @see DefaultEditorKit#getActions
     * @see Keymap#setDefaultAction
     * @see Keymap#getDefaultAction
     */
    public static class DefaultKeyTypedAction extends TextAction {

        private static final long serialVersionUID = -3262326214394840937L;
        JConsoleArea console;

        /**
         * Creates this object with the appropriate identifier.
         */
        public DefaultKeyTypedAction(JConsoleArea console) {
            super(DefaultEditorKit.defaultKeyTypedAction);
            this.console = console;
        }

        /**
         * The operation to perform when this action is triggered. Check that
         * the character is valid, and set the first typing location if needed.
         * In Java 1.1 this is called for all key strokes. In 1.2 it is not
         * called for the printable keystrokes.
         * 
         * @param e
         *            the action event
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);

            if ((target != null) && (e != null)) {

                String content = e.getActionCommand();
                int mod = e.getModifiers();
                if ((content != null) && (content.length() > 0)
                        && ((mod & ActionEvent.ALT_MASK) == 0)) {
                    char c = content.charAt(0);
                    // if ((c >= 0x20) && (c != 0x7F)) { Old
                    if (!Character.isISOControl(c)) {
                        // Printable character - move at end
                        Document doc = target.getDocument();
                        int dot = doc.getLength();
                        target.setCaretPosition(dot);
                        // Save location of first character typed if needed
                        console.setFirstInputLocation(dot);
                        // Insert content
                        target.replaceSelection(content);
                    }
                }
            }
        }
    }

    /**
     * Action for RETURN - send text to console.
     * <P>
     * The text from the first input location to the end (where the RETURN was
     * typed) is send to the caller. The first input location is reset to none.
     */
    public static class ReturnKeyTypedAction extends TextAction {

        private static final long serialVersionUID = 1626509977773851535L;
        JConsoleArea console;

        /**
         * Creates this object with the appropriate identifier.
         */
        public ReturnKeyTypedAction(JConsoleArea console) {
            super("return");
            this.console = console;
        }

        /**
         * The operation to perform when this action is triggered.
         * 
         * @param e
         *            the action event
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);

            if ((target != null) && (e != null)) {

                // Set caret at end
                Document doc = target.getDocument();
                int dot = doc.getLength();
                target.setCaretPosition(dot);

                // Insert return
                target.replaceSelection("\n");

                // Get data
                String inputText = null;
                int start = console.getFirstInputLocation();
                int length = dot - start + 1;
                // System.err.println("\nRET star="+start+", l="+length); //
                // *************
                if (start != -1 && length > 0) {
                    try {
                        inputText = doc.getText(start, length);
                    } catch (BadLocationException ex) {
                        throw new ProgrammingError("Unexpected exception: "
                                + ex.toString());
                    }

                    console.mainConsole.send(inputText);

                    // Mark data sent
                    console.resetFirstInputLocation();
                }

            }
        }
    }

    /**
     * Cuts the selected region and place its contents into the system
     * clipboard.
     * <p>
     * Warning: serialized objects of this class will not be compatible with
     * future swing releases. The current serialization support is appropriate
     * for short term storage or RMI between Swing1.0 applications. It will not
     * be possible to load serialized Swing1.0 objects with future releases of
     * Swing. The JDK1.2 release of Swing will be the compatibility baseline for
     * the serialized form of Swing objects.
     * 
     * @see DefaultEditorKit#cutAction
     * @see DefaultEditorKit#getActions
     */
    public static class CutAction extends TextAction {

        private static final long serialVersionUID = 4026524064748078956L;

        /** Create this object with the appropriate identifier. */
        public CutAction() {
            super(DefaultEditorKit.cutAction);
        }

        /**
         * The operation to perform when this action is triggered.
         * 
         * @param e
         *            the action event
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.cut();
            }
        }
    }

    /**
     * Copies the selected region and place its contents into the system
     * clipboard.
     * <p>
     * Warning: serialized objects of this class will not be compatible with
     * future swing releases. The current serialization support is appropriate
     * for short term storage or RMI between Swing1.0 applications. It will not
     * be possible to load serialized Swing1.0 objects with future releases of
     * Swing. The JDK1.2 release of Swing will be the compatibility baseline for
     * the serialized form of Swing objects.
     * 
     * @see DefaultEditorKit#copyAction
     * @see DefaultEditorKit#getActions
     */
    public static class CopyAction extends TextAction {

        private static final long serialVersionUID = -8227889870650692398L;

        /** Create this object with the appropriate identifier. */
        public CopyAction() {
            super(DefaultEditorKit.copyAction);
        }

        /**
         * The operation to perform when this action is triggered.
         * 
         * @param e
         *            the action event
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.copy();
            }
        }
    }

    /**
     * Pastes the contents of the system clipboard at the end of the text area.
     * Mark the first input location if needed.
     * <p>
     * Warning: serialized objects of this class will not be compatible with
     * future swing releases. The current serialization support is appropriate
     * for short term storage or RMI between Swing1.0 applications. It will not
     * be possible to load serialized Swing1.0 objects with future releases of
     * Swing. The JDK1.2 release of Swing will be the compatibility baseline for
     * the serialized form of Swing objects.
     * 
     * @see DefaultEditorKit#pasteAction
     * @see DefaultEditorKit#getActions
     */
    public static class PasteAction extends TextAction {

        private static final long serialVersionUID = -5394052819455024487L;
        JConsoleArea console;

        /** Create this object with the appropriate identifier. */
        public PasteAction(JConsoleArea console) {
            super(DefaultEditorKit.pasteAction);
            this.console = console;
        }

        /**
         * The operation to perform when this action is triggered.
         * 
         * @param e
         *            the action event
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.paste(); // overloaded to PASTE AT END
            }
        }
    }

}