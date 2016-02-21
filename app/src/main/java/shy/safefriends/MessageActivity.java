package shy.safefriends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by SunHee on 16/02/2016.
 */
public class MessageActivity extends AppCompatActivity {

    private SharedPreferences preferences = null;
    private EditText edtex = null;
    private Button b_save = null;


    /* HERE : ACTION BAR */
    private String[] drawerItemList;
    private ListView myDrawer;
    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        edtex = (EditText) findViewById(R.id.message_activity_editText);
        preferences = getSharedPreferences("safefriends_message", Context.MODE_PRIVATE);
        edtex.setText(preferences.getString("sf_message", ""));

        b_save = (Button) findViewById(R.id.message_activity_valid_button);
        b_save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("sf_message", edtex.getText().toString());
                editor.commit();

                Toast.makeText(MessageActivity.this, "The message has been saved.", Toast.LENGTH_LONG).show();
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
        getSupportActionBar().setTitle("SafeFriends / Message");
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.opening, R.string.closing) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle("SafeFriends / Message");
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
                Intent i = new Intent(MessageActivity.this, Settings.class);
                finish();
                startActivity(i);
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
                    iii = new Intent(getApplicationContext(), FriendsActivity.class);
                    finish();
                    startActivity(iii);
                    break;
                case 2 : // message
                    //iii = new Intent(getApplicationContext(), MessageActivity.class);
                    //finish();
                    //startActivity(iii);
                    break ;

            }

        }
    }
}
