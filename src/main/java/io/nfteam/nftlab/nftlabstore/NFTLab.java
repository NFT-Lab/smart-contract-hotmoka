package io.nfteam.nftlab.nftlabstore;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;

import java.math.BigInteger;

public final class NFTLab extends Storage
{
    private final Contract artist;
    private final BigInteger artistId;
    private final String hash;
    private final String timestamp;

    public NFTLab(Contract artist, BigInteger artistId, String hash, String timestamp) {
        this.artist = artist;
        this.artistId = artistId;
        this.hash = hash;
        this.timestamp = timestamp;
    }

    @View
    public Contract getArtist() { return artist; };

    @View
    public BigInteger getArtistId() { return artistId; };

    @View
    public String getHash() { return hash; };

    @View
    public String getTimestamp() { return timestamp; };

    @Override @View
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFTLab nftLab = (NFTLab) o;
        return artist.equals(nftLab.artist) &&
                artistId.equals(nftLab.artistId) &&
                hash.equals(nftLab.hash) &&
                timestamp.equals(nftLab.timestamp);
    }
}
