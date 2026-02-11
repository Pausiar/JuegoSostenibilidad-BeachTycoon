package com.example.juegosostenibilidad;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Objeto de basura en el juego
 */
public class Trash {

    private float x, y;
    private float size;
    private RectF bounds;

    private int type;
    private int value;
    private float weight;
    private int color;
    private int colorDark;
    private int colorLight;

    private boolean active;
    private boolean beingCollected;

    private float animOffset;
    private float animSpeed;
    private float rotationAngle;

    private Paint mainPaint;
    private Paint shadowPaint;
    private Paint highlightPaint;
    private Paint outlinePaint;
    private Paint detailPaint;

    public Trash() {
        bounds = new RectF();

        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0x44000000);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(0x55FFFFFF);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(2.5f);
        outlinePaint.setColor(0xFF1A1A2E);

        detailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        active = false;
        beingCollected = false;
    }

    public void init(float x, float y, int type, float zoneMultiplier) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.size = GameConstants.TRASH_SIZE * 1.2f; // Un poco más grande

        this.value = (int)(GameConstants.TRASH_VALUES[type] * zoneMultiplier);
        this.weight = GameConstants.TRASH_WEIGHTS[type];

        // Colores mejorados con variantes claras y oscuras
        switch (type) {
            case GameConstants.TRASH_PLASTIC:
                this.color = 0xFF4A90D9;
                this.colorDark = 0xFF2E6CB5;
                this.colorLight = 0xFF7AB8F5;
                break;
            case GameConstants.TRASH_ORGANIC:
                this.color = 0xFF4CAF50;
                this.colorDark = 0xFF2E7D32;
                this.colorLight = 0xFF81C784;
                break;
            case GameConstants.TRASH_METAL:
                this.color = 0xFF78909C;
                this.colorDark = 0xFF546E7A;
                this.colorLight = 0xFFB0BEC5;
                break;
            case GameConstants.TRASH_GLASS:
                this.color = 0xFF26C6DA;
                this.colorDark = 0xFF0097A7;
                this.colorLight = 0xFF80DEEA;
                break;
            default:
                this.color = 0xFF4A90D9;
                this.colorDark = 0xFF2E6CB5;
                this.colorLight = 0xFF7AB8F5;
        }

        // Animación aleatoria
        animOffset = (float)(Math.random() * Math.PI * 2);
        animSpeed = 2f + (float)(Math.random() * 1.5f);
        rotationAngle = (float)(Math.random() * 30 - 15); // -15 a +15 grados

        active = true;
        beingCollected = false;
        updateBounds();
    }

    public void update(float deltaTime) {
        if (!active) return;
        animOffset += animSpeed * deltaTime;
        updateBounds();
    }

    public void draw(Canvas canvas) {
        if (!active) return;

        float offsetY = (float)Math.sin(animOffset) * 4;
        float offsetX = (float)Math.cos(animOffset * 0.7f) * 2;
        float scale = 1f + (float)Math.sin(animOffset * 2) * 0.03f;

        canvas.save();
        canvas.translate(x + offsetX, y + offsetY);
        canvas.rotate(rotationAngle + (float)Math.sin(animOffset) * 3);
        canvas.scale(scale, scale);

        // Sombra debajo
        drawShadow(canvas);

        // Dibujar según el tipo
        switch (type) {
            case GameConstants.TRASH_PLASTIC:
                drawPlasticBottle(canvas);
                break;
            case GameConstants.TRASH_ORGANIC:
                drawOrganicWaste(canvas);
                break;
            case GameConstants.TRASH_METAL:
                drawMetalCan(canvas);
                break;
            case GameConstants.TRASH_GLASS:
                drawGlassBottle(canvas);
                break;
        }

        canvas.restore();

        // Indicador de recogida
        if (beingCollected) {
            drawCollectionIndicator(canvas, x + offsetX, y + offsetY);
        }
    }

    private void drawShadow(Canvas canvas) {
        shadowPaint.setColor(0x33000000);
        canvas.drawOval(-size * 0.35f, size * 0.4f, size * 0.35f, size * 0.55f, shadowPaint);
    }

    private void drawCollectionIndicator(Canvas canvas, float cx, float cy) {
        float pulse = (float)Math.abs(Math.sin(animOffset * 3));
        Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(3);
        indicatorPaint.setColor(Color.argb((int)(150 * pulse), 255, 255, 255));
        canvas.drawCircle(cx, cy, size * 0.6f + pulse * 8, indicatorPaint);
    }

    private void drawPlasticBottle(Canvas canvas) {
        // Gradiente para el cuerpo
        LinearGradient bodyGradient = new LinearGradient(
            -size * 0.3f, 0, size * 0.3f, 0,
            colorLight, colorDark, Shader.TileMode.CLAMP
        );
        mainPaint.setShader(bodyGradient);

        // Cuerpo de la botella
        RectF body = new RectF(-size * 0.28f, -size * 0.35f, size * 0.28f, size * 0.38f);
        canvas.drawRoundRect(body, 12, 12, mainPaint);

        // Cuello
        RectF neck = new RectF(-size * 0.14f, -size * 0.55f, size * 0.14f, -size * 0.32f);
        canvas.drawRoundRect(neck, 6, 6, mainPaint);
        mainPaint.setShader(null);

        // Tapón rojo con gradiente
        Paint capPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient capGradient = new LinearGradient(
            -size * 0.12f, -size * 0.7f, size * 0.12f, -size * 0.52f,
            0xFFFF6B6B, 0xFFCC3333, Shader.TileMode.CLAMP
        );
        capPaint.setShader(capGradient);
        RectF cap = new RectF(-size * 0.12f, -size * 0.68f, size * 0.12f, -size * 0.52f);
        canvas.drawRoundRect(cap, 4, 4, capPaint);

        // Outline
        canvas.drawRoundRect(body, 12, 12, outlinePaint);
        canvas.drawRoundRect(neck, 6, 6, outlinePaint);
        canvas.drawRoundRect(cap, 4, 4, outlinePaint);

        // Brillo/reflejo
        highlightPaint.setColor(0x44FFFFFF);
        RectF highlight = new RectF(-size * 0.22f, -size * 0.3f, -size * 0.08f, size * 0.32f);
        canvas.drawRoundRect(highlight, 8, 8, highlightPaint);

        // Etiqueta
        detailPaint.setColor(0xFFFFFFFF);
        RectF label = new RectF(-size * 0.2f, -size * 0.1f, size * 0.2f, size * 0.15f);
        canvas.drawRoundRect(label, 4, 4, detailPaint);

        // Líneas de la etiqueta
        detailPaint.setColor(0xFF2196F3);
        canvas.drawLine(-size * 0.15f, -size * 0.02f, size * 0.15f, -size * 0.02f, detailPaint);
        canvas.drawLine(-size * 0.15f, size * 0.06f, size * 0.1f, size * 0.06f, detailPaint);

        // Símbolo de reciclaje simplificado
        drawRecycleSymbol(canvas, 0, size * 0.25f, size * 0.12f);
    }

    private void drawOrganicWaste(Canvas canvas) {
        // Manzana mordida / residuo orgánico

        // Sombra interior
        RadialGradient organicGradient = new RadialGradient(
            -size * 0.1f, -size * 0.1f, size * 0.5f,
            colorLight, colorDark, Shader.TileMode.CLAMP
        );
        mainPaint.setShader(organicGradient);

        // Cuerpo principal (forma de manzana)
        Path applePath = new Path();
        applePath.moveTo(0, -size * 0.35f);
        applePath.cubicTo(size * 0.4f, -size * 0.35f, size * 0.45f, size * 0.1f, size * 0.3f, size * 0.35f);
        applePath.cubicTo(size * 0.15f, size * 0.45f, -size * 0.15f, size * 0.45f, -size * 0.3f, size * 0.35f);
        applePath.cubicTo(-size * 0.45f, size * 0.1f, -size * 0.4f, -size * 0.35f, 0, -size * 0.35f);
        applePath.close();

        canvas.drawPath(applePath, mainPaint);
        mainPaint.setShader(null);
        canvas.drawPath(applePath, outlinePaint);

        // Mordida
        Paint bitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitePaint.setColor(0xFFFFF8DC); // Color crema interior
        canvas.drawCircle(size * 0.25f, 0, size * 0.18f, bitePaint);

        // Semillas en la mordida
        detailPaint.setColor(0xFF5D4037);
        canvas.drawOval(size * 0.2f, -size * 0.05f, size * 0.26f, size * 0.05f, detailPaint);
        canvas.drawOval(size * 0.28f, size * 0.02f, size * 0.34f, size * 0.1f, detailPaint);

        // Tallo
        detailPaint.setColor(0xFF5D4037);
        detailPaint.setStrokeWidth(3);
        detailPaint.setStyle(Paint.Style.STROKE);
        Path stem = new Path();
        stem.moveTo(0, -size * 0.35f);
        stem.quadTo(size * 0.05f, -size * 0.45f, size * 0.02f, -size * 0.5f);
        canvas.drawPath(stem, detailPaint);
        detailPaint.setStyle(Paint.Style.FILL);

        // Hoja
        Paint leafPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leafPaint.setColor(0xFF66BB6A);
        Path leaf = new Path();
        leaf.moveTo(size * 0.02f, -size * 0.48f);
        leaf.quadTo(size * 0.15f, -size * 0.55f, size * 0.2f, -size * 0.45f);
        leaf.quadTo(size * 0.1f, -size * 0.42f, size * 0.02f, -size * 0.48f);
        canvas.drawPath(leaf, leafPaint);

        // Brillo
        highlightPaint.setColor(0x33FFFFFF);
        canvas.drawOval(-size * 0.25f, -size * 0.3f, -size * 0.05f, -size * 0.1f, highlightPaint);
    }

    private void drawMetalCan(Canvas canvas) {
        // Gradiente metálico
        LinearGradient metalGradient = new LinearGradient(
            -size * 0.35f, 0, size * 0.35f, 0,
            new int[]{colorDark, colorLight, color, colorLight, colorDark},
            new float[]{0f, 0.2f, 0.5f, 0.8f, 1f},
            Shader.TileMode.CLAMP
        );
        mainPaint.setShader(metalGradient);

        // Cuerpo de la lata
        RectF can = new RectF(-size * 0.32f, -size * 0.4f, size * 0.32f, size * 0.35f);
        canvas.drawRoundRect(can, 8, 8, mainPaint);
        mainPaint.setShader(null);

        // Borde superior
        Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setColor(colorLight);
        RectF topRim = new RectF(-size * 0.32f, -size * 0.43f, size * 0.32f, -size * 0.35f);
        canvas.drawRoundRect(topRim, 4, 4, rimPaint);

        // Borde inferior
        RectF bottomRim = new RectF(-size * 0.32f, size * 0.32f, size * 0.32f, size * 0.4f);
        canvas.drawRoundRect(bottomRim, 4, 4, rimPaint);

        // Outline
        canvas.drawRoundRect(can, 8, 8, outlinePaint);
        canvas.drawRoundRect(topRim, 4, 4, outlinePaint);
        canvas.drawRoundRect(bottomRim, 4, 4, outlinePaint);

        // Etiqueta roja
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient labelGradient = new LinearGradient(
            -size * 0.28f, -size * 0.2f, size * 0.28f, size * 0.2f,
            0xFFE53935, 0xFFB71C1C, Shader.TileMode.CLAMP
        );
        labelPaint.setShader(labelGradient);
        RectF label = new RectF(-size * 0.3f, -size * 0.2f, size * 0.3f, size * 0.2f);
        canvas.drawRoundRect(label, 4, 4, labelPaint);

        // Texto en la etiqueta (simulado con líneas)
        detailPaint.setColor(0xFFFFD54F);
        detailPaint.setStrokeWidth(2.5f);
        canvas.drawLine(-size * 0.2f, -size * 0.05f, size * 0.2f, -size * 0.05f, detailPaint);
        canvas.drawLine(-size * 0.15f, size * 0.08f, size * 0.15f, size * 0.08f, detailPaint);

        // Brillo metálico
        highlightPaint.setColor(0x55FFFFFF);
        RectF shine = new RectF(-size * 0.28f, -size * 0.38f, -size * 0.12f, size * 0.3f);
        canvas.drawRoundRect(shine, 4, 4, highlightPaint);

        // Abolladuras (detalles)
        detailPaint.setColor(0x22000000);
        canvas.drawOval(size * 0.1f, size * 0.22f, size * 0.22f, size * 0.3f, detailPaint);
    }

    private void drawGlassBottle(Canvas canvas) {
        // Efecto de vidrio transparente
        Paint glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Gradiente de vidrio
        LinearGradient glassGradient = new LinearGradient(
            -size * 0.25f, 0, size * 0.25f, 0,
            new int[]{0xAA0097A7, 0xDD80DEEA, 0xAA26C6DA, 0xDD80DEEA, 0xAA0097A7},
            new float[]{0f, 0.25f, 0.5f, 0.75f, 1f},
            Shader.TileMode.CLAMP
        );
        glassPaint.setShader(glassGradient);

        // Cuerpo de la botella
        Path bottlePath = new Path();
        bottlePath.moveTo(-size * 0.22f, size * 0.4f);
        bottlePath.lineTo(-size * 0.22f, -size * 0.15f);
        bottlePath.lineTo(-size * 0.12f, -size * 0.25f);
        bottlePath.lineTo(-size * 0.1f, -size * 0.5f);
        bottlePath.lineTo(size * 0.1f, -size * 0.5f);
        bottlePath.lineTo(size * 0.12f, -size * 0.25f);
        bottlePath.lineTo(size * 0.22f, -size * 0.15f);
        bottlePath.lineTo(size * 0.22f, size * 0.4f);
        bottlePath.close();

        canvas.drawPath(bottlePath, glassPaint);
        glassPaint.setShader(null);

        // Borde del cuello
        glassPaint.setColor(colorLight);
        RectF neckRim = new RectF(-size * 0.12f, -size * 0.55f, size * 0.12f, -size * 0.48f);
        canvas.drawRoundRect(neckRim, 4, 4, glassPaint);

        // Base de la botella
        glassPaint.setColor(colorDark);
        glassPaint.setAlpha(180);
        RectF base = new RectF(-size * 0.2f, size * 0.35f, size * 0.2f, size * 0.42f);
        canvas.drawRoundRect(base, 3, 3, glassPaint);

        // Outline
        outlinePaint.setColor(0xFF006064);
        canvas.drawPath(bottlePath, outlinePaint);
        canvas.drawRoundRect(neckRim, 4, 4, outlinePaint);
        outlinePaint.setColor(0xFF1A1A2E);

        // Reflejos de luz (efecto vidrio)
        highlightPaint.setColor(0x66FFFFFF);
        canvas.drawLine(-size * 0.15f, -size * 0.4f, -size * 0.15f, size * 0.3f, highlightPaint);

        highlightPaint.setColor(0x33FFFFFF);
        RectF highlight = new RectF(-size * 0.18f, -size * 0.1f, -size * 0.08f, size * 0.25f);
        canvas.drawRoundRect(highlight, 4, 4, highlightPaint);

        // Líquido residual (opcional, da más realismo)
        Paint liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidPaint.setColor(0x4426C6DA);
        RectF liquid = new RectF(-size * 0.18f, size * 0.15f, size * 0.18f, size * 0.38f);
        canvas.drawRoundRect(liquid, 4, 4, liquidPaint);

        // Burbujas en el líquido
        highlightPaint.setColor(0x55FFFFFF);
        canvas.drawCircle(-size * 0.05f, size * 0.25f, size * 0.03f, highlightPaint);
        canvas.drawCircle(size * 0.08f, size * 0.3f, size * 0.025f, highlightPaint);
    }

    private void drawRecycleSymbol(Canvas canvas, float cx, float cy, float symbolSize) {
        Paint recyclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        recyclePaint.setColor(0xFF4CAF50);
        recyclePaint.setStyle(Paint.Style.STROKE);
        recyclePaint.setStrokeWidth(2);
        recyclePaint.setStrokeCap(Paint.Cap.ROUND);

        // Triángulo simplificado de reciclaje
        Path triangle = new Path();
        float r = symbolSize;
        for (int i = 0; i < 3; i++) {
            float angle = (float)(Math.PI * 2 * i / 3 - Math.PI / 2);
            float x1 = cx + (float)Math.cos(angle) * r;
            float y1 = cy + (float)Math.sin(angle) * r;
            if (i == 0) {
                triangle.moveTo(x1, y1);
            } else {
                triangle.lineTo(x1, y1);
            }
        }
        triangle.close();
        canvas.drawPath(triangle, recyclePaint);

        // Flechitas en las esquinas
        recyclePaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            float angle = (float)(Math.PI * 2 * i / 3 - Math.PI / 6);
            float ax = cx + (float)Math.cos(angle) * r * 0.7f;
            float ay = cy + (float)Math.sin(angle) * r * 0.7f;
            canvas.drawCircle(ax, ay, 2, recyclePaint);
        }
    }

    private void updateBounds() {
        bounds.set(x - size/2, y - size/2, x + size/2, y + size/2);
    }

    public boolean containsPoint(float px, float py) {
        if (!active) return false;
        float dx = px - x;
        float dy = py - y;
        return (dx * dx + dy * dy) <= (GameConstants.TOUCH_RADIUS * GameConstants.TOUCH_RADIUS);
    }

    public float distanceTo(float px, float py) {
        float dx = px - x;
        float dy = py - y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    // Getters y setters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getType() { return type; }
    public int getValue() { return value; }
    public float getWeight() { return weight; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isBeingCollected() { return beingCollected; }
    public void setBeingCollected(boolean beingCollected) { this.beingCollected = beingCollected; }
    public RectF getBounds() { return bounds; }
}
