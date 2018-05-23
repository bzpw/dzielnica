package pw.mpb.dzielnica;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;

public class map_screen extends AppCompatActivity {

    MapView map = null;
    public Button cameraBtn;
    private PopupWindow window;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        cameraBtn = (Button) findViewById(R.id.cameraBtn);


        GeoPoint center = new GeoPoint(52.220428, 21.010725);

        map = (MapView)findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(11.0);
        map.getController().setCenter(center);


        GeoJSONParse("{\"type\":\"FeatureCollection\",\"features\":[{\"id\":1,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.945648734753988,52.26411927892249]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":2,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[20.992941444098115,52.2530865417704]},\"properties\":{\"img\":null,\"type\":1}},{\"id\":3,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.010725,52.220428]},\"properties\":{\"img\":\"Schopenhauer_wN6nkI5.jpg\",\"type\":1}},{\"id\":4,\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[21.0096950317385,52.225990832712824]},\"properties\":{\"img\":\"media/images/zgloszenia/4.jpg\",\"type\":1}}]}");

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPopupWindow();
            }
        });

    }

    public void onResume(){
        super.onResume();
        map.onResume();
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
    }

}
