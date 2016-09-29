package qianfeng.a6_4baidumap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.util.List;

import qianfeng.a6_4baidumap.overlayutil.BusLineOverlay;
import qianfeng.a6_4baidumap.overlayutil.PoiOverlay;
import qianfeng.a6_4baidumap.overlayutil.TransitRouteOverlay;
import qianfeng.a6_4baidumap.overlayutil.WalkingRouteOverlay;


/**
 * 1.在百度地图网站上创建一个应用，http://lbsyun.baidu.com/apiconsole/key/create，根据http://lbsyun.baidu.com/index.php?title=androidsdk/guide/key
 * 页面的指引生成sha1,网站上填写的应用包名要和实际的应用包名一致。
 * 2.将下载的SDK中的\BaiduMap_AndroidSDK_v4.0.0_Sample\BaiduMapsApiASDemo\app\libs文件夹下的文件拷贝到项目中,再创建一个jniLibs文件夹，修改它指向的路径为'libs'
 * 3.根据http://lbsyun.baidu.com/index.php?title=androidsdk/guide/hellobaidumap配置清单文件
 * 4.布局文件中添加地图控件，在Application中初始化地图控件
 * 5.将debug.keystore拷贝到项目app目录下；然后在gradle文件中进行配置
 *
 * 补充 keytool 是你的AS的SDK的lib的bin文件里面，输入的debug.keystore是全路径就可以了
 */

public class MainActivity extends AppCompatActivity {


    private BaiduMap baiduMap;
    private PoiSearch poiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapView mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();

    }

    public void location(View view) {
        // 先在xml里面查找到MapView，就是你xml布局的那个控件，再用mapView的getMap()方法来获取一个百度地图实例 baiduMap, 往后的所有操作都会围绕这个baiduMap来进行
        // 定位(定位需要new一个LocationClient,再用这个定位客户端注册一个定位监听器,registerLocationListener(bdLocation)----> 这个bdLocation可以获取到经纬度信息,通过locationClient.start()开始定位 )
        // 得到经纬度信息之后，就设置baiduMap对象的当前LocationData,使百度地图定位到当前的这个坐标上，baiduMap.setLocationData(new MyLoacationData.Builder().latitude().longitude().build() )
        // 给baiduMap设置动画，切换到当前已经设置好的位置上 baiduMap.animateMapStatus(MapStatus) --> MapStates是通过MapStatusUpdataFactory.newMapStatus(mapStatus)来生成的。
       // 这个mapStatus是通过链式编程来生成的，即 new MapStatus.Builder() 来生成的，注意里面的targer属性，这个属性是设置baiduMap中心要显示的经纬度坐标，
        //  你传进去一个经纬度坐标对象(latLng)即可，latLng = new LatLog(float v,float v1);

        LocationClient locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new BDLocationListener() {
            // 开始定位时，调用这个方法
            @Override
            public void onReceiveLocation(BDLocation bdLocation) { // 注册一个BDLoacation定位监听
                Log.d("google-my:", "onReceiveLocation: " + bdLocation.getLocType() ); // 得到一个返回码，然后上百度地图开发者平台的 '定位SDK'那里查找'开发指南'，然后找到'错误码'，进去那里核对
                if(bdLocation.getLocType() == BDLocation.TypeGpsLocation)
                {
                    Toast.makeText(MainActivity.this,"GPS",Toast.LENGTH_SHORT).show();
                }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation)
                {
                    Toast.makeText(MainActivity.this,"网络定位",Toast.LENGTH_SHORT).show();
                }else if(bdLocation.getLocType() == BDLocation.TypeOffLineLocation)
                {
                    Toast.makeText(MainActivity.this,"离线定位",Toast.LENGTH_SHORT).show();
                }

                // 坐标转化
                CoordinateConverter converter = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                // sourceLatLng待转换坐标
                converter.coord(new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude()));
                LatLng desLatLng = converter.convert();
                showLocation(desLatLng);
            }
        });
        // 开始定位
        locationClient.start();







    }

    private void showLocation(LatLng deslatLng) { // 显示定位到当前的点

//        double latitude = bdLocation.getLatitude(); // 获取定位结果的纬度
//        double longitude = bdLocation.getLongitude(); // 获取定位结果的精度

        // 构造一个MyLocationData(通过经纬度数据)
        MyLocationData myLocationData = new MyLocationData.Builder().latitude(deslatLng.latitude).longitude(deslatLng.longitude).build();
        // 设置百度地图当前的定位坐标，是广州而不是北京
        baiduMap.setMyLocationData(myLocationData);
        // 通过动画让地图滑动到定位结果处
//        LatLng latLng = new LatLng(latitude,longitude);
        MapStatus mapStatus = new MapStatus.Builder()
                .zoom(18f)// 缩放比例 1-21个级别可以选
                // 手机屏幕正中央显示的点
                .target(deslatLng)
                .build();

        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));

        // 覆盖物
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.mark_pic);
        OverlayOptions overlay = new MarkerOptions()
                // 设置覆盖物的图标
                .icon(bitmapDescriptor)
                // 设置覆盖物的位置
                .position(deslatLng);


        baiduMap.addOverlay(overlay);



    }

    /**
     * 兴趣点检索
     * @param view
     */
    public void poiSearch2(View view) {

        poiSearch = PoiSearch.newInstance();

        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            // 第一次点击按钮搜索时调用,正式开始搜索啦！
            @Override
            public void onGetPoiResult(PoiResult poiResult) {

                // 清除地图上已经放置过的覆盖物
                baiduMap.clear();

                MyOverLay overLay = new MyOverLay(baiduMap);

                overLay.addToMap();

                overLay.zoomToSpan();

                // 设置自己定义类的覆盖物
                baiduMap.setOnMarkerClickListener(overLay);

            }

            // 点击上面第一个方法的搜索结果时调用，这是第二次搜索
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

                Log.d("google-my:", "onGetPoiIndoorResult: " + poiDetailResult.getAddress());
                Log.d("google-my:", "onGetPoiIndoorResult: " + poiDetailResult.getName());
                Log.d("google-my:", "onGetPoiIndoorResult: " + poiDetailResult.getDetailUrl());
                Log.d("google-my:", "onGetPoiIndoorResult: " + poiDetailResult.getShopHours());
                Log.d("google-my:", "onGetPoiIndoorResult: " + poiDetailResult.getTelephone());

                // 点击了某个覆盖物之后，放大到该位置上，如果不设置这个，地图是不会放大到你点击的位置的，只会处理点击+搜索事件
                showLocation(poiDetailResult.getLocation());

            }

            // 室内搜索，暂时不作处理
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {




            }
        });

        // 这个方法搜索成功会调用上面监听的第一个方法
        poiSearch.searchInCity(new PoiCitySearchOption().city("广州").keyword("银行")); // 搜索完之后会调用onGetPoiResult(PoiResult poiResult)

        Toast.makeText(MainActivity.this,"兴趣点检索2",Toast.LENGTH_SHORT).show();

    }




    // 兴趣点搜索嘛，当然继承自 兴趣点搜索类 PoiOverlay
    class MyOverLay extends PoiOverlay {

        /**
         * 构造函数
         *
         * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
         */
        public MyOverLay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        // 点击了某一个搜索结果时触发这个方法，i表示点击了第几个搜索结果
        // 我自己定义的自定义覆盖物的点击事件的处理方法(我自定义的覆盖物被点击后，会调用这个方法)
        @Override
        public boolean onPoiClick(int i) { // 监听的第一个方法搜索成功之后，会有覆盖物，如果你点击了覆盖物之后，会调用这个方法
            super.onPoiClick(i);
            // 这里只是监听到覆盖物的点击事件，要搜索，还是得通过这个全局变量poiSearch(兴趣点查询)

          // 这是被点击的覆盖物的position
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(i);

            // 再发起一次详情搜索呗,搜索上面被点击的覆盖物
            poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(poiInfo.uid)); // 这次搜索，poiSearch这个全局变量会调用第二个方法


            return true;
        }
    }



    public void btnClick3(View view) {  // 公交线路查询

        final BusLineSearch busLineSearch = BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(new OnGetBusLineSearchResultListener() {
            @Override
            public void onGetBusLineResult(BusLineResult busLineResult) { // 搜索巴士相当于两层搜索，第一层先是最大范围的搜索，然后查询巴士路线、查询附近酒店银行等，是第二次搜索了(第二次搜索所用的方法和搜索对象不一定相同)！

               // 在搜索到巴士结果之后，使用巴士的覆盖物，就可以在地图上显示巴士的覆盖物路线，这就是巴士路线
                baiduMap.clear(); // clear()是baiduMap的方法，先清除掉百度地图中已有的覆盖物
                BusLineOverlay busLineOverlay = new BusLineOverlay(baiduMap);
                busLineOverlay.setData(busLineResult);  // 设置巴士覆盖物的数据，该摆放在哪里
                busLineOverlay.addToMap();
                busLineOverlay.zoomToSpan();

            }
        });

        // 重新创建一个用于兴趣点搜索的poiSearch对象，一个这个对象只能使用一次，不同的结果就不能使用这个同一个对象了。除非调用destroy()方法，销毁搜索过的信息.
        PoiSearch poiSearch = PoiSearch.newInstance();

        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {// 当poiSearch搜索巴士线路成功时，会调用这个方法，还是要用到poiSearch

                List<PoiInfo> allPoi = poiResult.getAllPoi();
                for(PoiInfo poiInfo : allPoi)
                {
                    if(poiInfo.type == PoiInfo.POITYPE.BUS_LINE) // 如果查询所有和B12有关的，可能是公寓啊公园啊房子什么的，可是我要类型为巴士路线的
                    {
                        // 在这里展开BusLineSearch的搜索，因为兴趣点搜索能搜的范围最大，所以要先进行兴趣点搜索
                        // 在兴趣点搜索能找到结果之后，再看是不是你要搜索的内容，再进一步细分下一步的搜索

                        busLineSearch.searchBusLine(new BusLineSearchOption().city("广州").uid(poiInfo.uid)); // 查询到结果之后，会调用onGetBusLineResult方法
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

        // 这是第一次搜索，是兴趣点搜索，搜索到的结果(会在onGetPoiResult中显示)进行type的过滤，
        // 就可以进行第二次公交路线(busLineSearch.searchBusLine() )的搜索，
        poiSearch.searchInCity(new PoiCitySearchOption().city("广州").keyword("B12")); // 当搜索结果成功时，会调用onGetPoiResult这个方法



    }

    public void btnClick4(View view) { // 线路规划

        RoutePlanSearch routePlanSearch = RoutePlanSearch.newInstance();

        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult result) {

                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    //result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {

                    WalkingRouteOverlay walkingRouteOverlay = new WalkingRouteOverlay(baiduMap);
                    walkingRouteOverlay.setData(result.getRouteLines().get(0));
                    walkingRouteOverlay.addToMap();
                    walkingRouteOverlay.zoomToSpan();

                }

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    //result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    // 注意这里baiduMap还没有clear哦！上一次的覆盖物，在这里是会显示出来的哦！

                    TransitRouteOverlay transitRouteOverlay = new TransitRouteOverlay(baiduMap);
//                    transitRouteOverlay.setData(result.getRouteLines().get(0)); 也可以是这种，只拿到结果集里面的第一条数据
//                    transitRouteOverlay.addToMap();
//                    transitRouteOverlay.zoomToSpan();
                    List<TransitRouteLine> routeLines = result.getRouteLines();
                    for(TransitRouteLine transitRouteLine : routeLines) // 这种是遍历结果集的多条数据,在地图可以显示多条路线规划
                    {
                        transitRouteOverlay.setData(transitRouteLine);
                        transitRouteOverlay.addToMap();
                        transitRouteOverlay.zoomToSpan();
                    }
                }
            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });


        // 百度地图的API目前不支持模糊查询，即两个公交站的站名要明确指定，否则查不到数据
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("广州", "马场路南");
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("广州", "冼村路北");
//        PlanNode stNode = PlanNode.withCityNameAndPlaceName("广州", "上社站");
//        PlanNode enNode = PlanNode.withCityNameAndPlaceName("广州", "天河客运站");

        routePlanSearch.transitSearch(new TransitRoutePlanOption().from(stNode).to(enNode).city("广州"));
        // 如果这里搜索有结果之后，会调用上面onGetTransitRouteResult()

//        routePlanSearch.bikingSearch(new BikingRoutePlanOption().from(stNode).to(enNode));

        Toast.makeText(MainActivity.this,"线路规划",Toast.LENGTH_SHORT).show();

    }

}
