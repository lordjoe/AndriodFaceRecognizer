package com.lordjoe.identifier;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.*;

import static com.lordjoe.identifier.OpenCVUtilities.chooseandRemove;
import static com.lordjoe.identifier.OpenCVUtilities.findFilesWithLabel;
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
        int numberToChoose = 5;
        if(args.length > 2)
            numberToChoose = Integer.parseInt(args[2]);

        outDir.mkdirs();

        Map<Integer,List<File>> filesWithLabel = findFilesWithLabel(inDir);

        for (Integer label : filesWithLabel.keySet()) {
            List<File> files = filesWithLabel.get(label);
            List<File> testFiles = OpenCVUtilities.chooseUnique(files, numberToChoose);
            OpenCVUtilities.moveFiles(testFiles,outDir);
        }



     }





}
