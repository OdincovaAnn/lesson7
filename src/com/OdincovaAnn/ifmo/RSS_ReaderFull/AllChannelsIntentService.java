package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

/**
 * Created by MaryAnn on 04.02.14.
 */
public class AllChannelsIntentService extends IntentService {
    public static final String startKey = "startKey.com.example.RSS_HW5.AllChannelsIntentService";
    public static final String finishKey = "finishKey.com.example.RSS_HW5.AllChannelsIntentService";
    ChannelDataBaseHelper dataBaseHelper;
    SQLiteDatabase database;
    ArrayList<String> summaries;
    ArrayList<String> titles;

    public AllChannelsIntentService() {
        super("AllChannelsIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Intent newIntent = new Intent();
        newIntent.setAction(startKey);
        sendBroadcast(newIntent);

        dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        database = dataBaseHelper.getReadableDatabase();
        Cursor cursor = database.query(ChannelDataBaseHelper.TABLE_NAME, null, null, null, null, null, null);

        final ArrayList<String> channels = new ArrayList<String>();
        final ArrayList<String> url = new ArrayList<String>();
        final ArrayList<Integer> tableName = new ArrayList<Integer>();
        while (cursor.moveToNext()) {
            channels.add(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL)));
            url.add(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.URL)));
            tableName.add(cursor.getInt(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL_TABLE_NAME)));
        }
        cursor.close();
        boolean result = false;
        boolean nullChannels;
        if (channels.size() != 0) {
            nullChannels = false;
            for (int i = 0; i < channels.size(); i++) {
                String link = url.get(i);
                String table = "table" + tableName.get(i);
                result = false;
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
                if (result) {

                    database.execSQL("DROP TABLE IF EXISTS " + table);
                    database.execSQL("CREATE TABLE " + table +
                            " (" + ChannelDataBaseHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ChannelDataBaseHelper.TITLE
                            + " TEXT, " + ChannelDataBaseHelper.SUMMARY + " TEXT);");

                    for (int j = 0; j < summaries.size(); j++) {
                        ContentValues values = new ContentValues();
                        values.put(ChannelDataBaseHelper.TITLE, titles.get(j));
                        values.put(ChannelDataBaseHelper.SUMMARY, summaries.get(j));
                        database.insert(table, null, values);
                    }
                }
            }
        } else {
            nullChannels = true;
        }
        database.close();
        dataBaseHelper.close();
        Intent response = new Intent();
        response.setAction(finishKey);
        response.putExtra("result", result);
        response.putExtra("null", nullChannels);
        sendBroadcast(response);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 600000, 600000, pendingIntent);
    }
}

