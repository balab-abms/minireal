package org.balab.minireal.data.service;

import lombok.RequiredArgsConstructor;
import org.balab.minireal.data.entity.SimSession;
import org.balab.minireal.views.helpers.SImRelatedHelpers;
import org.balab.minireal.views.helpers.SimulationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.util.tuples.Pair;
import reactor.core.publisher.Sinks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class SimulationService
{
    private final SimSessionService sim_session_service;
    private final SImRelatedHelpers sim_helper_service;
    private final Sinks.Many<String> sim_session_del_publisher;
    private ConcurrentHashMap<String, Process> process_map = new ConcurrentHashMap<>();
    public SimulationResult runSimulation(String file_path, String model_params, SimSession sim_session, String comb_name) throws IOException, InterruptedException {
        boolean is_sim_sucess = false;
        String kafka_serializer_path = "minireal_data" + File.separator + "dependencies" + File.separator + "kafka_template.ser";
        ProcessBuilder processBuilder = null;
        if(comb_name != null){
            processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    file_path,
                    kafka_serializer_path,
                    model_params,
                    sim_session.getToken(),
                    comb_name);
        } else {
            processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    file_path,
                    kafka_serializer_path,
                    model_params,
                    sim_session.getToken());
        }

        // Start the timer before running the process
        long startTime = System.currentTimeMillis();

        Process process = processBuilder.start();
        process_map.put(sim_session.getToken(), process);

        // Capture the output stream
        StringBuilder outputBuilder = new StringBuilder();
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = outputReader.readLine()) != null) {
            System.out.println(line);
            outputBuilder.append(line).append("\n");
        }

        // Capture the error stream
        StringBuilder errorBuilder = new StringBuilder();
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
            errorBuilder.append(line).append("\n");
        }

        int exitCode = process.waitFor();

        // Stop the timer after process completion
        long endTime = System.currentTimeMillis();
        long elapsedTimeMs = endTime - startTime;
        // Convert milliseconds to minutes as a double and round to 2 decimal places
//        double minutes = elapsedTimeMs / 60000.0;
//        double roundedMinutes = Math.round(minutes * 100.0) / 100.0;
        // convert elapsed time to relevenat scale
        Pair<Double, String> elapsed_time_unit_pair = simElapsedTimeConverter(elapsedTimeMs);


        System.out.println("*** Elapsed time: " + elapsed_time_unit_pair.getA() + " " + elapsed_time_unit_pair.getB());

        System.out.println("Jar execution done ... exit code = " + exitCode);
        if(exitCode == 0) {
            is_sim_sucess = true;
        }

        process_map.remove(sim_session.getToken());

        // todo: kill all the threads on db and ui services related to this sim

        return new SimulationResult(
                is_sim_sucess,
                elapsed_time_unit_pair.getA(),
                elapsed_time_unit_pair.getB(),
                outputBuilder.toString(),
                errorBuilder.toString()
        );
    }

    public boolean stopSimulation(SimSession sim_session)
    {
        boolean is_sim_stopped = false;
        Process process = process_map.get(sim_session.getToken());
        if (process != null) {
            process.destroy();
            process_map.remove(sim_session.getToken());
            is_sim_stopped = true;
        }

        return is_sim_stopped;
    }

    // This method will be executed every 12 hours
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    public void simSessionCleanupTask() {
        // Your task here
        System.out.println("Running task Simulation session cleanup task.");
        // get all sim session tokens
        List<SimSession> all_sessions = sim_session_service.getAllSimSessions();
        for(SimSession session: all_sessions){
            try {
                LocalDateTime one_day_ago = LocalDateTime.now().minusDays(1);
                LocalDateTime three_day_ago = LocalDateTime.now().minusDays(3);
                LocalDateTime seven_day_ago = LocalDateTime.now().minusDays(7);
                // remove any session that was created a day ago but not updated
                if(session.getUpdated_at() == null && session.getCreated_at().isBefore(one_day_ago)){
                    sessionCleanupTask(session);
                } else if((session.is_completed() || session.is_failed()) && session.getUpdated_at().isBefore(one_day_ago)){
                    // check for completed or failed sessions and remove if more than a day since the last update
                    sessionCleanupTask(session);
                } else if(!session.is_running() && session.getUpdated_at().isBefore(three_day_ago)){
                    // check for not running sessions and remove if more than 3 days
                    sessionCleanupTask(session);
                } else if(session.getUpdated_at().isBefore(seven_day_ago)){
                    // remove any session that was updated more than a week ago
                    sessionCleanupTask(session);
                }

            } catch (NullPointerException exp){
                System.err.println(exp.getMessage());
            }
        }
    }

    private void sessionCleanupTask(SimSession session){
        sim_helper_service.deleteThreadsTopics(session.getToken());
        sim_session_del_publisher.emitNext(session.getToken(), (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
        sim_session_service.deleteSimSession(session);
    }

    private Pair<Double, String> simElapsedTimeConverter(long elapsedTimeMs)
    {
        double timeValue;
        String timeUnit;
        if (elapsedTimeMs < 1000) {
            // Less than 1 second, show in milliseconds
            timeValue = elapsedTimeMs;
            timeUnit = "ms";
        } else if (elapsedTimeMs < 60000) {
            // Less than 1 minute, show in seconds
            timeValue = elapsedTimeMs / 1000.0;
            timeUnit = "seconds";
        } else if (elapsedTimeMs < 3600000) {
            // Less than 1 hour, show in minutes
            timeValue = elapsedTimeMs / 60000.0;
            timeUnit = "minutes";
        } else if (elapsedTimeMs < 86400000) {
            // Less than 1 day, show in hours
            timeValue = elapsedTimeMs / 3600000.0;
            timeUnit = "hours";
        } else {
            // 1 day or more, show in days
            timeValue = elapsedTimeMs / 86400000.0;
            timeUnit = "days";
        }

        // Use BigDecimal to round the time value to 2 decimal places
        double roundedTimeValue = BigDecimal.valueOf(timeValue)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return new Pair<>(roundedTimeValue, timeUnit);
    }
}
