package org.flowkeeper.client.ui.workitem;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.StatusType;
import org.flowkeeper.server.WorkitemType;

/**
 *
 * 
 */
public class PomodoroIcon implements Icon {

    private final WorkitemType wi;

    private final static Image IMAGE_NEW_PLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/new_planned.png")).getImage();
    private final static Image IMAGE_NEW_UNPLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/new_unplanned.png")).getImage();
    private final static Image IMAGE_COMPLETED_PLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/completed_planned.png")).getImage();
    private final static Image IMAGE_COMPLETED_UNPLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/completed_unplanned.png")).getImage();
    private final static Image IMAGE_FAILED_PLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/failed_planned.png")).getImage();
    private final static Image IMAGE_FAILED_UNPLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/failed_unplanned.png")).getImage();
    private final static Image IMAGE_NOW_PLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/now_planned.png")).getImage();
    private final static Image IMAGE_NOW_UNPLANNED = new ImageIcon(
            PomodoroIcon.class.getResource("/images/now_unplanned.png")).getImage();

    private final static int w = IMAGE_COMPLETED_PLANNED.getWidth(null);
    private final static int h = IMAGE_COMPLETED_PLANNED.getHeight(null);

    public PomodoroIcon(WorkitemType wi) {
        this.wi = wi;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        int i = 0;
        for (PomodoroType p: wi.getPomodoro()) {

            Image img = null;

            if (p.getStatus().equals(StatusType.NEW)) {
                img = p.isPlanned() ? IMAGE_NEW_PLANNED : IMAGE_NEW_UNPLANNED;
            } else if (p.getStatus().equals(StatusType.COMPLETED)) {
                img = p.isPlanned() ? IMAGE_COMPLETED_PLANNED : IMAGE_COMPLETED_UNPLANNED;
            } else if (p.getStatus().equals(StatusType.FAILED)) {
                img = p.isPlanned() ? IMAGE_FAILED_PLANNED : IMAGE_FAILED_UNPLANNED;
            } else if (p.getStatus().equals(StatusType.STARTED)) {
                img = p.isPlanned() ? IMAGE_NOW_PLANNED : IMAGE_NOW_UNPLANNED;
            }
//            if (!p.getInterruption().isEmpty()) {
//                // Add interruption sign
//            }
//            if (!p.getMessage().isEmpty()) {
//                // Add message sign
//            }

            g.drawImage(img, x + w * i, y, null);

            ++i;
        }
    }

    public int getIconWidth() {
        return w * wi.getPomodoro().size();
    }

    public int getIconHeight() {
        return h;
    }
}
