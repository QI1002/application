package io.github.qi1002.ilearn;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PronunciationExamActivity extends AppCompatActivity {

    public final int total_count = 5;
    private int test_count = 0;
    private int correct_count = 0;

    private String currentExam = "";
    private String current_mean = "";
    private WebView mWebView = null;
    private TextView mWordLabel = null;
    private EditText mWordAnswer = null;
    private Menu contextMenu = null;
    private Button nextButton = null;
    private String datasetEnumerateWay = "Counter";
    private IEnumerable datasetEnumerate = null;
    private ScoreRecord scoreData = null;
    private boolean bPlayVoiceDone = true;
    private boolean bLoadPageDone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation_exam);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nextButton = (Button) findViewById(R.id.pron_exam_next);
        nextButton.setText("(" + correct_count + "/" + test_count + "/" + total_count + ") Next");
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
        mWebView.setWebViewClient(new WebViewClient() {
            //refer: http://stackoverflow.com/questions/6199717/how-can-i-know-that-my-webview-is-loaded-100
            @Override
            public void onPageFinished(WebView view, String url) {
                bLoadPageDone = true;
                Log.d("ExamInfo", "URL done " + url);

                bPlayVoiceDone = false;
                view.loadUrl("javascript:(function() { " +
                        DatasetRecord.getDictionaryProvider().getWordVoiceLink(currentExam) +
                        " app.voiceDone('" + currentExam + "' ); })()");
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.endsWith(".mp3"))
                    Log.d("ExamInfo", "Resource done " + url);
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

        // default to set foucs to WebView
        focusWebView();

        // let apk use media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //do the first exam
        datasetEnumerate = DatasetRecord.getEnumerator(DatasetRecord.getDataset(), datasetEnumerateWay);
        examWord();

        //initialize
        scoreData = new ScoreRecord();
        scoreData.type = ScoreRecord.PRONUNCIATION;
        scoreData.test_cnt = total_count;
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
            if ( mWordAnswer.getText().toString() == null || mWordAnswer.getText().toString().length() == 0) {
                Toast.makeText(this, "The word answer is not got yet", Toast.LENGTH_SHORT).show();
                return;
            } else {
                boolean correct = false;
                if (currentExam.compareTo(mWordAnswer.getText().toString()) == 0) {
                    correct_count++;
                    correct = true;
                }

                scoreData.applyResult(currentExam, test_count - 1, correct);
                nextButton.setText("(" + correct_count + "/" + test_count + "/" + total_count + ") Next");
                mWordAnswer.setText("");
            }
        }

        if (test_count < total_count) {
            current_mean = "";
            bLoadPageDone = false;
            DatasetRecord record = datasetEnumerate.getCurrent();
            datasetEnumerate.moveNext();
            mWebView.setVisibility(View.INVISIBLE);
            mWebView.loadUrl(DatasetRecord.getDictionaryProvider().getWordMeanLink(record.name));
            mWordLabel.setText("Exam : ");
            currentExam = record.name;
        }else
        {
            Helper.ExitBox(this, "the score is " + (correct_count * 100 / total_count) + " (" + correct_count + "/" + test_count + "/" + total_count + ")\nand then exit");
            scoreData.updateRecord(this);
        }

        test_count++;
    }

    private void focusWebView() {
        mWebView.requestFocus();
    }

    public void setVoiceDone(boolean value) { bPlayVoiceDone = value; }
}
