package io.github.qi1002.ilearn;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}
//TODO00: how to do blocking wait in UI thread smartly.
//TODO01: use ecdict as dictionary provider
//TODO02: make preference header layout beautiful more (TODO)
//TODO03:
//TODO04: check if more rules are added to .gitignore
//TODO05: handle network go out case in any network connection case (4 activity and download data.xml) (TODO)
//TODO06: get android simulator in T420
//TODO07: 
//TODO08: check why reset setting  flow not trigger OnPreferenceChangeListener (TODO)
//TODO09: enumerate words by category limitation
//TODO10: input the word from english studio classmate (one category)
//TODO11: common 7000 words
//TODO12: dickson idioms
//TODO13: new verifyStoragePermissions ok in SS 6.01 but NG in emulator 6.0
//TODO14: don't consider no voice case if checking if pron exam ok <=> dataset size (TODO)
//TODO15: test all data.xml for dictionary feasibility
//TODO16: score update by hash set flow => apply to checkWord (TODO))
//TODO17:
//TODO18: add the default path to merge ecdict (TODO)
//TODO19: wait  checkHTMLSource function (thread join to avoid large data.xml), if we can check its really finalized not in the background , refer http://steveliles.github.io/is_my_android_app_currently_foreground_or_background.html
