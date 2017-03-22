package com.lordjoe.identifier;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import static com.lordjoe.identifier.OpenCVUtilities.saveAndLabelCroppedImage;

/**
 * com.lordjoe.identifier.SelectLabeledTrainingSet
 * User: Steve
 * Date: 3/17/2017
 */
public class SelectLabeledTrainingSet {
    public static final SelectLabeledTrainingSet[] EMPTY_ARRAY = {};
    public static final Random RND = new Random();

    private static void addToTrainingSet(File file,File outDir) throws IOException {

        File newFile = new File(outDir,file.getName());
        Files.move(file.toPath(),newFile.toPath());
    }


    public static void main(String[] args) throws Exception  {
        File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        double fractionSelected = Double.parseDouble(args[2]);
        FilenameFilter imageFilter = OpenCVUtilities.makeImageFilter();
        File[] dirs = inDir.listFiles(imageFilter);
        if(dirs == null)
            return;

        outDir.mkdirs();

        for (int i = 0; i < dirs.length; i++) {
            File file = dirs[i];
            if(RND.nextDouble() < fractionSelected)
                addToTrainingSet(file,outDir);

        }

    }

  }
