package org.flowkeeper.server.api;

import java.util.Calendar;

import org.flowkeeper.server.InterruptionType;
import org.flowkeeper.server.Messages;
import org.flowkeeper.server.PlanType;
import org.flowkeeper.server.Plans;
import org.flowkeeper.server.PomodoroType;
import org.flowkeeper.server.UserType;
import org.flowkeeper.server.Users;
import org.flowkeeper.server.WorkitemType;

/**
 *
 * 
 */
public interface Server {

    Users getAllUsers();
    UserType getUser();

    PomodoroType addPomodoro(WorkitemType wi) throws NotFoundException;
    void voidPomodoro(WorkitemType workItem) throws InvalidStateException;
    void completePomodoro(WorkitemType workItem) throws InvalidStateException;
    PomodoroType startNextPomodoro(WorkitemType wi) throws NotFoundException, NoPomodorosLeftException;

    WorkitemType addWorkItem(String workItemTitle, int pomodoros) throws NotFoundException;
    void completeWorkItem(WorkitemType wi);
    
    Plans getPlans();
    PlanType createPlan() throws AlreadyExistsException;
    PlanType getPlan(Calendar date) throws NotFoundException;
    PlanType getPlan() throws NotFoundException;

    void registerInterruption(WorkitemType workItem, InterruptionType interruption) throws InvalidStateException;
    void sendMessage(String userId, String message) throws InvalidStateException;

    Messages getMessages(WorkitemType workItem) throws InvalidStateException;

    void logout();

    void deletePlan(PlanType plan);
    
}
