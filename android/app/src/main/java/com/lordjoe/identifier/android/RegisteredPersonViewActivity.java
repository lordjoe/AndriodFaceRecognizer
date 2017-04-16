package com.lordjoe.identifier.android;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lordjoe.identifier.R;
import com.lordjoe.identifier.RecognizerFrameRecorder;
import com.lordjoe.identifier.RegisteredPerson;
import com.lordjoe.identifier.RegisteredPersonSet;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RegisteredPersonViewActivity extends Activity   {

    private final static String CLASS_LABEL = "RegisteredPerson";
    private final static String LOG_TAG = CLASS_LABEL;

    /* layout setting */

    private int imageWidth = 250; // 320;
    private int imageHeight = 150; // 240;
         /* video data getting thread */
    private ImageView faceView;

    private int screenWidth, screenHeight;
    private TextView nameView;
    private ImageView exemplarView;
    private Spinner trainingView;
    private MainActivity activityParent;
    private RegisteredPerson person;

    public RegisteredPerson getPerson() {
        return person;
    }

    public void setPerson(RegisteredPerson person) {
        if(this.person == person)
            return;
        this.person = person;
        String id = Integer.toString(person.getId());
        nameView.setText(person.getName() + " " + id);
        File exemplar = person.getExemplar();
        Bitmap bitmap = AndroidUtilities.fromFile(exemplar, imageWidth, imageHeight);
        exemplarView.setImageBitmap(bitmap);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityParent = MainActivity.getActiveInstance();

        setContentView(R.layout.activity_show_registered);

         initLayout();

        RegisteredPersonSet registeredPeople = activityParent.getRegisteredPeople();
        RegisteredPerson p = registeredPeople.getPerson(0);
         setPerson(p);
    }



    private void initLayout() {

        /* get size of screen */
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        RelativeLayout.LayoutParams layoutParam = null;
        LayoutInflater myInflate = null;
        myInflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout topLayout = new RelativeLayout(this);
        setContentView(topLayout);
        LinearLayout preViewLayout = (LinearLayout) myInflate.inflate(R.layout.activity_record, null);
        layoutParam = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        topLayout.addView(preViewLayout, layoutParam);
        nameView =  (TextView)findViewById(R.id.personName);
        exemplarView =  (ImageView)findViewById(R.id.personExemplar);
        trainingView = (Spinner) findViewById(R.id.personImages);
        Log.e(LOG_TAG, "cameara preview start: OK");
    }

     private class MyCallback implements PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
  }



}
