package ca.ubc.cs.cpsc538b.replay;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "ca.ubc.cs.cpsc538b.replay";

    // The shared instance
    private static Activator plugin;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .addPerspectiveListener(new ControlRecordingPerspectiveListener());

        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    private static final class ControlRecordingPerspectiveListener implements IPerspectiveListener {

        @Override
        public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
        }

        @Override
        public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            if (false /* TODO doesn't really stop the recording, fix this */&& perspective.getId().equals(
                    "ca.ubc.cs.cpsc538b.replay.perspective")) {
                ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
                Command command = service.getCommand("ca.ubc.cs.cpsc538b.replay.commands.record");
                State state = command.getState("org.eclipse.ui.commands.toggleState");
                boolean isRecording = (boolean) state.getValue();

                if (isRecording) {
                    try {
                        HandlerUtil.toggleCommandState(command);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
