package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.squirrelframework.foundation.component.impl.AbstractSubject;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ActionExecutor;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.util.ReflectUtils;

import com.google.common.base.Preconditions;

class ActionExecutorImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractSubject implements ActionExecutor<T, S, E, C> {
	
	protected final Stack<List<ExectionContext<T, S, E, C>>> stack = new Stack<List<ExectionContext<T, S, E, C>>>();
	
	@Override
    public void begin() {
		List<ExectionContext<T, S, E, C>> executionContext = new ArrayList<ExectionContext<T, S, E, C>>();
        stack.push(executionContext);
    }

	@Override
    public void execute() {
		List<ExectionContext<T, S, E, C>> executionContexts = stack.pop();
        for (int i=0, size=executionContexts.size(); i<size; ++i) {
        	fireEvent(ExecActionEventImpl.get(i+1, size, executionContexts.get(i)));
        	executionContexts.get(i).run();
        }
    }

	@Override
    public void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
		Preconditions.checkNotNull(action);
        stack.peek().add(ExectionContext.get(action, from, to, event, context, stateMachine));
    }
	
	private static final Method EXECUTOR_EVENT_METHOD = 
            ReflectUtils.getMethod(ExecActionLisenter.class, "beforeExecute", new Class<?>[]{ExecActionEvent.class});
	
	@Override
    public void addListener(ExecActionLisenter<T, S, E, C> listener) {
		addListener(ExecActionEvent.class, listener, EXECUTOR_EVENT_METHOD);
    }
	
	@Override
	public void removeListener(ExecActionLisenter<T, S, E, C> listener) {
		removeListener(ExecActionEvent.class, listener);
	}
	
	static class ExecActionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements ExecActionEvent<T, S, E, C> {
		private ExectionContext<T, S, E, C> executionContext;
		private int pos;
		private int size;
		
		ExecActionEventImpl(int pos, int size, ExectionContext<T, S, E, C> executionContext) {
			this.pos = pos;
			this.size = size;
			this.executionContext = executionContext;
		}
		
		static <T extends StateMachine<T, S, E, C>, S, E, C> ExecActionEvent<T, S, E, C> get(
				int pos, int size, ExectionContext<T, S, E, C> executionContext) {
			return new ExecActionEventImpl<T, S, E, C>(pos, size, executionContext);
		}

		@Override
        public Action<T, S, E, C> getExecutionTarget() {
	        return executionContext.action;
        }

		@Override
        public S getFrom() {
	        return executionContext.from;
        }

		@Override
        public S getTo() {
	        return executionContext.to;
        }

		@Override
        public E getEvent() {
	        return executionContext.event;
        }

		@Override
        public C getContext() {
	        return executionContext.context;
        }

		@Override
        public T getStateMachine() {
	        return executionContext.stateMachine;
        }

		@Override
        public int[] getMOfN() {
	        return new int[]{pos, size};
        }
	}
	
	private static class ExectionContext<T extends StateMachine<T, S, E, C>, S, E, C> {
		final Action<T, S, E, C> action;
		final S from;
		final S to;
		final E event;
		final C context;
		final T stateMachine;
		
		private ExectionContext(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
			this.action = action;
			this.from = from;
			this.to = to;
			this.event = event;
			this.context = context;
			this.stateMachine = stateMachine;
		}
		
		static <T extends StateMachine<T, S, E, C>, S, E, C> ExectionContext<T, S, E, C> get(
				Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
			return new ExectionContext<T, S, E, C>(action, from, to, event, context, stateMachine);
		}

		public void run() {
			action.execute(from, to, event, context, stateMachine);
		}
	}
}
