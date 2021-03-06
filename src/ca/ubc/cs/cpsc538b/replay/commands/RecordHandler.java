package ca.ubc.cs.cpsc538b.replay.commands;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import ca.ubc.cs.cpsc538b.replay.Constants;

public class RecordHandler extends AbstractHandler {

    private Timer recordingTimer;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Command command = event.getCommand();
        boolean wasRecording = HandlerUtil.toggleCommandState(command);

        if (wasRecording) {
            recordingTimer.cancel();
            recordingTimer = null;
        } else {
            recordingTimer = new Timer(true);
            recordingTimer.scheduleAtFixedRate(new RecordingTimerTask(), 0, Constants.FRAME_DELAY_TIME);
        }

        return null;
    }

    public class RecordingTimerTask extends TimerTask {

        @Override
        public void run() {
            try {
                Robot robot = new Robot();
                Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
                ImageIO.write(bufferedImage, "png", new File(Constants.BASE_DIRECTORY, System.currentTimeMillis()
                        + ".png"));
            } catch (AWTException | IOException e) {
                e.printStackTrace();
            }
        }

    }

}
