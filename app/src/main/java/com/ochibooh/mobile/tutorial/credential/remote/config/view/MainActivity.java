package com.ochibooh.mobile.tutorial.credential.remote.config.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ochibooh.mobile.tutorial.credential.remote.config.R;
import com.ochibooh.mobile.tutorial.credential.remote.config.databinding.ActivityMainBinding;
import com.ochibooh.mobile.tutorial.credential.remote.config.lifecycle.MainActivityLifecycleObserver;
import com.ochibooh.mobile.tutorial.credential.remote.config.viewmodel.MainActivityViewModel;

import java.util.logging.Level;

import lombok.extern.java.Log;

@Log
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        this.getLifecycle().addObserver(new MainActivityLifecycleObserver());
        this.binding.setLifecycleOwner(this);
        this.viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        this.binding.setViewModel(this.viewModel);
        setup();
    }

    private void setup() {
        NavHostFragment navHostFragment = (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.main_nav_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                log.log(Level.INFO, String.format("Page navigation [ name=%s, id=%s arguments=%s, messagesId=%s, messageViewId=%s ]", destination.getLabel(), destination.getId(), arguments, R.id.smsMessagesPage, R.id.smsMessageViewPage));
                this.viewModel.setPage(destination.getId());
                if (destination.getId() == R.id.smsMessagesPage) {
                    this.viewModel.setTitle("Messages");
                }
                if (destination.getId() == R.id.smsMessageViewPage) {
                    String title = arguments != null ? arguments.getString("recipientNumber", "Unknown") : "Unknown";
                    this.viewModel.setTitle(title);
                }
            });
            NavigationUI.setupWithNavController(this.binding.toolbar, navController, new AppBarConfiguration.Builder(navController.getGraph()).build());
        }

        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        log.log(Level.WARNING, String.format("Firebase remote config fetch error [ %s ]", task.getException()));
                    }
                });
    }
}