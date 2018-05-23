package pw.mpb.dzielnica.pojo;

/**
 * Created by Mateusz on 20.05.2018.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Dzielnica {

    @SerializedName("gid")
    @Expose
    private Integer gid;
    @SerializedName("name")
    @Expose
    private String name;

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
