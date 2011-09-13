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

<%@ include file="/html/portlet/asset_publisher/init.jsp" %>

<%
String randomNamespace = PortalUtil.generateRandomKey(request, "portlet_asset_publisher_edit_query_rule") + StringPool.UNDERLINE;

int index = ParamUtil.getInteger(request, "index", GetterUtil.getInteger((String)request.getAttribute("configuration.jsp-index")));
int queryLogicIndex = GetterUtil.getInteger((String)request.getAttribute("configuration.jsp-queryLogicIndex"));

boolean queryContains = true;
boolean queryAndOperator = false;
String queryName = "assetTags";
String queryValues = null;

if (queryLogicIndex >= 0) {
	queryContains = PrefsParamUtil.getBoolean(preferences, request, "queryContains" + queryLogicIndex, true);
	queryAndOperator = PrefsParamUtil.getBoolean(preferences, request, "queryAndOperator" + queryLogicIndex);
	queryName = PrefsParamUtil.getString(preferences, request, "queryName" + queryLogicIndex, "assetTags");
	queryValues = StringUtil.merge(preferences.getValues("queryValues" + queryLogicIndex , new String[0]));

	if (Validator.equals(queryName, "assetTags")) {
		queryValues = ParamUtil.getString(request, "queryTagNames" + queryLogicIndex, queryValues);
	}
	else {
		queryValues = ParamUtil.getString(request, "queryCategoryIds" + queryLogicIndex, queryValues);
	}
}
%>

<div class="aui-field-row query-row">
	<aui:select inlineField="<%= true %>" label="" name='<%= "queryContains" + index %>'>
		<aui:option label="contains" selected="<%= queryContains %>" value="true" />
		<aui:option label="does-not-contain" selected="<%= !queryContains %>" value="false" />
	</aui:select>

	<aui:select inlineField="<%= true %>" label="" name='<%= "queryAndOperator" + index %>'>
		<aui:option label="all" selected="<%= queryAndOperator %>" value="true" />
		<aui:option label="any" selected="<%= !queryAndOperator %>" value="false" />
	</aui:select>

	<aui:select cssClass="asset-query-name" id='<%= randomNamespace + "selector" %>' inlineLabel="left" label="of-the-following" name='<%= "queryName" + index %>'>
		<aui:option label="tags" selected='<%= Validator.equals(queryName, "assetTags") %>' value="assetTags" />
		<aui:option label="categories" selected='<%= Validator.equals(queryName, "assetCategories") %>' value="assetCategories" />
	</aui:select>

	<div class="aui-field tags-selector <%= Validator.equals(queryName, "assetTags") ? StringPool.BLANK : "aui-helper-hidden" %>">
		<liferay-ui:asset-tags-selector
			hiddenInput='<%= "queryTagNames" + index %>'
			curTags='<%= Validator.equals(queryName, "assetTags") ? queryValues : null %>'
			focus="<%= false %>"
		/>
	</div>

	<div class="aui-field categories-selector <%= Validator.equals(queryName, "assetCategories") ? StringPool.BLANK : "aui-helper-hidden" %>">
		<liferay-ui:asset-categories-selector
			hiddenInput='<%= "queryCategoryIds" + index %>'
			curCategoryIds='<%= Validator.equals(queryName, "assetCategories") ? queryValues : null %>'
			focus="<%= false %>"
		/>
	</div>
</div>

<aui:script use="aui-base">
	var select = A.one('#<portlet:namespace /><%= randomNamespace %>selector');

	if (select) {
		var row = select.ancestor('.query-row');

		if (row) {
			select.on(
				'change',
				function(event) {
					var tagsSelector = row.one('.tags-selector');
					var categoriesSelector = row.one('.categories-selector');

					if (select.val() == 'assetTags') {
						if (tagsSelector) {
							tagsSelector.show();
						}

						if (categoriesSelector) {
							categoriesSelector.hide();
						}
					}
					else {
						if (tagsSelector) {
							tagsSelector.hide();
						}

						if (categoriesSelector) {
							categoriesSelector.show();
						}
					}
				}
			);
		}
	}
</aui:script>