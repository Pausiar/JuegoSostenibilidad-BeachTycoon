package com.example.juegosostenibilidad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la carga y renderizado de iconos vectoriales
 */
public class IconManager {

    private static IconManager instance;
    private Context context;
    private Map<Integer, Bitmap> iconCache;

    public static final int ICON_COIN = R.drawable.ic_coin;
    public static final int ICON_TRASH_BAG = R.drawable.ic_trash_bag;
    public static final int ICON_LOCATION = R.drawable.ic_location;
    public static final int ICON_BROOM = R.drawable.ic_broom;
    public static final int ICON_SHOP = R.drawable.ic_shop;
    public static final int ICON_LIGHTBULB = R.drawable.ic_lightbulb;
    public static final int ICON_LOCK = R.drawable.ic_lock;
    public static final int ICON_VOLUNTEER = R.drawable.ic_volunteer;
    public static final int ICON_WORKER = R.drawable.ic_worker;
    public static final int ICON_ROBOT = R.drawable.ic_robot;
    public static final int ICON_STORE = R.drawable.ic_store;
    public static final int ICON_CLOSE = R.drawable.ic_close;
    public static final int ICON_ZONE_BEACH = R.drawable.ic_zone_beach;
    public static final int ICON_ZONE_OCEAN = R.drawable.ic_zone_ocean;
    public static final int ICON_ZONE_REEF = R.drawable.ic_zone_reef;
    public static final int ICON_ZONE_DEEP = R.drawable.ic_zone_deep;
    public static final int ICON_PLASTIC = R.drawable.ic_plastic;
    public static final int ICON_ORGANIC = R.drawable.ic_organic;
    public static final int ICON_METAL = R.drawable.ic_metal;
    public static final int ICON_GLASS = R.drawable.ic_glass;
    public static final int ICON_CHECK = R.drawable.ic_check;
    public static final int ICON_STAR = R.drawable.ic_star;
    public static final int ICON_RECYCLE = R.drawable.ic_recycle;

    private IconManager(Context context) {
        this.context = context.getApplicationContext();
        this.iconCache = new HashMap<>();
    }

    public static synchronized IconManager getInstance(Context context) {
        if (instance == null) {
            instance = new IconManager(context);
        }
        return instance;
    }

    public Bitmap getIcon(int iconResId, int size) {
        int cacheKey = iconResId * 10000 + size;

        if (iconCache.containsKey(cacheKey)) {
            return iconCache.get(cacheKey);
        }

        Bitmap bitmap = createBitmapFromDrawable(iconResId, size, size);
        if (bitmap != null) {
            iconCache.put(cacheKey, bitmap);
        }
        return bitmap;
    }

    public Bitmap getIcon(int iconResId, int width, int height) {
        int cacheKey = iconResId * 10000000 + width * 1000 + height;

        if (iconCache.containsKey(cacheKey)) {
            return iconCache.get(cacheKey);
        }

        Bitmap bitmap = createBitmapFromDrawable(iconResId, width, height);
        if (bitmap != null) {
            iconCache.put(cacheKey, bitmap);
        }
        return bitmap;
    }

    private Bitmap createBitmapFromDrawable(int drawableResId, int width, int height) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
            if (drawable == null) return null;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void drawIcon(Canvas canvas, int iconResId, float x, float y, int size) {
        Bitmap bitmap = getIcon(iconResId, size);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, x - size / 2f, y - size / 2f, null);
        }
    }

    public void drawIconAt(Canvas canvas, int iconResId, float left, float top, int size) {
        Bitmap bitmap = getIcon(iconResId, size);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, left, top, null);
        }
    }

    public void preloadIcons(int defaultSize) {
        int[] icons = {
            ICON_COIN, ICON_TRASH_BAG, ICON_LOCATION, ICON_BROOM,
            ICON_SHOP, ICON_LIGHTBULB, ICON_LOCK, ICON_VOLUNTEER,
            ICON_WORKER, ICON_ROBOT, ICON_STORE, ICON_CLOSE,
            ICON_ZONE_BEACH, ICON_ZONE_OCEAN, ICON_ZONE_REEF, ICON_ZONE_DEEP,
            ICON_PLASTIC, ICON_ORGANIC, ICON_METAL, ICON_GLASS,
            ICON_CHECK, ICON_STAR, ICON_RECYCLE
        };

        for (int iconId : icons) {
            getIcon(iconId, defaultSize);
        }
    }

    public void release() {
        for (Bitmap bitmap : iconCache.values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        iconCache.clear();
    }

    public int getCleanerIcon(int cleanerType) {
        switch (cleanerType) {
            case GameConstants.CLEANER_VOLUNTEER:
                return ICON_VOLUNTEER;
            case GameConstants.CLEANER_WORKER:
                return ICON_WORKER;
            case GameConstants.CLEANER_ROBOT:
                return ICON_ROBOT;
            default:
                return ICON_VOLUNTEER;
        }
    }

    public int getZoneIcon(int zoneType) {
        switch (zoneType) {
            case GameConstants.ZONE_BEACH:
                return ICON_ZONE_BEACH;
            case GameConstants.ZONE_OCEAN:
                return ICON_ZONE_OCEAN;
            case GameConstants.ZONE_REEF:
                return ICON_ZONE_REEF;
            case GameConstants.ZONE_DEEP:
                return ICON_ZONE_DEEP;
            default:
                return ICON_ZONE_BEACH;
        }
    }

    public int getTrashIcon(int trashType) {
        switch (trashType) {
            case GameConstants.TRASH_PLASTIC:
                return ICON_PLASTIC;
            case GameConstants.TRASH_ORGANIC:
                return ICON_ORGANIC;
            case GameConstants.TRASH_METAL:
                return ICON_METAL;
            case GameConstants.TRASH_GLASS:
                return ICON_GLASS;
            default:
                return ICON_PLASTIC;
        }
    }
}
