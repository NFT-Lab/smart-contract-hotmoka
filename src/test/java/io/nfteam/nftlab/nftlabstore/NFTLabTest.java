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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import static io.hotmoka.beans.Coin.*;
import static io.hotmoka.beans.Coin.panarea;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NFTLabTest extends TakamakaTest {
    private static final ClassType NFTLab = new ClassType("io.nfteam.nftlab.nftlabstore.NFTLab");
    private static final ClassType UBI = ClassType.UNSIGNED_BIG_INTEGER;
    private static final ConstructorSignature CONSTRUCTOR_NFTLAB_CONTRACT_UBI_STR_STR =
            new ConstructorSignature(
                    NFTLab,
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
        Path erc721Code = Paths.get(
                System.getProperty("user.home") + "/.m2/repository/io/nfteam/nftlab/hotmoka-erc721-custom/0.1/hotmoka-erc721-custom-0.1.jar"
        );

        TransactionReference erc721Ref = addJarStoreTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                gasHelper.getSafeGasPrice(),
                node.getTakamakaCode(),
                Files.readAllBytes(erc721Code),
                node.getTakamakaCode());

        Path smartContractCode = Paths.get(
                "./target/smart-contract-hotmoka-1.0.jar"
        );

        classpath = addJarStoreTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                gasHelper.getSafeGasPrice(),
                node.getTakamakaCode(),
                Files.readAllBytes(smartContractCode),
                erc721Ref);
    }

    @Test
    void constructor() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2019");

        StorageReference artistId = addConstructorCallTransaction(
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
                CONSTRUCTOR_NFTLAB_CONTRACT_UBI_STR_STR,
                account(2),
                artistId,
                hash,
                timestamp
        );
    }

    @Test
    void equals_ValidObject_True() throws TransactionException, TransactionRejectedException, CodeExecutionException, SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue timestamp = new StringValue("2019");

        StorageReference artistId = addConstructorCallTransaction(
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
                CONSTRUCTOR_NFTLAB_CONTRACT_UBI_STR_STR,
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
                CONSTRUCTOR_NFTLAB_CONTRACT_UBI_STR_STR,
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