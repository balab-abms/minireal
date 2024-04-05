package org.balab.minireal.data.service;

import org.balab.minireal.data.entity.SimSession;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.repository.SimSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SimSessionService
{
    private final SimSessionRepository simSessionRepository;

    public SimSessionService(SimSessionRepository simSessionRepository) {
        this.simSessionRepository = simSessionRepository;
    }

    public SimSession createSimSession(
            User sim_user,
            String file_path
    ) {
        SimSession simSession = new SimSession();
        simSession.setSim_user(sim_user);
        simSession.setFile_path(file_path);

        String token;
        do {
            token = "_" + UUID.randomUUID().toString().replace("-", "_");
        } while(simSessionRepository.existsByToken(token));
        simSession.setToken(token);

        return simSessionRepository.save(simSession);
    }
    public SimSession getSimSession(String token)
    {
        return simSessionRepository.getSimSessionByToken(token);
    }

    public  SimSession updateSimSession(SimSession sim_session)
    {
        return simSessionRepository.save(sim_session);
    }
}
