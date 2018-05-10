package com.example.shadow.searchmapdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_map_search_add.*
import java.util.ArrayList

class MapSearchAddActivity : Activity(), PoiSearch.OnPoiSearchListener {
    var currentPage = 1
    var city = ""
    private var query: PoiSearch.Query? = null// Poi查询条件类
    private val lp = LatLonPoint(39.993167, 116.473274)//
    private var poiSearch: PoiSearch? = null
    private var adapter: MapHomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_search_add)
        var intent = intent
        city = intent.getStringExtra("city")
        tv_title_right.setOnClickListener {
            doSearchQuery(et_search.text.toString())
        }


    }

    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {

    }

    override fun onPoiSearched(result: PoiResult?, rcode: Int) {
        if (rcode == 1000) {
            LogUtils.a("TAG", Gson().toJson(result))
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
                    search_list.setLayoutManager(manager)
                    search_list.adapter = adapter
                } else {
                    adapter!!.addData(tem)
                }
                adapter!!.setOnLoadMoreListener(object : BaseQuickAdapter.RequestLoadMoreListener {
                    override fun onLoadMoreRequested() {
                        currentPage = currentPage + 1
                        doSearchQuery(et_search.text.toString())//开启周边搜索
                    }
                }, search_list)
                adapter!!.notifyDataSetChanged()
                if (tem != null)
                    adapter!!.disableLoadMoreIfNotFullPage()
                adapter!!.setPreLoadNumber(2)
                LogUtils.a(Gson().toJson(tem))

                adapter!!.setOnItemClickListener { adapters, view, position ->
                    val LatLonPoint = adapter!!.data.get(position).getPoint()
                    val intent = Intent()
                    intent.putExtra("poiItem", LatLonPoint)
                    setResult(6, intent)
                    finish()
                }
            }
        }
    }

    /**
     * 开始进行poi搜索
     */
    fun doSearchQuery(toString: String) {
        val mType = "汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施"
        query = PoiSearch.Query(toString, "", city)
        query!!.pageSize = 20
        LogUtils.a("Tag", currentPage)
        query!!.pageNum = currentPage
        if (lp != null) {
            poiSearch = PoiSearch(this, query)
            poiSearch!!.setOnPoiSearchListener(this)
//            poiSearch!!.bound = PoiSearch.SearchBound(lp, 5000, true)
            poiSearch!!.searchPOIAsyn()
        }
    }

}