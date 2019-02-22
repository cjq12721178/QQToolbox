package com.cjq.tool.qqtoolbox.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast
import com.cjq.tool.qbox.util.FileUtil
import com.cjq.tool.qqtoolbox.R
import java.io.File
import java.lang.StringBuilder

class TestFileIOActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_file_io)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_create_new_file -> {
                val file = FileUtil.openOrCreate(StringBuilder()
                        .append(Environment.getExternalStorageDirectory())
                        .append(File.separator)
                        .append("QQToolBox")
                        .append(File.separator)
                        .append("Files")
                        .append(File.separator)
                        .append("IO.txt")
                        .toString())
                if (file === null) {
                    SimpleCustomizeToast.show("打开文件失败")
                } else {
                    SimpleCustomizeToast.show("打开文件${file.absolutePath}成功")
                }
            }
        }
    }
}
