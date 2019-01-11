package com.cjq.tool.qqtoolbox.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.tool.qqtoolbox.R
import com.cleveroad.adaptivetablelayout.LinkedAdaptiveTableAdapter
import com.cleveroad.adaptivetablelayout.ViewHolderImpl
import kotlinx.android.synthetic.main.activity_test_adaptive_table_layout.*
import java.util.*

class TestAdaptiveTableLayoutActivity : AppCompatActivity(), View.OnClickListener {

    val values = MutableList(4) { row ->
        MutableList(8) { column ->
            row * 8 + column
        }.toIntArray()
    }
    var running = true
    val adapter = Adapter(values)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_adaptive_table_layout)

        atl_table.isHeaderFixed = true
        atl_table.setAdapter(adapter)
        //atl_table.notifyColumnChanged()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_change_cell -> {
                val random = Random()
                val row = random.nextInt(values.size)
                val column = random.nextInt(values[0].size)
                values[row][column] = random.nextInt(10000000)
                atl_table.notifyItemChanged(row, column)
            }
            R.id.btn_change_row -> {
                val random = Random()
                val row = random.nextInt(values.size)
                values[row] = MutableList(8) {
                    random.nextInt(10000000)
                }.toIntArray()
                atl_table.notifyRowChanged(row)
            }
            R.id.btn_change_column -> {
                val random = Random()
                val column = random.nextInt(values[0].size)
                values.forEach {
                    it[column] = random.nextInt(10000000)
                }
                atl_table.notifyColumnChanged(column)
            }
            R.id.btn_change_all -> {
                val random = Random()
                values.addAll(MutableList(random.nextInt(values.size)) { row ->
                    MutableList(8) { column ->
                        random.nextInt(10000000)
                    }.toIntArray()
                })
                atl_table.notifyDataSetChanged()
            }
        }
    }

    class Adapter(val values: MutableList<IntArray>) : LinkedAdaptiveTableAdapter<Adapter.TestViewHolder>() {

        private val columnHeaderLabels = arrayOf("m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8")
        override fun onCreateItemViewHolder(parent: ViewGroup): TestViewHolder {
            return createViewHolder(parent)
        }

        private fun createViewHolder(parent: ViewGroup): TestViewHolder {
            return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.li_item, parent, false))
        }

        override fun onBindLeftTopHeaderViewHolder(viewHolder: TestViewHolder) {
            (viewHolder.itemView as TextView).text = "时间/测量量"
        }

        override fun onCreateColumnHeaderViewHolder(parent: ViewGroup): TestViewHolder {
            return createViewHolder(parent)
        }

        override fun onBindHeaderColumnViewHolder(viewHolder: TestViewHolder, column: Int) {
            (viewHolder.itemView as TextView).text = columnHeaderLabels[column]
        }

        override fun onCreateRowHeaderViewHolder(parent: ViewGroup): TestViewHolder {
            return createViewHolder(parent)
        }

        override fun getHeaderRowWidth(): Int {
            return 200
        }

        override fun onBindHeaderRowViewHolder(viewHolder: TestViewHolder, row: Int) {
            (viewHolder.itemView as TextView).text = "row header ${values[row][0]}"
        }

        override fun getRowCount(): Int {
            return values.size
        }

        override fun onCreateLeftTopHeaderViewHolder(parent: ViewGroup): TestViewHolder {
            return createViewHolder(parent)
        }

        override fun onBindViewHolder(viewHolder: TestViewHolder, row: Int, column: Int) {
            (viewHolder.itemView as TextView).text = "value ${values[row][column]}"
        }

        override fun getRowHeight(row: Int): Int {
            return 50
        }

        override fun getColumnWidth(column: Int): Int {
            return 100
        }

        override fun getHeaderColumnHeight(): Int {
            return 100
        }

        override fun getColumnCount(): Int {
            return values[0].size
        }

        class TestViewHolder(itemView: View) : ViewHolderImpl(itemView) {

        }
    }
}
