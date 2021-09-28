package it.davidlab.algonfa.service;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.IndexerClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.*;

import it.davidlab.algonfa.domain.AssetModel;
import it.davidlab.algonfa.domain.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class AlgoService {

    private final Logger logger = LoggerFactory.getLogger(AlgoService.class);

    private AlgodClient algodClient;
    private IndexerClient indexerClient;

    @Value("${algorand.algod.address}")
    private String ALGOD_API_ADDR;
    @Value("${algorand.algod.port}")
    private Integer ALGOD_PORT;
    @Value("${algorand.algod.api-token}")
    private String ALGOD_API_TOKEN;

    @Value("${algorand.indexer.address}")
    private String INDEXER_API_ADDR;
    @Value("${algorand.indexer.port}")
    private int INDEXER_API_PORT;

    @Value("${algorand.account.passphrase}")
    private String ACC_PASSPHRASE;
    @Value("${algorand.account.address}")
    private String ACC_ADDRESS;



    private Account algoAccount;
    private Address algoAddress;

    @PostConstruct
    public void AlgoServiceBuilder() throws GeneralSecurityException {
        algoAccount = new Account(ACC_PASSPHRASE);
        algoAddress = new Address(ACC_ADDRESS);

        algodClient = new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        indexerClient = new IndexerClient(INDEXER_API_ADDR, INDEXER_API_PORT);
    }




    public void waitForConfirmation(String txId, int timeout) throws Exception {

        Long txConfirmedRound = -1L;
        Response<NodeStatusResponse> statusResponse = algodClient.GetStatus().execute();

        long lastRound;
        if (statusResponse.isSuccessful()) {
            lastRound = statusResponse.body().lastRound + 1L;
        }
        else {
            throw new IllegalStateException("Cannot get node status");
        }

        long maxRound = lastRound + timeout;

        for (long currentRound = lastRound; currentRound < maxRound; currentRound++) {
            Response<PendingTransactionResponse> response = algodClient.PendingTransactionInformation(txId).execute();

            if (response.isSuccessful()) {
                txConfirmedRound = response.body().confirmedRound;
                if (txConfirmedRound == null) {
                    if (!algodClient.WaitForBlock(currentRound).execute().isSuccessful()) {
                        throw new Exception();
                    }
                }
                else {
                    return;
                }
            } else {
                throw new IllegalStateException("The transaction has been rejected");
            }
        }

        throw new IllegalStateException("Transaction not confirmed after " + timeout + " rounds!");
    }



    public AssetModel createAsset(AssetModel assetModel) throws Exception{

        Response<TransactionParametersResponse> txParResp = algodClient.TransactionParams().execute();

        TransactionParametersResponse txParams;
        if (txParResp.isSuccessful()) {
            txParams = txParResp.body();
        }
        else {
            logger.error("Cannot get tx parameters", txParResp.message());
            throw new Exception("Cannot get tx parameters");
        }

        byte[] encAssetProps = Encoder.encodeToMsgPack(assetModel.getMetadata());

        com.algorand.algosdk.transaction.Transaction txTicket = com.algorand.algosdk.transaction.Transaction.AssetCreateTransactionBuilder()
                .sender(algoAddress)
                .assetTotal(assetModel.getAssetTotal())
                .assetDecimals(assetModel.getAssetDecimals())
                .assetUnitName(assetModel.getUnitName())
                .assetName(assetModel.getAssetName())
                .url(assetModel.getUrl())
                .manager(algoAddress)
                .reserve(algoAddress)
                .freeze(algoAddress)
                .clawback(algoAddress)
                .defaultFrozen(assetModel.isDefaultFrozen())
                .note(encAssetProps)
//???                .metadataHash(propsHashR)
                .suggestedParams(txParams)
                .build();

        // Set the tx Fees
        BigInteger origfee = BigInteger.valueOf(txParams.fee);
        Account.setFeeByFeePerByte(txTicket, origfee);

        SignedTransaction signedTx = algoAccount.signTransaction(txTicket);
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);

        Response<PostTransactionsResponse> txResponse =
                algodClient.RawTransaction().rawtxn(encodedTxBytes).execute();

        String txId;

        if (txResponse.isSuccessful()) {
            txId = txResponse.body().txId;
            logger.info("Transaction id: {}", txId);
            waitForConfirmation(txId, 6);

            assetModel.setTxId(txId);
            return assetModel;

        } else {
            logger.error("Transaction not accepted", txResponse.message());
            throw new Exception("Transaction Failed");
        }
    }


    public Optional<Metadata> getAssetParams(long assetId) {

        Optional<Metadata> metadataOpt = Optional.empty();

        // search for the ACFG transactions
        Response<TransactionsResponse> txResponse;
        try {
            txResponse = indexerClient.searchForTransactions()
                    .address(algoAddress).addressRole(Enums.AddressRole.SENDER)
                    .assetId(assetId).txType(Enums.TxType.ACFG).execute();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return metadataOpt;
        }

        if (txResponse.isSuccessful()) {
            List<Transaction> txs = txResponse.body().transactions;

            // get the last note field not null
            byte[] note = txs.stream().min(Comparator.comparingLong(t -> t.confirmedRound))
                    .map(transaction -> transaction.note).orElse(null);

            if (note != null) {
                try {
                    metadataOpt = Optional.of(Encoder.decodeFromMsgPack(note, Metadata.class));
                } catch (IOException e) {
                    logger.error("It's not possible to decode note for tx:{}", txs.get(0).id);
                }
            }
        }
        return metadataOpt;
    }

}
