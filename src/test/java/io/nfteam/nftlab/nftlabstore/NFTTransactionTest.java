package io.nfteam.nftlab.nftlabstore;

import io.hotmoka.beans.CodeExecutionException;
import io.hotmoka.beans.TransactionException;
import io.hotmoka.beans.TransactionRejectedException;
import io.hotmoka.beans.references.TransactionReference;
import io.hotmoka.beans.signatures.ConstructorSignature;
import io.hotmoka.beans.signatures.NonVoidMethodSignature;
import io.hotmoka.beans.types.BasicTypes;
import io.hotmoka.beans.types.ClassType;
import io.hotmoka.beans.values.BigIntegerValue;
import io.hotmoka.beans.values.BooleanValue;
import io.hotmoka.beans.values.StorageReference;
import io.hotmoka.beans.values.StringValue;
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

class NFTTransactionTest extends TakamakaTest {
    private static final ClassType NFTTransaction = new ClassType("io.nfteam.nftlab.nftlabstore.NFTTransaction");
    private static final ConstructorSignature CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_BI_CONTRACT_BI_STR_STR =
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

    @Test
    void constructor() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue expectedPrice = new StringValue("1ETH");
        StringValue expectedTimestamp = new StringValue("2020");

        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        StorageReference expectedSeller = account(2);
        BigIntegerValue expectedSellerId = new BigIntegerValue(BigInteger.ZERO);

        StorageReference expectedBuyer = account(3);
        BigIntegerValue expectedBuyerId = new BigIntegerValue(BigInteger.valueOf(1));

        StorageReference transaction = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_BI_CONTRACT_BI_STR_STR,
                expectedTokenId,
                expectedSeller,
                expectedSellerId,
                expectedBuyer,
                expectedBuyerId,
                expectedPrice,
                expectedTimestamp
        );

        BigIntegerValue actualTokenId = (BigIntegerValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "getTokenId",
                        ClassType.BIG_INTEGER
                ),
                transaction
        );

        StorageReference actualSeller = (StorageReference) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "getSeller",
                        ClassType.CONTRACT
                ),
                transaction
        );

        BigIntegerValue actualSellerId = (BigIntegerValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "getSellerId",
                        ClassType.BIG_INTEGER
                ),
                transaction
        );

        StorageReference actualBuyer = (StorageReference) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "getBuyer",
                        ClassType.CONTRACT
                ),
                transaction
        );

        BigIntegerValue actualBuyerId = (BigIntegerValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "getBuyerId",
                        ClassType.BIG_INTEGER
                ),
                transaction
        );

        StringValue actualPrice = (StringValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "getPrice",
                        ClassType.STRING
                ),
                transaction
        );

        StringValue actualTimestamp = (StringValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "getTimestamp",
                        ClassType.STRING
                ),
                transaction
        );

        BooleanValue sameSeller = (BooleanValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                new NonVoidMethodSignature(
                        ClassType.CONTRACT,
                        "equals",
                        BasicTypes.BOOLEAN,
                        ClassType.OBJECT
                ),
                expectedSeller,
                actualSeller
        );
        BooleanValue sameBuyer = (BooleanValue) addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                new NonVoidMethodSignature(
                        ClassType.CONTRACT,
                        "equals",
                        BasicTypes.BOOLEAN,
                        ClassType.OBJECT
                ),
                expectedBuyer,
                actualBuyer
        );

        assertEquals(expectedTokenId, actualTokenId);
        assertTrue(sameSeller.value);
        assertEquals(expectedSellerId, actualSellerId);
        assertTrue(sameBuyer.value);
        assertEquals(expectedBuyerId, actualBuyerId);
        assertEquals(expectedPrice, actualPrice);
        assertEquals(expectedTimestamp, actualTimestamp);
    }

    @Test
    void equals_ValidObject_True() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2020");

        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        StorageReference expectedSeller = account(2);
        BigIntegerValue expectedSellerId = new BigIntegerValue(BigInteger.ZERO);

        StorageReference expectedBuyer = account(3);
        BigIntegerValue expectedBuyerId = new BigIntegerValue(BigInteger.valueOf(1));

        StorageReference firstObject = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_BI_CONTRACT_BI_STR_STR,
                expectedTokenId,
                expectedSeller,
                expectedSellerId,
                expectedBuyer,
                expectedBuyerId,
                hash,
                timestamp
        );

        StorageReference secondObject = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_BI_CONTRACT_BI_STR_STR,
                expectedTokenId,
                expectedSeller,
                expectedSellerId,
                expectedBuyer,
                expectedBuyerId,
                hash,
                timestamp
        );

        BooleanValue equals = (BooleanValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTTransaction,
                        "equals",
                        BasicTypes.BOOLEAN,
                        ClassType.OBJECT
                ),
                firstObject,
                secondObject
        );

        assertTrue(equals.value);
    }
}
