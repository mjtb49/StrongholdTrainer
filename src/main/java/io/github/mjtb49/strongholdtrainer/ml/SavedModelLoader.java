package io.github.mjtb49.strongholdtrainer.ml;

import net.fabricmc.loader.api.FabricLoader;
import org.tensorflow.SavedModelBundle;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class SavedModelLoader {

    public static Path CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("stronghold-trainer");
    private final boolean forceZipLoading;
    private final String zipName;
    private final String jarName;
    private final ClassLoader currentClassloader;

    public SavedModelLoader(String src, String jarName, ClassLoader loader, boolean forceZipLoading){
        this.currentClassloader = loader;
        this.zipName = src;
        this.jarName = jarName;
        this.forceZipLoading = forceZipLoading;
    }

    public SavedModelBundle loadModel() throws IOException {
        File modelFolder;
        if(FabricLoader.getInstance().isDevelopmentEnvironment() && !forceZipLoading){
            System.out.println("Detected a development environment, using ClassLoader");
            try {
                modelFolder = new File(currentClassloader.getResource(this.jarName).getPath());
            } catch (NullPointerException e){
                return null;
            }
            return SavedModelBundle.load(modelFolder.getPath(), "serve");
        } else {
            modelFolder = new File(CONFIG_DIRECTORY.toString());
            System.out.println("Detected a production environment, extracting model to/using extracted model at " + modelFolder.getPath());

            if(!modelFolder.mkdirs()){
                System.out.println("Unable to initialize config directory.");
            }
            if(modelFolder.isDirectory() && modelFolder.listFiles().length == 0){
                URLConnection connection = Thread.currentThread().getContextClassLoader().getResource(this.zipName).openConnection();
                unzipModel(new ZipInputStream(connection.getInputStream()));
            }
            String path = CONFIG_DIRECTORY.resolve("model/").toAbsolutePath().toString();
            return SavedModelBundle.load(path, "serve");
        }
    }
    /**
     * Unzip our model to the config directory
     */
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

    public static void unzipModel(ZipInputStream f) throws IOException {
        Path targetDirectory = CONFIG_DIRECTORY;
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
