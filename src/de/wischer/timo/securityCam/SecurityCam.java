package de.wischer.timo.securityCam;
import java.util.Calendar;
import java.util.Date;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import javax.microedition.media.*;
import javax.microedition.media.control.VideoControl;

public class SecurityCam extends MIDlet implements CommandListener {

	private final String destDir = "file:///4:/SecurityCam";

	private Display display;
	private Form form;
	private Command cmdBack, cmdCapture, cmdCamera;
	private Player player;
	private VideoControl videoControl;
	private Video video;

	public SecurityCam() {
		form = new Form("Take photo");
		cmdCamera = new Command("Camera", Command.SCREEN, 1);
		form.addCommand(cmdCamera);
		cmdCapture = new Command("Capture", Command.SCREEN, 3);
		form.addCommand(cmdCapture);
		form.setCommandListener(this);
	}

	public void startApp() {
		display = Display.getDisplay(this);
		display.setCurrent(form);
	}

	public void commandAction(Command c, Displayable s) {

		String label = c.getLabel();

		if (label.equals("Camera")) {
			showCamera();
		} else if (label.equals("Back")) {
			display.setCurrent(form);
		} else if (label.equals("Capture")) {
			video = new Video(this);
			video.start();
		}
	}

	public void showCamera() {
		try {
			player = Manager.createPlayer("capture://video");
			player.realize();
			videoControl = (VideoControl) player.getControl("VideoControl");
			Item mVideoItem = (Item) videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
	        form.append(mVideoItem);

/*			Canvas canvas = new VideoCanvas(videoControl);

			cmdBack = new Command("Back", Command.BACK, 2);
			cmdCapture = new Command("Capture", Command.SCREEN, 3);
			canvas.addCommand(cmdBack);
			canvas.addCommand(cmdCapture);
			canvas.setCommandListener(this);
			display.setCurrent(canvas);*/
			player.start();
		} catch (IOException ioe) {
		} catch (MediaException me) {
		}
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
		player.close();
		notifyDestroyed();
	}

	class Video extends Thread {

		SecurityCam midlet;

		public Video(SecurityCam midlet) {
			this.midlet = midlet;
		}

		public void run() {
			takePhoto();
		}

		public void takePhoto() {
			try {
				byte[] photo = videoControl.getSnapshot("encoding=jpeg&width=320&height=240");
				// width=1280&height=960
				saveImage2File(photo);
			} catch (MediaException me) {
			}
		}
	};

	void saveImage2File(byte[] photo) {
		// Receive a photo as byte array
		// Save Image to file
		try {
			FileConnection fileConn = (FileConnection) Connector.open(destDir + "/IMG_" + getCurrentDateString() + ".jpg");
			if (!fileConn.exists()) {
				fileConn.create();
			}
			DataOutputStream dos = new DataOutputStream(
					fileConn.openOutputStream());
			dos.write(photo);
			dos.flush();
			dos.close();
			fileConn.close();
			
			System.out.println("Done");

		} catch (IOException ioe) {
			System.out.println("Error!" + ioe);
		}
	}
	
	String getCurrentDateString() {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(new Date());

	    int year = calendar.get(Calendar.YEAR);
	    int month = calendar.get(Calendar.MONTH) + 1;
	    int day = calendar.get(Calendar.DAY_OF_MONTH);
	    int hour = calendar.get(Calendar.HOUR_OF_DAY);
	    int minute = calendar.get(Calendar.MINUTE);
	    int second = calendar.get(Calendar.SECOND);
	    
	    String dateString = year + "-" + month + "-" + day + "_" + hour + "-" + minute + "-" + second;
	    
	    return dateString;
	}

} // end of VideoCaptureMidlet

class VideoCanvas extends Canvas {

	public VideoCanvas(VideoControl videoControl) {

		int width = getWidth() / 2;
		int height = getHeight() / 2;

		videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);

		try {
			videoControl.setDisplayLocation(2, 2);
			videoControl.setDisplaySize(width, height);

		} catch (MediaException me) {
		}
		videoControl.setVisible(true);
	}

	public void paint(Graphics g) {

		int width = getWidth() / 2;
		int height = getHeight() / 2;

		g.setColor(255, 255, 0);
		g.drawRect(0, 0, width, height);
		g.drawRect(1, 1, width, height);
	}
}
