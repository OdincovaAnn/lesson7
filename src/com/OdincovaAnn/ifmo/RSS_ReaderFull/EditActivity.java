package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends Activity {
    EditText nameEdittext;
    EditText linkEdittext;
    Button addEditButton;
    boolean edit;
    String oldName;
    String oldLink;
    int id;
    ChannelDataBaseHelper dataBaseHelper;
    SQLiteDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        nameEdittext = (EditText) findViewById(R.id.editTextName);
        linkEdittext = (EditText) findViewById(R.id.editTextLink);
        addEditButton = (Button) findViewById(R.id.buttonAddEdit);
        edit = getIntent().getExtras().getBoolean("flag");
        if (edit) {
            addEditButton.setText(R.string.edit);
            oldName = getIntent().getExtras().getString("name");
            oldLink = getIntent().getExtras().getString("link");
            id = getIntent().getExtras().getInt("number");
            nameEdittext.setText(oldName);
            linkEdittext.setText(oldLink);
        } else {
            addEditButton.setText(R.string.add_new_chahhel);
        }
    }

    public void onClickAddEdit(View view) {
        String name = nameEdittext.getText().toString();
        String link = linkEdittext.getText().toString();
        while (!"".equals(name) && name.charAt(0) == ' ') {
            name = name.substring(1);
        }
        while (!"".equals(link) && link.charAt(0) == ' ') {
            link = link.substring(1);
        }

        if ("".equals(name) || "".equals(link)) {
            Toast myToast = Toast.makeText(getApplicationContext(), R.string.error_null, Toast.LENGTH_SHORT);
            myToast.setGravity(Gravity.CENTER, 0, 0);
            myToast.show();
        } else {
            dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
            database = dataBaseHelper.getWritableDatabase();
            if (!repetition(name, link)) {

                ContentValues values = new ContentValues();
                values.put(ChannelDataBaseHelper.CHANNEL, name);
                values.put(ChannelDataBaseHelper.URL, link);
                if (edit) {
                    database.update(ChannelDataBaseHelper.TABLE_NAME, values, ChannelDataBaseHelper._ID + "=" + id, null);
                } else {
                    int max = 0;
                    Cursor cursor = database.query(ChannelDataBaseHelper.TABLE_NAME, null, null, null, null, null, null);
                    while (cursor.moveToNext()) {
                        if (cursor.getInt(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL_TABLE_NAME)) > max) {
                            max = cursor.getInt(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL_TABLE_NAME));
                        }
                    }
                    cursor.close();
                    max++;
                    values.put(ChannelDataBaseHelper.CHANNEL_TABLE_NAME, max);
                    database.insert(ChannelDataBaseHelper.TABLE_NAME, null, values);
                    String newTable = "table" + max;
                    database.execSQL("CREATE TABLE " + newTable +
                            " (" + ChannelDataBaseHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ChannelDataBaseHelper.TITLE
                            + " TEXT, " + ChannelDataBaseHelper.SUMMARY + " TEXT);");
                }
                database.close();
                dataBaseHelper.close();
                Intent intent = new Intent(EditActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            } else {
                database.close();
                dataBaseHelper.close();
            }
        }
    }

    public void onClickCancel(View view) {
        Intent intent = new Intent(EditActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean repetition(String name, String link) {
        Cursor cursor = database.query(ChannelDataBaseHelper.TABLE_NAME, null, null, null, null, null, null);
        boolean repeat = false;

        while (cursor.moveToNext()) {
            if (!edit || !(id == cursor.getInt(cursor.getColumnIndex(ChannelDataBaseHelper._ID)))) {
                if (name.equals(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL)))) {
                    repeat = true;
                    Toast myToast = Toast.makeText(getApplicationContext(), R.string.repeat_name, Toast.LENGTH_SHORT);
                    myToast.setGravity(Gravity.CENTER, 0, 0);
                    myToast.show();
                    break;

                }
                if (link.equals(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.URL)))) {
                    repeat = true;
                    Toast myToast = Toast.makeText(getApplicationContext(), R.string.repeat_link, Toast.LENGTH_SHORT);
                    myToast.setGravity(Gravity.CENTER, 0, 0);
                    myToast.show();
                    break;
                }
            }
        }
        return repeat;
    }
}
