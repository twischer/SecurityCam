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
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class DeleteThread extends DelayThread {
    private final String destDir;
    private final long minFreeSpaceInByte;
    private final long minFreeMemorySpace;

    public DeleteThread(final String destDir, final int deleteDelay, final int minFreeSpaceInMiByte) {
	super(deleteDelay);
	this.destDir = destDir;
	this.minFreeSpaceInByte = minFreeSpaceInMiByte * 1024 * 1024;
	this.minFreeMemorySpace = Runtime.getRuntime().totalMemory() / 100;

	start();
    }

    public void execute() {
	try {
	    final FileConnection folderConn = (FileConnection) Connector.open(destDir);

	    // delete folder a hole day if not enough free space available
	    // use a reserve of 8kB on the memory
	    if (folderConn.availableSize() < minFreeSpaceInByte) {
		long oldestTimeStamp = -1;
		FileConnection oldestFileConn = null;
		final Enumeration folderEnum = folderConn.list();
		while (folderEnum.hasMoreElements()) {
		    final String folder = (String) folderEnum.nextElement();
		    final FileConnection fileConn = (FileConnection) Connector.open(folder);

		    // use the current folder if it is older
		    final long timeStamp = fileConn.lastModified();
		    if (oldestTimeStamp > timeStamp || oldestTimeStamp < 0) {
			oldestTimeStamp = timeStamp;

			if (oldestFileConn != null)
			    oldestFileConn.close();

			oldestFileConn = fileConn;
		    } else
			fileConn.close();
		}
		folderConn.close();

		if (oldestFileConn != null) {
		    oldestFileConn.delete();
		    oldestFileConn.close();
		} else {
		    ErrorHandler.doAlert("Not enough space on memory. Could not find files of SecurityCam which could be deleted.");
		}

	    }
	} catch (IOException e) {
	    ErrorHandler.doAlert(e);
	}
	
	// run garvage collector if not enough memory space available
	// some mobile phones do this not automatically
	final long freeMemory = Runtime.getRuntime().freeMemory();
	if (freeMemory < minFreeMemorySpace) {
	    System.gc();
	}
    }
}
