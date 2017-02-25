package io.github.qi1002.ilearn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // global settings
    public static final String PREFS_NAME = "PreferenceSettings";
    public static boolean saveToXML = true;
    public static boolean practiceMean = false;
    public static boolean examSpeak = false;
    public static boolean switchActivity = false;

    // controls
    private Button mBtLoopupDictionary;
    private Button mBtPracticeDataset;
    private Button mBtVocabularyExam;
    private Button mBtPronunciationExam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtLoopupDictionary = (Button) findViewById(R.id.bt_dictionary_lookup);
        mBtLoopupDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(LookupDictionaryActivity.class);
            }
        });

        mBtPracticeDataset = (Button) findViewById(R.id.bt_dataset_practice);
        mBtPracticeDataset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(PracticeDatasetActivity.class);
            }
        });

        mBtVocabularyExam = (Button) findViewById(R.id.bt_exam_vocabulary);
        mBtVocabularyExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(VocabularyExamActivity.class);
            }
        });

        mBtPronunciationExam = (Button) findViewById(R.id.bt_exam_pronunciation);
        mBtPronunciationExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(PronunciationExamActivity.class);
            }
        });

        // enable external storage
        Helper.verifyStoragePermissions(this);

        // initialize dataset in default
        DatasetRecord.initialDataset(this);

        // initialize scoreHistory  in default
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        ScoreRecord.initialScoreHistory(this, cal.get(Calendar.YEAR));

        // let apk use media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //read preference settings
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        saveToXML = settings.getBoolean("saveToXML", true);
        practiceMean = settings.getBoolean("practiceMean", false);
        examSpeak = settings.getBoolean("examSpeak", false);
    }

    private void launchActivity(Class<?> cls) {
        if (DatasetRecord.isInitialized()) {
            Intent intent = new Intent(this, cls);
            startActivity(intent);
            switchActivity = true;
        } else {
            Helper.MessageBox(this, "dataset is not initialized yet");
        }
    }

    @Override
    public void onStop() {
        // wait  checkHTMLSource function (thread join to avoid lrge data.xml)
        // if we can check its really finalized not in the background , refer http://steveliles.github.io/is_my_android_app_currently_foreground_or_background.html
        super.onStop();
        if (DatasetRecord.isDirty() && switchActivity == false) {
            DatasetRecord.writeDataset(this, DatasetRecord.output_filename);
            DatasetRecord.updateDataset(DatasetRecord.output_filename, DatasetRecord.dataset_filename);
            Log.d("LookupInfo", "write xml");
        }

        //read preference settings
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("saveToXML", saveToXML);
        editor.putBoolean("practiceMean", practiceMean);
        editor.putBoolean("examSpeak", examSpeak);
        editor.commit(); // Commit the edits!

        switchActivity = false;
    }
}


