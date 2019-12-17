package android.com.syncrow;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final String User_Prefs="USER_PREFS";
    private static final String TAG = "ProfileActivity";
    private CircleImageView mUserImage;
    private TextView mUserName;
    private TextView mUserEmail;
    private TextView mDriveStorageUsed;
    private TextView mDriveStorageAval;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUserImage=findViewById(R.id.profile_image);
        mUserName=findViewById(R.id.profile_user_name);
        mUserEmail=findViewById(R.id.profile_email_val);
        mDriveStorageUsed=findViewById(R.id.profile_usestr_val);
        mDriveStorageAval=findViewById(R.id.profile_stravl_val);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ProfileActivity.this);
        if(acct!=null){
            String userName = acct.getDisplayName();
            String userEmail = acct.getEmail();
            String userId= acct.getId();
            Uri userPhoto= acct.getPhotoUrl();


            mUserName.setText(userName);
            mUserEmail.setText(userEmail);
            //Log.d(TAG,userPhoto);
            if(userPhoto!=null){
            Picasso.get().load((userPhoto.toString())).into(mUserImage);
            }else{
                Toast.makeText(ProfileActivity.this,"Add a photo to your google account",Toast.LENGTH_SHORT).show();
            }

        }



    }


}
