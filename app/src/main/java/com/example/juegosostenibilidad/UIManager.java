package com.example.juegosostenibilidad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Gestiona la interfaz de usuario del juego
 */
public class UIManager {

    private int screenWidth, screenHeight;
    private GameManager gameManager;
    private IconManager iconManager;
    private KenneyAssetManager kenneyAssets;
    private Context context;

    private Paint hudBgPaint;
    private Paint hudTextPaint;
    private Paint moneyPaint;
    private Paint buttonPaint;
    private Paint buttonDisabledPaint;
    private Paint buttonTextPaint;
    private Paint panelPaint;
    private Paint titlePaint;
    private Paint descPaint;

    private RectF shopButton;
    private RectF speedButton;
    private RectF rebirthButton;
    private RectF[] cleanerButtons;
    private RectF[] zoneButtons;
    private RectF closeShopButton;

    private int currentSpeedIndex = 0;
    private float pulseAnim;

    public enum TouchResult {
        NONE,
        SHOP_TOGGLE,
        SPEED_TOGGLE,
        BUY_VOLUNTEER,
        BUY_WORKER,
        BUY_ROBOT,
        ZONE_BEACH,
        ZONE_OCEAN,
        ZONE_REEF,
        ZONE_DEEP,
        CLOSE_SHOP,
        REBIRTH
    }

    public UIManager(Context context, int screenWidth, int screenHeight, GameManager gameManager) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.gameManager = gameManager;
        this.iconManager = IconManager.getInstance(context);
        this.kenneyAssets = KenneyAssetManager.getInstance(context);

        initPaints();
        initButtons();

        // Precargar iconos
        iconManager.preloadIcons(48);
        kenneyAssets.preloadIcons(48);
    }

    private void initPaints() {
        // Obtener fuente de Kenney
        Typeface gameFont = kenneyAssets.getGameFont();

        // Fondo del HUD
        hudBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudBgPaint.setColor(GameConstants.COLOR_UI_BACKGROUND);

        // Texto del HUD
        hudTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudTextPaint.setColor(GameConstants.COLOR_UI_TEXT);
        hudTextPaint.setTextSize(36);
        hudTextPaint.setTypeface(gameFont);

        // Dinero (más grande)
        moneyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        moneyPaint.setColor(0xFFF1C40F); // Amarillo dorado
        moneyPaint.setTextSize(48);
        moneyPaint.setTypeface(gameFont);

        // Botones activos
        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(GameConstants.COLOR_UI_BUTTON);

        // Botones deshabilitados
        buttonDisabledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonDisabledPaint.setColor(GameConstants.COLOR_UI_BUTTON_DISABLED);

        // Texto de botones
        buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(32);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Panel de tienda
        panelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelPaint.setColor(0xEE1A2530);

        // Título
        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(44);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Descripción
        descPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        descPaint.setColor(0xFFBDC3C7);
        descPaint.setTextSize(28);
        descPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initButtons() {
        // Botón de tienda (esquina inferior derecha)
        float shopBtnSize = 120;
        shopButton = new RectF(
            screenWidth - shopBtnSize - 20,
            screenHeight - shopBtnSize - 20,
            screenWidth - 20,
            screenHeight - 20
        );

        // Botón de velocidad (encima del botón de tienda)
        float speedBtnWidth = 100;
        float speedBtnHeight = 60;
        speedButton = new RectF(
            screenWidth - speedBtnWidth - 30,
            screenHeight - shopBtnSize - 90,
            screenWidth - 30,
            screenHeight - shopBtnSize - 30
        );

        // Botones de limpiadores (se muestran en el panel de tienda)
        cleanerButtons = new RectF[3];
        float btnWidth = screenWidth * 0.25f;
        float btnHeight = 180;
        float startX = screenWidth * 0.1f;
        float btnY = screenHeight * 0.35f;
        float gap = (screenWidth * 0.8f - btnWidth * 3) / 2;

        for (int i = 0; i < 3; i++) {
            float x = startX + i * (btnWidth + gap);
            cleanerButtons[i] = new RectF(x, btnY, x + btnWidth, btnY + btnHeight);
        }

        // Botones de zonas
        zoneButtons = new RectF[4];
        float zoneBtnWidth = screenWidth * 0.2f;
        float zoneBtnHeight = 80;
        float zoneStartX = screenWidth * 0.1f;
        float zoneY = screenHeight * 0.65f;
        float zoneGap = (screenWidth * 0.8f - zoneBtnWidth * 4) / 3;

        for (int i = 0; i < 4; i++) {
            float x = zoneStartX + i * (zoneBtnWidth + zoneGap);
            zoneButtons[i] = new RectF(x, zoneY, x + zoneBtnWidth, zoneY + zoneBtnHeight);
        }

        // Botón de cerrar tienda
        float closeBtnSize = 60;
        closeShopButton = new RectF(
            screenWidth - closeBtnSize - 30,
            30,
            screenWidth - 30,
            30 + closeBtnSize
        );

        // Botón de rebirth (en la parte inferior del panel de tienda)
        float rebirthBtnWidth = screenWidth * 0.5f;
        float rebirthBtnHeight = 70;
        rebirthButton = new RectF(
            (screenWidth - rebirthBtnWidth) / 2,
            screenHeight * 0.82f,
            (screenWidth + rebirthBtnWidth) / 2,
            screenHeight * 0.82f + rebirthBtnHeight
        );
    }

    /**
     * Actualiza animaciones de UI
     */
    public void update(float deltaTime) {
        pulseAnim += deltaTime * 3;
    }

    /**
     * Dibuja toda la UI
     */
    public void draw(Canvas canvas, boolean shopOpen) {
        // HUD superior siempre visible
        drawHUD(canvas);

        // Botón de velocidad
        drawSpeedButton(canvas);

        // Botón de tienda
        drawShopButton(canvas);

        // Panel de tienda si está abierto
        if (shopOpen) {
            drawShopPanel(canvas);
        }
    }

    /**
     * Dibuja el HUD superior
     */
    private void drawHUD(Canvas canvas) {
        // Fondo del HUD con gradiente
        RectF hudRect = new RectF(0, 0, screenWidth, 140);
        canvas.drawRect(hudRect, hudBgPaint);

        // Línea decorativa inferior
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFF3498DB);
        linePaint.setStrokeWidth(3);
        canvas.drawLine(0, 137, screenWidth, 137, linePaint);

        // Icono y texto de dinero (usando Kenney dollar)
        int iconSize = 44;
        kenneyAssets.drawIconAt(canvas, KenneyAssetManager.ICON_DOLLAR, 20, 12, iconSize);
        String moneyText = "$" + gameManager.getMoney();
        canvas.drawText(moneyText, 70, 55, moneyPaint);

        // Mostrar multiplicador de rebirth si existe
        int rebirthCount = gameManager.getRebirthCount();
        if (rebirthCount > 0) {
            Paint rebirthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rebirthPaint.setColor(0xFFE040FB); // Púrpura/magenta
            rebirthPaint.setTextSize(24);
            rebirthPaint.setTypeface(Typeface.DEFAULT_BOLD);
            String multiplierText = "x" + (int)gameManager.getValueMultiplier() + " ♻" + rebirthCount;
            float moneyWidth = moneyPaint.measureText(moneyText);
            canvas.drawText(multiplierText, 80 + moneyWidth, 55, rebirthPaint);
        }

        // Icono y estadísticas de basura (usando pouch de Kenney)
        kenneyAssets.drawIconAt(canvas, KenneyAssetManager.ICON_POUCH, 20, 68, iconSize);
        String statsText = String.format("%.1f kg | %d items",
            gameManager.getTotalKgCollected(),
            gameManager.getTotalItemsCollected());
        canvas.drawText(statsText, 70, 110, hudTextPaint);

        // Zona actual - alineado a la derecha para evitar cortes
        String zoneText = GameConstants.ZONE_NAMES[gameManager.getCurrentZone()];
        Paint zoneTextPaint = new Paint(hudTextPaint);
        zoneTextPaint.setTextAlign(Paint.Align.RIGHT);
        float zoneTextWidth = zoneTextPaint.measureText(zoneText);
        // Primero el texto, luego el icono a la izquierda del texto
        canvas.drawText(zoneText, screenWidth - 20, 55, zoneTextPaint);
        iconManager.drawIconAt(canvas, IconManager.ICON_LOCATION, screenWidth - 25 - (int)zoneTextWidth - iconSize, 22, iconSize);

        // Limpiadores activos - alineado a la derecha (usando pawns de Kenney)
        int totalCleaners = gameManager.getCleaners().size();
        String cleanersText = totalCleaners + " limpiadores";
        Paint cleanersTextPaint = new Paint(hudTextPaint);
        cleanersTextPaint.setTextAlign(Paint.Align.RIGHT);
        float cleanersTextWidth = cleanersTextPaint.measureText(cleanersText);
        // Primero el texto, luego el icono a la izquierda del texto
        canvas.drawText(cleanersText, screenWidth - 20, 110, cleanersTextPaint);
        kenneyAssets.drawIconAt(canvas, KenneyAssetManager.ICON_PAWNS, screenWidth - 25 - (int)cleanersTextWidth - iconSize, 77, iconSize);
    }

    /**
     * Dibuja el botón de tienda
     */
    private void drawShopButton(Canvas canvas) {
        // Efecto de pulso
        float scale = 1 + (float)Math.sin(pulseAnim) * 0.05f;
        float cx = shopButton.centerX();
        float cy = shopButton.centerY();
        float halfW = shopButton.width() / 2 * scale;
        float halfH = shopButton.height() / 2 * scale;

        RectF pulsedBtn = new RectF(cx - halfW, cy - halfH, cx + halfW, cy + halfH);

        // Fondo del botón
        Paint shopBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shopBtnPaint.setColor(0xFF9B59B6); // Púrpura
        canvas.drawRoundRect(pulsedBtn, 20, 20, shopBtnPaint);

        // Borde
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(pulsedBtn, 20, 20, borderPaint);

        // Icono de tienda
        int iconSize = (int)(60 * scale);
        iconManager.drawIcon(canvas, IconManager.ICON_SHOP, cx, cy, iconSize);
    }

    /**
     * Dibuja el botón de velocidad
     */
    private void drawSpeedButton(Canvas canvas) {
        // Fondo del botón según velocidad
        Paint speedBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int[] speedColors = {0xFF3498DB, 0xFFF39C12, 0xFFE74C3C}; // Azul, Naranja, Rojo
        speedBtnPaint.setColor(speedColors[currentSpeedIndex]);
        canvas.drawRoundRect(speedButton, 15, 15, speedBtnPaint);

        // Borde
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(speedButton, 15, 15, borderPaint);

        // Texto de velocidad
        Paint speedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedTextPaint.setColor(Color.WHITE);
        speedTextPaint.setTextSize(32);
        speedTextPaint.setTextAlign(Paint.Align.CENTER);
        speedTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        String speedLabel = GameConstants.SPEED_LABELS[currentSpeedIndex];
        canvas.drawText(speedLabel, speedButton.centerX(), speedButton.centerY() + 10, speedTextPaint);
    }

    /**
     * Dibuja el panel completo de la tienda
     */
    private void drawShopPanel(Canvas canvas) {
        // Fondo oscuro semi-transparente
        Paint overlayPaint = new Paint();
        overlayPaint.setColor(0xAA000000);
        canvas.drawRect(0, 0, screenWidth, screenHeight, overlayPaint);

        // Panel principal con borde decorativo
        float panelMargin = 40;
        RectF panel = new RectF(panelMargin, panelMargin,
                                screenWidth - panelMargin, screenHeight - panelMargin);
        canvas.drawRoundRect(panel, 30, 30, panelPaint);

        // Borde del panel
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setColor(0xFF3498DB);
        canvas.drawRoundRect(panel, 30, 30, borderPaint);

        // Icono y título (usando award de Kenney)
        kenneyAssets.drawIcon(canvas, KenneyAssetManager.ICON_AWARD, screenWidth / 2f - 80, panelMargin + 55, 50);
        canvas.drawText("TIENDA", screenWidth / 2f + 10, panelMargin + 70, titlePaint);

        // Dinero disponible
        iconManager.drawIcon(canvas, IconManager.ICON_COIN, screenWidth / 2f - 60, panelMargin + 105, 30);
        String moneyStr = "$" + gameManager.getMoney();
        canvas.drawText(moneyStr, screenWidth / 2f + 20, panelMargin + 120, descPaint);

        // Sección de limpiadores
        canvas.drawText("— LIMPIADORES —", screenWidth / 2f, panelMargin + 180, descPaint);

        drawCleanerButtons(canvas);

        // Sección de zonas
        canvas.drawText("— ZONAS —", screenWidth / 2f, screenHeight * 0.58f, descPaint);

        drawZoneButtons(canvas);

        // Botón de rebirth
        drawRebirthButton(canvas);

        // Botón de cerrar
        drawCloseButton(canvas);
    }

    /**
     * Dibuja los botones de compra de limpiadores
     */
    private void drawCleanerButtons(Canvas canvas) {
        int money = gameManager.getMoney();
        Typeface gameFont = kenneyAssets.getGameFont();

        // Colores de los limpiadores
        int[] colors = {
            GameConstants.COLOR_VOLUNTEER,
            GameConstants.COLOR_WORKER,
            GameConstants.COLOR_ROBOT
        };

        // Iconos de Kenney para limpiadores
        String[] kenneyIcons = {
            KenneyAssetManager.ICON_CHARACTER,
            KenneyAssetManager.ICON_PAWN,
            KenneyAssetManager.ICON_SHIELD
        };

        for (int i = 0; i < 3; i++) {
            RectF btn = cleanerButtons[i];
            int cost = GameConstants.CLEANER_COSTS[i];
            boolean canAfford = money >= cost;

            // Fondo del botón con efecto
            Paint btnBg = new Paint(Paint.ANTI_ALIAS_FLAG);
            btnBg.setColor(canAfford ? colors[i] : 0xFF555555);
            canvas.drawRoundRect(btn, 15, 15, btnBg);

            // Borde
            Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(canAfford ? 4 : 2);
            borderPaint.setColor(canAfford ? 0xFFFFFFFF : 0xFF888888);
            canvas.drawRoundRect(btn, 15, 15, borderPaint);

            // Icono del limpiador (usando Kenney)
            kenneyAssets.drawIcon(canvas, kenneyIcons[i], btn.centerX(), btn.top + 50, 55);

            // Nombre con fuente de Kenney
            Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(Color.WHITE);
            namePaint.setTextSize(24);
            namePaint.setTextAlign(Paint.Align.CENTER);
            namePaint.setTypeface(gameFont);
            canvas.drawText(GameConstants.CLEANER_NAMES[i], btn.centerX(), btn.top + 100, namePaint);

            // Cantidad actual
            int count = gameManager.getCleanerCount(i);
            canvas.drawText("x" + count, btn.centerX(), btn.top + 130, namePaint);

            // Precio con icono de dolar de Kenney
            kenneyAssets.drawIcon(canvas, KenneyAssetManager.ICON_DOLLAR, btn.centerX() - 30, btn.top + 155, 28);
            Paint pricePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pricePaint.setColor(canAfford ? 0xFFF1C40F : 0xFFE74C3C);
            pricePaint.setTextSize(26);
            pricePaint.setTextAlign(Paint.Align.CENTER);
            pricePaint.setTypeface(gameFont);
            canvas.drawText("" + cost, btn.centerX() + 10, btn.top + 165, pricePaint);
        }
    }

    /**
     * Dibuja los botones de zonas
     */
    private void drawZoneButtons(Canvas canvas) {
        int money = gameManager.getMoney();
        int currentZone = gameManager.getCurrentZone();
        Typeface gameFont = kenneyAssets.getGameFont();

        // Colores de las zonas
        int[] colors = {
            GameConstants.COLOR_SAND,
            GameConstants.COLOR_SHALLOW_WATER,
            0xFF1ABC9C, // Arrecife - Turquesa
            GameConstants.COLOR_DEEP_WATER
        };

        // Iconos de zonas (vectores propios)
        int[] zoneIcons = {
            IconManager.ICON_ZONE_BEACH,
            IconManager.ICON_ZONE_OCEAN,
            IconManager.ICON_ZONE_REEF,
            IconManager.ICON_ZONE_DEEP
        };

        for (int i = 0; i < 4; i++) {
            RectF btn = zoneButtons[i];
            boolean isUnlocked = gameManager.isZoneUnlocked(i);
            boolean isCurrentZone = currentZone == i;
            int cost = GameConstants.ZONE_UNLOCK_COSTS[i];
            boolean canAfford = money >= cost || isUnlocked;

            // Fondo del botón
            Paint btnBg = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (isCurrentZone) {
                btnBg.setColor(0xFF2ECC71); // Verde si es zona actual
            } else if (isUnlocked) {
                btnBg.setColor(colors[i]);
            } else if (canAfford) {
                btnBg.setColor(0xFF555555);
            } else {
                btnBg.setColor(0xFF333333);
            }
            canvas.drawRoundRect(btn, 12, 12, btnBg);

            // Borde
            Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(isCurrentZone ? 4 : 2);
            borderPaint.setColor(isCurrentZone ? 0xFFFFFFFF : 0xFFAAAAAA);
            canvas.drawRoundRect(btn, 12, 12, borderPaint);

            // Nombre de zona o candado
            Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(Color.WHITE);
            namePaint.setTextSize(16);
            namePaint.setTextAlign(Paint.Align.CENTER);
            namePaint.setTypeface(gameFont);

            if (isUnlocked) {
                // Dibujar mini icono de zona
                iconManager.drawIcon(canvas, zoneIcons[i], btn.centerX(), btn.centerY() - 8, 32);
                canvas.drawText(GameConstants.ZONE_NAMES[i], btn.centerX(), btn.bottom - 6, namePaint);
            } else {
                // Mostrar candado de Kenney y precio
                kenneyAssets.drawIcon(canvas, KenneyAssetManager.ICON_LOCK_CLOSED, btn.centerX(), btn.centerY() - 8, 30);
                canvas.drawText("$" + cost, btn.centerX(), btn.bottom - 6, namePaint);
            }
        }
    }

    /**
     * Dibuja el botón de rebirth
     */
    private void drawRebirthButton(Canvas canvas) {
        boolean canRebirth = gameManager.canRebirth();
        int rebirthCount = gameManager.getRebirthCount();
        float valueMultiplier = gameManager.getValueMultiplier();

        // Fondo del botón con efecto especial
        Paint btnBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (canRebirth) {
            // Efecto de brillo pulsante cuando se puede hacer rebirth
            float pulse = (float) Math.abs(Math.sin(pulseAnim * 2));
            int r = (int) (180 + 75 * pulse);
            int g = (int) (50 + 50 * pulse);
            int b = (int) (200 + 55 * pulse);
            btnBg.setColor(Color.rgb(r, g, b));
        } else {
            btnBg.setColor(0xFF444444);
        }
        canvas.drawRoundRect(rebirthButton, 20, 20, btnBg);

        // Borde brillante
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(canRebirth ? 4 : 2);
        borderPaint.setColor(canRebirth ? 0xFFFFD700 : 0xFF666666);
        canvas.drawRoundRect(rebirthButton, 20, 20, borderPaint);

        // Texto principal
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(canRebirth ? Color.WHITE : 0xFFAAAAAA);
        textPaint.setTextSize(28);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        String rebirthText = "♻ REBIRTH";
        if (rebirthCount > 0) {
            rebirthText += " #" + (rebirthCount + 1);
        }
        canvas.drawText(rebirthText, rebirthButton.centerX(), rebirthButton.centerY() - 5, textPaint);

        // Texto secundario (costo o multiplicador actual)
        Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setTextSize(18);
        subTextPaint.setTextAlign(Paint.Align.CENTER);

        if (canRebirth) {
            subTextPaint.setColor(0xFF00FF00);
            canvas.drawText("¡DISPONIBLE! → Valor x" + (int)(valueMultiplier * 2),
                rebirthButton.centerX(), rebirthButton.centerY() + 20, subTextPaint);
        } else {
            subTextPaint.setColor(0xFFAAAAAA);
            String progress = "$" + gameManager.getMoney() + " / $" + GameConstants.REBIRTH_COST;
            if (rebirthCount > 0) {
                progress += " | Actual: x" + (int)valueMultiplier;
            }
            canvas.drawText(progress, rebirthButton.centerX(), rebirthButton.centerY() + 20, subTextPaint);
        }
    }

    /**
     * Dibuja el botón de cerrar
     */
    private void drawCloseButton(Canvas canvas) {
        Paint closeBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        closeBg.setColor(0xFFE74C3C);
        canvas.drawRoundRect(closeShopButton, 15, 15, closeBg);

        // Icono de cerrar
        iconManager.drawIcon(canvas, IconManager.ICON_CLOSE,
            closeShopButton.centerX(), closeShopButton.centerY(), 30);
    }

    /**
     * Maneja un toque en la UI
     */
    public TouchResult handleTouch(float x, float y, boolean shopOpen) {
        if (shopOpen) {
            // Verificar botón de cerrar
            if (closeShopButton.contains(x, y)) {
                return TouchResult.CLOSE_SHOP;
            }

            // Verificar botones de limpiadores
            for (int i = 0; i < cleanerButtons.length; i++) {
                if (cleanerButtons[i].contains(x, y)) {
                    switch (i) {
                        case 0: return TouchResult.BUY_VOLUNTEER;
                        case 1: return TouchResult.BUY_WORKER;
                        case 2: return TouchResult.BUY_ROBOT;
                    }
                }
            }

            // Verificar botones de zonas
            for (int i = 0; i < zoneButtons.length; i++) {
                if (zoneButtons[i].contains(x, y)) {
                    switch (i) {
                        case 0: return TouchResult.ZONE_BEACH;
                        case 1: return TouchResult.ZONE_OCEAN;
                        case 2: return TouchResult.ZONE_REEF;
                        case 3: return TouchResult.ZONE_DEEP;
                    }
                }
            }

            // Verificar botón de rebirth
            if (rebirthButton.contains(x, y)) {
                return TouchResult.REBIRTH;
            }

            return TouchResult.NONE;
        } else {
            // Verificar botón de velocidad
            if (speedButton.contains(x, y)) {
                // Ciclar al siguiente índice de velocidad
                currentSpeedIndex = (currentSpeedIndex + 1) % GameConstants.SPEED_MULTIPLIERS.length;
                return TouchResult.SPEED_TOGGLE;
            }

            // Verificar botón de tienda
            if (shopButton.contains(x, y)) {
                return TouchResult.SHOP_TOGGLE;
            }
        }

        return TouchResult.NONE;
    }

    /**
     * Obtiene el multiplicador de velocidad actual
     */
    public float getSpeedMultiplier() {
        return GameConstants.SPEED_MULTIPLIERS[currentSpeedIndex];
    }

    /**
     * Obtiene el índice de velocidad actual
     */
    public int getSpeedIndex() {
        return currentSpeedIndex;
    }
}
