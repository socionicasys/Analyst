package ru.socionicasys.analyst;

import java.util.EventListener;

public interface ADocumentChangeListener extends EventListener {
	void aDocumentChanged(ADocument document);
}
