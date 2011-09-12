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
AssetEntry assetEntry = (AssetEntry)request.getAttribute("view.jsp-assetEntry");
AssetRenderer assetRenderer = (AssetRenderer)request.getAttribute("view.jsp-assetRenderer");

String title = (String)request.getAttribute("view.jsp-title");

if (Validator.isNull(title)) {
	title = assetRenderer.getTitle(locale);
}

String summary = StringUtil.shorten(assetRenderer.getSummary(locale), abstractLength);

PortalUtil.setPageSubtitle(title, request);
PortalUtil.setPageDescription(summary, request);
PortalUtil.setPageKeywords(PluginAssetUtil.getAssetKeywords(assetEntry.getClassName(), assetEntry.getClassPK()), request);
%>