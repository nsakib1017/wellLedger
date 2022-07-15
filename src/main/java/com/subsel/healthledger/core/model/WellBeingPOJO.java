package com.subsel.healthledger.core.model;

import lombok.Data;

@Data
public class WellBeingPOJO {
    private final String uname;
    private final String type;
    private final String stepsCount;
    private final String calorieBurnt;
    private final String calorieIntake;
    private final String sleepTime;
    private final String date;
    private final String heartRate;
    private final String orgMsp;
}
