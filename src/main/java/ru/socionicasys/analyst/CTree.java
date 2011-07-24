package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.types.Sociotype;

import java.awt.*;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author Виктор
 */
@SuppressWarnings("serial")
public class CTree extends JTree implements ADocumentChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(CTree.class);
	private static final Color TEXT_COLOR = new Color(30, 120, 255);
	private static final int SCALE = 10;
	private static final float PERCENT = 100.0f;

	private final DocumentHolder documentHolder;
	private final DefaultMutableTreeNode rootNode;
	private final DefaultTreeModel treeModel;

	private Map<Sociotype, DefaultMutableTreeNode> nodeMap;
	private Map<Sociotype, Integer> matchCount;
	private Map<Sociotype, Integer> missCount;
	private Map<Sociotype, Float> matchCoefficients;

	public CTree(DocumentHolder documentHolder) {
		this.documentHolder = documentHolder;
		rootNode = new DefaultMutableTreeNode(documentHolder.getModel().getProperty(Document.TitleProperty));
		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);

		nodeMap = new EnumMap<Sociotype, DefaultMutableTreeNode>(Sociotype.class);
		for (Sociotype sociotype : Sociotype.values()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode();
			rootNode.add(node);
			nodeMap.put(sociotype, node);
		}

		matchCount = new EnumMap<Sociotype, Integer>(Sociotype.class);
		missCount = new EnumMap<Sociotype, Integer>(Sociotype.class);
		matchCoefficients = new EnumMap<Sociotype, Float>(Sociotype.class);

		setCellRenderer(new HistogramCellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setEditable(false);
		toggleClickCount = 1;
		putClientProperty("JTree.lineStyle", "None");

		updateTree(documentHolder.getModel());
	}

	private void updateTree(ADocument document) {
		if (document == null) {
			return;
		}

		for (Sociotype sociotype : Sociotype.values()) {
			matchCount.put(sociotype, 0);
			missCount.put(sociotype, 0);
		}

		// Подсчет числа соответствий/несоответствий
		for (Entry<ASection, AData> dataEntry : document.getADataMap().entrySet()) {
			AData data = dataEntry.getValue();
			String aspect = data.getAspect();
			String secondAspect = data.getSecondAspect();
			String modifier = data.getModifier();
			String dimension = data.getDimension();
			String sign = data.getSign();
			String mv = data.getMV();

			if (aspect == null || aspect.equals(AData.DOUBT)) {
				continue;
			}
			if (modifier != null && modifier.equals(AData.JUMP)) {
				continue;
			}

			for (Sociotype sociotype : Sociotype.values()) {
				if (SocionicsType.matches(sociotype, aspect, secondAspect, sign, dimension, mv)) {
					matchCount.put(sociotype, matchCount.get(sociotype) + 1);
				} else {
					missCount.put(sociotype, missCount.get(sociotype) + 1);
				}
			}
		}

		// Подсчет коэффициентов соответствий = число соответствий/чисто несоответствий
		boolean exactMatch = false;
		for (Sociotype sociotype : Sociotype.values()) {
			if (missCount.get(sociotype) != 0) {
				matchCoefficients.put(sociotype, (float)matchCount.get(sociotype) / missCount.get(sociotype));
			} else {
				exactMatch = true;
				matchCoefficients.put(sociotype, Float.POSITIVE_INFINITY);
			}
		}

		// Масштабирование коэффециентов, максимальный из них должен быть равен SCALE
		float maxValue = Collections.max(matchCoefficients.values());
		for (Sociotype sociotype : Sociotype.values()) {
			Float coefficient = matchCoefficients.get(sociotype);
			if (exactMatch) {
				matchCoefficients.put(sociotype, coefficient.isInfinite() ? (float)SCALE : 0.0f);
			} else {
				matchCoefficients.put(sociotype, (float)SCALE * coefficient / maxValue);
			}
		}

		StringBuilder barBuilder = new StringBuilder(SCALE + 1);
		for (int i = 0; i <= SCALE; i++) {
			barBuilder.append('█');
		}
		String bar = barBuilder.toString();

		// Заполнение гистограммы
		for (Sociotype sociotype : Sociotype.values()) {
			DefaultMutableTreeNode node = nodeMap.get(sociotype);
			float coefficient = matchCoefficients.get(sociotype);
			node.setUserObject(String.format("%1$s : %2$s %3$2.0f",
				sociotype.getAbbreviation(),
				bar.substring(0, (int) coefficient + 1),
				PERCENT * coefficient / (float)SCALE
			));
		}

		treeModel.reload();
	}

	public JScrollPane getContainer() {
		JScrollPane sp = new JScrollPane(this);
		sp.setPreferredSize(new Dimension(200, 500));
		return sp;
	}

	@Override
	public void aDocumentChanged(ADocument document) {
		updateTree(document);
	}

	private static class HistogramCellRenderer implements TreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			JLabel label = new JLabel();
			if (leaf) {
				label.setText(value.toString());
				label.setFont(new Font(Font.MONOSPACED, Font.BOLD, label.getFont().getSize()));
				label.setForeground(TEXT_COLOR);
			} else {
				label.setText(" ");
				label.setForeground(Color.BLACK);
			}
			return label;
		}
	}

	public String getReport() {
		if (!documentHolder.getModel().getADataMap().isEmpty()) {
			StringBuilder reportBuilder = new StringBuilder(
				"<br/>" +
				"<h2> Соответствие ТИМу </h2>" +
				"Приведенная ниже таблица позволяет определить наиболее вероятный ТИМ типируемого.<br/>" +
				"Для получения данных таблицы использовался следующий алгоритм. Каждый из отмеченных экспертом фрагментов текста<br/> " +
				"типируемого проверяется на соответствие обработке информации каждым из 16 ТИМов. Если фрагмент соответствует модели <br/>" +
				"обработки информации для данного ТИМа, значение в столбце \"Соответствие\" для данного ТИМа будет увеличено на 1.<br/>" +
				"Если нет, соответственно, увеличивается значение  в столбце \"Несоответствие\" для данного ТИМа.<br/><br/>" +
				"В столбце \"Коэффициент соответствия\" приведен нормализованный расчетный коэффициент, который рассчитывается для каждого ТИМа " +
				"по формуле:<br/>   <code> К.С. = NORM<small style=\"vertical-align:sub;color:black\"> 100</small>( СООТВЕТСТВИЕ / НЕСООТВЕТСТВИЕ )</code><br/>" +
				"Этот коэффициент применяется для выделения наиболее вероятного ТИМа,<br/>" +
				"но не следует рассматривать его как математическую вероятность определения ТИМа. <br/><br/>" +
				"<table title=\"TIM analysis\" border=1 width=\"80%\">" +
				"<tr>\n" +
				"	<th width=\"40%\"> ТИМ </th>\n" +
				"	<th width=\"20%\"> Соответствие </th>\n" +
				"	<th width=\"20%\"> Несоответствие </th>\n" +
				"	<th width=\"20%\"> Коэффициент соответствия </th>\n" +
				"</tr>\n"
			);
			for (Sociotype sociotype : Sociotype.values()) {
				reportBuilder.append(String.format(
					"<tr>\n" +
					"	<td style=\"font-weight:bold\">%s</td>\n" +
					"		<td align=\"center\">%s </td>\n" +
					"		<td align=\"center\">%s </td>\n" +
					"		<td align=\"center\"> %2.0f </td>\n" +
					"</tr>\n",
					sociotype,
					matchCount.get(sociotype),
					matchCount.get(sociotype),
					PERCENT * matchCoefficients.get(sociotype) / (float)SCALE
				));
			}
			reportBuilder.append("</table>");
			return reportBuilder.toString();
		} else {
			return "<br/><h2> Невозможно определить ТИМ </h2><br/>";
		}
	}
}
