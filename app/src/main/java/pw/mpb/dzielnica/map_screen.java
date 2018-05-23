package pw.mpb.dzielnica;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import pw.mpb.dzielnica.pojo.Zgloszenie;
import pw.mpb.dzielnica.utils.ApiUtils;
import pw.mpb.dzielnica.utils.SessionManager;
import pw.mpb.dzielnica.utils.WebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;






public class map_screen extends AppCompatActivity {

    GeoPoint currentLocation;
    private static final int REQ_CODE_PERMISSION = 1;

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            currentLocation = new GeoPoint(location);
            Log.d("GEO", Double.toString(currentLocation.getLatitude()));
            //displayMyCurrentLocationOverlay();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }


    MapView map = null;
    public FloatingActionButton cameraBtn;
    private PopupWindow window;


    // Adapter REST z Retrofita
    Retrofit retrofit;
    // Interfejs API
    private WebService mWebService;

    SharedPreferences sp;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        // Lokalizacja
//        Location location = new Location(LocationManager.GPS_PROVIDER);
//        location.setLatitude(MAP_DEFAULT_LATITUDE);
//        location.setLongitude(MAP_DEFAULT_LONGITUDE);
//        GeoPoint ZmiennaLokalizacjiwPunkcie = new GeoPoint(location);
//        ZmiennaLokalizacjiwPunkcie.getLatitude();
//        ZmiennaLokalizacjiwPunkcie.getLongitude();


        //Lokalizacja 2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE_PERMISSION);
        }

        MyLocationListener locationListener = new MyLocationListener();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if( location != null ) {
            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        }

        // Token, REST API
        sp = getSharedPreferences("authentication", MODE_PRIVATE);
        mWebService = ApiUtils.getAPIService();

        cameraBtn = (FloatingActionButton) findViewById(R.id.cameraBtn);


        GeoPoint center = new GeoPoint(52.220428, 21.010725);

        map = (MapView)findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(center);


        //GeoJSONParse("{\"type\":\"FeatureCollection\",\"features\":[{\"id\":1,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.945648734753988,52.26411927892249]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":2,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.992941444098115,52.2530865417704]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":3,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.010725,52.220428]},\"properties\":{\"img\":\"Schopenhauer_wN6nkI5.jpg\",\"type\":1}},{\"id\":4,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.0096950317385,52.225990832712824]},\"properties\":{\"img\":\"media/images/zgloszenia/4.jpg\",\"type\":1}}]}");

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ONClICK", "costam");
                ShowPopupWindow();


            }
        });

    }

    public void onResume(){
        super.onResume();
        map.onResume();

        refreshZgloszenia();

    }

    private void refreshZgloszenia() {
        mWebService.listZgloszenia("JWT "+ SessionManager.getToken(sp)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    String json = null;
                    try {
                        json = response.body().string();
                        GeoJSONParse(json);
                        Log.w("json", json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.w("json", call.request().header("Authorization"));
                    Log.w("json", response.toString());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void onPause(){
        super.onPause();
        map.onPause();
    }

    public void GeoJSONParse(String geoJSON){
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(geoJSON);
        Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, 0x20AA1010);
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument);
        map.getOverlays().add(geoJsonOverlay);
        map.invalidate();
    }

    private void ShowPopupWindow(){
        LayoutInflater inflater = (LayoutInflater) map_screen.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_popup, null);
        window = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        final EditText editTypeNo = (EditText)layout.findViewById(R.id.repCatNrTxt);
        final EditText editDesc = (EditText)layout.findViewById(R.id.repReportTxt);

        Button btnReport = (Button)layout.findViewById(R.id.repReportBtn);

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int cat = Integer.parseInt(editTypeNo.getText().toString());
                String desc = editDesc.getText().toString();
                //{"type": "Point", "coordinates": [21.010725, 52.220428]}
                mWebService.addZgloszenie(cat, desc, "{\"type\": \"Point\", \"coordinates\": [21.010725, 52.220428]}", 1).enqueue(new Callback<Zgloszenie>() {
                    @Override
                    public void onResponse(Call<Zgloszenie> call, Response<Zgloszenie> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(map_screen.this, "OK!", Toast.LENGTH_SHORT).show();

                        } else {
                            ApiUtils.logResponse(response.toString());
                            Toast.makeText(map_screen.this, "BAD", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(map_screen.this, "Dodano zgłoszenie!", Toast.LENGTH_SHORT).show();

                        window.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Zgloszenie> call, Throwable t) {
                        ApiUtils.logFailure(t);
                    }
                });
            }
        });



        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.setOutsideTouchable(false);
        window.showAtLocation(layout, Gravity.CENTER, 40, 60);


    }

    private void displayMyCurrentLocationOverlay() {
//        if( currentLocation != null) {
//            if( currentLocationOverlay == null ) {
//                currentLocationOverlay = new ArrayItemizedOverlay(myLocationMarker);
//                myCurrentLocationOverlayItem = new OverlayItem(currentLocation, "My Location", "My Location!");
//                currentLocationOverlay.addItem(myCurrentLocationOverlayItem);
//                map.getOverlays().add(currentLocationOverlay);
//            } else {
//                myCurrentLocationOverlayItem.setPoint(currentLocation);
//                currentLocationOverlay.requestRedraw();
//            }
//            map.getController().setCenter(currentLocation);
//        }
    }
}
