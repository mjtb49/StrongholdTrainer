import tensorflow as tf

# script to convert between keras and saved model.
model = tf.keras.models.load_model("model102.keras")
tf.saved_model.save(model, "model102")

