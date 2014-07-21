package org.flowkeeper.client.ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

class Util {

    private final static Image ICON16 = new ImageIcon(
            Util.class.getResource("/images/tomato.png")).getImage();
    private final static Image ICON32 = new ImageIcon(
            Util.class.getResource("/images/tomato32.png")).getImage();
    private final static List<Image> ICONS = new ArrayList<Image>();

    static {
        ICONS.add(ICON16);
        ICONS.add(ICON32);
    }

    private static void center(Window w) {
        Dimension windowSize = w.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;
        w.setLocation(x, y);
    }

    private static void setIcon(Window w) {
        w.setIconImages(ICONS);
    }

    public static void decorate(final Window w) {
        center(w);
        setIcon(w);
    }

    public static void addEscapeListener(final JDialog d) {
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
            }
        };
        d.getRootPane().registerKeyboardAction(
                al,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static void decorate(final JDialog d, boolean closeOnEscape) {
        center(d);
        setIcon(d);
        if (closeOnEscape) {
            addEscapeListener(d);
        }
    }
}
