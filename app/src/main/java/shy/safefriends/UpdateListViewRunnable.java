package shy.safefriends;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;
import java.util.Locale;


/**
 * Created by SunHee on 19/02/2016.
 */
public class UpdateListViewRunnable implements Runnable {

    private ListView lv;
    private List<String> names;
    private MainActivity parent;
    private ArrayAdapter<String> ad;
    private double[] lats;
    private double[] longs;

    public void setData(ListView lv, List<String> names, MainActivity parent, double[] lats, double[] longi) {
        this.lv = lv;
        this.names = names;
        this.parent = parent;
        this.lats = lats;
        this.longs = longi;
        ad = new ArrayAdapter<String>(parent, android.R.layout.simple_list_item_1, names);
    }
    public void run() {
        lv.setAdapter(ad);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent_, View view, int position, long id) {
                    double destinationLatitude = lats[position];
                    double destinationLongitude = longs[position];
                    String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", destinationLatitude, destinationLongitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    parent.startActivity(intent);
            }
        });
    }
}
