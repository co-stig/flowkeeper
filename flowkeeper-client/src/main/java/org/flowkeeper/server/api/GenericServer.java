package org.flowkeeper.server.api;

import java.util.Calendar;

import org.flowkeeper.server.InterruptionType;
import org.flowkeeper.server.Messages;
import org.flowkeeper.server.PlanType;
import org.flowkeeper.server.Plans;
import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.StatusType;
import org.flowkeeper.server.UserType;
import org.flowkeeper.server.Users;
import org.flowkeeper.server.WorkitemType;

public abstract class GenericServer implements Server {

	protected UserType user;
    protected Users users;

    // *************************************** Read-only methods ***************************************
    
    public PlanType getPlan(Calendar date) throws NotFoundException {
        for (PlanType p: user.getPlan()) {
            if (ServerUtilities.sameDay(date, p.getDate().toGregorianCalendar())) {
                return p;
            }
        }
        throw new NotFoundException("Plan for given date (" + date + ") not found.");
    }

    public PlanType getPlan() throws NotFoundException {
        return getPlan(getToday());
    }

    protected Calendar getToday () {
        return Calendar.getInstance();
    }

    public UserType getUser() {
        return user;
    }
    
    public Plans getPlans() {
        Plans plans = new Plans();
        plans.getPlan().addAll(user.getPlan());
        return plans;
    }

    public Users getAllUsers() {
        return users;
    }

    // *************************************** Modifying methods ***************************************
    
    public PomodoroType addPomodoro(WorkitemType wi) throws NotFoundException {
    	try {
	        PomodoroType p = new PomodoroType();
	        p.setPlanned(getPlan().getStatus().equals(StatusType.NEW));
	        p.setStatus(StatusType.NEW);
	        wi.getPomodoro().add(p);
	        return p;
    	} finally {
    		flush();
    	}
    }

    public WorkitemType addWorkItem(String workItemTitle, int pomodoros) throws NotFoundException {
    	try {
	        WorkitemType wi = new WorkitemType();
	        boolean planned = getPlan().getStatus().equals(StatusType.NEW);
	        wi.setSection(planned ? "planned" : "unplanned");
	        wi.setStatus(StatusType.NEW);
	        wi.setTitle(workItemTitle);
	        for (int i = 0; i < pomodoros; ++i) {
	            PomodoroType p = new PomodoroType();
	            p.setPlanned(true);
	            p.setStatus(StatusType.NEW);
	            wi.getPomodoro().add(p);
	        }
	        getPlan().getWorkitem().add(wi);
	        return wi;
    	} finally {
    		flush();
    	}
    }

    public void completeWorkItem(WorkitemType wi) {
    	try {
    		wi.setStatus(StatusType.COMPLETED);
    	} finally {
    		flush();
    	}
    }
    
    public void registerInterruption(WorkitemType workItem, InterruptionType interruption) throws InvalidStateException {
    	try {
	        for (PomodoroType p: workItem.getPomodoro()) {
	            if (p.getStatus().equals(StatusType.STARTED)) {
	                p.getInterruption().add(interruption);
	                // There should be only one started pomodoro at a time
	                return;	
	            }
	        }
	        
	        throw new InvalidStateException("Unable to register interruption because work item is not started");
		} finally {
			flush();
		}
    }

    public void voidPomodoro(WorkitemType workItem) throws InvalidStateException {
    	try {
	        for (PomodoroType p: workItem.getPomodoro()) {
	            if (p.getStatus().equals(StatusType.STARTED)) {
	                p.setStatus(StatusType.FAILED);
	                // There should be only one started pomodoro at a time
	                return;
	            }
	        }
	        
	        throw new InvalidStateException("Unable to void pomodoro because work item is not started");
		} finally {
			flush();
		}
    }
    
    public Messages getMessages(WorkitemType workItem) throws InvalidStateException {
    	try {
	        for (PomodoroType p: workItem.getPomodoro()) {
	            if (p.getStatus().equals(StatusType.STARTED)) {
			        Messages m = new Messages();
			        m.getMessage().addAll(p.getMessage());
	                // There should be only one started pomodoro at a time
			        return m;
	            }
	        }
	        
	        throw new InvalidStateException("Unable to get messages because work item is not started");
    	} finally {
    		flush();
    	}
    }

    public void completePomodoro(WorkitemType workItem) throws InvalidStateException {
    	try {
	        for (PomodoroType p: workItem.getPomodoro()) {
	            if (p.getStatus().equals(StatusType.STARTED)) {
			        p.setFinish(ServerUtilities.dateToXmlGregorianCalendar(getToday()));
			        p.setStatus(StatusType.COMPLETED);
	                // There should be only one started pomodoro at a time
			        return;
	            }
	        }
	        
	        throw new InvalidStateException("Unable to complete pomodoro because work item is not started");
    	} finally {
    		flush();
    	}
    }

    public PlanType createPlan() throws AlreadyExistsException {
    	try {
	        PlanType res = new PlanType();
	        res.setStatus(StatusType.NEW);
	        res.setDate(ServerUtilities.dateToXmlGregorianCalendar(getToday()));
	        user.getPlan().add(res);
	        return res;
    	} finally {
    		flush();
    	}
    }

    public PomodoroType startNextPomodoro(WorkitemType wi) throws NotFoundException, NoPomodorosLeftException {
    	try {
	        for (PomodoroType p: wi.getPomodoro()) {
	            if (p.getStatus().equals(StatusType.NEW)) {
	            	p.setStart(ServerUtilities.dateToXmlGregorianCalendar(getToday()));
	                p.setStatus(StatusType.STARTED);
	                wi.setStatus(StatusType.STARTED);
	                getPlan().setStatus(StatusType.STARTED);
	                return p;
	            }
	        }
	        throw new NoPomodorosLeftException();
    	} finally {
    		flush();
    	}
    }
    
    public void deletePlan(PlanType plan) {
    	try {
    		user.getPlan().remove(plan);
    	} finally {
    		flush();
    	}
    }
    
    public abstract void flush();
}
