package io.github.mjtb49.strongholdtrainer.ml.model;

import org.tensorflow.SavedModelBundle;

public class SavedModelLoader {

    protected String sourcePath;

    public SavedModelLoader(String sourcePath){
        this.sourcePath = sourcePath;
    }

    public SavedModelBundle loadModel() {
        return SavedModelBundle.load(this.sourcePath, "serve");
    }
}
