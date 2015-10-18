package com.dawgandpony.pd2skills.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.dawgandpony.pd2skills.BuildObjects.Attachment;
import com.dawgandpony.pd2skills.BuildObjects.Weapon;
import com.dawgandpony.pd2skills.BuildObjects.WeaponBuild;
import com.dawgandpony.pd2skills.Database.DataSourceWeapons;
import com.dawgandpony.pd2skills.Database.MySQLiteHelper;
import com.dawgandpony.pd2skills.Fragments.AttachmentListFragment;
import com.dawgandpony.pd2skills.Fragments.BlankFragment;
import com.dawgandpony.pd2skills.Fragments.WeaponListFragment;
import com.dawgandpony.pd2skills.R;

/**
 * Created by Jamie on 11/10/2015.
 */
public class EditWeaponActivity extends AppCompatActivity {

    public static final String EXTRA_WEAPON_TYPE = "WeaponType";
    private static final String TAG = "EditWeaponActivity";
    Weapon currentWeapon;
    long currentWeaponID = -2;
    int weaponType = -1;
    ArrayList<Weapon> baseWeaponInfo;
    ArrayList<Attachment> baseAttachmentInfo;
    ArrayList<ArrayList<Attachment>> attachmentsSplitUp;
    ArrayList<WeaponsCallbacks> mListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_weapon);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        if (savedInstanceState == null){
            currentWeaponID = getIntent().getLongExtra(WeaponListFragment.EXTRA_WEAPON_ID, -1);
            weaponType = getIntent().getIntExtra(EXTRA_WEAPON_TYPE, -1);
        } else {
            currentWeaponID = savedInstanceState.getLong(WeaponListFragment.EXTRA_WEAPON_ID);
            weaponType = savedInstanceState.getInt(EXTRA_WEAPON_TYPE);
        }

        new GetWeaponsXMLTask(weaponType).execute();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_weapon, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(WeaponListFragment.EXTRA_WEAPON_ID, currentWeaponID);
        outState.putInt(EXTRA_WEAPON_TYPE, weaponType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = this.getIntent();
                intent.putExtra(WeaponListFragment.EXTRA_WEAPON_ID, -2);
                intent.putExtra(EXTRA_WEAPON_TYPE, 0);
                this.setResult(RESULT_CANCELED, intent);
                finish();
                return true;
            case R.id.action_equip:
                Intent intent2 = this.getIntent();
                intent2.putExtra(WeaponListFragment.EXTRA_WEAPON_ID, -2);
                this.setResult(RESULT_OK, intent2);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        mListeners = new ArrayList<>();
        final Adapter adapter = new Adapter(getSupportFragmentManager());

        adapter.addFragment(BlankFragment.newInstance("WIP"), "Overview");

        String[] attachment_types = getResources().getStringArray(R.array.attachment_types);
        for (int i = 0; i < MySQLiteHelper.COLUMNS_ATTACHMENTS.length; i++){
            adapter.addFragment(AttachmentListFragment.newInstance(i), attachment_types[i]);
        }

        viewPager.setAdapter(adapter);
    }


    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    public class GetWeaponsXMLTask extends AsyncTask<Void, Integer, ArrayList<Weapon>> {

        private final int type;

        public GetWeaponsXMLTask(int weaponType) {
            super();
            this.type = weaponType;
        }

        @Override
        protected ArrayList<Weapon> doInBackground(Void... params) {
            ArrayList<Weapon> weapons = new ArrayList<>();

            //Get list of skill builds from database.
            weapons = WeaponBuild.getWeaponsFromXML(getResources(), type);

            return weapons;
        }

        @Override
        protected void onPostExecute(ArrayList<Weapon> weapons) {
            super.onPostExecute(weapons);
            baseWeaponInfo = weapons;
            onBaseInfoReady();
        }
    }

    public interface WeaponsCallbacks{
        void onWeaponReady();
    }

    public void listen(Fragment fragment){
        mListeners.add((WeaponsCallbacks) fragment);
    }

    public void stopListening(Fragment fragment){
        mListeners.remove((WeaponsCallbacks) fragment);
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public ArrayList<Attachment> getPossibleAttachments(int attachmentType){
        return attachmentsSplitUp.get(attachmentType);
    }

    public void updateCurrentWeapon(int attachmentType, int currentAttachmentIndex) {
        Attachment newAttachment = attachmentsSplitUp.get(attachmentType).get(currentAttachmentIndex);
        //currentWeapon.getAttachments().set(attachmentType, newAttachment);
        DataSourceWeapons dataSourceWeapons = new DataSourceWeapons(this, baseWeaponInfo, baseAttachmentInfo);
        dataSourceWeapons.open();
        dataSourceWeapons.updateAttachment(currentWeapon.getId(), attachmentType, newAttachment.getPd2skillsID());
        dataSourceWeapons.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mListeners = null;
    }

    public class GetAttachmentsXMLTask extends AsyncTask<Void, Integer, ArrayList<Attachment>> {


        public GetAttachmentsXMLTask() {
            super();
        }

        @Override
        protected ArrayList<Attachment> doInBackground(Void... params) {
            ArrayList<Attachment> attachments = new ArrayList<>();

            //Get list of skill builds from database.
            attachments = Attachment.getAttachmentsFromXML(getResources());

            return attachments;
        }

        @Override
        protected void onPostExecute(ArrayList<Attachment> attachments) {
            super.onPostExecute(attachments);
            baseAttachmentInfo = attachments;

            onAttachmentInfoReady();
        }
    }

    public class GetWeaponFromDB extends AsyncTask<Void, Integer, Weapon> {

        private final long id;

        public GetWeaponFromDB(long id) {
            super();
            this.id = id;
        }

        @Override
        protected Weapon doInBackground(Void... params) {
            DataSourceWeapons dataSourceWeapons = new DataSourceWeapons(EditWeaponActivity.this, baseWeaponInfo, baseAttachmentInfo);
            dataSourceWeapons.open();
            Weapon weapon = dataSourceWeapons.getWeapon(id);
            dataSourceWeapons.close();

            return weapon;
        }

        @Override
        protected void onPostExecute(Weapon weapon) {
            super.onPostExecute(weapon);
            currentWeapon = weapon;

            attachmentsSplitUp = new ArrayList<>();
            for (int i = Attachment.MOD_BARREL; i <= Attachment.MOD_UPPER_RECEIVER; i++){
                attachmentsSplitUp.add(new ArrayList<Attachment>());
            }
            for (Attachment attachment : baseAttachmentInfo){
                for (Long l : currentWeapon.getPossibleAttachments()){
                    if (l == attachment.getPd2skillsID()){
                        attachmentsSplitUp.get(attachment.getAttachmentType()).add(attachment);
                    }
                }
            }

            onWeaponReady();
        }
    }

    private void onBaseInfoReady() {
        new GetAttachmentsXMLTask().execute();
    }

    private void onAttachmentInfoReady() {
        new GetWeaponFromDB(currentWeaponID).execute();
    }

    private void onWeaponReady() {
        if (mListeners != null){
            for (WeaponsCallbacks listener : mListeners){
                listener.onWeaponReady();
            }
        }
    }

    //todo Retrieve weapon
    //todo retrieve attachments from xml
    //link attacments with possible attachments
    //display in fragment
}
