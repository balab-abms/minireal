package org.balab.minireal.data.service;

import org.balab.minireal.data.entity.SampleModel;
import org.balab.minireal.data.repository.SampleModelRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SampleModelService
{
    private final SampleModelRepository sample_repo;

    public SampleModelService(SampleModelRepository sampleRepo)
    {
        sample_repo = sampleRepo;
    }

    public List<SampleModel> findByModelName(String model_name){
        return sample_repo.findByModel(model_name);
    }

    public Optional<SampleModel> getSampleModel(Long id){
        return sample_repo.findById(id);
    }

    public SampleModel saveSampleModel(SampleModel sample_model){
        return sample_repo.save(sample_model);
    }

    public void deleteSampleModel(Long id){
        sample_repo.deleteById(id);
    }

    public void deleteSampleModel(SampleModel sample_model){
        sample_repo.delete(sample_model);
    }

    public List<SampleModel> getAllSampleModels(){
        return sample_repo.findAll();
    }
}
