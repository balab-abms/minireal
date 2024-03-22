package org.balab.minireal.data.service;

import org.springframework.stereotype.Service;
import oshi.util.tuples.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class OsService
{
    public Pair<Integer, String> commandRunner (String dir, String cmd, int mode)
    {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        int exit_code = -1;
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (isWindows) {
            processBuilder.command("cmd.exe", "/c", cmd);
        } else {
            processBuilder.command("bash", "-c", cmd);
        }

        if (dir != null && !dir.isEmpty()) {
            processBuilder.directory(new File(dir));
        }
        StringBuilder output = new StringBuilder();
        try
        {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // for future see how to utilize the output of the command process
                // System.out.println(line);
                output.append(line + "\n");
            }
            exit_code = process.waitFor();
            output.append("\nExited with error code : " + exit_code);
        } catch (IOException | InterruptedException e)
        {
            output.append("Error executing command: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        return new Pair<>(exit_code, output.toString());
    }

}
