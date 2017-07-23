package com.example.reshma.kicsee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import static android.R.attr.angle;
import static android.R.attr.pivotX;
import static android.R.attr.pivotY;

public class MainActivity extends AppCompatActivity {

    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int zoom = 18;
        final boolean hybrid = false;

       final Button But = (Button) findViewById(R.id.screenP);
        final Button liv = (Button) findViewById(R.id.buttonLive);

        URL url = null;
        try {
            url = new URL("https://note-runfree.rhcloud.com/follow/fetchLocation.php?id=1");
            new getLoc(getApplicationContext(),zoom,hybrid).execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        liv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Live is data intensive.",Toast.LENGTH_SHORT).show();





            }
        });
        liv.setEnabled(false);

        But.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    URL url = new URL("https://note-runfree.rhcloud.com/follow/fetchLocation.php?id=1");
                    But.setText("Snapping...");
                    But.setEnabled(false);
                    if(isNetworkOnline()) {
                        new getLoc(getApplicationContext(), zoom, hybrid).execute(url);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Data connection off !, turn it On and restart app",Toast.LENGTH_LONG).show();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        

    }

    public boolean isNetworkOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    private void doSomethingRepeatedly() {
        Toast.makeText(getApplicationContext(),"Nee idhinae",Toast.LENGTH_SHORT).show();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask() {
            public void run() {

                try{

                    //new SendToServer().execute();
                    Toast.makeText(getApplicationContext(),"Dish",Toast.LENGTH_SHORT).show();


                }
                catch (Exception e) {
                    // TODO: handle exception
                    //Toast.makeText(getApplicationContext(),e,Toast.LENGTH_SHORT).show();

                }

            }
        }, 0, 1000);
    }

class getLoc extends AsyncTask<URL, Integer, JSONArray> {
    Context c;
    int zoom;
    boolean hyb;

    public getLoc(Context s,int z,Boolean hy)
    {
        hyb=hy;
        c=s;
        zoom=z;
    }
    protected JSONArray doInBackground(URL... urls) {

        try {
            JSONArray response = getJSONArrayFromURL("https://note-runfree.rhcloud.com/follow/fetchLocation.php?id=1");
            return response;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    protected void onPostExecute(JSONArray result) {
        Button But = (Button) findViewById(R.id.screenP);
        But.setText("Snap");
        But.setEnabled(true);
        try {
            JSONObject arr = result.getJSONObject(0);
            String lat = arr.getString("lat");
            String lng = arr.getString("lng");
            String speed = arr.getString("speed");


            float kmph = Float.parseFloat(speed);
            double dlat = (Double.parseDouble(lat));
            double dlng = (Double.parseDouble(lng));

            kmph = kmph*(18/5);

            //double speed =  Double.parseDouble(arr.getString("speed"));
            String spd = String.valueOf(speed);

            SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = null;
            try
            {
                date = form.parse(arr.getString("timed"));
            }
            catch (ParseException e)
            {

                e.printStackTrace();
            }
            SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm:ss, MMMM dd yyyy");
            String newDateStr = postFormater.format(date);


            TextView tvLast = (TextView) findViewById(R.id.tvlast);
            tvLast.setText((newDateStr));


            TextView tvCoords = (TextView) findViewById(R.id.tvcoords);
            tvCoords.setText(String.format("%.4f", dlat)+","+String.format("%.4f", dlng));

            TextView spddd = (TextView) findViewById(R.id.tvspeed);
            spddd.setText(Math.round(kmph)+" Kmph");
            if(!hyb) {
                String surl = "https://maps.googleapis.com/maps/api/staticmap?maptype=roadmap&center=" + lat + "," + lng + "&zoom=" + zoom + "&size=400x400&markers=color:blue|" + lat + "," + lng + "&key=AIzaSyBqpM4FCdaD3dz4MuYv1sE3f0iHDYqPBNw";
                ImageView vv = (ImageView) findViewById(R.id.imageView);
                new ImageLoadTask(surl,vv).execute();
            }
            else
            {
                String surl = "https://maps.googleapis.com/maps/api/staticmap?maptype=roadmap&center="+lat+","+lng+"&zoom="+zoom+"&size=400x400&markers=color:blue|"+lat+","+lng+"&key=AIzaSyBqpM4FCdaD3dz4MuYv1sE3f0iHDYqPBNw";

                ImageView vv = (ImageView) findViewById(R.id.imageView);
                new ImageLoadTask(surl,vv).execute();
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {


        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            /*Matrix matrix = new Matrix();
            imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
            matrix.postRotate((float) angle, pivotX, pivotY);
            imageView.setImageMatrix(matrix);*/
            imageView.setImageBitmap(result);
        }

    }




    public JSONArray getJSONArrayFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);
        urlConnection.setDoOutput(true);
        urlConnection.connect();
        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();
        jsonString = sb.toString();
        System.out.println("JSON: " + jsonString);
        urlConnection.disconnect();

        return new JSONArray(jsonString);
    }

}
    public String epochTo(String args) {
        String x = args;
        try {
            DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss a");
            long milliSeconds = Long.parseLong(x);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliSeconds);
            return(formatter.format(calendar.getTime()));

        }
        catch(Exception e)
        {
            return args;
        }
    }

}
