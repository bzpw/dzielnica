package pw.mpb.dzielnica.utils.osm;

import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import pw.mpb.dzielnica.R;
import pw.mpb.dzielnica.utils.ApiUtils;

/**
 * Custom bubble marker info (after clicking marker)
 */

public class CustomMarkerInfoWindow extends MarkerInfoWindow {
    //private Context context;
    private String desc;
    private String type;
    private String img;
    private String dzielnica;

    CustomMarkerInfoWindow(int layoutResId, MapView mapView, String desc, String type, String img, String dzielnica) {
        super(layoutResId, mapView);
        this.desc = desc;
        this.type = type;
        this.img = img;
        this.dzielnica = dzielnica;
    }


    @Override
    public void onOpen(Object item){
        Marker m = (Marker) item;

        ImageView iv = (ImageView) mView.findViewById(R.id.bubble_image);


        TextView title = (TextView) mView.findViewById(R.id.bubble_title);
        title.setText(desc);

        TextView snippet = (TextView) mView.findViewById(R.id.bubble_description);
        snippet.setText(type);

        TextView subdesc = (TextView) mView.findViewById(R.id.bubble_subdescription);
        subdesc.setText(dzielnica);

        if (!this.img.equals("null")) {
            Uri uri = Uri.parse(ApiUtils.BASE_URL+this.img);
            Log.d("IMG", "Uri: "+ApiUtils.BASE_URL+this.img);
            Picasso.with(mView.getContext()).load(uri).noFade().placeholder(R.drawable.placeholder)
                    .into(iv, new MarkerCallback(m));
        }
    }



    static class MarkerCallback implements Callback {
        Marker marker=null;

        MarkerCallback(Marker marker) {
            this.marker=marker;
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.showInfoWindow();
                marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
            }
        }
    }
}

