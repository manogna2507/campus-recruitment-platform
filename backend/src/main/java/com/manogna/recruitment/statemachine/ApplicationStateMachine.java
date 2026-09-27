package com.manogna.recruitment.statemachine;

import com.manogna.recruitment.entity.ApplicationStatus;
import com.manogna.recruitment.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Explicit finite-state machine for {@link ApplicationStatus}.
 *
 * Rather than letting any service set any status on an Application (which is
 * how most fresher projects handle "status fields"), every transition must be
 * validated here first. This is the single source of truth for what moves are
 * legal, which makes the business rule testable in isolation (see
 * ApplicationStateMachineTest) and impossible to bypass accidentally.
 *
 * Transition graph:
 *
 *   APPLIED --------> UNDER_REVIEW --------> SHORTLISTED --------> SELECTED
 *      |                    |                     |
 *      v                    v                     v
 *  WITHDRAWN            REJECTED              REJECTED
 *
 * SELECTED, REJECTED and WITHDRAWN are terminal: no further transitions are
 * allowed out of them.
 */
@Component
public class ApplicationStateMachine {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> TRANSITIONS = new EnumMap<>(ApplicationStatus.class);

    static {
        TRANSITIONS.put(ApplicationStatus.APPLIED, EnumSet.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.WITHDRAWN));
        TRANSITIONS.put(ApplicationStatus.UNDER_REVIEW, EnumSet.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN));
        TRANSITIONS.put(ApplicationStatus.SHORTLISTED, EnumSet.of(ApplicationStatus.SELECTED, ApplicationStatus.REJECTED));
        TRANSITIONS.put(ApplicationStatus.SELECTED, EnumSet.noneOf(ApplicationStatus.class));
        TRANSITIONS.put(ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class));
        TRANSITIONS.put(ApplicationStatus.WITHDRAWN, EnumSet.noneOf(ApplicationStatus.class));
    }

    /**
     * @throws InvalidStateTransitionException if moving from {@code current} to
     *                                          {@code next} is not a legal transition.
     */
    public void validateTransition(ApplicationStatus current, ApplicationStatus next) {
        if (current == next) {
            throw new InvalidStateTransitionException("Application is already in status " + current);
        }
        Set<ApplicationStatus> allowed = TRANSITIONS.getOrDefault(current, Collections.emptySet());
        if (!allowed.contains(next)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition application from " + current + " to " + next +
                            ". Allowed next states: " + allowed);
        }
    }

    public boolean isTerminal(ApplicationStatus status) {
        return TRANSITIONS.getOrDefault(status, Collections.emptySet()).isEmpty();
    }

    public Set<ApplicationStatus> allowedNextStates(ApplicationStatus current) {
        return Collections.unmodifiableSet(TRANSITIONS.getOrDefault(current, Collections.emptySet()));
    }
}
