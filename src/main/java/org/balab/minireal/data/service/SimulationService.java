package org.balab.minireal.data.service;

import org.balab.minireal.data.entity.SimSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationService
{
    private ConcurrentHashMap<String, Process> process_map = new ConcurrentHashMap<>();
    public boolean runSimulation(String file_path, String model_params, SimSession sim_session) throws IOException, InterruptedException {
        boolean is_sim_sucess = false;
        String kafka_serializer_path = "simreal_data" + File.separator + "dependencies" + File.separator + "kafka_template.ser";
        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-jar",
                file_path,
                kafka_serializer_path,
                model_params,
                sim_session.getToken());

        Process process = processBuilder.start();
        process_map.put(sim_session.getToken(), process);

        // Capture the output stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Jar execution done ... exit code = " + exitCode);
        if(exitCode == 0) {
            is_sim_sucess = true;
        }

        process_map.remove(sim_session.getToken());

        // todo: kill all the threads on db and ui services related to this sim
        return is_sim_sucess;
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


}
