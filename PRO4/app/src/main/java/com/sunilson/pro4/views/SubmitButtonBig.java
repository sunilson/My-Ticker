package com.sunilson.pro4.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunilson.pro4.R;

/**
 * @author Linus Weiss
 */

public class SubmitButtonBig extends RelativeLayout {

    private ProgressBar progressBar;
    private TextView textView;
    private Button button;
    private String textNormal, textLoading;
    private boolean loadingState = false;

    public SubmitButtonBig(Context context) {
        super(context);

        this.textNormal = context.getString(R.string.channel_edit_save);
        this.textLoading = context.getString(R.string.loading);
    }

    public SubmitButtonBig(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubmitButtonBig(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        progressBar = (ProgressBar) findViewById(R.id.submit_button_progress);
        textView = (TextView) findViewById(R.id.submit_button_text);
        button = (Button) findViewById(R.id.submit_button);
    }

    public void setText(String textNormal, String textLoading) {
        this.textNormal = textNormal;
        this.textLoading = textLoading;
        setTextView();
    }

    private void setTextView () {
        if(loadingState) {
            textView.setText(this.textLoading);
        } else {
            textView.setText(this.textNormal);
        }
    }

    public void loading(boolean loading) {
        loadingState = loading;
        setTextView();
        if(loading) {
            progressBar.setVisibility(VISIBLE);
            button.setEnabled(false);
        } else {
            progressBar.setVisibility(GONE);
            button.setEnabled(true);
        }
    }
}
