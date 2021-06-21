package io.nfteam.nftlab.nftlabstore;

import io.hotmoka.beans.CodeExecutionException;
import io.hotmoka.beans.TransactionException;
import io.hotmoka.beans.TransactionRejectedException;
import io.hotmoka.beans.references.TransactionReference;
import io.hotmoka.beans.signatures.ConstructorSignature;
import io.hotmoka.beans.signatures.NonVoidMethodSignature;
import io.hotmoka.beans.types.BasicTypes;
import io.hotmoka.beans.types.ClassType;
import io.hotmoka.beans.values.BooleanValue;
import io.hotmoka.beans.values.StorageReference;
import io.hotmoka.beans.values.StringValue;
import io.hotmoka.views.GasHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import static io.hotmoka.beans.Coin.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NFTTransactionTest extends TakamakaTest {
    private static final ClassType NFTTransaction = new ClassType("io.nfteam.nftlab.nftlabstore.NFTTransaction");
    private static final ClassType UBI = ClassType.UNSIGNED_BIG_INTEGER;
    private static final ConstructorSignature CONSTRUCTOR_NFTTRANSACTION_UBI_CONTRACT_UBI_CONTRACT_UBI_STR_STR =
            new ConstructorSignature(
                    NFTTransaction,
                    ClassType.UNSIGNED_BIG_INTEGER,
                    ClassType.CONTRACT,
                    ClassType.UNSIGNED_BIG_INTEGER,
                    ClassType.CONTRACT,
                    ClassType.UNSIGNED_BIG_INTEGER,
                    ClassType.STRING,
                    ClassType.STRING
            );
    private static final ConstructorSignature CONSTRUCTOR_UBI_STR = new ConstructorSignature(UBI, ClassType.STRING);

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
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2020");

        StorageReference tokenId = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                CONSTRUCTOR_UBI_STR,
                new StringValue("1")
        );

        StorageReference sellerId = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                CONSTRUCTOR_UBI_STR,
                new StringValue("0")
        );

        StorageReference buyerId = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                CONSTRUCTOR_UBI_STR,
                new StringValue("0")
        );

        addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTTRANSACTION_UBI_CONTRACT_UBI_CONTRACT_UBI_STR_STR,
                tokenId,
                account(2),
                sellerId,
                account(3),
                buyerId,
                hash,
                timestamp
        );
    }

    @Test
    void equals_ValidObject_True() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2020");

        StorageReference tokenId = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                CONSTRUCTOR_UBI_STR,
                new StringValue("1")
        );

        StorageReference sellerId = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                CONSTRUCTOR_UBI_STR,
                new StringValue("0")
        );

        StorageReference buyerId = addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                takamakaCode(),
                CONSTRUCTOR_UBI_STR,
                new StringValue("0")
        );

        StorageReference firstObject = (StorageReference) addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTTRANSACTION_UBI_CONTRACT_UBI_CONTRACT_UBI_STR_STR,
                tokenId,
                account(2),
                sellerId,
                account(3),
                buyerId,
                hash,
                timestamp
        );

        StorageReference secondObject = (StorageReference) addConstructorCallTransaction(
                creator_prv_key,
                creator,
                _500_000,
                panarea(1),
                classpath,
                CONSTRUCTOR_NFTTRANSACTION_UBI_CONTRACT_UBI_CONTRACT_UBI_STR_STR,
                tokenId,
                account(2),
                sellerId,
                account(3),
                buyerId,
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
