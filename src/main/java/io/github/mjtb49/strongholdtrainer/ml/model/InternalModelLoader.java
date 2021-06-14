package io.github.mjtb49.strongholdtrainer.ml.model;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class InternalModelLoader extends SavedModelLoader{
    public static Path CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("stronghold-trainer");

    public InternalModelLoader(String sourcePath, String modelIdentifier) throws IOException {
        super(sourcePath);

        File modelFolder;
        modelFolder = new File(CONFIG_DIRECTORY.toString());
        // HACK FIX: to prevent cluttering. TODO: refactor id->UUID so that it can cache models over multiple sessions.
        modelFolder.delete();
        if(!modelFolder.exists()){
            modelFolder.mkdirs();
        }
        if(modelFolder.isDirectory()){
            URLConnection connection = Thread.currentThread().getContextClassLoader().getResource(this.sourcePath).openConnection();
            unzipModel(new ZipInputStream(connection.getInputStream()), modelIdentifier);
        }
        this.sourcePath = CONFIG_DIRECTORY.resolve(modelIdentifier).resolve("model/").toAbsolutePath().toString();
    }

    @Deprecated
    public static void unzipModel(ZipFile f) throws IOException {
        Path targetDirectory = CONFIG_DIRECTORY;
        System.out.println("Unzipping model to " + targetDirectory);
        Enumeration<? extends ZipEntry> entries = f.entries();
        while(entries.hasMoreElements()){
            ZipEntry currentEntry = entries.nextElement();
            if(currentEntry.isDirectory()){
                Files.createDirectories(targetDirectory.resolve(currentEntry.getName()));
            } else{
                Files.copy(f.getInputStream(currentEntry), targetDirectory.resolve(currentEntry.getName()));
            }
        }
    }

    public static void unzipModel(ZipInputStream f, String targetInternal) throws IOException {
        Path targetDirectory = CONFIG_DIRECTORY.resolve(targetInternal + "/");
        if((new File(targetDirectory.toString())).exists()){
            System.out.println("Model folder " + targetInternal + " already exists!");
            return;
        }
        System.out.println("Unzipping model to " + targetDirectory);
        ZipEntry e;
        while((e = f.getNextEntry()) != null){
            if(e.isDirectory()){
                Files.createDirectories(targetDirectory.resolve(e.getName()));
            } else{
                Files.copy(f, targetDirectory.resolve(e.getName()));
            }
        }
    }
}
