package xiaomi.mich.com.android_togethermap.model;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 初始化数据
     * @return
     */
    public static List<DotInfo> initData() {
        List<DotInfo> dotInfoList = new ArrayList<>();

        DotInfo dotInfo1 = new DotInfo();
        dotInfo1.setDotLat(39.976318);
        dotInfo1.setDotLon(116.318988);
        dotInfoList.add(dotInfo1);

        DotInfo dotInfo2 = new DotInfo();
        dotInfo2.setDotLat(40.045813);
        dotInfo2.setDotLon(116.304504);
        dotInfoList.add(dotInfo2);

        DotInfo dotInfo3 = new DotInfo();
        dotInfo3.setDotLat(39.923347);
        dotInfo3.setDotLon(116.507537);
        dotInfoList.add(dotInfo3);

        DotInfo dotInfo4 = new DotInfo();
        dotInfo4.setDotLat(39.91125);
        dotInfo4.setDotLon(116.477378);
        dotInfoList.add(dotInfo4);

        DotInfo dotInfo5 = new DotInfo();
        dotInfo5.setDotLat(40.041337);
        dotInfo5.setDotLon(116.312181);
        dotInfoList.add(dotInfo5);

        DotInfo dotInfo6 = new DotInfo();
        dotInfo6.setDotLat(39.971879);
        dotInfo6.setDotLon(116.306446);
        dotInfoList.add(dotInfo6);

        DotInfo dotInfo7 = new DotInfo();
        dotInfo7.setDotLat(40.002434);
        dotInfo7.setDotLon(116.463232);
        dotInfoList.add(dotInfo7);

        DotInfo dotInfo8 = new DotInfo();
        dotInfo8.setDotLat(39.980959);
        dotInfo8.setDotLon(116.331772);
        dotInfoList.add(dotInfo8);

        DotInfo dotInfo9 = new DotInfo();
        dotInfo9.setDotLat(39.925659);
        dotInfo9.setDotLon(116.508567);
        dotInfoList.add(dotInfo9);

        DotInfo dotInfo10 = new DotInfo();
        dotInfo10.setDotLat(39.88543);
        dotInfo10.setDotLon(116.461778);
        dotInfoList.add(dotInfo10);

        DotInfo dotInfo11 = new DotInfo();
        dotInfo11.setDotLat(39.98343);
        dotInfo11.setDotLon(116.311843);
        dotInfoList.add(dotInfo11);

        DotInfo dotInfo12 = new DotInfo();
        dotInfo12.setDotLat(40.029849);
        dotInfo12.setDotLon(116.313302);
        dotInfoList.add(dotInfo12);

        return dotInfoList;
    }
}
