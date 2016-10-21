# android-togetherMap

本文我将讲解一下我最近实现的高德地图Marker的聚合功能。在项目开发中需要使用到地图Marker的聚合功能，但是高德地图并没有实现对Marker的聚合功能，所以需要自己实现其聚合功能（这里说一下百度是实现的，为啥高德不做？）下面我将介绍一下具体的实现步骤。

本项目的github地址：<a href="https://github.com/yipianfengye/android-togetherMap">android-togetherMap</a>

本项目的具体实现效果如下：

![image](http://img.blog.csdn.net/20161019092142793)

**（一）集成高德地图API**

- 参考高德地图开放平台文档

高德地图API集成参考地址：<a href="http://lbs.amap.com/">高德开放平台</a>

包括配置Manifest，申请AppId等步骤；


- 添加Layout布局文件

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.amap.api.maps.MapView
        android:id="@+id/map"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</RelativeLayout>
```
这里主要是添加MapView控件，该控件是地图显示控件，也是我们实现地图功能的主要控件。

- 执行Map对象初始化以及各个生命周期方法

```
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
```
主要是在Activity的生命周期方法中添加对MapView的处理...


- 地图页面中添加Marker对象

```
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
```

删除方法中传递的参数是我在客户端写死的集合数据，执行到这里至此我们就实现了一个简单的地图页面，并添加了相应的Marker对象，具体效果如下：

<img src="http://img.blog.csdn.net/20161019092631689" width="300" height="500">


**（二）添加地图缩放聚合功能**

在上面我们已经实现了一个简单的地图显示页面以及添加了几个Marker对象，现在需要实现的效果是当我们扩大地图的显示比例的时候，相邻的Marker会聚合成一个合成的Marker对象，具体的实现效果就是我们一开始展示的效果。

- 重写地图的OnCameraChangeListener

该Listener会在地图移动或者是改变显示比例的时候被回调，这里我们再其回调方法onCameraChangeFinish方法中执行我们的Marker计算，聚合功能，具体代码如下：

```
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
```

好吧，然后我们看一下自定义方法的updateMapMakers方法的实现逻辑：

```
/**
 * 主要用于更改计算聚合Marker对象
 */
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
```

地图缩放操作有两部分，分别为展示普通marker部分和展示聚合marker部分，这里地图显示比例有一个阙值，当我们的显示比例小于这个阙值的时候计算结果显示聚合Marker对象，当显示比例大于这个阙值的时候计算结果显示普通的Marker对象。


**（三）展示聚合Marker操作**

下面开始我们就将执行具体的Marker对象的聚合功能了

```
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
```

我们可以发现具体的Marker聚合操作是在MapTogetherManager类中实现的，所以我们需要看一下MaoTigetherManager的实现逻辑。

```
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
```
可以发现在其中我们调用了assignSingleCluster方法对Marker对象进行计算判断其属于哪个聚合Marker对象，具体assignSingleCluster方法的实现逻辑：

```
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
            // 更新聚合网点个数
            togDotInfo.setDotCount(togDotInfo.getDotCount() + 1);
        } else {
            togDotInfo = new TogDotInfo(point, latLng);
            // 更新聚合网点个数
            togDotInfo.setDotCount(1);
            togDotInfo.addClusterItem(marker);
            togDotInfoMap.put(dotInfo.getDotId() + SystemClock.currentThreadTimeMillis(), togDotInfo);
        }
    }
```

好吧，具体的实现逻辑下放到了getCluster方法中：

```
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
```

这样经过一系列的操作之后我们就生成了聚合Marker对象列表并将其放置到我们的togDotInfoMap对象中，然后我们调用了addTogDotInfoToMap将聚合Marker对象添加到地图页面中。

```
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
```

这样我们最终将聚合的Marker对象绘制到了地图中，这其中绘制聚合Marker对象的样式都是可定制的，这里只是简单的绘制成一个黑色的圆形，也可以扩充一下，比如当数据大于10的时候一个颜色，当数据大于20的时候一个颜色等等，具体的可参考代码。



**总结：**

本文主要是通过自定义的地图Marker的聚合算法，实现了Marker对象的聚合功能，这里定制化内容较多就不做成库了，具体可参考我的：<a href="https://github.com/yipianfengye/android-togetherMap">android-togetherMap</a>。另外也可参考我的博客：<a href="http://blog.csdn.net/qq_23547831/article/details/52063010">快速实现自定义地图聚合操作</a>




<br>另外对github项目，开源项目解析感兴趣的同学可以参考我的：
<br><a href="http://blog.csdn.net/qq_23547831/article/details/50592352"> github项目解析（四）-->动态更改TextView的字体大小</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/51707796"> github项目解析（五）-->Android日志框架</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/51713824"> github项目解析（六）-->自定义实现ButterKnife框架</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/51730472">github项目解析（七）-->防止按钮重复点击</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/51764304">Github项目解析（八）-->Activity启动过程中获取组件宽高的五种方式</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/51821159">Github项目解析（九）-->实现Activity跳转动画的五种方式</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/52037710">Github项目解析（十）-->几行代码快速集成二维码扫描库</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/52121633">Github项目解析（十一）-->一个简单，强大的自定义广告活动弹窗</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/52593674">Github项目解析（十二）-->一个简单的多行文本显示控件</a>
<br><a href="http://blog.csdn.net/qq_23547831/article/details/52593670">Github项目解析（十三）-->使用Kotlin实现UC头条ViewPager左右滑动效果</a>
