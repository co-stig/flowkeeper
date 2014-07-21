package org.flowkeeper.client.ui.timer;

import org.flowkeeper.server.PomodoroType;

public interface TimerListener {

    void onPomodoroStarted(PomodoroType pomodoro);
    void onWorkCompleted(PomodoroType pomodoro, boolean successfully);
    void onReady();
    void onTick();

}
