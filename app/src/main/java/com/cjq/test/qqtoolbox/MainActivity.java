package com.cjq.test.qqtoolbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cjq.test.qbox.ui.dialog.BaseDialog;
import com.cjq.test.qbox.ui.dialog.ConfirmDialog;
import com.cjq.test.qbox.ui.dialog.EditDialog;
import com.cjq.test.qbox.ui.dialog.ListDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                ConfirmDialog.setOverallDecorator(new ConfirmDialog.Decorator() {
                    @Override
                    public int getTitleTextSize() {
                        return R.dimen.size_text_title_large;
                    }

                    @Override
                    public int getOkLabel() {
                        return R.string.hao;
                    }

                    @Override
                    public int getExitButtonTextColor() {
                        return R.color.colorAccent;
                    }

                    @Override
                    public int getExitButtonTextSize() {
                        return R.dimen.size_text_title_large;
                    }
                });
                ConfirmDialog dialog1 = new ConfirmDialog();
                dialog1.show(getSupportFragmentManager(),
                        "test_confirm_new_overall",
                        "use new overall decorator");
                break;
            case R.id.btn_confirm_custom_decorator:
                ConfirmDialog dialog2 = new ConfirmDialog();
                dialog2.setCustomDecorator(new ConfirmDialog.Decorator() {
                    @Override
                    public int getTitleLayout() {
                        return R.layout.group_dialog_title;
                    }

                    @Override
                    public int getTitleId() {
                        return R.id.tv_custom_title;
                    }

                    @Override
                    public int getTitleTextSize() {
                        return R.dimen.size_text_activity;
                    }

                    @Override
                    public int getOkCancelLayout() {
                        return R.layout.group_ok_cancel_custom;
                    }

                    @Override
                    public int getOkId() {
                        return R.id.btn_ok_custom;
                    }

                    @Override
                    public int getCancelId() {
                        return R.id.btn_cancel_custom;
                    }

                    @Override
                    public boolean isEnableExitGroupDetailSetting() {
                        return false;
                    }

                    @Override
                    public int getBaseLeftPadding() {
                        return R.dimen.dialog_base_padding_left;
                    }

                    @Override
                    public int getBaseTopPadding() {
                        return R.dimen.dialog_base_padding_top;
                    }
                });
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
                EditDialog.setOverallDecorator(new EditDialog.Decorator() {
                    @Override
                    public int getTitleTextSize() {
                        return R.dimen.size_text_title_large;
                    }

                    @Override
                    public int getEditTextSize() {
                        return R.dimen.size_text_title_large;
                    }
                });
                EditDialog editDialog1 = new EditDialog();
                editDialog1.show(getSupportFragmentManager(),
                        "test_edit_new_overall_decorator",
                        "use new overall decorator",
                        "yaya");
                break;
            case R.id.btn_edit_use_custom_decorator:
                EditDialog editDialog2 = new EditDialog();
                editDialog2.setCustomDecorator(new EditDialog.Decorator() {
                    @Override
                    public int getContentLayout() {
                        return R.layout.et_custom;
                    }

                    @Override
                    public int getEditId() {
                        return R.id.et_custom;
                    }

                    @Override
                    public boolean completeCustomForContentView() {
                        return true;
                    }
                });
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
        }
    }
}
