package org.flowkeeper.client.ui;

import java.io.BufferedInputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.flowkeeper.client.ui.timer.Timer;
import org.flowkeeper.client.ui.timer.TimerListener;
import org.flowkeeper.client.ui.timer.TimerState;
import org.flowkeeper.server.PomodoroType;

public class SoundsTimerListener implements TimerListener {

    private final Clip clipTick;
    private final Clip clipEnd;
    private boolean active = true;
    private final Timer timer;
    private boolean firstTime = true;

    public SoundsTimerListener(Timer timer) {
        this.timer = timer;
        try {
            AudioInputStream ais = null;
            try {
                ais = AudioSystem.getAudioInputStream(
                		new BufferedInputStream(
                				getClass().getClassLoader().getResourceAsStream("sounds/tick.wav")
                			)
                	);
                clipTick = AudioSystem.getClip();
                clipTick.open(ais);
                clipTick.setLoopPoints(0, -1);
            } finally {
                if (ais != null) {
                    ais.close();
                }
            }
            try {
                ais = AudioSystem.getAudioInputStream(
                		new BufferedInputStream(
                				getClass().getClassLoader().getResourceAsStream("sounds/end.wav")
                			)
                	);
                clipEnd = AudioSystem.getClip();
                clipEnd.open(ais);
                clipEnd.setLoopPoints(0, -1);
            } finally {
                if (ais != null) {
                    ais.close();
                }
            }
        } catch (Throwable t) {
            throw new UnsupportedOperationException(t);
        }
    }

    public void onPomodoroStarted(PomodoroType pomodoro) {
        active = true;
        start();
    }

    private void playDing (){
        if (firstTime) {
            clipEnd.loop(0);
            firstTime = false;
        } else {
            clipEnd.loop(1);
        }
    }

    public void onWorkCompleted(PomodoroType pomodoro, boolean successfully) {
        active = false;
        stop();
        if (successfully) {
            playDing();
        }
    }

    public void onReady() {
        active = false;
        stop();
        playDing();
    }

    public void onTick() {
        if (!clipTick.isRunning() && active && timer.getState().equals(TimerState.BUSY)) {
            start();
        }
    }

    public void enable() {
        active = true;
    }

    public void disable() {
        active = false;
        stop();
    }

    private void stop() {
        clipTick.stop();
    }

    private void start() {
        clipTick.loop(Clip.LOOP_CONTINUOUSLY);
    }
}
