package ca.ubc.cs.cpsc538b.replay;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IPerspectiveListener {

    // The plug-in ID
    public static final String PLUGIN_ID = "ca.ubc.cs.cpsc538b.replay";

    // The shared instance
    private static Activator plugin;

    private ICommandService commandService;

    private IHandlerService handlerService;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);

        commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);

        // Set the toggle buttons to be unselected at startup
        commandService.getCommand("ca.ubc.cs.cpsc538b.replay.commands.record")
                .getState("org.eclipse.ui.commands.toggleState").setValue(Boolean.FALSE);
        commandService.getCommand("ca.ubc.cs.cpsc538b.replay.commands.play")
                .getState("org.eclipse.ui.commands.toggleState").setValue(Boolean.FALSE);

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

    @Override
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
    }

    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        // Stop/start recording when switching to/from the Replay perspective
        Command recordingCommand = commandService.getCommand("ca.ubc.cs.cpsc538b.replay.commands.record");
        State state = recordingCommand.getState("org.eclipse.ui.commands.toggleState");
        boolean isRecording = (boolean) state.getValue();

        if (perspective.getId().equals("ca.ubc.cs.cpsc538b.replay.perspective") == isRecording) {
            try {
                handlerService.executeCommand(recordingCommand.getId(), null);
            } catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
                e.printStackTrace();
            }
        }
    }
}
