import com.mario.config.gateway.SocketGatewayConfig;
import com.mario.gateway.socket.udt.NettyUDTSocketGateway;
import com.nhb.common.utils.Initializer;

public class TestSocketGateway {

	static {
		Initializer.bootstrap(TestSocketGateway.class);
	}

	public static void main(String[] args) throws Exception {
		SocketGatewayConfig socketGatewayConfig = new SocketGatewayConfig();
		socketGatewayConfig.setPort(9999);
		socketGatewayConfig.setName("Test");
		NettyUDTSocketGateway gateway = new NettyUDTSocketGateway();
		gateway.init(socketGatewayConfig);
		gateway.start();
	}

}
