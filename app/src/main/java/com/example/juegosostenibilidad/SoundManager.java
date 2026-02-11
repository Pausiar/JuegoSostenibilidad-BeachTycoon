package com.example.juegosostenibilidad;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Gestiona los efectos de sonido del juego
 */
public class SoundManager {

    private SoundPool soundPool;
    private int soundCollect;
    private int soundPurchase;
    private int soundUnlock;
    private int soundError;

    private boolean soundEnabled;
    private float volume;
    private boolean soundsLoaded;

    public SoundManager(Context context) {
        soundEnabled = true;
        volume = 0.7f;
        soundsLoaded = false;

        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build();

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            soundsLoaded = true;
        });

        soundsLoaded = true;
        soundCollect = 0;
        soundPurchase = 0;
        soundUnlock = 0;
        soundError = 0;
    }

    public void playCollect() {
        if (soundEnabled && soundCollect != 0) {
            soundPool.play(soundCollect, volume, volume, 1, 0, 1.0f);
        }
    }

    public void playPurchase() {
        if (soundEnabled && soundPurchase != 0) {
            soundPool.play(soundPurchase, volume, volume, 1, 0, 1.0f);
        }
    }

    public void playUnlock() {
        if (soundEnabled && soundUnlock != 0) {
            soundPool.play(soundUnlock, volume, volume, 1, 0, 1.0f);
        }
    }

    public void playError() {
        if (soundEnabled && soundError != 0) {
            soundPool.play(soundError, volume, volume, 1, 0, 1.0f);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0, Math.min(1, volume));
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}
