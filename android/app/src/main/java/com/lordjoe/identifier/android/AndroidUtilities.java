package com.lordjoe.identifier.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

/**
 * Created by Steve on 4/14/2017.
 */

public class AndroidUtilities {

    /**
     * read a bitmap from a file and return a bitmap of a specific size
     * @param f existing file
     * @param desiredHeight
     * @param desiredWidth
     * @return
     */
    public static Bitmap fromFile(File f,int desiredHeight,int desiredWidth) {
        Bitmap bmp = fromFile(  f);
        return getResizedBitmap(bmp,desiredHeight,desiredWidth);

    }

    /**
     *  read a bitmap from a file
     * @param f existing file
     * @return
     */
    public static Bitmap fromFile(File f)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        return bitmap;
    }
    /**
     * resize a bitmap
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
