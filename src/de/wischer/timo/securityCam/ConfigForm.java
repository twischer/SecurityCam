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
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

public class ConfigForm extends Form {
    private static final String RMS_SETTINGS = "Settings";
    private static final int RMS_DEST_DIR_ID = 1;
    private static final int RMS_SNAPSHOT_DELAY_ID = 2;
    private static final int RMS_MIN_FREE_SPACE_ID = 3;
    private static final int RMS_LENGTH = 3;

    private final MIDlet midlet;
    private final Command cmdStart = new Command("Start", Command.SCREEN, 1);
    private final Command cmdExit = new Command("Exit", Command.SCREEN, 1);

    private final TextField destDirTextField = new TextField("Destination directory:", "file:///4:/SecurityCam", 30, TextField.URL);
    private final String[] listElements = new String[] { "0", "1", "2", "3", "4", "5", "10", "15", "20" };
    private final ChoiceGroup snapshotDelayChoiceGroup = new ChoiceGroup("Snapshot delay (sec):", ChoiceGroup.EXCLUSIVE, listElements, null);
    private final TextField minFreeSpaceTextField = new TextField("Min free space (MiByte):", "10", 3, TextField.NUMERIC);

    public ConfigForm(final MIDlet midlet) {
	super("Configuration");

	this.midlet = midlet;

	loadSettings();

	addCommand(cmdStart);
	addCommand(cmdExit);
	setCommandListener(new ConfigCommandListener());

	append(destDirTextField);
	append(snapshotDelayChoiceGroup);
	append(minFreeSpaceTextField);
	
	// Print debug info of last crash
	try {
	    RecordStore rsData = RecordStore.openRecordStore("DEBUG", true);
	    if (rsData.getNumRecords() == 1) {
		append( getSettingAsString(rsData, 1) );
	    } else {
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
	
	
	Display.getDisplay(midlet).setCurrent(this);
    }

    private final int getSelectedSnapshotDelay() {
	final int sel = snapshotDelayChoiceGroup.getSelectedIndex();
	final String snapshotDelayString = snapshotDelayChoiceGroup.getString(sel);
	final int snapshotDelay = Integer.parseInt(snapshotDelayString);

	return snapshotDelay;
    }

    private void loadSettings() {
	try {
	    RecordStore rsData = RecordStore.openRecordStore(RMS_SETTINGS, true);

	    if (rsData.getNumRecords() == RMS_LENGTH) {
		destDirTextField.setString(getSettingAsString(rsData, RMS_DEST_DIR_ID));

		final byte[] snapshotDelay = rsData.getRecord(RMS_SNAPSHOT_DELAY_ID);
		int snapshotDelayInt = 0;
		if (snapshotDelay != null)
		    snapshotDelayInt = (int) snapshotDelay[0];
		snapshotDelayChoiceGroup.setSelectedIndex(snapshotDelayInt, true);
		
		minFreeSpaceTextField.setString(getSettingAsString(rsData, RMS_MIN_FREE_SPACE_ID));
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

    private String getSettingAsString(final RecordStore rsData, final int id) throws RecordStoreNotOpenException, InvalidRecordIDException,
	    RecordStoreException {
	final byte[] setting = rsData.getRecord(id);
	String settingString = "";
	if (setting != null)
	    settingString = new String(setting);

	return settingString;
    }

    private void saveSettings() {
	try {
	    final RecordStore rsData = RecordStore.openRecordStore(RMS_SETTINGS, true);

	    setSetting(rsData, RMS_DEST_DIR_ID, destDirTextField.getString() );

	    final byte[] snapshotDelay = { (byte) snapshotDelayChoiceGroup.getSelectedIndex() };
	    rsData.setRecord(RMS_SNAPSHOT_DELAY_ID, snapshotDelay, 0, snapshotDelay.length);

	    setSetting(rsData, RMS_MIN_FREE_SPACE_ID, minFreeSpaceTextField.getString() );
	    
	    rsData.closeRecordStore();
	} catch (RecordStoreFullException e) {
	    ErrorHandler.doAlert(e);
	} catch (RecordStoreNotFoundException e) {
	    ErrorHandler.doAlert(e);
	} catch (RecordStoreException e) {
	    ErrorHandler.doAlert(e);
	}
    }
    
    private void setSetting(final RecordStore rsData, final int id, final String setting) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreFullException, RecordStoreException {
	rsData.setRecord(id, setting.getBytes(), 0, setting.length());
    }

    private class ConfigCommandListener implements CommandListener {

	public void commandAction(Command cmd, Displayable s) {
	    if (cmd.equals(cmdStart)) {
		saveSettings();
		
		final int minFreeSpaceInMiByte = Integer.parseInt(minFreeSpaceTextField.getString());
		// TODO
		new CameraForm(midlet, destDirTextField.getString(), getSelectedSnapshotDelay(), true, minFreeSpaceInMiByte);
	    } else if (cmd.equals(cmdExit)) {
		midlet.notifyDestroyed();
	    }
	}

    }

}
