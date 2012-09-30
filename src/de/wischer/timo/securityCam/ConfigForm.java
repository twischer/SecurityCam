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

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class ConfigForm extends Form {
	private final String RMS_SETTINGS = "Settings";
	private final int RMS_DEST_DIR_ID = 1;
	private final int RMS_SNAPSHOT_DELAY_ID = 2;
	private final int RMS_LENGTH = 2;

	private final MIDlet midlet;
	private final Command cmdStart = new Command("Start", Command.SCREEN, 1);
	private final Command cmdExit = new Command("Exit", Command.SCREEN, 1);

	private final TextField destDirTextField = new TextField(
			"Destination directory:", "file:///4:/SecurityCam", 30,
			TextField.URL);
	private final String[] listElements = new String[] { "0", "1", "2", "3",
			"4", "5", "10", "15", "20" };
	private final ChoiceGroup snapshotDelayChoiceGroup = new ChoiceGroup(
			"Snapshot delay (sec):", ChoiceGroup.EXCLUSIVE, listElements, null);

	public ConfigForm(final MIDlet midlet) {
		super("Configuration");

		this.midlet = midlet;

		loadSettings();

		addCommand(cmdStart);
		addCommand(cmdExit);
		setCommandListener(new ConfigCommandListener());

		append(destDirTextField);
		append(snapshotDelayChoiceGroup);

		Display.getDisplay(midlet).setCurrent(this);
	}

	private final int getSelectedSnapshotDelay() {
		final int sel = snapshotDelayChoiceGroup.getSelectedIndex();
		final String snapshotDelayString = snapshotDelayChoiceGroup
				.getString(sel);
		final int snapshotDelay = Integer.parseInt(snapshotDelayString);

		return snapshotDelay;
	}

	private void loadSettings() {
		try {
			RecordStore rsData = RecordStore
					.openRecordStore(RMS_SETTINGS, true);

			if (rsData.getNumRecords() == RMS_LENGTH) {
				final byte[] destDir = rsData.getRecord(RMS_DEST_DIR_ID);
				String destDirString = "";
				if (destDir != null)
					destDirString = new String(destDir);
				destDirTextField.setString(destDirString);

				final byte[] snapshotDelay = rsData
						.getRecord(RMS_SNAPSHOT_DELAY_ID);
				int snapshotDelayInt = 0;
				if (snapshotDelay != null)
					snapshotDelayInt = (int) snapshotDelay[0];
				snapshotDelayChoiceGroup.setSelectedIndex(snapshotDelayInt,
						true);
			} else {
				rsData.closeRecordStore();
				RecordStore.deleteRecordStore(RMS_SETTINGS);
				rsData = RecordStore.openRecordStore(RMS_SETTINGS, true);

				for (int i = 0; i < RMS_LENGTH; i++)
					rsData.addRecord(null, 0, 0);
			}

			rsData.closeRecordStore();
		} catch (RecordStoreFullException e) {
			ErrorHandler.doAlert(e);
		} catch (RecordStoreNotFoundException e) {
			ErrorHandler.doAlert(e);
		} catch (RecordStoreException e) {
			ErrorHandler.doAlert(e);
		}
	}

	private void saveSettings() {
		try {
			final RecordStore rsData = RecordStore.openRecordStore(
					RMS_SETTINGS, true);

			final String destDir = destDirTextField.getString();
			rsData.setRecord(RMS_DEST_DIR_ID, destDir.getBytes(), 0,
					destDir.length());

			final byte[] snapshotDelay = { (byte) snapshotDelayChoiceGroup
					.getSelectedIndex() };
			rsData.setRecord(RMS_SNAPSHOT_DELAY_ID, snapshotDelay, 0,
					snapshotDelay.length);

			rsData.closeRecordStore();
		} catch (RecordStoreFullException e) {
			ErrorHandler.doAlert(e);
		} catch (RecordStoreNotFoundException e) {
			ErrorHandler.doAlert(e);
		} catch (RecordStoreException e) {
			ErrorHandler.doAlert(e);
		}
	}

	private class ConfigCommandListener implements CommandListener {

		public void commandAction(Command cmd, Displayable s) {
			if (cmd.equals(cmdStart)) {
				saveSettings();
				new CameraForm(midlet, destDirTextField.getString(),
						getSelectedSnapshotDelay());
			} else if (cmd.equals(cmdExit)) {
				midlet.notifyDestroyed();
			}
		}

	}

}
