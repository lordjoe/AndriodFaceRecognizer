package com.lordjoe.identifier;

import java.io.File;
import java.io.FilenameFilter;

import java.util.*;

import static com.lordjoe.identifier.OpenCVUtilities.chooseandRemove;
import static com.lordjoe.identifier.OpenCVUtilities.findFilesWithLabel;
import static com.lordjoe.identifier.OpenCVUtilities.getLabelFromFile;

/**
 * com.lordjoe.identifier.ImageCropper
 * User: Steve
 * Date: 3/17/2017
 * ifw 5749 Failures 279
 *
 * *
 * Sample command line
 * T:\training\caltech_training   T:\training\caltech_test 5
 * arg 0 saved directory with labeled cropped faces
 * arg 1 directory of   test images
 * arg 2 number test cases
 * select a  number test cases from the set of files in the source directory and move them to the test directory
 * NOTE - start with ALL images in the source directory and and with a test and training set
 *

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
