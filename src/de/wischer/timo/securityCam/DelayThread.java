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

public abstract class DelayThread extends Thread {
    private final int delay;
    private boolean running;

    public DelayThread(final int delayInSec) {
	this.delay = delayInSec;
	this.running = true;
    }

    public void run() {
	try {
	    while (running) {
		execute();

		Thread.sleep(delay * 1000);
	    }
	} catch (InterruptedException e) {
	    ErrorHandler.doAlert(e);
	}
	
	shutdown();
    }

    public abstract void execute();
    
    
    public void shutdown(){
	// could be implemented by inherit classes
    }

    public void stop() throws InterruptedException {
	running = false;
	join();
    }
}
