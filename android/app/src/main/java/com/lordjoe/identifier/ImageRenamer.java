package com.lordjoe.identifier;

import java.io.File;
import java.io.FilenameFilter;

import static com.lordjoe.identifier.OpenCVUtilities.indexFromName;
import static com.lordjoe.identifier.OpenCVUtilities.makeImageFilter;


/**
 * com.lordjoe.identifier.ImageRenamer
 * User: Steve
 * Date: 3/17/2017
 */
public class ImageRenamer {
    public static final ImageRenamer[] EMPTY_ARRAY = {};
    private static int[] people_boundaries = {
            1,
            20,
            40,
            45,
            67,
            88,
            111,
            131,
            136,
            157,
            164,
            169,
            174,
            194,
            215,
            240,
            262,
            267,
            286,
            306,
            335,
            355,
            375,
            397,
            398,
            399,
            400,
            401,
            402 ,
            407,
            429,
            451
    };


    private static int processFile(File file, int index,File outDir) {
        int boundary =  people_boundaries[index];
        String name = file.getName();
        int testIndex  =  indexFromName(name);
        if(testIndex >= boundary)
            index++;
        String newName = Integer.toString(index) + "-"  + name;
        File dst = new File(outDir,newName);
        OpenCVUtilities.copyFile(file, dst);
        return index;

    }


    public static void main(String[] args) {
        File sourceDir = new File(args[0]);
        File outDir = new File(args[1]);
        outDir.mkdirs();
        FilenameFilter imgFilter = makeImageFilter();

        File[] files = sourceDir.listFiles(imgFilter);
        if(files == null)
            return;

        int index = 0;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            index = processFile(file,index,outDir);

        }


    }

}
