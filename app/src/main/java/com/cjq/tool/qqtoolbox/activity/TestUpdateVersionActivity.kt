package com.cjq.tool.qqtoolbox.activity

import android.app.DownloadManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.cjq.tool.qqtoolbox.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import com.cjq.tool.qqtoolbox.util.DebugTag
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.app.ProgressDialog
import android.content.pm.PackageInfo
import android.os.Environment
import android.support.v7.preference.PreferenceManager
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.lib.weisi.iot.container.ValueContainer
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL


class TestUpdateVersionActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_update_version)
        val sensor = SensorManager.getPhysicalSensor(12)
        val container: ValueContainer<*> = sensor.getMeasurementByPosition(0).historyValueContainer
        container.detachSubValueContainer(container)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_use_url_connection -> {
                update(false)
            }
            R.id.btn_use_download_manager -> {
                update(true)
            }
        }
    }

    private fun update(useDownloadManager: Boolean) {
        checkUpdate("apk", "1.0.0", object : CheckCallBack {
            override fun onSuccess(updateInfo: UpdateAppInfo) {
                val isForce = updateInfo.data!!.lastForce//是否需要强制更新
                val downUrl = updateInfo.data!!.updateurl//apk下载地址
                val updateinfo = updateInfo.data!!.upgradeinfo//apk更新详情
                val appName = updateInfo.data!!.appname
                val serverVersion = updateInfo.data!!.serverVersion
                Log.d(DebugTag.GENERAL_LOG_TAG, "downUrl: $downUrl\nupdateinfo: $updateInfo\nappname: $appName\nserverVersion: $serverVersion\n")
                if (isForce == "1" && !TextUtils.isEmpty(updateinfo)) {//强制更新
                    forceUpdate(this@TestUpdateVersionActivity, appName!!, downUrl!!, updateinfo!!, useDownloadManager)
                } else {//非强制更新
                    //正常升级
                    //normalUpdate(this, appName, downUrl, updateinfo)
                }
            }

            override fun onError() {
                Log.d(DebugTag.GENERAL_LOG_TAG, "check update error")
                //noneUpdate(this@MainActivity)
            }
        })
    }

    private fun forceUpdate(context: Context, appName: String, downUrl: String, updateinfo: String, useDownloadManager: Boolean) {
        AlertDialog.Builder(context)
                .setTitle(appName + "又更新咯！")
                .setMessage(updateinfo)
                .setPositiveButton("立即更新", DialogInterface.OnClickListener { dialog, which ->
                    if (!canDownloadState()) {
                        showDownloadSetting()
                        return@OnClickListener
                    }
                    if (useDownloadManager) {
                        DownLoadApk.download(this@TestUpdateVersionActivity, downUrl, updateinfo, appName);
                    } else {
                        AppInnerDownLoder.downLoadApk(this@TestUpdateVersionActivity, downUrl, appName)
                    }
        }).setCancelable(false).create().show()
    }

    private fun showDownloadSetting() {
        val packageName = "com.android.providers.downloads"
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun canDownloadState(): Boolean {
        try {
            val state = this.packageManager.getApplicationEnabledSetting("com.android.providers.downloads")
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    object AppInnerDownLoder {
        val SD_FOLDER: String = Environment.getExternalStorageDirectory().toString() + "/VersionChecker/"
        private val TAG = AppInnerDownLoder::class.java.simpleName

        /**
         * 从服务器中下载APK
         */
        fun downLoadApk(context: Context, downURL: String, appName: String) {

            val pd: ProgressDialog // 进度条对话框
            pd = ProgressDialog(context)
            pd.setCancelable(false)// 必须一直下载完，不可取消
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            pd.setMessage("正在下载安装包，请稍后")
            pd.setTitle("版本升级")
            pd.show()
            object : Thread() {
                override fun run() {
                    try {
                        val file = downloadFile(downURL, appName, pd)
                        Thread.sleep(3000)
                        installApk(context, file)
                        // 结束掉进度条对话框
                        pd.dismiss()
                    } catch (e: Exception) {
                        pd.dismiss()

                    }

                }
            }.start()
        }

        /**
         * 从服务器下载最新更新文件
         *
         * @param path
         * 下载路径
         * @param pd
         * 进度条
         * @return
         * @throws Exception
         */
        @Throws(Exception::class)
        private fun downloadFile(path: String, appName: String, pd: ProgressDialog): File {
            // 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
            if (Environment.MEDIA_MOUNTED.equals(Environment
                            .getExternalStorageState())) {
                val url = URL(path)
                val conn = url.openConnection() as HttpURLConnection
                conn.setConnectTimeout(5000)
                // 获取到文件的大小
                pd.setMax(conn.getContentLength())
                val `is` = conn.getInputStream()
                val fileName = (SD_FOLDER
                        + appName + ".apk")
                val file = File(fileName)
                // 目录不存在创建目录
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs()
                val fos = FileOutputStream(file)
                val bis = BufferedInputStream(`is`)
                val buffer = ByteArray(1024)
                var len: Int
                var total = 0
                while (true) {
                    len = bis.read(buffer)
                    if (len == -1) {
                        break
                    }
                    fos.write(buffer, 0, len)
                    total += len
                    // 获取当前下载量
                    pd.setProgress(total)
                }
                fos.close()
                bis.close()
                `is`.close()
                return file
            } else {
                throw IOException("未发现有SD卡")
            }
        }

        /**
         * 安装apk
         */
        private fun installApk(mContext: Context, file: File) {
            val fileUri = Uri.fromFile(file)
            val it = Intent()
            it.action = Intent.ACTION_VIEW
            it.setDataAndType(fileUri, "application/vnd.android.package-archive")
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)// 防止打不开应用
            mContext.startActivity(it)
        }

        /**
         * 获取应用程序版本（versionName）
         *
         * @return 当前应用的版本号
         */

        private fun getLocalVersion(context: Context): Double {
            val manager = context.packageManager
            var info: PackageInfo? = null
            try {
                info = manager.getPackageInfo(context.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "获取应用程序版本失败，原因：" + e.message)
                return 0.0
            }

            return java.lang.Double.valueOf(info!!.versionName)
        }

        /**
         * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
         *
         * @param bytes
         * @return
         */
        fun bytes2kb(bytes: Long): String {
            val filesize = BigDecimal(bytes)
            val megabyte = BigDecimal(1024 * 1024)
            var returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP).toFloat()
                    //.floatValue()
            if (returnValue > 1)
                return returnValue.toString() + "MB"
            val kilobyte = BigDecimal(1024)
            returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP).toFloat()
                    //.floatValue()
            return returnValue.toString() + "KB"
        }
    }

    class FileDownloadManager private constructor(context: Context) {
        val downloadManager: DownloadManager
        private val context: Context

        init {
            downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            this.context = context.applicationContext
        }

        /**
         * @param uri
         * @param title
         * @param description
         * @return download id
         */
        fun startDownload(uri: String, title: String, description: String, appName: String): Long {
            val req = DownloadManager.Request(Uri.parse(uri))
            req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            //req.setAllowedOverRoaming(false);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            //设置文件的保存的位置[三种方式]
            //第一种
            //file:///storage/emulated/0/Android/data/your-package/files/Download/update.apk
            //req.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "$appName.apk")
            //第二种
            //file:///storage/emulated/0/Download/update.apk
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk");
            //第三种 自定义文件路径
            //req.setDestinationUri()


            // 设置一些基本显示信息
            req.setTitle(title)
            req.setDescription(description)
            //req.setMimeType("application/vnd.android.package-archive");
            return downloadManager.enqueue(req)//异步
            //dm.openDownloadedFile()
        }

        /**
         * 获取文件保存的路径
         *
         * @param downloadId an ID for the download, unique across the system.
         * This ID is used to make future calls related to this download.
         * @return file path
         * @see FileDownloadManager.getDownloadUri
         */
        fun getDownloadPath(downloadId: Long): String? {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val c = downloadManager.query(query)
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        return c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    }
                } finally {
                    c.close()
                }
            }
            return null
        }

        /**
         * 获取保存文件的地址
         *
         * @param downloadId an ID for the download, unique across the system.
         * This ID is used to make future calls related to this download.
         * @see FileDownloadManager.getDownloadPath
         */
        fun getDownloadUri(downloadId: Long): Uri {
            return downloadManager.getUriForDownloadedFile(downloadId)
        }

        /**
         * 获取下载状态
         *
         * @param downloadId an ID for the download, unique across the system.
         * This ID is used to make future calls related to this download.
         * @return int
         * @see DownloadManager.STATUS_PENDING
         *
         * @see DownloadManager.STATUS_PAUSED
         *
         * @see DownloadManager.STATUS_RUNNING
         *
         * @see DownloadManager.STATUS_SUCCESSFUL
         *
         * @see DownloadManager.STATUS_FAILED
         */
        fun getDownloadStatus(downloadId: Long): Int {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val c = downloadManager.query(query)
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    }
                } finally {
                    c.close()
                }
            }
            return -1
        }

        companion object {
            private var instance: FileDownloadManager? = null

            fun getInstance(context: Context): FileDownloadManager {
                if (instance == null) {
                    instance = FileDownloadManager(context)
                }
                return instance!!
            }
        }
    }

    object DownLoadApk {
        val TAG = DownLoadApk::class.java.simpleName

        fun download(context: Context, url: String, title: String, appName: String) {
            // 获取存储ID
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val downloadId = sp.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (downloadId != -1L) {
                val fdm = FileDownloadManager.getInstance(context)
                val status = fdm.getDownloadStatus(downloadId)
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    //启动更新界面
                    val uri = fdm.getDownloadUri(downloadId)
                    if (uri != null) {
                        if (compare(getApkInfo(context, uri.path), context)) {
                            startInstall(context, uri)
                            return
                        } else {
                            fdm.downloadManager.remove(downloadId)
                        }
                    }
                    start(context, url, title, appName)
                } else if (status == DownloadManager.STATUS_FAILED) {
                    start(context, url, title, appName)
                } else {
                    Log.d(TAG, "apk is already downloading")
                }
            } else {
                start(context, url, title, appName)
            }
        }

        private fun start(context: Context, url: String, title: String, appName: String) {
            val id = FileDownloadManager.getInstance(context).startDownload(url,
                    title, "下载完成后点击打开", appName)
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            sp.edit().putLong(DownloadManager.EXTRA_DOWNLOAD_ID, id).commit()
            Log.d(TAG, "apk start download $id")
        }

        fun startInstall(context: Context, uri: Uri) {
            val install = Intent(Intent.ACTION_VIEW)
            install.setDataAndType(uri, "application/vnd.android.package-archive")
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(install)
        }


        /**
         * 获取apk程序信息[packageName,versionName...]
         *
         * @param context Context
         * @param path    apk path
         */
        private fun getApkInfo(context: Context, path: String): PackageInfo? {
            val pm = context.packageManager
            return pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
        }


        /**
         * 下载的apk和当前程序版本比较
         *
         * @param apkInfo apk file's packageInfo
         * @param context Context
         * @return 如果当前应用版本小于apk的版本则返回true
         */
        private fun compare(apkInfo: PackageInfo?, context: Context): Boolean {
            if (apkInfo == null) {
                return false
            }
            val localPackage = context.packageName
            if (apkInfo.packageName.equals(localPackage)) {
                try {
                    val packageInfo = context.packageManager.getPackageInfo(localPackage, 0)
                    if (apkInfo.versionCode > packageInfo.versionCode) {
                        return true
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

            }
            return false
        }
    }

    class UpdateAppInfo {
        var data: UpdateInfo? = null // 信息
        var error_code: Int? = null // 错误代码
        var error_msg: String? = null // 错误信息

        class UpdateInfo {
            // app名字
            var appname: String? = null
            //服务器版本
            var serverVersion: String? = null
            //服务器标志
            var serverFlag: String? = null
            //强制升级
            var lastForce: String? = null
            //app最新版本地址
            var updateurl: String? = null
            //升级信息
            var upgradeinfo: String? = null
        }
    }

    interface ApiService {
        //实际开发过程可能的接口方式
        @GET("update")
        fun getUpdateInfo(@Query("appname") appname: String, @Query("serverVersion") appVersion: String): Observable<UpdateAppInfo>

        //以下方便版本更新接口测试
        @GET("update")
        fun getUpdateInfo(): Observable<UpdateAppInfo>
    }

    object ServiceFactory {
        private val BASEURL = "http://192.168.1.54:21524/"
        fun <T> createServiceFrom(serviceClass: Class<T>): T {
            val adapter = Retrofit.Builder()
                    .baseUrl(BASEURL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 添加Rx适配器
                    .addConverterFactory(GsonConverterFactory.create()) // 添加Gson转换器
                    .build()
            return adapter.create(serviceClass)
        }
    }

    /**
     * 检查更新
     */
    fun checkUpdate(appCode: String, curVersion: String, updateCallback: CheckCallBack) {
        val apiService = ServiceFactory.createServiceFrom(ApiService::class.java)
        apiService.getUpdateInfo()//测试使用
                //.apiService.getUpdateInfo(appCode, curVersion)//开发过程中可能使用的
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe() {
                    if (it.error_code == 0 || it.data?.updateurl == null) {
                        updateCallback.onError(); // 失败
                    } else {
                        updateCallback.onSuccess(it);
                    }
                }
    }

    interface CheckCallBack {
        //检测成功或者失败的相关接口
        fun onSuccess(updateInfo: UpdateAppInfo)

        fun onError()
    }
}
