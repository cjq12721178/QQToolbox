package com.cjq.tool.qqtoolbox.activity;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cjq.lib.weisi.communicator.receiver.DataReceiver;
import com.cjq.lib.weisi.communicator.SerialPortKit;
import com.cjq.lib.weisi.data.FilterCollection;
import com.cjq.lib.weisi.data.Storage;
import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.LogicalSensor;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.lib.weisi.iot.Warner;
import com.cjq.lib.weisi.protocol.UdpSensorProtocol;
import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.cjq.tool.qbox.ui.dialog.EditDialog;
import com.cjq.tool.qbox.ui.dialog.FilterDialog;
import com.cjq.tool.qbox.ui.dialog.ListDialog;
import com.cjq.tool.qbox.ui.dialog.SearchDialog;
import com.cjq.tool.qbox.ui.dialog.SortDialog;
import com.cjq.tool.qbox.ui.manager.SwitchableFragmentManager;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qbox.ui.view.SizeSelfAdaptionTextView;
import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.cjq.lib.weisi.util.NumericConverter;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.bean.filter.BleProtocolFilter;
import com.cjq.tool.qqtoolbox.bean.filter.SensorUseForRealTimeFilter;
import com.cjq.tool.qqtoolbox.bean.sorter.SensorAddressSorter;
import com.cjq.tool.qqtoolbox.fragment.NoTitleConstraintLayoutDialog;
import com.cjq.tool.qqtoolbox.fragment.NoTitleLinearLayoutDialog;
import com.cjq.tool.qqtoolbox.fragment.NoTitleRelativeLayoutDialog;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment1;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment2;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment3;
import com.cjq.tool.qqtoolbox.util.CrashHandler;

import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.cjq.tool.qqtoolbox.util.DebugTag.GENERAL_LOG_TAG;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        SortDialog.OnSortTypeChangedListener,
        FilterDialog.OnFilterChangeListener,
        ListDialog.OnMultipleItemSelectedListener,
        CompoundButton.OnCheckedChangeListener,
        TextView.OnEditorActionListener,
        DataReceiver.Listener,
        SearchDialog.OnSearchListener,
        Storage.ElementsProvider<PhysicalSensor> {

    private static final int RC_WRITE_EXTERNAL_STORAGE = 1;
    private SwitchableFragmentManager mSwitchableFragmentManager;
    private String[] mFragmentTags = new String[] {"visual1", "visual2", "visual3"};
    private SizeSelfAdaptionTextView mSizeSelfAdaptionTextView;
    private EditText mEtSetText;
    private SortDialog mSortDialog;
    private SerialPortKit mSerialPortKit;
    private DataReceiver mSerialPortDataReceiver;
    private EditText mEtSerialPortName;
    private TextView mTvReception;
    private CheckBox mChkHexEmission;
    private CheckBox mChkHexReception;
    private Spinner mSpnBaudRate;
    private TextView mTvEmission;
    private String mEmissionTextCopy;
    private String mReceptionTextCopy;
    private FilterDialog mFilterDialog;
    private SearchDialog mSearchDialog;
    //private FilterCollection<PhysicalSensor> mFilterCollection;
    //private Storage<PhysicalSensor> mPhysicalSensorStorage;
    //private Person mCjq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExceptionLog.initialize(this, "QQToolBox");
        ClosableLog.setEnablePrint(true);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        mSizeSelfAdaptionTextView = (SizeSelfAdaptionTextView) findViewById(R.id.tv_fix_size);
        mEtSetText = (EditText) findViewById(R.id.et_set_text);

        mEtSerialPortName = (EditText) findViewById(R.id.et_serial_port_name);
        mTvEmission = (TextView) findViewById(R.id.tv_emission_content);
        mTvReception = (TextView) findViewById(R.id.tv_reception_content);
        mChkHexEmission = (CheckBox) findViewById(R.id.chk_hex_emission);
        mChkHexEmission.setOnCheckedChangeListener(this);
        mChkHexReception = (CheckBox) findViewById(R.id.chk_hex_reception);
        mChkHexReception.setOnCheckedChangeListener(this);
        mSpnBaudRate = (Spinner) findViewById(R.id.spn_baud_rate);
        mSpnBaudRate.setSelection(16);
        EditText etEmission = (EditText) findViewById(R.id.et_emission);
        etEmission.setOnEditorActionListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelable("filters", mFilterCollection);
        //outState.putParcelable("storage", mPhysicalSensorStorage);
        //outState.putParcelable("person", mCjq);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSerialPort();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm_default_overall_decorator: {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setTitle("use default overall decorator");
                dialog.setDrawCancelButton(false);
                //dialog.setContent("this is content");
                //ConfirmDialog.Decorator decorator = dialog.getCustomDecorator();
                //decorator.setDrawSeparationLine(false);
                dialog.getCustomDecorator().setDrawSeparationLine(true);
                dialog.show(getSupportFragmentManager(),
                        "test_confirm");
            } break;
            case R.id.btn_confirm_new_overall_decorator:
                ConfirmDialog.Decorator decorator = ConfirmDialog.getOverallDecorator(ConfirmDialog.class);
                decorator.reset();
                decorator.setTitleTextSize(R.dimen.size_text_title_large);
                decorator.setOkLabel(R.string.hao);
                decorator.setExitButtonTextColor(R.color.colorAccent);
                decorator.setExitButtonTextSize(R.dimen.size_text_title_large);
                ConfirmDialog dialog1 = new ConfirmDialog();
                dialog1.setTitle("use new overall decorator");
                dialog1.show(getSupportFragmentManager(),
                        "test_confirm_new_overall");
                break;
            case R.id.btn_confirm_custom_decorator:
                ConfirmDialog dialog2 = new ConfirmDialog();
                ConfirmDialog.Decorator decorator1 = dialog2.getCustomDecorator();
                decorator1.setTitleLayoutRes(R.layout.group_dialog_title);
                decorator1.setTitleId(R.id.tv_custom_title);
                decorator1.setTitleTextSize(R.dimen.size_text_activity);
                decorator1.setOkCancelLayout(R.layout.group_ok_cancel_custom);
                decorator1.setOkId(R.id.btn_ok_custom);
                decorator1.setCancelId(R.id.btn_cancel_custom);
                decorator1.setBasePadding(R.dimen.dialog_base_padding_left,
                        R.dimen.dialog_base_padding_top,
                        0,
                        0);
                dialog2.setTitle("use custom decorator");
                dialog2.show(getSupportFragmentManager(),
                        "test_confirm_custom_decorator");
                break;
            case R.id.btn_edit_use_default_overall_decorator:
                EditDialog editDialog = new EditDialog();
                editDialog.setTitle("use default overall decorator \n1\n2\n3\n4\n5");
                editDialog.setContent("yaya");
                //editDialog.getCustomDecorator().setDrawSeparationLine(false);
                editDialog.show(getSupportFragmentManager(),
                        "test_edit_default_overall_decorator");
                break;
            case R.id.btn_edit_use_new_overall_decorator:
                EditDialog.Decorator decorator2 = EditDialog.getOverallDecorator(EditDialog.class);
                decorator2.reset();
                decorator2.setTitleTextSize(R.dimen.size_text_title_large);
                decorator2.setEditTextSize(R.dimen.size_text_title_large);
                EditDialog editDialog1 = new EditDialog();
                editDialog1.setTitle("use new overall decorator");
                editDialog1.setContent("yaya");
                editDialog1.show(getSupportFragmentManager(),
                        "test_edit_new_overall_decorator");
                break;
            case R.id.btn_edit_use_custom_decorator:
                EditDialog editDialog2 = new EditDialog();
                EditDialog.Decorator decorator3 = editDialog2.getCustomDecorator();
                decorator3.setContentLayoutRes(R.layout.et_custom);
                decorator3.setEditId(R.id.et_custom);
                editDialog2.setTitle("use custom decorator");
                editDialog2.setContent("yaya");
                editDialog2.show(getSupportFragmentManager(),
                        "test_edit_custom_decorator");
                break;
            case R.id.btn_list_dialog: {
                ListDialog listDialog = new ListDialog();
                listDialog.setTitle("this is list dialog");
                listDialog.setItems(new String[] { "item1", "item2" });
                listDialog.show(getSupportFragmentManager(),
                        "test_list");
            } break;
            case R.id.btn_multi_list_dialog: {
                ListDialog listDialog = new ListDialog();
                listDialog.setTitle("请在已配置过的传感器中选择所需作为节点");
                //listDialog.setTitle("this is multiple select list dialog");
                String[] items = new String[40];
                for (int i = 0;i < items.length;++i) {
                    items[i] = "item" + (i + 1);
                }
                listDialog.setItems(items);
                listDialog.setMultipleSelect(true);
                listDialog.show(getSupportFragmentManager(),
                        "test_mul_list");
            } break;
            case R.id.btn_set_base_decoration:
                BaseDialog.Decorator decorator4 = BaseDialog.getBaseOverallDecorator();
                decorator4.setTitleTextSize(R.dimen.super_text_size);
                decorator4.setCancelLabel(R.string.custom_cancel);
                break;
            case R.id.btn_show_simple_toast_in_main_thread:
                SimpleCustomizeToast.show("simple toast在主线程中弹出");
                break;
            case R.id.btn_show_normal_toast_in_other_thread:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(MainActivity.this, "normal toast在其他线程中弹出", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            ClosableLog.d(GENERAL_LOG_TAG, "normal toast 果然不靠谱啊!");
                        }
                    }
                }).start();
                break;
            case R.id.btn_show_simple_toast_in_other_thread:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleCustomizeToast.show("simple toast在其他线程中弹出");
                    }
                }).start();
                break;
            case R.id.btn_switch_fragment:
                initSwitchableFragmentManager();
                int index = v.getTag() == null ? 0 : (int)v.getTag();
                mSwitchableFragmentManager.switchTo(index == 3 ? null : mFragmentTags[index]);
                if (index == 0) {
                    VisualFragment fragment = (VisualFragment)mSwitchableFragmentManager.getCurrentFragment();
                    if (fragment != null) {
                        fragment.setStudent(new VisualFragment.Student("fisrt", (int)(50.0 * Math.random())));
                    }
                }
                mSwitchableFragmentManager.notifyDataSetChanged();
                index += 1;
                if (index >= 4) {
                    index = 0;
                }
                v.setTag(index);
                break;
            case R.id.btn_notify_data_set_changed:
                initSwitchableFragmentManager();
                VisualFragment fragment = (VisualFragment)mSwitchableFragmentManager.getCurrentFragment();
                mSwitchableFragmentManager.notifyDataSetChanged();
                if (fragment != null) {
                    fragment.setStudent(new VisualFragment.Student("second", (int)(50.0 * Math.random())));
                }
                break;
            case R.id.btn_set_text:
                mSizeSelfAdaptionTextView.setText(mEtSetText.getText().toString());
                break;
            case R.id.btn_test_general_recycler_view:
                startActivity(new Intent(this, TestGeneralRecyclerViewActivity.class));
                break;
            case R.id.btn_test_recycler_view_base_adapter:
                startActivity(new Intent(this, TestRecyclerViewBaseAdapterActivity.class));
                break;
            case R.id.btn_sort_dialog:
                if (mSortDialog == null) {
                    mSortDialog = new SortDialog();
                    mSortDialog.addSortType(R.id.address, "地址")
                            .addSortType(R.id.time, "时间")
                            .addSortType(R.id.name, "名称")
                            .addSortType(R.id.unit, "单位")
                            .setDefaultSelectedId(R.id.time);
                }
                mSortDialog.show(getSupportFragmentManager(), "sort dialog");
                break;
            case R.id.tv_text_view_on_click:
                SimpleCustomizeToast.show("when you see me, it means CustomDrawableSizeTextView can be clicked");
                break;
            case R.id.btn_test_dialog_fragment_constraint_layout_match_parent:
                NoTitleConstraintLayoutDialog dialog3 = new NoTitleConstraintLayoutDialog();
                dialog3.show(getSupportFragmentManager(), "no title constraint dialog");
                break;
            case R.id.btn_test_dialog_fragment_linear_layout_match_parent:
                NoTitleLinearLayoutDialog dialog4 = new NoTitleLinearLayoutDialog();
                dialog4.show(getSupportFragmentManager(), "no title linear dialog");
                break;
            case R.id.btn_test_dialog_fragment_relative_layout_match_parent:
                NoTitleRelativeLayoutDialog dialog5 = new NoTitleRelativeLayoutDialog();
                dialog5.show(getSupportFragmentManager(), "no title relative dialog");
                break;
            case R.id.btn_print_activity_and_fragment_lifecycle:
                startActivity(new Intent(this, PrintLifecycleActivity.class));
                break;
            case R.id.btn_open_serial_port:
                Button btnSerialPort = (Button) v;
                if ("open".equals(btnSerialPort.getText())) {
                    //powerOnSerialPort();
                    if (mSerialPortKit == null) {
                        mSerialPortKit = new SerialPortKit();
                    }
                    String serialPortName = mEtSerialPortName.getText().toString();
                    if (TextUtils.isEmpty(serialPortName)) {
                        SimpleCustomizeToast.show("serial port name can not be empty");
                    } else {
                        //powerOnSerialPort();
                        if (mSerialPortKit.launch(serialPortName,
                                Integer.parseInt((String) mSpnBaudRate.getSelectedItem()),
                                0)) {
                            btnSerialPort.setText("close");
                            //mSerialPortKit.startListen(this);
                            mSerialPortDataReceiver = new DataReceiver(mSerialPortKit);
                            mSerialPortDataReceiver.startListen(this);
                            SimpleCustomizeToast.show(serialPortName + " opened");
                        } else {
                            SimpleCustomizeToast.show("open serial port failed");
                        }
                    }
                } else {
                    closeSerialPort();
                    btnSerialPort.setText("open");
                }
                break;
            case R.id.btn_time_synchronization:
                sendCommand(new UdpSensorProtocol().makeTimeSynchronizationFrame());
                break;
            case R.id.btn_request_data:
                sendCommand(new UdpSensorProtocol().makeDataRequestFrame());
                break;
            case R.id.btn_serial_port_power:
                Button btnSerialPortPower = (Button) v;
                if ("power on".equals(btnSerialPortPower.getText())) {
                    powerOnSerialPort();
                    btnSerialPortPower.setText("power off");
                } else {
                    powerOffSerialPort();
                    btnSerialPortPower.setText("power on");
                }
                break;
            case R.id.btn_clear:
                mTvEmission.setText(null);
                mTvReception.setText(null);
                break;
            case R.id.btn_open_settings_activity:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.btn_open_settings_activity2:
                startActivity(new Intent(this, SettingsActivity2.class));
                break;
            case R.id.btn_open_usb_activity:
                startActivity(new Intent(this, UsbDebugActivity.class));
                break;
            case R.id.btn_open_udp_activity:
                startActivity(new Intent(this, UdpDebugActivity.class));
                break;
            case R.id.btn_open_ble_activity:
                startActivity(new Intent(this, BleDebugActivity.class));
                break;
            case R.id.btn_filter_dialog:
                if (mFilterDialog == null) {
                    mFilterDialog = new FilterDialog();
                    mFilterDialog.addFilterType("协议", new String[] { "BLE", "ESB" });
                    mFilterDialog.addFilterType("类型", new String[] { "温度传感器", "重力加速度", "智能避雷器", "液位传感器" });
                    mFilterDialog.addFilterType("项目", new String[] { "水厂", "铝厂", "尼乐园南站", "WEISI", "测试", "ABCDEFG", "HIJKLMN", "OPQ", "RST", "UVW", "XYZ", "其他"});
                    //FilterDialog.Decorator decorator5 = mFilterDialog.getCustomDecorator();
                    //decorator5.setDisplayTagAll(false);
                }
                mFilterDialog.show(getSupportFragmentManager(), "test_filter_dialog");
                break;
            case R.id.btn_search_dialog:
                if (mSearchDialog == null) {
                    mSearchDialog = new SearchDialog();
                    mSearchDialog.setTitle("test search dialog");
                }
                mSearchDialog.show(getSupportFragmentManager(), "search_dialog");
                break;
            case R.id.btn_open_test_simple_cursor_loader_activity:
                startActivity(new Intent(this, TestSimpleCursorLoaderActivity.class));
                break;
            case R.id.btn_open_test_recycler_view_cursor_loader_activity:
                startActivity(new Intent(this, TestRecyclerViewCursorLoaderActivity.class));
                break;
            case R.id.btn_open_tcp_activity:
                startActivity(new Intent(this, TcpDebugActivity.class));
                break;
            case R.id.btn_open_tcp_activity2:
                startActivity(new Intent(this, TcpDebugActivity2.class));
                break;
            case R.id.btn_test_recycler_view_inconsistency:
                startActivity(new Intent(this, TestRecyclerViewInconsistencyActivity.class));
                break;
            case R.id.btn_test_permission_write_external_storage:
                try {
                    Byte.parseByte("A8", 16);
                } catch (Exception e) {
                    ExceptionLog.record(e);
                }
                break;
            case R.id.btn_export_inner_error_info:
                exportInnerErrorInfo();
                break;
            case R.id.btn_test_parcel_invoke:
                Intent intent = new Intent(this, TestParcelableActivity.class);
                Storage<PhysicalSensor> physicalSensorStorage = new Storage<>(new Storage.ElementsProvider<PhysicalSensor>() {
                    @Override
                    public void onProvideElements(@NonNull List<PhysicalSensor> elements, FilterCollection<PhysicalSensor> filters) {

                    }
                });
                physicalSensorStorage.setSorter(new SensorAddressSorter<PhysicalSensor>(), true);
                physicalSensorStorage.addFilter(1, new BleProtocolFilter<PhysicalSensor>());
                physicalSensorStorage.addFilter(2, new SensorUseForRealTimeFilter<PhysicalSensor>());
                //FilterCollection<PhysicalSensor> filterCollection = new FilterCollection<>();
                //filterCollection.add(new BleProtocolFilter<PhysicalSensor>());
                //filterCollection.add(new SensorUseForRealTimeFilter<PhysicalSensor>().setType(7));
                intent.putExtra("storage", physicalSensorStorage);
                startActivity(intent);
                break;
            case R.id.btn_test_update_version:
                startActivity(new Intent(this, TestUpdateVersionActivity.class));
                break;
        }
    }

    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE)
    private void exportInnerErrorInfo() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //FileUtil.copyOnlyFile(getFilesDir() + File.separator + "ErrorInfo.txt", Environment.getExternalStorageDirectory() + File.separator + "ErrorInfo_copy.txt");
            ExceptionLog.exportErrorInfo(null);
        } else {
            EasyPermissions.requestPermissions(this, "我要写文件", RC_WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void sendCommand(byte[] command) {
        mTvEmission.append(NumericConverter.bytesToHexDataString(command));
        mTvEmission.append("\n");
        sendData(command);
    }

    private void closeSerialPort() {
        if (mSerialPortKit != null) {
            mSerialPortDataReceiver.stopListen();
            mSerialPortKit.shutdown();
            SimpleCustomizeToast.show(mEtSerialPortName.getText() + " closed");
            //powerOffSerialPort();
        }
    }

    private void powerOnSerialPort() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_dc_in_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_vbat_out_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_gpio_112"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_a"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_b"});
            SimpleCustomizeToast.show("power on");
        } catch (IOException e) {
            ExceptionLog.display(e);
        }
    }

    private void powerOffSerialPort() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_dc_in_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_vbat_out_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_a"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_b"});
            SimpleCustomizeToast.show("power off");
        } catch (IOException e) {
            ExceptionLog.display(e);
        }
    }

    public void onNormalTextViewClick(View v) {
        SimpleCustomizeToast.show("when you see me, it means normal TextView can be clicked");
    }

    public void onCustomTextViewClick(View v) {
        SimpleCustomizeToast.show("when you see me, it means CustomDrawableSizeTextView can be clicked");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSortDialog = (SortDialog) getSupportFragmentManager().findFragmentByTag("sort dialog");
        }
    }

    private void initSwitchableFragmentManager() {
        if (mSwitchableFragmentManager == null) {
            mSwitchableFragmentManager = new SwitchableFragmentManager(
                    getSupportFragmentManager(),
                    R.id.fl_fragment_stub,
                    mFragmentTags,
                    new Class[] {VisualFragment1.class, VisualFragment2.class, VisualFragment3.class});
        }
    }

    @Override
    public void onSortTypeChanged(@IdRes int checkedId, boolean isAscending) {
        ClosableLog.d(SortDialog.TAG, "R.id.address = " + R.id.address
                + "R.id.time = " + R.id.time
                + "R.id.name = " + R.id.name
                + "R.id.unit = " + R.id.unit);
        ClosableLog.d(SortDialog.TAG, "checkedId = " + checkedId + ", isAscend = " + isAscending);
    }

    @Override
    public int onDataReceived(final byte[] data, final int len) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvReception.append(mChkHexReception.isChecked()
                        ? NumericConverter.bytesToHexDataString(data, 0, len)
                        : new String(data, 0, len));
                if (mChkHexReception.isChecked()) {
                    mTvReception.append("\n");
                }
            }
        });
        return len;
    }

    @Override
    public boolean onErrorOccurred(Exception e) {
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.chk_hex_emission: {
                String tmp = mEmissionTextCopy;
                mEmissionTextCopy = mTvEmission.getText().toString();
                mTvEmission.setText(tmp);
            } break;
            case R.id.chk_hex_reception: {
                String tmp = mReceptionTextCopy;
                mReceptionTextCopy = mTvReception.getText().toString();
                mTvReception.setText(tmp);
            } break;
        }
//        if (isChecked) {
//            mTvReception.setText(NumericConverter.bytesToHexDataString(mTvReception.getText().toString().getBytes()));
//        } else {
//            CharSequence text = mTvReception.getText();
//            if (!TextUtils.isEmpty(text)) {
//                mTvReception.setText(new String(NumericConverter.hexDataStringToBytes(text.toString())));
//            }
//        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        CharSequence emissionText = v.getText();
        if (!TextUtils.isEmpty(emissionText)) {
            mTvEmission.append(emissionText);
            //mTvEmission.append("\n");
            sendData(mChkHexEmission.isChecked()
                    ? NumericConverter.hexDataStringToBytes(emissionText.toString())
                    : emissionText.toString().getBytes());
            v.setText(null);
        }
        return false;
    }

    private void sendData(byte[] data) {
        if (mSerialPortKit != null && data != null) {
            try {
                mSerialPortKit.send(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFilterChange(FilterDialog dialog, boolean[] hasFilters, List<Integer>[] checkedFilterEntryValues) {
        Log.d(GENERAL_LOG_TAG, generateFilterChangeMsg(hasFilters, checkedFilterEntryValues));
    }

    private String generateFilterChangeMsg(boolean[] hasFilters, List<Integer>[] checkedFilterEntryValues) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = hasFilters.length;i < size;++i) {
            builder.append("hasFilters: ")
                    .append(hasFilters[i])
                    .append(", checkedFilterEntryValues: ");
            if (hasFilters[i]) {
                for (Integer entryValue
                        : checkedFilterEntryValues[i]) {
                    builder.append(entryValue).append(',');
                }
                builder.replace(builder.length() - 1, builder.length(), "\n");
            } else {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public void onSearch(String target) {
        Log.d(GENERAL_LOG_TAG, "search content: " + target);
    }

    public void test() {
        WarnerImpl warner = new WarnerImpl();
    }

    @Override
    public void onProvideElements(@NonNull List<PhysicalSensor> elements, FilterCollection<PhysicalSensor> filters) {

    }

    @Override
    public void onItemsSelected(ListDialog dialog, int[] positions) {
        StringBuilder builder = new StringBuilder();
        builder.append("selected items: ");
        if (positions.length > 0) {
            builder.append(positions[0]);
        }
        for (int i = 1;i < positions.length;++i) {
            builder.append(", ").append(positions[i]);
        }
        Log.d(GENERAL_LOG_TAG, builder.toString());
    }

    private static class Value extends com.cjq.lib.weisi.iot.container.Value {

        public Value(long timestamp) {
            super(timestamp);
        }
    }

    private static class WarnerImpl implements Warner<Value> {

        @Override
        public int test(Value value) {
            return RESULT_NORMAL;
        }
    }

    private static class SingleValueDomainWarnerImpl implements DisplayMeasurement.SingleRangeWarner {

        @Override
        public int test(DisplayMeasurement.Value value) {
            return RESULT_ABOVE_HIGH_LIMIT;
        }
    }
}
