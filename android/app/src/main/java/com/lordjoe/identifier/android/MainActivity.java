package com.lordjoe.identifier.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.lordjoe.identifier.FaceRecognizerType;
import com.lordjoe.identifier.OpenCVUtilities;
import com.lordjoe.identifier.R;
import com.lordjoe.identifier.RegisteredPersonSet;

import java.io.File;

import static com.lordjoe.identifier.OpenCVUtilities.getDataFile;
import static com.lordjoe.identifier.OpenCVUtilities.getFaceClassifier;
import static com.lordjoe.identifier.OpenCVUtilities.getRecognitionRoot;
import static com.lordjoe.identifier.OpenCVUtilities.loadFaceDetector;

public class MainActivity extends AppCompatActivity {
    private static MainActivity activeInstance;

    public static MainActivity getActiveInstance() {
        return activeInstance;
    }

    public static void setActiveInstance(MainActivity activeInstance) {
        MainActivity.activeInstance = activeInstance;
    }

    private RegisteredPersonSet registeredPeople;
    private Spinner peopleSelect;
    private  PersonSelectorAdapter adapter;
    private final String[] textArray = {"fee","fie","foe","fum"};

    public RegisteredPersonSet getRegisteredPeople() {
        return registeredPeople;
    }

    public PersonSelectorAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActiveInstance(this);
        OpenCVUtilities.setAppContext(this
        );
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnRecord).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onRecord();
             }
        });
        findViewById(R.id.btnBuildRecognizer).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onRecognizer();
            }
        });
        findViewById(R.id.btnTestFileSystem).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onTestFileSystem();
            }
        });
        findViewById(R.id.btnIdentify).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSelectPerson();
            }
        });
        peopleSelect = (Spinner) findViewById(R.id.spinner);
        registeredPeople = null;
        adapter = new PersonSelectorAdapter(this, R.layout.spinner_value_layout, textArray,registeredPeople);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // see http://stackoverflow.com/questions/5999262/populate-spinner-dynamically-in-android-from-edit-text
        peopleSelect.setAdapter(adapter);

    }

    public void onSelectPerson()
    {
        if(adapter.getPeople() == null)
            return;
        Intent intent = new Intent(MainActivity.this, RegisteredPersonViewActivity.class);
       startActivity(intent);
        Log.e("onSelectPerson","hit");

    }


    public void onRecord()
    {
        startActivity(new Intent(MainActivity.this, RecordActivity.class));

    }

    public void onRecognizer()
    {
        loadFaceDetector();
        Log.e("onRecognizer","hit");
        File file = new File(getRecognitionRoot(),"recognizedPeople");
        String path = file.getAbsolutePath();

        registeredPeople = new RegisteredPersonSet(file, FaceRecognizerType.LBPHFaceFaces );
        File storeDirectory = registeredPeople.getStoreDirectory();
        boolean exists = storeDirectory.exists();
        String eStr = exists? "exists":"not there";
        Log.e("onRecognizer",storeDirectory.getAbsolutePath() + " " + eStr);

        adapter.setPeople(registeredPeople);

    }
    public void onTestFileSystem()
    {
        File recognitionRoot = getRecognitionRoot();
        recognitionRoot.mkdirs();
        OpenCVUtilities.writeTextFile(recognitionRoot,"Test.txt","Mary Had A Little Lamb");
          Log.e("Recognotion Root",recognitionRoot.getAbsolutePath());


    }
    public void onIdentify()
    {
        if(adapter.getPeople() == null)
            return;
        startActivity(new Intent(MainActivity.this, RegisteredPersonViewActivity.class));
    }
}
