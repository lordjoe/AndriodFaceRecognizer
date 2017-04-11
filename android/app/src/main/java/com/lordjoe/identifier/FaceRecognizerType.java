package com.lordjoe.identifier;

import org.bytedeco.javacpp.opencv_face;

import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

/**
 * com.lordjoe.identifier.FaceRecognizerType
 * User: Steve
 * Date: 3/25/2017
 */
public enum FaceRecognizerType {
    EigenFaces(false),LBPHFaceFaces(true),FischerFaces(false);



    private final boolean trainable;

    FaceRecognizerType(boolean trainable) {
        this.trainable = trainable;
    }

    public boolean isTrainable() {
        return trainable;
    }
}
