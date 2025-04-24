# Clothing Recommendation App

This app was developed for a specific user/client as demo and is a work in progress based on the user/client feedback.

## Overview

An Android app for recommending outfit pairings, combining a mobile front-end with a machine learning backend.  
The system uses vision transformers (ViT) with LLM head to extract attributes from images.  
These attributes are then used to train a deep neural network classifier that maps attributes to user preferences.  
The model is converted to TensorFlow Lite and integrated into an Android app for local, on-device recommendations.

## Data Flow

Images are preprocessed and passed to a ViT + LLM, which returns structured JSON attributes.  
These attributes are paired into labeled combinations for neural network training, exported as .tflite.

## Results

Test Accuracy: 1.00
Precision: 1.00
Recall: 1.00
F1 Score: 1.00

Analysis shows that:
1. The system does not show the user combinations that the user would not like.
2. The system does not miss combinations that the user would like, showing them 100% of the time.

Therefore, satisfying the user requirements in performance.

# License

This project is proprietary. All rights reserved. Commercial use is not permitted without prior written consent.