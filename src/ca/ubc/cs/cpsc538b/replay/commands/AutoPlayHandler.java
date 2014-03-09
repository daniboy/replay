package ca.ubc.cs.cpsc538b.replay.commands;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ca.ubc.cs.cpsc538b.replay.Constants;
import ca.ubc.cs.cpsc538b.replay.view.MainViewPart;

public class AutoPlayHandler extends AbstractHandler {

    private Timer autoPlayTimer;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Command command = event.getCommand();
        boolean wasAutoPlaying = HandlerUtil.toggleCommandState(command);

        if (wasAutoPlaying) {
            autoPlayTimer.cancel();
            autoPlayTimer = null;
        } else {
            MainViewPart replayView = (MainViewPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().findView("ca.ubc.cs.cpsc538b.replay.view.main");

            autoPlayTimer = new Timer(true);
            autoPlayTimer.scheduleAtFixedRate(new AutoPlayingTimerTask(replayView, command), 0,
                    Constants.REPLAY_FRAME_TIME);
        }

        return null;
    }

    public class AutoPlayingTimerTask extends TimerTask {

        private MainViewPart replayView;
        private Command command;

        public AutoPlayingTimerTask(MainViewPart replayView, Command command) {
            this.replayView = replayView;
            this.command = command;
        }

        @Override
        public void run() {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    boolean hasMoreFrames = replayView.showNextFrame();
                    if (!hasMoreFrames) {
                        try {
                            HandlerUtil.toggleCommandState(command);
                            autoPlayTimer.cancel();
                            autoPlayTimer = null;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

    }

}
