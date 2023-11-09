package com.DefiOptionVault.DOV.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Service
public class Web3jService {
    private final Web3j web3j;

    @Autowired
    public Web3jService(@Value("${ethereum.rpc.url}") String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    public Web3j getWeb3j() {
        return web3j;
    }
}