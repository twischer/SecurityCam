import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import javax.microedition.media.*;
import javax.microedition.media.control.VideoControl;

public class SecurityCam extends MIDlet
        implements CommandListener {
	
	private final String destDir = "file:///4:/SecurityCam/";

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

            Canvas canvas = new VideoCanvas(videoControl);

            cmdBack = new Command("Back", Command.BACK, 2);
            cmdCapture = new Command("Capture", Command.SCREEN, 3);
            canvas.addCommand(cmdBack);
            canvas.addCommand(cmdCapture);
            canvas.setCommandListener(this);
            display.setCurrent(canvas);
            player.start();
        } catch (IOException ioe) {
        } catch (MediaException me) {
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
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
                byte[] photo = videoControl.getSnapshot("encoding=jpeg&width=1280&height=960");
                saveImage2File(photo);
                Image image = Image.createImage(photo, 0, photo.length);
                form.append(image);
                display.setCurrent(form);
                player.close();
            } catch (MediaException me) {
            }
        }
    };






    void saveImage2File(byte[] photo) {
        // Receive a photo as byte array
        // Save Image to file
        FileConnection fileConn = null;
        DataOutputStream dos = null;

        try {
            fileConn = (FileConnection) Connector.open(destDir + "story1234.jpg");
            if (!fileConn.exists()) {
                fileConn.create();
            }
            dos = new DataOutputStream(fileConn.openOutputStream());
            dos.write(photo);
            dos.flush();
            dos.close();
            fileConn.close();

        } catch (IOException ioe) {
            System.out.println("Error!" + ioe);
        }
    }
} // end of VideoCaptureMidlet

class VideoCanvas extends Canvas {

    public VideoCanvas(VideoControl videoControl) {

        int width = getWidth()/2;
        int height = getHeight()/2;

        videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);

        try {
            videoControl.setDisplayLocation(2, 2);
            videoControl.setDisplaySize(width , height );

        } catch (MediaException me) {
        }
        videoControl.setVisible(true);
    }

    public void paint(Graphics g) {

        int width = getWidth()/2;
        int height = getHeight()/2;

        g.setColor(255, 255, 0);
        g.drawRect(0, 0, width , height );
        g.drawRect(1, 1, width , height );
    }
}

