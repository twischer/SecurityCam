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
import java.util.Enumeration;

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
	}

	public void run() {
		try {
		while(running) {
			final byte[] photo = videoControl.getSnapshot("encoding=jpeg&quality=100");
			saveImage2File(photo);
			
			Thread.sleep(snapshotTime*1000);
		}
		} catch (MediaException e) {
			ErrorHandler.doAlert(e);
		} catch (InterruptedException e) {
			ErrorHandler.doAlert(e);
		}
	}
	
	
	private void saveImage2File(final byte[] photo) {
		try {
			final FileConnection fileConn = (FileConnection) Connector.open(getPictureFileName());
			
			// delete folder a hole day if not enough free space available
			// use a reserve of 8kB on the memory
			if ( fileConn.availableSize() < (photo.length + 8096) )
				deleteOldestDayFolder();
			
			if (!fileConn.exists()) {
				fileConn.create();
			}
			final DataOutputStream dos = new DataOutputStream(
					fileConn.openOutputStream());
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
	    fileName += getFormattedNumber(day) + "/IMG_" + getFormattedNumber(hour) + "-";
	    fileName += getFormattedNumber(minute) + "-" + getFormattedNumber(second) + ".jpg";
	    
	    return fileName;
	}
	
	
	private final void deleteOldestDayFolder() throws IOException {
		final FileConnection folderConn = (FileConnection) Connector.open(destDir);
		
		long oldestTimeStamp = -1;
		FileConnection oldestFileConn = null;
		final Enumeration folderEnum = folderConn.list();
		while (folderEnum.hasMoreElements()) {
	         final String folder = (String)folderEnum.nextElement();
	         final FileConnection fileConn = (FileConnection) Connector.open(folder);
	         
	         // use the current folder if it is older
	         final long timeStamp = fileConn.lastModified();
	         if (oldestTimeStamp > timeStamp || oldestTimeStamp < 0)
	         {
	        	 oldestTimeStamp = timeStamp;
	        	 
	        	 if (oldestFileConn != null)
	        		oldestFileConn.close();
	        	
	        	 oldestFileConn = fileConn;
	         }
	         else
	        	 fileConn.close();
	     }
		folderConn.close();
		
		if (oldestFileConn != null) {
			oldestFileConn.delete();
			oldestFileConn.close();
		}
		else {
			ErrorHandler.doAlert("Not enough space on memory. Could not find files of SecurityCam which could be deleted.");
		}
			
    		
	}
	
	
	private final String getFormattedNumber(int number) {
		String formattedNumber = "";
		if (number <= 9)
			formattedNumber += "0";
		formattedNumber += number;
		
		return formattedNumber;
	}
	
	
	public void stop() throws InterruptedException{
		running = false;
		join();
	}
}
