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

public class VocabularyExamActivity extends AppCompatActivity {

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
    private boolean bPlayVoiceDone = true;
    private boolean bLoadPageDone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_exam);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nextButton = (Button) findViewById(R.id.voc_exam_next);
        nextButton.setText("(" + correct_count + "/" + test_count + "/" + total_count + ") Next");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                examWordCheck();
            }
        });

        mWebView = (WebView) findViewById(R.id.voc_exam_dataset_webview);
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

                if (MainActivity.examSpeak) {
                    bPlayVoiceDone = false;
                    view.loadUrl("javascript:(function() { " +
                            DatasetRecord.getDictionaryProvider().getWordVoiceLink(currentExam) +
                            " app.voiceDone('" + currentExam + "' ); })()");
                }

                view.loadUrl("javascript:app.getHTMLSource" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>', '" + currentExam + "');");
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.endsWith(".mp3"))
                    Log.d("ExamInfo", "Resource done " + url);
            }
        });

        mWordAnswer = (EditText) findViewById(R.id.voc_exam_answer);
        mWordLabel = (TextView) findViewById(R.id.voc_exam_dataset);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vocabulary_exam, menu);
        contextMenu = menu;
        // update menu item "speak"
        updateSpeakOption();
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
            case R.id.action_speak:
                MainActivity.examSpeak = !MainActivity.examSpeak;
                updateSpeakOption();
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
            if (current_mean == null || current_mean.length() == 0) {
                Toast.makeText(this, "The word mean is not got yet", Toast.LENGTH_SHORT).show();
                return;
            } else if ( mWordAnswer.getText().toString() == null || mWordAnswer.getText().toString().length() == 0) {
                Toast.makeText(this, "The word answer is not got yet", Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (currentExam.contains(mWordAnswer.getText().toString())) {
                    correct_count++;
                }

                nextButton.setText("(" + correct_count + "/" + test_count + "/" + total_count + ") Next");
            }
        }

        if (test_count < total_count) {
            current_mean = "";
            bLoadPageDone = false;
            DatasetRecord record = datasetEnumerate.getCurrent();
            datasetEnumerate.moveNext();
            mWebView.setVisibility(View.INVISIBLE);
            mWebView.loadUrl(DatasetRecord.getDictionaryProvider().getWordMeanLink(record.name));
            mWordLabel.setText("Exam : " + record.name);
            currentExam = record.name;
        }else
        {
            Helper.ExitBox(this, "the score is " + (correct_count * 100 / total_count) + " (" + correct_count + "/" + test_count + "/" + total_count + ")\nand then exit");
        }

        test_count++;
    }

    private void focusWebView() {
        mWebView.requestFocus();
    }

    public void setVoiceDone(boolean value) { bPlayVoiceDone = value; }

    public void setHTMLDone(String html, String word)
    {
        current_mean = DatasetRecord.getDictionaryProvider().getWordMean(this, html, word);
    }

    private void updateSpeakOption() {
        MenuItem speakItem = contextMenu.findItem(R.id.action_speak);
        speakItem.setCheckable(true);
        speakItem.setChecked(MainActivity.examSpeak);
        bPlayVoiceDone = true;
    }
}
