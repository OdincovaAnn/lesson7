package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class StartActivity extends Activity {
    ChannelDataBaseHelper dataBaseHelper;
    SQLiteDatabase database;
    final int MENU_EDIT = 1;
    final int MENU_DELETE = 2;
    final int DELETE_DIALOG = 1;
    int tableName;
    String name;
    String link;
    TextView textView;
    Button addButton;
    Button updateButton;
    FinishBroadcastReceiver finishBR;
    StartBroadcastReceiver startBR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        textView = (TextView) findViewById(R.id.noChannel);
        addButton = (Button) findViewById(R.id.add);
        updateButton = (Button) findViewById(R.id.upDate);
        finishBR = new FinishBroadcastReceiver();
        registerReceiver(finishBR, new IntentFilter(AllChannelsIntentService.finishKey));
        startBR = new StartBroadcastReceiver();
        registerReceiver(startBR, new IntentFilter(AllChannelsIntentService.startKey));
        showChannellist();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENU_EDIT, 0, R.string.menu_edit);
        menu.add(0, MENU_DELETE, 0, R.string.menu_delete);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long position = info.id;
        dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        database = dataBaseHelper.getReadableDatabase();
        Cursor cursor = database.query(ChannelDataBaseHelper.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToPosition((int) position);
        link = cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.URL));
        name = cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL));
        tableName = cursor.getInt(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL_TABLE_NAME));
        int id = cursor.getInt(cursor.getColumnIndex(ChannelDataBaseHelper._ID));
        cursor.close();
        database.close();
        dataBaseHelper.close();
        switch (item.getItemId()) {
            case MENU_EDIT: {
                Intent intent = new Intent(StartActivity.this, EditActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("link", link);
                intent.putExtra("number", id);
                intent.putExtra("flag", true);
                startActivity(intent);
                finish();
                break;
            }
            case MENU_DELETE: {
                showDialog(DELETE_DIALOG);

                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    public void onClickAdd(View view) {
        Intent intent = new Intent(StartActivity.this, EditActivity.class);
        intent.putExtra("flag", false);
        startActivity(intent);
        finish();
    }

    public void OnClickUpdateAll(View view) {
        Intent newIntent = new Intent(this, AllChannelsIntentService.class);
        startService(newIntent);
    }

    public void showChannellist() {
        dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        database = dataBaseHelper.getReadableDatabase();
        Cursor cursor = database.query(ChannelDataBaseHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.getCount() == 0) {

            textView.setText(R.string.no_channels);
        }

        final ArrayList<String> channels = new ArrayList<String>();
        final ArrayList<String> url = new ArrayList<String>();
        final ArrayList<Integer> tableName = new ArrayList<Integer>();
        while (cursor.moveToNext()) {
            channels.add(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL)));
            url.add(cursor.getString(cursor.getColumnIndex(ChannelDataBaseHelper.URL)));
            tableName.add(cursor.getInt(cursor.getColumnIndex(ChannelDataBaseHelper.CHANNEL_TABLE_NAME)));
        }
        cursor.close();
        database.close();
        dataBaseHelper.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, channels);
        ListView listView = (ListView) findViewById(R.id.listViewChannel);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(StartActivity.this, ChannelActivity.class);
                intent.putExtra("channel", channels.get(i));
                intent.putExtra("url", url.get(i));
                intent.putExtra("nameTable", "table" + tableName.get(i));
                startActivity(intent);
            }
        });
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DELETE_DIALOG) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);

            adb.setTitle(R.string.deleting);
            adb.setMessage(R.string.message_delete_dialog);
            adb.setPositiveButton(R.string.delete, myClickListener);
            adb.setNegativeButton(R.string.cancel, myClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    OnClickListener myClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
                    database = dataBaseHelper.getWritableDatabase();
                    database.execSQL("DELETE FROM " + ChannelDataBaseHelper.TABLE_NAME + " WHERE " + ChannelDataBaseHelper.CHANNEL + " = '" + name + "'");
                    database.execSQL("DROP TABLE IF EXISTS " + "table" + tableName);
                    database.close();
                    dataBaseHelper.close();
                    showChannellist();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    public class FinishBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean result = intent.getExtras().getBoolean("result");
            boolean nullChannels = intent.getExtras().getBoolean("null");
            addButton.setEnabled(true);
            updateButton.setEnabled(true);

            if (nullChannels) {
                textView.setText(R.string.no_channels);
                Toast myToast = Toast.makeText(getApplicationContext(), R.string.no_channels_for_update, Toast.LENGTH_SHORT);
                myToast.setGravity(Gravity.CENTER, 0, 0);
                myToast.show();
            } else {
                textView.setText("");
                if (result) {
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
    }

    public class StartBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast myToast = Toast.makeText(getApplicationContext(), R.string.download_update, Toast.LENGTH_SHORT);
            myToast.setGravity(Gravity.CENTER, 0, 0);
            myToast.show();
            textView.setText(R.string.download);
            updateButton.setEnabled(false);
            addButton.setEnabled(false);
        }
    }

}
