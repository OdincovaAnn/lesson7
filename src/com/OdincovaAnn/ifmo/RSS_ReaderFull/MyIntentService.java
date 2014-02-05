package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MyIntentService extends IntentService {
    ArrayList<String> summaries;
    ArrayList<String> titles;
    public static final String key = "com.example.RSS_HW5.MyIntentService";

    public MyIntentService() {
        super("MyIntentService");
    }

    String link = null;
    String tableName;

    @Override
    public void onHandleIntent(Intent intent) {
        link = intent.getStringExtra("link");
        tableName = intent.getStringExtra("table");
        boolean result = false;
        try {
            summaries = new ArrayList<String>();
            titles = new ArrayList<String>();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(link);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String inform = EntityUtils.toString(httpResponse.getEntity());
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(new ByteArrayInputStream(inform.getBytes()), new RSSHandler(summaries, titles));
            result = true;
        } catch (SAXException e) {
            result = false;
        } catch (IOException e) {
            result = false;
        } catch (ParserConfigurationException e) {
            result = false;
        } catch (IllegalStateException e) {
            result = false;
        }
        Intent response = new Intent();
        response.setAction(key);
        response.putExtra("result", result);
        if (result) {
            ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("DROP TABLE IF EXISTS " + tableName);
            database.execSQL("CREATE TABLE " + tableName +
                    " (" + ChannelDataBaseHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ChannelDataBaseHelper.TITLE
                    + " TEXT, " + ChannelDataBaseHelper.SUMMARY + " TEXT);");

            for (int i = 0; i < summaries.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(ChannelDataBaseHelper.TITLE, titles.get(i));
                values.put(ChannelDataBaseHelper.SUMMARY, summaries.get(i));
                database.insert(tableName, null, values);
            }
            database.close();
            dataBaseHelper.close();
        }
        sendBroadcast(response);
    }
}

