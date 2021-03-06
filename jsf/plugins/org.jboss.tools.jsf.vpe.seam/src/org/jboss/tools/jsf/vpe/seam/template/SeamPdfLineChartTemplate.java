/******************************************************************************* 
 * Copyright (c) 2007-2009 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jsf.vpe.seam.template;

import org.jboss.tools.vpe.editor.context.VpePageContext;
import org.jboss.tools.vpe.editor.template.VpeCreationData;
import org.jboss.tools.vpe.editor.util.HTML;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SeamPdfLineChartTemplate extends SeamPdfAbstractTemplate {

	private final static String LINE_CHART = "/lineChart/lineChart.png"; //$NON-NLS-1$
	private final static String LINE_CHART_SERIES_NESTED = "/lineChart/lineChartSeriesNested.png"; //$NON-NLS-1$


	public VpeCreationData create(VpePageContext pageContext, Node sourceNode,
			nsIDOMDocument visualDocument) {
		nsIDOMElement visualElement = visualDocument
				.createElement(HTML.TAG_IMG);
		Node[] seriesNodes = SeamUtil.getChildsByName(pageContext, sourceNode,
				"p:series"); //$NON-NLS-1$
		if (seriesNodes != null && seriesNodes.length != 0) {
			SeamUtil.setImg(visualElement, LINE_CHART_SERIES_NESTED);
		} else {
			SeamUtil.setImg(visualElement, LINE_CHART);
		}
		copySizeAttrs(visualElement, (Element)sourceNode);
		return new VpeCreationData(visualElement);
	}

}
