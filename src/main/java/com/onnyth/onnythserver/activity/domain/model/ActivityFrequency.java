package com.onnyth.onnythserver.activity.domain.model;

/**
 * Frequency at which an activity can be logged.
 * DAILY — once per day (or per cooldown period)
 * WEEKLY — once per week
 */
public enum ActivityFrequency {
    DAILY,
    WEEKLY
}
