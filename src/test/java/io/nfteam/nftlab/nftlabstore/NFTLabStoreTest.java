package io.nfteam.nftlab.nftlabstore;

import io.hotmoka.beans.CodeExecutionException;
import io.hotmoka.beans.TransactionException;
import io.hotmoka.beans.TransactionRejectedException;
import io.hotmoka.beans.references.TransactionReference;
import io.hotmoka.beans.signatures.ConstructorSignature;
import io.hotmoka.beans.signatures.NonVoidMethodSignature;
import io.hotmoka.beans.types.BasicTypes;
import io.hotmoka.beans.types.ClassType;
import io.hotmoka.beans.values.*;
import io.hotmoka.constants.Constants;
import io.hotmoka.views.GasHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import static io.hotmoka.beans.Coin.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NFTLabStoreTest extends TakamakaTest {
    private static final ClassType NFTLabStore = new ClassType("io.nfteam.nftlab.nftlabstore.NFTLabStore");
    private static final ClassType NFTLab = new ClassType("io.nfteam.nftlab.nftlabstore.NFTLab");
    private static final ClassType NFTTransaction = new ClassType("io.nfteam.nftlab.nftlabstore.NFTTransaction");
    private static final ConstructorSignature CONSTRUCTOR_NFTLABSTORE_STR_STR = new ConstructorSignature(NFTLabStore, ClassType.STRING, ClassType.STRING);
    private static final ConstructorSignature CONSTRUCTOR_NFTLAB_CONTRACT_BI_STR_STR =
            new ConstructorSignature(
                NFTLab,
                ClassType.CONTRACT,
                ClassType.BIG_INTEGER,
                ClassType.STRING,
                ClassType.STRING
            );
    private static final ConstructorSignature CONSTRUCTOR_NFTTRANSACTION_UBI_CONTRACT_UBI_CONTRACT_BI_STR_STR =
            new ConstructorSignature(
                    NFTTransaction,
                    ClassType.BIG_INTEGER,
                    ClassType.CONTRACT,
                    ClassType.BIG_INTEGER,
                    ClassType.CONTRACT,
                    ClassType.BIG_INTEGER,
                    ClassType.STRING,
                    ClassType.STRING
            );

    private TransactionReference classpath;

    private StorageReference creator;
    private PrivateKey creator_prv_key;

    private StorageReference nftLabStore;

    @BeforeEach
    void beforeEach() throws Exception {
        setAccounts(stromboli(1), filicudi(100), filicudi(100), filicudi(100));
        creator = account(1);
        creator_prv_key = privateKey(1);

        GasHelper gasHelper = new GasHelper(node);

        TransactionReference erc721Ref = addJarStoreTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                gasHelper.getSafeGasPrice(),
                node.getTakamakaCode(),
                Files.readAllBytes(erc721CodePath),
                node.getTakamakaCode());

        classpath = addJarStoreTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                gasHelper.getSafeGasPrice(),
                node.getTakamakaCode(),
                Files.readAllBytes(smartContractCodePath),
                erc721Ref);
    }

    @BeforeEach
    void createNFTLabStore() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue name = new StringValue("NFTLab");
        StringValue symbol = new StringValue("NFTL");

        nftLabStore = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTLABSTORE_STR_STR,
                name,
                symbol
        );
    }

    @Test
    void constructor() throws SignatureException, TransactionException, CodeExecutionException, InvalidKeyException, TransactionRejectedException {
        StringValue expectedName = new StringValue("NFTLab");
        StringValue expectedSymbol = new StringValue("NFTL");

        StorageReference nftLabStore = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTLABSTORE_STR_STR,
                expectedName,
                expectedSymbol
        );

        StorageValue actualName = addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(NFTLabStore, "name", ClassType.STRING),
                nftLabStore
        );

        StorageValue actualSymbol = addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(NFTLabStore, "symbol", ClassType.STRING),
                nftLabStore
        );

        assertEquals(expectedName, actualName);
        assertEquals(expectedSymbol, actualSymbol);
    }

    @Test
    void mint_ValidNFT_NFTTokenId() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ZERO);

        BigIntegerValue actualTokenId = (BigIntegerValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                artistId,
                new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq"),
                new StringValue("2019")
        );

        assertEquals(expectedTokenId, actualTokenId);
    }

    @Test
    void mint_MintWithNonOwnerAccount_ThrowException() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StorageReference nonOwner = account(2);
        PrivateKey nonOwner_prv_key = privateKey(2);

        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ZERO);

        throwsTransactionExceptionWithCause(Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () -> addInstanceMethodCallTransaction(
                        nonOwner_prv_key,
                        nonOwner,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                NFTLabStore,
                                "mint",
                                ClassType.BIG_INTEGER,
                                ClassType.CONTRACT,
                                ClassType.BIG_INTEGER,
                                ClassType.STRING,
                                ClassType.STRING),
                        nftLabStore,
                        account(2),
                        artistId,
                        new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq"),
                        new StringValue("2019")
                )
        );
    }

    @Test
    void mint_MintTwoTimesSameNFT_ThrowException() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ZERO);

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                artistId,
                new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq"),
                new StringValue("2019")
        );

        throwsTransactionExceptionWithCause(Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () -> addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                artistId,
                new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq"),
                new StringValue("2019")
          )
        );
    }

    @Test
    void transfer_ValidTransfer_True() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BigIntegerValue sellerId = new BigIntegerValue(BigInteger.ZERO);

        BigIntegerValue buyerId = new BigIntegerValue(BigInteger.ONE);

        BigIntegerValue tokenId = (BigIntegerValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                sellerId,
                hash,
                new StringValue("2019")
        );

        BooleanValue transferred = (BooleanValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "transfer",
                        BasicTypes.BOOLEAN,
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING
                ),
                nftLabStore,
                tokenId,
                account(2),
                sellerId,
                account(3),
                buyerId,
                hash,
                new StringValue("2020")
        );

        assertTrue(transferred.value);
    }

    @Test
    void transfer_TransferWithNonOwnerAccount_ThrowException() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StorageReference nonOwner = account(3);
        PrivateKey nonOwner_prv_key = privateKey(3);

        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BigIntegerValue sellerId = new BigIntegerValue(BigInteger.ZERO);

        BigIntegerValue buyerId = new BigIntegerValue(BigInteger.ONE);

        BigIntegerValue tokenId = (BigIntegerValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                sellerId,
                hash,
                new StringValue("2019")
        );

        throwsTransactionExceptionWithCause(Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME, () -> addInstanceMethodCallTransaction(
                nonOwner_prv_key,
                nonOwner,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "transfer",
                        BasicTypes.BOOLEAN,
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING
                ),
                nftLabStore,
                tokenId,
                account(2),
                sellerId,
                account(3),
                buyerId,
                hash,
                new StringValue("2020")
        ));
    }

    @Test
    void getHistory_ExistingNFT_ItsHistory() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2020");

        BigIntegerValue tokenId = new BigIntegerValue(BigInteger.ONE);

        BigIntegerValue sellerId = new BigIntegerValue(BigInteger.ZERO);

        BigIntegerValue buyerId = new BigIntegerValue(BigInteger.ONE);

        StorageReference expectedTransaction = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTTRANSACTION_UBI_CONTRACT_UBI_CONTRACT_BI_STR_STR,
                tokenId,
                account(2),
                sellerId,
                account(3),
                buyerId,
                hash,
                timestamp
        );

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                sellerId,
                hash,
                new StringValue("2019")
        );

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "transfer",
                        BasicTypes.BOOLEAN,
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING
                ),
                nftLabStore,
                tokenId,
                account(2),
                sellerId,
                account(3),
                buyerId,
                hash,
                timestamp
        );

        StorageReference history = (StorageReference) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getHistory",
                        ClassType.STORAGE_LINKED_LIST,
                        ClassType.BIG_INTEGER
                ),
                nftLabStore,
                tokenId
        );

        StorageReference actualTransaction = (StorageReference) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        ClassType.STORAGE_LINKED_LIST,
                        "get",
                        ClassType.OBJECT,
                        BasicTypes.INT
                ),
                history,
                new IntValue(0)
        );

        BooleanValue sameTransaction = (BooleanValue) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "equals",
                        BasicTypes.BOOLEAN,
                        ClassType.OBJECT
                ),
                actualTransaction,
                expectedTransaction
        );

        assertTrue(sameTransaction.value);
    }

    @Test
    void getHistory_NonExistingNFT_ThrowException() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        BigIntegerValue tokenId = new BigIntegerValue(BigInteger.ONE);

        throwsTransactionExceptionWithCause(Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME, () -> addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getHistory",
                        ClassType.STORAGE_LINKED_LIST,
                        ClassType.BIG_INTEGER
                ),
                nftLabStore,
                tokenId
        ));
    }

    @Test
    void getTokenId_ExistingNFT_ItsTokenId() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ZERO);

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                artistId,
                hash,
                new StringValue("2019")
        );

        BigIntegerValue actualTokenId = (BigIntegerValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getTokenId",
                        ClassType.BIG_INTEGER,
                        ClassType.STRING),
                nftLabStore,
                hash
        );

        assertEquals(expectedTokenId, actualTokenId);
    }

    @Test
    void getTokenId_NotExistingNFT_ThrowException() {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        throwsTransactionExceptionWithCause(Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME, () -> addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getTokenId",
                        ClassType.BIG_INTEGER,
                        ClassType.STRING),
                nftLabStore,
                hash
        ));
    }

    @Test
    void getNFTByHash_ExistingNFT_NFT() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2019");

        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ZERO);

        StorageReference expectedNFT = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTLAB_CONTRACT_BI_STR_STR,
                account(2),
                artistId,
                hash,
                timestamp
        );

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                artistId,
                hash,
                timestamp
        );

        StorageReference actualNFT = (StorageReference) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getNFTByHash",
                        NFTLab,
                        ClassType.STRING
                ),
                nftLabStore,
                hash
        );

        BooleanValue sameNFT = (BooleanValue) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLab,
                        "equals",
                        BasicTypes.BOOLEAN,
                        ClassType.OBJECT
                ),
                expectedNFT,
                actualNFT
        );

        assertTrue(sameNFT.value);
    }

    @Test
    void getNFTByHash_NotExistingNFT_ThrowException() {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        throwsTransactionExceptionWithCause(Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME, () -> runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getNFTByHash",
                        NFTLab,
                        ClassType.STRING
                ),
                nftLabStore,
                hash
        ));
    }

    @Test
    void getNFTById_ExistingNFT_NFT() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ZERO);

        StorageReference expectedNFT = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTLAB_CONTRACT_BI_STR_STR,
                account(2),
                artistId,
                hash,
                timestamp
        );

        BigIntegerValue tokenId = (BigIntegerValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                artistId,
                hash,
                timestamp
        );

        StorageReference actualNFT = (StorageReference) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getNFTById",
                        NFTLab,
                        ClassType.BIG_INTEGER
                ),
                nftLabStore,
                tokenId
        );

        BooleanValue sameNFT = (BooleanValue) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLab,
                        "equals",
                        BasicTypes.BOOLEAN,
                        ClassType.OBJECT
                ),
                expectedNFT,
                actualNFT
        );

        assertTrue(sameNFT.value);
    }

    @Test
    void getNFTById_NotExistingNFT_ThrowException() {
        BigIntegerValue tokenId = new BigIntegerValue(BigInteger.ONE);

        throwsTransactionExceptionWithCause(Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME, () -> runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "getNFTById",
                        NFTLab,
                        ClassType.BIG_INTEGER
                ),
                nftLabStore,
                tokenId
        ));
    }

    @Test
    void tokenURI_ExistingNFT_NFTURI() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue expectedURI = new StringValue("https://cloudflare-ipfs.com/ipfs/" + hash);

        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ONE);

        BigIntegerValue tokenId = (BigIntegerValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER,
                        ClassType.STRING,
                        ClassType.STRING),
                nftLabStore,
                account(2),
                artistId,
                hash,
                new StringValue("2019")
        );

        StorageReference tokenIdUBI = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                new ConstructorSignature(ClassType.UNSIGNED_BIG_INTEGER, ClassType.BIG_INTEGER),
                tokenId
        );

        StringValue actualURI = (StringValue) runInstanceMethodCallTransaction(
                creator,
                _10_000_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "tokenURI",
                        ClassType.STRING,
                        ClassType.UNSIGNED_BIG_INTEGER
                ),
                nftLabStore,
                tokenIdUBI
        );

        assertEquals(expectedURI, actualURI);
    }
}
