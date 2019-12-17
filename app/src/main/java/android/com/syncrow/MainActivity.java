package android.com.syncrow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

//client ID=287121036961-h5n4cgehrjdebbtgh2v4h66ma1m2hlml.apps.googleusercontent.com
//client secret=wxOK6HszUp4KBvrWKR4ZUl4k

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final int RC_AUTHORIZE_DRIVE =0 ;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private View mHeaderView;
    private String mPath;
    private CircleImageView profileImage;
    private TextView mUseremail;
    private GoogleSignInClient mGoogleSignInClient;
    private DriveServiceHelper mDriveServiceHelper;
    private Drive mDriveService;
    private CardView mSyncbtnView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isStoragePermissionGranted();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        mGoogleSignInClient= GoogleSignIn.getClient(MainActivity.this,gso);

        mDrawerLayout=(DrawerLayout)findViewById(R.id.activity_main);
        mToggle=new ActionBarDrawerToggle(this,mDrawerLayout,R.string.Open,R.string.Close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSyncbtnView = findViewById(R.id.main_sync_btn_view);
        mNavigationView=findViewById(R.id.navigationView);
        mHeaderView = LayoutInflater.from(this).inflate(R.layout.nav_header,mNavigationView,false);
        mNavigationView.addHeaderView(mHeaderView);

        profileImage =mHeaderView.findViewById(R.id.nav_header_image);
        mUseremail=mHeaderView.findViewById(R.id.nav_header_email);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.menu_profile:
                        startProfileIntent();
                        break;
                    case R.id.menu_settings:
                        startSettingsIntent();
                        break;
                    case R.id.menu_help:
                        startHelpIntent();
                        break;
                    case R.id.menu_logout:
                        signOut();
                        break;
                }
                return true;

            }
        });


        /*String folder_main="/Syncrow Folder";

        File f=new File(Environment.getExternalStorageDirectory()+folder_main);

        if(!f.exists()){
            Toast.makeText(MainActivity.this,"New file created",Toast.LENGTH_SHORT).show();
            f.mkdirs();
            Log.d(TAG,f.getAbsolutePath());
            }
        mPath=f.getPath();
        */





        Scope SCOPE_DRIVE=new Scope(Scopes.DRIVE_FULL);
        Scope DRIVE_FILE=new Scope(Scopes.DRIVE_FILE);
        GoogleSignInAccount acct=GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if(acct!=null){
            if(!GoogleSignIn.hasPermissions(acct,DRIVE_FILE,SCOPE_DRIVE)){
                GoogleSignIn.requestPermissions(MainActivity.this,RC_AUTHORIZE_DRIVE,acct,DRIVE_FILE,SCOPE_DRIVE);
            }else {

                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(acct.getAccount());

                //Toast.makeText(MainActivity.this,credential.toString(),Toast.LENGTH_LONG).show();
                mDriveService = new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName("Syncrow").build();

                mDriveServiceHelper = new DriveServiceHelper(mDriveService);


            }
            String email=acct.getEmail();
            Uri photo=acct.getPhotoUrl();

            mUseremail.setText(email);
            if(photo!=null) {
                Picasso.get().load(photo.toString()).placeholder(R.mipmap.ic_launcher_profile_logo).into(profileImage);
            }else{
                Toast.makeText(MainActivity.this,"Add a photo to your google account",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(RC_AUTHORIZE_DRIVE==requestCode){
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));

                Drive googleDriveServices = new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Syncrow")
                        .build();

                mDriveServiceHelper = new DriveServiceHelper(googleDriveServices);

            }
        }
    }
//Chandskjhkjsfkjslfkhkljsh

    public void uploadfile(View v)
    {
        String path = "/storage/emulated/0/mypdf.pdf";
        mDriveServiceHelper.createFile(path).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {

                Toast.makeText(MainActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this, "Check Connection", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void downloadfile(View v)
    {



        mDriveServiceHelper.download().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {

                Toast.makeText(MainActivity.this, "Downloaded!", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this, "Check Connection", Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private String getMimeType(String url) {
        String type=null;
        String extension=MimeTypeMap.getFileExtensionFromUrl(url);
        if(extension!=null){
            type=MimeTypeMap.getSingleton().getExtensionFromMimeType(extension);
        }
        return type;
    }


    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG,"LogOut Successful");
                startSignInActivity();
            }
        });
    }

    private void startProfileIntent() {
        Intent profileIntent=new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profileIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(this);
        if(account==null){
            startSignInActivity();
        }
    }

    private void startSignInActivity() {
        Intent signInintent=new Intent(MainActivity.this,SignInActivity.class);
        startActivity(signInintent);
        finish();
    }



    private void startHelpIntent() {
        Intent helpIntent=new Intent(getApplicationContext(),HelpActivity.class);
        startActivity(helpIntent);
    }

    private void startSettingsIntent() {
        Intent settingIntent=new Intent(getApplicationContext(),SettingsActivity.class);
        settingIntent.putExtra("FilePath",mPath);
        startActivity(settingIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
}
