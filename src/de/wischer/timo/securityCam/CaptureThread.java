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

public class CaptureThread extends Thread {
	private final VideoControl videoControl;
	private final String destDir;
	private final int snapshotTime;
	private boolean running;
	
	
	public CaptureThread(final VideoControl videoControl, final String destDir, final int snapshotTime) {
		this.videoControl = videoControl;
		this.destDir = destDir;
		this.snapshotTime = snapshotTime;
		running = true;
		
		start();
		
		//boolean ta = interrupt0();
	}

	public void run() {
		try {
		while(running) {
			final byte[] photo = videoControl.getSnapshot("encoding=jpeg&width=320&height=240");
			// width=1280&height=960
			saveImage2File(photo);
			
			Thread.sleep(snapshotTime*1000);
		}
		} catch (MediaException me) {
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void saveImage2File(final byte[] photo) {
		// Receive a photo as byte array
		// Save Image to file
		try {
			final FileConnection fileConn = (FileConnection) Connector.open(getPictureFileName());
			if (!fileConn.exists()) {
				fileConn.create();
			}
			final DataOutputStream dos = new DataOutputStream(
					fileConn.openOutputStream());
			dos.write(photo);
			dos.flush();
			dos.close();
			fileConn.close();

		} catch (IOException ioe) {
			System.out.println("Error!" + ioe);
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
	    
	    String fileName = destDir + "/" + year + "-" + month + "-" + day + "/IMG_" + hour + "-" + minute + "-" + second + ".jpg";
	    
	    return fileName;
	}
	
	public void stop() throws InterruptedException{
		running = false;
		join();
	}
}
