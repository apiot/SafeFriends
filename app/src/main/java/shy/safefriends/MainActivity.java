package shy.safefriends;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static long wait_duration = 5000L;

    private InternetManager im;
    private Thread th;

    private Button b_call_friends = null;
    private Button check_friends = null;
    private ListView friends_resp = null;

    private String[] names = null;
    private String[] phones = null;

    private ArrayAdapter<String> aa;

    private SharedPreferences preferences = null;

    private SmsManager smsManager = null;
    private String myPhone = null;

    private GPSTracker GPStracker = null;


    /* HERE : ACTION BAR */
    private String[] drawerItemList;
    private ListView myDrawer;
    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent response = getIntent();
        if (response.hasExtra("names") && response.hasExtra("phones")) {
            names = response.getStringArrayExtra("names");
            phones = response.getStringArrayExtra("phones");
        }

        friends_resp = (ListView) findViewById(R.id.friends_response);

        // retrieve phone number
        preferences = getSharedPreferences("safefriends_message", Context.MODE_PRIVATE);
        myPhone = preferences.getString("sf_phone", "");
        if (myPhone.equals(""))
            Toast.makeText(MainActivity.this, "You have to enter your phone number in the settings.", Toast.LENGTH_LONG).show();

        // Start Menu
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerItemList = getResources().getStringArray(R.array.items_drawer);
        myDrawer = (ListView) findViewById(R.id.my_drawer);
        myDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, drawerItemList));
        myDrawer.setOnItemClickListener(new MyDrawerItemClickListener());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.opening, R.string.closing) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle("SafeFriends");
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

        check_friends = (Button) findViewById(R.id.button_check_friends);
        check_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPStracker = new GPSTracker(MainActivity.this);

                if (GPStracker.canGetLocation()) {

                    final double latitude = GPStracker.getLatitude();
                    final double longitude = GPStracker.getLongitude();

                    im = new InternetManager(myPhone, latitude, longitude, phones, names, MainActivity.this, wait_duration, aa, friends_resp);
                    im.CheckDistressCall();
                    if (im.call_id_ != 0 && im.latitude_ != 0 && im.longitude_ != 0.0 && im.friendPhone_ != null) {
                        String friendName = FriendsActivity.getContactName(MainActivity.this, im.friendPhone_);
                        // open alert dialog to ask to the user what he wants to do
                        AlertDialog.Builder responseDialog = new AlertDialog.Builder(MainActivity.this);
                        responseDialog.setTitle("Your friend needs your help");
                        responseDialog.setMessage("Your friend " + friendName + " needs your help, do you want to share your GPS location ?");
                        responseDialog.setPositiveButton("Yes and display on Maps.", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                im.AddResponse(im.call_id_, latitude, longitude);
                                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", im.latitude_, im.longitude_);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                startActivity(intent);
                            }
                        });
                        responseDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        responseDialog.show();

                    }

                } else {
                    GPStracker.showSettingsAlert();
                }
            }
        });

        b_call_friends = (Button) findViewById(R.id.button_call_friend);
        b_call_friends.setBackgroundColor(Color.rgb(58,247,76));

        b_call_friends.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (b_call_friends.getText().toString().equals("Call my friends")){
                    if (names == null || phones == null) {
                        Toast.makeText(MainActivity.this, "You have to select friends", Toast.LENGTH_LONG).show();
                        return;
                    }
                    // first : send SMS to all friends

                    preferences = getSharedPreferences("safefriends_message", Context.MODE_PRIVATE);
                    String msg = preferences.getString("sf_message", "");
                    if (msg == "")
                        msg = getResources().getString(R.string.default_sms_message);

                    smsManager = SmsManager.getDefault();
                    for (int i = 0; i < phones.length; ++i) {
                        ArrayList<String> dividedMsg = smsManager.divideMessage(msg);
                        smsManager.sendMultipartTextMessage(phones[i], null, dividedMsg, null, null);
                    }
                    //Toast.makeText(MainActivity.this, "Sms have been sent.", Toast.LENGTH_LONG).show();


                    // second : retrieve GPS location
                    GPStracker = new GPSTracker(MainActivity.this);

                    if (GPStracker.canGetLocation()) {

                        double latitude = GPStracker.getLatitude();
                        double longitude = GPStracker.getLongitude();

                        b_call_friends.setText("Threat is over");
                        b_call_friends.setBackgroundColor(Color.rgb(250, 69, 76));

                        //Toast.makeText(getApplicationContext(), "Start communication with server", Toast.LENGTH_LONG).show();
                        // third : communicate with server to exchange GPS location
                        im = new InternetManager(myPhone, latitude, longitude, phones, names, MainActivity.this, wait_duration, aa, friends_resp);
                        im.SetInConnexion(true);
                        th = new Thread(im);
                        th.start();

                    } else {
                        // Can't get location.
                        // GPS or network is not enabled.
                        // Ask user to enable GPS/network in settings.
                        GPStracker.showSettingsAlert();
                    }

                }
                else if (b_call_friends.getText().toString().equals("Threat is over")) {
                    b_call_friends.setText("Call my friends");
                    b_call_friends.setBackgroundColor(Color.rgb(58, 247, 76));
                    GPStracker.stopUsingGPS();

                    im.SetInConnexion(false);
                    th.interrupt();
                    im.TerminateDistressCall(im.call_id_);

                    List<String> nullList = new ArrayList<String>();
                    ArrayAdapter<String> atmp = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, nullList);
                    friends_resp.setAdapter(atmp);

                    Toast.makeText(getApplicationContext(), "Call is over", Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), "Result:"+im.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (names != null && phones != null && names.length > 0 && names.length == phones.length) {
            String s = "";
            if (names.length > 1)
                s = "s";
            Toast.makeText(MainActivity.this, "You have " + names.length + " friend" + s + " selected.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (im != null)
            im.TerminateDistressCall(im.call_id_);
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
                Intent i = new Intent(MainActivity.this, Settings.class);
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
                    //startActivity(iii);
                    break;
                case 1 : // friends
                    iii = new Intent(getApplicationContext(), FriendsActivity.class);
                    //startActivityForResult(iii, ACTIVITY_FRIENDS);
                    startActivity(iii);
                    break;
                case 2 : // message
                    iii = new Intent(getApplicationContext(), MessageActivity.class);
                    startActivity(iii);
                    break ;

            }

        }
    }

}
