package com.example.juegosostenibilidad;

/**
 * Constantes globales del juego
 */
public class GameConstants {

    // FPS
    public static final int TARGET_FPS = 60;
    public static final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

    // Spawn
    public static final int MAX_TRASH_ON_SCREEN = 25;
    public static final long TRASH_SPAWN_INTERVAL = 2000;
    public static final float SPAWN_PROBABILITY = 0.7f;

    // Tipos de basura
    public static final int TRASH_PLASTIC = 0;
    public static final int TRASH_ORGANIC = 1;
    public static final int TRASH_METAL = 2;
    public static final int TRASH_GLASS = 3;
    public static final int[] TRASH_VALUES = {5, 3, 10, 8};
    public static final float[] TRASH_WEIGHTS = {0.1f, 0.05f, 0.3f, 0.2f};
    public static final String[] TRASH_NAMES = {"Plástico", "Orgánico", "Metal", "Vidrio"};

    // Tipos de limpiadores
    public static final int CLEANER_VOLUNTEER = 0;
    public static final int CLEANER_WORKER = 1;
    public static final int CLEANER_ROBOT = 2;
    public static final float[] CLEANER_SPEEDS = {80f, 150f, 250f};
    public static final int[] CLEANER_COSTS = {50, 200, 500};
    public static final float[] CLEANER_CAPACITY = {1f, 3f, 5f};
    public static final String[] CLEANER_NAMES = {"Voluntario", "Trabajador", "Robot"};

    // Zonas
    public static final int ZONE_BEACH = 0;
    public static final int ZONE_OCEAN = 1;
    public static final int ZONE_REEF = 2;
    public static final int ZONE_DEEP = 3;
    public static final int[] ZONE_UNLOCK_COSTS = {0, 100, 300, 600};
    public static final float[] ZONE_VALUE_MULTIPLIERS = {1f, 1.5f, 2f, 3f};
    public static final String[] ZONE_NAMES = {"Playa", "Océano", "Arrecife", "Mar Profundo"};

    // Colores
    public static final int COLOR_SAND = 0xFFF4D03F;
    public static final int COLOR_SHALLOW_WATER = 0xFF5DADE2;
    public static final int COLOR_DEEP_WATER = 0xFF2874A6;
    public static final int COLOR_SKY = 0xFF85C1E9;
    public static final int COLOR_PLASTIC = 0xFF3498DB;
    public static final int COLOR_ORGANIC = 0xFF27AE60;
    public static final int COLOR_METAL = 0xFF7F8C8D;
    public static final int COLOR_GLASS = 0xFF1ABC9C;
    public static final int COLOR_VOLUNTEER = 0xFFF39C12;
    public static final int COLOR_WORKER = 0xFF8E44AD;
    public static final int COLOR_ROBOT = 0xFFE74C3C;
    public static final int COLOR_UI_BACKGROUND = 0xCC000000;
    public static final int COLOR_UI_TEXT = 0xFFFFFFFF;
    public static final int COLOR_UI_BUTTON = 0xFF2ECC71;
    public static final int COLOR_UI_BUTTON_DISABLED = 0xFF95A5A6;

    // Tamaños
    public static final float TRASH_SIZE = 40f;
    public static final float CLEANER_SIZE = 50f;
    public static final float TOUCH_RADIUS = 60f;

    // Datos educativos
    public static final String[] ECO_FACTS = {
        "Una botella de plástico tarda 450 años en degradarse",
        "El 80% de la basura marina proviene de tierra firme",
        "8 millones de toneladas de plástico llegan al océano cada año",
        "Un solo voluntario puede recoger 20kg de basura por hora",
        "Reciclar 1 lata de aluminio ahorra energía para 3 horas de TV",
        "El vidrio es 100% reciclable infinitas veces",
        "Los microplásticos ya están en el agua que bebemos",
        "Las tortugas confunden bolsas de plástico con medusas",
        "En 2050 habrá más plástico que peces en el océano",
        "Cada minuto se compran 1 millón de botellas de plástico"
    };

    // Guardado
    public static final String PREFS_NAME = "JuegoSostenibilidad";
    public static final String KEY_MONEY = "money";
    public static final String KEY_TOTAL_KG = "total_kg";
    public static final String KEY_TOTAL_ITEMS = "total_items";
    public static final String KEY_CLEANERS = "cleaners";
    public static final String KEY_ZONES = "zones_unlocked";
    public static final String KEY_CURRENT_ZONE = "current_zone";
    public static final String KEY_REBIRTH_COUNT = "rebirth_count";
    public static final String KEY_TOTAL_REBIRTHS = "total_rebirths";

    // Velocidad
    public static final float[] SPEED_MULTIPLIERS = {1f, 2f, 4f};
    public static final String[] SPEED_LABELS = {"x1", "x2", "x4"};

    // Rebirth
    public static final int REBIRTH_COST = 10000;
    public static final float REBIRTH_VALUE_MULTIPLIER = 2.0f;
    public static final float REBIRTH_SPAWN_MULTIPLIER = 1.2f;
}
