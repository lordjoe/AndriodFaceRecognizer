package com.lordjoe.identifier;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.*;

import static android.R.attr.label;
import static com.lordjoe.identifier.OpenCVUtilities.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
// import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
// import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

/**
 * I couldn't find any tutorial on how to perform face recognition using OpenCV and Java,
 * so I decided to share a viable solution here. The solution is very inefficient in its
 * current form as the training model is built at each run, however it shows what's needed
 * to make it work.
 * <p>
 * The class below takes two arguments: The path to the directory containing the training
 * faces and the path to the image you want to classify. Not that all images has to be of
 * the same size and that the faces already has to be cropped out of their original images
 * (Take a look here http://fivedots.coe.psu.ac.th/~ad/jg/nui07/index.html if you haven't
 * done the face detection yet).
 * <p>
 * For the simplicity of this post, the class also requires that the training images have
 * filename format: <label>-rest_of_filename.png. For example:
 * <p>
 * 1-jon_doe_1.png
 * 1-jon_doe_2.png
 * 2-jane_doe_1.png
 * 2-jane_doe_2.png
 * ...and so on.
 * <p>
 * Source: http://pcbje.com/2012/12/doing-face-recognition-with-javacv/
 *
 * @author Petter Christian Bjelland
 */

/**
 * com.lordjoe.identifier.FaceRecognizer
 * User: Steve
 * Date: 3/13/2017
 */
public class AndroidFaceRecognizer {

    public static final double CONFIDENCE_LIMIT = 700;

    public static FaceRecognizer getFaceRecognizer( File root,FaceRecognizerType type, String saveFile) {

        if (saveFile == null)
            saveFile = type.toString() + ".xml";

        FaceRecognizer faceRecognizer = OpenCVUtilities.createFaceRecognizerOfType(type);

        if(new File(saveFile).exists())   {
            faceRecognizer.load(saveFile);
        }
        else {
            long start = System.currentTimeMillis();
            System.out.println("Training...");
            trainFaceRecognizer(  faceRecognizer,  root,   saveFile);
            long end = System.currentTimeMillis() - start;
            System.out.println("Done Training... in " + end/1000 + " sec");
         }

           return faceRecognizer;
    }

    protected static void trainFaceRecognizer(FaceRecognizer faceRecognizer,File root, String saveFile)
    {
        File[] imageFiles = root.listFiles(OpenCVUtilities.makeImageFilter());
        MatVector images = new MatVector(imageFiles.length);

        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            Mat smaller = null;
            //    org.bytedeco.javacpp.opencv_imgcodecs.
            int label = 0;
            String name = image.getName();
            label = getLabelFromFile(name);

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }

        faceRecognizer.train(images, labels);

        faceRecognizer.save(saveFile);

    }

    public static void updateFaceRecognizer(FaceRecognizer faceRecognizer,List<File> files)
    {
        int size = files.size();
        MatVector images = new MatVector(size);

        Mat labels = new Mat(size, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File image : files) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            Mat smaller = null;
            //    org.bytedeco.javacpp.opencv_imgcodecs.
            int label = 0;
            String name = image.getName();
            label = getLabelFromFile(name);

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }

        faceRecognizer.update(images, labels);


    }



    public static void testMultiRecognizer(File trainingDir, File testImageDir, FaceRecognizer faceRecognizer,Set<Integer> trainedLabels,File updateDir) {

        Map<Integer,List<File>> updateSet = findFilesWithLabel( updateDir);
        Map<Integer,List<File>> trainingSet = findFilesWithLabel( trainingDir);
        int retainedResults = (int)Math.max(2,(int)trainingSet.size() * 0.04);

          int numberTotal = 0;
          int numberCorrect = 0;
          Map<Integer,List<File>> testSet = findFilesWithLabel(testImageDir);
          double[] confidence = {0.0 };
          for (Integer label : testSet.keySet()) {
              boolean inTrainingSet =  trainedLabels.contains(label);
            List<File> files = testSet.get(label);
             List<IdentificationResult> allIds = new ArrayList<>();
             for (File testFile : files) {
                 List<IdentificationResult>  thisResults = matchFile(testFile,faceRecognizer,retainedResults);
                 allIds.addAll(thisResults);
             }
             Collections.sort(allIds);
              boolean isCorrect = evaluateResults(label, confidence, allIds);
              if(!inTrainingSet)
                  isCorrect = !isCorrect; // identified is bad
              numberTotal++;
              if(isCorrect)
                  numberCorrect++;
              String error = "";
              if(!isCorrect )
                  error = inTrainingSet ? "****"  : "=====";
              System.out.println(Integer.toString(label) +  " confidence " + String.format("%3.1f",confidence[0]) + " "   + error);

              if(isCorrect && !inTrainingSet &&  updateSet.containsKey(label)) {
                  boolean nowRecognozed = updateAndTest(label,updateSet.get(label),faceRecognizer,files,  retainedResults);
                  numberTotal++;
                  if(nowRecognozed)
                      numberCorrect++;
              }

          }
          System.out.println("Total " + numberTotal + " Correct " + numberCorrect + " % " + String.format("%3.1f",(100.0 * numberCorrect)/ numberTotal));
    }

    public static boolean updateAndTest(Integer label, List<File> trainingFiles, FaceRecognizer faceRecognizer, List<File> testfiles,int retainedResults) {
        updateFaceRecognizer(  faceRecognizer,trainingFiles);
          List<IdentificationResult> allIds = new ArrayList<>();
        for (File testFile : testfiles) {
            List<IdentificationResult>  thisResults = matchFile(testFile,faceRecognizer,retainedResults);
            allIds.addAll(thisResults);
        }
        Collections.sort(allIds);
        double[] confidence = {0.0 };
        boolean isCorrect = evaluateResults(label, confidence, allIds);
            String error = "";
        if(!isCorrect )
            error =   "=====";
        System.out.println("Trained " + Integer.toString(label) +  " confidence " + String.format("%3.1f",confidence[0]) + " "   + error);
        return isCorrect;
    }

    public static boolean evaluateResults(Integer label, double[] confidence, List<IdentificationResult> allIds) {
        double minConfidence = Double.MAX_VALUE;
        double minLabelConfidence = 1000;
        for (IdentificationResult ident : allIds) {
            minConfidence = Math.min(ident.confidence,minConfidence);
            if(ident.label == label)
                minLabelConfidence = Math.min(ident.confidence,minLabelConfidence);

        }
        ModeFinder modes = new ModeFinder();
        double acceptedConfidence = 1.2 * minConfidence;
        for (IdentificationResult ident : allIds) {
           if(ident.confidence < acceptedConfidence)
                modes.addItem(ident.label);
        }
          confidence[0] =  minLabelConfidence;

        boolean ret = modes.isMode(label) || minLabelConfidence < 80;

        return ret;
    }


    /**
     * creates and saves a face recognizer also may test
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //   System.setProperty("org.bytedeco.javacpp.logger.debug", "true");
        //   System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "0");
        //  System.out.println(System.getProperty("java.library.path"));
//        try {
//            Loader.load(AndroidFaceRecognizer.class);
//        } catch (UnsatisfiedLinkError e) {
//            e.printStackTrace();
//            String path = Loader.cacheResource(AndroidFaceRecognizer.class, "windows-x86_64/jniAndroidFaceRecognizer.dll").getPath();
//            new ProcessBuilder("c:/scripts/depends.exe", path).start().waitFor();
//        }

        String trainingDir = args[0];
        Map<Integer,List<File>> trainingSet = findFilesWithLabel( new File(trainingDir));

        File testImageDir = new File(args[1]);


       File root = new File(trainingDir);

        String saveFileName = null;
        if (args.length > 2)
            saveFileName = args[2];

        File updateDirectory = null;
        if (args.length > 3)
            updateDirectory = new File(args[3]);

        FaceRecognizer faceRecognizer = getFaceRecognizer( root,FaceRecognizerType.LBPHFaceFaces, saveFileName);

        if(false && testImageDir.exists())
            AndroidFaceRecognizerTest.testRecognizer(testImageDir,faceRecognizer);

        if(testImageDir.exists())
            testMultiRecognizer(root,testImageDir, faceRecognizer,new HashSet<Integer>(trainingSet.keySet()),updateDirectory);


    }



}
