package com.DefiOptionVault.DOV;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import Wrapper.DovWrapper;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class DovApplicationTests {
	private final String USDC = "0xe059aA96255990826D0d62c62462Feea47AF82a7";
	private final String DOV_ADDRESS = "0xC1f89aE003F0264792C3Cf985c3bCAA8c1C7b3C0";
	private final String ADMIN_ADDRESS = "0x6472A2F7187C49f05022b4F04b8f4545a7f88797";
	private final String PRIVATE_KEY = "0x4e09642cccd2e9bbabb4f1136aa128986d46037e953cc21006f489a547ff995f";
	private final String RPC = "https://polygon-mumbai.g.alchemy.com/v2/-cnnQMgoJVRgbeWlXVmUWJnn4YylXDng";

	@Test
	void balanceOf() throws IOException {
		// Web3j 초기화
		Web3j web3j = Web3j.build(new HttpService(RPC));

		// 호출할 함수와 파라미터 설정
		String functionName = "balanceOf";
		String accountAddress = "0x6472A2F7187C49f05022b4F04b8f4545a7f88797"; // 조회할 계정 주소
		List<Type> inputParameters = Collections.singletonList(new Address(accountAddress));
		List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<org.web3j.abi.datatypes.Uint>() {});

		// 함수 객체 생성
		Function function = new Function(
				functionName,
				inputParameters,
				outputParameters);

		// 트랜잭션을 인코딩
		String encodedFunction = FunctionEncoder.encode(function);

		// 함수 호출
		EthCall response = web3j.ethCall(
						org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
								null, USDC, encodedFunction),
						DefaultBlockParameterName.LATEST)
				.send();

		// 결과를 디코딩
		List<Type> result = FunctionReturnDecoder.decode(
				response.getValue(), function.getOutputParameters());

		// 결과 출력
		if (!result.isEmpty()) {
			BigInteger balance = (BigInteger) result.get(0).getValue();
			System.out.println("Balance of account " + accountAddress + " is: " + balance);
		} else {
			System.out.println("Empty response");
		}
	}

	@Test
	void bootstrap() throws IOException {
		// 계정 연결
		Credentials credentials = Credentials.create(PRIVATE_KEY);
		Web3j web3j = Web3j.build(new HttpService(RPC));

		// Specify Polygon Mumbai network ID (80001)
		long chainId = 80001;

		TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId);

		ContractGasProvider gasProvider = new DefaultGasProvider();

		// Create Contract instance
		DovWrapper contract = DovWrapper.load(DOV_ADDRESS, web3j, transactionManager, gasProvider);

		// Set values for the function parameters
		BigInteger[] strikes = new BigInteger[]{BigInteger.valueOf(1000), BigInteger.valueOf(2000)};
		BigInteger expiry = BigInteger.valueOf(1700203297);
		String expirySymbol = "EXPIRY_SYMBOL";

		// Call the bootstrap function
		try {
			TransactionReceipt transactionReceipt = contract.bootstrap(Arrays.asList(strikes), expiry, expirySymbol).send();
			System.out.println("Transaction Hash: " + transactionReceipt.getTransactionHash());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
