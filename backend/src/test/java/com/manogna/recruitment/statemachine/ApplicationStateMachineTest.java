package com.manogna.recruitment.statemachine;

import com.manogna.recruitment.entity.ApplicationStatus;
import com.manogna.recruitment.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationStateMachineTest {

    private final ApplicationStateMachine machine = new ApplicationStateMachine();

    @Test
    void allowsApplyingToUnderReview() {
        assertDoesNotThrow(() ->
                machine.validateTransition(ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW));
    }

    @Test
    void allowsFullHappyPathToSelected() {
        assertDoesNotThrow(() -> {
            machine.validateTransition(ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW);
            machine.validateTransition(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SHORTLISTED);
            machine.validateTransition(ApplicationStatus.SHORTLISTED, ApplicationStatus.SELECTED);
        });
    }

    @Test
    void rejectsSkippingStates() {
        // APPLIED cannot jump straight to SELECTED - must go through review and shortlisting
        assertThrows(InvalidStateTransitionException.class, () ->
                machine.validateTransition(ApplicationStatus.APPLIED, ApplicationStatus.SELECTED));
    }

    @Test
    void rejectsTransitionOutOfTerminalState() {
        assertThrows(InvalidStateTransitionException.class, () ->
                machine.validateTransition(ApplicationStatus.SELECTED, ApplicationStatus.APPLIED));
        assertThrows(InvalidStateTransitionException.class, () ->
                machine.validateTransition(ApplicationStatus.REJECTED, ApplicationStatus.UNDER_REVIEW));
    }

    @Test
    void rejectsNoOpTransition() {
        assertThrows(InvalidStateTransitionException.class, () ->
                machine.validateTransition(ApplicationStatus.APPLIED, ApplicationStatus.APPLIED));
    }

    @Test
    void identifiesTerminalStatesCorrectly() {
        assertTrue(machine.isTerminal(ApplicationStatus.SELECTED));
        assertTrue(machine.isTerminal(ApplicationStatus.REJECTED));
        assertTrue(machine.isTerminal(ApplicationStatus.WITHDRAWN));
        assertFalse(machine.isTerminal(ApplicationStatus.APPLIED));
        assertFalse(machine.isTerminal(ApplicationStatus.UNDER_REVIEW));
    }

    @Test
    void allowsWithdrawalFromEitherOpenState() {
        assertDoesNotThrow(() -> machine.validateTransition(ApplicationStatus.APPLIED, ApplicationStatus.WITHDRAWN));
        assertDoesNotThrow(() -> machine.validateTransition(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.WITHDRAWN));
    }
}
