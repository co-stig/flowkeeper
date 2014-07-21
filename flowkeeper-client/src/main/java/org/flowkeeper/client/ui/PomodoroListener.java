package org.flowkeeper.client.ui;

import org.flowkeeper.client.ui.timer.Timer;
import org.flowkeeper.client.ui.timer.TimerListener;
import org.flowkeeper.server.InterruptionType;
import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.WorkitemType;

/**
 * <p>
 * Classes implementing this interface will be notified about Pomodoro lifecycle
 * events, such as interruption, completion, etc. Those should not be confused
 * with the timer events (see {@link TimerListener}), such as begin, end and
 * tick.
 * 
 * <p>
 * The user controls Pomodoro lifecycle through {@link TimerWindow}, therefore
 * it is a natural place to register and fire those listeners (see
 * {@link TimerWindow#addPomodoroListener(PomodoroListener)}).
 * 
 * <p>
 * Typically the listeners do the following:
 * <ul>
 * <li>Refresh UI (enable / disable buttons, update status bar, etc)</li>
 * <li>Modify the underlying data model</li>
 * <li>Stop the timer, if the user voids the Pomodoro</li>
 * </ul>
 */
public interface PomodoroListener {

	/**
	 * This will be fired when the user clicks "Void" on a currently ticking
	 * Pomodoro. It is fired before the underlying data model is modified, thus
	 * it is easy to find the Pomodoro object from the provided Workitem --
	 * there should be only one active Pomodoro, and this one is getting void.
	 * 
	 * @param workItem
	 *            Workitem containing the Pomodoro being void
	 */
	void pomodoroVoid(WorkitemType workItem);

	/**
	 * This will be fired when the user clicks "Interrupt" on a currently
	 * ticking Pomodoro. It is fired before the underlying data model is
	 * modified.
	 * 
	 * @param workItem
	 *            Workitem containing the Pomodoro being interrupted
	 * @param interruption
	 *            Interruption details the user input in
	 *            {@link InterruptionDialog}
	 */
	void interruption(WorkitemType workItem, InterruptionType interruption);

	/**
	 * This will be fired as a result of the current {@link Timer}'s
	 * {@link TimerListener#onReady()} event, i.e. in two cases:
	 * 
	 * <ul>
	 * <li>The work <b>and rest</b> for the current Pomodoro are done</li>
	 * <li>The currently ticking Pomodoro was void</li>
	 * </ul>
	 * 
	 * Basically, it says that we are not in the Pomodoro anymore, thus UI
	 * should to be brought forward and enabled.
	 * 
	 * @param workItem
	 *            Workitem containing the Pomodoro which is ready
	 */
	void ready(WorkitemType workItem);

	/**
	 * This will be fired as a result of the current {@link Timer}'s
	 * {@link TimerListener#onWorkCompleted(PomodoroType, boolean)} event, i.e.
	 * when the work duration had elapsed and the rest period started.
	 * 
	 * @param workItem
	 *            Workitem containing the Pomodoro
	 * @param successfully
	 *            Always true
	 */
	void workCompleted(WorkitemType workItem, boolean successfully);

	// TODO: Remove "successfully" parameter, it is always true anyway
	// TODO: The data model is actually modified inside the listener, not
	// before. Supply Pomodoro object to be able to find the changed item, and
	// fix the javadocs.
}
