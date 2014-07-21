package org.flowkeeper.client.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.flowkeeper.client.ui.timer.Timer;
import org.flowkeeper.client.ui.timer.TimerListener;
import org.flowkeeper.client.ui.timer.TimerState;
import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.api.NoPomodorosLeftException;
import org.flowkeeper.server.api.NotFoundException;

public class TraySupport implements TimerListener {

    private Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    private final static String TEXT_READY = "Flowkeeper is ready for new tasks";
    private final static Image IMAGE_READY = new ImageIcon(
        TraySupport.class.getResource("/images/tomato.png")).getImage();

    private final static String TEXT_BUSY = "In a Pomodoro";
//    private final static Image IMAGE_BUSY = new ImageIcon(
//        TraySupport.class.getResource("/images/tray-busy.png")).getImage();

    private final static String TEXT_REST = "Having rest";
//    private final static Image IMAGE_REST = new ImageIcon(
//        TraySupport.class.getResource("/images/tray-rest.png")).getImage();

    private final TrayIcon trayIcon;

    private final ActionListener exitListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };

    private final ActionListener settingsListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            mainWindow.displaySettings();
        }
    };

    private final ActionListener nextPomodoroListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
                mainWindow.startPomodoro();
            } catch (NotFoundException ex) {
                Logger.getLogger(TraySupport.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoPomodorosLeftException ex) {
                Logger.getLogger(TraySupport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    private final PopupMenu popup = new PopupMenu();
    private final MenuItem menuExitItem = new MenuItem("Exit");
    private final MenuItem menuSettingsItem = new MenuItem("Settings");
    private final MenuItem menuNextPomodoroItem = new MenuItem("Next Pomodoro");
    private final Timer timer;
    private final PomodoroClientView mainWindow;

    public void pomodoroItemSetEnabled(boolean enabled) {
        menuNextPomodoroItem.setEnabled(enabled);
    }

    public TraySupport(final Timer timer, final PomodoroClientView mainWindow) {
        this.timer = timer;
        this.mainWindow = mainWindow;

        // TODO: Add this check to settings window as well
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            menuExitItem.addActionListener(exitListener);
            menuSettingsItem.addActionListener(settingsListener);
            menuNextPomodoroItem.addActionListener(nextPomodoroListener);

            popup.add(menuNextPomodoroItem);
            popup.add(menuSettingsItem);
            popup.add(menuExitItem);
            
            trayIcon = new TrayIcon(IMAGE_READY, TEXT_READY, popup);

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mainWindow.getFrame().setVisible(true);
                    mainWindow.getFrame().toFront();
                }
            };

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
            
        } else {
            trayIcon = null;
        }
    }

    private void displayMessage(String caption, String text) {
        if (trayIcon != null) {
            trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);
        }
    }

    private void displayMessage(String text) {
        displayMessage(null, text);
    }

    private void changeState(TimerState state) {
        if (trayIcon == null) {
            return;
        }
        switch (state) {
            case BUSY:
//                trayIcon.setImage(IMAGE_BUSY);
                trayIcon.setToolTip(TEXT_BUSY);
                break;
            case READY:
                trayIcon.setImage(IMAGE_READY);
                trayIcon.setToolTip(TEXT_READY);
                displayMessage("Ready for new tasks");
                break;
            case REST:
//                trayIcon.setImage(IMAGE_REST);
                trayIcon.setToolTip(TEXT_REST);
                displayMessage("Time is up, have some rest now");
                break;
        }
    }

    public void onPomodoroStarted(PomodoroType pomodoro) {
        changeState(TimerState.BUSY);
    }

    public void onWorkCompleted(PomodoroType pomodoro, boolean successfully) {
        changeState(TimerState.REST);
    }

    public void onReady() {
        changeState(TimerState.READY);
    }

    public void onTick() {
        double angle = getCurrentAngle();
        //System.out.println(angle);
        updateImage(angle);
        updateTooltip();
        if (trayIcon != null) {
            trayIcon.setImage(img);
        }
    }

    private double getCurrentAngle() {
        try {
            long fullTime = timer.getProjectedFinishTime().getTime() - timer.getStateEnterTime().getTime();
            return 1.0 - (double)timer.getTimeLeft() / fullTime;
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateImage(double angle) {
        Graphics gr = img.getGraphics();

        gr.setColor(Color.white);
        gr.fillOval(1, 1, 14, 14);

        gr.setColor(Color.getHSBColor((float)(0.5 - angle / 2), 1f, 1));
        gr.fillArc(1, 1, 14, 14, 90, (int)(-360.0 * angle));

        gr.setColor(Color.darkGray);
        gr.drawOval(1, 1, 14, 14);
        gr.drawLine(8, 8, 8, 1);
        double angleRad = (0 - 2.0 * Math.PI * angle);
        gr.drawLine(8, 8, 8 - (int)(7.0 * Math.sin(angleRad)), 8 - (int)(7.0 * Math.cos(angleRad)));

        gr.setColor(Color.white);
        gr.fillOval(6, 6, 4, 4);
        gr.setColor(Color.darkGray);
        gr.drawOval(6, 6, 4, 4);
    }

    private void updateTooltip() {
        if (trayIcon == null) {
            return;
        }
        switch (timer.getState()) {
            case BUSY:
                long minLeft = timer.getTimeLeft() / 60000;
                long secLeft = (timer.getTimeLeft() % 60000) / 1000;
                trayIcon.setToolTip("In a Pomodoro, " + minLeft + ":" + secLeft + " left");
                break;
            case READY:
                trayIcon.setToolTip(TEXT_READY);
                break;
            case REST:
                minLeft = timer.getTimeLeft() / 60000;
                secLeft = (timer.getTimeLeft() % 60000) / 1000;
                trayIcon.setToolTip("Having rest, " + minLeft + ":" + secLeft + " left");
                break;
        }
    }
}
