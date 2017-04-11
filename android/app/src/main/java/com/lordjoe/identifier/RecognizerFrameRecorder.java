/*
 * Copyright (C) 2009-2012 Samuel Audet
 *
 * Licensed either under the Apache License, Version 2.0, or (at your option)
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation (subject to the "Classpath" exception),
 * either version 2, or any later version (collectively, the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     http://www.gnu.org/licenses/
 *     http://www.gnu.org/software/classpath/license.html
 *
 * or as provided in the LICENSE.txt file that accompanied this code.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lordjoe.identifier;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_videoio.CV_FOURCC_DEFAULT;
import static org.bytedeco.javacpp.opencv_videoio.CV_FOURCC_PROMPT;
import static org.bytedeco.javacpp.opencv_videoio.VideoWriter;

/**
 *
 * @author Samuel Audet
 */
public class RecognizerFrameRecorder extends FrameRecorder {
    public static RecognizerFrameRecorder createDefault(File f, int w, int h)   throws Exception { return new RecognizerFrameRecorder(f, w, h); }
    public static RecognizerFrameRecorder createDefault(String f, int w, int h) throws Exception { return new RecognizerFrameRecorder(f, w, h); }

    private static Exception loadingException = null;
    public static void tryLoad() throws Exception {
        if (loadingException != null) {
            throw loadingException;
        } else {
            try {
                Loader.load(org.bytedeco.javacpp.opencv_highgui.class);
            } catch (Throwable t) {
                throw loadingException = new Exception("Failed to load " + RecognizerFrameRecorder.class, t);
            }
        }
    }

    public RecognizerFrameRecorder(File file, int imageWidth, int imageHeight) {
        this(file.getAbsolutePath(), imageWidth, imageHeight);
    }
    public RecognizerFrameRecorder(String filename, int imageWidth, int imageHeight) {
        this.filename    = filename;
        this.imageWidth  = imageWidth;
        this.imageHeight = imageHeight;

        this.pixelFormat = 1;
        this.videoCodec  = windows ? CV_FOURCC_PROMPT : CV_FOURCC_DEFAULT;
        this.frameRate   = 30;
    }
    public void release() throws Exception {
        if (writer != null) {
            writer.release();
            writer = null;
        }
    }
    @Override protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    private static final boolean windows = Loader.getPlatform().startsWith("windows");
    private String filename;
    private VideoWriter writer = null;
    private OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    public void start() throws Exception {
        writer = new VideoWriter(filename, fourCCCodec(), frameRate, new Size(imageWidth, imageHeight), isColour());
    }

    /**
     * Pixel format is an int and maps to colour if != 0, greyscale otherwise.
     */
    private boolean isColour() {
        return pixelFormat != 0;
    }

    /**
     * VideoCodec in JavaCV jargon is the same as FourCC code in OpenCV speak
     */
    private int fourCCCodec() {
        return videoCodec;
    }

    public void stop() throws Exception {
        release();
    }

    public void record(Frame frame) throws Exception {
        Mat mat = converter.convert(frame);
        if (writer != null) {
            writer.write(mat);
        } else {
            throw new Exception("Cannot record: There is no writer (Has start() been called?)");
        }
        frame.keyFrame = true;
    }
}
