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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import ca.ubc.cs.cpsc538b.replay.Constants;

public class MainViewPart extends ViewPart implements SelectionListener, MouseWheelListener, IExecutionListener {

    private static final IOFileFilter FILE_FILTER = FileFilterUtils.and(CanReadFileFilter.CAN_READ,
            new RegexFileFilter("^\\d+\\.png$"));

    private Device device;
    private Scale scale;
    private Composite imageLabelContainer;
    private Label imageLabel;

    private ICommandService iCommandService;

    private int imageFileIndex;
    private List<File> imageFiles;

    private Image backgroundImage;
    private Image currentImage;

    @Override
    public void createPartControl(Composite parent) {
        GridLayout gridLayout = new GridLayout(2, false);
        parent.setLayout(gridLayout);

        device = Display.getCurrent();

        scale = new Scale(parent, SWT.VERTICAL | SWT.NO_FOCUS);
        scale.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        scale.addSelectionListener(this);

        imageLabelContainer = new Composite(parent, SWT.NO_BACKGROUND);
        imageLabelContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        imageLabelContainer.addMouseWheelListener(this);

        imageLabel = new Label(imageLabelContainer, SWT.NO_BACKGROUND);

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
        imageLabel.setImage(null);
        scale.setEnabled(false);

        imageFileIndex = 0;
        imageFiles.clear();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        imageFileIndex = scale.getSelection();
        replaceImage();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    @Override
    public void mouseScrolled(MouseEvent e) {
        if (e.count > 0) {
            imageFileIndex = Math.max(0, imageFileIndex - 1);
        } else if (e.count < 0) {
            imageFileIndex = Math.min(imageFiles.size() - 1, imageFileIndex + 1);
        }
        replaceImage();
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

            scale.setMinimum(0);
            scale.setMaximum(imageFileIndex);
            scale.setEnabled(true);

            replaceImage();
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

    private void replaceImage() {
        safeDisposeImage(currentImage);

        if (!imageFiles.isEmpty()) {
            scale.setSelection(imageFileIndex);

            Image backendImage = new Image(device, imageFiles.get(imageFileIndex).getAbsolutePath());

            Rectangle containerBounds = imageLabelContainer.getBounds();
            Rectangle backendImageBounds = backendImage.getBounds();
            Rectangle resizedImageBounds = fitImageInContainer(containerBounds, backendImageBounds);

            currentImage = new Image(device, containerBounds.width, containerBounds.height);
            GC gc = new GC(currentImage);
            gc.setAntialias(SWT.ON);
            gc.drawImage(backendImage, 0, 0, backendImageBounds.width, backendImageBounds.height, 0, 0,
                    resizedImageBounds.width, resizedImageBounds.height);
            backendImage.dispose();

            imageLabel.setImage(currentImage);

            imageLabel.setSize(resizedImageBounds.width, resizedImageBounds.height);
        }
    }

    private Rectangle fitImageInContainer(Rectangle containerBounds, Rectangle imageBounds) {
        Rectangle resizeBounds = new Rectangle(0, 0, 0, 0);

        double containerRatio = (double) containerBounds.width / (double) containerBounds.height;
        double imageRatio = (double) imageBounds.width / (double) imageBounds.height;

        if (containerRatio > imageRatio) {

            resizeBounds.width = (int) Math.round(imageBounds.width
                    * ((double) containerBounds.height / (double) imageBounds.height));
            resizeBounds.height = containerBounds.height;
        } else {
            resizeBounds.width = containerBounds.width;
            resizeBounds.height = (int) Math.round(imageBounds.height
                    * ((double) containerBounds.width / (double) imageBounds.width));
        }

        return resizeBounds;
    }

    private void safeDisposeImage(Image image) {
        if (image != null && !image.isDisposed()) {
            image.dispose();
        }
    }

}
