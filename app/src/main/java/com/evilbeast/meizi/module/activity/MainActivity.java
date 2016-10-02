package com.evilbeast.meizi.module.activity;


import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.base.RxBaseActivity;
import com.evilbeast.meizi.module.meizi.MeiZiPageFragment;
import com.evilbeast.meizi.module.meizi.MeiZiSimpleFragment;
import com.evilbeast.meizi.module.zxfuli.FuliPageFragment;
import com.evilbeast.meizi.utils.SnackbarUtil;

import butterknife.BindView;

public class MainActivity extends RxBaseActivity {

    private long mUserExitTime;

    // 绑定toolbar
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    // NavigateView
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;




    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
       // MeiZiPageFragment fragment = MeiZiPageFragment.newInstance();
        FuliPageFragment fragment = FuliPageFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.content,fragment).commit();

    }

    @Override
    public void initToolbar() {
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitApp();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exitApp() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (System.currentTimeMillis() - mUserExitTime > 2000) {
            SnackbarUtil.showMessage(mDrawerLayout, getString(R.string.user_exit_app_tip));
            mUserExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
