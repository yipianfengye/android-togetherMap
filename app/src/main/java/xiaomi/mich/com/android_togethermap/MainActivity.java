package xiaomi.mich.com.android_togethermap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import xiaomi.mich.com.android_togethermap.model.DotInfo;
import xiaomi.mich.com.android_togethermap.utils.DisplayUtil;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private AMap aMap;

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
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        initMarker(DotInfo.initData());
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
     * 初始化marker数据
     */
    private void initMarker(List<DotInfo> dotList) {
        if (dotList == null || dotList.size() == 0) {
            return;
        }

        for (int i = 0; i < dotList.size(); i++) {
            DotInfo dotInfo = dotList.get(i);

            MarkerOptions options = new MarkerOptions();
            options.anchor(0.5f, 1.0f);
            options.position(new LatLng(dotInfo.getDotLat(), dotInfo.getDotLon()));

            setIconToOptions(dotInfo, options);

            Marker marker = aMap.addMarker(options);
            marker.setObject(dotInfo);
            marker.setZIndex(14);
        }
    }

    /**
     * 为marker的Options设置icon
     * @param dotInfo
     * @param options
     */
    private void setIconToOptions(DotInfo dotInfo, MarkerOptions options) {
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        View view = createTextIconByType(dotInfo.getCarTotal() + "", dotInfo, mBitmap);
        options.icon(BitmapDescriptorFactory.fromView(view));
    }


    /**
     * 根据网点的数量以及选中状态返回Marker的样式
     *
     * @param count
     * @param dotInfo
     * @return
     */
    private View createTextIconByType(String count, DotInfo dotInfo, Bitmap mBitmap) {

        View view = LayoutInflater.from(this).inflate(R.layout.map_marker_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_marker);
        TextView textView = (TextView) view.findViewById(R.id.tv_marker_number);
        // 当网点车辆数大于1位时，修改显示网点TextView的宽高
        if (count.length() > 1) {
            int size = DisplayUtil.dip2px(this, 22);
            textView.getLayoutParams().width = size;
            textView.getLayoutParams().height = size;
        }
        textView.setText(count);
        textView.setBackgroundResource(R.drawable.marker_num_nonumber_bg);
        imageView.setImageResource(R.drawable.ic_location_normal);

        return view;
    }
}
