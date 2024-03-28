package org.balab.minireal.data.repository;

import org.balab.minireal.data.entity.SampleModel;
import org.balab.minireal.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SampleModelRepository extends JpaRepository<SampleModel, Long>, JpaSpecificationExecutor<User>
{
    @Override
    Optional<SampleModel> findById(Long aLong);

    List<SampleModel> findByModel(String model_name);
}
