package org.squirrelframework.foundation.fsm;

import java.util.List;

import org.squirrelframework.foundation.component.Observable;
import org.squirrelframework.foundation.event.SquirrelEvent;
import org.squirrelframework.foundation.fsm.ActionExecutor.ExecActionLisenter;

/**
 * Interface for finite state machine.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface StateMachine<T extends StateMachine<T, S, E, C>, S, E, C> extends Visitable<T, S, E, C>, Observable {
    
	/**
	 * Fires the specified event
	 * @param event the event
	 * @param context external context
	 */
    void fire(E event, C context);
    
    /**
     * @return current state id of state machine
     */
    S getCurrentState();
    
    /**
     * @param stateId the identify of state 
     * @return raw state of the same state identify
     */
    ImmutableState<T, S, E, C> getRawStateFrom(S stateId);
    
    /**
     * @return current raw state of state machine
     */
    ImmutableState<T, S, E, C> getCurrentRawState();
    
    /**
     * @param parentStateId id of parent state
     * @return last active child state of the parent state
     */
    S getLastActiveChildStateOf(S parentStateId);
    
    /**
     * Set last active child state of parent state
     * @param parentStateId id of parent state
     * @param childStateId id of child state
     */
    void setLastActiveChildState(S parentStateId, S childStateId);
    
    List<S> getSubStatesOn(S parentState);
    
    void setSubState(S parentState, S subState);
    
    void removeSubState(S parentState, S subState);
    
    void removeSubStatesOn(S parentState);
    
    /**
     * @return id of state machine initial state
     */
    S getInitialState();
    
    /**
     * Start state machine under external context
     * @param context external context
     */
    void start(C context);
    
    /**
     * Terminate state machine under external context
     * @param context external context
     */
    void terminate(C context);
    
    /**
     * Terminate state machine under external context without exiting any states
     * @param context external context
     */
    void terminateWithoutExitStates(C context);
    
    /**
     * @return current status of state machine
     */
    StateMachineStatus getStatus();
    
    interface StateMachineListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void stateMachineEvent(StateMachineEvent<T, S, E, C> event);
    }
    interface StateMachineEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelEvent {
        T getStateMachine();
    }
    void addListener(StateMachineListener<T, S, E, C> listener);
    void removeListener(StateMachineListener<T, S, E, C> listener);
    
    interface StartListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void started(StartEvent<T, S, E, C> event);
    }
    interface StartEvent <T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {}
    void addListener(StartListener<T, S, E, C> listener);
    void removeListener(StartListener<T, S, E, C> listener);
    
    interface TerminateListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void terminated(TerminateEvent<T, S, E, C> event);
    }
    interface TerminateEvent <T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {}
    void addListener(TerminateListener<T, S, E, C> listener);
    void removeListener(TerminateListener<T, S, E, C> listener);
    
    interface StateMachineExceptionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void stateMachineException(StateMachineExceptionEvent<T, S, E, C> event);
    }
    interface StateMachineExceptionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {
        Exception getException();
    }
    void addListener(StateMachineExceptionListener<T, S, E, C> listener);
    void removeListener(StateMachineExceptionListener<T, S, E, C> listener);
    
    interface TransitionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {
        S getSourceState();
        E getCause();
        C getContext();
    }
    
    interface TransitionBeginListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionBegin(TransitionBeginEvent<T, S, E, C> event);
    }
    interface TransitionBeginEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {}
    void addListener(TransitionBeginListener<T, S, E, C> listener);
    void removeListener(TransitionBeginListener<T, S, E, C> listener);
    
    interface TransitionCompleteListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionComplete(TransitionCompleteEvent<T, S, E, C> event);
    }
    interface TransitionCompleteEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {
        S getTargetState();
    }
    void addListener(TransitionCompleteListener<T, S, E, C> listener);
    void removeListener(TransitionCompleteListener<T, S, E, C> listener);
    
    interface TransitionExceptionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionException(TransitionExceptionEvent<T, S, E, C> event);
    }
    interface TransitionExceptionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {
        S getTargetState();
        Exception getException();
    }
    void addListener(TransitionExceptionListener<T, S, E, C> listener);
    void removeListener(TransitionExceptionListener<T, S, E, C> listener);
    
    interface TransitionDeclinedListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionDeclined(TransitionDeclinedEvent<T, S, E, C> event);
    }
    interface TransitionDeclinedEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {}
    void addListener(TransitionDeclinedListener<T, S, E, C> listener);
    void removeListener(TransitionDeclinedListener<T, S, E, C> listener);
    
    void addListener(ExecActionLisenter<T, S, E, C> listener);
	
	void removeListener(ExecActionLisenter<T, S, E, C> listener);
}
