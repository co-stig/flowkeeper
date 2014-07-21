package org.flowkeeper.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.datatype.XMLGregorianCalendar;

import org.flowkeeper.client.ui.timer.TimerState;
import org.flowkeeper.client.ui.workitem.PomodoroRenderer;
import org.flowkeeper.server.InterruptionType;
import org.flowkeeper.server.Messages;
import org.flowkeeper.server.PlanType;
import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.StatusType;
import org.flowkeeper.server.WorkitemType;
import org.flowkeeper.server.api.AlreadyExistsException;
import org.flowkeeper.server.api.InvalidStateException;
import org.flowkeeper.server.api.NoPomodorosLeftException;
import org.flowkeeper.server.api.NotFoundException;
import org.flowkeeper.server.api.OfflineServerImpl;
import org.flowkeeper.server.api.Server;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;

/**
 * The application's main frame.
 */
public class PomodoroClientView extends FrameView {

    private final Server server;
    private final TimerWindow timerWindow;
    private TraySupport tray = null;
    private final Preferences prefs = Preferences.userRoot().node("pomodoroServer");
    private SoundsTimerListener soundListener = null;

    private final MouseListener showPomodoroMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            timerWindow.setVisible(true);
        }
    };

    private void checkForUpdates() {
        if (prefs.get("updatesCheck", "Y").equals("Y")) {
            try {
                URL u = new URL("http://flowkeeper.org/updates");
//                URL u = new URL("file:///c://1/upd.html");
                InputStream is = null;
                String html = null;
                try {
                    is = u.openStream();
                    byte[] buf = new byte[8192];
                    is.read(buf, 0, buf.length);
                    html = new String(buf);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
                if (html != null) {
                    int start = html.indexOf("<!--latest-version#");
                    int end = html.indexOf("#latest-version-->");
                    if (start > 0 && end > 0) {
                        String version = html.substring(start + 19, end);
                        if (!version.equals("12 / 2010")) {
                            new UpdateDialog(getFrame(), true, version).setVisible(true);
                        }
                    }
                }
            } catch (Throwable t) {
                Logger.getLogger(PomodoroClientView.class.getName()).log(Level.INFO, "Unable to connect to updates server");
            }
        }
    }

    private final PomodoroListener pomodoroListener = new PomodoroListener() {

        public void refreshUiOnFinish(WorkitemType workItem) {
            try {
                // statusMessageLabel.removeMouseListener(showPomodoroMouseListener);
                setToolbarButtonsEnabledAuto();
                try {
                    refreshWorkItem(findWorkItem(workItem));
                } catch (NotFoundException ex) {
                    Logger.getLogger(PomodoroClientView.class.getName()).log(Level.SEVERE, null, ex);
                }
                checkForMessages(workItem);
            } catch (InvalidStateException ex) {
                Logger.getLogger(PomodoroClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void pomodoroVoid(WorkitemType workItem) {
            try {
                statusMessageLabel.setText("Pomodoro cancelled");
                server.voidPomodoro(workItem);
                timer.voidCurrentPomodoro();
                refreshUiOnFinish(workItem);
//                tray.displayMessage("Pomodoro void, ready for new tasks.", "Working on \"" + workItem.getTitle() + "\"");
//                tray.changeState(TimerState.READY);
            } catch (InvalidStateException ex) {
                Logger.getLogger(PomodoroClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void interruption(WorkitemType workItem, InterruptionType interruption) {
            try {
                server.registerInterruption(workItem, interruption);
                refreshWorkItem(findWorkItem(workItem));
            } catch (Exception ex) {
                Logger.getLogger(PomodoroClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void workCompleted(WorkitemType workItem, boolean successfully) {
            if (successfully) {
                // Always?
                statusMessageLabel.setText("Work ended successfully. Having rest now.");
                try {
                    server.completePomodoro(workItem);
                } catch (InvalidStateException ex) {
                    Logger.getLogger(PomodoroClientView.class.getName()).log(Level.SEVERE, null, ex);
                }
                refreshUiOnFinish(workItem);
//                tray.displayMessage("Time is up, having rest now.", "Working on \"" + workItem.getTitle() + "\"");
//                tray.changeState(TimerState.REST);
            }
        }

        public void ready(WorkitemType workItem) {
            // Not in pomodoro anymore
            statusMessageLabel.setText("");
            refreshUiOnFinish(workItem);

            switch (getTimerWindowMode()) {
                case NO_TRAY:
                    // Do not hide anything
                    break;
                case HIDE_MAIN_WINDOW_ONLY:
                    getFrame().setVisible(true);
                    getFrame().toFront();
                    break;
                case HIDE_EVERYTHING:
                    getFrame().setVisible(true);
                    getFrame().toFront();
                    break;
            }
//            tray.displayMessage("Rest ended, ready for new tasks.", "Working on \"" + workItem.getTitle() + "\"");
//            tray.changeState(TimerState.READY);
        }

        private void checkForMessages(WorkitemType workItem) throws InvalidStateException {
            if (!(server instanceof OfflineServerImpl)) {
                Messages msg = server.getMessages(workItem);
                if (msg != null && !msg.getMessage().isEmpty()) {
                    MessagesWindow w = new MessagesWindow(null, true, msg);
                    w.setVisible(true);
                }
            }
        }
    };

    private final org.flowkeeper.client.ui.timer.Timer timer;

    private int findWorkItem(WorkitemType wi) throws NotFoundException {
        return server.getPlan().getWorkitem().indexOf(wi);
    }

    private class SelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            setToolbarButtonsEnabledAuto();
        }
    }

    public PomodoroClientView(SingleFrameApplication app, Server server) {
        super(app);
        this.server = server;

        initComponents();
        getFrame().setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

        if (server instanceof OfflineServerImpl) {
            usersButton.setEnabled(false);
        }

        workItemTable.getSelectionModel().addListSelectionListener(new SelectionListener());

        createNewPlanButton.setVisible(false);
        deletePlanMenuItem.setEnabled(true);

        timer = new org.flowkeeper.client.ui.timer.Timer(server.getUser());

        // TODO: Tray doesn't seem to work in Gnome 3
        try {
            tray = new TraySupport(timer, this);
            timer.addListener(tray);
        } catch (Throwable t) {
            // Disable tray for this session by assigning null to tray
            tray = null;
        }

        try {
            soundListener = new SoundsTimerListener(timer);
            // TODO: Exception -- invalid format
            if (isSoundEnabled()) {
                timer.addListener(soundListener);
                soundListener.enable();
            }
        } catch (Throwable t) {
            // Disable sound for this session by assigning null to soundListener
            if (soundListener != null) {
                timer.removeListener(soundListener);
            }
            soundListener = null;
        }

        timerWindow = new TimerWindow(this, false, timer);
        timerWindow.addPomodoroListener(pomodoroListener);

        initTitle();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        Util.decorate(this.getFrame());
        checkForUpdates();
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = PomodoroClientApp.getApplication().getMainFrame();
            aboutBox = new PomodoroClientAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PomodoroClientApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        headerTextLabel = new javax.swing.JLabel();
        createNewPlanButton = new javax.swing.JButton();
        headerLogoLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        workItemTable = new javax.swing.JTable();
        toolBar = new javax.swing.JToolBar();
        addWorkItemButton = new javax.swing.JButton();
        completeWorkItemButton = new javax.swing.JButton();
        deleteWorkItemButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        startPomodoroButton = new javax.swing.JButton();
        addPomodoroButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        statisticsButton = new javax.swing.JButton();
        usersButton = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        deletePlanMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.flowkeeper.client.ui.PomodoroClientApp.class).getContext().getResourceMap(PomodoroClientView.class);
        jPanel1.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);

        headerPanel.setBackground(resourceMap.getColor("headerPanel.background")); // NOI18N
        headerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(resourceMap.getColor("headerPanel.border.lineColor"))); // NOI18N
        headerPanel.setName("headerPanel"); // NOI18N

        headerTextLabel.setFont(headerTextLabel.getFont().deriveFont(headerTextLabel.getFont().getSize()+7f));
        headerTextLabel.setText(resourceMap.getString("headerTextLabel.text")); // NOI18N
        headerTextLabel.setName("headerTextLabel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.flowkeeper.client.ui.PomodoroClientApp.class).getContext().getActionMap(PomodoroClientView.class, this);
        createNewPlanButton.setAction(actionMap.get("createPlan")); // NOI18N
        createNewPlanButton.setText(resourceMap.getString("createNewPlanButton.text")); // NOI18N
        createNewPlanButton.setName("createNewPlanButton"); // NOI18N

        headerLogoLabel.setIcon(resourceMap.getIcon("headerLogoLabel.icon")); // NOI18N
        headerLogoLabel.setText(resourceMap.getString("headerLogoLabel.text")); // NOI18N
        headerLogoLabel.setName("headerLogoLabel"); // NOI18N

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createNewPlanButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerLogoLabel))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerLogoLabel)
            .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(headerTextLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(createNewPlanButton))
        );

        jScrollPane1.setBackground(resourceMap.getColor("jScrollPane1.background")); // NOI18N
        jScrollPane1.setName("jScrollPane1"); // NOI18N
        jScrollPane1.setOpaque(false);

        workItemTable.setFont(workItemTable.getFont().deriveFont(workItemTable.getFont().getSize()+3f));
        workItemTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "Work Item Title", "Pomodoros"
            }
        ) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 3014814267627003947L;
			boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        workItemTable.setGridColor(resourceMap.getColor("workItemTable.gridColor")); // NOI18N
        workItemTable.setName("workItemTable"); // NOI18N
        workItemTable.setOpaque(false);
        workItemTable.setRowHeight(25);
        workItemTable.setRowMargin(0);
        workItemTable.setSelectionBackground(resourceMap.getColor("workItemTable.selectionBackground")); // NOI18N
        workItemTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(workItemTable);
        workItemTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        workItemTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("workItemTable.columnModel.title0")); // NOI18N
        workItemTable.getColumnModel().getColumn(0).setCellRenderer(new org.flowkeeper.client.ui.workitem.WorkItemStatusRenderer());
        workItemTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        workItemTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("workItemTable.columnModel.title1")); // NOI18N
        workItemTable.getColumnModel().getColumn(1).setCellRenderer(new org.flowkeeper.client.ui.workitem.WorkItemTitleRenderer());
        workItemTable.getColumnModel().getColumn(2).setPreferredWidth(20);
        workItemTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("workItemTable.columnModel.title2")); // NOI18N
        workItemTable.getColumnModel().getColumn(2).setCellRenderer(new PomodoroRenderer());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
        );

        toolBar.setRollover(true);
        toolBar.setName("toolBar"); // NOI18N

        addWorkItemButton.setAction(actionMap.get("addWorkItem")); // NOI18N
        addWorkItemButton.setIcon(resourceMap.getIcon("addWorkItemButton.icon")); // NOI18N
        addWorkItemButton.setText(resourceMap.getString("addWorkItemButton.text")); // NOI18N
        addWorkItemButton.setFocusable(false);
        addWorkItemButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addWorkItemButton.setName("addWorkItemButton"); // NOI18N
        addWorkItemButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(addWorkItemButton);

        completeWorkItemButton.setAction(actionMap.get("completeWorkItem")); // NOI18N
        completeWorkItemButton.setIcon(resourceMap.getIcon("completeWorkItemButton.icon")); // NOI18N
        completeWorkItemButton.setText(resourceMap.getString("completeWorkItemButton.text")); // NOI18N
        completeWorkItemButton.setFocusable(false);
        completeWorkItemButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        completeWorkItemButton.setName("completeWorkItemButton"); // NOI18N
        completeWorkItemButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(completeWorkItemButton);

        deleteWorkItemButton.setAction(actionMap.get("deleteWorkItem")); // NOI18N
        deleteWorkItemButton.setIcon(resourceMap.getIcon("deleteWorkItemButton.icon")); // NOI18N
        deleteWorkItemButton.setText(resourceMap.getString("deleteWorkItemButton.text")); // NOI18N
        deleteWorkItemButton.setFocusable(false);
        deleteWorkItemButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteWorkItemButton.setName("deleteWorkItemButton"); // NOI18N
        deleteWorkItemButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(deleteWorkItemButton);

        jSeparator2.setName("jSeparator2"); // NOI18N
        toolBar.add(jSeparator2);

        startPomodoroButton.setAction(actionMap.get("startPomodoro")); // NOI18N
        startPomodoroButton.setFont(resourceMap.getFont("startPomodoroButton.font")); // NOI18N
        startPomodoroButton.setIcon(resourceMap.getIcon("startPomodoroButton.icon")); // NOI18N
        startPomodoroButton.setText(resourceMap.getString("startPomodoroButton.text")); // NOI18N
        startPomodoroButton.setFocusable(false);
        startPomodoroButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startPomodoroButton.setName("startPomodoroButton"); // NOI18N
        startPomodoroButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(startPomodoroButton);

        addPomodoroButton.setAction(actionMap.get("addPomodoro")); // NOI18N
        addPomodoroButton.setIcon(resourceMap.getIcon("addPomodoroButton.icon")); // NOI18N
        addPomodoroButton.setText(resourceMap.getString("addPomodoroButton.text")); // NOI18N
        addPomodoroButton.setFocusable(false);
        addPomodoroButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addPomodoroButton.setName("addPomodoroButton"); // NOI18N
        addPomodoroButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(addPomodoroButton);

        jSeparator1.setName("jSeparator1"); // NOI18N
        toolBar.add(jSeparator1);

        statisticsButton.setAction(actionMap.get("displayStatistics")); // NOI18N
        statisticsButton.setIcon(resourceMap.getIcon("statisticsButton.icon")); // NOI18N
        statisticsButton.setText(resourceMap.getString("statisticsButton.text")); // NOI18N
        statisticsButton.setFocusable(false);
        statisticsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statisticsButton.setName("statisticsButton"); // NOI18N
        statisticsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(statisticsButton);

        usersButton.setAction(actionMap.get("displayUsers")); // NOI18N
        usersButton.setIcon(resourceMap.getIcon("usersButton.icon")); // NOI18N
        usersButton.setText(resourceMap.getString("usersButton.text")); // NOI18N
        usersButton.setFocusable(false);
        usersButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        usersButton.setName("usersButton"); // NOI18N
        usersButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(usersButton);

        settingsButton.setAction(actionMap.get("displaySettings")); // NOI18N
        settingsButton.setIcon(resourceMap.getIcon("settingsButton.icon")); // NOI18N
        settingsButton.setText(resourceMap.getString("settingsButton.text")); // NOI18N
        settingsButton.setFocusable(false);
        settingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsButton.setName("settingsButton"); // NOI18N
        settingsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(settingsButton);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        deletePlanMenuItem.setAction(actionMap.get("deletePlan")); // NOI18N
        deletePlanMenuItem.setText(resourceMap.getString("deletePlanMenuItem.text")); // NOI18N
        deletePlanMenuItem.setName("deletePlanMenuItem"); // NOI18N
        fileMenu.add(deletePlanMenuItem);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 400, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void addWorkItem() throws NotFoundException {
        AddWorkItemDialog dlg = new AddWorkItemDialog(getFrame(), true);
        dlg.setVisible(true);
        if (dlg.isCreated()) {
            DefaultTableModel m = (DefaultTableModel) workItemTable.getModel();
            WorkitemType wi = server.addWorkItem(dlg.getWorkItemTitle(), dlg.getPomodoros());
            m.addRow(new Object[]{wi, wi, wi});
        }
    }

    @Action
    public void deleteWorkItem() {
        // TODO: Add selection check here
        // TODO: Implement deleting on the server
        if (JOptionPane.showConfirmDialog(getFrame(), "Are you sure you want to delete this work item?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int offset = 0;
            for (int r : workItemTable.getSelectedRows()) {
                DefaultTableModel m = (DefaultTableModel) workItemTable.getModel();
                m.removeRow(r - (offset++));
            }
        }
    }

    @Action
    public void startPomodoro() throws NotFoundException, NoPomodorosLeftException {
        int i = workItemTable.getSelectedRow();
        WorkitemType wi = server.getPlan().getWorkitem().get(i);
        PomodoroType pomodoro = server.startNextPomodoro(wi);
        timerWindow.setWorkItem(wi);
        timerWindow.setVisible(true);
        statusMessageLabel.setText("In a Pomodoro now. Click here to see the timer.");
        statusMessageLabel.addMouseListener(showPomodoroMouseListener);
        timer.startPomodoro(pomodoro);
        setToolbarButtonsEnabledAuto();
        refreshWorkItem(i);
        updateTitle();

        switch (getTimerWindowMode()) {
            case NO_TRAY:
                // Do not hide anything
                break;
            case HIDE_MAIN_WINDOW_ONLY:
                this.getFrame().setVisible(false);
                timerWindow.setVisible(true);
                break;
            case HIDE_EVERYTHING:
                this.getFrame().setVisible(false);
                timerWindow.setVisible(false);
                break;
        }
//        tray.changeState(TimerState.BUSY);
    }

    public enum TimerWindowMode {
        NO_TRAY, HIDE_MAIN_WINDOW_ONLY, HIDE_EVERYTHING
    }

    private boolean isSoundEnabled() {
        return prefs.get("timerSounds", "Y").equals("Y");
    }

    private TimerWindowMode getTimerWindowMode() {
        String mode = prefs.get("timerWindowMode", "M");
        if (mode.equals("N") || tray == null) {
            return TimerWindowMode.NO_TRAY;
        } else if (mode.equals("M")) {
            return TimerWindowMode.HIDE_MAIN_WINDOW_ONLY;
        } else {
            return TimerWindowMode.HIDE_EVERYTHING;
        }
    }

    private void updateTitle() throws NotFoundException {
        PlanType plan = server.getPlan();
        String state = "new";
        if (plan.getStatus().equals(StatusType.FAILED)) {
            state = "failed";
        } else if (plan.getStatus().equals(StatusType.COMPLETED)) {
            state = "completed";
        } else if (plan.getStatus().equals(StatusType.STARTED)) {
            state = "started";
        }
        
        
        XMLGregorianCalendar date = plan.getDate();
        String dateStr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
        		date.toGregorianCalendar().getTime()
    		);
		headerTextLabel.setText("Plan for " + dateStr + " (" + state + ")");
    }

    private void initTitle() {
        try {
            updateTitle();
            populateTable();
            createNewPlanButton.setVisible(false);
            deletePlanMenuItem.setEnabled(true);
            setWorkItemActionsEnabled(true);
            setToolbarButtonsEnabledAuto();
//            tray.changeState(TimerState.READY);
        } catch (NotFoundException ex) {
        	ex.printStackTrace();
            headerTextLabel.setText("There is no plan for today");
            createNewPlanButton.setVisible(true);
            deletePlanMenuItem.setEnabled(false);
            setWorkItemActionsEnabled(false);
            setToolbarButtonsEnabledAuto();
            DefaultTableModel m = (DefaultTableModel) workItemTable.getModel();
            m.setRowCount(0);
        }
    }

    private void populateTable() throws NotFoundException {
        DefaultTableModel m = (DefaultTableModel) workItemTable.getModel();
        PlanType plan = server.getPlan();
        
        for (WorkitemType wi : plan.getWorkitem()) {
            m.addRow(new Object[]{wi, wi, wi});
        }
    }

    @Action
    public void createPlan() throws AlreadyExistsException, NotFoundException {
        server.createPlan();
        initTitle();
    }

    private void setWorkItemActionsEnabled(boolean enabled) {
        addWorkItemButton.setEnabled(enabled);
        deleteWorkItemButton.setEnabled(enabled);
        completeWorkItemButton.setEnabled(enabled);
        addPomodoroButton.setEnabled(enabled);
        statisticsButton.setEnabled(enabled);
    }

    private void setToolbarButtonsEnabledAuto() {
        try {
            int row = workItemTable.getSelectedRow();
            if (row == -1) {
                startPomodoroButton.setEnabled(false);
                if (tray != null) {
                    tray.pomodoroItemSetEnabled(false);
                }
                timerWindow.enableNextButton(false);
                addPomodoroButton.setEnabled(false);
                deleteWorkItemButton.setEnabled(false);
            } else {
                PlanType plan = server.getPlan();
                WorkitemType wi = plan.getWorkitem().get(row);
                boolean wiFinished = wi.getStatus().equals(StatusType.COMPLETED) || wi.getStatus().equals(StatusType.FAILED);
                boolean planFinished = plan.getStatus().equals(StatusType.COMPLETED) || plan.getStatus().equals(StatusType.FAILED);
                if (planFinished) {
                    // Everything is sealed
                    deleteWorkItemButton.setEnabled(false);
                    completeWorkItemButton.setEnabled(false);
                    addPomodoroButton.setEnabled(false);
                    addWorkItemButton.setEnabled(false);
                    startPomodoroButton.setEnabled(false);
                    if (tray != null) {
                        tray.pomodoroItemSetEnabled(false);
                    }
                    timerWindow.enableNextButton(false);
                } else {
                    completeWorkItemButton.setEnabled(!wiFinished);
                    addPomodoroButton.setEnabled(!wiFinished);
                    boolean pomodoroLeft = false;
                    for (PomodoroType p : wi.getPomodoro()) {
                        if (p.getStatus().equals(StatusType.NEW)) {
                            pomodoroLeft = true;
                            break;
                        }
                    }
                    boolean notInPomodoro = timer.getState().equals(TimerState.READY);
                    boolean enablePomodoroButton = !wiFinished && pomodoroLeft && notInPomodoro;
                    startPomodoroButton.setEnabled(enablePomodoroButton);
                    if (tray != null) {
                        tray.pomodoroItemSetEnabled(enablePomodoroButton);
                    }
                    timerWindow.enableNextButton(enablePomodoroButton);
                }
            }
        } catch (NotFoundException ex) {
            startPomodoroButton.setEnabled(false);
            if (tray != null) {
                tray.pomodoroItemSetEnabled(false);
            }
            timerWindow.enableNextButton(false);
        }
    }

    @Action
    public void addPomodoro() throws NotFoundException {
        int i = workItemTable.getSelectedRow();
        WorkitemType wi = server.getPlan().getWorkitem().get(i);
        // TODO: Check workitem status
        if (wi.getPomodoro().size() >= 4) {
            JOptionPane.showMessageDialog(getFrame(), "It is not advisable to assign more than\nfour pomodoros per each work item", "Pomodoro Technique Advice", JOptionPane.WARNING_MESSAGE);
        }
        server.addPomodoro(wi);
        refreshWorkItem(i);
    }

    private void refreshWorkItem(int index) throws NotFoundException {
        WorkitemType wi = server.getPlan().getWorkitem().get(index);
        workItemTable.getModel().setValueAt(wi, index, 0);
        workItemTable.getModel().setValueAt(wi, index, 1);
        workItemTable.getModel().setValueAt(wi, index, 2);
        setToolbarButtonsEnabledAuto();
    }

    @Action
    public void completeWorkItem() throws NotFoundException {
//        if (JOptionPane.showConfirmDialog(getFrame(), "Are you sure this work item is complete?\nYou will not be able to undo this operation.", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // TODO: Check if this w/i is not active at the moment
            int i = workItemTable.getSelectedRow();
            WorkitemType wi = server.getPlan().getWorkitem().get(i);
            server.completeWorkItem(wi);
            refreshWorkItem(i);
//        }
    }

    @Action
    public void displayStatistics() throws NotFoundException {
        new StatisticsWindow(null, true, server.getPlan()).setVisible(true);
    }

    @Action
    public void displayUsers() {
        new UsersWindow(null, true, server).setVisible(true);
    }

    @Action
    public void deletePlan() {
        if (JOptionPane.showConfirmDialog(getFrame(), "Are you sure you want to delete this plan? You will lose all data for today.", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                timer.voidCurrentPomodoro();
            } catch (IllegalStateException ex) {
                Logger.getLogger(PomodoroClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                server.deletePlan(server.getPlan());
                initTitle();
            } catch (Exception ex) {
                Logger.getLogger(PomodoroClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Action
    public void displaySettings() {
        SettingsWindow w = new SettingsWindow(getFrame(), true, server.getUser());
        w.setVisible(true);
        if (soundListener != null) {
            timer.removeListener(soundListener);
            soundListener.disable();
            if (isSoundEnabled()) {
                timer.addListener(soundListener);
                soundListener.enable();
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPomodoroButton;
    private javax.swing.JButton addWorkItemButton;
    private javax.swing.JButton completeWorkItemButton;
    private javax.swing.JButton createNewPlanButton;
    private javax.swing.JMenuItem deletePlanMenuItem;
    private javax.swing.JButton deleteWorkItemButton;
    private javax.swing.JLabel headerLogoLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel headerTextLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton settingsButton;
    private javax.swing.JButton startPomodoroButton;
    private javax.swing.JButton statisticsButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JButton usersButton;
    private javax.swing.JTable workItemTable;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
