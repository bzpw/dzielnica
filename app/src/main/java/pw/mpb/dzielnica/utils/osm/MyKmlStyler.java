package pw.mpb.dzielnica.utils.osm;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.KmlTrack;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.List;

import pw.mpb.dzielnica.R;
import pw.mpb.dzielnica.pojo.Type;
import pw.mpb.dzielnica.utils.ApiUtils;

public class MyKmlStyler implements KmlFeature.Styler {

    private MapView map;
    private Style mDefaultStyle;
    private KmlDocument mKmlDocument;

    public MyKmlStyler(MapView map, Style mdefaultStyle, KmlDocument mkmlDocument) {
        this.map = map;
        this.mDefaultStyle = mdefaultStyle;
        this.mKmlDocument = mkmlDocument;
    }

    @Override
    public void onFeature(Overlay overlay, KmlFeature kmlFeature) {

    }

    @Override
    public void onPoint(final Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
        Log.d("OSM", "jestem");
        String type = kmlPlacemark.getExtendedData("type");
        Gson gson = new Gson();
        java.lang.reflect.Type tt = new TypeToken<Type>(){}.getType();
        final Type obj = gson.fromJson(type, tt);


        String id = Integer.toString(obj.getCategory().getId());
        if(id != null) {
            kmlPlacemark.mStyle = id;
        }
        kmlPoint.applyDefaultStyling(marker, mDefaultStyle, kmlPlacemark, mKmlDocument, map);

        String desc = kmlPlacemark.getExtendedData("desc");
        String dzielnica = kmlPlacemark.getExtendedData("dzielnica");
        String img = kmlPlacemark.getExtendedData("img");

        marker.setInfoWindow(new CustomMarkerInfoWindow(R.layout.my_bubble, map, desc, obj.getTypeName(), img, dzielnica));
        marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);

    }

    @Override
    public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {

    }

    @Override
    public void onPolygon(Polygon polygon, KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {

    }

    @Override
    public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {

    }
}