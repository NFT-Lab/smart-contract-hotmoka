package io.nfteam.nftlab.nftlabstore;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import io.takamaka.code.math.UnsignedBigInteger;

public final class NFTLab extends Storage
{
    public final Contract artist;
    public final UnsignedBigInteger artistId;
    public final String hash;
    public final String timestamp;

    public NFTLab(Contract artist, UnsignedBigInteger artistId, String hash, String timestamp) {
        this.artist = artist;
        this.artistId = artistId;
        this.hash = hash;
        this.timestamp = timestamp;
    }

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
