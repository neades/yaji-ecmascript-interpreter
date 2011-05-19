// Editor.java
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
import java.awt.Container;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import FESI.Exceptions.ProgrammingError;

/**
 * Simplistic editor for FESI
 */
public class Editor extends JPanel {


    private JTextComponent editor;
    private JMenuBar menubar;
    
    private File currentFile = null;
    private boolean dirty = false;
    private String defaultTitle = null;

    protected FileDialog fileDialog;
    private SwingConsole mainConsole;
  
    private DocumentListener theDocumentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {dirty=true; refreshTitle();}
      public void insertUpdate(DocumentEvent e) {dirty=true; refreshTitle();}
      public void removeUpdate(DocumentEvent e) {dirty=true; refreshTitle();}
    };
    
    /**
     * Create an editor for a new file
     */
    Editor(SwingConsole console) {
        super(true);
        mainConsole = console;
        init();
    }
    
    /**
     * Initialize the editor window
     */
    void init() {
      
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
    
        // create the embedded JTextComponent
        editor = new JTextArea();
        editor.setFont(new Font("Monospaced", Font.PLAIN, 12));
        Document doc = editor.getDocument();
        doc.addDocumentListener(theDocumentListener);
    
        JScrollPane scroller = new JScrollPane();
        JViewport port = scroller.getViewport();
        port.add(editor);
        port.setBackingStoreEnabled(false);
    
        menubar = createMenubar();
        add("North", menubar);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());    
        panel.add("Center", scroller);
        add("Center", panel);
              
    }


    /**
     * Check if the current content must be saved - return true
     * if the operation must be aborted
     */
    boolean checkDirty() {
      if (dirty) {
         int result = JOptionPane.showConfirmDialog(this,   
               "Save changes in " + getWindowBaseTitle()  + " ?", 
               "FESI Editor",
               JOptionPane.YES_NO_CANCEL_OPTION,
               JOptionPane.WARNING_MESSAGE,
               null);
          if (result == JOptionPane.CANCEL_OPTION) {
              return true;
          } else if (result == JOptionPane.YES_OPTION) {
              commandSave();
              return false;
          } else {
              return false; // assume no
          }
      } else {
          return false; // We can continue
      }
    }

    /**
     * Find the hosting frame, for the file-chooser dialog.
     */
    protected Frame getFrame() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
            return (Frame) p;
            }
        }
        return null;
    }

 

    protected JMenuBar getMenubar() {
	    return menubar;
    }

    /**
     * Create the menubar for the app.  By default this pulls the
     * definition of the menu from the associated resource file. 
     */
    protected JMenuBar createMenubar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
  
        // Command new
        JMenuItem newItem = new JMenuItem("New  Ctrl+N");
        newItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandNew();
                }
            });
        fileMenu.add(newItem);
        // Shortcut ^N
        registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandNew();
                }
            },
              KeyStroke.getKeyStroke('N', Event.CTRL_MASK),
              JComponent.WHEN_IN_FOCUSED_WINDOW);

        // open a file for editing - use AWT file dialog as the Swing one is still not defined !
        JMenuItem openItem = new JMenuItem("Open... Ctrl+O");
        openItem.setMnemonic('O');
        openItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandOpen();
                }
            });
        fileMenu.add(openItem);
        // Shortcut ^O
        registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandOpen();
                }
            },
              KeyStroke.getKeyStroke('O', Event.CTRL_MASK),
              JComponent.WHEN_IN_FOCUSED_WINDOW);
    
        // Close
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setMnemonic('C');
        closeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandClose();
                }
            });
        fileMenu.add(closeItem);
        
        // Save 
        JMenuItem saveItem = new JMenuItem("Save  Ctrl+S");
        saveItem.setMnemonic('S');
        saveItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandSave();
                }
            });
        fileMenu.add(saveItem);
        // Shortcut ^S
        registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandSave();
                }
            },
              KeyStroke.getKeyStroke('S', Event.CTRL_MASK),
              JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        // Save as
        JMenuItem saveAsItem = new JMenuItem("Save as..");
        saveAsItem.setMnemonic('a');
        saveAsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandSaveAs();
                }
            });
        fileMenu.add(saveAsItem);
        
        menuBar.add(fileMenu);

        // EDIT menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
  
        // Cut
        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setMnemonic('t');
        cutItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.editor.cut();
                }
            });
        editMenu.add(cutItem);

  
        // Copy
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setMnemonic('C');
        copyItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.editor.copy();
                }
            });
        editMenu.add(copyItem);
  
        // Paste
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setMnemonic('P');
        pasteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.editor.paste();
                    
                }
            });
        editMenu.add(pasteItem);
    	
    	    menuBar.add(editMenu);

    		 // RUN menu
        JMenu runMenu = new JMenu("Run");
        runMenu.setMnemonic('R');
  
        // Command execute
        JMenuItem executeItem = new JMenuItem("Execute F4");
        executeItem.setMnemonic('E');
        executeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandExecute();
                }
            });
        runMenu.add(executeItem);
        // Shortcut F4
        registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    Editor.this.commandExecute();
                }
            },
              KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
              WHEN_IN_FOCUSED_WINDOW);

        // Add menu 
        menuBar.add(runMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        // Help content
        JMenuItem contentItem = new JMenuItem("Content F1");
        contentItem.setMnemonic('C');
        contentItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    mainConsole.displayHelpWindow();
                }
            });
        helpMenu.add(contentItem);
        // Shortcut F1
        registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    mainConsole.displayHelpWindow();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    
        // Help commands
        JMenuItem commandsItem = new JMenuItem("Commands");
        commandsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    mainConsole.displayHelpText();
                }
            });
        helpMenu.add(commandsItem);
    
        // About
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent action) {
                    mainConsole.displayAbout();
                }
            });
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
    
        
          // Add menu to menu bar
        menuBar.add(helpMenu);
        
        return menuBar;
    }

    // Get the title for dialog box
    public String getWindowBaseTitle() {
        Frame frame = getFrame();
        if (defaultTitle == null) {
          defaultTitle = frame.getTitle();
        }
        if (defaultTitle == null) {
          defaultTitle = "FESI Editor"; // defensive programming
        }
        String title = (currentFile == null ? defaultTitle : currentFile.getPath());
        return title;
    }

    // Get the title for list in menu
    public String getWindowTitle() {
        String title = getWindowBaseTitle() + 
                        (dirty ? " *" : "");
        return title;
    }

    // Refresh the title, using a standard title if no file name known and * indicator
    private void refreshTitle() {
        Frame frame = getFrame();
        frame.setTitle(getWindowTitle());
    }

    // Set a title for a newly loaded file (or after save as)
    private void setTitle(File file) {
        currentFile = file;
        dirty = false;
        refreshTitle();
    }
  
    /**
     * Discard the current content and load a file
     * @param file File to load
     */
    public void loadFile(File file) {
        Document doc = new PlainDocument();
        editor.setDocument(doc);
        setTitle(file);
        Thread loader = new FileLoader(file, editor.getDocument());
        loader.start();
    }
    
    /**
     * Process close command, checking if the buffer must be saved first
     */
    void commandClose() {
        if (checkDirty()) return;
        Frame frame = getFrame();
        frame.dispose(); // Will mark window clsoed by the closedWindowEvent
    }

    
    /**
     * Process the Open command, check if the buffer must be saved,
     * then ask for a file and load its content
     */
    void commandOpen() {
      if (checkDirty()) return;
      
	    Frame frame = getFrame();
	    if (fileDialog == null) {
	    	fileDialog = new FileDialog(frame);
	    }
	    fileDialog.setMode(FileDialog.LOAD);
	    fileDialog.show();

	    String file = fileDialog.getFile();
	    if (file == null) {
    		  return;
	    }
	    String directory = fileDialog.getDirectory();
	    File f = new File(directory, file);
	    if (f.exists()) {
            loadFile(f);
	    }
    }
    
    /**
     * save the buffer in the specified file
     */
    public void saveFile(File file) {
        setTitle(file);
        try{
            Writer out = new FileWriter(file);
            Document doc = editor.getDocument();
            String text = null;
            try {
                text = doc.getText(0, doc.getLength());
            } catch (BadLocationException e) {
                throw new ProgrammingError("Unexpected exception: " + e);
            }
            out.write(text);
            out.close();
            dirty = false;
            refreshTitle();
        } catch (IOException e) {
            System.out.println("IO Error " + e);
        }
    }
   
    /**
     * Ask a new file name in which to save the buffer content
     * then save the buffer.
     */
    void commandSaveAs() {
        Frame frame = getFrame();
        if (fileDialog == null) {
            fileDialog = new FileDialog(frame);
        }
        fileDialog.setMode(FileDialog.SAVE);
        fileDialog.show();

        String file = fileDialog.getFile();
        if (file == null) {
            return;
        }
        String directory = fileDialog.getDirectory();
        File f = new File(directory, file);
        saveFile(f);
    }
   
    /**
     * Save the buffer, asking for a file name if none was specified
     */
    void commandSave() {
        if (currentFile == null) {
            commandSaveAs();
        } else {
            saveFile(currentFile);
        }
    }

    /**
     * Execute the content of the buffer
     */
    void commandExecute() {
        Document doc = editor.getDocument();
        String text = null;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            throw new ProgrammingError("Unexpected exception: " + e);
        }
        Frame frame = getFrame();
        int errorLine = mainConsole.executeString(text, frame.getTitle());  
        // System.out.println("Error @ " + errorLine);
        if (errorLine>0) {
            int length = text.length();
            char cs[] = new char[length];
            text.getChars(0, length, cs, 0);
            
            errorLine --;
            int found = -1;
            for (int i=0; i<length; i++) {
                if (errorLine==0) {found = i; break;}
                if (cs[i]=='\n') errorLine--;
            }
            if (found>=0) {
                editor.setCaretPosition(found);
            }
        }
    }
    
    /**
     * Save the buffer if required, then clear the content to start a new document
     */
    void commandNew() {
      if (checkDirty()) return;
      
      Document doc = new PlainDocument();
      doc.addDocumentListener(theDocumentListener);
	    editor.setDocument(doc);
	    revalidate();
    }

   /**
     * Thread to load a file into the text storage model
     * (From the Notepad example from Sun - not really usefull here)
     */
    class FileLoader extends Thread {

        FileLoader(File f, Document doc) {
            setPriority(4);
            this.f = f;
            this.doc = doc;
        }

        public void run() {
            try {
                /*
                JProgressBar progress = new JProgressBar();
                progress.setMinimum(0);
                progress.setMaximum((int) f.length());
                status.add(progress);
                status.revalidate();
                */
        
                // try to start reading
                Reader in = new FileReader(f);
                char[] buff = new char[4096];
                int nch;
                while ((nch = in.read(buff, 0, buff.length)) != -1) {
                    doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
                    // progress.setValue(progress.getValue() + nch);
                }
        
                // we are done... get rid of progressbar
                //status.removeAll();
                //status.revalidate();
              
                // Listen to any change
                doc.addDocumentListener(theDocumentListener);

            }
            catch (IOException e) {
                System.err.println(e.toString());
            }
            catch (BadLocationException e) {
                System.err.println(e.getMessage());
            }
        }
    
        Document doc;
        File f;
    }

    

}