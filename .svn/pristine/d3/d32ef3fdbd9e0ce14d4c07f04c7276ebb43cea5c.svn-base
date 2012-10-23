package sample;

import java.io.IOException;

import rescuecore2.Constants;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.connection.ConnectionException;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;
import sample.agent.SampleAmbulanceTeam;
import sample.agent.SampleBirdMan;
import sample.agent.SampleFireBrigade;
import sample.agent.SamplePoliceForce;

/**
 * Launch SEU's Agents
 */
public final class Main {

	private static final String RANDOM_SEED = "122";

	// Rule 中规定的智能体最大数量
	private static final int maxAgent = 30;
	private static final int maxCentre = 5;

	private Main() {
	}

	/**
	 * @param 启动参数的定义如下
	 *            -p <port>, -h <hostname>, -fb <fire brigades>, -pf <police
	 *            forces>, -at <ambulance teams>
	 */
	public static void main(String[] args) {

		try {
			Registry.SYSTEM_REGISTRY
					.registerEntityFactory(StandardEntityFactory.INSTANCE);
			Registry.SYSTEM_REGISTRY
					.registerMessageFactory(StandardMessageFactory.INSTANCE);
			Registry.SYSTEM_REGISTRY
					.registerPropertyFactory(StandardPropertyFactory.INSTANCE);
			Config config = new Config();

			args = CommandLineOptions.processArgs(args, config);

			// Keep
			config.setValue(Constants.RANDOM_SEED_KEY, RANDOM_SEED);

			int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY,
					Constants.DEFAULT_KERNEL_PORT_NUMBER);

			String defaultHost = config.getValue(
					Constants.KERNEL_HOST_NAME_KEY,
					Constants.DEFAULT_KERNEL_HOST_NAME);

			int fb = -1;
			int pf = -1;
			int at = -1;
			int bm = -1;
			fb = (args[0].equals("-")) ? maxAgent : Integer.parseInt(args[0]);

			pf = (args[1].equals("-")) ? maxAgent : Integer.parseInt(args[1]);

			at = (args[2].equals("-")) ? maxAgent : Integer.parseInt(args[2]);

			bm = (args[3].equals("-")) ? maxAgent : Integer.parseInt(args[3]);

			String host = (args[4].equals("-")) ? defaultHost : args[4];

			ComponentLauncher launcher = new TCPComponentLauncher(host, port,
					config);

			// 建立连接
			connect(launcher, fb, pf, at, bm, config);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void connect(ComponentLauncher launcher, int fb, int pf,
			int at, int bm, Config config) throws InterruptedException,
			ConnectionException {

		int i = 0;
		try {
			while (fb-- > 0) {
				launcher.connect(new SampleFireBrigade());
				System.out.println("Connecting Fire Brigade " + (i++)
						+ "... Success");
			}
		} catch (ComponentConnectionException e) {
			System.out.println("Fire Brigade Conncetion Done");
		}

		i = 0;
		try {
			while (pf-- > 0) {
				launcher.connect(new SamplePoliceForce());
				System.out.println("Connecting Police Force " + (i++)
						+ "... Success");
			}
		} catch (ComponentConnectionException e) {
			System.out.println("Police Force Connection Done");
		}

		i = 0;
		try {
			while (at-- > 0) {
				launcher.connect(new SampleAmbulanceTeam());
				System.out.println("Connecting Ambulance Team " + (i++)
						+ "... Success");
			}
		} catch (ComponentConnectionException e) {
			System.out.println("Ambulance Team Connection Done");
		}
		i = 0;
		try {
			while (bm-- > 0) {
				launcher.connect(new SampleBirdMan());
				System.out.println("Connecting Bird Man " + (i++)
						+ "... Success");
			}
		} catch (ComponentConnectionException e) {
			System.out.println("Bird Man Connection Done");
		}

	}
}
