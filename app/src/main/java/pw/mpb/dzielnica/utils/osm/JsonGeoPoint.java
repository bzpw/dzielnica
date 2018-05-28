package pw.mpb.dzielnica.utils.osm;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Mateusz on 25.05.2018.
 */

public class JsonGeoPoint extends GeoPoint {


    public JsonGeoPoint(int aLatitudeE6, int aLongitudeE6) {
        super(aLatitudeE6, aLongitudeE6);
    }

    public JsonGeoPoint(int aLatitudeE6, int aLongitudeE6, int aAltitude) {
        super(aLatitudeE6, aLongitudeE6, aAltitude);
    }

    public JsonGeoPoint(double aLatitude, double aLongitude) {
        super(aLatitude, aLongitude);
    }

    public JsonGeoPoint(double aLatitude, double aLongitude, double aAltitude) {
        super(aLatitude, aLongitude, aAltitude);
    }

    public JsonGeoPoint(Location aLocation) {
        super(aLocation);
    }

    public JsonGeoPoint(GeoPoint aGeopoint) {
        super(aGeopoint);
    }

    public String getJSON() {
        String lonS = Double.toString(this.getLongitude());
        String latS = Double.toString(this.getLatitude());
        return "{\"type\": \"Point\", \"coordinates\": ["+lonS+", "+latS+"]}";
    }

    public String getPOINT() {
        String lonS = Double.toString(this.getLongitude());
        String latS = Double.toString(this.getLatitude());
        return "POINT("+lonS+" "+latS+")";

    }
}
