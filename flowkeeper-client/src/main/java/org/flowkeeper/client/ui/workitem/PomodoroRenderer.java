package org.flowkeeper.client.ui.workitem;

import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.StatusType;
import org.flowkeeper.server.WorkitemType;

/**
 *
 * 
 */
public class PomodoroRenderer extends WorkItemRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2407288321605841476L;

	@Override
    public void setValue(WorkitemType w) {
        setIcon(new PomodoroIcon(w));
        //setText(r.toString());
    }

	public static String toString(WorkitemType w) {
		StringBuilder r = new StringBuilder();

        for (PomodoroType p: w.getPomodoro()) {
            if (p.getStatus().equals(StatusType.NEW) && p.isPlanned()) {
                r.append('N');
            } else if (p.getStatus().equals(StatusType.COMPLETED) && p.isPlanned()) {
                r.append('C');
            } else if (p.getStatus().equals(StatusType.FAILED) && p.isPlanned()) {
                r.append('F');
            } else if (p.getStatus().equals(StatusType.NEW) && !p.isPlanned()) {
                r.append('n');
            } else if (p.getStatus().equals(StatusType.COMPLETED) && !p.isPlanned()) {
                r.append('c');
            } else if (p.getStatus().equals(StatusType.FAILED) && !p.isPlanned()) {
                r.append('f');
            } else if (p.getStatus().equals(StatusType.STARTED)) {
                if (p.isPlanned()) {
                    r.append('S');
                } else {
                    r.append('s');
                }
            } else {
                r.append('?');
            }
            for (int i = 0; i < p.getInterruption().size(); ++i) {
                r.append('\'');
            }
        }
        
        return r.toString();
	}
}
