package com.corejsf;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.corejsf.util.Messages;
import com.corejsf.util.Renderers;

@FacesRenderer(componentFamily = "javax.faces.Command", rendererType = "com.corejsf.TabbedPane")
public class TabbedPaneRenderer extends Renderer {
	private static Logger logger = Logger.getLogger("com.corejsf.util");

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	@Override
	public void decode(FacesContext context, UIComponent component) {
		Map<String, String> requestParams = context.getExternalContext()
				.getRequestParameterMap();
		String clientId = component.getClientId(context);

		String content = (String) (requestParams.get(clientId));
		if (content != null && !content.equals("")) {
			UITabbedPane tabbedPane = (UITabbedPane) component;
			tabbedPane.setContent(content);
		}

		component.queueEvent(new ActionEvent(component));
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("table", component);

		String styleClass = (String) component.getAttributes()
				.get("styleClass");
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, null);
		}

		writer.write("\n");
	}

	@Override
	public void encodeChildren(FacesContext context, UIComponent component)
			throws IOException {
		if (component.getChildCount() == 0) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("thead", component);
		writer.startElement("tr", component);
		writer.startElement("th", component);

		writer.startElement("table", component);
		writer.startElement("tbody", component);
		writer.startElement("tr", component);

		for (SelectItem item : Renderers.getSelectItems(component)) {
			encodeTab(context, writer, item, component);
		}

		writer.endElement("tr");
		writer.endElement("tbody");
		writer.endElement("table");

		writer.endElement("th");
		writer.endElement("tr");
		writer.endElement("thead");

		writer.write("\n");
	}

	private void encodeTab(FacesContext context, ResponseWriter writer,
			SelectItem item, UIComponent component) throws IOException {

		String tabText = getLocalizedTabText(component, item.getLabel());
		String content = (String) item.getValue();

		writer.startElement("td", component);
		writer.startElement("a", component);
		writer.writeAttribute("href", "#", "href");

		String clientId = component.getClientId(context);
		String formId = Renderers.getFormId(context, component);

		writer.writeAttribute("onclick", "document.forms['" + formId + "']['"
				+ clientId + "'].value='" + content + "'; "
				+ "document.forms['" + formId + "'].submit(); ", null);

		UITabbedPane tabbedPane = (UITabbedPane) component;
		String selectedContent = tabbedPane.getContent();

		String tabClass = null;
		if (content.equals(selectedContent)) {
			tabClass = (String) component.getAttributes().get(
					"selectedTabClass");
		} else {
			tabClass = (String) component.getAttributes().get("tabClass");
		}

		if (tabClass != null) {
			writer.writeAttribute("class", tabClass, null);
		}

		writer.write(tabText);

		writer.endElement("a");
		writer.endElement("td");
		writer.write("\n");
	}

	private String getLocalizedTabText(UIComponent tabbedPane, String key) {
		String bundle = (String) tabbedPane.getAttributes().get(
				"resourceBundle");
		String localizedText = null;

		if (bundle != null) {
			localizedText = Messages.getString(bundle, key, null);
		}

		if (localizedText == null) {
			localizedText = key;
		}

		return localizedText;
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {
		ResponseWriter writer = context.getResponseWriter();

		UITabbedPane tabbedPane = (UITabbedPane) component;
		String content = tabbedPane.getContent();

		writer.startElement("tbody", component);
		writer.startElement("tr", component);
		writer.startElement("td", component);

		if (content != null) {
			UIComponent facet = component.getFacet(content);
			if (facet != null) {
				if (facet.isRendered()) {
					facet.encodeBegin(context);
					if (facet.getRendersChildren()) {
						facet.encodeChildren(context);
					}
					facet.encodeEnd(context);
				}
			} else {
				includePage(context, component);
			}
		}

		writer.endElement("td");
		writer.endElement("tr");
		writer.endElement("tbody");

		writer.endElement("table");
		
		encodeHiddenField(context, writer, component);
	}

	private void includePage(FacesContext context, UIComponent component) {
		ExternalContext ec = context.getExternalContext();
		ServletContext sc = (ServletContext)ec.getContext();
		
		UITabbedPane tabbedPane = (UITabbedPane)component;
		String content = tabbedPane.getContent();
		
		ServletRequest request = (ServletRequest)ec.getRequest();
		ServletResponse response = (ServletResponse)ec.getResponse();
		
		try {
			sc.getRequestDispatcher(content).include(request, response);
		} catch (ServletException e) {
			logger.log(Level.WARNING, "Couldn't load page: " + content, e);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Couldn't load page: " + content, e);
		}
	}

	private void encodeHiddenField(FacesContext context, ResponseWriter writer,
			UIComponent component) throws IOException {
		writer.startElement("input", component);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("name", component.getClientId(context), null);
		writer.endElement("input");
	}
}
