// BasicIO.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implements basic input/out capabilities for console based (stdin/stdout)
 * applications.
 * <P>
 * The few functions allow minimal communication with the user (including
 * displaying error messages and prompting) in a way portable to windowing
 * environments.
 */
public class BasicIO extends AbstractBasicIO {

    // Implement the EcmaScript functions
    private static final long serialVersionUID = 8886886834315148679L;

    @Override
    protected void displayAlert(String message) {
        System.err.print("[[ALERT]] ");
        System.err.println(message);
    }

    protected String displayPrompt(String prompt, String defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "] ? ");
        System.out.flush();
        String response = null;
        try {
            response = (new BufferedReader(new InputStreamReader(System.in)))
            .readLine();
        } catch (IOException e) {
            // response = null;
        }
        if (response == null || response.equals(""))
            response = defaultValue;
        return response;
    }

    protected boolean displayConfirm(String prompt) {
        System.out.print(prompt + " [y/n] ? ");
        System.out.flush();
        String response = null;
        try {
            response = (new BufferedReader(new InputStreamReader(System.in)))
            .readLine();
        } catch (IOException e) {
            // response = null;
        }
        boolean result;
        if (response != null
                && response.trim().toLowerCase().startsWith("y")) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Create a new instance of the BasicIO extension
     */
    public BasicIO() {
        super();
    }


}
