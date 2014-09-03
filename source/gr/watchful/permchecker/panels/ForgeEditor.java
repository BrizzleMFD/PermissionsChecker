package gr.watchful.permchecker.panels;

import gr.watchful.permchecker.datastructures.ForgeType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ForgeEditor extends JPanel {
	private JLabel label;
	private JComboBox<ForgeType> forgeTypeEditor;
	private JTextField forgeVersionEditor;
    private ChangeListener changeListener;
    private String oldVersion;
    private ForgeType oldType;

	public ForgeEditor(String name) {
        this(name, null);
    }

    public ForgeEditor(String name, ChangeListener changeListener) {
        this.changeListener = changeListener;
        oldVersion = "";
        oldType = ForgeType.RECOMMENDED;

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentX(0);

		label = new JLabel(name);
		label.setMinimumSize(new Dimension(90, 21));
		label.setMaximumSize(new Dimension(90, 21));
		label.setPreferredSize(new Dimension(90, 21));
		this.add(label);

		forgeTypeEditor = new JComboBox<>(ForgeType.values());
		forgeTypeEditor.setMaximumSize(new Dimension(120, 21));
        forgeTypeEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(oldType.equals(getForgeType())) return;
                oldType = getForgeType();
                notifyChanged();
            }
        });
		this.add(forgeTypeEditor);

		this.add(Box.createHorizontalGlue());

		forgeVersionEditor = new JTextField();
		forgeVersionEditor.setMaximumSize(new Dimension(120, 21));
		forgeVersionEditor.setPreferredSize(new Dimension(120, 21));
        forgeVersionEditor.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // do nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(oldVersion.equals(forgeVersionEditor.getText())) return;
                oldVersion = forgeVersionEditor.getText();
                notifyChanged();
            }
        });
		this.add(forgeVersionEditor);
	}

	public void setForgeType(ForgeType forgeType) {
		forgeTypeEditor.setSelectedItem(forgeType);
	}

	public void setForgeVersion(int forgeVersion) {
		forgeVersionEditor.setText(Integer.toString(forgeVersion));
	}

	public ForgeType getForgeType() {
		return (ForgeType) forgeTypeEditor.getSelectedItem();
	}

	public int getForgeVersion() {
		return Integer.parseInt(forgeVersionEditor.getText());
	}

    public void notifyChanged() {
        if(changeListener == null) return;
        changeListener.stateChanged(new ChangeEvent(this));
    }
}
