package pw.mpb.dzielnica;

import org.json.JSONObject;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import pw.mpb.dzielnica.pojo.Category;
import pw.mpb.dzielnica.pojo.Type;
import pw.mpb.dzielnica.pojo.Zgloszenie;
import pw.mpb.dzielnica.utils.AndroidCameraFragment;
import pw.mpb.dzielnica.utils.ApiUtils;
import pw.mpb.dzielnica.utils.SessionManager;
import pw.mpb.dzielnica.utils.Utils;
import pw.mpb.dzielnica.utils.WebService;
import pw.mpb.dzielnica.utils.osm.JsonGeoPoint;
import pw.mpb.dzielnica.utils.osm.MyKmlStyler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;



public class map_screen extends AppCompatActivity {

    JsonGeoPoint currentLocation;
    private static final int REQ_CODE_PERMISSION = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    private MyLocationNewOverlay currentLocationOverlay;


    MapView map = null;
    public FloatingActionButton cameraBtn;
    private PopupWindow window;


    // Adapter REST z Retrofita
    Retrofit retrofit;
    // Interfejs API
    private WebService mWebService;

    SharedPreferences sp;
    SharedPreferences sp_typy;
    SharedPreferences sp_kategorie;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        // Sprawdzenie zezwoleń - lokalizacja
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE_PERMISSION);
            }

            return;
        }

        // Token, REST API
        sp = getSharedPreferences("authentication", MODE_PRIVATE);
        sp_typy = getSharedPreferences("TYPY", MODE_PRIVATE);
        sp_kategorie = getSharedPreferences("KATEGORIE", MODE_PRIVATE);

        mWebService = ApiUtils.getAPIService();

        cameraBtn = (FloatingActionButton) findViewById(R.id.cameraBtn);


        GeoPoint center = new GeoPoint(52.220428, 21.010725);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(center);


        //GeoJSONParse("{\"type\":\"FeatureCollection\",\"features\":[{\"id\":1,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.945648734753988,52.26411927892249]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":2,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.992941444098115,52.2530865417704]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":3,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.010725,52.220428]},\"properties\":{\"img\":\"Schopenhauer_wN6nkI5.jpg\",\"type\":1}},{\"id\":4,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.0096950317385,52.225990832712824]},\"properties\":{\"img\":\"media/images/typy/4.jpg\",\"type\":1}}]}");

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ONClICK", "costam");
                try {
                    ShowPopupWindow();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    public void onResume() {
        super.onResume();
        map.onResume();

        createLocationRequest();
        refreshtypy();

    }

    private void refreshtypy() {
        mWebService.listZgloszenia("JWT " + SessionManager.getToken(sp)).enqueue(new Callback<ResponseBody>() {
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

    public void onPause() {
        super.onPause();
        map.onPause();
    }

    public void GeoJSONParse(String geoJSON) {
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(geoJSON);

        // ścieżka do pamięci telefonu
        File sd = getExternalFilesDir(null);

        // Dodawanie styli na podstawie pobranych wcześniej
        Gson gson = new Gson();
        String json_kategorie = sp_kategorie.getString("Kategorie", null);
        java.lang.reflect.Type type = new TypeToken<List<Category>>(){}.getType();
        List<Category> cats = gson.fromJson(json_kategorie, type);

        for (Category kategoria : cats) {
            // TODO: wywalić to gdzieś indziej
            String filename = "";
            if(kategoria.getIcon() != null) {
                filename = kategoria.getIcon().substring(kategoria.getIcon().lastIndexOf('/') + 1);

                File img = new File(sd.getPath() + File.separator + filename);
                Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(img.getPath()), 50, 50, false);
                Style style = new Style(bitmap, 0x901010AA, 5f, 0x20AA1010);

                kmlDocument.putStyle(Integer.toString(kategoria.getId()), style);
            }

        }

        // Domyślny marker jeśli nie ma ikonki
        Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style mdefaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, 0x20AA1010);

        // Styler do zmiany ikonek w zależności od kategorii
        MyKmlStyler styler = new MyKmlStyler(map, mdefaultStyle, kmlDocument);
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);


        map.getOverlays().add(geoJsonOverlay);
        map.invalidate();
    }


    // Co się dzieje po aktualizacji lokalizacji
    private LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                updateLocation(location);
                Log.d("GEO", currentLocation.toString());
                Log.d("GEO", "(" + Double.toString(currentLocation.getLatitude()));
                displayMyCurrentLocationOverlay();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //createLocationRequest();
    }


    protected void updateLocation(Location location) {
        // Logic to handle location object
        String latVal = Double.toString(location.getLatitude());
        String lonVal = Double.toString(location.getLongitude());

        String timestamp = Double.toString(location.getTime());


        Log.d("LOKACJA", "[" + timestamp + "] Location: " + latVal + ", " + lonVal);

        currentLocation = new JsonGeoPoint(location);
        Log.d("GEO", Double.toString(currentLocation.getLatitude()));
        displayMyCurrentLocationOverlay();

    }

    @SuppressLint("MissingPermission")
    private void createLocationRequest() {
        if (Utils.hasLocationPermission(this)) {

            LocationRequest request = new LocationRequest();
            request.setInterval(5000);
            request.setFastestInterval(1000);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Log.d("GEO", "onRequestPermissionResults <---");

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(request, callback, null);
        }

    }

    private void displayMyCurrentLocationOverlay() {
        Log.d("GEO", "displayOverlay: "+currentLocation.toString());
        if( currentLocation != null) {
            if(currentLocationOverlay == null ) {
                Context context = this.getApplicationContext();
                currentLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
                currentLocationOverlay.enableMyLocation();
                Log.d("GEO", currentLocationOverlay.toString());
                map.getOverlays().add(currentLocationOverlay);

            } else {

            }

        }
    }

    // Okienko dodawania zgłoszeń
    private void ShowPopupWindow() throws IOException {
        LayoutInflater inflater = (LayoutInflater) map_screen.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_popup, null);
        window = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        //final EditText editTypeNo = (EditText)layout.findViewById(R.id.repCatNrTxt);
        final EditText editTypeNo = (EditText)layout.findViewById(R.id.repReportTxt);
        final EditText editDesc = (EditText)layout.findViewById(R.id.repReportTxt);

        Gson gson = new Gson();
        String json = sp_typy.getString("Typy", "");
        Log.d("API", json);

        String json_cats = sp_kategorie.getString("Kategorie", null);
        Log.d("API-kategorie", json_cats);

        java.lang.reflect.Type type = new TypeToken<List<Type>>(){}.getType();
        List<Type> typy = gson.fromJson(json, type);


        // Spinner
        ArrayAdapter typeAdapter = new ArrayAdapter(getApplicationContext(), R.layout.cat_spinner, typy);

        final Spinner typeSpinner = (Spinner) layout.findViewById(R.id.repSpinner);
        typeSpinner.setAdapter(typeAdapter);

        Button btnReport = (Button)layout.findViewById(R.id.repReportBtn);
        Button btnCamera = (Button)layout.findViewById(R.id.repCameraBtn);

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Type typ = (Type) typeSpinner.getSelectedItem();
                int cat = typ.getId();
                String desc = editDesc.getText().toString();

                dodajZgloszenie(cat, desc, 1, "POINT");
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.mapContent, new AndroidCameraFragment());
                transaction.commit();

                //finish();
                //startActivity(new Intent(map_screen.this, AndroidCameraApi.class));
            }
        });

        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.setOutsideTouchable(false);
        window.showAtLocation(layout, Gravity.CENTER, 40, 60);


    }

    private void dodajZgloszenie(int cat, String desc, int user, String JSONmethod) {

        String geometry = "";
        if (JSONmethod.equals("POINT"))
            geometry = currentLocation.getPOINT();
        else if (JSONmethod.equals("GEOJSON"))
            geometry = currentLocation.getJSON();

        mWebService.addZgloszenie("JWT "+SessionManager.getToken(sp), cat, desc, geometry, user).enqueue(new Callback<Zgloszenie>() {
            @Override
            public void onResponse(Call<Zgloszenie> call, Response<Zgloszenie> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(map_screen.this, "Dodano zgłoszenie!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        int code = response.code();
                        String err = response.errorBody().string();
                        ApiUtils.logResponse(err);
                        ApiUtils.showErrToast(getApplicationContext(), code, err);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ApiUtils.logResponse(response.errorBody().toString());
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
}
