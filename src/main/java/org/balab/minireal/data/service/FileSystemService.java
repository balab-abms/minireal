package org.balab.minireal.data.service;

import com.vaadin.flow.server.StreamResource;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class FileSystemService
{
    private final StorageProperties storage_props;

    public boolean saveFile(String file_path, byte[] file_byte) throws IOException
    {
        String storage_file_path = file_path;
        File file_store = new File(storage_file_path);
        file_store.getParentFile().mkdirs();
        FileUtils.writeByteArrayToFile(file_store, file_byte);
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
        System.out.println(file_path);
        return new StreamResource("image.png", () -> {
            InputStream stream = getClass().getResourceAsStream(file_path);
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

    public StreamResource getFileResource(String file_path)
    {
        System.out.println(file_path);
        return new StreamResource("image.png", () -> {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(file_path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return stream;
        });
    }

    public String getMetaData(String file_path) throws IOException
    {
        URL inputURL = new URL("jar:file:"+file_path+"!/metaData.json");
        JarURLConnection conn = (JarURLConnection)inputURL.openConnection();
        try (InputStream in = conn.getInputStream()) {
            String meta_text = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).readLine();
            return meta_text;
        }
    }

    public boolean createUserDir(Long user_id)
    {
        String user_dir_path = storage_props.getUsers() + File.separator + user_id;
        boolean isDirectoryCreated = new File(user_dir_path).mkdirs();
        return isDirectoryCreated;
    }

    public boolean deleteUserDir(Long user_id)
    {
        String user_dir_path = storage_props.getUsers() + File.separator + user_id;
        new File(user_dir_path).delete();
        return true;
    }


}
