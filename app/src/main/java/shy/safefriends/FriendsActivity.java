package shy.safefriends;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by SunHee on 16/02/2016.
 */
public class FriendsActivity extends AppCompatActivity {

    private static final int number_friends = 10;

    private ListView friends = null;
    private Button call_them = null;
    private String[] recent_friends_call = null;
    private String[] recent_friends_sms = null;
    private String[] recent_friends = null;
    private String[] recent_friends_phones = null;

    /* HERE : ACTION BAR */
    private String[] drawerItemList;
    private ListView myDrawer;
    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friends = (ListView) findViewById(R.id.select_friends);
        ArrayList<Long> date_sms = new ArrayList<Long>();
        ArrayList<Long> date_call = new ArrayList<Long>();
        ArrayList<String> phone_sms = new ArrayList<String>();
        ArrayList<String> phone_call = new ArrayList<String>();

        recent_friends_sms = getSMSDetails(FriendsActivity.this, date_sms, phone_sms);
        recent_friends_call = getCallDetails(FriendsActivity.this, date_call, phone_call);
        int max = recent_friends_sms.length;
        if (recent_friends_call.length > max)
            max = recent_friends_call.length;
        recent_friends = new String[max];
        recent_friends_phones = new String[max];

        HashSet<String> in_names = new HashSet<String>();
        int i1 = 0, i2 = 0, i = 0;
        while (i < number_friends)
        {
            if (date_sms.get(i1) > date_call.get(i2)) {

                if (!in_names.contains(recent_friends_sms[i1])) {
                    recent_friends[i] = recent_friends_sms[i1];
                    recent_friends_phones[i] = phone_sms.get(i1);
                    ++i;
                    in_names.add(recent_friends_sms[i1]);
                }
                ++i1;
            }
            else {

                if (!in_names.contains(recent_friends_call[i2])) {
                    recent_friends[i] = recent_friends_call[i2];
                    recent_friends_phones[i] = phone_call.get(i2);
                    ++i;
                    in_names.add(recent_friends_call[i2]);
                }
                ++i2;
            }

        }


        friends.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, recent_friends));

        call_them = (Button)findViewById(R.id.call_them_button);
        call_them.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                int nb_friends = 0;
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<String> phones = new ArrayList<String>();
                int len = friends.getCount();
                SparseBooleanArray checked = friends.getCheckedItemPositions();
                for (int i = 0; i < len; ++i) {
                    if (checked.get(i)) {
                        ++nb_friends;
                        names.add(recent_friends[i]);
                        phones.add(recent_friends_phones[i]);
                    }
                }

                if (nb_friends > 0) {
                    Intent results = new Intent(getApplicationContext(), MainActivity.class);
                    results = results.putExtra("names", names.toArray(new String[names.size()]));
                    results = results.putExtra("phones", phones.toArray(new String[phones.size()]));
                    finish();
                    results.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(results);
                    //setResult(1, results);

                }
                else {
                    Toast.makeText(FriendsActivity.this, "You have to select at least one friend.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Start Menu
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerItemList = getResources().getStringArray(R.array.items_drawer);
        myDrawer = (ListView) findViewById(R.id.my_drawer);
        myDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, drawerItemList));
        myDrawer.setOnItemClickListener(new MyDrawerItemClickListener());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("SafeFriends / Friends");
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.opening, R.string.closing) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle("SafeFriends / Friends");
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu();

            }
        };

        drawerLayout.setDrawerListener(mDrawerToggle);

        myDrawer.bringToFront();
        drawerLayout.requestLayout();
    }

    private static String[] getCallDetails(Context context, ArrayList<Long> dates,  ArrayList<String> phones) {
        String permission = "android.permission.READ_CALL_LOG";
        int checkPerm = context.checkCallingOrSelfPermission(permission);
        if (checkPerm == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int date = cursor.getColumnIndex(CallLog.Calls.DATE);
            int iter = 0;
            ArrayList<String> results = new ArrayList<String>();
            HashSet<String> names = new HashSet<String>();
            while (cursor.moveToNext() && iter < number_friends) {
                StringBuffer stringBuffer = new StringBuffer();
                String phNumber = cursor.getString(number);
                String contactName = getContactName(context, phNumber);
                if (contactName == null)
                    continue;
                if (names.contains(contactName))
                    continue;
                names.add(contactName);
                String callDate = cursor.getString(date);
                Long timestamp = Long.parseLong(callDate);
                dates.add(timestamp);
                phones.add(phNumber);
                //stringBuffer.append(contactName+" "+ phNumber + "\n" + timestamp);
                stringBuffer.append(contactName);
                results.add(stringBuffer.toString());
                ++iter;
            }
            cursor.close();
            return results.toArray(new String[results.size()]);
        }
        return null;
    }

    private static String[] getSMSDetails(Context context, ArrayList<Long> dates,  ArrayList<String> phones) {
        String permission = "android.permission.READ_SMS";
        int checkPerm = context.checkCallingOrSelfPermission(permission);
        if (checkPerm == PackageManager.PERMISSION_GRANTED) {
            Uri uriSms = Uri.parse("content://sms");
            ArrayList<String> smsAll = new ArrayList<String>();
            Cursor cursor = context.getContentResolver().query(
                    uriSms, new String[]{"_id", "address", "date", "body", "type", "read"},
                    null, null, "date" + " COLLATE LOCALIZED ASC");
            if (cursor != null) {
                cursor.moveToLast();
                HashSet<String> names = new HashSet<String>();
                if (cursor.getCount() > 0) {
                    int iter = 0;
                    do {
                        String date = cursor.getString(cursor.getColumnIndex("date"));
                        Long timestamp = Long.parseLong(date);
                        String phone = cursor.getString(cursor.getColumnIndex("address"));
                        String contactName = getContactName(context, phone);
                        if (contactName == null)
                            continue;
                        if (names.contains(contactName))
                            continue;
                        names.add(contactName);
                        dates.add(timestamp);
                        phones.add(phone);
                        String msg = "";
                        msg += contactName;
                        //msg += " "+phone;
                        //msg += "\n" + timestamp;
                        smsAll.add(msg);
                        ++iter;
                    } while (cursor.moveToPrevious() && iter < number_friends);
                }
            }
            cursor.close();
            return smsAll.toArray(new String[smsAll.size()]);
        }
        return null;
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch(id){
            case R.id.menu_settings:
                Intent i = new Intent(FriendsActivity.this, Settings.class);
                finish();
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //pour garder les changements d'icone
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    public class MyDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int pos, long id) {
            drawerLayout.closeDrawer(myDrawer);
            Intent iii = null;
            switch (pos){
                case 0 :

                    //iii = new Intent(getApplicationContext(), MainActivity.class);
                    finish();
                    //startActivity(iii);
                    break;
                case 1 : // friends
                    //iii = new Intent(getApplicationContext(), FriendsActivity.class);
                    //finish();
                    //startActivity(iii);
                    break;
                case 2 : // message
                    iii = new Intent(getApplicationContext(), MessageActivity.class);
                    finish();
                    startActivity(iii);
                    break ;

            }

        }
    }
}
