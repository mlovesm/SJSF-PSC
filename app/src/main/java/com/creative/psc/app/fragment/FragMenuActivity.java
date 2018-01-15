package com.creative.psc.app.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.creative.psc.app.R;
import com.creative.psc.app.board.NoticeBoardFragment;
import com.creative.psc.app.common.WorkPlaceFragment;
import com.creative.psc.app.gear.GearMainFragment;
import com.creative.psc.app.gear.GearPopupActivity;
import com.creative.psc.app.inno.IdeaScribFragment;
import com.creative.psc.app.menu.LoginActivity;
import com.creative.psc.app.menu.MainFragment;
import com.creative.psc.app.safe.DangerMainFragment;
import com.creative.psc.app.safe.DangerPopupActivity;
import com.creative.psc.app.safe.PeerLoveFragment;
import com.creative.psc.app.safe.PeerLoveWriteFragment;
import com.creative.psc.app.util.BackPressCloseSystem;
import com.creative.psc.app.util.SettingPreference;
import com.creative.psc.app.util.UtilClass;
import com.creative.psc.app.work.WorkStandardFragment;

public class FragMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FragMenuActivity";
    private SettingPreference pref = new SettingPreference("loginData",this);
    private String title;

    private DrawerLayout drawer;
    private FragmentManager fm;

    private BackPressCloseSystem backPressCloseSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        backPressCloseSystem = new BackPressCloseSystem(this);
        title= getIntent().getStringExtra("title");
        if(title==null) title= "";
        UtilClass.logD(TAG,"onCreate title="+title);

        onMenuInfo(title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }//onCreate


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int fragmentStackCount = fm.getBackStackEntryCount();
            String tag=fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            UtilClass.logD(TAG, "count="+fragmentStackCount+", tag="+tag);
            if(tag.equals("메인")){
                backPressCloseSystem.onBackPressed();
            }else if(fragmentStackCount!=1&&(tag.equals(title+"작성")||tag.equals(title+"수정")||tag.equals(title+"상세"))){
                super.onBackPressed();
            }else{
                UtilClass.logD(TAG, "피니쉬");
                Fragment fragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putString("mode", "back");
                onFragment(fragment,bundle,"메인");
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        title= intent.getStringExtra("title");
        UtilClass.logD(TAG,"onNewIntent title="+title);
        onMenuInfo(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        BusProvider.getInstance().post(ActivityResultEvent.create(requestCode, resultCode, data));
    }

    public void onMenuInfo(String title){
        Fragment frag = null;
        Bundle bundle = new Bundle();

        fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if(title.equals("메인")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new MainFragment());
            String mode= getIntent().getStringExtra("mode");
            if(mode.equals("login")){
                bundle.putString("mode", "first");
            }else{
                bundle.putString("mode", "back");
            }

        }else if(title.equals("공지사항")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new NoticeBoardFragment());

        }else if(title.equals("사람찾기")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new PersonnelFragment());

        }else if(title.equals("무재해현황판")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Common/accident_view.do");

        }else if(title.equals("작업기준")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WorkStandardFragment());

        }else if(title.equals("QSS 활동계획조회")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Inno/cicle_plan_info.do");

        }else if(title.equals("연장근무현황")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Status/overtime_list.do?sabun_no="+MainFragment.loginSabun);

        }else if(title.equals("연차현황")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Status/vacation_list.do?sabun_no="+MainFragment.loginSabun);

        }else if(title.equals("급여명세서")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Status/salary_list.do?sabun_no="+MainFragment.loginSabun);

        }else if(title.equals("아이디어낙서방")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new IdeaScribFragment());

        }else if(title.equals("작업개소현황")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WorkPlaceFragment());

        }else if(title.equals("동료사랑카드")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new PeerLoveFragment());

        }else if(title.equals("장비점검")){
            String selectGearKey= getIntent().getStringExtra("selectGearKey");
            String selectDriver= getIntent().getStringExtra("selectDriver");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new GearMainFragment());
            bundle.putString("selectGearKey",selectGearKey);
            bundle.putString("selectDriver",selectDriver);
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Gear/gearList.do?gear_cd="+selectGearKey);

        }else if(title.equals("동료사랑카드")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new PeerLoveFragment());

        }else if(title.equals("동료사랑카드상세")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new PeerLoveWriteFragment());
            bundle.putString("peer_key", MainFragment.pendingPathKey);
            bundle.putString("mode", "update");

        }else if(title.equals("작업기준")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WorkStandardFragment());

        }else if(title.equals("위험기계사용점검")){
            String url= getIntent().getStringExtra("url");
            String selectDaeClassKey= getIntent().getStringExtra("selectDaeClassKey");
            String selectJungClassKey= getIntent().getStringExtra("selectJungClassKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new DangerMainFragment());
            bundle.putString("selectDaeClassKey",selectDaeClassKey);
            bundle.putString("selectJungClassKey",selectJungClassKey);
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/safe_write.do?large_cd="+selectDaeClassKey+"&mid_cd="+selectJungClassKey+"&sabun_no="+MainFragment.loginSabun);

        }else if(title.equals("위험기계사용점검이력팝업")||title.equals("위험기계사용점검이력")){
            String selectDaeClassKey= getIntent().getStringExtra("selectDaeClassKey");
            String selectJungClassKey= getIntent().getStringExtra("selectJungClassKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new DangerMainFragment());
            bundle.putString("title","위험기계사용점검이력");
            bundle.putString("selectDaeClassKey",selectDaeClassKey);
            bundle.putString("selectJungClassKey",selectJungClassKey);
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/safe_check_history.do?large_cd="+selectDaeClassKey+"&mid_cd="+selectJungClassKey+"&sabun_no="+MainFragment.loginSabun);

        }else{
            return;
        }

        fragmentTransaction.addToBackStack(title);

        bundle.putString("title",title);

        frag.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void onFragment(Fragment fragment, Bundle bundle, String title){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentReplace, fragment);
        fragmentTransaction.addToBackStack(title);

        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(FragMenuActivity.this);
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제하시겠습니까?");
        }else{
            alertDlg.setMessage("로그아웃 하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                }else if(gubun.equals("D")){
                }else{
                    Intent logIntent = new Intent(getBaseContext(), LoginActivity.class);
                    logIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(logIntent);
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlg.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent=null;

        if (id == R.id.nav_menu1 || id == R.id.nav_menu2 || id == R.id.nav_menu3 || id == R.id.nav_menu4) {
            return false;

        } else if (id == R.id.nav_menu1_1) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "공지사항");

        } else if (id == R.id.nav_menu1_2) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "사람찾기");

        } else if (id == R.id.nav_menu1_3) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "무재해현황판");

        } else if (id == R.id.nav_menu1_4) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "작업개소현황");

        } else if (id == R.id.nav_menu2_1) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "동료사랑카드");

        } else if (id == R.id.nav_menu2_2) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "작업기준");

        } else if (id == R.id.nav_menu2_3) {
            intent = new Intent(getApplicationContext(),DangerPopupActivity.class);
            intent.putExtra("title", "위험기계사용점검");
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return false;

        } else if (id == R.id.nav_menu3_1) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "QSS 활동계획조회");

        } else if (id == R.id.nav_menu3_2) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "아이디어낙서방");

        } else if (id == R.id.nav_menu4_1) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "연장근무현황");

        } else if (id == R.id.nav_menu4_2) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "연차현황");

        } else if (id == R.id.nav_menu4_3) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "급여명세서");

        } else if (id == R.id.nav_log_out) {
            alertDialog("L");
            return false;
        }else{

        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


}
