package io.nfteam.nftlab.nftlabstore;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import io.takamaka.code.math.UnsignedBigInteger;

public final class NFTTransaction extends Storage {
    public final UnsignedBigInteger tokenId;
    public final Contract seller;
    public final UnsignedBigInteger sellerId;
    public final Contract buyer;
    public final UnsignedBigInteger buyerId;
    public final String price;
    public final String timestamp;

    public NFTTransaction(UnsignedBigInteger tokenId, Contract seller, UnsignedBigInteger sellerId, Contract buyer, UnsignedBigInteger buyerId, String price, String timestamp) {
        this.tokenId = tokenId;
        this.seller = seller;
        this.sellerId = sellerId;
        this.buyer = buyer;
        this.buyerId = buyerId;
        this.price = price;
        this.timestamp = timestamp;
    }

    @Override @View
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFTTransaction that = (NFTTransaction) o;
        return tokenId.equals(that.tokenId) &&
                seller.equals(that.seller) &&
                sellerId.equals(that.sellerId) &&
                buyer.equals(that.buyer) &&
                buyerId.equals(that.buyerId) &&
                price.equals(that.price) &&
                timestamp.equals(that.timestamp);
    }
}