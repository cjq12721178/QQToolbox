package com.cjq.tool.qqtoolbox.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter
import com.cjq.tool.qqtoolbox.R
import com.kelin.scrollablepanel.library.PanelAdapter
import kotlinx.android.synthetic.main.activity_test_scrollable_linear_layout.*
import kotlinx.android.synthetic.main.li_scrollable_item.view.*

class TestScrollableLinearLayoutActivity : AppCompatActivity() {

    val adapter = Adapter()
    val values = MutableList(4) { row ->
        MutableList(8) { column ->
            row * 8 + column
        }.toIntArray()
    }
    var running = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_scrollable_linear_layout)

        tv_scroll.setOnTouchListener(object : View.OnTouchListener {

            private var mLastTouchX: Float = 0f

            private var mLastTouchY: Float = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        mLastTouchX = event.x
                        mLastTouchY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (mLastTouchX - event.x).toInt()
                        val absX = Math.abs(deltaX)
                        if (absX > Math.abs(event.y - mLastTouchY)) {
                            //hsv_scroll.smoothScrollBy(deltaX, 0)
                            ll_inner.scrollBy(deltaX, 0)
                            mLastTouchX = event.x
                            mLastTouchY = event.y
//                            if (deltaX > 0) {
//                                adapter.showNextItem(true)
//                                return true
//                            } else if (deltaX < 0) {
//                                adapter.showNextItem(false)
//                                return true
//                            }
                            return true
                        }
                    }
                }
                return true
            }
        })

        //ScrollView(this).smoothScrollBy()
        rv_scrollable_items.layoutManager = LinearLayoutManager(this)
        rv_scrollable_items.isNestedScrollingEnabled = false
//        rv_scrollable_items.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
//
//            private var mLastTouchX: Float = 0f
//            private var mLastTouchY: Float = 0f
//
//            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
//                when (e.actionMasked) {
//                    MotionEvent.ACTION_DOWN -> {
//                        mLastTouchX = e.x
//                        mLastTouchY = e.y
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val deltaX = (e.x - mLastTouchX).toInt()
//                        val absX = Math.abs(deltaX)
//                        if (absX > Math.abs(e.y - mLastTouchY)) {
//                            //hsv_scroll.smoothScrollBy(deltaX, 0)
//                            //requestDisallowInterceptTouchEvent(true)
//                            adapter.notifyItemMove(deltaX)
//                            mLastTouchX = e.x
//                            mLastTouchY = e.y
//                        }
//                    }
//                }
//            }
//
//            override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
//                return true
//            }
//
//            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//            }
//        })
        //rv_scrollable_items.onInterceptTouchEvent()
        rv_scrollable_items.setOnTouchListener(object : View.OnTouchListener {

            private var mLastTouchX: Float = 0f
            private var mLastTouchY: Float = 0f
            private var scrolling = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        mLastTouchX = event.x
                        mLastTouchY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (event.x - mLastTouchX).toInt()
                        val absX = Math.abs(deltaX)
                        if (absX > Math.abs(event.y - mLastTouchY)) {
                            //hsv_scroll.smoothScrollBy(deltaX, 0)
                            //requestDisallowInterceptTouchEvent(true)
                            scrolling = true
                            adapter.notifyItemMove(deltaX)
                            mLastTouchX = event.x
                            mLastTouchY = event.y
//                            if (deltaX > 0) {
//                                adapter.showNextItem(true)
//                                return true
//                            } else if (deltaX < 0) {
//                                adapter.showNextItem(false)
//                                return true
//                            }
                        } else {
                            scrolling = false
                        }
                    }
                }
                return scrolling
            }
        })
        rv_scrollable_items.adapter = adapter
        sp_items.setPanelAdapter(TestPanelAdapter(values))
        //sp_items.notifyDataSetChanged()
        Thread(Runnable {
            while (running) {
                Thread.sleep(500)
                runOnUiThread {
                    values.add(MutableList(8) {
                        values.size * 8 + it
                    }.toIntArray())
                    sp_items.notifyDataSetChanged()
                }
            }
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
    }

    data class MeasurementValue(val timestamp: Int,
                                val m1: Int,
                                val m2: Int,
                                val m3: Int,
                                val m4: Int,
                                val m5: Int,
                                val m6: Int,
                                val m7: Int)

    class Adapter : RecyclerViewBaseAdapter<MeasurementValue>() {

        val MOVE_ITEM = 1
//        var scrollPosition: Int = 0
//            private set
        private val measurementValues = mutableListOf<MeasurementValue>()

        init {
            var arg = 1
            for (i in 0 until 40) {
                measurementValues.add(MeasurementValue(arg, ++arg, ++arg, ++arg, ++arg, ++arg, ++arg, ++arg))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.li_scrollable_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: MeasurementValue, position: Int) {
            holder.itemView.tv_timestamp.text = item.timestamp.toString()
            holder.itemView.tv_m1.text = item.m1.toString()
            holder.itemView.tv_m2.text = item.m2.toString()
            holder.itemView.tv_m3.text = item.m3.toString()
            holder.itemView.tv_m4.text = item.m4.toString()
            holder.itemView.tv_m5.text = item.m5.toString()
            holder.itemView.tv_m6.text = item.m6.toString()
            holder.itemView.tv_m7.text = item.m7.toString()
            //holder.itemView.ll_inner.scrollTo(scrollPosition, 0)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: MeasurementValue, position: Int, payloads: MutableList<Any?>) {
            if (payloads[0] is MovePara) {
                val payload = payloads[0] as MovePara
                if (payload.payload == MOVE_ITEM) {
                    holder.itemView.hsv_scroll.smoothScrollBy(payload.deltaX, 0)
                }
            }
        }

        override fun getItemByPosition(position: Int): MeasurementValue {
            return measurementValues[position]
        }

        override fun getItemCount(): Int {
            return measurementValues.size
        }

        fun notifyItemMove(deltaX: Int) {
            //scrollPosition -= deltaX
            //Log.d(DebugTag.GENERAL_LOG_TAG, "scrollPos: $scrollPosition, deltaX: $deltaX")
            notifyItemRangeChanged(0, itemCount, MovePara(MOVE_ITEM, deltaX))
        }

        data class MovePara(val payload: Int, val deltaX: Int)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        }
    }

    class TestPanelAdapter(val values: MutableList<IntArray>) : PanelAdapter() {

        override fun getRowCount(): Int {
            return values.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.li_item, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, row: Int, column: Int) {
            (holder.itemView as TextView).text = values[row][column].toString()
        }

        override fun getColumnCount(): Int {
            return values[0].size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        }
    }
}
