/* === This file is part of SecurityCam ===
 *
 *   Copyright 2012, Timo Wischer
 *
 *   SecurityCam is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SecurityCam is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SecurityCam. If not, see <http://www.gnu.org/licenses/>.
 */
package de.wischer.timo.securityCam;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class ErrorHandler {
	private static Display display = null;
	
	public static void setMidlet(MIDlet midlet) {
		display = Display.getDisplay(midlet);
	}
	
	public static void doAlert(Exception e)
	{
		doAlert(e.toString());
	}
	
	public static void doAlert(String szText)
	{
		Alert alert = new Alert("Error");
		alert.addCommand( new Command("Back", Command.BACK, 1) );
		alert.setString(szText);
		alert.setTimeout(10000);
		display.setCurrent(alert);
	}
}
