package com.manogna.recruitment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on @Scheduled methods (see JobExpiryScheduler). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
