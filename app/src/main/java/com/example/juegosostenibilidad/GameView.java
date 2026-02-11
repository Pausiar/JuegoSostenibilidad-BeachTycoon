package com.example.juegosostenibilidad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.io.InputStream;

/**
 * GameView - Vista principal del juego con SurfaceView
 *
 * Contiene el game loop, renderizado y manejo de input.
 * Usa doble buffer para renderizado suave a 60 FPS.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // Thread y control del game loop
    private Thread gameThread;
    private volatile boolean running;
    private SurfaceHolder holder;

    // Managers
    private GameManager gameManager;
    private UIManager uiManager;
    private IconManager iconManager;

    // Renderizado
    private Paint backgroundPaint;
    private Paint sandPaint;
    private Paint waterPaint;
    private Paint textPaint;

    // Gradientes para el fondo
    private LinearGradient skyGradient;
    private LinearGradient waterGradient;

    // Sprite de fondo
    private Bitmap beachBackground;
    private Bitmap scaledBackground;
    private Rect backgroundSrcRect;
    private Rect backgroundDstRect;

    // Dimensiones
    private int screenWidth, screenHeight;
    private boolean surfaceReady;

    // Timing
    private long lastFrameTime;
    private float deltaTime;
    private int fps;
    private long fpsTimer;
    private int frameCount;

    // Estado del menú de tienda
    private boolean shopOpen;

    // Mensaje de dato educativo
    private String currentFact;
    private long factDisplayTime;
    private static final long FACT_DISPLAY_DURATION = 4000; // 4 segundos

    public GameView(Context context) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);

        // Inicializar GameManager
        gameManager = new GameManager(context);

        // Inicializar IconManager
        iconManager = IconManager.getInstance(context);

        // Configurar listener de eventos
        gameManager.setEventListener(new GameManager.GameEventListener() {
            @Override
            public void onMoneyChanged(int newMoney) {
                // UI se actualiza automáticamente
            }

            @Override
            public void onStatsChanged(float totalKg, int totalItems) {
                // UI se actualiza automáticamente
            }

            @Override
            public void onZoneUnlocked(int zone) {
                currentFact = "¡Zona desbloqueada: " + GameConstants.ZONE_NAMES[zone] + "!";
                factDisplayTime = System.currentTimeMillis();
            }

            @Override
            public void onFactDisplayed(String fact) {
                currentFact = fact;
                factDisplayTime = System.currentTimeMillis();
            }
        });

        // Inicializar paints
        initPaints();

        surfaceReady = false;
        shopOpen = false;
        currentFact = null;

        setFocusable(true);
    }

    private void initPaints() {
        backgroundPaint = new Paint();
        sandPaint = new Paint();
        sandPaint.setColor(GameConstants.COLOR_SAND);

        waterPaint = new Paint();
        waterPaint.setColor(GameConstants.COLOR_SHALLOW_WATER);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Cargar sprite de fondo
        loadBackgroundSprite();
    }

    /**
     * Carga el sprite de fondo de la playa
     */
    private void loadBackgroundSprite() {
        try {
            InputStream is = getContext().getAssets().open("beach_background.png");
            beachBackground = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            beachBackground = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Surface creada, pero esperamos a surfaceChanged para tener dimensiones
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;

        // Configurar GameManager con dimensiones
        gameManager.setScreenDimensions(width, height);
        gameManager.restoreCleaners();

// Crear UIManager con contexto
        uiManager = new UIManager(getContext(), width, height, gameManager);

        // Configurar IconManager para Cleaner
        Cleaner.setIconManager(iconManager);

        // Crear gradientes
        createGradients();

        // Preparar fondo escalado
        prepareScaledBackground();

        surfaceReady = true;

        // Iniciar el game loop si no está corriendo
        if (gameThread == null || !running) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        surfaceReady = false;

        // Esperar a que termine el thread
        boolean retry = true;
        while (retry) {
            try {
                if (gameThread != null) {
                    gameThread.join();
                }
                retry = false;
            } catch (InterruptedException e) {
                // Reintentar
            }
        }
    }

    private void createGradients() {
        float sandLineY = gameManager.getSandLineY();

        // Gradiente del cielo (arriba)
        skyGradient = new LinearGradient(
            0, 0, 0, sandLineY * 0.5f,
            GameConstants.COLOR_SKY,
            0xFFAED6F1,
            Shader.TileMode.CLAMP
        );

        // Gradiente del agua
        waterGradient = new LinearGradient(
            0, sandLineY, 0, screenHeight,
            GameConstants.COLOR_SHALLOW_WATER,
            GameConstants.COLOR_DEEP_WATER,
            Shader.TileMode.CLAMP
        );
    }

    /**
     * Prepara el fondo escalado a las dimensiones de pantalla
     */
    private void prepareScaledBackground() {
        if (beachBackground != null) {
            // Escalar el fondo para cubrir toda la pantalla
            scaledBackground = Bitmap.createScaledBitmap(
                beachBackground, screenWidth, screenHeight, true);
            backgroundSrcRect = new Rect(0, 0, scaledBackground.getWidth(), scaledBackground.getHeight());
            backgroundDstRect = new Rect(0, 0, screenWidth, screenHeight);
        }
    }

    /**
     * Game Loop principal
     */
    @Override
    public void run() {
        lastFrameTime = System.nanoTime();
        fpsTimer = System.currentTimeMillis();
        frameCount = 0;

        while (running) {
            if (!surfaceReady) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Ignorar
                }
                continue;
            }

            // Calcular delta time
            long currentTime = System.nanoTime();
            deltaTime = (currentTime - lastFrameTime) / 1000000000f; // Convertir a segundos
            lastFrameTime = currentTime;

            // Limitar deltaTime para evitar saltos grandes
            if (deltaTime > 0.1f) deltaTime = 0.1f;

            // Update
            update(deltaTime);

            // Render
            render();

            // Calcular FPS
            frameCount++;
            if (System.currentTimeMillis() - fpsTimer >= 1000) {
                fps = frameCount;
                frameCount = 0;
                fpsTimer = System.currentTimeMillis();
            }

            // Control de FPS
            long sleepTime = (GameConstants.OPTIMAL_TIME - (System.nanoTime() - currentTime)) / 1000000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // Ignorar
                }
            }
        }
    }

    /**
     * Actualiza la lógica del juego
     */
    private void update(float deltaTime) {
        float speedMultiplier = 1f;
        if (uiManager != null) {
            speedMultiplier = uiManager.getSpeedMultiplier();
        }
        float adjustedDeltaTime = deltaTime * speedMultiplier;

        if (!shopOpen) {
            gameManager.update(adjustedDeltaTime);
        }

        if (uiManager != null) {
            uiManager.update(deltaTime);
        }

        if (currentFact != null &&
            System.currentTimeMillis() - factDisplayTime > FACT_DISPLAY_DURATION) {
            currentFact = null;
        }
    }

    private void render() {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                synchronized (holder) {
                    drawGame(canvas);
                }
            }
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawGame(Canvas canvas) {
        drawBackground(canvas);

        for (Trash trash : gameManager.getTrashPool().getActiveObjects()) {
            trash.draw(canvas);
        }

        for (Cleaner cleaner : gameManager.getCleaners()) {
            cleaner.draw(canvas);
        }

        if (uiManager != null) {
            uiManager.draw(canvas, shopOpen);
        }

        if (currentFact != null) {
            drawFactMessage(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        float sandLineY = gameManager.getSandLineY();

        if (scaledBackground != null) {
            canvas.drawBitmap(scaledBackground, backgroundSrcRect, backgroundDstRect, null);

            Paint waterOverlayPaint = new Paint();
            LinearGradient waterOverlay = new LinearGradient(
                0, sandLineY * 0.8f, 0, screenHeight,
                0x00000080,
                0xAA1E90FF,
                Shader.TileMode.CLAMP
            );
            waterOverlayPaint.setShader(waterOverlay);
            canvas.drawRect(0, sandLineY * 0.8f, screenWidth, screenHeight, waterOverlayPaint);

        } else {
            backgroundPaint.setShader(skyGradient);
            canvas.drawRect(0, 0, screenWidth, sandLineY * 0.7f, backgroundPaint);
            backgroundPaint.setShader(null);

            canvas.drawRect(0, sandLineY * 0.5f, screenWidth, sandLineY, sandPaint);

            backgroundPaint.setShader(waterGradient);
            canvas.drawRect(0, sandLineY, screenWidth, screenHeight, backgroundPaint);
            backgroundPaint.setShader(null);
        }

        drawWaves(canvas, sandLineY);
        drawWaterDetails(canvas, sandLineY);
    }

    private void drawWaves(Canvas canvas, float sandLineY) {
        float time = System.currentTimeMillis() / 1000f;
        Paint foamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        for (int i = 0; i < 4; i++) {
            float waveY = sandLineY + i * 20 - 5;
            float offset = (float) Math.sin(time * 1.5f + i * 0.8f) * 15;
            int alpha = 180 - i * 40;

            foamPaint.setColor(Color.argb(alpha, 255, 255, 255));
            foamPaint.setStyle(Paint.Style.FILL);

            android.graphics.Path wavePath = new android.graphics.Path();
            wavePath.moveTo(0, waveY + 20);

            for (float x = 0; x <= screenWidth + 50; x += 30) {
                float y = waveY + (float) Math.sin((x + offset + time * 40) * 0.03) * (10 - i * 2);
                if (x == 0) {
                    wavePath.moveTo(x, y);
                } else {
                    wavePath.lineTo(x, y);
                }
            }

            wavePath.lineTo(screenWidth, waveY + 25);
            wavePath.lineTo(0, waveY + 25);
            wavePath.close();

            canvas.drawPath(wavePath, foamPaint);
        }
    }

    private void drawWaterDetails(Canvas canvas, float sandLineY) {
        float time = System.currentTimeMillis() / 1000f;

        Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shinePaint.setColor(0x33FFFFFF);

        for (int i = 0; i < 6; i++) {
            float x = (i * 180 + time * 25) % screenWidth;
            float y = sandLineY + 120 + i * 70;
            float size = 40 + (float) Math.sin(time + i) * 15;

            canvas.drawOval(x - size, y - size * 0.25f, x + size, y + size * 0.25f, shinePaint);
        }

        Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparklePaint.setColor(0x55FFFFFF);

        for (int i = 0; i < 12; i++) {
            float sparkleTime = time + i * 0.5f;
            float alpha = (float) Math.abs(Math.sin(sparkleTime * 2)) * 0.6f;
            sparklePaint.setAlpha((int)(alpha * 255));

            float x = (i * 90 + 50) % screenWidth;
            float y = sandLineY + 80 + (i % 4) * 100;
            float size = 8 + (float) Math.sin(sparkleTime * 3) * 4;

            canvas.drawCircle(x, y, size, sparklePaint);
        }
    }

    private void drawFactMessage(Canvas canvas) {
        if (currentFact == null) return;

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xDD2C3E50);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float padding = 30;
        float maxWidth = screenWidth * 0.8f;
        float iconSpace = 50;

        String[] words = currentFact.split(" ");
        StringBuilder line = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            String testLine = line.length() > 0 ? line + " " + word : word;
            if (textPaint.measureText(testLine) > maxWidth - iconSpace) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }

        float lineHeight = 45;
        float boxHeight = lines.size() * lineHeight + padding * 2;
        float boxY = screenHeight * 0.7f;

        RectF box = new RectF(
            screenWidth * 0.05f, boxY,
            screenWidth * 0.95f, boxY + boxHeight
        );
        canvas.drawRoundRect(box, 20, 20, bgPaint);

        iconManager.drawIcon(canvas, IconManager.ICON_LIGHTBULB,
            box.left + padding + 20, boxY + boxHeight / 2, 40);

        float textY = boxY + padding + 30;
        float textX = screenWidth / 2f + 25;
        for (String l : lines) {
            canvas.drawText(l, textX, textY, textPaint);
            textY += lineHeight;
        }
    }

    private void drawDebugInfo(Canvas canvas) {
        Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint.setColor(0xAAFFFFFF);
        debugPaint.setTextSize(30);

        canvas.drawText("FPS: " + fps, 20, screenHeight - 100, debugPaint);
        canvas.drawText("Basura: " + gameManager.getTrashPool().getActiveCount(), 20, screenHeight - 60, debugPaint);
        canvas.drawText("Limpiadores: " + gameManager.getCleaners().size(), 20, screenHeight - 20, debugPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            if (uiManager != null) {
                UIManager.TouchResult result = uiManager.handleTouch(x, y, shopOpen);

                switch (result) {
                    case SHOP_TOGGLE:
                        shopOpen = !shopOpen;
                        return true;
                    case BUY_VOLUNTEER:
                        gameManager.buyCleaner(GameConstants.CLEANER_VOLUNTEER);
                        return true;
                    case BUY_WORKER:
                        gameManager.buyCleaner(GameConstants.CLEANER_WORKER);
                        return true;
                    case BUY_ROBOT:
                        gameManager.buyCleaner(GameConstants.CLEANER_ROBOT);
                        return true;
                    case ZONE_BEACH:
                        if (gameManager.isZoneUnlocked(0)) gameManager.changeZone(0);
                        else gameManager.unlockZone(0);
                        return true;
                    case ZONE_OCEAN:
                        if (gameManager.isZoneUnlocked(1)) gameManager.changeZone(1);
                        else gameManager.unlockZone(1);
                        return true;
                    case ZONE_REEF:
                        if (gameManager.isZoneUnlocked(2)) gameManager.changeZone(2);
                        else gameManager.unlockZone(2);
                        return true;
                    case ZONE_DEEP:
                        if (gameManager.isZoneUnlocked(3)) gameManager.changeZone(3);
                        else gameManager.unlockZone(3);
                        return true;
                    case CLOSE_SHOP:
                        shopOpen = false;
                        return true;
                    case REBIRTH:
                        if (gameManager.canRebirth()) {
                            gameManager.doRebirth();
                            shopOpen = false;
                        }
                        return true;
                    case SPEED_TOGGLE:
                        return true;
                    case NONE:
                        break;
                }
            }

            if (shopOpen) {
                return true;
            }

            gameManager.processTap(x, y);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void pause() {
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
            }
        }
        gameManager.saveProgress();
    }

    public void resume() {
        if (!running && surfaceReady) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void release() {
        running = false;
        gameManager.release();
    }
}
