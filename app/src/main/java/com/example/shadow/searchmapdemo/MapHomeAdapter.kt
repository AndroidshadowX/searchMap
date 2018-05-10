package com.example.shadow.searchmapdemo

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.util.ArrayList

class MapHomeAdapter(layout: Int, list: ArrayList<PoiBean>) : BaseQuickAdapter<PoiBean, BaseViewHolder>(layout, list) {
    override fun convert(helper: BaseViewHolder?, item: PoiBean?) {
        helper!!.setText(R.id.tv_title, item!!.titleName)
        helper!!.setText(R.id.tv_address, item.getCityName() + item.getAd() + item.getSnippet())
        if (item.isSelected) {
            helper!!.setVisible(R.id.iv_checked, true)
        } else {
            helper!!.setVisible(R.id.iv_checked, false)
        }
    }
}