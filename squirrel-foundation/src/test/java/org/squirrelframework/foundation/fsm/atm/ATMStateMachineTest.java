package org.squirrelframework.foundation.fsm.atm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.SCXMLVisitor;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.StringConverter;
import org.squirrelframework.foundation.fsm.atm.ATMStateMachine.ATMState;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;
import org.squirrelframework.foundation.util.TypeReference;

public class ATMStateMachineTest {
    
    @BeforeClass
    public static void beforeTest() {
        ConverterProvider.INSTANCE.register(ATMState.class, new Converter.EnumConverter<ATMState>(ATMState.class));
        ConverterProvider.INSTANCE.register(String.class, StringConverter.INSTANCE);
    }
    
    @AfterClass
    public static void afterTest() {
        ConverterProvider.INSTANCE.clearRegistry();
        SquirrelPostProcessorProvider.getInstance().clearRegistry();
    }
    
    private ATMStateMachine stateMachine;
    
    @Before
    public void setup() {
        StateMachineBuilder<ATMStateMachine, ATMState, String, Void> builder = StateMachineBuilderImpl.
                newStateMachineBuilder(ATMStateMachine.class, ATMState.class, String.class, Void.class);
        builder.externalTransition().from(ATMState.Idle).to(ATMState.Loading).on("Connected");
        builder.externalTransition().from(ATMState.Loading).to(ATMState.Disconnected).on("ConnectionClosed");
        builder.externalTransition().from(ATMState.Loading).to(ATMState.InService).on("LoadSuccess");
        builder.externalTransition().from(ATMState.Loading).to(ATMState.OutOfService).on("LoadFail");
        builder.externalTransition().from(ATMState.OutOfService).to(ATMState.Disconnected).on("ConnectionLost");
        builder.externalTransition().from(ATMState.OutOfService).to(ATMState.InService).on("Startup");
        builder.externalTransition().from(ATMState.InService).to(ATMState.OutOfService).on("Shutdown");
        builder.externalTransition().from(ATMState.InService).to(ATMState.Disconnected).on("ConnectionLost");
        builder.externalTransition().from(ATMState.Disconnected).to(ATMState.InService).on("ConnectionRestored");
        
        stateMachine = builder.newStateMachine(ATMState.Idle);
    }
    
    @After
    public void teardown() {
        if(stateMachine!=null && stateMachine.getStatus()!=StateMachineStatus.TERMINATED) {
            stateMachine.terminate(null);
        }
    }
    
    @Test
    public void testIdelToInService() {
        stateMachine.start(null);
        assertThat(stateMachine.consumeLog(), is(equalTo("entryIdle")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Idle)));
        
        stateMachine.fire("Connected", null);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitIdle.transitFromIdleToLoadingOnConnected.entryLoading")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Loading)));
        
        stateMachine.fire("LoadSuccess", null);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitLoading.transitFromLoadingToInServiceOnLoadSuccess.entryInService")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.InService)));
        
        stateMachine.fire("Shutdown", null);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitInService.transitFromInServiceToOutOfServiceOnShutdown.entryOutOfService")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.OutOfService)));
        
        stateMachine.fire("ConnectionLost", null);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitOutOfService.transitFromOutOfServiceToDisconnectedOnConnectionLost.entryDisconnected")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Disconnected)));
        
        stateMachine.fire("ConnectionRestored", null);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitDisconnected.transitFromDisconnectedToInServiceOnConnectionRestored.entryInService")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.InService)));
    }
    
    @Test
    public void exportATMStateMachine() {
        SCXMLVisitor<ATMStateMachine, ATMState, String, Void> visitor = SquirrelProvider.getInstance().newInstance(
                new TypeReference<SCXMLVisitor<ATMStateMachine, ATMState, String, Void>>() {} );
        stateMachine.accept(visitor);
        visitor.convertSCXMLFile("ATMStateMachine", true);
    }

}
