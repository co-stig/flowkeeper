package org.flowkeeper.server.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;

import org.flowkeeper.server.PlanType;
import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.StatusType;
import org.flowkeeper.server.UserType;
import org.flowkeeper.server.Users;
import org.flowkeeper.server.WorkitemType;

/**
 *
 * 
 */
public class OfflineServerImpl extends GenericServer {

    private Date lastFlushDate = new Date();
    private int MAX_FLUSH_INTERVAL = 30;    // Seconds
    private static final String DATA_FILENAME = "flowkeeper.xml";
    
    private void fixStartedPomodoros() {
        for (PlanType p: user.getPlan()) {
            for (WorkitemType w: p.getWorkitem()) {
                for (PomodoroType o: w.getPomodoro()) {
                    if (o.getStatus().equals(StatusType.STARTED)) {
                        o.setStatus(StatusType.FAILED);
                    }
                }
            }
        }
    }

    public OfflineServerImpl() throws IOException, LoginException {
        InputStream is = new BufferedInputStream(new FileInputStream(getDataFile()));
        try {
            users = JAXB.unmarshal(is, Users.class);
            user = users.getUser().get(0);
            fixStartedPomodoros();
        } finally {
            is.close();
        }
    }

    public void logout() {
        flush(true);
    }

    public void sendMessage(String userId, String message) throws InvalidStateException {
        throw new UnsupportedOperationException("Sending messages is not supported in offline mode");
    }

    private File getDataFile() {
        try {
            String fn = System.getProperty("user.home") + System.getProperty("file.separator") + DATA_FILENAME;
            File f = new File(fn);
            if (!f.exists()) {
            	flush(f, initialUsers());
            }
            return f;
        } catch (Throwable t) {
            throw new RuntimeException("Fatal error while accessing data file", t);
        }
    }

	private Users initialUsers() {
		UserType user = new UserType();
		user.setBreakLength(5);
		user.setId("user");
		user.setName("Flowkeeper User");
		user.setPomodoroLength(25);
		user.setRegdate(ServerUtilities.dateToXmlGregorianCalendar(Calendar.getInstance()));
		user.setPassword("");
		
		Users users = new Users();
		users.getUser().add(user);
		return users;
	}

    // *************************************** Flushing logic ***************************************
	
    private void flush(File file, Users users) {
    	BufferedOutputStream bos = null;
    	try {
    		bos = new BufferedOutputStream(new FileOutputStream(file));
    		JAXB.marshal(users, bos);
    		lastFlushDate = new Date();
    	} catch (FileNotFoundException ex) {
    		Logger.getLogger(OfflineServerImpl.class.getName()).log(Level.SEVERE, null, ex);
    	} finally {
    		if (bos != null) {
    			try {
    				bos.close();
    			} catch (IOException ex) {
    				Logger.getLogger(OfflineServerImpl.class.getName()).log(Level.SEVERE, null, ex);
    			}
    		}
    	}
    }
    
	private long getTimeSinceLastFlush() {
		return System.currentTimeMillis() - lastFlushDate.getTime();
	}

    private synchronized void flush(boolean force) {
        if (force || getTimeSinceLastFlush() >= MAX_FLUSH_INTERVAL * 1000) {
            flush(getDataFile(), users);
        }
    }

	@Override
	public void flush() {
		flush(false);
	}
}
