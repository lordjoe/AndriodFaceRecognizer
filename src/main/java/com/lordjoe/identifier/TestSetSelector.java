package com.lordjoe.identifier;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.lordjoe.identifier.OpenCVUtilities.chooseandRemove;
import static com.lordjoe.identifier.OpenCVUtilities.getLabelFromFile;

/**
 * com.lordjoe.identifier.ImageCropper
 * User: Steve
 * Date: 3/17/2017
 * ifw 5749 Failures 279
 */
public class TestSetSelector {
    public static final TestSetSelector[] EMPTY_ARRAY = {};
    public static final double TEST_SET_FRACTION = 0.1;

      public static final Random RND = new Random();

    public static void main(String[] args) {
        File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        FilenameFilter imageFilter = OpenCVUtilities.makeImageFilter();
        File[] dirs = inDir.listFiles(imageFilter);
         if(dirs == null)
            return;
        Arrays.sort(dirs);

        outDir.mkdirs();
        int currentLabel  = 0;

        List<File> allFiles = new ArrayList<File>(Arrays.asList(dirs));
        List<File> filesWithLabel = new ArrayList<File>();
        int length = dirs.length;
        int desiredSize =  (int)(TEST_SET_FRACTION * length);

        while(filesWithLabel.size() < desiredSize)  {
            filesWithLabel.add(chooseandRemove(allFiles));

        }
        moveFiles(filesWithLabel,outDir);
    }



    private static void moveFiles(List<File> filesWithLabel, File outDir) {
        try {
            for (File file : filesWithLabel) {
                 File newFile = new File(outDir,file.getName());
                Files.move(file.toPath(),newFile.toPath());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }


}
