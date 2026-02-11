package com.example.juegosostenibilidad;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Núcleo de la lógica del juego
 */
public class GameManager {

    private Context context;
    private SharedPreferences prefs;

    private int money;
    private float totalKgCollected;
    private int totalItemsCollected;
    private int currentZone;
    private boolean[] zonesUnlocked;

    private int rebirthCount;
    private float valueMultiplier;
    private float spawnMultiplier;

    private ObjectPool<Trash> trashPool;
    private List<Cleaner> cleaners;

    private Random random;
    private float spawnAccumulator;
    private int screenWidth, screenHeight;
    private float sandLineY;

    private int sessionMoneyEarned;
    private float sessionKgCollected;
    private int sessionItemsCollected;

    private GameEventListener eventListener;
    private SoundManager soundManager;

    public interface GameEventListener {
        void onMoneyChanged(int newMoney);
        void onStatsChanged(float totalKg, int totalItems);
        void onZoneUnlocked(int zone);
        void onFactDisplayed(String fact);
    }

    public GameManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(GameConstants.PREFS_NAME, Context.MODE_PRIVATE);
        this.random = new Random();
        this.cleaners = new CopyOnWriteArrayList<>();
        this.zonesUnlocked = new boolean[4];

        trashPool = new ObjectPool<>(
            () -> new Trash(),
            15,
            GameConstants.MAX_TRASH_ON_SCREEN
        );

        soundManager = new SoundManager(context);
        loadProgress();
    }

    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.sandLineY = height * 0.35f;
    }

    public void update(float deltaTime) {
        for (Trash trash : trashPool.getActiveObjects()) {
            trash.update(deltaTime);
        }

        for (Cleaner cleaner : cleaners) {
            if (cleaner.isActive()) {
                cleaner.update(deltaTime, trashPool);
            }
        }

        trySpawnTrash(deltaTime);
        cleanupInactiveObjects();
    }

    private void trySpawnTrash(float deltaTime) {
        float spawnInterval = (GameConstants.TRASH_SPAWN_INTERVAL / 1000f) / spawnMultiplier;
        spawnAccumulator += deltaTime;

        if (spawnAccumulator >= spawnInterval) {
            spawnAccumulator -= spawnInterval;

            if (random.nextFloat() <= GameConstants.SPAWN_PROBABILITY) {
                if (trashPool.canObtain() &&
                    trashPool.getActiveCount() < GameConstants.MAX_TRASH_ON_SCREEN) {
                    spawnTrash();
                }
            }
        }
    }

    private void spawnTrash() {
        Trash trash = trashPool.obtain();
        if (trash == null) return;

        float x = random.nextFloat() * (screenWidth - 100) + 50;
        float y = sandLineY + random.nextFloat() * (screenHeight - sandLineY - 100) + 50;
        int type = random.nextInt(4);
        float multiplier = GameConstants.ZONE_VALUE_MULTIPLIERS[currentZone] * valueMultiplier;

        trash.init(x, y, type, multiplier);
    }

    private void cleanupInactiveObjects() {
        List<Trash> toRemove = new ArrayList<>();
        for (Trash trash : trashPool.getActiveObjects()) {
            if (!trash.isActive()) {
                toRemove.add(trash);
            }
        }
        for (Trash trash : toRemove) {
            trashPool.free(trash);
        }
    }

    public boolean processTap(float x, float y) {
        for (Trash trash : trashPool.getActiveObjects()) {
            if (trash.isActive() && trash.containsPoint(x, y)) {
                collectTrashManually(trash);
                return true;
            }
        }
        return false;
    }

    private void collectTrashManually(Trash trash) {
        int earned = trash.getValue();
        money += earned;
        sessionMoneyEarned += earned;

        totalKgCollected += trash.getWeight();
        sessionKgCollected += trash.getWeight();
        totalItemsCollected++;
        sessionItemsCollected++;

        trash.setActive(false);
        trash.setBeingCollected(false);

        soundManager.playCollect();

        if (eventListener != null) {
            eventListener.onMoneyChanged(money);
            eventListener.onStatsChanged(totalKgCollected, totalItemsCollected);
        }

        if (random.nextFloat() < 0.1f) {
            showRandomFact();
        }
    }

    public void onTrashCollectedByCleaner(Trash trash) {
        int earned = trash.getValue();
        money += earned;
        sessionMoneyEarned += earned;

        totalKgCollected += trash.getWeight();
        sessionKgCollected += trash.getWeight();
        totalItemsCollected++;
        sessionItemsCollected++;

        soundManager.playCollect();

        if (eventListener != null) {
            eventListener.onMoneyChanged(money);
            eventListener.onStatsChanged(totalKgCollected, totalItemsCollected);
        }
    }

    public boolean buyCleaner(int type) {
        int cost = GameConstants.CLEANER_COSTS[type];

        if (money >= cost) {
            money -= cost;

            Cleaner cleaner = new Cleaner(this);
            float startX = screenWidth / 2f + (random.nextFloat() - 0.5f) * 200;
            float startY = sandLineY - 30;
            float depositX = screenWidth / 2f;
            float depositY = sandLineY - 50;

            cleaner.init(startX, startY, type, depositX, depositY);
            cleaners.add(cleaner);

            soundManager.playPurchase();

            if (eventListener != null) {
                eventListener.onMoneyChanged(money);
            }

            return true;
        }
        return false;
    }

    public boolean unlockZone(int zone) {
        if (zone < 0 || zone >= zonesUnlocked.length) return false;
        if (zonesUnlocked[zone]) return false;

        int cost = GameConstants.ZONE_UNLOCK_COSTS[zone];

        if (money >= cost) {
            money -= cost;
            zonesUnlocked[zone] = true;

            soundManager.playUnlock();

            if (eventListener != null) {
                eventListener.onMoneyChanged(money);
                eventListener.onZoneUnlocked(zone);
            }

            return true;
        }
        return false;
    }

    public boolean changeZone(int zone) {
        if (zone >= 0 && zone < zonesUnlocked.length && zonesUnlocked[zone]) {
            currentZone = zone;
            return true;
        }
        return false;
    }

    private void showRandomFact() {
        String fact = GameConstants.ECO_FACTS[random.nextInt(GameConstants.ECO_FACTS.length)];
        if (eventListener != null) {
            eventListener.onFactDisplayed(fact);
        }
    }

    public void saveProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(GameConstants.KEY_MONEY, money);
        editor.putFloat(GameConstants.KEY_TOTAL_KG, totalKgCollected);
        editor.putInt(GameConstants.KEY_TOTAL_ITEMS, totalItemsCollected);
        editor.putInt(GameConstants.KEY_CURRENT_ZONE, currentZone);
        editor.putInt(GameConstants.KEY_REBIRTH_COUNT, rebirthCount);

        int[] cleanerCounts = new int[3];
        for (Cleaner cleaner : cleaners) {
            if (cleaner.isActive()) {
                cleanerCounts[cleaner.getType()]++;
            }
        }
        editor.putString(GameConstants.KEY_CLEANERS,
            cleanerCounts[0] + "," + cleanerCounts[1] + "," + cleanerCounts[2]);

        StringBuilder zonesStr = new StringBuilder();
        for (int i = 0; i < zonesUnlocked.length; i++) {
            zonesStr.append(zonesUnlocked[i] ? "1" : "0");
            if (i < zonesUnlocked.length - 1) zonesStr.append(",");
        }
        editor.putString(GameConstants.KEY_ZONES, zonesStr.toString());

        editor.apply();
    }

    private void loadProgress() {
        money = prefs.getInt(GameConstants.KEY_MONEY, 0);
        totalKgCollected = prefs.getFloat(GameConstants.KEY_TOTAL_KG, 0);
        totalItemsCollected = prefs.getInt(GameConstants.KEY_TOTAL_ITEMS, 0);
        currentZone = prefs.getInt(GameConstants.KEY_CURRENT_ZONE, 0);
        rebirthCount = prefs.getInt(GameConstants.KEY_REBIRTH_COUNT, 0);

        calculateRebirthMultipliers();

        zonesUnlocked[0] = true;

        String zonesStr = prefs.getString(GameConstants.KEY_ZONES, "1,0,0,0");
        String[] zones = zonesStr.split(",");
        for (int i = 0; i < Math.min(zones.length, zonesUnlocked.length); i++) {
            zonesUnlocked[i] = zones[i].equals("1");
        }
    }

    private void calculateRebirthMultipliers() {
        valueMultiplier = (float) Math.pow(GameConstants.REBIRTH_VALUE_MULTIPLIER, rebirthCount);
        spawnMultiplier = (float) Math.pow(GameConstants.REBIRTH_SPAWN_MULTIPLIER, rebirthCount);
    }

    public boolean canRebirth() {
        return money >= GameConstants.REBIRTH_COST;
    }

    public boolean doRebirth() {
        if (!canRebirth()) return false;

        rebirthCount++;
        calculateRebirthMultipliers();

        money = 0;
        sessionMoneyEarned = 0;
        sessionKgCollected = 0;
        sessionItemsCollected = 0;

        for (int i = 0; i < zonesUnlocked.length; i++) {
            zonesUnlocked[i] = (i == 0);
        }
        currentZone = 0;

        cleaners.clear();

        for (Trash trash : trashPool.getActiveObjects()) {
            trash.setActive(false);
        }

        if (screenWidth > 0 && screenHeight > 0) {
            addFreeVolunteer();
        }

        saveProgress();

        if (eventListener != null) {
            eventListener.onFactDisplayed("¡REBIRTH #" + rebirthCount + "! Valor x" +
                String.format("%.0f", valueMultiplier) + " | Spawn x" +
                String.format("%.1f", spawnMultiplier));
        }

        return true;
    }

    private void addFreeVolunteer() {
        Cleaner cleaner = new Cleaner(this);
        float startX = screenWidth / 2f;
        float startY = sandLineY + 100;
        float depositX = screenWidth / 2f;
        float depositY = sandLineY - 50;

        cleaner.init(startX, startY, GameConstants.CLEANER_VOLUNTEER, depositX, depositY);
        cleaners.add(cleaner);
    }

    public float getValueMultiplier() {
        return valueMultiplier;
    }

    public float getSpawnMultiplier() {
        return spawnMultiplier;
    }

    public int getRebirthCount() {
        return rebirthCount;
    }

    public int getRebirthCost() {
        return GameConstants.REBIRTH_COST;
    }

    public void restoreCleaners() {
        String cleanersStr = prefs.getString(GameConstants.KEY_CLEANERS, "0,0,0");
        String[] counts = cleanersStr.split(",");

        int totalCleaners = 0;
        for (int type = 0; type < Math.min(counts.length, 3); type++) {
            int count = Integer.parseInt(counts[type]);
            totalCleaners += count;
            for (int i = 0; i < count; i++) {
                Cleaner cleaner = new Cleaner(this);
                float startX = screenWidth / 2f + (random.nextFloat() - 0.5f) * 200;
                float startY = sandLineY + random.nextFloat() * 100;
                float depositX = screenWidth / 2f;
                float depositY = sandLineY - 50;

                cleaner.init(startX, startY, type, depositX, depositY);
                cleaners.add(cleaner);
            }
        }

        if (rebirthCount > 0 && totalCleaners == 0) {
            addFreeVolunteer();
        }
    }

    public void release() {
        saveProgress();
        soundManager.release();
    }

    public ObjectPool<Trash> getTrashPool() { return trashPool; }
    public List<Cleaner> getCleaners() { return cleaners; }
    public int getMoney() { return money; }
    public float getTotalKgCollected() { return totalKgCollected; }
    public int getTotalItemsCollected() { return totalItemsCollected; }
    public int getCurrentZone() { return currentZone; }
    public boolean isZoneUnlocked(int zone) {
        return zone >= 0 && zone < zonesUnlocked.length && zonesUnlocked[zone];
    }
    public float getSandLineY() { return sandLineY; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public int getCleanerCount(int type) {
        int count = 0;
        for (Cleaner c : cleaners) {
            if (c.isActive() && c.getType() == type) count++;
        }
        return count;
    }

    public void setEventListener(GameEventListener listener) {
        this.eventListener = listener;
    }
}
