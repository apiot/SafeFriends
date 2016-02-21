package shy.safefriends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SunHee on 19/02/2016.
 */
public class InternetManager implements Runnable {

    private long wait_duration;
    private MainActivity parent;

    private List<String> nms;
    private ArrayAdapter<String> aa;
    private ListView lv;

    private String myPhone;
    private double lat;
    private double longi;
    private String[] phones;
    private String[] names;
    private InternetRequest ir;
    private String result;
    private boolean inConnexion;

    // where we store results from the server
    public int call_id_;
    public double latitude_;
    public double longitude_;
    public String friendPhone_;
    public String[] users_phone_;
    public double[] latitudes_;
    public double[] longitudes_;

    public InternetManager(String myPhone, double latitude, double longitude, String[] phones,
                           String[] names, MainActivity parent, long wd, ArrayAdapter<String> aa,
                           ListView lv)
    {
        nms = new ArrayList<String>();
        this.aa = aa;
        this.lv = lv;
        this.wait_duration = wd;
        this.names = names;
        this.parent = parent;
        this.myPhone = myPhone;
        this.lat = latitude;
        this.longi = longitude;
        this.phones = phones;
    }

    public String getResult()
    {
        return result;
    }

    public void SetInConnexion(boolean ic)
    {
        inConnexion = ic;
    }

    public boolean AddDistressCall(double latitude, double longitude)
    {
        ir = new InternetRequest();
        ir.setUrlAddDistressCall(myPhone, latitude, longitude);
        return fetchServer(0);
    }

    public boolean TerminateDistressCall(int cid)
    {
        ir = new InternetRequest();
        ir.setUrlTerminateDistressCall(cid);
        return fetchServer(1);
    }

    public boolean AddCallee(int cid, String uPhone)
    {
        ir = new InternetRequest();
        ir.setUrlAddCallee(cid, uPhone);
        return fetchServer(2);
    }

    public boolean AddResponse(int cid, double latitude, double longitude)
    {
        ir = new InternetRequest();
        ir.setUrlAddResponse(cid, myPhone, latitude, longitude);
        System.out.println(result);
        return fetchServer(3);
    }

    public boolean CheckDistressCall()
    {
        ir = new InternetRequest();
        ir.setUrlCheckDistressCall(myPhone);
        return fetchServer(4);
    }

    public boolean CheckResponse(int cid)
    {
        ir = new InternetRequest();
        ir.setUrlCheckResponse(cid);
        return fetchServer(5);
    }

    public void run()
    {
        AddDistressCall(lat, longi);
        for (int i = 0; i < phones.length; ++i)
            AddCallee(call_id_, phones[i]);

        while (inConnexion) {
            try {
                Thread.sleep(wait_duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CheckResponse(call_id_);
        }
    }

    private boolean fetchServer(int code)
    {
        while (!sendPacket());

        if (result.equals("status:failed"))
            return false;
        else if (result.equals("false"))
            return false;
        else if (result.equals("notallowed"))
            return false;
        else {
            switch (code) {
                case 0: // add distress call
                    String[] t = result.split(":");
                    call_id_ = Integer.parseInt(t[t.length-1]);
                    break;
                case 1: // terminate distress call
                    // nothing to do
                    break;
                case 2: // add callee
                    // nothing to do
                    break;
                case 3: // add response
                    // nothing to do
                    break;
                case 4: // check distress call
                    String[] tab = result.split("-");
                    for (int i = 0; i < 4; ++i) {
                        String[] tab2 = tab[i].split(":");
                        if (i == 0)
                            call_id_ = Integer.parseInt(tab2[1]);
                        else if (i == 1)
                            latitude_ = Double.parseDouble(tab2[1]);
                        else if (i == 2)
                            longitude_ = Double.parseDouble(tab2[1]);
                        else if (i == 3)
                            friendPhone_ = tab2[1];
                    }
                    break;
                case 5: // check response
                    //System.out.println(result);
                    if (result.equals("false"))
                        break;
                    String[] tt = result.split(":");
                    if (tt.length>1) {
                        users_phone_ = new String[tt.length - 1];
                        latitudes_ = new double[tt.length - 1];
                        longitudes_ = new double[tt.length - 1];
                        List<String> nns = new ArrayList<String>();
                        System.out.println(result);
                        for (int i = 1; i < tt.length; ++i) {
                            String[] tt2 = tt[i].split("-");
                            if (tt2.length == 3) {
                                users_phone_[i - 1] = tt2[0];
                                latitudes_[i - 1] = Double.parseDouble(tt2[1]);
                                longitudes_[i - 1] = Double.parseDouble(tt2[2]);
                                String name = FriendsActivity.getContactName(parent, users_phone_[i - 1]);

                                if (name != null && !nns.contains(name)) {
                                    nns.add(name);
                                }
                                UpdateListViewRunnable ulv = new UpdateListViewRunnable();
                                ulv.setData(lv, nns, parent, latitudes_, longitudes_);
                                parent.runOnUiThread(ulv);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
    }



    private boolean sendPacket()
    {
        Thread th = new Thread(ir);
        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
            return false;
            //e.printStackTrace();
        }
        result = ir.getResult();
        return true;
    }
}
