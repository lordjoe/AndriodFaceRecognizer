package com.lordjoe.identifier;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.bytedeco.javacpp.*;

import java.io.*;
import java.net.URL;

import java.util.*;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.util.Arrays;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.FrameRecorder.Exception;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_videoio.*;

/**
 * com.lordjoe.identifier.OpenCVUtilities
 * User: Steve
 * Date: 3/14/2017
 */
public class OpenCVUtilities {

    private static final int SCALE = 2;
    public static final int FACE_WIDTH = 200;
    public static final int FACE_HEIGHT = 200;
    public static final double MAX_RECOGNIZED_CONFIDENCE = 100;
    public static final double MIN_RECOGNIZED_CONFIDENCE = 0;

    public static final Random RND = new Random();
    public static final int READ_BLOCK = 100000;
    public static final String FACE_RECOGNITION_NAME = "FaceRecognition";
    public static final String HAAR_CLASSIFIER_RESOURCE2 = "com.lordjoe.identifier.haarcascade_frontalface_alt.xml";
    public static final String HAAR_CLASSIFIER_RESOURCE = "haarcascade_frontalface_alt.xml";
    private static opencv_objdetect.CvHaarClassifierCascade cascade;
    private static opencv_objdetect.CascadeClassifier faceDetector;
    private static CvMemStorage storage;
//    private static OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
     private static OpenCVFrameConverter.ToMat converter;

    public static ContextWrapper getAppContext() {
        return AppContext;
    }

    public static void setAppContext(ContextWrapper appContext) {
        AppContext = appContext;
    }

    private static ContextWrapper AppContext;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static boolean isLoaded = false;

    public static void guaranteeLoaded() {
        if (!isLoaded) {
            // System.setProperty("org.bytedeco.javacpp.logger.debug", "true");
            // preload the opencv_objdetect module to work around a known bug
            Loader.load(opencv_objdetect.class);
            isLoaded = true;
        }

    }


    public static IplImage doDetectFace(IplImage smallImg, double[] faceFractions) {
        int imageHeight = smallImg.height();
        int imageWidth = smallImg.width();

        // create temp storage, used during object detection
        if (storage == null)
            storage = CvMemStorage.create();

        // instantiate a classifier cascade for face detection
        opencv_objdetect.CvHaarClassifierCascade cascade = getHaarClassifier();
        CvSeq faces = cvHaarDetectObjects(smallImg, cascade, storage, 1.1, 3,
                CV_HAAR_DO_CANNY_PRUNING);
        // CV_HAAR_DO_ROUGH_SEARCH);
        // 0);

        // iterate over the faces and draw yellow rectangles around them
        int total = faces.total();
        CvRect r = new CvRect(cvGetSeqElem(faces, 0));
        if (r.address() == 0)
            return null;
        int height = r.height();
        int width = r.width();


        double heightFraction = (double) height / imageHeight;

        double widthFraction = (double) width / imageWidth;
        faceFractions[0] = Math.min(heightFraction, widthFraction);

        // After setting ROI (Region-Of-Interest) all processing will only be done on the ROI
        cvSetImageROI(smallImg, r);
        IplImage cropped = cvCreateImage(cvGetSize(smallImg), smallImg.depth(), smallImg.nChannels());
        // Copy original image (only ROI) to the cropped image
        cvCopy(smallImg, cropped);
        smallImg.release();
        cvClearMemStorage(storage);

        return cropped;
    }

//    public static CvMemStorage readAsMemory(InputStream is) {
//        CvMemStorage ret = CvMemStorage.create();
//        byte[] data = new byte[READ_BLOCK];
//        try {
//            int read = is.read(data);
//            while (read > 0) {
//                read = is.read(data);
//                ret.
//            }
//
//
//            return ret;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }
//    }


    /**
     * go through a directory placing all labeled files in label buckets
     *
     * @param directory
     * @return
     */
    public static Map<Integer, List<File>> findFilesWithLabel(File directory) {
        Map<Integer, List<File>> ret = new HashMap<>();
        for (File file : directory.listFiles(makeImageFilter())) {
            categorizeImageFile(file, ret);
        }
        return ret;
    }


    /**
     * find files with required numbers of duplicates
     *
     * @param inp               all labeled images
     * @param requiredDuplictes number duplicate images needed
     * @return
     */
    public static Map<Integer, List<File>> findCommonFilesWithLabel(Map<Integer, List<File>> inp, int requiredDuplictes) {
        Map<Integer, List<File>> ret = new HashMap<>();
        for (Integer label : inp.keySet()) {
            List<File> files = inp.get(label);
            if (files.size() >= requiredDuplictes)
                ret.put(label, files);
        }
        return ret;
    }


    /**
     * stick the file under the label
     *
     * @param file
     * @param ret
     */
    private static void categorizeImageFile(File file, Map<Integer, List<File>> ret) {
        Integer label = getLabelFromFile(file);
        List<File> files = ret.get(label);
        if (files == null) {
            files = new ArrayList<>();
            ret.put(label, files);

        }
        files.add(file);
    }

    public static opencv_objdetect.CascadeClassifier getFaceClassifier() {
        if (faceDetector == null) {
            loadFaceDetector();
        }
        return faceDetector;
    }

    public static void showFreeJavaMemory(String message) {
        System.out.print(message + " ");
        System.out.print("Free memory: " +
                Runtime.getRuntime().freeMemory());

  /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
  /* Maximum amount of memory the JVM will attempt to use */
        System.out.println(" Maximum memory: " + maxMemory);
    }

    public static void showFreeMemory(String message) {
        if (true)
            return;
    }


    public static void loadFaceDetector() {
        guaranteeLoaded();
        showFreeMemory("Before Face Detector Load");
       // URL resource = OpenCVUtilities.class.getResource(HAAR_CLASSIFIER_RESOURCE);
       // File file = new File(resource.getFile());

        File file = getDataFile(HAAR_CLASSIFIER_RESOURCE,null);
                String path = file.getAbsolutePath();
        faceDetector = new opencv_objdetect.CascadeClassifier(
                cvLoad(path));
        showFreeMemory("After Face Detector Load");

    }


    public static opencv_objdetect.CvHaarClassifierCascade getHaarClassifier() {
        if (cascade == null) {
            loadCascade();
        }
        return cascade;
    }

    private static void loadCascade() {
        guaranteeLoaded();


        URL resource = OpenCVUtilities.class.getResource(HAAR_CLASSIFIER_RESOURCE);
        File file = new File(resource.getFile());
        String path = file.getAbsolutePath();
        cascade = new opencv_objdetect.CvHaarClassifierCascade(
                cvLoad(path));

    }

    public static opencv_core.IplImage normailzedImage(File imageFile) {
        String path = imageFile.getAbsolutePath();
        guaranteeLoaded();
        opencv_core.IplImage origImg = cvLoadImage(path);

        // convert to grayscale
        opencv_core.IplImage grayImg = cvCreateImage(cvGetSize(origImg), IPL_DEPTH_8U, 1);
        cvCvtColor(origImg, grayImg, CV_BGR2GRAY);

        // scale the grayscale (to speed up face detection)
        opencv_core.IplImage smallImg = opencv_core.IplImage.create(grayImg.width() / SCALE,
                grayImg.height() / SCALE, IPL_DEPTH_8U, 1);
        cvResize(grayImg, smallImg, CV_INTER_LINEAR);

        // equalize the small grayscale
        cvEqualizeHist(smallImg, smallImg);
        origImg.release();
        grayImg.release();

        return smallImg;
    }


    public static Mat asMat(File f) {
        String path = f.getAbsolutePath();
        opencv_core.IplImage origImg = cvLoadImage(path);
        return asMat(origImg);

    }

    /**
     * return an image from a file with one face
     *
     * @param imageFile
     * @return
     */
    public static Mat extractFace(File imageFile) {
        // showFreeMemory("Before ExtractFace");

        System.out.println(imageFile.getAbsolutePath());
        IplImage smallImg = normailzedImage(imageFile);

        double[] fractionFace = {0};
        IplImage cropped = doDetectFace(smallImg, fractionFace);
        if (cropped == null)
            return null;
        IplImage resizedImage = IplImage.create(FACE_WIDTH, FACE_HEIGHT, cropped.depth(), cropped.nChannels());
        cvResize(cropped, resizedImage);
        int size = resizedImage.imageSize();

        //  Mat mat = asMat(resizedImage);
        File saveDir = new File("T:/training/cropped_unlabeled_faces");
        saveDir.mkdirs();
        File saveFile = new File(saveDir, imageFile.getName());
        cvSaveImage(saveFile.getAbsolutePath(), resizedImage);
        cropped.release();
        //    System.gc();
        //   showFreeMemory("After ExtractFace");
        return null;

    }

    /**
     * filter for image files
     *
     * @return
     */
    public static FilenameFilter makeImageFilter() {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };
    }

    public static void copyFile(File src, File dst) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dst);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * randomly choose unique elements
     *
     * @param collection     original collection - returns with unchosen elements
     * @param numberToChoose size of returned collection if  collection has this many elements
     * @param <T>
     * @return chosen items - no longer in original collection
     */
    public static <T> List<T> chooseUnique(List<T> collection, int numberToChoose) {
        List<T> ret = new ArrayList<>();
        if (collection.size() <= numberToChoose) {
            ret = new ArrayList<>(collection);
            collection.clear();
        } else {
            while (ret.size() < numberToChoose || collection.isEmpty()) {

                int size = collection.size();
                int choice = 0;
                if (size > 1)
                    choice = RND.nextInt(size);
                T chosen = collection.get(choice);
                ret.add(chosen);
                collection.remove(chosen);
            }
        }
        return ret;
    }


    public static <T> T chooseandRemove(List<T> collection) {
        T ret = collection.get(0);
        if (collection.size() == 1) {
            collection.clear();
        } else {
            ret = collection.get(RND.nextInt(collection.size()));
            collection.remove(ret);
        }
        return ret;
    }


    public static int getLabelFromFile(File file) {
        return getLabelFromFile(file.getName());
    }

    public static int getLabelFromFile(String name) {
        int label;
        label = Integer.parseInt(name.split("\\-")[0]);
        return label;
    }


    public static void saveAndVerifyCroppedImage(File imageFile, File saveDir, File badSaveDir, opencv_face.FaceRecognizer checker) {
        try {
            System.out.println(imageFile.getAbsolutePath());
            IplImage smallImg = normailzedImage(imageFile);

            double[] fractionFace = {0};

            IplImage cropped = doDetectFace(smallImg, fractionFace);
            if (cropped == null)
                return;
            IplImage resizedImage = IplImage.create(FACE_WIDTH, FACE_HEIGHT, cropped.depth(), cropped.nChannels());
            cvResize(cropped, resizedImage);

            // equalize the small grayscale
            cvEqualizeHist(resizedImage, resizedImage);

            int size = resizedImage.imageSize();

            File tempFile = new File("/tempImage.jpg");
            cvSaveImage(tempFile.getAbsolutePath(), resizedImage);
            Mat testImage = imread(tempFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            boolean imageIsOk = checkImage(testImage, checker, fractionFace[0]);
            testImage.release();

            File saveFile = new File(saveDir, imageFile.getName());
            if (!imageIsOk)
                saveFile = new File(badSaveDir, imageFile.getName());

            cvSaveImage(saveFile.getAbsolutePath(), resizedImage);
            cropped.release();
        } catch (RuntimeException e) {
            e.printStackTrace();    // ignore we expect a few errors
            return;

        }
    }


    public static final double BAD_CONFIDENCE_CUTOFF = 105;
    public static final double MINIMUM_FACE_FRACTION = 0.25;
    public static boolean checkImage(Mat testImage, opencv_face.FaceRecognizer checker, double faceFraction) {
//         opencv_face.StandardCollector standardCollector = opencv_face.StandardCollector.create();
//        checker.predict_collect(testImage,standardCollector);
//        IntDoublePairVector results = standardCollector.getResults(true);
//        long size = results.size();
//
//          double confidence  =  results.second(0) ;

        opencv_face.MinDistancePredictCollector collector = new opencv_face.MinDistancePredictCollector(1);
        checker.predict(testImage, collector);
        double confidence = collector.getDist();
        System.out.println("Confidence " + confidence + " fraction " + faceFraction);

        if (confidence > BAD_CONFIDENCE_CUTOFF)
            return false;
        if (faceFraction < MINIMUM_FACE_FRACTION)
            return false;

        return true;
    }

    public static void saveCroppedImage(File imageFile, File saveDir) {
        try {
            System.out.println(imageFile.getAbsolutePath());
            IplImage smallImg = normailzedImage(imageFile);

            double[] fractionFace = {0};
            IplImage cropped = doDetectFace(smallImg, fractionFace);
            if (cropped == null)
                return;
            IplImage resizedImage = IplImage.create(FACE_WIDTH, FACE_HEIGHT, cropped.depth(), cropped.nChannels());
            cvResize(cropped, resizedImage);

            // equalize the small grayscale
            cvEqualizeHist(resizedImage, resizedImage);

            int size = resizedImage.imageSize();

            File saveFile = new File(saveDir, imageFile.getName());
            cvSaveImage(saveFile.getAbsolutePath(), resizedImage);
            cropped.release();
        } catch (RuntimeException e) {
            e.printStackTrace();    // ignore we expect a few errors
            return;

        }
    }


    public static boolean saveAndLabelCroppedImage(File imageFile, File saveDir, int index) {
        try {
            String label = Integer.toString(index) + "-";
            System.out.println(imageFile.getAbsolutePath());
            IplImage smallImg = normailzedImage(imageFile);

            double[] fractionFace = {0};
            IplImage cropped = doDetectFace(smallImg, fractionFace);
            if (cropped == null)
                return false;
            IplImage resizedImage = IplImage.create(FACE_WIDTH, FACE_HEIGHT, cropped.depth(), cropped.nChannels());
            cvResize(cropped, resizedImage);

            // equalize the small grayscale
            cvEqualizeHist(resizedImage, resizedImage);

            int size = resizedImage.imageSize();

            String name = label + imageFile.getName();

            File saveFile = new File(saveDir, name);
            cvSaveImage(saveFile.getAbsolutePath(), resizedImage);
            cropped.release();
            return true; // success
        } catch (RuntimeException e) {
            e.printStackTrace();    // ignore we expect a few errors
            return false;

        }
    }


    public static int indexFromName(String fileName) {
        String test = fileName.replace("image_", "").replace(".jpg", "");
        return Integer.parseInt(test);
    }

    public static int indexFromTestName(String fileName) {

        String substring = fileName.substring(0, fileName.indexOf("-"));
        return Integer.parseInt(substring);
    }

    public static Mat asMat(IplImage resizedImage) {
        try {
            showFreeMemory("Before New Mat");
            Mat mat = new Mat(resizedImage.address());
            resizedImage.release();
            return mat;
        } catch (RuntimeException e) {
            showFreeMemory("After Failure");
            throw new RuntimeException(e);

        }
    }

    public static void doPredict(Mat testImage, opencv_face.FaceRecognizer faceRecognizer) {
//        opencv_face.StandardCollector standardCollector = opencv_face.StandardCollector.create();
//        faceRecognizer.predict_collect(testImage,standardCollector);
//        IntDoublePairVector results = standardCollector.getResults(true);

        opencv_face.MinDistancePredictCollector collector = new opencv_face.MinDistancePredictCollector(4);
        faceRecognizer.predict(testImage, collector);

        for (int i = 0; i < collector.sizeof(); i++) {
            collector.position(i);
            int index = collector.getLabel();
            double confidence = collector.getDist();
//            int index =  results.first(i) ;
//            double confidence =  results.second(i) ;
            System.out.println("index " + index + " confidence " + confidence);

        }
    }

    /**
     * return the top retainedResults  of File TestFile by  faceRecognizer
     *
     * @param testFile
     * @param faceRecognizer
     * @param retainedResults
     * @return
     */
    public static List<IdentificationResult> matchFile(File testFile, opencv_face.FaceRecognizer faceRecognizer, int retainedResults) {
        List<IdentificationResult> holder = new ArrayList<>();
        Mat testImage = imread(testFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
        String name = testFile.getName();
        int fileIndex = indexFromTestName(name);
//        opencv_face.StandardCollector standardCollector = opencv_face.StandardCollector.create();
//        faceRecognizer.predict_collect(testImage,standardCollector);
//        IntDoublePairVector results = standardCollector.getResults(true);
//        long size = results.size();
//        for (int i = 0; i <  size ; i++) {
//            int index =  results.first(i) ;
//            double confidence  =  results.second(i) ;
//            holder.add(new IdentificationResult(name,index,confidence));
//
//        }

        opencv_face.MinDistancePredictCollector collector = new opencv_face.MinDistancePredictCollector(1);
        faceRecognizer.predict(testImage, collector);
        List<IdentificationResult> ret = new ArrayList<>();

        collector.position(0);
        int index = collector.getLabel();
        double confidence = collector.getDist();
        ret.add(new IdentificationResult(name, index, confidence));

//        int sizeof = collector.sizeof();
//        for (int i = 0; i < sizeof; i++) {
//            collector.position(i);
//            int index = collector.getLabel();
//            double confidence = collector.getDist();
//            if (confidence < MAX_RECOGNIZED_CONFIDENCE && confidence >= MIN_RECOGNIZED_CONFIDENCE)
//                holder.add(new IdentificationResult(name, index, confidence));
//
//        }
//
//        List<IdentificationResult> ret = new ArrayList<>();
//        if (holder.isEmpty())
//            return ret;
//        if (holder.size() == 1) {
//            ret.add(holder.get(0));
//            return ret;
//        }
//        Collections.sort(holder);
//        for (int i = 0; i < Math.min(retainedResults, holder.size()); i++) {
//            ret.add(holder.get(i));
//
//        }
        return ret;
    }

    /**
     * return the top retainedResults  of File TestFile by  faceRecognizer
     *
       * @param faceRecognizer
     * @param retainedResults
     * @return
     */
    public static List<IdentificationResult> matchFiles(List<File> files, opencv_face.FaceRecognizer faceRecognizer, int retainedResults) {
        List<IdentificationResult> holder = new ArrayList<>();
        for (File file : files) {
            holder.addAll(matchFile(file, faceRecognizer, 3));
        }

        Collections.sort(holder);
        List<IdentificationResult> ret = new ArrayList<>();
        for (int i = 0; i < Math.min(retainedResults, holder.size()); i++) {
            ret.add(holder.get(i));

        }
        return ret;
    }


    public static boolean verifyIdentity(List<File> files, opencv_face.FaceRecognizer faceRecognizer, double[] confidence, int[] position, int testIndex) {
        final int numberFaces = faceRecognizer.sizeof();
        for (File testFile : files) {
        }

        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    public static final double MIN_ACCEPTED_FRACTION = 0.01;

    public static boolean verifyIdentity(Mat testImage, opencv_face.FaceRecognizer faceRecognizer, double[] confidence, int[] position, int testIndex) {
        final int numberFaces = faceRecognizer.sizeof();
        opencv_face.MinDistancePredictCollector collector = new opencv_face.MinDistancePredictCollector(3);
        faceRecognizer.predict(testImage, collector);
//        opencv_face.StandardCollector standardCollector = opencv_face.StandardCollector.create();
//        faceRecognizer.predict_collect(testImage,standardCollector);
//        IntDoublePairVector results = standardCollector.getResults(true);

        long size = collector.sizeof();

        int minAccepted = (int) Math.max(1, MIN_ACCEPTED_FRACTION * size);
        for (int i = 0; i < size; i++) {
            collector.position(i);
            position[0] = collector.getLabel();
            confidence[0] = collector.getDist();
            if (collector.getLabel() == testIndex)
                return i < minAccepted; // identified

        }

        position[0] = 0;
        confidence[0] = 0;
        return false; // did not find
    }

    public static final int DROPPED_FRAMES = 10;
    public static final int SAVED_FRAMES = 20;
    public static final int FRAME_DELAY = 100; // millisec

//    public static List<Mat>  facesFromVideo(String fileName,int number)
//    {
//        List<Mat> ret = new ArrayList<>(number);
//        FrameGrabber grabber = new OpenCVFrameGrabber(fileName);
//        try {
//            grabber.start();
//            IplImage grab  = grabber.grab();
//               for (int i = 0; i < DROPPED_FRAMES; i++) {
//                  grab = grabber.grab();
//                }
//
//            while(ret.size() < SAVED_FRAMES)  {
//                grab = grabber.grab();
//                Mat mattemp = new Mat(grab.address());
//                ret.add(mattemp);
//                delay(FRAME_DELAY);
//
//            }
//            grabber.release();
//            return ret;
//        } catch (FrameGrabber.Exception e) {
//            throw new RuntimeException(e);
//
//        }
//    }

    public static void delay(int d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException ex) {
        }
    }

    public static void moveFiles(List<File> filesWithLabel, File outDir) {
        try {
            for (File file : filesWithLabel) {
                File newFile = new File(outDir, file.getName());
                file.renameTo(newFile);
            }
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);

        }
    }

    public static void copyFiles(List<File> filesWithLabel, File outDir) {
        outDir.mkdirs();
        try {
            for (File file : filesWithLabel) {
                File newFile = new File(outDir, file.getName());
                copyFile(file, newFile);
            }
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);

        }
    }


    public static opencv_face.FaceRecognizer createFaceRecognizerOfType(FaceRecognizerType type) {
        switch (type) {
            case EigenFaces:
                return createEigenFaceRecognizer();
            case LBPHFaceFaces:
                return createLBPHFaceRecognizer();
            case FischerFaces:
                return createFisherFaceRecognizer();
            default:
                throw new IllegalStateException("never get here");

        }

    }

//    /**
//     * Checks if the app has permission to write to device storage
//     * <p>
//     * If the app does not has permission then the user will be prompted to grant permissions
//     *
//     * @param activity
//     */
//    public static void verifyStoragePermissions(Activity activity) {
//        // Check if we have write permission
//        int permission = ActivityCompat.(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//        permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//    }

    public static File getDataRoot() {
        return Environment.getExternalStorageDirectory();
//        ContextWrapper appContext = getAppContext();
//        return appContext.getExternalFilesDir(null);
    }

    public static File getRecognitionRoot() {
        return getDataFile(FACE_RECOGNITION_NAME, getDataRoot());
    }

    public static File getDataFile(String name, File dir) {
        if (dir == null)
            dir =  getDataRoot(); //getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        return new File(dir, name);
    }

    public static void writeTextFile(String fileName, String text) {
        File dataRoot = getDataRoot();
        writeTextFile(dataRoot, fileName, text);
    }


    public static void writeTextFile(File dir, String fileName, String text) {
        BufferedWriter writer = null;
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //create a temporary file
            File logFile = new File(dir, fileName);
            boolean preExisting = logFile.exists();

            // This will output the full path where the file will be written to...
            Log.e("outFile", logFile.getAbsolutePath());

            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (IOException e) {
            }
        }
    }


    public static String readTextFile(File f) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader("file.txt"));
            try {
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
            } finally {
                br.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }


}
