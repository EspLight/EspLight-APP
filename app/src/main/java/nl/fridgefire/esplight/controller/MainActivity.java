package nl.fridgefire.esplight.controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ColorPicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity

        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;

    //Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;
    private Boolean firstBoot = true;

    ArrayList<String> location_section;
    ArrayList<String> ip_section;
    ArrayList<String> pin_section;
    ArrayList<String> effect_section;
    ArrayList<String> rgbRed_section;
    ArrayList<String> rgbGreen_section;
    ArrayList<String> rgbBlue_section;
    ArrayList<String> brightness_section;
    ArrayList<String> speed_section;
    ArrayList<String> esp_finds_name = new ArrayList();
    ArrayList<String> esp_finds_ip = new ArrayList();
    private TextView resultText;
    public Boolean colorTimer = false;
    public AsyncTask UdpSendAsyncTask;
    public Integer position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Restore preferences

        location_section = getStringArrayPref(this, "locations", "[\"ESP 1\",\"ESP 2\",\"ESP 3\",\"\"]");
        ip_section = getStringArrayPref(this, "ip", "[\"192.168.12.1\",\"192.168.12.2\",\"192.168.12.3\",\"\"]");
        pin_section = getStringArrayPref(this, "pin", "[\"\",\"\",\"\",\"\"]");
        effect_section = getStringArrayPref(this, "effect", "[\"0\",\"0\",\"0\",\"0\"]");
        rgbRed_section = getStringArrayPref(this, "rgbRed", "[\"0\",\"0\",\"0\",\"0\"]");
        rgbGreen_section = getStringArrayPref(this, "rgbGreen", "[\"0\",\"0\",\"0\",\"0\"]");
        rgbBlue_section = getStringArrayPref(this, "rgbBlue", "[\"0\",\"0\",\"0\",\"0\"]");
        brightness_section = getStringArrayPref(this, "brightness", "[\"99\",\"99\",\"99\",\"99\"]");
        speed_section = getStringArrayPref(this, "speed", "[\"127\",\"127\",\"127\",\"99\"]");
        position = getIntPref(this, "position", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        //disable slider
        final SeekBar speedBar =(SeekBar) findViewById(R.id.speedBar);
        final TextView textspeed = (TextView) findViewById(R.id.textSpeed);
        speedBar.setVisibility(View.GONE);
        textspeed.setVisibility(View.GONE);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        addListenerOnButtons();

        SetPosition(position);


    }


    @Override
    public void onStop() {
        // store preference
        setStringArrayPref(this, "locations", location_section);
        setStringArrayPref(this, "ip", ip_section);
        setStringArrayPref(this, "pin", pin_section);
        setStringArrayPref(this, "effect", effect_section);
        setStringArrayPref(this, "rgbRed", rgbRed_section);
        setStringArrayPref(this, "rgbGreen", rgbGreen_section);
        setStringArrayPref(this, "rgbBlue", rgbBlue_section);
        setStringArrayPref(this, "brightness", brightness_section);
        setStringArrayPref(this, "speed", speed_section);
        setIntPref(this, "position", position);
        super.onStop();
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        if (firstBoot == false) {
            Integer location_sectionLength = location_section.size();
            for (int l = 0; l <= location_sectionLength; l++) {
                if (number == l + 1) {
                    mTitle = location_section.get(l);
                    Log.v("title", mTitle.toString());
                }
            }
        }
        else{
            firstBoot = false;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containingcl a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public static void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));

        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
            Log.v("save", a.toString());
        } else {
            editor.putString(key, null);
            Log.v("save", "null");
        }
        editor.commit();
    }

    public static ArrayList<String> getStringArrayPref(Context context, String key, String defaultString) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, defaultString);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    Log.v("url: ", url);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public static void setIntPref(Context context, String key, Integer value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        Log.v("set int", value.toString());
        editor.commit();
    }

    public static Integer getIntPref(Context context, String key, Integer defaultInt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Integer value = prefs.getInt(key, defaultInt);
        Log.v("get int", value.toString());
        return value;
    }

    public ArrayList<String>  getLocation_section() {
        return location_section;

    }

    //https://stackoverflow.com/questions/11536802/add-radio-button-dynamically-android
   void showEditDialog(final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.changeitem_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

       final EditText editName = (EditText) promptView.findViewById(R.id.editname);
       final EditText editIp = (EditText) promptView.findViewById(R.id.editip);
       final EditText editpin = (EditText) promptView.findViewById(R.id.editPin);

        RadioGroup ipListGroup = (RadioGroup) promptView.findViewById(R.id.ipListGroup);

        RadioButton[] radiobutton = new RadioButton[esp_finds_name.size()+1];

       //broadcast button
       radiobutton[0] = new RadioButton(this);

       radiobutton[0].setText("Broadcast");
       radiobutton[0].setId(100);
       ipListGroup.addView(radiobutton[0]);

       //espfind
        for (int i = 0; i < esp_finds_name.size(); i++) {
            Log.v("this is: ", this.toString());

            radiobutton[i+1] = new RadioButton(this);
            radiobutton[i+1].setText(esp_finds_name.get(i+1));
            radiobutton[i+1].setId(i + 101);
            ipListGroup.addView(radiobutton[i+1]);
        }

      ipListGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

           @Override
           public void onCheckedChanged(RadioGroup group, int checkedId) {
               int checkedButton = checkedId - 100;
               System.out.println(checkedButton);
               System.out.println(R.id.radioButton3);
               if (checkedButton == 0){
                   try {
                       Context mContext = getApplicationContext();
                       WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                       DhcpInfo dhcp = wifi.getDhcpInfo();
                       int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
                       byte[] quads = new byte[4];
                       for (int k = 0; k < 4; k++)
                           quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
                       editIp.setText(InetAddress.getByAddress(quads).toString().substring(1));
                   }
                   catch (IOException e) {
                       Log.e("error", "Could not get broadcast Ip", e);
                       editIp.setText("broadcast error");
                   }
               }
               else if (checkedButton + 100 == R.id.radioButton3){
                   editIp.setText("");
               }
               else {
                   editIp.setText(esp_finds_ip.get(checkedButton).substring(1));
               }
           }
       });

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        location_section.set(position, editName.getText().toString().toUpperCase());
                        location_section.add("");
                        ip_section.add("");
                        pin_section.add("");
                        effect_section.add("0");
                        rgbRed_section.add("0");
                        rgbGreen_section.add("0");
                        rgbBlue_section.add("0");
                        brightness_section.add("99");
                        speed_section.add("127");
                        mNavigationDrawerFragment.setLocation_section(location_section);

                        ip_section.set(position, editIp.getText().toString());
                        pin_section.set(position, editpin.getText().toString());

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (location_section.get(position).equals("")) {

                            location_section.add("");
                            ip_section.add("");
                            pin_section.add("");
                            effect_section.add("0");
                            rgbRed_section.add("0");
                            rgbGreen_section.add("0");
                            rgbBlue_section.add("0");
                            brightness_section.add("99");
                            speed_section.add("127");
                        }

                        location_section.remove(position);
                        ip_section.remove(position);
                        pin_section.remove(position);
                        effect_section.remove(position);
                        rgbRed_section.remove(position);
                        rgbGreen_section.remove(position);
                        rgbBlue_section.remove(position);
                        brightness_section.remove(position);
                        speed_section.remove(position);

                        mNavigationDrawerFragment.setLocation_section(location_section);

                    }
                });

        // edit text
        editName.setText(location_section.get(position));
        editpin.setText(pin_section.get(position));


        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    public void sendData() {

        String url = ip_section.get(position);
        String pincode = pin_section.get(position);
        String effect = effect_section.get(position);
        String brightness = brightness_section.get(position);
        String var0;
        String var1;
        String var2;

        System.out.println(position);
        System.out.println(brightness);
        System.out.println(effect);


        //if effect == RGB
        if(effect.equals("0")){
            System.out.println("rgb");
            var0 = rgbRed_section.get(position);
            var1 = rgbGreen_section.get(position);
            var2 = rgbBlue_section.get(position);
        }
        //if effect == Fade
        else if(effect.equals("1")){
            Integer speed = 255-Integer.parseInt(speed_section.get(position));
            var0 = speed.toString();
            var1 = "0";
            var2 = "0";
        }
        //if effect == Rainbow
        else if(effect.equals("2")){
            Integer speed = 255-Integer.parseInt(speed_section.get(position));
            var0 = speed.toString();
            var1 = "0";
            var2 = "0";
        }
        else{
            System.out.println("HELP!! ERROR!");
            var0 = "0";
            var1 = "0";
            var2 = "0";
        }



        new UdpSendAsyncTask().execute(url, pincode, effect, brightness, var0, var1, var2);
    }


    public void addListenerOnButtons() {

        final Button buttonRGB = (Button) findViewById(R.id.buttonRGB);
        final Button buttonFADE = (Button) findViewById(R.id.buttonFADE);
        final Button buttonRAINBOW = (Button) findViewById(R.id.buttonRAINBOW);
        final Button buttonWhite = (Button) findViewById(R.id.buttonWhite);
        final Button buttonBlack = (Button) findViewById(R.id.buttonBlack);
        final SeekBar speedBar =(SeekBar) findViewById(R.id.speedBar);
        final TextView textspeed = (TextView) findViewById(R.id.textSpeed);
        final ColorPicker colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
        final SeekBar brightnessBar = (SeekBar) findViewById(R.id.brightnessBar);

        buttonRGB.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.v("button", "RGB");

                buttonRGB.getBackground().setAlpha(100);
                buttonFADE.getBackground().setAlpha(255);
                buttonRAINBOW.getBackground().setAlpha(255);

                speedBar.setVisibility(View.GONE);
                textspeed.setVisibility(View.GONE);
                buttonWhite.setVisibility(View.VISIBLE);
                buttonBlack.setVisibility(View.VISIBLE);


                effect_section.set(position, "0");
                sendData();

                getWindow().getDecorView().findViewById(android.R.id.content).invalidate();

            }

        });

        buttonFADE.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.v("button", "FADE");

                buttonRGB.getBackground().setAlpha(255);
                buttonFADE.getBackground().setAlpha(100);
                buttonRAINBOW.getBackground().setAlpha(255);


                speedBar.setVisibility(View.VISIBLE);
                textspeed.setVisibility(View.VISIBLE);
                buttonWhite.setVisibility(View.GONE);
                buttonBlack.setVisibility(View.GONE);

                //colorPicker.setVisibility(View.GONE);
                effect_section.set(position, "1");
                sendData();

                getWindow().getDecorView().findViewById(android.R.id.content).invalidate();

            }

        });

        buttonRAINBOW.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.v("button", "RAINBOW");
                buttonRGB.getBackground().setAlpha(255);
                buttonFADE.getBackground().setAlpha(255);
                buttonRAINBOW.getBackground().setAlpha(100);
                speedBar.setVisibility(View.VISIBLE);
                textspeed.setVisibility(View.VISIBLE);
                buttonWhite.setVisibility(View.GONE);
                buttonBlack.setVisibility(View.GONE);
                //colorPicker.setVisibility(View.GONE);

                effect_section.set(position, "2");
                sendData();

                getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
            }

        });

        buttonWhite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.v("button", "White");
                buttonRGB.getBackground().setAlpha(100);
                buttonFADE.getBackground().setAlpha(255);
                buttonRAINBOW.getBackground().setAlpha(255);

                rgbRed_section.set(position, "255");
                rgbGreen_section.set(position, "255");
                rgbBlue_section.set(position, "255");
                ;
                sendData();
            }

        });

        buttonBlack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.v("button", "Black");
                buttonRGB.getBackground().setAlpha(100);
                buttonFADE.getBackground().setAlpha(255);
                buttonRAINBOW.getBackground().setAlpha(255);

                rgbRed_section.set(position, "0");
                rgbGreen_section.set(position, "0");
                rgbBlue_section.set(position, "0");
                sendData();
            }

        });


        colorPicker.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.v("colour", "click");
                //buttonRGB.performClick();

                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:

                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        int cx = x - colorPicker.getWidth() / 2;
                        int cy = y - colorPicker.getHeight() / 2;
                        double d = Math.sqrt(cx * cx + cy * cy);


                        if (d <= colorPicker.colorWheelRadius) {

                            colorPicker.colorHSV[0] = (float) (Math.toDegrees(Math.atan2(cy, cx)) + 180f);
                            colorPicker.colorHSV[1] = Math.max(0f, Math.min(1f, (float) (d / colorPicker.colorWheelRadius)));

                            colorPicker.invalidate();

                        } else if (x >= colorPicker.getWidth() / 2 && d >= colorPicker.innerWheelRadius) {

                            colorPicker.colorHSV[2] = (float) Math.max(0, Math.min(1, Math.atan2(cy, cx) / Math.PI + 0.5f));

                            colorPicker.invalidate();
                        }

                        Integer color = colorPicker.getColor();
                        rgbRed_section.set(position, Integer.toString(Color.red(color)));
                        rgbGreen_section.set(position, Integer.toString(Color.green(color)));
                        rgbBlue_section.set(position, Integer.toString(Color.blue(color)));
                        sendData();

                }

                return true;
            }

        });

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println(String.valueOf(progress + 2));
                brightness_section.set(position, String.valueOf(progress + 2));
                sendData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println(String.valueOf(progress));
                speed_section.set(position, String.valueOf(progress));
                sendData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

    }

    public void udpFind(int position){

        String data = "EspFind";
        esp_finds_name.clear();
        esp_finds_ip.clear();

        try{
            Context mContext=getApplicationContext();
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            // handle null somehow

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            Log.v("IP broadcast: ", InetAddress.getByAddress(quads).toString());


            DatagramSocket socket = new DatagramSocket(1337);
            socket.setBroadcast(true);

            DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByAddress(quads), 1337);
            //DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByName("10.42.255.255"), 1337);

            socket.send(packet);
            socket.close();

            byte[] buf = new byte[1024];

            for (int i = 0; i < 20; i++) {
                socket = new DatagramSocket(1337);
                socket.setBroadcast(true);
                socket.setSoTimeout(100);
                packet = new DatagramPacket(buf, buf.length);
                try{
                    socket.receive(packet);
                    String s = new String(packet.getData());
                    System.out.println(s);
                    System.out.println(packet.getLength());
                    System.out.println(packet.getAddress());
                    esp_finds_name.add(s.substring(0, packet.getLength()));
                    esp_finds_ip.add(packet.getAddress().toString());
                    socket.close();
                } catch (IOException e) {
                    socket.close();
                }
            }
            showEditDialog(position);


        } catch (IOException e) {
            Log.e("error", "Could not send discovery request", e);
        }

    }

    private class UdpSendAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            String pincode = params[1];
            String effect = params[2];
            String brightness = params[3];
            String var0 = params[4];
            String var1 = params[5];
            String var2 = params[6];
            Log.v("HTTP ", url);

            String data = "?"+"pin="+pincode+"&effect="+effect+"&brightness="+brightness+"&var0="+var0+"&var1="+var1+"&var2="+var2;
            System.out.println(data);

            try {
                DatagramSocket socket = new DatagramSocket(1337);
                socket.setBroadcast(true);

                InetAddress inetAddress = InetAddress.getByName(url);

                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), inetAddress, 1337);
                socket.send(packet);
                socket.close();
            } catch (IOException e) {
                Log.e("error", "Could not send discovery request", e);
            }

            return null;
        }
        protected Void onPostExecute(Double result){
            return null;
        }

        protected Void onProgressUpdate(Integer... progress){
            return null;
        }
    }

    public void SetPosition(Integer position_locale)
    {
        position = position_locale;

        final SeekBar brightnessBar = (SeekBar) findViewById(R.id.brightnessBar);
        final SeekBar speedBar = (SeekBar) findViewById(R.id.speedBar);
        final FrameLayout effectButtons = (FrameLayout) findViewById(R.id.effectButtons);
        final Button effectButton = (Button) effectButtons.getChildAt(Integer.parseInt(effect_section.get(position)));

        Integer brightness = Integer.parseInt(brightness_section.get(position));
        Integer speed = Integer.parseInt(speed_section.get(position));
        System.out.println(position_locale);
        brightnessBar.setProgress(0);
        brightnessBar.setProgress(brightness - 2);
        speedBar.setProgress(0);
        speedBar.setProgress(speed);
        effectButton.performClick();
        ActionBar actionBar = getSupportActionBar();
        mTitle = location_section.get(position);
        actionBar.setTitle(mTitle);
    }

    /*
    I found this comment on the internet:
    some devices filter multicast to save battery life. If it doesn´t work, Use this snippet of code to fix the issue:
    Before first receive() call WifiManager? wifi = (WifiManager??) mContext.getSystemService(Context.WIFI_SERVICE); MulticastLock? mlock = wifi.createMulticastLock("Tag for your program"); mlock.acquire(); after done with all receive() calls mlock.release();
    */


    private class UdpFindAsyncTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            Integer positionNum = params[0];

            String data = "EspFind";

            try{
                Log.v("udpfind log: ","1");
                DatagramSocket socket = new DatagramSocket(1337);
                socket.setBroadcast(true);
                Log.v("udpfind log: ", "2");

                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), getBroadcastAddress(), 1337);
                //DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByName("10.42.255.255"), 1337);
                Log.v("udpfind log: ","3.1");
                socket.send(packet);
                Log.v("udpfind log: ", "3.2");
                socket.close();
                Log.v("udpfind log: ", "4");

                byte[] buf = new byte[1024];

                for (int i = 0; i < 20; i++) {
                    Log.v("udpfind log: ", "5");
                    try {
                        socket = new DatagramSocket(1337);
                        socket.setBroadcast(true);
                        socket.setSoTimeout(100);
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String s = new String(packet.getData());
                        System.out.println(s);
                        System.out.println(packet.getLength());
                        System.out.println(packet.getAddress());
                        socket.close();
                        Log.v("udpfind log: ", "6");
                    } catch (IOException e) {
                        Log.v("error", "Could not send discovery request");
                    }
                }

                System.out.println("find is send");

                Log.v("udpfind log: ", "9");

            } catch (IOException e) {
                Log.e("error", "Could not send discovery request", e);
            }

            return null;
        }
        protected Void onPostExecute(Double result){
            return null;
        }

        protected Void onProgressUpdate(Integer... progress){
            return null;
        }

        InetAddress getBroadcastAddress() throws IOException {
            Context mContext=getApplicationContext();
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            // handle null somehow

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            return InetAddress.getByAddress(quads);
        }
    }

}

