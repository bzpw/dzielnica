package pw.mpb.dzielnica;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.events.MapEventsReceiver;
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
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import pw.mpb.dzielnica.pojo.Category;
import pw.mpb.dzielnica.pojo.Type;
import pw.mpb.dzielnica.pojo.Zgloszenie;
import pw.mpb.dzielnica.utils.ApiUtils;
import pw.mpb.dzielnica.utils.SessionManager;
import pw.mpb.dzielnica.utils.Utils;
import pw.mpb.dzielnica.utils.WebService;
import pw.mpb.dzielnica.utils.osm.JsonGeoPoint;
import pw.mpb.dzielnica.utils.osm.MyKmlStyler;
import pw.mpb.dzielnica.utils.osm.RotationGestureDetector;
import pw.mpb.dzielnica.utils.osm.RotationGestureOverlay;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;



public class map_screen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RotationGestureDetector.RotationListener, MapEventsReceiver {


    JsonGeoPoint currentLocation;
    private static final int REQ_CODE_PERMISSION = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    private MyLocationNewOverlay currentLocationOverlay;
    private FolderOverlay geoJsonOverlay;

    MapView map = null;
    public FloatingActionButton cameraBtn;
    private PopupWindow window;


    // Adapter REST z Retrofita
    Retrofit retrofit;
    // Interfejs API
    private WebService mWebService;

    // Pobieranie (co 10s) zgłoszeń
    final Handler handler = new Handler();
    final int delay = 10000; //10s
    final Runnable runnable = new Runnable() {
        public void run() {
            updateZgloszeniaOnMap();
            Log.d("HANDLER", "t");
            handler.postDelayed(this, delay);
        }
    };

    SharedPreferences sp;
    SharedPreferences sp_typy;
    SharedPreferences sp_kategorie;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        // Obsługa menu rozwijanego z lewej
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        FloatingActionButton btnOpenMenu = (FloatingActionButton) findViewById(R.id.btnMenu);
        btnOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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

        //cameraBtn = (FloatingActionButton) findViewById(R.id.cameraBtn);


        GeoPoint center = new GeoPoint(52.220428, 21.010725);



        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getOverlays().add(new RotationGestureOverlay(this, this));
        map.getController().setZoom(19.0);
        map.setMinZoomLevel(15.0);
        map.getController().setCenter(center);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);


        FloatingActionButton btnCenter = (FloatingActionButton) findViewById(R.id.btnCenterLocation);
        btnCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != currentLocation) {
                    GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.getController().animateTo(myPosition);
                } else {
                    Toast.makeText(map_screen.this, "Nie wykryto pozycji użytkownika... Upewnij się, że lokalizacja działa poprawnie.", Toast.LENGTH_LONG).show();

                }
            }
        });
        updateZgloszeniaOnMap();
        centerToLocation();
    }

    public void onResume() {
        super.onResume();
        map.onResume();
        ApiUtils.onUnLoggedRedirect(sp, map_screen.this, UserLogin.class);
        createLocationRequest();
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, delay);
        centerToLocation();
    }

    private void updateZgloszeniaOnMap() {
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
        updateZgloszeniaOnMap();
        handler.removeCallbacks(runnable);
    }

    public void GeoJSONParse(String geoJSON) {
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(geoJSON);

        addCatsStyles(kmlDocument);

        // Domyślny marker jeśli nie ma ikonki
        Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style mdefaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, 0x20AA1010);

        // Styler do zmiany ikonek w zależności od kategorii
        MyKmlStyler styler = new MyKmlStyler(map, mdefaultStyle, kmlDocument);
        geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);

        map.getOverlays().add(1, geoJsonOverlay);
        map.invalidate();
    }

    // Dodawanie styli na podstawie pobranych wcześniej ikonek kategorii
    private void addCatsStyles(KmlDocument kmlDocument) {
        Gson gson = new Gson();
        String json_kategorie = sp_kategorie.getString("Kategorie", null);
        java.lang.reflect.Type type = new TypeToken<List<Category>>(){}.getType();
        List<Category> cats = gson.fromJson(json_kategorie, type);

        // ścieżka do pamięci telefonu
        File sd = getExternalFilesDir(null);

        for (Category kategoria : cats) {
            String filename = "";
            if(kategoria.getIcon() != null) {
                filename = kategoria.getIcon().substring(kategoria.getIcon().lastIndexOf('/') + 1);

                File img = new File(sd.getPath() + File.separator + filename);
                Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(img.getPath()), 200, 200, false);
                Style style = new Style(bitmap, 0x901010AA, 5f, 0x20AA1010);

                kmlDocument.putStyle(Integer.toString(kategoria.getId()), style);
            }

        }
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

//        btnCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.mapContent, new AndroidCameraFragment());
//                transaction.commit();
//
//                //finish();
//                //startActivity(new Intent(map_screen.this, AndroidCameraApi.class));
//            }
//        });

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

                window.dismiss();
            }

            @Override
            public void onFailure(Call<Zgloszenie> call, Throwable t) {
                ApiUtils.logFailure(t);
            }
        });
    }

    // Obsługa menu
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addReport) {
            try {
                ShowPopupWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_myProfile) {

           // finish();
            startActivity(new Intent(map_screen.this, ProfileScreen.class));

        } else if (id == R.id.nav_myReports) {

        } else if (id == R.id.nav_options) {

        } else if (id == R.id.nav_logout) {
            SessionManager.removeToken(sp);
            ApiUtils.onUnLoggedRedirect(sp, map_screen.this, UserLogin.class);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Obsługa kliknięć na mapie
    @Override public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(map);
        for (InfoWindow infoWindow:
        InfoWindow.getOpenedInfoWindowsOn(map)) {

            Log.d("INFOWINDOW", infoWindow.toString());

        }
        Log.d("INFOWINDOW", "normal click");
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    @Override
    public void onRotate(float deltaAngle) {
        Log.d("OSM", "rotate: " + deltaAngle);
        //map.setMapOrientation(map.getMapOrientation()+deltaAngle);
    }


    private void centerToLocation() {
        if (null != currentLocation) {
            GeoPoint center = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.getController().setCenter(center);
        }
    }
}
