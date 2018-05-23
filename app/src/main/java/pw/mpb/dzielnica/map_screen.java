package pw.mpb.dzielnica;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
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

    MapView map = null;

    // Adapter REST z Retrofita
    Retrofit retrofit;
    // Interfejs API
    private WebService mWebService;

    SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        // Token, REST API
        sp = getSharedPreferences("authentication", MODE_PRIVATE);
        mWebService = ApiUtils.getAPIService();


        GeoPoint center = new GeoPoint(52.220428, 21.010725);

        map = (MapView)findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(11.0);
        map.getController().setCenter(center);


        //GeoJSONParse("{\"type\":\"FeatureCollection\",\"features\":[{\"id\":1,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.945648734753988,52.26411927892249]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":2,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.992941444098115,52.2530865417704]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":3,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.010725,52.220428]},\"properties\":{\"img\":\"Schopenhauer_wN6nkI5.jpg\",\"type\":1}},{\"id\":4,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.0096950317385,52.225990832712824]},\"properties\":{\"img\":\"media/images/zgloszenia/4.jpg\",\"type\":1}}]}");
    }

    public void onResume(){
        super.onResume();
        map.onResume();
        Toast.makeText(this, "JWT "+ SessionManager.getToken(sp), Toast.LENGTH_SHORT).show();
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

}
