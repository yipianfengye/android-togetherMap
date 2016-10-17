package xiaomi.mich.com.android_togethermap.together;

import android.graphics.Point;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合网点信息列表用于保存聚合网点信息
 */
class TogDotInfo {
	private Point mPoint;// 中心网点
	private LatLng mLatLng;// 中心网点经纬度
	private List<Marker> mClusterItems;// 网点列表
	private int carCount;// 网点可用车辆个数
	private int dotCount;// 网点个数
	private TextView textView;// 聚合网点显示


	TogDotInfo(Point point, LatLng latLng) {
		mPoint = point;
		mLatLng = latLng;
		mClusterItems = new ArrayList<Marker>();
	}

	void addClusterItem(Marker clusterItem) {
		mClusterItems.add(clusterItem);
	}

	Point getCenterPoint() {
		return mPoint;
	}

	LatLng getCenterLatLng() {
		return mLatLng;
	}

	public int getCarCount() {
		return carCount;
	}

	public void setCarCount(int carCount) {
		this.carCount = carCount;
	}

	public int getDotCount() {
		return dotCount;
	}

	public void setDotCount(int dotCount) {
		this.dotCount = dotCount;
	}

	public List<Marker> getmClusterItems() {
		return mClusterItems;
	}

	public TextView getTextView() {
		return textView;
	}

	public void setTextView(TextView textView) {
		this.textView = textView;
	}
}
