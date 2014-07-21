package org.flowkeeper.client.ui.workitem;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * 
 */
public class PomodoroSelectionComboBox extends JComboBox {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3331295815324871788L;

	public PomodoroSelectionComboBox () {
        setModel(new DefaultComboBoxModel(new Integer[] { 1, 2, 3, 4 }));
    }
}
