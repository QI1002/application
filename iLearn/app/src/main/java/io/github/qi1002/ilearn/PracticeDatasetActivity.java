package io.github.qi1002.ilearn;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PracticeDatasetActivity extends AppCompatActivity {

    private String currentPractice = "";
    private WebView mWebView = null;
    private WebViewClient mWebViewClient = null;
    private TextView mWordLabel = null;
    private Menu contextMenu = null;
    private IEnumerable datasetEnumerate = null;
    private boolean bPlayVoiceDone = true;
    private boolean bLoadPageDone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_dataset);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Practice Dataset");
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar ();
        actionbar.setDisplayHomeAsUpEnabled(true);

        Button nextButton = (Button) findViewById(R.id.practice_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                practiceWordCheck();
            }
        });

        mWebView = (WebView) findViewById(R.id.practice_dataset_webview);
        /// Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "app");
        // Force links and redirects to open in the WebView instead of in a browser
        mWebViewClient = new WebViewClient() {
            //refer: http://stackoverflow.com/questions/6199717/how-can-i-know-that-my-webview-is-loaded-100
            @Override
            public void onPageFinished(WebView view, String url) {
                bLoadPageDone = true;
                bPlayVoiceDone = false;
                Log.d("PracticeInfo", "URL done " + url);
                view.loadUrl("javascript:(function() {  app.voiceCheck(" + DatasetRecord.getDictionaryProvider().getWordVoiceCheck(currentPractice) + ");  " +
                             DatasetRecord.getDictionaryProvider().getWordVoiceLink(currentPractice) +
                             " app.voiceDone('" + currentPractice + "' ); })()");
                view.loadUrl("javascript:app.getHTMLSource" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>', '" + currentPractice + "');");
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.endsWith(".mp3"))
                    Log.d("PracticeInfo", "Resource done " + url);
            }
        };

        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //Log.d("LookupInfo", "URL " + view.getUrl() + " progress: " + progress);
                if (progress >= 95 && !bLoadPageDone) {
                    // workaround for chrome 5.5 with A Parser-blocking, cross-origin script
                    mWebViewClient.onPageFinished(view, view.getUrl());
                }
            }
        });

        mWordLabel = (TextView) findViewById(R.id.practice_dataset);
        mWordLabel.setOnTouchListener(new SwipeTouchListener(this) {

            public void onSwipeLeft() {
                practiceWordCheck();
            }

            public void onClick() {
                practiceWordCheck();
            }
        });

        // default to set focus to WebView
        focusWebView();

        // let apk use media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //do the first practice
        if (Helper.getPreferenceBoolean(this, R.string.pref_key_resume_practice) && MainActivity.practiceEnumerate != null)
        {
            datasetEnumerate = MainActivity.practiceEnumerate;
        }else {
            String datasetEnumerateWay = Helper.getPreferenceString(this, R.string.pref_key_practice_enumerate);
            datasetEnumerate = DatasetRecord.getEnumerator(DatasetRecord.getDataset(), datasetEnumerateWay);
        }

        practiceWord();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Helper.getPreferenceBoolean(this, R.string.pref_key_resume_practice))
            MainActivity.practiceEnumerate = datasetEnumerate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_practice_dataset, menu);
        contextMenu = menu;
        // update menu item "mean"
        updateMeanOption();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_return:
                finish();
                return true;
            case R.id.action_next:
                practiceWordCheck();
                return true;
            case R.id.action_mean:
                Helper.putPreferenceBoolean(this, R.string.pref_key_show_mean_dialog, !getPracticeMean());
                updateMeanOption();
                return true;
            case R.id.action_show:
                if (!bLoadPageDone) {
                    Log.d("PracticeInfo", "Show not yet");
                    Toast.makeText(this, "Load Page not done yet", Toast.LENGTH_SHORT).show();
                }else {
                    mWebView.setVisibility(View.VISIBLE);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void practiceWordCheck() {
        if (!bPlayVoiceDone || !bLoadPageDone) {
            Log.d("PracticeInfo", "Next not yet");
            Toast.makeText(this, "Load Page or Play voice not done yet", Toast.LENGTH_SHORT).show();
        }else {
            practiceWord();
        }
    }

    public void practiceAgain()
    {
        datasetEnumerate.reset();
        practiceWord(datasetEnumerate.getCurrent());
    }

    public void practiceWord() {

        DatasetRecord record = datasetEnumerate.getCurrent();

        if (record == null) {

            DialogInterface.OnClickListener positiveListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            practiceAgain();
                        }
                    };

            DialogInterface.OnClickListener negativeListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    };

            Helper.SelectionBox(this, "Finish practicing all items in dataset...", "Repeat", "Return", "Practice Selection Box", positiveListener, negativeListener);
        } else {
            practiceWord(record);
        }
    }

    private void practiceWord(DatasetRecord record) {
        assert (record != null);
        bLoadPageDone = false;
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.loadUrl(DatasetRecord.getDictionaryProvider().getWordMeanLink(record.name));
        mWordLabel.setText("Practice: " + record.name);
        currentPractice = record.name;
        datasetEnumerate.moveNext();
    }

    private void focusWebView() {
        mWebView.requestFocus();
    }

    public void setVoiceDone(boolean value) { bPlayVoiceDone = value; }

    public void setHTMLDone(String html, String word)
    {
        if (getPracticeMean()) {
            String mean = DatasetRecord.getDictionaryProvider().getWordMean(this, html, word);
            if (DatasetRecord.getDictionaryProvider().isTranslate())
                mean = Translate.StoT(mean);
            Helper.InformationBox(this, "Extracted Meaning", mean);
        }
    }

    public boolean getPracticeMean()
    {
        return Helper.getPreferenceBoolean(this, R.string.pref_key_show_mean_dialog);
    }

    private void updateMeanOption() {
        MenuItem meanItem = contextMenu.findItem(R.id.action_mean);
        meanItem.setCheckable(true);
        meanItem.setChecked(getPracticeMean());
    }
}
