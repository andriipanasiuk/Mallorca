package mallorcatour.frames;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import mallorcatour.game.core.Card;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.core.Spectrum;
import mallorcatour.game.core.Card.Suit;
import mallorcatour.game.core.Card.Value;
import mallorcatour.util.FrameUtils;
import mallorcatour.util.SerializatorUtils;

public class SpectrumFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int width = 420;
	private int height = 420;
	private int quadroSize = this.width / 14;
	int currentX;
	int currentY;
	int oldX;
	int oldY;
	private Map<Integer, Double> map;
	private double maxValue;
	private JPanel spectrumPanel;

	public SpectrumFrame() {
		initComponents();
		setDefaultCloseOperation(2);
		setResizable(false);
		setPreferredSize(new Dimension(this.width, this.height));
	}

	public SpectrumFrame(Spectrum spectrum, String title) {
		initComponents();
		setTitle(title);
		setDefaultCloseOperation(2);
		setResizable(false);
		setPreferredSize(new Dimension(this.width, this.height));
		prepareMap(spectrum);
	}

	private void prepareMap(Spectrum spectrum) {
		this.map = new HashMap<>();
		for (HoleCards cards : Spectrum.random()) {
			int hashCode = cards.hashCodeForValues();
			if (this.map.get(Integer.valueOf(hashCode)) != null)
				this.map.put(
						Integer.valueOf(hashCode),
						Double.valueOf(((Double) this.map.get(Integer.valueOf(hashCode))).doubleValue()
								+ spectrum.getWeight(cards)));
			else {
				this.map.put(Integer.valueOf(hashCode), Double.valueOf(spectrum.getWeight(cards)));
			}
		}
		this.maxValue = maxValue(this.map);
	}

	private void initComponents() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open...");
		fileMenu.add(openItem);
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = FrameUtils.openFileChooser(SpectrumFrame.this, "./");
				SpectrumFrame.this.setTitle(new File(path).getName());
				Spectrum spectrum = (Spectrum) SerializatorUtils.load(path, Spectrum.class);
				SpectrumFrame.this.prepareMap(spectrum);
				SpectrumFrame.this.spectrumPanel.repaint();
			}
		});
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		this.spectrumPanel = new SpectrumPanel();
		this.spectrumPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				SpectrumFrame.this.spectrumPanelMousePressed(evt);
			}
		});
		this.spectrumPanel.setBackground(new Color(255, 255, 255));
		this.spectrumPanel.setBorder(BorderFactory.createBevelBorder(0));

		setContentPane(this.spectrumPanel);
		pack();
	}

	private void spectrumPanelMousePressed(MouseEvent evt) {
		this.oldX = evt.getX();
		this.oldY = evt.getY();
		int first = this.oldY / this.quadroSize;
		int second = this.oldX / this.quadroSize;
		HoleCards cards = new HoleCards(new Card(Card.Value.valueOf(13 - first), Card.Suit.CLUBS), new Card(
				Card.Value.valueOf(13 - second), Card.Suit.DIAMONDS));

		JToolTip tip = new JToolTip();
		tip.setTipText(cards.toString() + ": " + this.map.get(Integer.valueOf(cards.hashCodeForValues()))
				+ " max value: " + this.maxValue);

		final Popup popup = new PopupFactory().getPopup(this, tip, evt.getXOnScreen(), evt.getYOnScreen());
		popup.show();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1500L);
					popup.hide();
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}
		}.start();
	}

	private <T> double maxValue(Map<T, Double> map) {
		double max = Double.MIN_VALUE;
		for (Map.Entry<T, Double> entry : map.entrySet()) {
			if (max < ((Double) entry.getValue()).doubleValue()) {
				max = ((Double) entry.getValue()).doubleValue();
			}
		}
		return max;
	}

	public static void main(String[] args) {
		new SpectrumFrame().setVisible(true);
	}

	private class SpectrumPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		SpectrumPanel() {
			setPreferredSize(new Dimension(SpectrumFrame.this.width, SpectrumFrame.this.height));
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (SpectrumFrame.this.map == null) {
				return;
			}
			int fontSize = 18;

			for (int i = 0; i < 13; i++) {
				g.setColor(Color.BLACK);
				g.setFont(new Font("Serif", 1, fontSize));
				g.drawString(Card.Value.valueOf(12 - i).toString(), (i + 1) * SpectrumFrame.this.quadroSize
						+ (SpectrumFrame.this.quadroSize - fontSize) / 2, (SpectrumFrame.this.quadroSize - fontSize)
						/ 2 + fontSize);

				g.drawString(Card.Value.valueOf(12 - i).toString(), 12, (i + 1) * SpectrumFrame.this.height / 14 + 24);
			}

			for (int i = 0; i < 13; i++)
				for (int j = 0; j < 13; j++) {
					HoleCards cards = new HoleCards(new Card(Card.Value.valueOf(12 - i), Card.Suit.DIAMONDS), new Card(
							Card.Value.valueOf(12 - j), Card.Suit.CLUBS));

					int color = 255 - (int) (255.0D * ((Double) SpectrumFrame.this.map.get(Integer.valueOf(cards
							.hashCodeForValues()))).doubleValue() / SpectrumFrame.this.maxValue);
					g.setColor(new Color(color, color, color));
					g.fillRect((i + 1) * SpectrumFrame.this.width / 14, (j + 1) * SpectrumFrame.this.height / 14,
							SpectrumFrame.this.width / 14, SpectrumFrame.this.height / 14);
				}
		}
	}
}
