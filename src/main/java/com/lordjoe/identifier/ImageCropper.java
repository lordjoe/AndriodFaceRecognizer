package com.lordjoe.identifier;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.lordjoe.identifier.AndroidFaceRecognizer.getFaceRecognizer;
import static com.lordjoe.identifier.OpenCVUtilities.*;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;

/**
 * com.lordjoe.identifier.ImageCropper
 * User: Steve
 * Date: 3/17/2017
 */
public class ImageCropper {
    public static final ImageCropper[] EMPTY_ARRAY = {};



    public static void main(String[] args) {
        File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        File badOutDir = new File(args[2]);
        FilenameFilter imageFilter = OpenCVUtilities.makeImageFilter();
        File[] images = inDir.listFiles(imageFilter);
        if(images == null)
            return;

        String saveFileName = args[3];

        opencv_face.FaceRecognizer faceRecognizer = getFaceRecognizer( null,FaceRecognizerType.LBPHFaceFaces, saveFileName);


        outDir.mkdirs();
        badOutDir.mkdirs();

        for (int i = 0; i < images.length; i++) {
            File image = images[i];
            saveAndVerifyCroppedImage(image,outDir,badOutDir,faceRecognizer);
        }

    }


}
