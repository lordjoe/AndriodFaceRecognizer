package com.lordjoe.identifier;

import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.nio.IntBuffer;
import java.util.*;

import static com.lordjoe.identifier.OpenCVUtilities.getLabelFromFile;
import static com.lordjoe.identifier.OpenCVUtilities.saveAndLabelCroppedImage;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

/**
 * com.lordjoe.identifier.RegisteredPersonSet
 * User: Steve
 * Date: 4/10/2017
 */
public class RegisteredPersonSet {
    public static final String TAG_NAME = "RegisteredPersonSet";
    public static final double MINIMUM_CONFIDENCE = 0.75;
    public static final String EXAMPLAR_STRING = "Exemplar";
    
    

    private final Map<Integer,RegisteredPerson> registeredPeople = new HashMap<>();
    private final Map<String,RegisteredPerson> registeredNames = new HashMap<>();
    private final List<RegisteredPerson> people = new ArrayList<>();
    private final File storeDirectory;
    private final opencv_face.FaceRecognizer recognizer;
    private final opencv_objdetect.CvHaarClassifierCascade haarClassifier;
    private final FaceRecognizerType type;

    public RegisteredPersonSet(File storeDirectory,FaceRecognizerType type ) {
        this.storeDirectory = storeDirectory;
        this.type = type;
        recognizer = OpenCVUtilities.createFaceRecognizerOfType(type);
        haarClassifier = OpenCVUtilities.getHaarClassifier();
          buildFromFiles();
    }

    private void buildFromFiles() {
        File[] files = storeDirectory.listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                RegisteredPerson rp = buildRegisteredPerson(file);
            }
            trainFromFiles();
        }

    }

    private void trainFromFiles()
    {
        List<File> imageFiles  = getTrainingFiles() ;
        opencv_core.Mat labels = new opencv_core.Mat(imageFiles.size(), 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();
        opencv_core.MatVector images = new opencv_core.MatVector(imageFiles.size());

        int counter = 0;

        for (File image : imageFiles) {
            opencv_core.Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            opencv_core.Mat smaller = null;
            //    org.bytedeco.javacpp.opencv_imgcodecs.
            int label = 0;
            String name = image.getName();
            label = getLabelFromFile(name);
            images.put(counter, img);
             labelsBuf.put(counter, label);
           counter++;
        }


        recognizer.train(images, labels);


    }

    private RegisteredPerson buildRegisteredPerson(File file) {
        String fileName = file.getName();
        String substring = fileName.substring(fileName.lastIndexOf("_") + 1);
        Integer id = new Integer(substring);
        String name =  fileName.replace("_" +  id,"");
        while(name.endsWith("_"))
            name = name.substring(0,name.length() - 1);
        File[] files = file.listFiles();
        if(files == null)
            return null;
        List<File> images = new ArrayList<>();
        File examplar = null;
        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            String name1 = file1.getName();
            if(name1.startsWith(EXAMPLAR_STRING))
                examplar = file1;
            else
                images.add(file1);
        }

        return registerOnePerson(name,id,examplar,images);
    }

    public IdentificationResult identify(List<File> images )    {
        int retainedResults = 1;
        List<IdentificationResult>   results = OpenCVUtilities.matchFiles(images,getRecognizer(), retainedResults);
        IdentificationResult ret = results.get(0);
        if(ret.confidence > MINIMUM_CONFIDENCE)
            return ret;
        return null;
    }

    public List<File> getTrainingFiles()
    {
        List<File> ret = new ArrayList<>();
        for (RegisteredPerson registeredPerson : people) {
             List<File> sampleImages = registeredPerson.getSampleImages();
            ret.addAll(sampleImages);
        }
        return ret;
    }

    public boolean isTrainable() {
        return type.isTrainable();
    }


    public File getStoreDirectory() {
        return storeDirectory;
    }

    public opencv_face.FaceRecognizer getRecognizer() {
        return recognizer;
    }

    public FaceRecognizerType getType() {
        return type;
    }

    public Integer findUnusedId()    {
        return findUnusedId(1);
    }

    public Integer findUnusedId(int ret)    {
          while(registeredPeople.containsKey(ret))    {
            ret++;
        }
        return ret;
    }

    public  RegisteredPerson addPerson(String name,File imageDirectory)
    {
        File[] a = imageDirectory.listFiles();
        List<File> images = new ArrayList<>(Arrays.asList(a));// defensive copy top allow remove
       if(name == null) {
           String filename = images.get(0).getName();
           name = filename.substring(0,filename.indexOf(".") );
       }
       Integer id = findUnusedId();
        List<File> testFiles = OpenCVUtilities.chooseUnique(images, 1);
        File examplar = testFiles.get(0);
         return addPerson(name,id,examplar,images);
    }

    private RegisteredPerson addPerson(String name, Integer id, File examplar, List<File> images) {
        File base = getStoreDirectory();
        String dirName = name;
        String suffix = "_" + id;
        if(!name.endsWith(suffix))
            dirName = name + "_" + id      ;
        else
            name = name.replace(suffix,"");

        File baseDir = new File(base,dirName  )     ;

        if(baseDir.exists())
            throw new IllegalStateException("must make new directory");
        if(!baseDir.mkdirs())
            throw new IllegalStateException("cannot make directory " + baseDir.getAbsolutePath());  ;
        File examplarFile = new File(baseDir,EXAMPLAR_STRING + "_" + dirName  + ".jpg");
        OpenCVUtilities.copyFile(examplar,examplarFile);
        List<File>  croppedImages = new ArrayList<>();
        int index = 1;
        for (File imageFile : images) {
            saveAndLabelCroppedImage( imageFile, baseDir,id);
            croppedImages.add(new File(baseDir,id.toString() + "-" + imageFile.getName()));
           }
        return new RegisteredPerson(name,id,examplarFile,croppedImages);

    }

    public RegisteredPerson registerOnePerson(String name,Integer id,List<File> imagesx) {
        List<File> images = new ArrayList<>(imagesx);// defensive copy top allow remove
        List<File> testFiles = OpenCVUtilities.chooseUnique(images, 1);
        File examplar = testFiles.get(0);
        images.remove(examplar) ;
        return registerOnePerson(name, id, examplar, images);
    }

    public RegisteredPerson registerOnePerson(String name,Integer id, File examplar,List<File> images)
    {
        if(id == null)
            return  registerOnePerson(  name,findUnusedId() , images);
        if(name == null)
            return  registerOnePerson(  "Person_" + id,findUnusedId() , images);

        if(registeredPeople.containsKey(id))
            throw new IllegalStateException("Id must ne unique " + id );

        if(registeredNames.containsKey(name))
            name = name + "_" + id;

        RegisteredPerson person = new RegisteredPerson(name,id,examplar,images); //makeRegisteredPerson(name,  id,examplar, images);
        registeredPeople.put(id,person) ;
        registeredNames.put(name,person) ;
        people.add(person);
        return person;

    }

    public RegisteredPerson makeRegisteredPerson(String name, Integer id,File examplar, List<File> images) {
        File base = getStoreDirectory();
          String dirName = name;
        String suffix = "_" + id;
        if(!name.endsWith(suffix))
            dirName = name + "_" + id      ;
       else
           name = name.replace(suffix,"");

        File baseDir = new File(base,dirName  )     ;

        if(baseDir.exists())
            throw new IllegalStateException("must make new directory");  
        if(!baseDir.mkdirs())
            throw new IllegalStateException("cannot make directory " + baseDir.getAbsolutePath());  ;
         File examplarFile = new File(baseDir,EXAMPLAR_STRING + "_" + dirName  + ".jpg");
        OpenCVUtilities.copyFile(examplar,examplarFile);
        int index = 1;
        List<File>  croppedImages = new ArrayList<>();
        for (File image : images) {
            File dest = new File(baseDir,id + "-" + name + "_" + index++ + ".jpg");
            OpenCVUtilities.copyFile(image,dest);
             croppedImages.add(dest);
        }
        return new RegisteredPerson(name,id,examplar,croppedImages);
    }

    public static void main(String[] args) {
        RegisteredPersonSet rp = buildPersonSet(args);

        RegisteredPerson registeredPerson = rp.addPerson(null, new File(args[1]));
        File exemplar = registeredPerson.getExemplar();

    }

    private static void testFile(File testFile, RegisteredPersonSet rp) {
        int labelFromFile = OpenCVUtilities.getLabelFromFile(testFile);
         opencv_face.FaceRecognizer recognizer = rp.getRecognizer();
        List<IdentificationResult> identificationResults = OpenCVUtilities.matchFile(testFile, recognizer, 1);
        IdentificationResult result = identificationResults.get(0);
        if(labelFromFile != result.label)
            throw new IllegalStateException("problem"); // ToDo change

    }

    public static RegisteredPersonSet buildPersonSet(String[] args)
    {
        RegisteredPersonSet ret = new RegisteredPersonSet(new File(args[0]),FaceRecognizerType.LBPHFaceFaces);
        File storeDirectory = ret.storeDirectory;
        return ret;

    }

    public int size() {
        return people.size();
    }

    public RegisteredPerson getPerson(int index) {
        Log.e(TAG_NAME,"register person");
        return people.get(index);
    }
//
//    private static void createCaltechPersonSet(String[] args) {
//        File basedir = new File(args[0]) ;
//        File destDir = new File(args[1]) ;
//        RegisteredPersonSet set = new RegisteredPersonSet(destDir,FaceRecognizerType.LBPHFaceFaces);
//        File[] files = basedir.listFiles();
//        for (File file : files) {
//            makeCaltechRegisteredPerson(set,file);
//        }
//    }
//
//    private static void makeCaltechRegisteredPerson( RegisteredPersonSet set,File file) {
//
//        String name1 = file.getName();
//        String substring = name1.substring(name1.lastIndexOf("_") + 1);
//        int id = new Integer(substring) ;
//        String name = "Caltech_" + id;
//        File[] files = file.listFiles();
//        File examplar = null;
//        List<File>  images = new ArrayList<File>(Arrays.asList(files)) ;
//        for (File image : images) {
//            if(image.getName().startsWith(EXAMPLAR_STRING))   {
//                examplar = image;
//                images.remove(image) ;
//                break;
//            }
//        }
//        set.registerOnePerson( name,id,examplar,images) ;
//
//    }
//
//
//    private static void createIFWPersonSet(String[] args) {
//        File basedir = new File(args[0]) ;
//        File destDir = new File(args[1]) ;
//        RegisteredPersonSet set = new RegisteredPersonSet(destDir,FaceRecognizerType.LBPHFaceFaces);
//        File[] files = basedir.listFiles();
//        for (File file : files) {
//            makeIFWRegisteredPerson(set,file);
//        }
//    }
//
//    public static String ifwNameFromFile(File fnale)  {
//        String ret = fnale.getName();
//        ret = ret.substring(ret.lastIndexOf("-") + 1);
//        ret = ret.substring(0,ret.lastIndexOf("_") + 1);
//        return ret;
//    }
//
//    private static void makeIFWRegisteredPerson( RegisteredPersonSet set,File file) {
//
//        String name1 = file.getName();
//        String substring = name1.substring(name1.lastIndexOf("_") + 1);
//        int id = new Integer(substring) ;
//        String name = "IFW" + id;
//        File[] files = file.listFiles();
//        File examplar = null;
//        List<File>  images = new ArrayList<File>(Arrays.asList(files)) ;
//        for (File image : images) {
//            if(image.getName().startsWith(EXAMPLAR_STRING))   {
//                examplar = image;
//                images.remove(image) ;
//                break;
//            }
//        }
//        name =  ifwNameFromFile(images.get(0))  + "_" + id;
//        set.registerOnePerson( name,id,examplar,images) ;
//
//    }


}
