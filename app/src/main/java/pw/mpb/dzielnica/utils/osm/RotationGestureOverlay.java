package pw.mpb.dzielnica.utils.osm;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

/**
 * Created by Mateusz on 12.06.2018.
 */

public class RotationGestureOverlay extends Overlay implements RotationGestureDetector.RotationListener {

    private final RotationGestureDetector mRotationDetector;
    private RotationGestureDetector.RotationListener rotationListener;


    public RotationGestureOverlay(Context context, RotationGestureDetector.RotationListener rotationListener) {
        super(context);
        this.rotationListener = rotationListener;
        mRotationDetector = new RotationGestureDetector(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if (this.isEnabled()) {
            mRotationDetector.onTouch(event);
        }
        return super.onTouchEvent(event, mapView);
    }

    @Override
    public void onRotate(float deltaAngle) {
        rotationListener.onRotate(deltaAngle);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean b) {

    }
}