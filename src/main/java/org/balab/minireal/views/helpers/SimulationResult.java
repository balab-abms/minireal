package org.balab.minireal.views.helpers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulationResult {
    private final boolean success;
    private final double elapsedTime;
    private final String output;
    private final String error;
    private final String time_unit;

    public SimulationResult(boolean success, double elapsedTime, String output, String error) {
        this.success = success;
        this.elapsedTime = elapsedTime;
        this.output = output;
        this.error = error;
        this.time_unit = "minute";
    }
}

