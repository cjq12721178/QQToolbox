package com.cjq.tool.qqtoolbox;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.cjq.tool.qbox.ui.dialog.EditDialog;
import com.cjq.tool.qbox.ui.dialog.ListDialog;
import com.cjq.tool.qbox.ui.manager.SwitchableFragmentManager;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment1;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment2;
import com.cjq.tool.qqtoolbox.switchable_fragment_manager.VisualFragment3;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SwitchableFragmentManager mSwitchableFragmentManager;
    private String[] mFragmentTags = new String[] {"visual1", "visual2", "visual3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitchableFragmentManager = new SwitchableFragmentManager(
                getSupportFragmentManager(),
                R.id.fl_fragment_stub,
                mFragmentTags,
                new Class[] {VisualFragment1.class, VisualFragment2.class, VisualFragment3.class});
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
            case R.id.btn_show_simple_toast:
                SimpleCustomizeToast.show(this, "尼玛");
                break;
            case R.id.btn_switch_fragment:
                int index = v.getTag() == null ? 0 : (int)v.getTag();
                mSwitchableFragmentManager.switchTo(mFragmentTags[index]);
                index += 1;
                if (index >= 3) {
                    index = 0;
                }
                v.setTag(index);
                break;
        }
    }
}
