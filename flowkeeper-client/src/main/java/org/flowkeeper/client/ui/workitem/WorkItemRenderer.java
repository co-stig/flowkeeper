package org.flowkeeper.client.ui.workitem;

import javax.swing.table.DefaultTableCellRenderer;

import org.flowkeeper.server.WorkitemType;

/**
 *
 * 
 */
public abstract class WorkItemRenderer extends DefaultTableCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6996626578491644776L;

	@Override
    public void setValue(Object value) {
        setOpaque(true);
        setValue ((WorkitemType) value);
    }
    
    public abstract void setValue(WorkitemType wi);
}
