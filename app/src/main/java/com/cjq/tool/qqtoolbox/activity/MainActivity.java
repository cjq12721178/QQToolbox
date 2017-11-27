package com.cjq.tool.qqtoolbox.activity;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cjq.lib.weisi.communicator.SerialPortKit;
import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.cjq.tool.qbox.ui.dialog.EditDialog;
import com.cjq.tool.qbox.ui.dialog.ListDialog;
import com.cjq.tool.qbox.ui.dialog.SortDialog;
import com.cjq.tool.qbox.ui.manager.SwitchableFragmentManager;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qbox.ui.view.SizeSelfAdaptionTextView;
import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.cjq.tool.qbox.util.NumericConverter;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.fragment.NoTitleConstraintLayoutDialog;
import com.cjq.tool.qqtoolbox.fragment.NoTitleLinearLayoutDialog;
import com.cjq.tool.qqtoolbox.fragment.NoTitleRelativeLayoutDialog;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment1;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment2;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment3;
import com.cjq.tool.qqtoolbox.util.DebugTag;

import java.io.IOException;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        SortDialog.OnSortTypeChangedListener, SerialPortKit.OnDataReceivedListener, CompoundButton.OnCheckedChangeListener {

    private SwitchableFragmentManager mSwitchableFragmentManager;
    private String[] mFragmentTags = new String[] {"visual1", "visual2", "visual3"};
    private SizeSelfAdaptionTextView mSizeSelfAdaptionTextView;
    private EditText mEtSetText;
    private SortDialog mSortDialog;
    private SerialPortKit mSerialPortKit;
    private EditText mEtSerialPortName;
    private TextView mTvReception;
    private CheckBox mChkHexEmission;
    private CheckBox mChkHexReception;
    private Spinner mSpnBaudRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ClosableLog.setEnablePrint(true);
        mSizeSelfAdaptionTextView = (SizeSelfAdaptionTextView) findViewById(R.id.tv_fix_size);
        mEtSetText = (EditText) findViewById(R.id.et_set_text);
        //findViewById(R.id.tv_text_view_on_click).setOnClickListener(this);

        mEtSerialPortName = (EditText) findViewById(R.id.et_serial_port_name);
        mTvReception = (TextView) findViewById(R.id.tv_reception);
        mChkHexEmission = (CheckBox) findViewById(R.id.chk_hex_emission);
        mChkHexEmission.setOnCheckedChangeListener(this);
        mChkHexReception = (CheckBox) findViewById(R.id.chk_hex_reception);
        mChkHexReception.setOnCheckedChangeListener(this);
        mSpnBaudRate = (Spinner) findViewById(R.id.spn_baud_rate);
        mSpnBaudRate.setSelection(16);
        EditText etEmission = (EditText) findViewById(R.id.et_emission);
        etEmission.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (mSerialPortKit != null) {
                    try {
                        mSerialPortKit.send(mChkHexEmission.isChecked()
                                ? NumericConverter.hexDataStringToBytes(v.getText().toString())
                                : v.getText().toString().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                int i;
//                CharSequence t = v.getText();
//                char[] text = new char[t.length()];
//                for (i=0; i<t.length(); i++) {
//                    text[i] = t.charAt(i);
//                }
//                try {
//                    //mOutputStream.send(new String(text).getBytes());
//                    //mOutputStream.send('\n');
//                    //mOutputStream.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSerialPort();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm_default_overall_decorator:
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.show(getSupportFragmentManager(),
                        "test_confirm",
                        "use default overall decorator",
                        false);
                break;
            case R.id.btn_confirm_new_overall_decorator:
                ConfirmDialog.Decorator decorator = ConfirmDialog.getOverallDecorator(ConfirmDialog.class);
                decorator.reset();
                decorator.setTitleTextSize(R.dimen.size_text_title_large);
                decorator.setOkLabel(R.string.hao);
                decorator.setExitButtonTextColor(R.color.colorAccent);
                decorator.setExitButtonTextSize(R.dimen.size_text_title_large);
                ConfirmDialog dialog1 = new ConfirmDialog();
                dialog1.show(getSupportFragmentManager(),
                        "test_confirm_new_overall",
                        "use new overall decorator");
                break;
            case R.id.btn_confirm_custom_decorator:
                ConfirmDialog dialog2 = new ConfirmDialog();
                ConfirmDialog.Decorator decorator1 = dialog2.getCustomDecorator();
                decorator1.setTitleLayout(R.layout.group_dialog_title);
                decorator1.setTitleId(R.id.tv_custom_title);
                decorator1.setTitleTextSize(R.dimen.size_text_activity);
                decorator1.setOkCancelLayout(R.layout.group_ok_cancel_custom);
                decorator1.setOkId(R.id.btn_ok_custom);
                decorator1.setCancelId(R.id.btn_cancel_custom);
                decorator1.setBasePadding(R.dimen.dialog_base_padding_left,
                        R.dimen.dialog_base_padding_top,
                        0,
                        0);
                dialog2.show(getSupportFragmentManager(),
                        "test_confirm_custom_decorator",
                        "use custom decorator");
                break;
            case R.id.btn_edit_use_default_overall_decorator:
                EditDialog editDialog = new EditDialog();
                editDialog.show(getSupportFragmentManager(),
                        "test_edit_default_overall_decorator",
                        "use default overall decorator",
                        "yaya");
                break;
            case R.id.btn_edit_use_new_overall_decorator:
                EditDialog.Decorator decorator2 = EditDialog.getOverallDecorator(EditDialog.class);
                decorator2.reset();
                decorator2.setTitleTextSize(R.dimen.size_text_title_large);
                decorator2.setEditTextSize(R.dimen.size_text_title_large);
                EditDialog editDialog1 = new EditDialog();
                editDialog1.show(getSupportFragmentManager(),
                        "test_edit_new_overall_decorator",
                        "use new overall decorator",
                        "yaya");
                break;
            case R.id.btn_edit_use_custom_decorator:
                EditDialog editDialog2 = new EditDialog();
                EditDialog.Decorator decorator3 = editDialog2.getCustomDecorator();
                decorator3.setContentLayout(R.layout.et_custom);
                decorator3.setEditId(R.id.et_custom);
                editDialog2.show(getSupportFragmentManager(),
                        "test_edit_custom_decorator",
                        "use custom decorator",
                        "yaya");
                break;
            case R.id.btn_list_dialog:
                ListDialog listDialog = new ListDialog();
                listDialog.show(getSupportFragmentManager(),
                        "test_list",
                        "this is list dialog",
                        new String[] { "item1", "item2" });
                break;
            case R.id.btn_set_base_decoration:
                BaseDialog.Decorator decorator4 = BaseDialog.getBaseOverallDecorator();
                decorator4.setTitleTextSize(R.dimen.super_text_size);
                decorator4.setCancelLabel(R.string.custom_cancel);
                break;
            case R.id.btn_show_simple_toast_in_main_thread:
                SimpleCustomizeToast.show(this, "simple toast在主线程中弹出");
                break;
            case R.id.btn_show_normal_toast_in_other_thread:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(MainActivity.this, "normal toast在其他线程中弹出", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            ClosableLog.d(DebugTag.GENERAL_LOG_TAG, "normal toast 果然不靠谱啊!");
                        }
                    }
                }).start();
                break;
            case R.id.btn_show_simple_toast_in_other_thread:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleCustomizeToast.show(MainActivity.this, "simple toast在其他线程中弹出");
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
                SimpleCustomizeToast.show(this, "when you see me, it means CustomDrawableSizeTextView can be clicked");
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
                    powerOnSerialPort();
                    if (mSerialPortKit == null) {
                        mSerialPortKit = new SerialPortKit();
                    }
                    String serialPortName = mEtSerialPortName.getText().toString();
                    if (TextUtils.isEmpty(serialPortName)) {
                        SimpleCustomizeToast.show(this, "serial port name can not be empty");
                    } else {
                        powerOnSerialPort();
                        if (mSerialPortKit.launch(serialPortName,
                                Integer.parseInt((String) mSpnBaudRate.getSelectedItem()),
                                0)) {
                            btnSerialPort.setText("close");
                            mSerialPortKit.startListen(this);
                            SimpleCustomizeToast.show(this, serialPortName + " opened");
                        } else {
                            SimpleCustomizeToast.show(this, "open serial port failed");
                        }
                    }
                } else {
                    closeSerialPort();
                    btnSerialPort.setText("open");
                }
                break;
        }
    }

    private void closeSerialPort() {
        if (mSerialPortKit != null) {
            mSerialPortKit.shutdown();
            powerOffSerialPort();
        }
    }

    private void powerOnSerialPort() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_dc_in_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_vbat_out_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_gpio_112"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_a"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_uart_b"});
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
        } catch (IOException e) {
            ExceptionLog.display(e);
        }
    }

    public void onNormalTextViewClick(View v) {
        SimpleCustomizeToast.show(this, "when you see me, it means normal TextView can be clicked");
    }

    public void onCustomTextViewClick(View v) {
        SimpleCustomizeToast.show(this, "when you see me, it means CustomDrawableSizeTextView can be clicked");
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
    public int onDataReceived(byte[] data, int len) {
        mTvReception.append(mChkHexReception.isChecked()
                ? NumericConverter.bytesToHexDataString(data)
                : new String(data, 0, len));
        return 0;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mTvReception.setText(isChecked
                ? NumericConverter.bytesToHexDataString(mTvReception.getText().toString().getBytes())
                : new String(NumericConverter.hexDataStringToBytes(mTvReception.getText().toString())));
    }
}
