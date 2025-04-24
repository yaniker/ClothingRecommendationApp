# Clothing Recommendation App

An Android app for recommending outfit pairings. The system uses vision transformers (via OpenAI’s GPT-4o) to extract structured attributes from clothing images.  
These attributes are then used to train a binary classifier that predicts compatibility between clothing items.  
The model is converted to TensorFlow Lite and integrated into an Android app for local, on-device recommendations.

# Overview

This project combines a mobile front-end with a machine learning backend:

The Android app presents outfit suggestions based on a TFLite model.
A Python-based pipeline handles model training and data preparation.
ViT with LLM head is used to generate structured attribute labels from images.

# Data Flow

Images are preprocessed and passed to an LLM, which returns structured JSON attributes.  
These attributes are paired into labeled combinations for training.  
A small neural network is trained to predict compatibility, exported as .tflite, and embedded in the app.

# License

This project is proprietary. All rights reserved. Commercial use is not permitted without prior written consent.