package io.nfteam.nftlab.nftlabstore;

import io.nfteam.nftlab.hotmoka.erc721_customized.extensions.ERC721URIStorage;
import io.takamaka.code.lang.*;
import io.takamaka.code.math.UnsignedBigInteger;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageMap;
import io.takamaka.code.util.StorageTreeMap;

import java.math.BigInteger;

public class NFTLabStore extends ERC721URIStorage {
    private final Contract owner;
    private BigInteger tokenId = BigInteger.ONE;

    private final StorageMap<UnsignedBigInteger, NFTLab> nfts = new StorageTreeMap<>();
    private final StorageMap<String, UnsignedBigInteger> hashToId = new StorageTreeMap<>();
    private final StorageMap<UnsignedBigInteger, StorageLinkedList<NFTTransaction>> history = new StorageTreeMap<>();

    @FromContract
    public NFTLabStore(String name, String symbol) {
        super(name, symbol, true);
        owner = caller();
    }

    @FromContract
    public BigInteger mint(
            Contract artist,
            BigInteger artistId,
            String hash,
            String timestamp
    ) {
        onlyOwner();
        Takamaka.require(hashToId.get(hash) == null, "The token already exists.");

        UnsignedBigInteger newTokenId = new UnsignedBigInteger(tokenId);

        _safeMint(artist, newTokenId);
        _setTokenURI(newTokenId, hash);

        nfts.put(newTokenId, new NFTLab(
                artist,
                artistId,
                hash,
                timestamp
        ));
        hashToId.put(hash, newTokenId);

        approve(owner, newTokenId);

        tokenId = tokenId.add(BigInteger.ONE);

        Takamaka.event(new Minted(artist, hash, timestamp));

        return newTokenId.toBigInteger();
    }

    @FromContract
    public boolean transfer(
            BigInteger tokenId,
            Contract seller,
            BigInteger sellerId,
            Contract buyer,
            BigInteger buyerId,
            String price,
            String timestamp
    ) {
        onlyOwner();

        UnsignedBigInteger tokenIdUBI = new UnsignedBigInteger(tokenId);

        safeTransferFrom(seller, buyer, tokenIdUBI);

        history.putIfAbsent(tokenIdUBI, new StorageLinkedList<>());

        history.get(tokenIdUBI).add(new NFTTransaction(
                        tokenId,
                        seller,
                        sellerId,
                        buyer,
                        buyerId,
                        price,
                        timestamp
                ));

        Takamaka.event(new Transferred(
                tokenId,
                seller,
                buyer,
                price,
                timestamp
        ));

        return true;
    }

    @View
    public StorageLinkedList<NFTTransaction> getHistory(BigInteger tokenId){
        UnsignedBigInteger tokenIdUBI = new UnsignedBigInteger(tokenId);
        Takamaka.require(_exists(tokenIdUBI), "Unable to get the history of a non-existent NFT.");

        return history.getOrDefault(tokenIdUBI, new StorageLinkedList<NFTTransaction>());
    }

    @View
    public BigInteger getTokenId(String hash) {
        Takamaka.require(hashToId.get(hash) != null, "Unable to get the ID of a non-existent NFT.");

        return hashToId.get(hash).toBigInteger();
    }

    @View
    public NFTLab getNFTByHash(String hash) {
        Takamaka.require(hashToId.get(hash) != null, "Unable to get a non-existent NFT.");

        return nfts.get(hashToId.get(hash));
    }

    @View
    public NFTLab getNFTById(BigInteger tokenId) {
        UnsignedBigInteger tokenIdUBI = new UnsignedBigInteger(tokenId);
        Takamaka.require(_exists(tokenIdUBI), "Unable to get a non-existent NFT.");

        return nfts.get(tokenIdUBI);
    }

    @Override
    protected String _baseURI() {
        return "https://cloudflare-ipfs.com/ipfs/";
    }

    @FromContract
    private void onlyOwner() {
        Takamaka.require(owner == caller(), "Only owner can do this operation.");
    }

    // Events

    class Minted extends Event {
        public final Contract artist;
        public final String hash;
        public final String timestamp;

        @FromContract
        public Minted(Contract artist, String hash, String timestamp) {
            this.artist = artist;
            this.hash = hash;
            this.timestamp = timestamp;
        }
    }

    class Transferred extends Event {
        public final BigInteger tokenId;
        public final Contract seller;
        public final Contract buyer;
        public final String price;
        public final String timestamp;

        @FromContract
        public Transferred(BigInteger tokenId, Contract seller, Contract buyer, String price, String timestamp) {
            this.tokenId = tokenId;
            this.seller = seller;
            this.buyer = buyer;
            this.price = price;
            this.timestamp = timestamp;
        }
    }
}
