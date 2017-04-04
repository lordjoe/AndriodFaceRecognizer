package com.lordjoe.identifier;



import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.*;

import static com.lordjoe.identifier.OpenCVUtilities.*;

/**
 * com.lordjoe.identifier.IFWImagePruner
 * Utilities to sort through a file set and either cur or find labels with a lot of images
 * User: Steve
 * Date: 3/17/2017
 * ifw 5749 Failures 279
 */
public class IFWImagePruner {
    public static final IFWImagePruner[] EMPTY_ARRAY = {};

    public static final int MAX_IMAGES_PER_PERSON = 10;
    public static final Random RND = new Random();


    private static void processLabeledFiles(List<File> filesWithLabel, File outDir) {
        if (filesWithLabel.isEmpty())
            return;
        if (filesWithLabel.size() <= MAX_IMAGES_PER_PERSON) {
            saveFiles(filesWithLabel, outDir);
        } else {
            List<File> chosenFiles = chooseFiles(filesWithLabel);
            saveFiles(chosenFiles, outDir);
        }

        filesWithLabel.clear();
    }

    private static List<File> chooseFiles(List<File> filesWithLabel) {
        List<File> chosen = new ArrayList<File>();
        while (chosen.size() < MAX_IMAGES_PER_PERSON) {
            chosen.add(chooseandRemove(filesWithLabel));
        }
        return chosen;
    }

    private static <T> T chooseandRemove(List<T> collection) {
        T ret = collection.get(0);
        if (collection.size() == 1) {
            collection.clear();
        } else {
            ret = collection.get(RND.nextInt(collection.size()));
            collection.remove(ret);
        }
        return ret;
    }

    private static void saveFiles(List<File> filesWithLabel, File outDir) {
        try {
            for (File file : filesWithLabel) {
                copyFile(file, outDir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }


    public static void pruneNumberFilesWithLabel(String[] args) {
        File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        FilenameFilter imageFilter = OpenCVUtilities.makeImageFilter();
        File[] dirs = inDir.listFiles();
        if (dirs == null)
            return;
        Arrays.sort(dirs);

        outDir.mkdirs();
        int currentLabel = 0;

        List<File> filesWithLabel = new ArrayList<File>();

        int length = dirs.length;
        for (int i = 0; i < length; i++) {
            File dir = dirs[i];
            int label = getLabelFromFile(dir);
            if (label == currentLabel) {
                filesWithLabel.add(dir);
            } else {
                processLabeledFiles(filesWithLabel, outDir);
                currentLabel = label;
                filesWithLabel.add(dir);
                System.out.println(currentLabel);
            }

        }
        processLabeledFiles(filesWithLabel, outDir);
    }


    public static void copyFilesWithCommonLabels(String[] args) {
         File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        int requiredDuplictes = Integer.parseInt(args[2]);
        int max_images = requiredDuplictes;
          outDir.mkdirs();

        Map<Integer, List<File>> allFiles = findFilesWithLabel(inDir);
        Map<Integer, List<File>> commonFiles = findCommonFilesWithLabel(allFiles, requiredDuplictes);

        for (List<File> files : commonFiles.values()) {
            List<File> selected = OpenCVUtilities.chooseUnique(files, requiredDuplictes);
            OpenCVUtilities.copyFiles(selected,outDir);
        }

    }

    public static void main(String[] args) {
        guaranteeLoaded();
   //     pruneNumberFilesWithLabel(args);
        copyFilesWithCommonLabels(args);
    }


}
