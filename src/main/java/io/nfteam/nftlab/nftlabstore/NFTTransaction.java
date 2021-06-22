package io.nfteam.nftlab.nftlabstore;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;

import java.math.BigInteger;

public final class NFTTransaction extends Storage {
    private final BigInteger tokenId;
    private final Contract seller;
    private final BigInteger sellerId;
    private final Contract buyer;
    private final BigInteger buyerId;
    private final String price;
    private final String timestamp;

    public NFTTransaction(BigInteger tokenId, Contract seller, BigInteger sellerId, Contract buyer, BigInteger buyerId, String price, String timestamp) {
        this.tokenId = tokenId;
        this.seller = seller;
        this.sellerId = sellerId;
        this.buyer = buyer;
        this.buyerId = buyerId;
        this.price = price;
        this.timestamp = timestamp;
    }

    @View
    public BigInteger getTokenId() { return tokenId; }

    @View
    public Contract getSeller() { return seller; }

    @View
    public BigInteger getSellerId() { return sellerId; }

    @View
    public Contract getBuyer() { return buyer; }

    @View
    public BigInteger getBuyerId() { return buyerId; }

    @View
    public String getPrice() { return price; }

    @View
    public String getTimestamp() { return timestamp; }

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