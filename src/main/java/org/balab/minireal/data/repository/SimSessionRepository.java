package org.balab.minireal.data.repository;

import org.balab.minireal.data.entity.SimSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimSessionRepository extends JpaRepository<SimSession, String> {
    boolean existsByToken(String token);
    SimSession getSimSessionByToken(String token);
}
