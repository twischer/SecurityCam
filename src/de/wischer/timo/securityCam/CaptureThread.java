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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.VideoControl;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class CaptureThread extends DelayThread {
    private final VideoControl videoControl;
    private final String destDir;
    private final boolean saveAsVideo;
    private int lastHour;
    private FileConnection fileConn;
    private DataOutputStream dataOutputStream;

    public CaptureThread(final VideoControl videoControl, final String destDir, final int snapshotDelay, final boolean saveAsVideo) {
	super(snapshotDelay);
	this.videoControl = videoControl;
	this.destDir = destDir;
	this.saveAsVideo = saveAsVideo;

	this.lastHour = -1;

	start();
    }

    public void execute() {
	try {
	    final byte[] photo = videoControl.getSnapshot("encoding=jpeg&quality=100");
	    saveImage2File(photo, saveAsVideo);
	} catch (MediaException e) {
	    if (e.getMessage().equals("Could not take snapshot.")) {
		// ignore because could happen if the camera is to slow
		// not cirtical
	    } else {
		ErrorHandler.doAlert(e);
	    }
	}
    }

    public void shutdown() {
	// close old file if opened
	if (dataOutputStream != null) {
	    try {
		dataOutputStream.close();
		fileConn.close();
	    } catch (IOException e) {
		ErrorHandler.doAlert(e);
	    }
	}
    }

    private void saveImage2File(final byte[] photo, final boolean saveAsVideo) {
	final Calendar calendar = Calendar.getInstance();
	calendar.setTime(new Date());

	try {
	    // open new file if it should be saved as a picture or a new hour
	    // begins
	    if (!saveAsVideo || calendar.get(Calendar.HOUR_OF_DAY) != lastHour) {
		lastHour = calendar.get(Calendar.HOUR_OF_DAY);
		// close old file if opened
		if (dataOutputStream != null) {
		    dataOutputStream.close();
		    fileConn.close();
		}
		
		setDebugInfo("closed");

		// create and open new file
		fileConn = (FileConnection) Connector.open(getPictureFileName(calendar, saveAsVideo));
		setDebugInfo("opend");
		if (!fileConn.exists()) {
		    fileConn.create();
		}
		setDebugInfo("created");
		
		dataOutputStream = new DataOutputStream(fileConn.openOutputStream());
		setDebugInfo("stream");
	    }

	    dataOutputStream.write(photo);
	    dataOutputStream.flush();
	} catch (IOException e) {
	    ErrorHandler.doAlert(e);
	}
    }

    private final String getPictureFileName(final Calendar calendar, final boolean isVideo) {
	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH) + 1;
	int day = calendar.get(Calendar.DAY_OF_MONTH);
	int hour = calendar.get(Calendar.HOUR_OF_DAY);
	int minute = calendar.get(Calendar.MINUTE);
	int second = calendar.get(Calendar.SECOND);

	String fileName = destDir + "/" + year + "-" + getFormattedNumber(month) + "-" + getFormattedNumber(day);
	if (!isVideo) {
	    fileName += "/Hour_" + getFormattedNumber(hour);
	}
	fileName += "/IMG_" + getFormattedNumber(hour) + "-" + getFormattedNumber(minute) + "-";
	fileName += getFormattedNumber(second) + (isVideo ? ".mjpg" : ".jpg");

	return fileName;
    }

    private final String getFormattedNumber(int number) {
	String formattedNumber = "";
	if (number <= 9)
	    formattedNumber += "0";
	formattedNumber += number;

	return formattedNumber;
    }
    
    
    private void setDebugInfo(final String desc) {
	final long total = Runtime.getRuntime().totalMemory();
	final long space = Runtime.getRuntime().freeMemory();
	final String debugInfo = desc + " (space: " + space + ", total: " + total + ")";
	
	try {
	    RecordStore rsData = RecordStore.openRecordStore("DEBUG", true);
	    rsData.setRecord(1, debugInfo.getBytes(), 0, debugInfo.length());
	    rsData.closeRecordStore();
	} catch (RecordStoreFullException e) {
	    ErrorHandler.doAlert(e);
	} catch (RecordStoreNotFoundException e) {
	    ErrorHandler.doAlert(e);
	} catch (RecordStoreException e) {
	    ErrorHandler.doAlert(e);
	}
    }
}
