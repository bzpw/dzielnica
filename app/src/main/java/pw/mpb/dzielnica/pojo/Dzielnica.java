package pw.mpb.dzielnica.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Dzielnica POJO
 */

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
