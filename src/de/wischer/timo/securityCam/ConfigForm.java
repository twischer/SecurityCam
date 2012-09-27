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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;


public class ConfigForm extends Form{
	private final MIDlet midlet;
	private final Command cmdStart = new Command("Start", Command.SCREEN, 1);
	private final Command cmdExit = new Command("Exit", Command.SCREEN, 2);
	
	
	public ConfigForm(final MIDlet midlet){
		super("Configuration");
		
		this.midlet = midlet;
		
		addCommand(cmdStart);
		addCommand(cmdExit);
		setCommandListener(new ConfigCommandListener());
		
		Display.getDisplay(midlet).setCurrent(this);
	}
	
	
	private class ConfigCommandListener implements CommandListener{

		public void commandAction(Command cmd, Displayable s) {
			if (cmd.equals(cmdStart)) {
				new CameraForm(midlet);
			} else if (cmd.equals(cmdExit)) {
				midlet.notifyDestroyed();
			}
		}
		
	}

}
