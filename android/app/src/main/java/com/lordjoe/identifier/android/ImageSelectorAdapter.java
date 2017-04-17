package com.lordjoe.identifier.android;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lordjoe.identifier.OpenCVUtilities;
import com.lordjoe.identifier.R;
import com.lordjoe.identifier.RegisteredPerson;
import com.lordjoe.identifier.RegisteredPersonSet;

import java.io.File;
import java.util.List;


/**
 * Created by Steve on 4/11/2017.
 */

public class ImageSelectorAdapter extends ArrayAdapter<String> {
    private final Context ctx;
    private RegisteredPerson  person;

    public ImageSelectorAdapter(Context context ,String[]  objects) {
          super(context, R.layout.spinner_value_layout, R.id.spinnerTextView, objects);
        this.ctx = context;
     }


    public void setPerson(RegisteredPerson  people) {
        this.person = people;
    }

    @Override
    public int getCount() {
        if(person == null)
            return 0;
        return person.getSampleImages().size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spinner_value_layout, null);

        }

        List<File> sampleImages = person.getSampleImages();
        File f   = sampleImages.get(position);
        TextView textView = (TextView) convertView.findViewById(R.id.spinnerTextView);
        textView.setText(f.getName());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.spinnerImages);
        Bitmap image = AndroidUtilities.fromFile(f,200,200);
        imageView.setImageBitmap(image);

        return convertView;

    }

    private Bitmap bitmapFromPerson(RegisteredPerson person) {
        throw new UnsupportedOperationException("not done");
    }
}
