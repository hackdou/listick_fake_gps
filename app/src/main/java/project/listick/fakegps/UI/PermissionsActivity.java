package project.listick.fakegps.UI;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import project.listick.fakegps.Contract.PermissionsImpl;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.Presenter.PermissionsPresenter;
import project.listick.fakegps.R;

public class PermissionsActivity extends Activity implements PermissionsImpl.UI {

    public static final int PERMISSION_REQUEST_CODE = 0;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;


    private PermissionsPresenter presenter;
    private Button mRequestPermissions;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            finishAffinity();
            return;
        }
        else { Toast.makeText(getBaseContext(), getString(R.string.doubletap_to_exit), Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        mRequestPermissions = findViewById(R.id.btn_continue);

        presenter = new PermissionsPresenter(this);

        presenter.onActivityLoad();

        mRequestPermissions.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.onPermissionRequest();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                presenter.onPermissionGranted();
            else {
                presenter.onPermissionDenied();
            }
        }
    }

    @Override
    public void showErrorOnButton() {
        mRequestPermissions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        mRequestPermissions.setTextColor(getColor(R.color.black));
        mRequestPermissions.setBackgroundColor(getColor(R.color.red_tonal_button));


        CountDownTimer timer = new CountDownTimer(800, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mRequestPermissions.setBackgroundColor(getColor(R.color.primaryColor));
                mRequestPermissions.clearAnimation();
                mRequestPermissions.startAnimation(AnimationUtils.loadAnimation(PermissionsActivity.this, R.anim.attenuation));
                mRequestPermissions.setTextColor(getColor(R.color.white));

                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }

            }
        };
        timer.start();
    }
}
