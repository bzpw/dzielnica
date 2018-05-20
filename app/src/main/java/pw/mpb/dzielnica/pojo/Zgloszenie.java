package pw.mpb.dzielnica.pojo;

import android.graphics.Point;

/**
 * Created by Mateusz on 20.05.2018.
 */

public class Zgloszenie {
    private int type;
    private int id;
    private String geometry;


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
}
