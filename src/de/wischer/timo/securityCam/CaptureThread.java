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

public class CaptureThread extends DelayThread {
    private final VideoControl videoControl;
    private final String destDir;

    public CaptureThread(final VideoControl videoControl, final String destDir, final int snapshotDelay) {
	super(snapshotDelay);
	this.videoControl = videoControl;
	this.destDir = destDir;

	start();
    }

    public void execute() {
	try {
	    final byte[] photo = videoControl.getSnapshot("encoding=jpeg&quality=100");
	    saveImage2File(photo);
	} catch (MediaException e) {
	    if (e.getMessage().equals("Could not take snapshot.")) {
		// ignore because could happen if the camera is to slow
		// not cirtical
	    } else {
		ErrorHandler.doAlert(e);
	    }
	}
    }

    private void saveImage2File(final byte[] photo) {
	try {
	    final FileConnection fileConn = (FileConnection) Connector.open(getPictureFileName());

	    if (!fileConn.exists()) {
		fileConn.create();
	    }
	    final DataOutputStream dos = new DataOutputStream(fileConn.openOutputStream());
	    dos.write(photo);
	    dos.flush();
	    dos.close();
	    fileConn.close();

	} catch (IOException e) {
	    ErrorHandler.doAlert(e);
	}
    }

    private final String getPictureFileName() {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(new Date());

	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH) + 1;
	int day = calendar.get(Calendar.DAY_OF_MONTH);
	int hour = calendar.get(Calendar.HOUR_OF_DAY);
	int minute = calendar.get(Calendar.MINUTE);
	int second = calendar.get(Calendar.SECOND);

	String fileName = destDir + "/" + year + "-" + getFormattedNumber(month) + "-";
	fileName += getFormattedNumber(day) + "/Hour_" + getFormattedNumber(hour) + "/IMG_" + getFormattedNumber(hour);
	fileName += "-" + getFormattedNumber(minute) + "-" + getFormattedNumber(second) + ".jpg";

	return fileName;
    }

    private final String getFormattedNumber(int number) {
	String formattedNumber = "";
	if (number <= 9)
	    formattedNumber += "0";
	formattedNumber += number;

	return formattedNumber;
    }
}
