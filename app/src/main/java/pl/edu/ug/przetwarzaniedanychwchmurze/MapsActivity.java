package pl.edu.ug.przetwarzaniedanychwchmurze;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    ArrayList<JSONObject> arrayListOfJsonObjects = new ArrayList<JSONObject>();

    ArrayList<Double> LatFromJsonArrayList = new ArrayList<Double>();
    ArrayList<Double> LonFromJsonArrayList = new ArrayList<Double>();
    ArrayList<String> DataGeneratedFromJsonArrayList = new ArrayList<String>();
    ArrayList<String> LineFromJsonArrayList = new ArrayList<String>();
    ArrayList<String> RouteFromJsonArrayList = new ArrayList<String>();
    ArrayList<String> VehicleCodeFromJsonArrayList = new ArrayList<String>();
    ArrayList<Integer> SpeedFromJsonArrayList = new ArrayList<Integer>();
    ArrayList<Integer> DelayFromJsonArrayList = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //wywolanie polaczenia z api
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://ckan2.multimediagdansk.pl/gpsPositions").get(); //get pozwala zaladowac dane na mape, bez tego wrzuca puste LatFromJson i LonFromJson
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng GdanskCenter = new LatLng(54.375739741318725, 18.629144086267008);
        //LatLng vehicle1 = new LatLng(54.35007858276367, 18.59699058532715);
        //LatLng vehicle2 = new LatLng(54.35274124145508, 18.550180435180664);

        for(int i=0; i<arrayListOfJsonObjects.size(); i++) {
            if (DelayFromJsonArrayList.get(i) > 180) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(LatFromJsonArrayList.get(i), LonFromJsonArrayList.get(i)))
                        .title("Info")
                        .snippet("Stan na: "+DataGeneratedFromJsonArrayList.get(i)+
                                "\nLinia: "+LineFromJsonArrayList.get(i)+
                                "\nTrasa: "+RouteFromJsonArrayList.get(i)+
                                "\nKod pojazdu: "+VehicleCodeFromJsonArrayList.get(i)+
                                "\nPrędkość: "+SpeedFromJsonArrayList.get(i)+" km/h"+
                                "\nOpóźnienie: "+DelayFromJsonArrayList.get(i)+" sekund"
                        )
                );
            }
        }

        //mMap.addMarker(new MarkerOptions().position(vehicle1).title("Vehicle 1").snippet("Population: 4,137,400. \nKlucz: wartosc. \nKlucz: wartosc. \nKlucz: wartosc"));
        //mMap.addMarker(new MarkerOptions().position(vehicle2).title("Vehicle 2").snippet("My Snippet"+"\n"+"1st Line Text"+"\n"+"2nd Line Text"+"\n"+"3rd Line Text"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GdanskCenter, 12));
        Log.i("liczba pojazdow", String.valueOf(arrayListOfJsonObjects.size()));

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(MapsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

    }

    //polaczenie z api
    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String json = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while (data != -1) {
                    char letter = (char) data;
                    json += letter;
                    data = reader.read();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            try {
                JSONObject jsonObject = new JSONObject(json);
                String vehiclesInfo = jsonObject.getString("Vehicles");
                JSONArray array = new JSONArray(vehiclesInfo);

                for(int i=0; i<array.length(); i++) {
                    arrayListOfJsonObjects.add(array.getJSONObject(i));
                    LatFromJsonArrayList.add(Double.valueOf(arrayListOfJsonObjects.get(i).getString("Lat")));
                    LonFromJsonArrayList.add(Double.valueOf(arrayListOfJsonObjects.get(i).getString("Lon")));
                    DataGeneratedFromJsonArrayList.add(arrayListOfJsonObjects.get(i).getString("DataGenerated"));
                    LineFromJsonArrayList.add(arrayListOfJsonObjects.get(i).getString("Line"));
                    RouteFromJsonArrayList.add(arrayListOfJsonObjects.get(i).getString("Route"));
                    VehicleCodeFromJsonArrayList.add(arrayListOfJsonObjects.get(i).getString("VehicleCode"));
                    SpeedFromJsonArrayList.add(Integer.valueOf(arrayListOfJsonObjects.get(i).getString("Speed")));
                    DelayFromJsonArrayList.add(Integer.valueOf(arrayListOfJsonObjects.get(i).getString("Delay")));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}