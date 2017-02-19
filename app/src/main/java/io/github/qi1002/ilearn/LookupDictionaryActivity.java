package io.github.qi1002.ilearn;

import android.content.Context;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

public class LookupDictionaryActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    private WebView mWebView = null;
    private EditText mLookupWord = null;
    private boolean saveToXML = true;
    private Menu contextMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookup_dictionary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWebView = (WebView) findViewById(R.id.lookup_dictionary_webview);
        /// Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.setWebViewClient(new WebViewClient());

        mWebView.loadUrl(DatasetRecord.getDictionaryProvider().getEntrance());

        mLookupWord = (EditText) findViewById(R.id.lookup_dictionary);
        mLookupWord.setOnEditorActionListener(this);
        mLookupWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // code to execute when EditText loses focus
                    hideKeyboard();
                }
            }
        });

        // default to set foucs to WebView
        focusWebView();

        // let apk use media volume
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lookup_dictionary, menu);
        contextMenu = menu;
        // update menu item "save"
        updateSaveOption();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id)
        {
            case R.id.action_return:
                finish();
                return true;
            case R.id.action_back:
                if (mWebView.canGoBack())
                    mWebView.goBack();
                return true;
            case R.id.action_next:
                if (mWebView.canGoForward())
                    mWebView.goForward();
                return true;
            case R.id.action_save:
                saveToXML = !saveToXML;
                //TODO
                updateSaveOption();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            String input = v.getText().toString();
            mWebView.loadUrl(DatasetRecord.getDictionaryProvider().getWordMeanLink(input));
            hideKeyboard();
            focusWebView();
            return true; // consume.
        }
        return false; // pass on to other listener
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mLookupWord.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void focusWebView() {
        mWebView.requestFocus();
    }

    private void updateSaveOption() {
        MenuItem saveItem = contextMenu.findItem(R.id.action_save);
        saveItem.setCheckable(true);
        saveItem.setChecked(saveToXML);
    }
}
