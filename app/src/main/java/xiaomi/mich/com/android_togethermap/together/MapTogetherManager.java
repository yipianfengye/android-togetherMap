package xiaomi.mich.com.android_togethermap.together;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import xiaomi.mich.com.android_togethermap.model.DotInfo;
import xiaomi.mich.com.android_togethermap.utils.DisplayUtil;

/**
 * Created by aaron on 16/6/2.
 * 地图网点聚合
 */
public class MapTogetherManager {

    public static final String TAG = MapTogetherManager.class.getSimpleName();

    private int CLUSTER_SIZE = 100;// 地图上距离小于多少个像素点,则合并
    private int DRAWABLE_RADIUS = 80;// 聚合网点的显示大小,drawable的radius大小
    private static Map<String, TogDotInfo> togDotInfoMap;// 聚合网点列表
    private static Map<String, Marker> togMarkerMap;// 聚合网点marker
    private Activity mContext;
    private AMap aMap;


    public static MapTogetherManager instance = null;

    public static MapTogetherManager getInstance(Activity mContext, AMap aMap) {
        if (instance == null) {
            instance = new MapTogetherManager(mContext, aMap);
        }
        return instance;
    }

    private MapTogetherManager(Activity mContext, AMap aMap) {
        this.mContext = mContext;
        this.aMap = aMap;
        togDotInfoMap = new ConcurrentHashMap<String, TogDotInfo>();
        togMarkerMap = new ConcurrentHashMap<String, Marker>();
    }

    // ########### 设置当地图加载完成事件 start ######################

    /**
     * 更新聚合网点显示
     * @param markerMap
     */
    public void onMapLoadedUpdateMarkerText(final List<DotInfo> allDotsList, final Map<String, Marker> markerMap) {
        // 循环遍历更新聚合marker显示文案
        if (togDotInfoMap != null && togDotInfoMap.entrySet().size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "开始循环遍历,执行更新网点text内容的操作...");
                    Iterator<Map.Entry<String, TogDotInfo>> entryIterator = togDotInfoMap.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        Map.Entry<String, TogDotInfo> entryTog = entryIterator.next();
                        TogDotInfo togDotInfo = entryTog.getValue();
                        List<Marker> listMarker = togDotInfo.getmClusterItems();
                        if (listMarker != null && listMarker.size() > 0) {
                            int count = 0;
                            for (Marker marker : listMarker) {
                                DotInfo dotInfo = (DotInfo) marker.getObject();
                                for (int i = 0; i < allDotsList.size(); i ++) {
                                    if (i < allDotsList.size()) {
                                        if (allDotsList.get(i).getDotId().equals(dotInfo.getDotId())) {
                                            if (i < allDotsList.size()) {
                                                marker.setObject(allDotsList.get(i));
                                            }
                                        }
                                    }
                                }
                                count = count + ((DotInfo)marker.getObject()).getCarTotal();
                            }
                            togDotInfo.setCarCount(count);
                            if (togDotInfo.getTextView() != null) {
                                String pre = togDotInfo.getTextView().getText().toString();

                                if (!TextUtils.isEmpty(pre) && !pre.trim().equals(togDotInfo.getCarCount() + "辆")) {
                                    togMarkerMap.get(entryTog.getKey()).remove();
                                    togMarkerMap.remove(entryTog.getKey());
                                    addTogDotInfoToMap(entryTog.getKey(), entryTog.getValue());
                                }
                            }
                        }
                    }
                }
            }).start();
        }
    }



    Object lockObject = new Object();
    /**
     * 更新聚合网点
     */
    public void onMapLoadedUpdateMarker(final Map<String, Marker> markerMap) {
        // 清空内存聚合网点数据
        togDotInfoMap.clear();
        togMarkerMap.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lockObject) {
                    Log.i(TAG, "开始循环遍历,执行网点聚合操作...");
                    Iterator<Map.Entry<String, Marker>> iterator = markerMap.entrySet().iterator();
                    // 循环遍历在已有的聚合基础上，对添加的单个元素进行聚合
                    while (iterator.hasNext()) {
                        assignSingleCluster(iterator.next().getValue());
                    }
                    Log.i(TAG, "开始执行将聚合网点展示在地图上...");
                    // 将聚合的网点现在在地图上
                    if (togDotInfoMap != null && togDotInfoMap.size() > 0) {
                        Iterator<Map.Entry<String, TogDotInfo>> cIterator = togDotInfoMap.entrySet().iterator();
                        while (cIterator.hasNext()) {
                            Map.Entry<String, TogDotInfo> togMap = cIterator.next();
                            addTogDotInfoToMap(togMap.getKey(), togMap.getValue());
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 在已有的聚合基础上，对添加的单个元素进行聚合
     */
    private void assignSingleCluster(Marker marker) {
        DotInfo dotInfo = (DotInfo) marker.getObject();
        LatLng latLng = new LatLng(dotInfo.getDotLat(), dotInfo.getDotLon());

        Point point = aMap.getProjection().toScreenLocation(latLng);
        TogDotInfo togDotInfo = getCluster(point);
        if (togDotInfo != null) {
            togDotInfo.addClusterItem(marker);
            // 更新聚合网点的车辆个数,网点个数
            togDotInfo.setCarCount(togDotInfo.getCarCount() + dotInfo.getCarTotal());
            togDotInfo.setDotCount(togDotInfo.getDotCount() + 1);
        } else {
            togDotInfo = new TogDotInfo(point, latLng);
            // 更新聚合网点的车辆个数
            togDotInfo.setCarCount(dotInfo.getCarTotal());
            togDotInfo.setDotCount(1);
            togDotInfo.addClusterItem(marker);
            togDotInfoMap.put(dotInfo.getDotId() + SystemClock.currentThreadTimeMillis(), togDotInfo);
        }
    }


    /**
     * 将聚合网点添加至地图显示
     * @param togDotInfo 需要更新的聚合网点对象
     */
    private void addTogDotInfoToMap(String dotId, TogDotInfo togDotInfo) {
        LatLng latlng = togDotInfo.getCenterLatLng();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.anchor(0.5f, 0.5f).icon(getBitmapDes(togDotInfo)).position(latlng);
        Marker marker = aMap.addMarker(markerOptions);
        togMarkerMap.put(dotId, marker);
    }


    /**
     * 获取每个聚合点的绘制样式
     **/
    private BitmapDescriptor getBitmapDes(final TogDotInfo togDotInfo) {
        TextView textView = new TextView(mContext);
        String tile = String.valueOf(togDotInfo.getCarCount());
        textView.setText(tile + "辆");
        textView.setGravity(Gravity.CENTER);

        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setBackgroundDrawable(getDrawAble(togDotInfo.getCarCount()));
        togDotInfo.setTextView(textView);
        return BitmapDescriptorFactory.fromView(textView);
    }

    /**
     * 根据一个点获取是否可以依附的聚合点，没有则返回null
     * @param point
     * @return
     */
    private TogDotInfo getCluster(Point point) {
        Iterator<Map.Entry<String, TogDotInfo>> cIterator = togDotInfoMap.entrySet().iterator();
        while (cIterator.hasNext()) {
            TogDotInfo togDotInfo = cIterator.next().getValue();
            Point poi = togDotInfo.getCenterPoint();
            double distance = getDistanceBetweenTwoPoints(point.x, point.y, poi.x, poi.y);
            if (distance < CLUSTER_SIZE) {
                return togDotInfo;
            }
        }

        return null;
    }

    /**
     * 计算地图上两个点之间的距离
     * @return
     */
    private double getDistanceBetweenTwoPoints(double x1, double y1, double x2,
                                               double y2) {
        double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
                * (y1 - y2));
        return distance;
    }


    /**
     * 获取聚合网点显示的Drawable
     * @param clusterNum
     * @return
     */
    public Drawable getDrawAble(int clusterNum) {
        int radius = DisplayUtil.dip2px(mContext, DRAWABLE_RADIUS);
        // 根据不同的网点个数,显示不同的聚合网点drawable
        // if (clusterNum < 5) {
            BitmapDrawable drawable = new BitmapDrawable(drawCircle(radius,
                    Color.argb(159, 210, 154, 6)));
            return drawable;
        /*} else if (clusterNum < 10) {
            BitmapDrawable drawable = new BitmapDrawable(drawCircle(radius,
                    Color.argb(199, 217, 114, 0)));
            return drawable;
        } else {
            BitmapDrawable drawable = new BitmapDrawable(drawCircle(radius,
                    Color.argb(235, 215, 66, 2)));
            return drawable;
        }*/
    }

    /**
     * 获取聚合网点显示的圆框
     * @param radius
     * @param color
     * @return
     */
    private Bitmap drawCircle(int radius, int color) {
        Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        RectF rectF = new RectF(0, 0, radius * 2, radius * 2);
        paint.setColor(color);
        canvas.drawArc(rectF, 0, 360, true, paint);
        return bitmap;
    }
    // ########### 设置当地图加载完成事件 end ######################
}
