package com.satfeed.activity;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/*
 * Created by Andrew Brin on 4/17/2016.
 */
final public class EditTextLooper {
    @BindingAdapter("bind_edit_text")
    public static void addTextChangedListener(EditText view, final int variable){
        final ViewDataBinding binding = DataBindingUtil.findBinding(view);
        view.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* no op*/ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String string = s.toString();
                binding.setVariable(variable, string);
                binding.executePendingBindings();
            }

            @Override
            public void afterTextChanged(Editable s) { /* no op*/ }
        });
    }
}
