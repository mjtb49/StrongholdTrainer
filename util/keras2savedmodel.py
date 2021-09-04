import tensorflow as tf
import sys
# script to convert between keras and saved model.
# if you get an AttributeError, you might need to downgrade your h5py installation to 2.10 (https://github.com/tensorflow/tensorflow/issues/44467)
if len(sys.argv) != 3:
    print("Invalid arguments! <.keras file> <directory>")
    sys.exit()
model = tf.keras.models.load_model(sys.argv[1])
tf.saved_model.save(model, sys.argv[2])

