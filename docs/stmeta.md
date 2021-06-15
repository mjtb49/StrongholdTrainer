# The STMETA 1.0 File Format

STMETA (**S**tronghold**T**rainer **META**data) is a lightweight file format intended to
allow models to specify both metadata and input/output encoding.

## Syntax
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
formatted as `[size_1, size_2, ... size_n]`. All sizes must be valid 64-bit integer literals and all unkown sizes (-1) will be
  converted to size 1, which the parser will warn about. 
  
- _Encoding Configuration:_ ``redefine(map_id):TOKEN_1,TOKEN_2,...TOKEN_n`` where `map_id` is a valid
re-assignable one-hot encoding map and the `TOKEN`s are valid tokens associated with the map.
    - Currently the only `map_id` supported is `ROOM_TO_VECTOR`. This redefines how the mod
    encodes a stronghold room type to a vector for the mod. The valid tokens for `ROOM_TYPE` are
      three-letter capitalized codes with the first three letters of the room type, with one exception
      for `StrongholdGenerator.Start` (`SPI*`).
      
    - The encoding works as follows. Each token represents the location that room type is encoded in
    the room vector. For example, if the order is `COR,STA`, the generated map is
      - ```
        Corridor -> {1,0}
        Stairs -> {0,1}
        ````
    - In the future the `DIR_TO_VECTOR` map will be reconfigurable.
    
- _Input Vector Order:_ ``input_vec_order=ROOM_DATA_1,ROOM_DATA_2,...ROOM_DATA_n``. The order in which the model
takes the room data. This reflects a restriction on models that the mod will load. **Models can only take
  their inputs as concatenated vectors/scalars in the innermost dimension of their input tensor.** The room
  data that models can take, as well as what they are encoded to are listed below.
    - ```
      CURRENT is the type of the current room, encoded to a vector with ROOM_TO_VECTOR
      PREV is the type of the previous room, encoded to vector with ROOM_TO_VECTOR
      EXIT_{1..5} are the types of the 5 exits from the current room, encoded to vectors with ROOM_TO_VECTOR
      EXIT_BACK a room data value that references the type of the previous room, encoded to a vector with ROOM_TO_VECTOR
      PREV_EXIT_INDEX is the index of exit that led to the current room, encoded directly to a scalar integer.
      DIRECTION is the facing direction of the current room, encoded to a vector with DIR_TO_VECTOR
      DEPTH is the depth of the current room, encoded directly with n to a scalar integer.
      ```
  
- _Output Vector Order:_  ``output_vec_order=<exits ⊆ {EXIT_1,EXIT_2,EXIT_3,EXIT_4,EXIT_5,EXIT_BACK}>``.
The order and number of model output weights. Currently, the parser ignores this.