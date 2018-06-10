package pw.mpb.dzielnica.utils.osm;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import pw.mpb.dzielnica.R;

/**
 * Created by Mateusz on 10.06.2018.
 */

public class CustomMarkerInfoWindow extends MarkerInfoWindow {
    //private Context context;

    public CustomMarkerInfoWindow(MapView mapView) {
        super(R.layout.my_bubble, mapView);
      //  this.context = ctx;
    }


    @Override
    public void onOpen(Object item){
        Marker m = (Marker) item;

        ImageView iv = (ImageView) mView.findViewById(R.id.bubble_image);
        iv.setImageResource(R.drawable.syrenka2);

        TextView title = (TextView) mView.findViewById(R.id.bubble_title);
        title.setText(m.getTitle());

        TextView snippet = (TextView) mView.findViewById(R.id.bubble_description);
        snippet.setText(m.getSnippet());

				/*Button bt = (Button) mView.findViewById(R.id.bubble_moreinfo);
				bt.setVisibility(View.VISIBLE);
				bt.setOnClickListener(new Button.OnClickListener(){

					@Override
					public void onClick(View v) {
						Toast.makeText(MainActivity.this, "Button working", Toast.LENGTH_SHORT).show();
					}

				});*/
    }
}

