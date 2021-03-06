package io.github.qi1002.ilearn;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PronunciationExamActivity extends AppCompatActivity {

    private int test_count = 0;
    private int correct_count = 0;

    private String currentExam = "";
    private String current_mean = "";
    private WebView mWebView = null;
    private WebViewClient mWebViewClient = null;
    private TextView mWordLabel = null;
    private EditText mWordAnswer = null;
    private Menu contextMenu = null;
    private Button nextButton = null;
    private IEnumerable datasetEnumerate = null;
    private ScoreRecord scoreData = null;
    private boolean bPlayVoiceDone = true;
    private boolean bLoadPageDone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation_exam);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Pronunciation Exam");
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar ();
        actionbar.setDisplayHomeAsUpEnabled(true);

        nextButton = (Button) findViewById(R.id.pron_exam_next);
        nextButton.setText("(" + correct_count + "/" + test_count + "/" + getTestCount() + ") Next");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                examWordCheck();
            }
        });

        mWebView = (WebView) findViewById(R.id.pron_exam_dataset_webview);
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
                Log.d("ExamInfo", "URL done " + url);

                bPlayVoiceDone = false;
                view.loadUrl("javascript:(function() {  app.voiceCheck(" + DatasetRecord.getDictionaryProvider().getWordVoiceCheck(currentExam) + ");  " +
                        DatasetRecord.getDictionaryProvider().getWordVoiceLink(currentExam) +
                        " app.voiceDone('" + currentExam + "' ); })()");
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.endsWith(".mp3"))
                    Log.d("ExamInfo", "Resource done " + url);
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

        mWordAnswer = (EditText) findViewById(R.id.pron_exam_answer);
        mWordLabel = (TextView) findViewById(R.id.pron_exam_dataset);
        mWordLabel.setOnTouchListener(new SwipeTouchListener(this) {

            public void onSwipeLeft() {
                examWordCheck();
            }

            public void onClick() {
                examWordCheck();
            }
        });

        // default to set focus to WebView
        focusWebView();

        // let apk use media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //do the first exam
        String datasetEnumerateWay = Helper.getPreferenceString(this, R.string.pref_key_pronunciation_exam_enumerate);
        datasetEnumerate = DatasetRecord.getEnumerator(DatasetRecord.getDataset(), datasetEnumerateWay);
        examWord();

        //initialize
        scoreData = new ScoreRecord();
        scoreData.type = ScoreRecord.PRONUNCIATION;
        scoreData.test_cnt = getTestCount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pronunciation_exam, menu);
        contextMenu = menu;
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
                examWordCheck();
                return true;
            case R.id.action_show:
                if (!bLoadPageDone) {
                    Log.d("ExamInfo", "Show not yet");
                    Toast.makeText(this, "Load Page not done yet", Toast.LENGTH_SHORT).show();
                }else {
                    mWebView.setVisibility(View.VISIBLE);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void examWordCheck() {
        if (!bPlayVoiceDone || !bLoadPageDone) {
            Log.d("ExamInfo", "Next not yet");
            Toast.makeText(this, "Load Page or Play voice not done yet", Toast.LENGTH_SHORT).show();
        }else {
            examWord();
        }
    }

    private void examWord() {

        // check score
        if (test_count != 0) {
            if ( Helper.isNullOrEmpty(mWordAnswer.getText().toString())) {
                Toast.makeText(this, "The word answer is not got yet", Toast.LENGTH_SHORT).show();
                return;
            } else {
                boolean correct = false;
                if (currentExam.compareTo(mWordAnswer.getText().toString()) == 0) {
                    correct_count++;
                    correct = true;
                }

                scoreData.applyResult(currentExam, test_count - 1, correct);
                nextButton.setText("(" + correct_count + "/" + test_count + "/" + getTestCount() + ") Next");
                mWordAnswer.setText("");
            }
        }

        if (test_count < getTestCount()) {
            examWordOnly();
        }else
        {
            Helper.ExitBox(this, "the score is " + (correct_count * 100 / getTestCount()) + " (" + correct_count + "/" +
                    test_count + "/" + getTestCount() + ")\nand then exit");
            scoreData.updateRecord(this);
        }

        test_count++;
    }

    private void examWordOnly() {
        current_mean = "";
        bLoadPageDone = false;
        DatasetRecord record = datasetEnumerate.getCurrent();
        datasetEnumerate.moveNext();
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.loadUrl(DatasetRecord.getDictionaryProvider().getWordMeanLink(record.name));
        currentExam = record.name;
    }

    private void focusWebView() {
        mWebView.requestFocus();
    }

    public int getTestCount() {
        return getTestCount(this);
    }
    public static int getTestCount(Context context) {
        String count = Helper.getPreferenceString(context, R.string.pref_key_pronunciation_exam_count);
        return Integer.valueOf(count);
    }

    public void setVoiceDone(boolean value) { bPlayVoiceDone = value; }
    public void skipTest() {
        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                examWordOnly();
            }
        });
    }
}
