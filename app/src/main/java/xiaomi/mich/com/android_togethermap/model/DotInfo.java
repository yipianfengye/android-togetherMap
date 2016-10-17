package xiaomi.mich.com.android_togethermap.model;

/**
 * Created by aaron on 2016/10/17.
 */

public class DotInfo {

    private String dotId;
    private int carTotal;
    private double dotLat;
    private double dotLon;


    public String getDotId() {
        return dotId;
    }

    public void setDotId(String dotId) {
        this.dotId = dotId;
    }

    public int getCarTotal() {
        return carTotal;
    }

    public void setCarTotal(int carTotal) {
        this.carTotal = carTotal;
    }

    public double getDotLat() {
        return dotLat;
    }

    public void setDotLat(double dotLat) {
        this.dotLat = dotLat;
    }

    public double getDotLon() {
        return dotLon;
    }

    public void setDotLon(double dotLon) {
        this.dotLon = dotLon;
    }
}
