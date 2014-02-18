package ca.ubc.cs.cpsc538b.replay.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class ReplayPerspectiveFactory implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        layout.setEditorAreaVisible(false);
        layout.addView("ca.ubc.cs.cpsc538b.replay.view.main", IPageLayout.TOP, 1.0f, layout.getEditorArea());
    }

}
