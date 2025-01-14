package com.simplecity.amp_library.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.simplecity.amp_library.http.HttpClient;
import com.simplecity.amp_library.http.itunes.ItunesResult;
import com.simplecity.amp_library.http.lastfm.LastFmResult;
import com.simplecity.amp_library.utils.ArtworkUtils;
import com.simplecity.amp_library.utils.ComparisonUtils;
import com.simplecity.amp_library.utils.DataManager;
import com.simplecity.amp_library.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;

public class Album implements
        Serializable,
        ArtworkProvider,
        Comparable<Album>,
        Sortable {

    public long id;
    public String name;

    public List<Artist> artists = new ArrayList<>();
    public String albumArtistName;

    public int year;
    public int numSongs;
    public int numDiscs;

    public long lastPlayed;
    public long dateAdded;

    public List<String> paths = new ArrayList<>();

    public int songPlayCount;

    private String artworkKey;

    private String sortKey;

    public Album(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.artists = builder.artists;
        this.albumArtistName = builder.albumArtistName;
        this.numSongs = builder.numSongs;
        this.numDiscs = builder.numDiscs;
        this.year = builder.year;
        this.lastPlayed = builder.lastPlayed;
        this.dateAdded = builder.dateAdded;
        this.paths = builder.paths;
        this.songPlayCount = builder.songPlayCount;

        //Populate the artwork key & sort key properties if null.
        setSortKey();
        setArtworkKey();
    }

    public Single<List<Song>> getSongsSingle() {
        return DataManager.getInstance().getSongsObservable(song -> song.albumId == id).firstOrError();
    }

    public static class Builder {

        private long id;
        private String name;
        private List<Artist> artists = new ArrayList<>();
        private String albumArtistName;
        private int numSongs;
        private int numDiscs;
        private int year;
        private long lastPlayed;
        private long dateAdded;
        private List<String> paths = new ArrayList<>();
        private int songPlayCount;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder addArtist(Artist artist) {
            if (!this.artists.contains(artist)) {
                this.artists.add(artist);
            }
            return this;
        }

        public Builder albumArtist(String albumArtistName) {
            this.albumArtistName = albumArtistName;
            return this;
        }

        public Builder numSongs(int numSongs) {
            this.numSongs = numSongs;
            return this;
        }

        public Builder numDiscs(int numDiscs) {
            this.numDiscs = numDiscs;
            return this;
        }

        public Builder year(int year) {
            this.year = year;
            return this;
        }

        public Builder lastPlayed(long lastPlayed) {
            if (lastPlayed > this.lastPlayed) {
                this.lastPlayed = lastPlayed;
            }
            return this;
        }

        public Builder dateAdded(long dateAdded) {
            if (dateAdded > this.dateAdded) {
                this.dateAdded = dateAdded;
            }
            return this;
        }

        public Builder path(String path) {
            if (!this.paths.contains(path)) {
                this.paths.add(path);
            }
            return this;
        }

        public Builder songPlayCount(int playCount) {
            songPlayCount = playCount;
            return this;
        }

        public Album build() {
            return new Album(id, name, artists, albumArtistName, numSongs, numDiscs, year, lastPlayed, dateAdded, paths, songPlayCount);
        }
    }

    public AlbumArtist getAlbumArtist() {
        return new AlbumArtist.Builder()
                .name(albumArtistName)
                .album(this)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        if (id != album.id) return false;
        return name != null ? name.equals(album.name) : album.name == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artists=" + artists +
                ", albumArtistName='" + albumArtistName + '\'' +
                ", year=" + year +
                ", numSongs=" + numSongs +
                ", lastPlayed=" + lastPlayed +
                ", dateAdded=" + dateAdded +
                ", paths=" + paths +
                '}';
    }

    @Override
    public String getSortKey() {
        if (sortKey == null) {
            setSortKey();
        }
        return sortKey;
    }

    @Override
    public void setSortKey() {
        sortKey = StringUtils.keyFor(name);
    }

    @Override
    public String getArtworkKey() {
        if (artworkKey == null) setArtworkKey();
        return artworkKey;
    }

    private void setArtworkKey() {
        artworkKey = String.format("%s_%s", albumArtistName, name);
    }

    @Override
    public Call<? extends LastFmResult> getLastFmArtwork() {
        return HttpClient.getInstance().lastFmService.getLastFmAlbumResult(albumArtistName, name);
    }

    @Override
    public Call<ItunesResult> getItunesArtwork() {
        return HttpClient.getInstance().itunesService.getItunesAlbumResult(String.format("%s %s", albumArtistName, name));
    }

    @Override
    public InputStream getMediaStoreArtwork() {
        return ArtworkUtils.getMediaStoreArtwork(this);
    }

    @Nullable
    @Override
    public InputStream getFolderArtwork() {
        return ArtworkUtils.getFolderArtwork(getArtworkPath());
    }

    @Override
    public InputStream getTagArtwork() {
        return ArtworkUtils.getTagArtwork(getArtworkPath());
    }

    @Override
    public List<File> getFolderArtworkFiles() {
        return ArtworkUtils.getAllFolderArtwork(getArtworkPath());
    }

    @Nullable
    @WorkerThread
    private String getArtworkPath() {
        if (paths != null && !paths.isEmpty()) {
            return paths.get(0);
        }
        return null;
    }

    @Override
    public int compareTo(@NonNull Album album) {
        return ComparisonUtils.compare(getSortKey(), album.getSortKey());
    }
}