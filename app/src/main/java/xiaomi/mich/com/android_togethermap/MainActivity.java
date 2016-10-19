package xiaomi.mich.com.android_togethermap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import xiaomi.mich.com.android_togethermap.model.DotInfo;
import xiaomi.mich.com.android_togethermap.together.MapTogetherManager;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private MapView mapView;
    private AMap aMap;
    /**
     * 地图初始化比例尺,地图比例尺
     */
    public static float ORGZOON = 10;
    /**
     * 数据列表
     */
    private List<DotInfo> dotList = new ArrayList<>();
    /**
     * marker数据集合
     */
    public Map<String, Marker> markerMap = new ConcurrentHashMap<>();

    public static final int MARKER_NORMA = 1;
    public static final int MARKER_TOGE = 2;
    public static int markerStatus = MARKER_NORMA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        aMap.setOnCameraChangeListener(onCameraChangeListener);

        dotList.clear();
        dotList = DotInfo.initData();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        updateNormalMarkers();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    /**
     * 设置地图移动监听
     */
    private AMap.OnCameraChangeListener onCameraChangeListener = new AMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
        }

        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            // 放大缩小完成后对聚合点进行重新计算
            updateMapMarkers();
        }
    };

    private synchronized void updateMapMarkers() {
        if (dotList != null && dotList.size() > 0) {
            Log.i(TAG, "地图级别:" + aMap.getCameraPosition().zoom);
            // 若当前地图级别小于初始化比例尺,则显示聚合网点
            if (aMap.getCameraPosition().zoom < ORGZOON) {
                 markerStatus = MARKER_TOGE;
                 updateTogMarkers();
            }
            // 显示普通marker
            else {
                if (markerStatus == MARKER_TOGE) {
                    markerStatus = MARKER_NORMA;
                    updateNormalMarkers();
                }
            }

            System.gc();
        }
    }


    /**
     * 更新普通网点数据
     */
    private void updateNormalMarkers() {
        // 判断上一次更新marker操作的操作类型,若上次显示的是聚合网点,则先清空地图,然后清空网点信息,在刷新地图marker
        aMap.clear();
        markerMap.clear();

        loadMarker(DotInfo.initData());
    }


    /**
     * 更新聚合网点
     */
    private void updateTogMarkers() {

        Log.i(TAG, "开始显示聚合网点,清空地图normal marker...");
        aMap.clear();
        // 更新聚合marker
        MapTogetherManager.getInstance(this, aMap).onMapLoadedUpdateMarker(markerMap);

        // 设置marker点击事件,若是聚合网点此时点击marker则放大地图显示正常网点
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 初始化地图按指定的比例尺移动到定位的坐标
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(marker.getPosition(), ORGZOON, 3, 0)), 1000, null);
                return true;
            }
        });
    }

    /**
     * 初始化marker数据
     */
    private void loadMarker(List<DotInfo> dotList) {
        if (dotList == null || dotList.size() == 0) {
            return;
        }

        for (int i = 0; i < dotList.size(); i++) {
            DotInfo dotInfo = dotList.get(i);

            MarkerOptions options = new MarkerOptions();
            options.anchor(0.5f, 1.0f);
            options.position(new LatLng(dotInfo.getDotLat(), dotInfo.getDotLon()));

            setIconToOptions(options);

            Marker marker = aMap.addMarker(options);
            marker.setObject(dotInfo);
            marker.setZIndex(ORGZOON);

            markerMap.put(dotInfo.getDotId(), marker);
        }
    }

    /**
     * 为marker的Options设置icon
     * @param options
     */
    private void setIconToOptions(MarkerOptions options) {

        View view = LayoutInflater.from(this).inflate(R.layout.map_marker_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_marker);
        TextView textView = (TextView) view.findViewById(R.id.tv_marker_number);
        textView.setText("1");
        textView.setBackgroundResource(R.drawable.marker_num_nonumber_bg);
        imageView.setImageResource(R.mipmap.ic_launcher);

        options.icon(BitmapDescriptorFactory.fromView(view));
    }
}
