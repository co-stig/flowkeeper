package org.flowkeeper.client.ui.timer;

import org.flowkeeper.server.PomodoroType;

public abstract class TimerAdapter implements TimerListener {

	@Override
	public void onWorkCompleted(PomodoroType pomodoro, boolean successfully) {
	}

	@Override
	public void onPomodoroStarted(PomodoroType pomodoro) {
	}

	@Override
	public void onReady() {
	}
	
	@Override
	public void onTick() {
	}
}
