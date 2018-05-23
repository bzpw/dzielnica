package pw.mpb.dzielnica.pojo;

import android.graphics.Point;

/**
 * Created by Mateusz on 20.05.2018.
 */

public class Zgloszenie {
    private int type;
    private int id;
    private String geometry;
    private int user_id;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
