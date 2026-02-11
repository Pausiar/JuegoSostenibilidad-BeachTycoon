package com.example.juegosostenibilidad;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la carga de assets PNG de Kenney
 */
public class KenneyAssetManager {

    private static KenneyAssetManager instance;
    private Context context;
    private AssetManager assetManager;
    private Map<String, Bitmap> bitmapCache;
    private Typeface gameFont;
    private Typeface gameFontNarrow;

    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_TOKEN = "token.png";
    public static final String ICON_POUCH = "pouch.png";
    public static final String ICON_POUCH_ADD = "pouch_add.png";
    public static final String ICON_LOCK_CLOSED = "lock_closed.png";
    public static final String ICON_LOCK_OPEN = "lock_open.png";
    public static final String ICON_CHARACTER = "character.png";
    public static final String ICON_PAWN = "pawn.png";
    public static final String ICON_PAWNS = "pawns.png";
    public static final String ICON_AWARD = "award.png";
    public static final String ICON_FLASK_FULL = "flask_full.png";
    public static final String ICON_FLASK_EMPTY = "flask_empty.png";
    public static final String ICON_FIRE = "fire.png";
    public static final String ICON_SKULL = "skull.png";
    public static final String ICON_SHIELD = "shield.png";
    public static final String ICON_HOURGLASS = "hourglass.png";
    public static final String ICON_APPLE = "resource_apple.png";
    public static final String ICON_HEART = "suit_hearts.png";
    public static final String ICON_CAMPFIRE = "campfire.png";
    public static final String ICON_IRON = "resource_iron.png";
    public static final String ICON_LUMBER = "resource_lumber.png";

    public static final String BAR_GREEN = "bar_round_gloss_small.png";
    public static final String BAR_GREEN_L = "bar_round_gloss_small_l.png";
    public static final String BAR_GREEN_M = "bar_round_gloss_small_m.png";
    public static final String BAR_GREEN_R = "bar_round_gloss_small_r.png";
    public static final String BUTTON_GREEN = "button_square_header_large_rectangle.png";

    private KenneyAssetManager(Context context) {
        this.context = context.getApplicationContext();
        this.assetManager = context.getAssets();
        this.bitmapCache = new HashMap<>();
        loadFonts();
    }

    public static synchronized KenneyAssetManager getInstance(Context context) {
        if (instance == null) {
            instance = new KenneyAssetManager(context);
        }
        return instance;
    }

    private void loadFonts() {
        try {
            gameFont = Typeface.createFromAsset(assetManager, "fonts/Kenney Future.ttf");
            gameFontNarrow = Typeface.createFromAsset(assetManager, "fonts/Kenney Future Narrow.ttf");
        } catch (Exception e) {
            gameFont = Typeface.DEFAULT_BOLD;
            gameFontNarrow = Typeface.DEFAULT;
        }
    }

    public Typeface getGameFont() {
        return gameFont;
    }

    public Typeface getGameFontNarrow() {
        return gameFontNarrow;
    }

    public Bitmap loadBitmap(String filename) {
        if (bitmapCache.containsKey(filename)) {
            return bitmapCache.get(filename);
        }

        try {
            InputStream is = assetManager.open("kenney/" + filename);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            if (bitmap != null) {
                bitmapCache.put(filename, bitmap);
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap loadBitmap(String filename, int targetWidth, int targetHeight) {
        String cacheKey = filename + "_" + targetWidth + "x" + targetHeight;

        if (bitmapCache.containsKey(cacheKey)) {
            return bitmapCache.get(cacheKey);
        }

        Bitmap original = loadBitmap(filename);
        if (original == null) return null;

        Bitmap scaled = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true);
        bitmapCache.put(cacheKey, scaled);
        return scaled;
    }

    public void drawIcon(Canvas canvas, String filename, float x, float y, int size) {
        Bitmap bitmap = loadBitmap(filename, size, size);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, x - size / 2f, y - size / 2f, null);
        }
    }

    public void drawIconAt(Canvas canvas, String filename, float left, float top, int size) {
        Bitmap bitmap = loadBitmap(filename, size, size);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, left, top, null);
        }
    }

    public void preloadIcons(int size) {
        String[] icons = {
            ICON_DOLLAR, ICON_TOKEN, ICON_POUCH, ICON_LOCK_CLOSED, ICON_LOCK_OPEN,
            ICON_CHARACTER, ICON_PAWN, ICON_AWARD, ICON_FIRE, ICON_SKULL,
            ICON_SHIELD, ICON_HOURGLASS, ICON_APPLE, ICON_HEART
        };

        for (String icon : icons) {
            loadBitmap(icon, size, size);
        }
    }

    public void release() {
        for (Bitmap bitmap : bitmapCache.values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        bitmapCache.clear();
    }
}
