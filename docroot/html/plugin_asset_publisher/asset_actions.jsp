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
boolean showIconLabel = ((Boolean)request.getAttribute("view.jsp-showIconLabel")).booleanValue();

AssetRenderer assetRenderer = (AssetRenderer)request.getAttribute("view.jsp-assetRenderer");

boolean showEditURL = ParamUtil.getBoolean(request, "showEditURL", true);

PortletURL editPortletURL = assetRenderer.getURLEdit(liferayPortletRequest, liferayPortletResponse);

if (showEditURL && (editPortletURL != null)) {
	editPortletURL.setWindowState(WindowState.MAXIMIZED);
	editPortletURL.setPortletMode(PortletMode.VIEW);

	editPortletURL.setParameter("redirect", currentURL);
}

Group stageableGroup = themeDisplay.getScopeGroup();

if (themeDisplay.getScopeGroup().isLayout()) {
	stageableGroup = layout.getGroup();
}
%>

<c:if test="<%= assetRenderer.hasEditPermission(permissionChecker) && (editPortletURL != null) && !stageableGroup.hasStagingGroup() %>">
	<div class="lfr-meta-actions asset-actions">
		<liferay-ui:icon
			image="edit"
			label="<%= showIconLabel %>"
			message='<%= showIconLabel ? LanguageUtil.format(pageContext, "edit-x-x", new Object[] {"aui-helper-hidden-accessible", HtmlUtil.escape(assetRenderer.getTitle(locale))}) : LanguageUtil.format(pageContext, "edit-x", HtmlUtil.escape(assetRenderer.getTitle(locale))) %>'
			url="<%= editPortletURL.toString() %>"
		/>
	</div>
</c:if>