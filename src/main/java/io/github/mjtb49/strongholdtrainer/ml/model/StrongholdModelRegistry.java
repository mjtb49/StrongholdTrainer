package io.github.mjtb49.strongholdtrainer.ml.model;


import java.util.Hashtable;
import java.util.Set;

/**
 * A class that can store and load StrongholdModels. The models are registered with their identifiers and stored in
 * a Hashtable if everything checks out. The registry has an "active model" TODO: move the active model out of the registry.
 */
public class StrongholdModelRegistry {

    private final Hashtable<String, StrongholdModel> modelRegistry;
    private StrongholdModel activeModel;

    /**
     * Initializes a new ModelRegistry
     */
    public StrongholdModelRegistry(){
        this.modelRegistry = new Hashtable<>();
    }

    /**
     * Sets the active model
     * @param id The string identifier of the mode
     * @throws IllegalArgumentException if the identifier specified doesn't exist in the registry.
     */
    public void setActiveModel(String id) throws IllegalArgumentException{
        if(this.modelRegistry.containsKey(id)){
            this.activeModel = modelRegistry.get(id);
        } else{
            throw new IllegalArgumentException("No such model registered with identifier \"" + id + "\"!");
        }
    }

    /**
     * Registers a model with its identifier being StrongholdModel#getIdentifier().
     * @param model The model to be registered.
     * @throws IllegalArgumentException if the identifier of the model already has an entry in the registry.
     */
    public void register(StrongholdModel model) throws IllegalArgumentException{
        if(!this.modelRegistry.containsKey(model.getIdentifier())){
            this.modelRegistry.put(model.getIdentifier(), model);
        } else{
            throw new IllegalArgumentException("There already exists a model with identifier \"" + model.getIdentifier() + "\"");
        }
    }

    /**
     * Checks if an identifier matches with this registry's active model.
     * @param id The identifier of the query.
     * @return whether or not the identifier refers to the active model.
     */
    public boolean isActiveMode(String id){
        return this.activeModel.getIdentifier().equals(id);
    }

    /**
     * Gets a set of the currently registered identifiers.
     * @return The currently registered identifiers.
     */
    public Set<String> getRegisteredIdentifiers(){
        return this.modelRegistry.keySet();
    }

    /**
     * Gets the active model.
     * @return The active model.
     */
    public StrongholdModel getActiveModel(){
        return this.activeModel;
    }

    /**
     * Creates and registers an <b>internal</b> StrongholdModel
     * @param zippedModelName The name of the model ZIP archive in the JAR.
     */
    public void createAndRegisterInternal(String zippedModelName, String creator){
        this.register(new StrongholdModel(zippedModelName, creator, true));
    }

    /**
     * Creates and registers an <b>external</b> StrongholdModel
     * @param systemPath The path to the model.
     * @param creator The creator of this model.
     */
    public void createAndRegisterExternal(String systemPath, String creator){
        this.register(new StrongholdModel(systemPath, creator, false));
    }
    public StrongholdModel getModel(String id){
        if(this.modelRegistry.containsKey(id)){
            return this.modelRegistry.get(id);
        } else {
            throw new IllegalArgumentException("There is no registered model with the id \"" + id + "\"");
        }
    }
}
