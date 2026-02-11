package com.example.juegosostenibilidad;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Limpiador automático con IA
 */
public class Cleaner {

    private float x, y;
    private float targetX, targetY;
    private float velocityX, velocityY;
    private float size;

    private int type;
    private float speed;
    private float capacity;
    private float currentLoad;
    private int color;

    private boolean active;
    private Trash targetTrash;
    private boolean returning;

    private float depositX, depositY;

    private float rotation;
    private float pulseAnim;

    private Paint bodyPaint;
    private Paint detailPaint;
    private Paint eyePaint;

    private GameManager gameManager;
    private static IconManager iconManager;

    public static void setIconManager(IconManager manager) {
        iconManager = manager;
    }

    public Cleaner(GameManager manager) {
        this.gameManager = manager;
        bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        detailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        detailPaint.setStyle(Paint.Style.STROKE);
        detailPaint.setStrokeWidth(3);
        detailPaint.setColor(0xFF2C3E50);
        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(0xFF2C3E50);
        active = false;
    }

    public void init(float x, float y, int type, float depositX, float depositY) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.depositX = depositX;
        this.depositY = depositY;
        this.size = GameConstants.CLEANER_SIZE;

        this.speed = GameConstants.CLEANER_SPEEDS[type];
        this.capacity = GameConstants.CLEANER_CAPACITY[type];
        this.currentLoad = 0;

        switch (type) {
            case GameConstants.CLEANER_VOLUNTEER:
                this.color = GameConstants.COLOR_VOLUNTEER;
                break;
            case GameConstants.CLEANER_WORKER:
                this.color = GameConstants.COLOR_WORKER;
                break;
            case GameConstants.CLEANER_ROBOT:
                this.color = GameConstants.COLOR_ROBOT;
                break;
            default:
                this.color = GameConstants.COLOR_VOLUNTEER;
        }

        bodyPaint.setColor(color);

        active = true;
        targetTrash = null;
        returning = false;
        rotation = 0;
        pulseAnim = 0;
    }

    public void update(float deltaTime, ObjectPool<Trash> trashPool) {
        if (!active) return;

        pulseAnim += deltaTime * 5;

        if (returning) {
            moveTowards(depositX, depositY, deltaTime);

            if (distanceTo(depositX, depositY) < 30) {
                currentLoad = 0;
                returning = false;
            }
            return;
        }

        if (targetTrash == null || !targetTrash.isActive()) {
            targetTrash = findNearestTrash(trashPool);
            if (targetTrash != null) {
                targetTrash.setBeingCollected(true);
            }
        }

        if (targetTrash != null) {
            moveTowards(targetTrash.getX(), targetTrash.getY(), deltaTime);

            if (distanceTo(targetTrash.getX(), targetTrash.getY()) < 25) {
                collectTrash();
            }
        } else {
            patrol(deltaTime);
        }
    }

    private Trash findNearestTrash(ObjectPool<Trash> trashPool) {
        Trash nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Trash trash : trashPool.getActiveObjects()) {
            if (trash.isActive() && !trash.isBeingCollected()) {
                float dist = distanceTo(trash.getX(), trash.getY());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = trash;
                }
            }
        }

        return nearest;
    }

    private void moveTowards(float tx, float ty, float deltaTime) {
        float dx = tx - x;
        float dy = ty - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance > 5) {
            float nx = dx / distance;
            float ny = dy / distance;

            x += nx * speed * deltaTime;
            y += ny * speed * deltaTime;
            rotation = (float)Math.atan2(dy, dx);
        }
    }

    private void patrol(float deltaTime) {
        float targetY = depositY + 200 + (float)(Math.sin(pulseAnim * 0.5) * 100);
        float targetX = x + (float)(Math.cos(pulseAnim * 0.3) * 2);

        moveTowards(targetX, targetY, deltaTime * 0.3f);
    }

    private void collectTrash() {
        if (targetTrash == null) return;

        currentLoad += targetTrash.getWeight();
        gameManager.onTrashCollectedByCleaner(targetTrash);

        targetTrash.setActive(false);
        targetTrash.setBeingCollected(false);
        targetTrash = null;

        if (currentLoad >= capacity) {
            returning = true;
        }
    }

    public void draw(Canvas canvas) {
        if (!active) return;

        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0x33000000);
        canvas.drawOval(x - size * 0.3f, y + size * 0.35f,
                       x + size * 0.3f, y + size * 0.45f, shadowPaint);

        canvas.save();
        canvas.translate(x, y);

        switch (type) {
            case GameConstants.CLEANER_VOLUNTEER:
                drawVolunteer(canvas);
                break;
            case GameConstants.CLEANER_WORKER:
                drawWorker(canvas);
                break;
            case GameConstants.CLEANER_ROBOT:
                drawRobot(canvas);
                break;
        }

        canvas.restore();

        if (currentLoad > 0) {
            drawLoadIndicator(canvas);
        }

        if (returning) {
            Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            arrowPaint.setColor(0xFF2ECC71);
            arrowPaint.setStyle(Paint.Style.FILL);
            Path arrow = new Path();
            arrow.moveTo(x, y - size * 0.7f);
            arrow.lineTo(x - 8, y - size * 0.5f);
            arrow.lineTo(x + 8, y - size * 0.5f);
            arrow.close();
            canvas.drawPath(arrow, arrowPaint);
        }
    }


    private void drawVolunteer(Canvas canvas) {
        float pulse = 1 + (float)Math.sin(pulseAnim) * 0.05f;
        float s = size * pulse;

        float walkCycle = (float)Math.sin(pulseAnim * 4) * 0.08f;
        float armSwing = (float)Math.sin(pulseAnim * 4) * 0.15f;

        Paint pantsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pantsPaint.setColor(0xFF3D5A80);

        canvas.save();
        canvas.rotate(walkCycle * 20, -s * 0.1f, s * 0.1f);
        canvas.drawRoundRect(-s * 0.18f, s * 0.1f, -s * 0.05f, s * 0.38f, 5, 5, pantsPaint);
        Paint shoesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shoesPaint.setColor(0xFF5D4E37);
        canvas.drawRoundRect(-s * 0.2f, s * 0.32f, -s * 0.02f, s * 0.42f, 4, 4, shoesPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(-walkCycle * 20, s * 0.1f, s * 0.1f);
        canvas.drawRoundRect(s * 0.05f, s * 0.1f, s * 0.18f, s * 0.38f, 5, 5, pantsPaint);
        canvas.drawRoundRect(s * 0.02f, s * 0.32f, s * 0.2f, s * 0.42f, 4, 4, shoesPaint);
        canvas.restore();

        Paint shirtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shirtPaint.setColor(0xFF4CAF50);
        RectF torsoRect = new RectF(-s * 0.22f, -s * 0.12f, s * 0.22f, s * 0.15f);
        canvas.drawRoundRect(torsoRect, 8, 8, shirtPaint);

        Paint collarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        collarPaint.setColor(0xFF388E3C);
        canvas.drawRoundRect(-s * 0.1f, -s * 0.15f, s * 0.1f, -s * 0.08f, 3, 3, collarPaint);

        Paint ecoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ecoPaint.setColor(0xFFFFFFFF);
        ecoPaint.setTextSize(s * 0.2f);
        ecoPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("♻", 0, s * 0.08f, ecoPaint);

        Paint skinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skinPaint.setColor(0xFFFFDBB4);

        canvas.save();
        canvas.rotate(-armSwing * 30, -s * 0.22f, -s * 0.05f);
        Paint sleevePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sleevePaint.setColor(0xFF4CAF50);
        canvas.drawRoundRect(-s * 0.32f, -s * 0.1f, -s * 0.2f, s * 0.02f, 4, 4, sleevePaint);
        canvas.drawRoundRect(-s * 0.34f, s * 0.0f, -s * 0.22f, s * 0.15f, 4, 4, skinPaint);
        canvas.drawCircle(-s * 0.28f, s * 0.18f, s * 0.06f, skinPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(armSwing * 30, s * 0.22f, -s * 0.05f);
        canvas.drawRoundRect(s * 0.2f, -s * 0.1f, s * 0.32f, s * 0.02f, 4, 4, sleevePaint);
        canvas.drawRoundRect(s * 0.22f, s * 0.0f, s * 0.34f, s * 0.15f, 4, 4, skinPaint);
        canvas.drawCircle(s * 0.28f, s * 0.18f, s * 0.06f, skinPaint);
        canvas.restore();

        canvas.drawRoundRect(-s * 0.06f, -s * 0.2f, s * 0.06f, -s * 0.1f, 3, 3, skinPaint);
        canvas.drawCircle(0, -s * 0.32f, s * 0.18f, skinPaint);
        canvas.drawCircle(-s * 0.17f, -s * 0.32f, s * 0.04f, skinPaint);
        canvas.drawCircle(s * 0.17f, -s * 0.32f, s * 0.04f, skinPaint);

        Paint hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(0xFF5D4037);
        Path hair = new Path();
        hair.moveTo(-s * 0.16f, -s * 0.35f);
        hair.quadTo(-s * 0.18f, -s * 0.52f, 0, -s * 0.54f);
        hair.quadTo(s * 0.18f, -s * 0.52f, s * 0.16f, -s * 0.35f);
        hair.quadTo(s * 0.1f, -s * 0.42f, 0, -s * 0.44f);
        hair.quadTo(-s * 0.1f, -s * 0.42f, -s * 0.16f, -s * 0.35f);
        canvas.drawPath(hair, hairPaint);

        float blinkPhase = (float)Math.sin(pulseAnim * 0.5f);
        float eyeHeight = blinkPhase > 0.95f ? 0.01f : 0.05f;

        Paint eyeWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhite.setColor(0xFFFFFFFF);
        canvas.drawOval(-s * 0.12f, -s * 0.38f, -s * 0.02f, -s * 0.38f + s * eyeHeight * 2, eyeWhite);
        canvas.drawOval(s * 0.02f, -s * 0.38f, s * 0.12f, -s * 0.38f + s * eyeHeight * 2, eyeWhite);

        float lookDir = rotation > 0 ? 0.02f : -0.02f;
        eyePaint.setColor(0xFF2C3E50);
        if (blinkPhase <= 0.95f) {
            canvas.drawCircle(-s * 0.07f + s * lookDir, -s * 0.34f, s * 0.025f, eyePaint);
            canvas.drawCircle(s * 0.07f + s * lookDir, -s * 0.34f, s * 0.025f, eyePaint);

            Paint eyeShine = new Paint(Paint.ANTI_ALIAS_FLAG);
            eyeShine.setColor(0xFFFFFFFF);
            canvas.drawCircle(-s * 0.08f + s * lookDir, -s * 0.35f, s * 0.01f, eyeShine);
            canvas.drawCircle(s * 0.06f + s * lookDir, -s * 0.35f, s * 0.01f, eyeShine);
        }

        Paint browPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        browPaint.setColor(0xFF5D4037);
        browPaint.setStrokeWidth(2);
        browPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(-s * 0.12f, -s * 0.42f, -s * 0.03f, -s * 0.43f, browPaint);
        canvas.drawLine(s * 0.03f, -s * 0.43f, s * 0.12f, -s * 0.42f, browPaint);

        Paint smilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smilePaint.setColor(0xFFD32F2F);
        smilePaint.setStyle(Paint.Style.STROKE);
        smilePaint.setStrokeWidth(2.5f);
        smilePaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(-s * 0.1f, -s * 0.32f, s * 0.1f, -s * 0.2f, 15, 150, false, smilePaint);

        Paint blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setColor(0x44FF6B6B);
        canvas.drawOval(-s * 0.16f, -s * 0.3f, -s * 0.08f, -s * 0.24f, blushPaint);
        canvas.drawOval(s * 0.08f, -s * 0.3f, s * 0.16f, -s * 0.24f, blushPaint);

        Paint nosePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nosePaint.setColor(0xFFEEC9A0);
        canvas.drawOval(-s * 0.025f, -s * 0.32f, s * 0.025f, -s * 0.28f, nosePaint);
    }

    private void drawWorker(Canvas canvas) {
        float pulse = 1 + (float)Math.sin(pulseAnim) * 0.03f;
        float s = size * pulse;

        float walkCycle = (float)Math.sin(pulseAnim * 3.5f) * 0.1f;
        float armSwing = (float)Math.sin(pulseAnim * 3.5f) * 0.12f;

        canvas.save();
        canvas.rotate(walkCycle * 18, -s * 0.1f, s * 0.1f);
        Paint pantsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pantsPaint.setColor(0xFF455A64);
        canvas.drawRoundRect(-s * 0.2f, s * 0.05f, -s * 0.05f, s * 0.32f, 5, 5, pantsPaint);
        Paint bootPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bootPaint.setColor(0xFF37474F);
        canvas.drawRoundRect(-s * 0.22f, s * 0.26f, -s * 0.03f, s * 0.42f, 5, 5, bootPaint);
        Paint solePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        solePaint.setColor(0xFFFFC107);
        canvas.drawRoundRect(-s * 0.22f, s * 0.38f, -s * 0.03f, s * 0.42f, 2, 2, solePaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(-walkCycle * 18, s * 0.1f, s * 0.1f);
        canvas.drawRoundRect(s * 0.05f, s * 0.05f, s * 0.2f, s * 0.32f, 5, 5, pantsPaint);
        canvas.drawRoundRect(s * 0.03f, s * 0.26f, s * 0.22f, s * 0.42f, 5, 5, bootPaint);
        canvas.drawRoundRect(s * 0.03f, s * 0.38f, s * 0.22f, s * 0.42f, 2, 2, solePaint);
        canvas.restore();

        Paint vestPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vestPaint.setColor(0xFFFF9800);
        RectF vestRect = new RectF(-s * 0.26f, -s * 0.15f, s * 0.26f, s * 0.12f);
        canvas.drawRoundRect(vestRect, 8, 8, vestPaint);

        Paint stripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stripePaint.setColor(0xFFE0E0E0);
        canvas.drawRoundRect(-s * 0.26f, -s * 0.04f, s * 0.26f, s * 0.0f, 2, 2, stripePaint);
        canvas.drawRoundRect(-s * 0.26f, s * 0.04f, s * 0.26f, s * 0.08f, 2, 2, stripePaint);

        Paint diagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        diagPaint.setColor(0xFFE0E0E0);
        diagPaint.setStrokeWidth(3);
        canvas.drawLine(-s * 0.2f, -s * 0.12f, -s * 0.08f, s * 0.08f, diagPaint);
        canvas.drawLine(s * 0.2f, -s * 0.12f, s * 0.08f, s * 0.08f, diagPaint);

        canvas.save();
        canvas.rotate(-armSwing * 25, -s * 0.26f, -s * 0.08f);
        Paint sleevePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sleevePaint.setColor(0xFF1565C0);
        canvas.drawRoundRect(-s * 0.4f, -s * 0.12f, -s * 0.24f, s * 0.1f, 5, 5, sleevePaint);
        Paint glovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glovePaint.setColor(0xFF8D6E63);
        canvas.drawCircle(-s * 0.32f, s * 0.14f, s * 0.08f, glovePaint);
        Paint gloveDetailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gloveDetailPaint.setColor(0xFF6D4C41);
        gloveDetailPaint.setStyle(Paint.Style.STROKE);
        gloveDetailPaint.setStrokeWidth(1.5f);
        canvas.drawCircle(-s * 0.32f, s * 0.14f, s * 0.08f, gloveDetailPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(armSwing * 25, s * 0.26f, -s * 0.08f);
        canvas.drawRoundRect(s * 0.24f, -s * 0.12f, s * 0.4f, s * 0.1f, 5, 5, sleevePaint);
        canvas.drawCircle(s * 0.32f, s * 0.14f, s * 0.08f, glovePaint);
        canvas.drawCircle(s * 0.32f, s * 0.14f, s * 0.08f, gloveDetailPaint);
        canvas.restore();

        Paint skinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skinPaint.setColor(0xFFFFDBB4);
        canvas.drawRoundRect(-s * 0.07f, -s * 0.22f, s * 0.07f, -s * 0.12f, 3, 3, skinPaint);
        canvas.drawCircle(0, -s * 0.35f, s * 0.17f, skinPaint);
        canvas.drawCircle(-s * 0.16f, -s * 0.35f, s * 0.035f, skinPaint);
        canvas.drawCircle(s * 0.16f, -s * 0.35f, s * 0.035f, skinPaint);

        Paint helmetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        helmetPaint.setColor(0xFFFFC107);
        Path helmet = new Path();
        helmet.moveTo(-s * 0.23f, -s * 0.35f);
        helmet.quadTo(-s * 0.24f, -s * 0.6f, 0, -s * 0.62f);
        helmet.quadTo(s * 0.24f, -s * 0.6f, s * 0.23f, -s * 0.35f);
        helmet.lineTo(-s * 0.23f, -s * 0.35f);
        canvas.drawPath(helmet, helmetPaint);

        Paint helmetRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        helmetRimPaint.setColor(0xFFFFD54F);
        canvas.drawRoundRect(-s * 0.26f, -s * 0.38f, s * 0.26f, -s * 0.32f, 3, 3, helmetRimPaint);

        Paint helmetLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        helmetLinePaint.setColor(0xFFFF8F00);
        helmetLinePaint.setStrokeWidth(3);
        canvas.drawLine(0, -s * 0.6f, 0, -s * 0.38f, helmetLinePaint);

        Paint lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightPaint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(-s * 0.05f, -s * 0.42f, s * 0.05f, -s * 0.38f, 2, 2, lightPaint);

        float lightPulse = (float)Math.abs(Math.sin(pulseAnim * 2));
        Paint lightGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightGlowPaint.setColor(android.graphics.Color.argb((int)(100 * lightPulse), 255, 255, 200));
        canvas.drawCircle(0, -s * 0.4f, s * 0.08f, lightGlowPaint);

        Paint gogglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gogglePaint.setColor(0xFF263238);
        canvas.drawRoundRect(-s * 0.16f, -s * 0.4f, -s * 0.02f, -s * 0.3f, 4, 4, gogglePaint);
        canvas.drawRoundRect(s * 0.02f, -s * 0.4f, s * 0.16f, -s * 0.3f, 4, 4, gogglePaint);

        Paint lensPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lensPaint.setColor(0xFF546E7A);
        canvas.drawRoundRect(-s * 0.14f, -s * 0.38f, -s * 0.04f, -s * 0.32f, 3, 3, lensPaint);
        canvas.drawRoundRect(s * 0.04f, -s * 0.38f, s * 0.14f, -s * 0.32f, 3, 3, lensPaint);

        Paint reflectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        reflectPaint.setColor(0x44FFFFFF);
        canvas.drawRoundRect(-s * 0.13f, -s * 0.38f, -s * 0.08f, -s * 0.34f, 2, 2, reflectPaint);
        canvas.drawRoundRect(s * 0.05f, -s * 0.38f, s * 0.1f, -s * 0.34f, 2, 2, reflectPaint);

        Paint bridgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bridgePaint.setColor(0xFF263238);
        canvas.drawRect(-s * 0.02f, -s * 0.36f, s * 0.02f, -s * 0.34f, bridgePaint);

        Paint nosePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nosePaint.setColor(0xFFEEC9A0);
        canvas.drawOval(-s * 0.03f, -s * 0.32f, s * 0.03f, -s * 0.26f, nosePaint);

        Paint mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(0xFF8D6E63);
        mouthPaint.setStrokeWidth(2.5f);
        mouthPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(-s * 0.06f, -s * 0.22f, s * 0.06f, -s * 0.22f, mouthPaint);

        Paint stubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stubblePaint.setColor(0x22000000);
        canvas.drawRoundRect(-s * 0.1f, -s * 0.24f, s * 0.1f, -s * 0.18f, 3, 3, stubblePaint);
    }

    private void drawRobot(Canvas canvas) {
        float pulse = 1 + (float)Math.sin(pulseAnim * 2) * 0.05f;
        float s = size * pulse;
        float ledPulse = (float)Math.abs(Math.sin(pulseAnim * 3));
        float scanPulse = (float)Math.abs(Math.sin(pulseAnim * 1.5f));

        float hoverOffset = (float)Math.sin(pulseAnim * 2) * s * 0.03f;
        canvas.translate(0, hoverOffset);

        Paint thrusterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int thrusterAlpha = (int)(80 + 40 * ledPulse);
        thrusterPaint.setColor(android.graphics.Color.argb(thrusterAlpha, 100, 200, 255));
        canvas.drawOval(-s * 0.25f, s * 0.35f, s * 0.25f, s * 0.45f, thrusterPaint);
        thrusterPaint.setColor(android.graphics.Color.argb(thrusterAlpha / 2, 150, 220, 255));
        canvas.drawOval(-s * 0.2f, s * 0.38f, s * 0.2f, s * 0.48f, thrusterPaint);

        Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(0xFF37474F);
        RectF baseRect = new RectF(-s * 0.32f, s * 0.22f, s * 0.32f, s * 0.35f);
        canvas.drawRoundRect(baseRect, 10, 10, basePaint);

        Paint baseLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baseLightPaint.setColor(android.graphics.Color.argb((int)(180 + 75 * ledPulse), 0, 200, 255));
        canvas.drawCircle(-s * 0.2f, s * 0.28f, s * 0.03f, baseLightPaint);
        canvas.drawCircle(s * 0.2f, s * 0.28f, s * 0.03f, baseLightPaint);
        canvas.drawCircle(0, s * 0.28f, s * 0.03f, baseLightPaint);

        Paint bodyPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint1.setColor(0xFFCFD8DC);
        RectF bodyRect = new RectF(-s * 0.3f, -s * 0.18f, s * 0.3f, s * 0.24f);
        canvas.drawRoundRect(bodyRect, 12, 12, bodyPaint1);

        Paint panelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelPaint.setColor(0xFF263238);
        RectF panelRect = new RectF(-s * 0.25f, -s * 0.12f, s * 0.25f, s * 0.18f);
        canvas.drawRoundRect(panelRect, 8, 8, panelPaint);

        Paint screenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        screenPaint.setColor(0xFF1B5E20);
        RectF screenRect = new RectF(-s * 0.2f, -s * 0.08f, s * 0.2f, s * 0.14f);
        canvas.drawRoundRect(screenRect, 5, 5, screenPaint);

        Paint dataPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dataPaint.setColor(0xFF4CAF50);
        dataPaint.setStrokeWidth(1.5f);
        for (int i = 0; i < 4; i++) {
            float lineY = -s * 0.04f + i * s * 0.045f;
            float lineWidth = s * (0.15f + (float)Math.random() * 0.1f * scanPulse);
            canvas.drawLine(-s * 0.16f, lineY, -s * 0.16f + lineWidth, lineY, dataPaint);
        }

        Paint recyclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        recyclePaint.setColor(0xFF4CAF50);
        recyclePaint.setTextSize(s * 0.12f);
        recyclePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("♻", s * 0.1f, s * 0.08f, recyclePaint);

        float clawAnim = (float)Math.sin(pulseAnim * 2) * 0.05f;
        canvas.save();
        canvas.rotate((float)Math.sin(pulseAnim * 1.5f) * 8, -s * 0.3f, 0);

        Paint armPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        armPaint.setColor(0xFF78909C);
        canvas.drawCircle(-s * 0.3f, -s * 0.05f, s * 0.06f, armPaint);
        canvas.drawRoundRect(-s * 0.46f, -s * 0.08f, -s * 0.28f, s * 0.02f, 4, 4, armPaint);
        canvas.drawCircle(-s * 0.46f, -s * 0.03f, s * 0.04f, armPaint);
        canvas.drawRoundRect(-s * 0.5f, -s * 0.02f, -s * 0.42f, s * 0.15f, 4, 4, armPaint);

        Paint clawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clawPaint.setColor(0xFF455A64);
        canvas.save();
        canvas.rotate(-10 + clawAnim * 100, -s * 0.46f, s * 0.15f);
        canvas.drawRoundRect(-s * 0.52f, s * 0.14f, -s * 0.44f, s * 0.28f, 2, 2, clawPaint);
        canvas.restore();
        canvas.save();
        canvas.rotate(10 - clawAnim * 100, -s * 0.46f, s * 0.15f);
        canvas.drawRoundRect(-s * 0.48f, s * 0.14f, -s * 0.4f, s * 0.28f, 2, 2, clawPaint);
        canvas.restore();

        canvas.restore();

        canvas.save();
        canvas.rotate(-(float)Math.sin(pulseAnim * 1.5f) * 8, s * 0.3f, 0);

        canvas.drawCircle(s * 0.3f, -s * 0.05f, s * 0.06f, armPaint);
        canvas.drawRoundRect(s * 0.28f, -s * 0.08f, s * 0.46f, s * 0.02f, 4, 4, armPaint);
        canvas.drawCircle(s * 0.46f, -s * 0.03f, s * 0.04f, armPaint);
        canvas.drawRoundRect(s * 0.42f, -s * 0.02f, s * 0.5f, s * 0.15f, 4, 4, armPaint);

        canvas.save();
        canvas.rotate(10 - clawAnim * 100, s * 0.46f, s * 0.15f);
        canvas.drawRoundRect(s * 0.44f, s * 0.14f, s * 0.52f, s * 0.28f, 2, 2, clawPaint);
        canvas.restore();
        canvas.save();
        canvas.rotate(-10 + clawAnim * 100, s * 0.46f, s * 0.15f);
        canvas.drawRoundRect(s * 0.4f, s * 0.14f, s * 0.48f, s * 0.28f, 2, 2, clawPaint);
        canvas.restore();

        canvas.restore();

        Paint headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headPaint.setColor(0xFFECEFF1);
        RectF headRect = new RectF(-s * 0.26f, -s * 0.54f, s * 0.26f, -s * 0.16f);
        canvas.drawRoundRect(headRect, 14, 14, headPaint);

        Paint headBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headBorderPaint.setStyle(Paint.Style.STROKE);
        headBorderPaint.setStrokeWidth(2);
        headBorderPaint.setColor(0xFF90A4AE);
        canvas.drawRoundRect(headRect, 14, 14, headBorderPaint);

        Paint visorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        visorPaint.setColor(0xFF0D47A1);
        RectF visorRect = new RectF(-s * 0.21f, -s * 0.5f, s * 0.21f, -s * 0.26f);
        canvas.drawRoundRect(visorRect, 10, 10, visorPaint);

        Paint visorReflectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        visorReflectPaint.setColor(0x33FFFFFF);
        canvas.drawRoundRect(-s * 0.19f, -s * 0.48f, -s * 0.05f, -s * 0.38f, 5, 5, visorReflectPaint);

        Paint ledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int ledAlpha = (int)(180 + 75 * ledPulse);

        ledPaint.setColor(android.graphics.Color.argb(ledAlpha, 0, 255, 150));
        RectF leftEye = new RectF(-s * 0.16f, -s * 0.45f, -s * 0.04f, -s * 0.33f);
        canvas.drawRoundRect(leftEye, 4, 4, ledPaint);

        RectF rightEye = new RectF(s * 0.04f, -s * 0.45f, s * 0.16f, -s * 0.33f);
        canvas.drawRoundRect(rightEye, 4, 4, ledPaint);

        Paint eyeGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeGlowPaint.setColor(android.graphics.Color.argb((int)(60 * ledPulse), 0, 255, 150));
        canvas.drawCircle(-s * 0.1f, -s * 0.39f, s * 0.1f, eyeGlowPaint);
        canvas.drawCircle(s * 0.1f, -s * 0.39f, s * 0.1f, eyeGlowPaint);

        float lookDir = rotation > 0 ? 0.02f : -0.02f;
        Paint pupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pupilPaint.setColor(0xFF00FF88);
        canvas.drawCircle(-s * 0.1f + s * lookDir, -s * 0.39f, s * 0.025f, pupilPaint);
        canvas.drawCircle(s * 0.1f + s * lookDir, -s * 0.39f, s * 0.025f, pupilPaint);

        float scanY = -s * 0.48f + scanPulse * s * 0.2f;
        Paint scanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scanPaint.setColor(android.graphics.Color.argb(100, 0, 255, 200));
        scanPaint.setStrokeWidth(2);
        canvas.drawLine(-s * 0.18f, scanY, s * 0.18f, scanY, scanPaint);

        Paint antennaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        antennaPaint.setColor(0xFF607D8B);
        antennaPaint.setStrokeWidth(3);
        canvas.drawLine(0, -s * 0.54f, 0, -s * 0.72f, antennaPaint);
        canvas.drawCircle(0, -s * 0.56f, s * 0.025f, antennaPaint);

        Paint antennaLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        antennaLightPaint.setColor(android.graphics.Color.argb(ledAlpha, 255, 80, 80));
        canvas.drawCircle(0, -s * 0.74f, s * 0.045f, antennaLightPaint);

        Paint haloLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        haloLightPaint.setColor(android.graphics.Color.argb((int)(40 * ledPulse), 255, 100, 100));
        canvas.drawCircle(0, -s * 0.74f, s * 0.08f, haloLightPaint);

        canvas.drawLine(-s * 0.2f, -s * 0.52f, -s * 0.28f, -s * 0.62f, antennaPaint);
        canvas.drawLine(s * 0.2f, -s * 0.52f, s * 0.28f, -s * 0.62f, antennaPaint);

        Paint sideLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sideLightPaint.setColor(android.graphics.Color.argb(ledAlpha, 0, 150, 255));
        canvas.drawCircle(-s * 0.29f, -s * 0.63f, s * 0.025f, sideLightPaint);
        canvas.drawCircle(s * 0.29f, -s * 0.63f, s * 0.025f, sideLightPaint);
    }

    private void drawLoadIndicator(Canvas canvas) {
        // Barra de carga sobre el limpiador
        float barWidth = size * 0.8f;
        float barHeight = 8;
        float fillRatio = currentLoad / capacity;

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0x88000000);

        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(0xFF2ECC71);

        float barX = x - barWidth / 2;
        float barY = y - size * 0.8f;

        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, bgPaint);
        canvas.drawRect(barX, barY, barX + barWidth * fillRatio, barY + barHeight, fillPaint);
    }

    private float distanceTo(float tx, float ty) {
        float dx = tx - x;
        float dy = ty - y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getType() { return type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
