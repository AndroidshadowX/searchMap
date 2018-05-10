package com.example.shadow.searchmapdemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.LocationSource
import com.amap.api.maps2d.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),
        LocationSource,
        AMapLocationListener,
        AMap.OnCameraChangeListener,
        PoiSearch.OnPoiSearchListener {
    private val REQUEST_SEARCH_CODE = 1
    private var aMap: AMap? = null
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var query: PoiSearch.Query? = null// Poi查询条件类
    private var currentPage = 1// 当前页面，从0开始计数
    private var lp = LatLonPoint(39.993167, 116.473274)//
    private var city = ""
    private var poiSearch: PoiSearch? = null
    private var adapter: MapHomeAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        map.onCreate(savedInstanceState)
        init()
        Log.d("TAG", SignUtil.getAppSign(this))
        map_search.setOnClickListener {
            openSearch()
        }
    }

    private fun init() {
        if (aMap == null) {
            aMap = map.map
            setUpMap()
        }
    }

    fun openSearch() {
        val intent = Intent(this, MapSearchAddActivity::class.java)
        intent.putExtra("city", city)
        startActivityForResult(intent, REQUEST_SEARCH_CODE)
    }

    private fun setUpMap() {
        var myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.location_marker))// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK)// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 130, 202, 226))// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(1.0f)// 设置圆形的边框粗细
        aMap!!.setMyLocationStyle(myLocationStyle)
        aMap!!.setLocationSource(this)// 设置定位监听
        aMap!!.getUiSettings().setMyLocationButtonEnabled(true)// 设置默认定位按钮是否显示
        aMap!!.setMyLocationEnabled(true)// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap!!.setOnCameraChangeListener(this)//移动地图事件监听器
    }

    //保存地址
    fun saveMap() {
        aMap!!.getMapScreenShot { bitmap ->
            var sdf = SimpleDateFormat("yyyyMMddHHmmss")
            var pathFolder = Environment.getExternalStorageDirectory().toString() + "MapAdd"
            var savePath = pathFolder + "t_" + sdf.format(Date()) + ".png"
            if (null == bitmap)
                return@getMapScreenShot
            try {
                val file = File(pathFolder)
                if (!file.exists()) {
                    file.mkdirs()
                }
                val fos = FileOutputStream(File(savePath))
                val b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                try {
                    fos.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val buffer = StringBuffer()
                if (b) {
                    buffer.append("截屏成功 ")
                    Log.e("yufs", "截图完成。。。")

                    //                        //上传图片到七牛
                    //                        imageBytes = ImageUtils.bmpToByteArray(bitmap, false);
                    //                        //获取七牛token
                    //                        getQiniuToken();

//                    hud = KProgressHUD.create(this@MapActivity)
//                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
//                            .setLabel("定位数据上传中...")
//                            .setMaxProgress(100)
//                            .show()
//                    hud.setProgress(90)
//                    PhotoUpload(this@MapActivity).QiNiu(0, savePath, hud)
                } else {
                    buffer.append("截屏失败 ")
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 停止定位
     */
    override fun deactivate() {
        mListener = null
        if (mlocationClient != null) {
            mlocationClient!!.stopLocation()
            mlocationClient!!.onDestroy()
        }
        mlocationClient = null
    }

    /**
     * 激活定位
     */
    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener
        if (mlocationClient == null) {
            mlocationClient = AMapLocationClient(this)
            mLocationOption = AMapLocationClientOption()
            mlocationClient!!.setLocationListener(this)
            mLocationOption!!.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
            mlocationClient!!.setLocationOption(mLocationOption)
            mlocationClient!!.startLocation()
        }
    }

    /**
     * 定位成功后回调函数
     */
    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.errorCode == 0) {
                city = amapLocation.city
                lp = LatLonPoint(amapLocation.latitude, amapLocation.longitude)
                mlocationClient!!.stopLocation()
                currentPage = 1
                doSearchQuery()//开启周边搜索
                aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(amapLocation.latitude, amapLocation.longitude), 14f))
                mListener!!.onLocationChanged(amapLocation)
            } else {
                Toast.makeText(this, "定位失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 对正在移动地图事件回调
     */
    override fun onCameraChange(cameraPosition: CameraPosition?) {

    }

    /**
     * 对移动地图结束事件回调
     */
    override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {
        lp = LatLonPoint(cameraPosition!!.target.latitude, cameraPosition!!.target.longitude)
        currentPage = 1
        doSearchQuery()//开启周边搜索
        LogUtils.a(Gson().toJson(cameraPosition))
    }

    /**
     * 开始进行poi搜索
     */
    fun doSearchQuery() {
        val mType = "汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施"
        query = PoiSearch.Query("", mType, "")
        query!!.pageSize = 20
        LogUtils.a("Tag", currentPage)
        query!!.pageNum = currentPage
        if (lp != null) {
            poiSearch = PoiSearch(this, query)
            poiSearch!!.setOnPoiSearchListener(this)
            poiSearch!!.bound = PoiSearch.SearchBound(lp, 1000, true)
            poiSearch!!.searchPOIAsyn()
        }
    }

    private var locationMarker: Marker? = null // 选择的点
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SEARCH_CODE && resultCode == 6) {
            var searchPonItem = data!!.getParcelableExtra<LatLonPoint>("poiItem")
            LogUtils.a("TAG", Gson().toJson(searchPonItem))
            lp = LatLonPoint(searchPonItem.latitude, searchPonItem.longitude)
            locationMarker = aMap!!.addMarker(MarkerOptions()
                    .position(LatLng(searchPonItem.latitude, searchPonItem.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(resources, R.mipmap.data_yuan_off)))
                    .draggable(true))
            locationMarker!!.setPosition(LatLng(searchPonItem.latitude, searchPonItem.longitude))
            aMap!!.invalidate()
            aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(searchPonItem.latitude, searchPonItem.longitude), 14f))
            currentPage = 1
            doSearchQuery()//开启周边搜索
        }
    }

    override fun onPoiItemSearched(result: PoiItem?, rcode: Int) {
//        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
//            if (result != null) {
//                LogUtils.a(Gson().toJson(result))
//            }
//        }
    }

    /**
     * poi搜索结果
     */
    override fun onPoiSearched(result: PoiResult?, rcode: Int) {
        if (rcode == 1000) {
            if (result != null && result.query != null) {
                val poiItems = result.pois
                val tem = ArrayList<PoiBean>()
                for (i in poiItems.indices) {
                    var bean = PoiBean()
                    bean.setTitleName(poiItems.get(i).getTitle())
                    bean.setCityName(poiItems.get(i).getCityName())
                    bean.setAd(poiItems.get(i).getAdName())
                    bean.setSnippet(poiItems.get(i).getSnippet())
                    bean.setPoint(poiItems.get(i).getLatLonPoint())
                    tem.add(bean)
                }
                if (currentPage == 1) {
                    adapter = MapHomeAdapter(R.layout.item_select_address, tem)
                    val manager = LinearLayoutManager(this)
                    manager.orientation = LinearLayoutManager.VERTICAL
                    map_list.setLayoutManager(manager)
                    map_list.adapter = adapter
                } else {
                    adapter!!.addData(tem)
                }
                adapter!!.setOnLoadMoreListener(object : BaseQuickAdapter.RequestLoadMoreListener {
                    override fun onLoadMoreRequested() {
                        currentPage = currentPage + 1
                        doSearchQuery()//开启周边搜索
                    }
                }, map_list)
                if (tem != null)
                    adapter!!.disableLoadMoreIfNotFullPage()
                adapter!!.setPreLoadNumber(2)
                LogUtils.a(Gson().toJson(tem))
                adapter!!.setOnItemClickListener { adapters, view, position ->
                    saveMap()
                    ToastUtils.showShort("${adapter!!.data.get(position).titleName}")
                }
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        map.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        map.onDestroy()
    }

}
