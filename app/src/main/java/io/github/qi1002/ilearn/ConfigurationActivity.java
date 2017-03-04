package io.github.qi1002.ilearn;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.github.qi1002.ilearn.configuration.ConfigurationScoreActivity;
import io.github.qi1002.ilearn.configuration.ConfigurationSettingActivity;
import io.github.qi1002.ilearn.configuration.ConfigurationUtilityActivity;

public class ConfigurationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        Button button;

        button = (Button) findViewById(R.id.bt_configuration_score);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(ConfigurationScoreActivity.class);
            }
        });

        button = (Button) findViewById(R.id.bt_configuration_settings);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(ConfigurationSettingActivity.class);
            }
        });

        button = (Button) findViewById(R.id.bt_configuration_utility);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(ConfigurationUtilityActivity.class);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
    }

    private void launchActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }
}
