package com.pya.javamusicplayer;

import javafx.scene.image.Image;

public class Music {
    private String path;
    private String title;
    private String artist;
    private String album;
    private Image albumImage;

    public Music(String path, String title, String artist, String album, Image albumImage) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumImage = albumImage;
    }

    public Image getAlbumImage() {
        return albumImage;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        return result;
    }

}
