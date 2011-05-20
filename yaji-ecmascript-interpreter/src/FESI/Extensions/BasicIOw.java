// BasicIOw.java
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

package FESI.Extensions;

import FESI.awtgui.AwtConfirmationBox;
import FESI.awtgui.AwtMessageBox;
import FESI.awtgui.AwtPromptBox;
import FESI.gui.ConfirmationBox;
import FESI.gui.MessageBox;
import FESI.gui.PromptBox;

/**
 * Swing based basic IO for FESI - see BasicIO
 */
public class BasicIOw extends AbstractBasicIO {
    private static final long serialVersionUID = -7708955408111357166L;


    public BasicIOw() {
        super();
    }

    protected void displayAlert(String message) {
        MessageBox mb = new AwtMessageBox("EcmaScript Alert", message);
        mb.waitOK();
    }

    protected String displayPrompt(String prompt, String defaultResponse) {
        PromptBox pb = new AwtPromptBox("EcmaScript promt", prompt,
                defaultResponse);
        String response = pb.waitResponse();
        return response;
    }

    protected boolean displayConfirm(String message) {
        ConfirmationBox mb = new AwtConfirmationBox("EcmaScript Confirm",
                message);
        boolean response = mb.waitYesOrNo();
        return response;
    }
}
