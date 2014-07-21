package org.flowkeeper.client.ui.workitem;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.flowkeeper.server.WorkitemType;

/**
 *
 * 
 */
public class WorkItemStatusRenderer extends WorkItemRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1264107692339703640L;
	private final static Icon ICON_UNPLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/grid-unplanned.png"));

    @Override
    public void setValue(WorkitemType w) {
        if (w.getSection().equalsIgnoreCase("unplanned")) {
            setIcon(ICON_UNPLANNED);
        } else {
            setIcon(null);
        }
    }
}
