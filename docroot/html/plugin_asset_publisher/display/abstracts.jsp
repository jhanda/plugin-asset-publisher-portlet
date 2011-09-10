<%@ include file="/html/plugin_asset_publisher/init.jsp" %>

<%
List results = (List)request.getAttribute("view.jsp-results");

int assetEntryIndex = ((Integer)request.getAttribute("view.jsp-assetEntryIndex")).intValue();

AssetEntry assetEntry = (AssetEntry)request.getAttribute("view.jsp-assetEntry");
AssetRendererFactory assetRendererFactory = (AssetRendererFactory)request.getAttribute("view.jsp-assetRendererFactory");
AssetRenderer assetRenderer = (AssetRenderer)request.getAttribute("view.jsp-assetRenderer");

boolean show = ((Boolean)request.getAttribute("view.jsp-show")).booleanValue();

request.setAttribute("view.jsp-showIconLabel", true);

String title = (String)request.getAttribute("view.jsp-title");

if (Validator.isNull(title)) {
	title = assetRenderer.getTitle(locale);
}

PortletURL viewFullContentURL = renderResponse.createRenderURL();

viewFullContentURL.setParameter("struts_action", "/asset_publisher/view_content");
viewFullContentURL.setParameter("assetEntryId", String.valueOf(assetEntry.getEntryId()));
viewFullContentURL.setParameter("type", assetRendererFactory.getType());

if (Validator.isNotNull(assetRenderer.getUrlTitle())) {
	if (assetRenderer.getGroupId() != scopeGroupId) {
		viewFullContentURL.setParameter("groupId", String.valueOf(assetRenderer.getGroupId()));
	}

	viewFullContentURL.setParameter("urlTitle", assetRenderer.getUrlTitle());
}

String viewFullContentURLString = viewFullContentURL.toString();

viewFullContentURLString = HttpUtil.setParameter(viewFullContentURLString, "redirect", currentURL);

String summary = StringUtil.shorten(assetRenderer.getSummary(locale), abstractLength);
String viewURL = viewInContext ? assetRenderer.getURLViewInContext(liferayPortletRequest, liferayPortletResponse, viewFullContentURLString) : viewFullContentURL.toString();
String viewURLMessage = viewInContext ? assetRenderer.getViewInContextMessage() : "read-more-x-about-x";

viewURL = _checkViewURL(viewURL, currentURL, themeDisplay);

%>

<c:if test="<%= show && assetRenderer.hasViewPermission(permissionChecker) %>">
	<div class="asset-abstract">

		<liferay-util:include 
			page="/html/plugin_asset_publisher/asset_actions.jsp"
			portletId ="<%= portletId %>"
			servletContext="<%=this.getServletContext() %>"
		/>

		<h3 class="asset-title">
			<c:choose>
				<c:when test="<%= Validator.isNotNull(viewURL) %>">
					<a href="<%= viewURL %>"><img alt="" src="<%= assetRendererFactory.getIconPath(renderRequest) %>" /> <%= HtmlUtil.escape(title) %></a>
				</c:when>
				<c:otherwise>
					<img src="<%= assetRendererFactory.getIconPath(renderRequest) %>" alt="" /> <%= HtmlUtil.escape(title) %>
				</c:otherwise>
			</c:choose>
		</h3>

		<div class="asset-content">
			<div class="asset-summary">

				<%
				String path = assetRenderer.render(renderRequest, renderResponse, AssetRenderer.TEMPLATE_ABSTRACT);

				request.setAttribute(WebKeys.ASSET_RENDERER, assetRenderer);
				request.setAttribute("ASSET_PUBLISHER_ABSTRACT_LENGTH", abstractLength);
				%>

				<c:choose>
					<c:when test="<%= path == null %>">
						<%= summary %>
					</c:when>
					<c:otherwise>
						<liferay-util:include page="<%= path %>" portletId="<%= assetRendererFactory.getPortletId() %>" />
					</c:otherwise>
				</c:choose>
			</div>

			<c:if test="<%= Validator.isNotNull(viewURL) %>">
				<div class="asset-more">
					<a href="<%= viewURL %>"><liferay-ui:message arguments='<%= new Object[] {"aui-helper-hidden-accessible", HtmlUtil.escape(assetRenderer.getTitle(locale))} %>' key="<%= viewURLMessage %>" /> &raquo; </a>
				</div>
			</c:if>
		</div>

		<div class="asset-metadata">
			<%@ include file="/html/plugin_asset_publisher/asset_metadata.jspf" %>
		</div>
	</div>

	<c:if test="<%= (assetEntryIndex + 1) == results.size() %>">
		<div class="final-separator"><!-- --></div>
	</c:if>
</c:if>