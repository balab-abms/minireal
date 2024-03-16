package org.balab.minireal.data.service;

import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
//@AllArgsConstructor
public class FileSystemService
{

    public boolean saveFile(String file_path, byte[] file_byte)
    {
        String storage_file_path = file_path;
        File file_store = new File(storage_file_path);
        try
        {
            FileUtils.writeByteArrayToFile(file_store, file_byte);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public byte[] getFile(String file_name)
    {
        // read the file from the filesystem
        Path file_path = Paths.get(file_name);
        byte[] file_byte;
        try {
            file_byte = Files.readAllBytes(file_path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file_byte;
    }

    public StreamResource getImageResource(String file_path)
    {
        return new StreamResource("image.png", () -> {
            InputStream stream = getClass().getResourceAsStream("/META-INF/resources/" + file_path);
            if (stream == null) {
                try {
                    throw new FileNotFoundException("File not found");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return stream;
        });
    }

}
