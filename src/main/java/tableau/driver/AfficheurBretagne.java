package tableau.driver;

import java.util.List;

/**
 * gere la partie protocole avec le tableau 
 * 
 * @author remi
 *
 */
public class AfficheurBretagne implements Network_iface {

	private Network network;
	private boolean debug;

	public void init(String port, int speed) {
		if (! network.connect(port, speed)) {
			throw new RuntimeException("sorry, there was an error connecting");
		}
		// board initialization 
		char ouverture[] = {0x01,0x01,0x30,0xff,0x32,0x30,0x37,0x03,0x01,0x01,0x31,0x32,0x38,0x30,0x35,0x32,0x30,0x31,0x33,0x32,0x32,0x31,0x31,0xff,0x31,0x39,0x33,0x03};
		network.writeSerial(new String(ouverture));
	}
	
	public void init() {
		init(network.getPortList().firstElement(), 9600);
	}
	
	public AfficheurBretagne() {
		network = new Network(0, this, 255);
		if (network.getPortList() != null && ! network.getPortList().isEmpty()) {
			init();
		} else {
			System.out.println("aucun port de detecte !! on continue, mais c'est juste pour du test");
		}
	}

	private String createTrame(String aTrame) {
		StringBuffer buf = new StringBuffer();
		long xor=0x01;
		for(int i=0;i<aTrame.length();i++)
			xor = xor ^ aTrame.charAt(i);	
		xor = xor ^ 0xff;
		buf.append((char)0x01)
			.append(aTrame)
			.append((char)0xff)
			.append(String.format("%03d", xor))			// 3 digits exactement
			.append((char)0x03);
		return buf.toString();
	}
	
	public synchronized void affiche(List<String> lignes) throws InterruptedException {
		StringBuffer buf = new StringBuffer();
		char dix22[] = {0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22};
//		String dix22 = "bbbbbbbbbb";
//		buf .append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22)
//			.append("aaaaaaaaaaaaaaaaaaaa").append(dix22);   

		buf .append("                    ").append(dix22)
		.append("                    ").append(dix22)
		.append("                    ").append(dix22)
		.append("                    ").append(dix22)
		.append("                    ").append(dix22)
		.append("                    ").append(dix22)
		.append("                    ").append(dix22)
		.append("                    ").append(dix22);   
		// ( 20 blancs + 10 0x22 ) * 8 = 240 c
		
		int transfert[] = {			 
				218,219,220,161,162,163,164,165,166,167,168,169,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
				215,216,217,152,153,154,155,156,157,158,159,160, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
				212,213,214,191,192,193,194,195,196,197,198,199, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
				 31,  0,  1,182,183,184,185,186,187,188,189,190, 92, 93, 94, 95, 96, 97, 98, 99,100,101,102,103,104,105,106,107,108,109,
				 91, 60, 61,221,222,223,224,225,226,227,228,229,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139 };
		// 30*5 
		
		String ligne;
		for(int iLigne=0; iLigne<5 && iLigne<lignes.size(); iLigne++) {
			ligne = lignes.get(iLigne).replaceAll("\\_", " ");
			for(int i=0;i<ligne.length() && i<20;i++) {
				buf.setCharAt(transfert[iLigne*30+i+10], ligne.charAt(i));
			}
		}
		
		// envoie message dans la memoire
//		char debutTrame[] = {0x01,0x32,0x01,0x01,0x63,0x7f,0x7f,0x01,0x01,0x14,0x01,0x01,0x01,0x14,0x09,0x01,0x01,0x17,0x3b,0x01,0x01,0x17,0x3b};
		char debutTrame[] = {0x01,0x32,
				0x01,0x01,0x63,0x7f,0x7f,
				0x01,0x01,0x14,
				0x01,0x01,0x01,0x14,0x09,
				0x01,0x01,0x17,0x3b,
				0x01,0x01,0x17,0x3b};
		buf.insert(0, debutTrame);

		String trame1 = createTrame(buf.toString());
		network.writeSerial(trame1);
		Thread.sleep(100);

		// demande au message de s'afficher
		char ch[]={0x01,0x01,0x33,0x01,0x00,0xff,'2','0','5',0x03};
		network.writeSerial(new String(ch));
	}
	
	/**
	 * Implementing {@link net.Network_iface#networkDisconnected(int)}, which is
	 * called when the connection has been closed. In this example, the program
	 * is ended.
	 * 
	 * @see net.Network_iface
	 */
	public void networkDisconnected(int id) {
		
	}

	/**
	 * Implementing {@link net.Network_iface#parseInput(int, int, int[])} to
	 * handle messages received over the serial port. In this example, the
	 * received bytes are written to command line (0 to 254) and the message is
	 * sent back over the same serial port.
	 * 
	 * @see net.Network_iface
	 */
	public void parseInput(int id, int numBytes, int[] message) {
		System.out.print("received the following message: ");
		System.out.print(message[0]);
		for (int i = 1; i < numBytes; ++i) {
			System.out.print(", ");
			System.out.print(message[i]);
		}
		System.out.println();
	}

	/**
	 * Implementing {@link net.Network_iface#writeLog(int, String)}, which is
	 * used to write information concerning the connection. In this example, all
	 * the information is simply written out to command line.
	 * 
	 * @see net.Network_iface
	 */
	public void writeLog(int id, String text) {
		System.out.println("   log:  |" + text + "|");
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
		network.setDebug(debug);
	}
}
