package com.example.pushin_v5;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.pushin_v5.interfaceFragment.review.ReviewFragment;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;
import com.example.pushin_v5.interfaceFragment.examlist.CreateExamFragment;
import com.example.pushin_v5.interfaceFragment.examlist.ExamListFragment;
import com.example.pushin_v5.interfaceFragment.flashcardlist.CreateFlashcardFragment;
import com.example.pushin_v5.interfaceFragment.lecturematerials.LectureMaterialsFragment;
import com.example.pushin_v5.interfaceFragment.login.LoginActivity;
import com.example.pushin_v5.interfaceFragment.profile.ProfileFragment;
import android.view.MenuItem;
import android.view.View;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pushin_v5.imageTask.GetBitmapByURL;
import com.example.pushin_v5.interfaceFragment.topiclist.CreateTopicFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private int userId = 0;
    private HttpConnection httpConnection;
    private ParseJson pj = new ParseJson();
    private String jsonstring, nickname, email;
    private TextView t_nickname, t_email, t_tutorial;
    private ImageView userProfileImage;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        final FloatingActionButton fab = findViewById(R.id.fab);

        //create exam, topic and flashcard
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu=new PopupMenu(getBaseContext(), fab);
                popupMenu.getMenuInflater().inflate(R.menu.creating_options,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        CharSequence title = item.getTitle();
                        Bundle bundle = new Bundle();
                        bundle.putInt("userId", userId);
                        if ("EXAM".equals(title)) {
                            CreateExamFragment createExam = new CreateExamFragment();
                            createExam.setArguments(bundle);
                            navController.navigate(R.id.nav_createExam);
                        } else if ("TOPIC".equals(title)) {
                            CreateTopicFragment createTopic = new CreateTopicFragment();
                            createTopic.setArguments(bundle);
                            navController.navigate(R.id.nav_createTopic);
                        } else if ("FLASHCARD".equals(title)) {
                            CreateFlashcardFragment createFlashcard =  new CreateFlashcardFragment();
                            createFlashcard.setArguments(bundle);
                            navController.navigate(R.id.nav_createFlashcard);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        //get userId from login activity
        Bundle b = getIntent().getExtras();
        userId = b.getInt("userId");

        //send userId to fragment
        ProfileFragment profile = new ProfileFragment();
        ExamListFragment exam = new ExamListFragment();
        LectureMaterialsFragment materialsFragment = new LectureMaterialsFragment();
        ReviewFragment review = new ReviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);

        profile.newInstance(bundle);
        exam.newInstance(bundle);
        materialsFragment.newInstance(bundle);
        review.newInstance(bundle);

        //show user info to show on header
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        t_nickname = headerView.findViewById(R.id.t_userNicknameinNav);
        t_email = headerView.findViewById(R.id.t_userEmailinNav);
        t_tutorial = headerView.findViewById(R.id.t_tutorial);
        userProfileImage = headerView.findViewById(R.id.image_userHeadProfile);

        //tutorial video of this app
        t_tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/J7ElfnvBZxA")));
            }
        });

        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/userInfo.php?id="+userId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if(jsonstring!=null) {
                if (pj.identification(jsonstring)) {
                    JSONObject userInfo = new JSONObject(jsonstring).getJSONArray("user").getJSONObject(0);
                    nickname = userInfo.getString("userNickname");
                    email = userInfo.getString("userEmail");
                    t_nickname.setText(nickname);
                    t_email.setText(email);
                }
            }
            tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/uploads/getProfilePhoto.php?userId=" + userId;
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null) {
                if (pj.identification(jsonstring)) {
                    String imagePath = new JSONObject(jsonstring).getString("image_path");
                    GetBitmapByURL load = new GetBitmapByURL();
                    Bitmap myBitmap = load.execute(imagePath).get();
                    if (myBitmap == null) System.out.println("null bitmap");
                    else {
                        bitmap = myBitmap;
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        drawable.setCircular(true);
                        userProfileImage.setImageDrawable(drawable);
                    }
                }
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_examlist,R.id.nav_review, R.id.nav_profile, R.id.nav_lecture_materials)
                .setDrawerLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
