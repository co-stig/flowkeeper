package org.flowkeeper.client.ui.workitem;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.flowkeeper.server.StatusType;
import org.flowkeeper.server.WorkitemType;

/**
 *
 * 
 */
public class WorkItemTitleRenderer extends WorkItemRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = -203483357836074665L;
	private final static Icon ICON_COMPLETE = new ImageIcon(
            PomodoroIcon.class.getResource("/images/tick.png"));
    private final static Icon ICON_NOW = new ImageIcon(
            PomodoroIcon.class.getResource("/images/grid-started.png"));

    @Override
    public void setValue(WorkitemType w) {
        setText(w.getTitle());
        if (w.getStatus().equals(StatusType.COMPLETED)) {
            setIcon(ICON_COMPLETE);
        } else if (w.getStatus().equals(StatusType.STARTED)) {
            setIcon(ICON_NOW);
        } else {
            setIcon(null);
        }
    }
}
