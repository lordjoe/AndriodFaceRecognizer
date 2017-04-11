package com.lordjoe.identifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.lordjoe.identifier.OpenCVUtilities.getLabelFromFile;

/**
 * com.lordjoe.identifier.RegisteredPerson
 * User: Steve
 * Date: 4/7/2017
 */
public class RegisteredPerson {

    public static List<RegisteredPerson> readRegisteredPeople(File dir) {
        List<RegisteredPerson> ret = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                RegisteredPerson added = createRegisteredPerson(file);
                if (added != null)
                    ret.add(added);
            }
        }
        return ret;
    }


    public static RegisteredPerson createRegisteredPerson(File dir) {
        RegisteredPerson ret = null;
        File[] files = dir.listFiles();
        List<File> images = new ArrayList<>();
        Integer id = null;
        if (files != null) {
            File exemplar = null;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.getName().startsWith("Examplar")) {
                    exemplar = file;
                    break;
                }
            }
            if (exemplar == null)
                return ret;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (!file.equals(exemplar)) {
                    if (id == null)
                        id = OpenCVUtilities.getLabelFromFile(file);
                    images.add(file);
                }
            }
            if (id == null)
                return ret;
            ret = new RegisteredPerson(dir.getName(), id, exemplar, images);
        }
        return ret;
    }


    public static String buildExamplarName(int id) {
        return "Exemplar-" + id + ".jpg";
    }


    public static Bitmap getExamplar(int id, File dir) {
        File imageFile = new File(dir, buildExamplarName(id));
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        return bitmap;
    }

    public static List<File> imagesFromDir(File dir)
    {
        File[] files = dir.listFiles();
        List<File> images = new ArrayList<>();
        Integer id = null;
        File exemplar = exemplarFromDir(dir);
         if (exemplar == null)
            throw new IllegalArgumentException("problem"); // ToDo change;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.equals(exemplar)) {
                images.add(file);
            }
        }
        return images;
    }
    public static File exemplarFromDir(File dir)
    {
        File[] files = dir.listFiles();
           File exemplar = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().startsWith("Examplar")) {
                exemplar = file;
                break;
            }
        }

        return exemplar;
    }

    public static Integer idFromDir(File dir)
    {
        File[] files = dir.listFiles();
         Integer id = null;
        File exemplar = exemplarFromDir(dir);
          if (exemplar == null)
            throw new IllegalArgumentException("problem"); // ToDo change;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.equals(exemplar)) {
                if (id == null)
                   return(OpenCVUtilities.getLabelFromFile(file));
               }
        }

          throw new IllegalArgumentException("problem"); // ToDo change;
      }

    private String name;
    private int id;
    private File exemplar;
    private List<File> sampleImages;


    public  RegisteredPerson(String namex, Integer idx, File exemplar, List<File> sampleImages) {
        this.name = namex;
        this.id = idx;
        this.exemplar = exemplar ;
        this.sampleImages = new ArrayList<>(sampleImages);
    }


    public RegisteredPerson(File dir) {
        this(dir.getName(), idFromDir(  dir), exemplarFromDir( dir), imagesFromDir( dir));
    }





    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public File getExemplar() {
        return exemplar;
    }

    public Bitmap getExemplarBitmap() {
        return BitmapFactory.decodeFile(getExemplar().getAbsolutePath());
 
    }

    public List<File> getSampleImages() {
        return new ArrayList<>(sampleImages);
    }
}
