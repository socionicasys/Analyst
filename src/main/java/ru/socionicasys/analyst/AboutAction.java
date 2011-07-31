package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class AboutAction extends AbstractAction {
	private static final Logger logger = LoggerFactory.getLogger(AboutAction.class);

	private final Component parent;

	AboutAction(Component parent) {
		super("О программе");
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JTextArea info = new JTextArea(6, 40);
		info.setEditable(false);
		info.setBackground(panel.getBackground());
		info.setEditable(false);
		info.setText(String.format("Программа \"%s\"\n" +
			'\n' +
			"© Школа системной соционики, Киев, 2010 г.\n" +
			"http://www.socionicasys.ru\n" +
			"Версия: %s",
			VersionInfo.getApplicationName(),
			VersionInfo.getVersion()
		));

		JTextArea licText = new JTextArea(15, 40);
		licText.setEditable(false);
		licText.setLineWrap(true);
		licText.setMargin(new Insets(3, 3, 3, 3));
		licText.setWrapStyleWord(true);
		licText.setAutoscrolls(false);

		licText.setText("ВНИМАНИЕ!!! Не удалось отрыть файл лицензии.\n\n" +
			"Согласно условий оригинальной лицензии GNU GPL, программное обеспечение должно поставляться вместе с текстом оригинальной лицензии.\n\n" +
			"Отсутствие такой лицензии может неправомерно ограничивать ваши права как пользователя. \n\n" +
			"Требуйте получение исходной лицензии от поставщика данного программного продукта.\n\n" +
			"Оригинальный текст GNU GPL на английском языке вы можете прочитать здесь: http://www.gnu.org/copyleft/gpl.html");

		InputStream is = getClass().getClassLoader().getResourceAsStream("license.txt");
		if (is != null) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				try {
					StringBuilder license = new StringBuilder();
					String next = br.readLine();
					while (next != null) {
						license.append(next).append('\n');
						next = br.readLine();
					}
					licText.setText(license.toString());
				} catch (IOException ex) {
					logger.error("Error opening license file", ex);
				} finally {
					try {
						br.close();
					} catch (IOException ex) {
						logger.error("Error closing BufferedReader", ex);
					}
				}
			} catch (UnsupportedEncodingException ex) {
				logger.error("Error creating BufferedReader", ex);
			}
		}

		JScrollPane licenseScrl = new JScrollPane(licText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		licText.getCaret().setDot(0);
		licText.insert("", 0);

		Border border = BorderFactory.createTitledBorder("Лицензия:");
		licenseScrl.setBorder(border);

		panel.add(info);
		panel.add(licenseScrl);

		JOptionPane.showOptionDialog(parent,
			panel,
			"О программе",
			JOptionPane.INFORMATION_MESSAGE,
			JOptionPane.PLAIN_MESSAGE,
			null,
			new Object[]{"Закрыть"},
			null
		);
	}
}
