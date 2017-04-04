package com.lordjoe.identifier;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.*;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.lordjoe.identifier.OpenCVUtilities.*;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_face.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

// import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
// import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

/**
 * I couldn't find any tutorial on how to perform face recognition using OpenCV and Java,
 * so I decided to share a viable solution here. The solution is very inefficient in its
 * current form as the training model is built at each run, however it shows what's needed
 * to make it work.
 *
 * The class below takes two arguments: The path to the directory containing the training
 * faces and the path to the image you want to classify. Not that all images has to be of
 * the same size and that the faces already has to be cropped out of their original images
 * (Take a look here http://fivedots.coe.psu.ac.th/~ad/jg/nui07/index.html if you haven't
 * done the face detection yet).
 *
 * For the simplicity of this post, the class also requires that the training images have
 * filename format: <label>-rest_of_filename.png. For example:
 *
 * 1-jon_doe_1.png
 * 1-jon_doe_2.png
 * 2-jane_doe_1.png
 * 2-jane_doe_2.png
 * ...and so on.
 *
 * Source: http://pcbje.com/2012/12/doing-face-recognition-with-javacv/
 *
 * @author Petter Christian Bjelland
 */

/**
 * com.lordjoe.identifier.FaceRecognizer
 * User: Steve
 * Date: 3/13/2017
 *
 * Sample command line
 * T:\training\LBPHfacesIFWTraining.xml   T:\training\ifw_test
 * arg 0 saved FaceRecognixer
 * arg 1 directory of labeled test images
 *
 */
public class AndroidFaceRecognizerTest {

    public static final String EIGEN_RECOGNIZER =  "Eigenfaces.xml";
    public static final String FISHER_RECOGNIZER =  "Fisherfaces.xml";
    public static final String LBPH_RECOGNIZER =  "LBPHfaces.xml";
    public static final double CONFIDENCE_LIMIT = 700;

    public static FaceRecognizer buildRecognizer(FilenameFilter imgFilter, File root)   {

        File[] imageFiles = root.listFiles(imgFilter);
        MatVector images = new MatVector(imageFiles.length);


        /**
         * do in two steps to drop bad faces
         * We get bad applocation sometimes
         */
        Map<Mat,Integer> faces = new HashMap<>(imageFiles.length);
        for (File image : imageFiles) {
            Integer label = 0;
            String name = image.getName();

            Mat mat = OpenCVUtilities.extractFace(image);
            if(mat != null) {
                label = getLabelFromFile(name);
                faces.put(mat, label);
            }
        }

        int counter = 0;
        Mat labels = new Mat(faces.size(), 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();
        for (Mat mat : faces.keySet()) {
            int label = faces.get(mat);
              images.put(counter, mat);
             labelsBuf.put(counter, label);
             counter++;
        }

         //  FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
        FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        // FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.train(images, labels);

        faceRecognizer.save("Eigenfaces.xml");

        return faceRecognizer;
    }



    public static FaceRecognizer trainLBPHFaceRecognizer(FilenameFilter imgFilter, File root, String saveFile) {
        File[] imageFiles = root.listFiles(imgFilter);

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

         FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.train(images, labels);

        faceRecognizer.save(saveFile);
        return faceRecognizer;
    }


    public static FaceRecognizer getLBPHFaceRecognizer (  String saveFile) {
         FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.load(saveFile);
        return faceRecognizer;
    }

    public static FaceRecognizer trainEigenFaceRecognizer(FilenameFilter imgFilter, File root, String saveFile) {
        File[] imageFiles = root.listFiles(imgFilter);

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

        //  FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
        FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        // FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.train(images, labels);

        faceRecognizer.save(saveFile);
        return faceRecognizer;
    }


    public static FaceRecognizer getEigenFaceRecognizer (FilenameFilter imgFilter, File root, String saveFile) {
         //  FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
        FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        // FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.load(saveFile);
        return faceRecognizer;
    }

    public static FaceRecognizer trainFisherFaceRecognizer(FilenameFilter imgFilter, File root, String saveFile) {
        File[] imageFiles = root.listFiles(imgFilter);

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

         FaceRecognizer faceRecognizer = createFisherFaceRecognizer();

        faceRecognizer.train(images, labels);

        faceRecognizer.save(saveFile);
        return faceRecognizer;
    }



    public static FaceRecognizer getFisherFaceRecognizer (File root, String saveFile) {
         FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
          faceRecognizer.load(saveFile);
        return faceRecognizer;
    }

    public static void testRecognizer(File testImageDir, FaceRecognizer faceRecognizer) {
        File[] testFiles = testImageDir.listFiles(OpenCVUtilities.makeImageFilter());
        int total = testFiles.length;
        int errors = 0;
        int[] index_found = { 0 };
        int high_confidence_errors = 0;
        double[] confidence = {0.0 };
        for (File testFile : testFiles) {
            Mat testImage = imread(testFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            int testIndex  =  indexFromTestName(testFile.getName());
            boolean verified = OpenCVUtilities.verifyIdentity(testImage,faceRecognizer,confidence,index_found,testIndex);

       //     int predictedLabel = recognizeFile(    faceRecognizer ,testFile,confidence);


            String error =  verified? "" :   " *****";
            if(!verified)
                high_confidence_errors++;
            int position_Found = index_found[0];
            if(position_Found > 0)
                System.out.println(testFile.getName() +  " confidence " + confidence[0] + "index " + position_Found + " file " + error);

        }
        System.out.println("tested " + total + " errors " + errors+ " HCErrors " + high_confidence_errors);
    }

    public static int recognizeFile(  FaceRecognizer faceRecognizer ,File testImageFile,double[] confidence) {
         Mat testImage = imread(testImageFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
         DoublePointer predicted_confidence = new DoublePointer(1);
         IntPointer pred = new IntPointer(1) ;
         faceRecognizer.predict(testImage,pred,predicted_confidence);
         int predictedLabel = pred.get();
          confidence[0] = predicted_confidence.get();

          OpenCVUtilities.doPredict(testImage,faceRecognizer);
          return  predictedLabel;
     }

    public static Mat readSizedImage(File image,Size sz)    {
        Mat src = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
        Mat smaller = new Mat();
        Mat dst = new Mat(); //no, you don't have to pre-allocate it. this is no more C.
      //   .resize(src, dst, new opencv_core.Size(200,200) ); //resize image
          return src;

    }


    public static void main(String[] args) throws Exception {
     //   System.setProperty("org.bytedeco.javacpp.logger.debug", "true");
     //   System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "0");
    //    System.out.println(System.getProperty("java.library.path"));
//        try {
//            Loader.load(AndroidFaceRecognizer.class);
//        } catch (UnsatisfiedLinkError e) {
//            e.printStackTrace();
//            String path = Loader.cacheResource(AndroidFaceRecognizer.class, "windows-x86_64/jniAndroidFaceRecognizer.dll").getPath();
//            new ProcessBuilder("c:/scripts/depends.exe", path).start().waitFor();
//        }

       String serializedFile = args[0];
        File testImageDir = new File(args[1]);
        if(!testImageDir.exists())
            throw new IllegalArgumentException("no test image"); // ToDo change

        FilenameFilter imgFilter = makeImageFilter();


      FaceRecognizer faceRecognizer = AndroidFaceRecognizer.getFaceRecognizer(null,FaceRecognizerType.LBPHFaceFaces, serializedFile);

       testRecognizer(testImageDir, faceRecognizer);

    }


}
