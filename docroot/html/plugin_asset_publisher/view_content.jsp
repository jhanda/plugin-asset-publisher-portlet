<%--
/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */
--%>

<%@ include file="/html/plugin_asset_publisher/init.jsp" %>

<%
long assetEntryId = ParamUtil.getLong(request, "assetEntryId");
String type = ParamUtil.getString(request, "type");
long groupId = ParamUtil.getLong(request, "groupId", scopeGroupId);
String urlTitle = ParamUtil.getString(request, "urlTitle");

boolean show = true;
boolean print = ParamUtil.getString(request, "viewMode").equals(Constants.PRINT);

List results = new ArrayList();

int assetEntryIndex = 0;

AssetEntry assetEntry = null;

String className = StringPool.BLANK;
long classPK = 0;

try {
	AssetRendererFactory assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByType(type);
	AssetRenderer assetRenderer = null;

	if (Validator.isNotNull(urlTitle)) {
		assetRenderer = assetRendererFactory.getAssetRenderer(groupId, urlTitle);

		className = assetRendererFactory.getClassName();
		classPK = assetRenderer.getClassPK();

		assetEntry = assetRendererFactory.getAssetEntry(className, classPK);
	}
	else {
		assetEntry = assetRendererFactory.getAssetEntry(assetEntryId);

		className = PortalUtil.getClassName(assetEntry.getClassNameId());
		classPK = assetEntry.getClassPK();

		if (portletName.equals(PortletKeys.MY_WORKFLOW_INSTANCES) || portletName.equals(PortletKeys.MY_WORKFLOW_TASKS) || portletName.equals(PortletKeys.WORKFLOW_DEFINITIONS) || portletName.equals(PortletKeys.WORKFLOW_INSTANCES) || portletName.equals(PortletKeys.WORKFLOW_TASKS)) {
			long assetEntryVersionId = ParamUtil.getLong(request, "assetEntryVersionId");

			assetRenderer = assetRendererFactory.getAssetRenderer(assetEntryVersionId, AssetRendererFactory.TYPE_LATEST);
		}
		else {
			assetRenderer = assetRendererFactory.getAssetRenderer(classPK, AssetRendererFactory.TYPE_LATEST_APPROVED);
		}

	}

	String title = assetRenderer.getTitle(locale);
	String summary = StringPool.BLANK;
	String viewURL = StringPool.BLANK;
	String viewURLMessage = StringPool.BLANK;
	String editURL = StringPool.BLANK;

	request.setAttribute("view.jsp-results", results);

	request.setAttribute("view.jsp-assetEntryIndex", new Integer(assetEntryIndex));

	request.setAttribute("view.jsp-assetEntry", assetEntry);
	request.setAttribute("view.jsp-assetRendererFactory", assetRendererFactory);
	request.setAttribute("view.jsp-assetRenderer", assetRenderer);

	request.setAttribute("view.jsp-title", title);

	request.setAttribute("view.jsp-show", new Boolean(show));
	request.setAttribute("view.jsp-print", new Boolean(print));
%>

	<div>
		<liferay-util:include 
			page="/html/plugin_asset_publisher/display/full_content.jsp" 
			portletId = "<%= portletId %>"
			servletContext="<%=this.getServletContext() %>"
		/>	
	</div>

	<liferay-util:include 
		page="/html/plugin_asset_publisher/asset_html_metadata.jsp"
		portletId = "<%= portletId %>"
		servletContext="<%=this.getServletContext() %>"
	 />

<%
	PortalUtil.addPortletBreadcrumbEntry(request, title, currentURL);
}
catch (NoSuchModelException nsme) {
	SessionErrors.add(renderRequest, NoSuchModelException.class.getName());
%>

	<liferay-util:include 
		page="/html/plugin_asset_publisher/error.jsp"
		portletId = "<%= portletId %>"
		servletContext="<%=this.getServletContext() %>"
	 />

<%
}
catch (Exception e) {
	_log.error(e);
}
%>

<%!
private static Log _log = LogFactoryUtil.getLog("portal-web.docroot.html.portlet.asset_publisher.view_content_jsp");
%>