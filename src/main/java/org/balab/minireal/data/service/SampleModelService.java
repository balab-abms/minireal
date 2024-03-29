package org.balab.minireal.data.service;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.balab.minireal.data.entity.SampleModel;
import org.balab.minireal.data.repository.SampleModelRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SampleModelService
{
    private final SampleModelRepository sample_repo;
    private final StorageProperties storage_properties;
    private final FileSystemService fs_service;


    public List<SampleModel> findByModelName(String model_name){
        return sample_repo.findByModel(model_name);
    }

    public Optional<SampleModel> getSampleModel(Long id){
        return sample_repo.findById(id);
    }

    public SampleModel saveSampleModel(SampleModel sample_model){
        return sample_repo.save(sample_model);
    }

    public SampleModel saveSampleModel(SampleModel sample_model, String file_name,  byte[] model_byte){
        SampleModel sample = sample_repo.save(sample_model);
        if(sample.getPath() == null){
            String model_path = storage_properties.getSystem() + File.separator + sample.getId() + File.separator + file_name;
            boolean is_saved = fs_service.saveFile(model_path, model_byte);
            if (is_saved){
                sample.setPath(model_path);
                saveSampleModel(sample);
            }
        }
        return sample;
    }

    public void deleteSampleModel(Long id) throws IOException {
        sample_repo.deleteById(id);
        // delete the user folder
        String model_path = storage_properties.getSystem() + File.separator + id;
        File user_folder = new File(model_path);
        if(user_folder.exists())
            FileUtils.deleteDirectory(user_folder);
    }

    public void deleteSampleModel(SampleModel sample_model){
        sample_repo.delete(sample_model);
    }

    public List<SampleModel> getAllSampleModels(){
        return sample_repo.findAll();
    }
}
