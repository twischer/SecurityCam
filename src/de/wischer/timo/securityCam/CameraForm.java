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

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.midlet.MIDlet;


public class CameraForm extends Form {
	private final MIDlet midlet;
	private final Command cmdStop = new Command("Stop", Command.SCREEN, 1);
	private final Command cmdExit = new Command("Exit", Command.SCREEN, 3);
	private Player player;
	private VideoControl videoControl;
	private final CaptureThread captureThread;

	
	public CameraForm(final MIDlet midlet) {
		super("Security Cam");

		this.midlet = midlet;

		try {
			player = Manager.createPlayer("capture://video");
			player.realize();
			videoControl = (VideoControl) player.getControl("VideoControl");
			player.start();
		} catch (IOException e) {
			ErrorHandler.doAlert(e);
		} catch (MediaException e) {
			ErrorHandler.doAlert(e);
		}
		
		addCommand(cmdStop);
		addCommand(cmdExit);
		setCommandListener(new CameraCommandListener());
		
		
		Item videoItem = (Item)videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
		try {
			videoControl.setDisplayFullScreen(true);
		} catch (MediaException e) {
			ErrorHandler.doAlert(e);
		}
		append(videoItem);
		
		Display.getDisplay(midlet).setCurrent(this);
		
		
		captureThread = new CaptureThread(videoControl, "file:///4:/SecurityCam", 5);
	}
	
	private void close(){
		try {
			captureThread.stop();
		} catch (InterruptedException e) {
			ErrorHandler.doAlert(e);
		}
		player.close();
	}

	
	private class CameraCommandListener implements CommandListener {

		public void commandAction(Command cmd, Displayable s) {
			if (cmd.equals(cmdStop)) {
				close();
				new ConfigForm(midlet);
			} else if (cmd.equals(cmdExit)) {
				close();
				midlet.notifyDestroyed();
			}
		}

	}
}
