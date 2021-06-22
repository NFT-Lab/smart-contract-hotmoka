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

class NFTLabTest extends TakamakaTest {
    private static final ClassType NFTLab = new ClassType("io.nfteam.nftlab.nftlabstore.NFTLab");
    private static final ConstructorSignature CONSTRUCTOR_NFTLAB_CONTRACT_BI_STR_STR =
            new ConstructorSignature(
                    NFTLab,
                    ClassType.CONTRACT,
                    ClassType.BIG_INTEGER,
                    ClassType.STRING,
                    ClassType.STRING
            );

    private TransactionReference classpath;

    private StorageReference creator;
    private PrivateKey creator_prv_key;

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
        StringValue expectedHash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue expectedTimestamp = new StringValue("2019");
        StorageReference expectedArtist = account(2);

        BigIntegerValue expectedArtistId = new BigIntegerValue(BigInteger.ZERO);

        StorageReference nftlab = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTLAB_CONTRACT_BI_STR_STR,
                expectedArtist,
                expectedArtistId,
                expectedHash,
                expectedTimestamp
        );

        StorageReference actualArtist = (StorageReference) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLab,
                        "getArtist",
                        ClassType.CONTRACT
                ),
                nftlab
        );

        BigIntegerValue actualArtistId = (BigIntegerValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLab,
                        "getArtistId",
                        ClassType.BIG_INTEGER
                ),
                nftlab
        );

        StringValue actualHash = (StringValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLab,
                        "getHash",
                        ClassType.STRING
                ),
                nftlab
        );

        StringValue actualTimestamp = (StringValue) runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLab,
                        "getTimestamp",
                        ClassType.STRING
                ),
                nftlab
        );

        BooleanValue sameAccount = (BooleanValue) addInstanceMethodCallTransaction(
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
                expectedArtist,
                actualArtist
        );

        assertTrue(sameAccount.value);
        assertEquals(expectedArtistId, actualArtistId);
        assertEquals(expectedHash, actualHash);
        assertEquals(expectedTimestamp, actualTimestamp);
    }

    @Test
    void equals_ValidObject_True() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2019");

        BigIntegerValue artistId = new BigIntegerValue(BigInteger.ZERO);

        StorageReference firstObject = (StorageReference) addConstructorCallTransaction(
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

        StorageReference secondObject = addConstructorCallTransaction(
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

        BooleanValue equals = (BooleanValue)runInstanceMethodCallTransaction(
                creator,
                _500_000,
                classpath,
                new NonVoidMethodSignature(
                        NFTLab,
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