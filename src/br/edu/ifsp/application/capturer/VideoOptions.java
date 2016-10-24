package br.edu.ifsp.application.capturer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.openni.VideoMode;

public class VideoOptions extends JDialog implements ActionListener {

	private List<VideoMode> modes;
	private VideoMode mode = null;

	private JComboBox<String> cbVideo;
	private JButton btSelect;

	public VideoOptions(JFrame parent, List<VideoMode> modes) {
		super(parent, "Video...", true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(500, 100);
		setLocationRelativeTo(this);

		this.modes = modes;

		initializeComponentsForm();

	}

	private void initializeComponentsForm() {

		cbVideo = new JComboBox<>(getModes());
		btSelect = new JButton("Select");

		btSelect.addActionListener(this);

		JPanel pnOption = new JPanel();
		pnOption.setBorder(new TitledBorder("Options"));
		pnOption.setLayout(new BorderLayout());
		pnOption.add(new JLabel("Color Video: "), BorderLayout.WEST);
		pnOption.add(cbVideo, BorderLayout.CENTER);
		pnOption.add(btSelect, BorderLayout.EAST);

		Container c = this.getContentPane();
		c.add(pnOption);
	}

	private String[] getModes() {
		String video[] = new String[modes.size()];
		for (int i = 0; i < modes.size(); i++) {
			VideoMode v = modes.get(i);
			String s = v.getResolutionX() + " x " + v.getResolutionY() + " @ " + v.getFps() + " FPS "
					+ v.getPixelFormat();
			video[i] = s;
		}
		return video;
	}

	public VideoMode getVideoMode(){
		return mode;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btSelect) {
			int index = cbVideo.getSelectedIndex();
			if (index != -1) {
				mode = modes.get(index);
			}
			this.dispose();
		}
	}

}
