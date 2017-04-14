package com.lordjoe.identifier.android;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.UnsupportedSchemeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lordjoe.identifier.R;
import com.lordjoe.identifier.RegisteredPerson;
import com.lordjoe.identifier.RegisteredPersonSet;



/**
 * Created by Steve on 4/11/2017.
 */

public class PersonSelectorAdapter extends ArrayAdapter<String> {
    private final Context ctx;
    private RegisteredPersonSet people;

    public PersonSelectorAdapter(Context context, int resource, String[] objects,
                                 RegisteredPersonSet people) {
        super(context, R.layout.spinner_value_layout, R.id.spinnerTextView, objects);
        this.ctx = context;
        this.people = people;
    }

    public RegisteredPersonSet getPeople() {
        return people;
    }

    public void setPeople(RegisteredPersonSet people) {
        this.people = people;
    }

    @Override
    public int getCount() {
        if(people == null)
            return 0;
        return people.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spinner_value_layout, null);

        }

        RegisteredPerson person = people.getPerson(position);
        TextView textView = (TextView) convertView.findViewById(R.id.spinnerTextView);
        textView.setText(person.getName());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.spinnerImages);
        Bitmap image = bitmapFromPerson(person);
        imageView.setImageBitmap(image);

        return convertView;

    }

    private Bitmap bitmapFromPerson(RegisteredPerson person) {
        throw new UnsupportedOperationException("not done");
    }
}
