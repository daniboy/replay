package ca.ubc.cs.cpsc538b.replay.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import ca.ubc.cs.cpsc538b.replay.Constants;

public class MainViewPart extends ViewPart implements MouseWheelListener, IExecutionListener {

    private static final IOFileFilter FILE_FILTER = FileFilterUtils.and(CanReadFileFilter.CAN_READ,
            new RegexFileFilter("^\\d+\\.png$"));

    private Label label;
    private Device device;

    private ICommandService iCommandService;

    private int imageFileIndex;
    private List<File> imageFiles;

    private Image backgroundImage;
    private Image currentImage;

    @Override
    public void createPartControl(Composite parent) {
        GridLayout layout = new GridLayout();
        parent.setLayout(layout);

        label = new Label(parent, SWT.NO_BACKGROUND);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        label.addMouseWheelListener(this);

        device = Display.getCurrent();

        iCommandService = (ICommandService) getSite().getService(ICommandService.class);
        iCommandService.addExecutionListener(this);

        imageFileIndex = 0;
        imageFiles = new ArrayList<>();

        try {
            Bundle bundle = Platform.getBundle("ca.ubc.cs.cpsc538b.replay");
            Path path = new Path("res/stripe_background.png");
            URL url = FileLocator.find(bundle, path, null);
            backgroundImage = new Image(device, url.openStream());
            parent.setBackgroundImage(backgroundImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setFocus() {
        label.setImage(null);
    }

    @Override
    public void mouseScrolled(MouseEvent e) {
        replaceImage(e.count);
    }

    @Override
    public void notHandled(String commandId, NotHandledException exception) {
    }

    @Override
    public void postExecuteFailure(String commandId, ExecutionException exception) {
    }

    @Override
    public void postExecuteSuccess(String commandId, Object returnValue) {
        if (commandId.equals("ca.ubc.cs.cpsc538b.replay.commands.refresh")) {
            imageFiles.clear();
            imageFiles.addAll(FileUtils.listFiles(Constants.BASE_DIRECTORY, FILE_FILTER, TrueFileFilter.TRUE));
            Collections.sort(imageFiles);

            imageFileIndex = imageFiles.size() - 1;
            replaceImage(0);
        }
    }

    @Override
    public void preExecute(String commandId, ExecutionEvent event) {
    }

    @Override
    public void dispose() {
        iCommandService.removeExecutionListener(this);
        safeDisposeImage(backgroundImage);
        safeDisposeImage(currentImage);

        super.dispose();
    }

    private void replaceImage(int dir) {
        safeDisposeImage(currentImage);

        if (!imageFiles.isEmpty()) {
            if (dir > 0) {
                imageFileIndex = Math.max(0, imageFileIndex - 1);
            } else if (dir < 0) {
                imageFileIndex = Math.min(imageFiles.size() - 1, imageFileIndex + 1);
            }

            currentImage = new Image(device, imageFiles.get(imageFileIndex).getAbsolutePath());
            label.setImage(currentImage);
        }
    }

    private void safeDisposeImage(Image image) {
        if (image != null && !image.isDisposed()) {
            image.dispose();
        }
    }

}
