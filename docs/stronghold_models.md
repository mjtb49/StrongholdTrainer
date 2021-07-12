# Model Loading
If you have questions, ask in the `#stronghold-ml channel` in the discord.
## Restrictions and Guidelines for Models
 - All models must be either in SavedModel format or a format convertible to SavedModel. This is because the mod (currently) only supports TensorFlow for model loading (sorry PyTorch users). Model input/output layers are determined from the `serving-default` signature definition.
 - All models must take their input as concatenated vectors representing data about a specific room in their innermost dimension (example input shape: ```[-1,   114]```). Currently, models that support batching input by stronghold must contain at least three dimensions _(note: conversion from keras -> SavedModel is known to add an extra dimension to model input layers)_ and have "rnn" in their identifer (TODO: make this a parameter that can be configured instead of this cursed hack fix). How to define the order and quantity of room data fed to a model is described below. The way room types and directions are one-hot encoded can also be defined.
 - All room data are defined in the `RoomData` enum and their values are calculated with the stronghold tree, the current room, and the previous room. Any data about a room that uses anything more than that isn't currently supported. 
 - Input batching by stronghold is supported, with the restrictions defined below.
## Model Structure
  - A model should have the following directory structure:
      ```
      model/
        ├ assets/
        ├ variables/
        ├ model.stmeta
        └ saved_model.pb
      ```
## Debugging and Developing
 - To quickly test a model in the mod, add `MODEL_REGISTRY.createAndRegisterExternal(<system-path>,null)` to the end of `init` in `StrongholdMachineLearning.java`. Note that system-path is the path **on your disk** to the model directory described above.
 - If a model fails to initially load, it will either print an error/warning message in the console or throw an exception and crash.
 - If querying a model fails (e.g. invalid input/output shape) it should print the error to logs and return a policy of `{0xFF,0xFF,0xFF,0xFF,0xFF,0xFF}.`
 - If you want more room data to be added, you can either add your own entry to `RoomData.java` (not recommended unless you know what you're doing) or open an issue specifying **exactly** what the new data describes.
 - The `/model` command allows in-game switching of models. Two features immediately useful for debugging are `verbose` (prints the output of the model to chat) and `debug` which prints the `signature_def` map for a model given an id.
## Metadata and Defining Input/Output - STMETA
STMETA (**S**tronghold**T**rainer **META**data) is a lightweight file format intended to
allow models to specify both metadata and input/output encoding.
### Syntax
__Note: Everything other than the file declaration is line-independent.__

- Lines are terminated by a newline character, there are no terminating characters.
  
- Unrecognized lines will be skipped and not throw a syntax error. As such, comments or notes don't have to
  be specified with any character.
 - _File Declaration:_ ``stmeta <version-number>``. The first line must declare the version of
the stmeta format that that file uses. STMETA is intended to be backwards compatible.
   
- _Creator Definition:_ `creator=<value>` where value is a string literal (double quotes only).
This defines the creator string of the model. This shows up in the `/model list` command. This field
  can be empty.
  
- _Identifier Definition:_ `id=<value>` where value is a *non-empty* string literal (double quotes only). This
defines the identifier with which the model is registered.
  
- _Input/Output Shape Definition:_ ``<input_shape|output_shape>=<shape_definition>`` where `<shape_definition>` is a tensor shape
formatted as `[size_1, size_2, ... size_n]`. All sizes must be valid 64-bit integer literals. At some point, shapes will be validated against the 
auto-detected shape of the model's signature definition. __Currently, only `[1,6]` and `[1,5]` are supported as output shapes__, anything else will cause an error.
  
- _Encoding Configuration:_ ``redefine(map_id):TOKEN_1,TOKEN_2,...TOKEN_n`` where `map_id` is a valid
re-assignable one-hot encoding map and the `TOKEN`s are valid tokens associated with the map.
    - Currently the only `map_id`s supported are `ROOM_TO_VECTOR` and `DIR_TO_VECTOR`. This redefines how the mod
    encodes a stronghold room type or direction to a vector for the mod. The valid tokens for `ROOM_TYPE` are
      three-letter capitalized codes with the first three letters of the room type, with one exception
      for `StrongholdGenerator.Start` (`SPI*`). The valid tokens for direction are just `N`,`S`,`E`,`W`.
      
    - The encoding works as follows. Each token represents the location that room type is encoded in
    the room vector. For example, if the order is `COR,STA`, the generated map is
      - ```
        Corridor -> {1,0}
        Stairs -> {0,1}
        ````
    
- _Input Vector Order:_ ``input_vec_order=ROOM_DATA_1,ROOM_DATA_2,...ROOM_DATA_n``. The order in which the model
takes the room data. This reflects a restriction on models that the mod will load. **Models can only take
  their inputs as concatenated vectors/scalars in the last dimension of their input tensor.** The room
  data that models can take, as well as what they are encoded to are listed below.
    - ```
      CURRENT is the type of the current room, encoded to a vector with ROOM_TO_VECTOR
      PARENT_ROOM is the type of the parent room, encoded to vector with ROOM_TO_VECTOR
      PREVIOUS_ROOM is the type of the previous room
      EXIT_{1..5} are the types of the 5 exits from the current room, encoded to vectors with ROOM_TO_VECTOR
      EXIT_BACK a room data value that references the type of the previous room, encoded to a vector with ROOM_TO_VECTOR
      PREV_EXIT_INDEX is the index of the exit that led to the current room, encoded directly to an integer scalar.
      PREV_EXIT_INDEX_COMPAT is the index of the exit from the parent room that led to this room, encoded directly to an index vector (exits indexed 1-5, 0 meaning the exit is non-existent)
      DIRECTION is the facing direction of the current room, encoded to a vector with DIR_TO_VECTOR
      DEPTH is the depth of the current room, encoded directly to an integer scalar.
      CONSTANT is just 0, encoded directly to an integer scalar.
      DOWNWARDS is whether or not the player is progressing or backtracking through the tree, an integer scalar that is only 1 or 0.
      ENTRY tells you the exit of the index the player came through if they are **backtracking** through the stronghold tree, encoded directly to an index vector where 0 means they are not backtracking.
      
      ```
  
- _Output Vector Order:_  ``output_vec_order=<exits ⊆ {EXIT_1,EXIT_2,EXIT_3,EXIT_4,EXIT_5,EXIT_BACK}>``.
The order and number of model output weights. Currently, the mod ignores this. Assume that the mod interprets a 
size 5 vector output as exits 1-5 and size 6 as backtracking then the ordered exits.
  
- _Optional EOF:_ `eof`. A quick way to terminate parsing before the actual end of the file.

- Input Data Type - The data type of the input. Currently only supports `int64` and `float32`.

## Example: The STMETA for rnn_8, with comments
```
stmeta 1.0
creator="neoprene1337"
id="rnn_8"
input_shape=[-1,-1,114]
input_vec_order=DIRECTION,PREV_EXIT_INDEX_COMPAT,ENTRY,EXIT_5,EXIT_4,EXIT_3,EXIT_2,EXIT_1,PARENT_ROOM,CURRENT
redefine(ROOM_TO_VECTOR)=COR,PRI,LEF,RIG,SQU,STA,SPI,FIV,CHE,LIB,POR,SMA,SPI*,NUL
redefine(DIR_TO_VECTOR)=N,S,E,W
output_shape=[1,6]
eof
```
Lines 1-3 are standard stuff, declaring the file, the creator, and the identifier of this model. Lines 4-5 define the input shape and the order of the input. The next two `redefine`s set up how the model expects room types and directions to be one-hot encoded. The penultimate line defines the shape of the output, the last ends the file.
