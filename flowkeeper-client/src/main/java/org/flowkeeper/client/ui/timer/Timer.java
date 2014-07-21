package org.flowkeeper.client.ui.timer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.UserType;

public class Timer {

    private TimerState state;
    private PomodoroType currentPomodoro;
    private Date stateEnterTime;
    private Date projectedFinishTime;
    private final UserType user;
    private final Set<TimerListener> listeners = new HashSet<TimerListener>();
    private java.util.Timer timer;
    private java.util.Timer tickTimer;
    public final static long TICK_PERIOD = 1000;

    public Timer(UserType user) {
        this.user = user;
        toReadyState();
    }

    private synchronized void toReadyState() {
        if (tickTimer != null) {
            tickTimer.cancel();
            tickTimer = null;
        }
        state = TimerState.READY;
        stateEnterTime = new Date();
        timer = new java.util.Timer();
        tickTimer = new java.util.Timer();
        for (TimerListener l : listeners) {
            l.onReady();
        }
    }

    private synchronized void toRestState(boolean successfully) {
        state = TimerState.REST;
        stateEnterTime = new Date();
        projectedFinishTime = new Date(stateEnterTime.getTime() + user.getBreakLength() * 60 * 1000);
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (user) {
                    toReadyState();
                }
            }
        }, projectedFinishTime);
        tickTimer.cancel();
        tickTimer = new java.util.Timer();
        tickTimer.schedule(new TimerTask() {
            public void run() {
                synchronized (user) {
                    tick();
                }
            }
        }, 0, TICK_PERIOD);
        for (TimerListener l : listeners) {
            l.onWorkCompleted(currentPomodoro, successfully);
        }
    }

    private synchronized void tick() {
        for (TimerListener l : listeners) {
            l.onTick();
        }
    }

    private synchronized void toBusyState(PomodoroType pomodoro) {
        state = TimerState.BUSY;
        stateEnterTime = new Date();
        currentPomodoro = pomodoro;
        projectedFinishTime = new Date(stateEnterTime.getTime() + user.getPomodoroLength() * 60 * 1000);
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (user) {
                    toRestState(true);
                }
            }
        }, projectedFinishTime);
        tickTimer.schedule(new TimerTask() {
            public void run() {
                synchronized (user) {
                    tick();
                }
            }
        }, 0, TICK_PERIOD);
        for (TimerListener l : listeners) {
            l.onPomodoroStarted(pomodoro);
        }
    }

    public synchronized void voidCurrentPomodoro() {
        if (state == TimerState.BUSY) {
            timer.cancel();
            timer = null;
            tickTimer.cancel();
            tickTimer = null;
            toReadyState();
        } else {
            throw new IllegalStateException("Must be in BUSY state to void Pomodoro");
        }
    }

    public synchronized void startPomodoro(PomodoroType pomodoro) {
        if (state == TimerState.READY) {
            toBusyState(pomodoro);
        } else {
            throw new IllegalStateException("Must be in READY state to start Pomodoro");
        }
    }

    public synchronized boolean addListener(TimerListener listener) {
        return listeners.add(listener);
    }

    public synchronized boolean removeListener(TimerListener listener) {
        return listeners.remove(listener);
    }

    public synchronized TimerState getState() {
        return state;
    }

    public synchronized Date getStateEnterTime() {
        return stateEnterTime;
    }

    public synchronized UserType getUser() {
        return user;
    }

    public synchronized long getTimeLeft() {
        if (state != TimerState.READY) {
            return projectedFinishTime.getTime() - new Date().getTime();
        } else {
            throw new IllegalStateException("Timer must be started");
        }
    }

    public synchronized Date getProjectedFinishTime() {
        if (state != TimerState.READY) {
            return projectedFinishTime;
        } else {
            throw new IllegalStateException("Timer must be started");
        }
    }

    public synchronized PomodoroType getCurrentPomodoro() {
        if (state != TimerState.READY) {
            return currentPomodoro;
        } else {
            throw new IllegalStateException("Timer must be started");
        }
    }
}
