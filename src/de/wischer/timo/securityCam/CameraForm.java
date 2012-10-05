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
	private final DelayThread captureThread;
	private final DelayThread deleteThread;

	
	public CameraForm(final MIDlet midlet, final String destDir, final int snapshotDelay, final int minFreeSpaceInMiByte) {
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
		
		
		// delay between checking the file system for free space is 20 times more than the snapshot delay
		final int deleteDelay = ( (snapshotDelay > 0) ? snapshotDelay : 1 ) * 20;
		deleteThread = new DeleteThread(destDir, deleteDelay, minFreeSpaceInMiByte);
		
		captureThread = new CaptureThread(videoControl, destDir, snapshotDelay);
	}
	
	private void close(){
		try {
			captureThread.stop();
			deleteThread.stop();
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
