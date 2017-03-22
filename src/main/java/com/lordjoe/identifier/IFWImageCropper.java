package com.lordjoe.identifier;

import java.io.File;
import java.io.FilenameFilter;

import static com.lordjoe.identifier.OpenCVUtilities.saveAndLabelCroppedImage;
import static com.lordjoe.identifier.OpenCVUtilities.saveCroppedImage;

/**
 * com.lordjoe.identifier.ImageCropper
 * User: Steve
 * Date: 3/17/2017
 * ifw 5749 Failures 279
 */
public class IFWImageCropper {
    public static final IFWImageCropper[] EMPTY_ARRAY = {};



    public static void main(String[] args) {
        File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        FilenameFilter imageFilter = OpenCVUtilities.makeImageFilter();
        File[] dirs = inDir.listFiles();
        if(dirs == null)
            return;

        outDir.mkdirs();
        int failures = 0;

        int length = dirs.length;
        for (int i = 0; i < length; i++) {
            File dir = dirs[i];
            File[] images = dir.listFiles(imageFilter);
            if(images == null)
                continue;
            for (File image : images) {
                if(!saveAndLabelCroppedImage(image,outDir,i + 1))
                    failures++;
            }

        }
        System.out.println("Cropped " + length + " Failures " + failures );

    }


}
