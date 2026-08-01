package audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.EnumMap;
import java.util.Map;

public class AudioManager {
    private static final String SFX_PATH = "audio/sfx/";
    private static final String MUSIC_PATH = "audio/music/";

    private static final AudioManager INSTANCE = new AudioManager();

    public static AudioManager getInstance() {
        return INSTANCE;
    }

    private final Map<SoundType, Sound> loadedSounds = new EnumMap<>(SoundType.class);
    private final Map<MusicType, Music> loadedMusics = new EnumMap<>(MusicType.class);

    private MusicType currentMusicType;
    private Music currentMusic;

    private float sfxVolume = 1f;
    private float musicVolume = 0.6f;
    private boolean sfxMuted = false;
    private boolean musicMuted = false;

    private AudioManager() {
    }

    public void playSound(SoundType type) {
        if (sfxMuted || type == null) {
            return;
        }

        Sound sound = getOrLoadSound(type);
        if (sound != null) {
            sound.play(sfxVolume);
        }
    }

    private Sound getOrLoadSound(SoundType type) {
        if (loadedSounds.containsKey(type)) {
            return loadedSounds.get(type);
        }

        Sound sound = null;
        try {
            sound = Gdx.audio.newSound(Gdx.files.internal(SFX_PATH + type.getFileName()));
        } catch (GdxRuntimeException e) {
            Gdx.app.error("AudioManager", "Nao foi possivel carregar o som " + type
                + " (" + SFX_PATH + type.getFileName() + "). Verifique se o arquivo existe em assets/.");
        }

        loadedSounds.put(type, sound);
        return sound;
    }

    public void playMusic(MusicType type) {
        if (type == null || type == currentMusicType) {
            return; // já é essa música que está tocando, não reinicia
        }

        stopMusic();

        Music music = getOrLoadMusic(type);
        if (music == null) {
            return;
        }

        music.setLooping(true);
        music.setVolume(musicMuted ? 0f : musicVolume);
        music.play();

        currentMusicType = type;
        currentMusic = music;
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = null;
        currentMusicType = null;
    }

    private Music getOrLoadMusic(MusicType type) {
        if (loadedMusics.containsKey(type)) {
            return loadedMusics.get(type);
        }

        Music music = null;
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_PATH + type.getFileName()));
        } catch (GdxRuntimeException e) {
            Gdx.app.error("AudioManager", "Nao foi possivel carregar a musica " + type
                + " (" + MUSIC_PATH + type.getFileName() + "). Verifique se o arquivo existe em assets/.");
        }

        loadedMusics.put(type, music);
        return music;
    }

    // --- CONTROLE DE VOLUME ---

    public void setSfxVolume(float volume) {
        sfxVolume = clamp(volume);
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp(volume);
        if (currentMusic != null && !musicMuted) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public void setSfxMuted(boolean muted) {
        sfxMuted = muted;
    }

    public void setMusicMuted(boolean muted) {
        musicMuted = muted;
        if (currentMusic != null) {
            currentMusic.setVolume(muted ? 0f : musicVolume);
        }
    }

    private float clamp(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }

    public void dispose() {
        for (Sound sound : loadedSounds.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        loadedSounds.clear();

        for (Music music : loadedMusics.values()) {
            if (music != null) {
                music.dispose();
            }
        }
        loadedMusics.clear();

        currentMusic = null;
        currentMusicType = null;
    }
}
