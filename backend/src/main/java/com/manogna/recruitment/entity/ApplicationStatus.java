package com.manogna.recruitment.entity;

/**
 * The states an Application can be in. Valid transitions between these are
 * enforced centrally by {@link com.manogna.recruitment.statemachine.ApplicationStateMachine}
 * rather than scattered across services - this is the "state machine" referenced
 * on the resume.
 */
public enum ApplicationStatus {
    APPLIED,
    UNDER_REVIEW,
    SHORTLISTED,
    SELECTED,
    REJECTED,
    WITHDRAWN
}
