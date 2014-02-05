package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class ChannelActivity extends Activity {
    TextView textView;
    Button button;
    String title;
    String link;
    String tableName;
    ChannelDataBaseHelper dataBaseHelper;
    SQLiteDatabase database;
    MyBroadcastReceiver mbr;
    IntentFilter intentFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel);
        button = (Button) findViewById(R.id.button);
        title = getIntent().getExtras().getString("channel");
        link = getIntent().getExtras().getString("url");
        tableName = getIntent().getExtras().getString("nameTable");
        textView = (TextView) findViewById(R.id.title);
        textView.setText(title);

        mbr = new MyBroadcastReceiver();
        intentFilter = new IntentFilter(MyIntentService.key);
        registerReceiver(mbr, intentFilter);

        dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        database = dataBaseHelper.getReadableDatabase();
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        database.close();
        dataBaseHelper.close();
        if (i < 1) {
            if (hasInternetConnection()) {
                Toast myToast = Toast.makeText(getApplicationContext(), R.string.download_update, Toast.LENGTH_SHORT);
                myToast.setGravity(Gravity.CENTER, 0, 0);
                myToast.show();
                textView.setText(R.string.download);
                button.setEnabled(false);
                updateChannel();
            } else {
                Toast myToast = Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT);
                myToast.setGravity(Gravity.CENTER, 0, 0);
                myToast.show();
            }
        } else {
            getResult();
        }
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {
                    return true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                    return true;
                }
        }
        return false;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean result = intent.getExtras().getBoolean("result");
            textView.setText(title);
            button.setEnabled(true);
            if (result) {
                getResult();
                Toast myToast = Toast.makeText(getApplicationContext(), R.string.download_finish, Toast.LENGTH_SHORT);
                myToast.setGravity(Gravity.CENTER, 0, 0);
                myToast.show();
            } else {
                Toast myToast = Toast.makeText(getApplicationContext(), R.string.download_error, Toast.LENGTH_SHORT);
                myToast.setGravity(Gravity.CENTER, 0, 0);
                myToast.show();
            }
        }
    }

    private void getResult() {
        ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        final ArrayList<String> titles = new ArrayList<String>();
        final ArrayList<String> summaries = new ArrayList<String>();

        while (cursor.moveToNext()) {
            titles.add(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.TITLE)));
            summaries.add(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.SUMMARY)));
        }
        cursor.close();
        database.close();
        dataBaseHelper.close();

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ChannelActivity.this, ArticleActivity.class);
                intent.putExtra("title", titles.get(i));
                intent.putExtra("description", summaries.get(i));
                startActivity(intent);
            }
        });

    }

    public void onClickUpdateChannel(View view) {
        Toast myToast = Toast.makeText(getApplicationContext(), R.string.download_update, Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.CENTER, 0, 0);
        myToast.show();
        textView.setText(R.string.download);
        button.setEnabled(false);
        updateChannel();
    }

    public void updateChannel() {
        Intent newIntent = new Intent(this, MyIntentService.class);
        newIntent.putExtra("link", link);
        newIntent.putExtra("table", tableName);
        startService(newIntent);
    }
}
