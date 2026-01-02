package com.example.wordclash.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.example.wordclash.utils.VocabularyImporter;
import com.google.android.material.navigation.NavigationView;

/**
 * מסך הבית של האפליקציה
 * כאן המשתמש יכול לבחור:
 * - להתחיל לשחק (Ranks)
 * - לראות לוח תוצאות (Leaderboard)
 * - לשחק Wordle
 * - להתנתק (Logout)
 *
 * אם המשתמש הוא אדמין, הוא גם יכול:
 * - לנהל משתמשים
 * - להוסיף מילים
 * - לערוך מילים
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // ========== חלק 1: משתנים ==========

    // כפתורים ראשיים
    private Button btnLogout;       // כפתור התנתקות
    private Button btnRanks;        // כפתור למשחק
    private Button btnWordle;       // כפתור לWordle
    private Button btnLeaderboard;  // כפתור ללוח תוצאות

    // טקסט וממשק
    private TextView tvHelloUser;   // טקסט "Hello [שם]"
    private DrawerLayout drawerLayout;     // תפריט צד
    private NavigationView navigationView; // פריטי תפריט
    private ImageView menuIcon;            // אייקון תפריט

    // נתונים
    private User user;  // המשתמש המחובר

    // קבועים
    private static final String PREFS_NAME = "WordClashPrefs";
    private static final String KEY_VOCABULARY_IMPORTED = "vocabulary_imported";

    // ========== חלק 2: יצירת המסך ==========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        // קבלת המשתמש המחובר
        user = SharedPreferencesUtils.getUser(this);
        if (user == null) {
            // אם אין משתמש מחובר - חזרה למסך התחלה
            startActivity(new Intent(this, StartPageActivity.class));
            finish();
            return;
        }

        // הקמת ממשק
        setupUI();

        // טעינת מילים (פעם ראשונה בלבד)
        importVocabularyIfNeeded();

        // הגדרת תפריט
        setupMenu();
    }

    // ========== חלק 3: הקמת ממשק ==========

    /**
     * חיבור כל הכפתורים וטקסטים מה-XML
     */
    private void setupUI() {
        // טקסט ברכה
        tvHelloUser = findViewById(R.id.tvHelloUser);
        tvHelloUser.setText("Hello " + user.getUserName());

        // תפריט צד
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menu_icon);

        navigationView.setNavigationItemSelectedListener(this);

        // פתיחת תפריט בלחיצה על האייקון
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // כפתור התנתקות
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        // כפתור למשחק
        btnRanks = findViewById(R.id.btnRanks);
        btnRanks.setOnClickListener(v ->
                startActivity(new Intent(this, RanksActivity.class))
        );

        // כפתור ללוח תוצאות
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class))
        );

        // כפתור ל-Wordle
        btnWordle = findViewById(R.id.WordleButton);
        btnWordle.setOnClickListener(v ->
                startActivity(new Intent(this, WordleActivity.class))
        );
    }

    // ========== חלק 4: תפריט צד ==========

    /**
     * הגדרת תפריט לפי סוג משתמש
     */
    private void setupMenu() {
        // אם המשתמש לא אדמין - הסתר אפשרויות אדמין
        if (!user.isAdmin()) {
            navigationView.getMenu().findItem(R.id.nav_users_table).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_add_word).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_manage_words).setVisible(false);
        }
    }

    /**
     * טיפול בבחירת פריט מהתפריט
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // מעבר למסך המתאים
        if (id == R.id.nav_users_table) {
            startActivity(new Intent(this, UserListActivity.class));
        } else if (id == R.id.nav_add_word) {
            startActivity(new Intent(this, AdminAddWordActivity.class));
        } else if (id == R.id.nav_manage_words) {
            startActivity(new Intent(this, AdminManageWordsActivity.class));
        } else if (id == R.id.nav_change_details) {
            startActivity(new Intent(this, ChangeDetailsActivity.class));
        }

        // סגירת התפריט
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ========== חלק 5: פונקציות עזר ==========

    /**
     * טעינת מילים פעם ראשונה
     */
    private void importVocabularyIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean imported = prefs.getBoolean(KEY_VOCABULARY_IMPORTED, false);

        if (!imported) {
            Toast.makeText(this, "טוען מילים לפעם הראשונה...",
                    Toast.LENGTH_LONG).show();

            // טעינת המילים
            VocabularyImporter.importVocabularyFromAssets(this);

            // סימון שטענו
            prefs.edit().putBoolean(KEY_VOCABULARY_IMPORTED, true).apply();

            Toast.makeText(this, "המילים נטענו בהצלחה!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * התנתקות מהמערכת
     */
    private void logout() {
        // מחיקת נתוני המשתמש
        SharedPreferencesUtils.signOutUser(this);

        // חזרה למסך התחלה
        Intent intent = new Intent(this, StartPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * טיפול בלחיצה על כפתור "חזרה"
     */
    @Override
    public void onBackPressed() {
        // אם התפריט פתוח - סגור אותו
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // אחרת - צא מהאפליקציה
            super.onBackPressed();
        }
    }
}