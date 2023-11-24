package com.DefiOptionVault.DOV.Strike;

import Wrapper.DovWrapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.hibernate.boot.model.convert.spi.ConverterAutoApplyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties.Credential;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.crypto.Credentials;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

@Service
public class Web3jService {
    private final String USDC = "0xe059aA96255990826D0d62c62462Feea47AF82a7";
    private final String ADMIN_ADDRESS = "0x6472A2F7187C49f05022b4F04b8f4545a7f88797";
    private final String rpcUrl = "https://polygon-mumbai.g.alchemy.com/v2/-cnnQMgoJVRgbeWlXVmUWJnn4YylXDng";
    private final String PRIVATE_KEY = "0x4e09642cccd2e9bbabb4f1136aa128986d46037e953cc21006f489a547ff995f";
    private final String DOV_ADDRESS = "0xfcAfdbC62E3B36D3aC0c0204F124Cf62c5273589";
    private final long CHAIN_ID = 80001;
    private  Web3j web3j;

    public Web3j getWeb3j() {
        return web3j;
    }

    public BigInteger BalanceOf() {
        BigInteger balance = new BigInteger("0");

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(PRIVATE_KEY);
        ContractGasProvider gasProvider = new DefaultGasProvider();

        DovWrapper contract = DovWrapper.load(USDC, web3j, credentials, gasProvider);

        try {
            balance = contract.balanceOf(ADMIN_ADDRESS).send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return balance;
    }

    public void bootstrap(BigInteger[] strikes, BigInteger expiry, String expirySymbol) {
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);
        ContractGasProvider gasProvider = new DefaultGasProvider();

        DovWrapper contract = DovWrapper.load(DOV_ADDRESS, web3j, transactionManager, gasProvider);

        //BigInteger[] strikes = new BigInteger[]{BigInteger.valueOf(1000), BigInteger.valueOf(2000)};
        //BigInteger expiry = BigInteger.valueOf(1701043199);
        //String expirySymbol = "EXPIRY_SYMBOL";

        try {
            TransactionReceipt transactionReceipt = contract.bootstrap(Arrays.asList(strikes), expiry, expirySymbol).send();
            System.out.println("Transaction Hash: " + transactionReceipt.getTransactionHash());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void expire(BigInteger settlementPrice) {
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);
        ContractGasProvider gasProvider = new DefaultGasProvider();

        DovWrapper contract = DovWrapper.load(DOV_ADDRESS, web3j, transactionManager, gasProvider);

        try {
            TransactionReceipt transactionReceipt = contract.expire(settlementPrice).send();
            System.out.println("Transaction Receipt: " + transactionReceipt.getTransactionHash());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateOptionPrices(String address, BigInteger[] optionPricesArray) {
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);
        ContractGasProvider gasProvider = new DefaultGasProvider();

        DovWrapper contract = DovWrapper.load(address, web3j, transactionManager, gasProvider);

        List<BigInteger> optionPrices = Arrays.asList(optionPricesArray);
        try {
            TransactionReceipt transactionReceipt = contract.updateOptionPrice(optionPrices).send();
            System.out.println("Transaction Receipt: " + transactionReceipt.getTransactionHash());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}