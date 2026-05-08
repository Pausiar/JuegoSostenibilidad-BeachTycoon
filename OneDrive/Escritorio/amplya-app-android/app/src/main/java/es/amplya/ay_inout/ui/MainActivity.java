package es.amplya.ay_inout.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.amplya.ay_inout.R;
import es.amplya.ay_inout.config.ConfigManager;
import es.amplya.ay_inout.config.SessionManager;
import es.amplya.ay_inout.notifications.NotificationHelper;

// pantalla principal con grid
public class MainActivity extends AppCompatActivity implements HomeAdapter.OnCardClickListener {

    private static final int PRIMARY_HOME_CARD_COUNT = 6;

    private SessionManager sessionManager;
        private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
    sessionManager.clearPendingNotificationCount();
    NotificationHelper.ensureNotificationChannel(this);
    requestNotificationPermissionIfNeeded();

        String[] titles = {
            getString(R.string.entrada),
            getString(R.string.historico_fichajes),
            getString(R.string.documentacion)
        };

        populateHomeCards(titles);

        View btnTopMenu = findViewById(R.id.btnTopMenu);
        if (btnTopMenu != null) {
            btnTopMenu.setOnClickListener(this::showTopMenu);
        }

        View btnTopMore = findViewById(R.id.btnTopMore);
        if (btnTopMore != null) {
            btnTopMore.setOnClickListener(v -> startActivity(new Intent(this, ConfigActivity.class)));
        }
    }

    private void showTopMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main_actions, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_logout) {
                logoutAndGoToLogin();
                return true;
            }
            if (itemId == R.id.action_exit_app) {
                finishAffinity();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void logoutAndGoToLogin() {
        // Prevent next user from inheriting previous gsBase settings on shared devices.
        new ConfigManager(this).clear();
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void populateHomeCards(String[] titles) {
        LinearLayout primaryCards = findViewById(R.id.layoutPrimaryCards);
        LinearLayout secondaryCards = findViewById(R.id.layoutSecondaryCards);
        View moreSection = findViewById(R.id.sectionMoreTools);
        LayoutInflater inflater = LayoutInflater.from(this);

        if (primaryCards == null || secondaryCards == null) {
            return;
        }

        primaryCards.removeAllViews();
        secondaryCards.removeAllViews();

        for (int i = 0; i < titles.length; i++) {
            boolean primaryCard = i < PRIMARY_HOME_CARD_COUNT;
            View card = inflater.inflate(R.layout.item_home_card, primaryCard ? primaryCards : secondaryCards, false);
            TextView tvTitle = card.findViewById(R.id.tvCardTitle);
            ImageView ivIcon = card.findViewById(R.id.ivCardIcon);

            tvTitle.setText(titles[i]);
            ivIcon.setImageResource(getHomeIconRes(i));

            final int position = i;
            card.setOnClickListener(v -> onCardClick(position));

            if (primaryCard) {
                primaryCards.addView(card);
            } else {
                secondaryCards.addView(card);
            }
        }

        boolean hasSecondaryCards = titles.length > PRIMARY_HOME_CARD_COUNT;
        secondaryCards.setVisibility(hasSecondaryCards ? View.VISIBLE : View.GONE);
        if (moreSection != null) {
            moreSection.setVisibility(hasSecondaryCards ? View.VISIBLE : View.GONE);
        }
    }

    private int getHomeIconRes(int position) {
        switch (position) {
            case 0:
                return android.R.drawable.ic_lock_idle_alarm;
            case 1:
                return android.R.drawable.ic_menu_recent_history;
            case 2:
                return android.R.drawable.ic_menu_agenda;
            default:
                return android.R.drawable.ic_menu_my_calendar;
        }
    }

    @Override
    public void onCardClick(int position) {
        switch (position) {
            case 0: // entrada/salida
                startActivity(new Intent(this, CheckInActivity.class));
                break;
            case 1: // histÃ³rico fichajes
                startActivity(new Intent(this, HistoricoActivity.class));
                break;
            case 2: // documentaciÃ³n
                startActivity(new Intent(this, DocumentacionActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_config) {
            startActivity(new Intent(this, ConfigActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            logoutAndGoToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

