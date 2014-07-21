package org.flowkeeper.client.ui;

import java.awt.SystemTray;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.flowkeeper.server.api.OfflineServerImpl;
import org.flowkeeper.server.api.Server;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class PomodoroClientApp extends SingleFrameApplication {

    private static final boolean MULTIUSER = false;
	private static String[] commandLine;

    private boolean startHidden() {
        return commandLine.length > 0 && commandLine[0].equals("-s");
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        
        checkTray();

        LoginWindow login = new LoginWindow(getMainFrame(), true);

        if (MULTIUSER) {
            //login.setLocationByPlatform(true);
            login.setVisible(true);

            if (!login.isSuccess()) {
                exit();
            }
        }
        // final Server server = login.getServer();
        try {
            
            final Server server = new OfflineServerImpl();

            addExitListener(new ExitListener() {
                public boolean canExit(EventObject event) {
                    return true;
                }
                public void willExit(EventObject event) {
                    System.out.println("Exiting now");
                    server.logout();
                }
            });

            PomodoroClientView pcv = new PomodoroClientView(this, server);
            show(pcv);
            if (startHidden()) {
                pcv.getFrame().setVisible(false);
            }

        } catch (Exception ex) {
            Logger.getLogger(PomodoroClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of PomodoroClientApp
     */
    public static PomodoroClientApp getApplication() {
        return Application.getInstance(PomodoroClientApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        commandLine = args;
        launch(PomodoroClientApp.class, args);
    }

    private void checkTray() {
        if (!SystemTray.isSupported()) {
            Preferences prefs = Preferences.userRoot().node("pomodoroServer");
            prefs.put("timerWindowMode", "N");
        }
    }
}
