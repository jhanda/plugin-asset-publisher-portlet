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

package com.liferay.plugin.portlet.assetpublisher.action;

import com.liferay.plugin.portlet.assetpublisher.util.PluginAssetPublisherUtil;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.asset.AssetTagException;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Brian Wing Shun Chan
 * @author Jeff Handa
 */
public class ConfigurationActionImpl extends DefaultConfigurationAction {

	@Override
	public void processAction(
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		try {
			String portletResource = ParamUtil.getString(
				actionRequest, "portletResource");

			PortletPreferences preferences =
				PortletPreferencesFactoryUtil.getPortletSetup(
					actionRequest, portletResource);

			if (cmd.equals("add-selection")) {
				PluginAssetPublisherUtil.addSelection(actionRequest, preferences);
			}
			else if (cmd.equals("move-selection-down")) {
				moveSelectionDown(actionRequest, preferences);
			}
			else if (cmd.equals("move-selection-up")) {
				moveSelectionUp(actionRequest, preferences);
			}
			else if (cmd.equals("remove-selection")) {
				removeSelection(actionRequest, preferences);
			}
			else if (cmd.equals("selection-style")) {
				setSelectionStyle(actionRequest, preferences);
			}
			else if (cmd.equals(Constants.UPDATE)) {
				String selectionStyle = preferences.getValue(
					"selection-style", "dynamic");

				if (selectionStyle.equals("dynamic")) {
					updateDynamicSettings(actionRequest, preferences);
				}
				else if (selectionStyle.equals("manual")) {
					updateManualSettings(actionRequest, preferences);
				}
			}

			if (SessionErrors.isEmpty(actionRequest)) {
				preferences.store();

				SessionMessages.add(
					actionRequest,
					portletConfig.getPortletName() + ".doConfigure");
			}

			String redirect = PortalUtil.escapeRedirect(
				ParamUtil.getString(actionRequest, "redirect"));

			if (Validator.isNotNull(redirect)) {
				actionResponse.sendRedirect(redirect);
			}
		}
		catch (Exception e) {
			if (e instanceof AssetTagException) {
				SessionErrors.add(actionRequest, e.getClass().getName(), e);
			}
			else {
				throw e;
			}
		}
	}

	@Override
	public String render(
			PortletConfig portletConfig, RenderRequest renderRequest,
			RenderResponse renderResponse)
		throws Exception {

		return "/html/plugin_asset_publisher/configuration.jsp";
	}

	protected void moveSelectionDown(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int assetEntryOrder = ParamUtil.getInteger(
			actionRequest, "assetEntryOrder");

		String[] manualEntries = preferences.getValues(
			"asset-entry-xml", new String[0]);

		if ((assetEntryOrder >= (manualEntries.length - 1)) ||
			(assetEntryOrder < 0)) {

			return;
		}

		String temp = manualEntries[assetEntryOrder + 1];

		manualEntries[assetEntryOrder + 1] = manualEntries[assetEntryOrder];
		manualEntries[assetEntryOrder] = temp;

		preferences.setValues("asset-entry-xml", manualEntries);
	}

	protected void moveSelectionUp(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int assetEntryOrder = ParamUtil.getInteger(
			actionRequest, "assetEntryOrder");

		String[] manualEntries = preferences.getValues(
			"asset-entry-xml", new String[0]);

		if ((assetEntryOrder >= manualEntries.length) ||
			(assetEntryOrder <= 0)) {

			return;
		}

		String temp = manualEntries[assetEntryOrder - 1];

		manualEntries[assetEntryOrder - 1] = manualEntries[assetEntryOrder];
		manualEntries[assetEntryOrder] = temp;

		preferences.setValues("asset-entry-xml", manualEntries);
	}

	protected void removeSelection(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int assetEntryOrder = ParamUtil.getInteger(
			actionRequest, "assetEntryOrder");

		String[] manualEntries = preferences.getValues(
			"asset-entry-xml", new String[0]);

		if (assetEntryOrder >= manualEntries.length) {
			return;
		}

		String[] newEntries = new String[manualEntries.length -1];

		int i = 0;
		int j = 0;

		for (; i < manualEntries.length; i++) {
			if (i != assetEntryOrder) {
				newEntries[j++] = manualEntries[i];
			}
		}

		preferences.setValues("asset-entry-xml", newEntries);
	}

	protected void setSelectionStyle(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		String selectionStyle = ParamUtil.getString(
			actionRequest, "selectionStyle");
		String displayStyle = ParamUtil.getString(
			actionRequest, "displayStyle");

		preferences.setValue("selection-style", selectionStyle);

		if (selectionStyle.equals("manual") ||
			selectionStyle.equals("view-count")) {

			preferences.setValue("show-query-logic", String.valueOf(false));
		}

		if (!selectionStyle.equals("view-count") &&
			displayStyle.equals("view-count-details")) {

			preferences.setValue("display-style", "full-content");
		}
	}

	protected void updateDynamicSettings(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		updateDisplaySettings(actionRequest, preferences);
		updateQueryLogic(actionRequest, preferences);
		updateRssSettings(actionRequest, preferences);

		boolean mergeUrlTags = ParamUtil.getBoolean(
			actionRequest, "mergeUrlTags");
		boolean defaultScope = ParamUtil.getBoolean(
			actionRequest, "defaultScope");
		String[] scopeIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "scopeIds"));
		long assetVocabularyId = ParamUtil.getLong(
			actionRequest, "assetVocabularyId");
		String orderByColumn1 = ParamUtil.getString(
			actionRequest, "orderByColumn1");
		String orderByColumn2 = ParamUtil.getString(
			actionRequest, "orderByColumn2");
		String orderByType1 = ParamUtil.getString(
			actionRequest, "orderByType1");
		String orderByType2 = ParamUtil.getString(
			actionRequest, "orderByType2");
		boolean excludeZeroViewCount = ParamUtil.getBoolean(
			actionRequest, "excludeZeroViewCount");
		boolean showQueryLogic = ParamUtil.getBoolean(
			actionRequest, "showQueryLogic");
		int delta = ParamUtil.getInteger(actionRequest, "delta");
		String paginationType = ParamUtil.getString(
			actionRequest, "paginationType");
		String[] extensions = actionRequest.getParameterValues("extensions");

		preferences.setValue("selection-style", "dynamic");
		preferences.setValue("merge-url-tags", String.valueOf(mergeUrlTags));
		preferences.setValue("default-scope", String.valueOf(defaultScope));
		preferences.setValues("scope-ids", ArrayUtil.toStringArray(scopeIds));
		preferences.setValue(
			"asset-vocabulary-id", String.valueOf(assetVocabularyId));
		preferences.setValue("order-by-column-1", orderByColumn1);
		preferences.setValue("order-by-column-2", orderByColumn2);
		preferences.setValue("order-by-type-1", orderByType1);
		preferences.setValue("order-by-type-2", orderByType2);
		preferences.setValue(
			"exclude-zero-view-count", String.valueOf(excludeZeroViewCount));
		preferences.setValue(
			"show-query-logic", String.valueOf(showQueryLogic));
		preferences.setValue("delta", String.valueOf(delta));
		preferences.setValue("pagination-type", paginationType);
		preferences.setValues("extensions", extensions);
	}

	protected void updateManualSettings(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		updateDisplaySettings(actionRequest, preferences);
		updateRssSettings(actionRequest, preferences);

		boolean defaultScope = ParamUtil.getBoolean(
			actionRequest, "defaultScope");
		String[] scopeIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "scopeIds"));

		preferences.setValue("default-scope", String.valueOf(defaultScope));
		preferences.setValues("scope-ids", ArrayUtil.toStringArray(scopeIds));
	}

	protected void updateDisplaySettings(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		String displayStyle = ParamUtil.getString(
			actionRequest, "displayStyle");
		boolean anyAssetType = ParamUtil.getBoolean(
			actionRequest, "anyAssetType");
		long[] classNameIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "classNameIds"), 0L);
		String[] scopeIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "scopeIds"));
		boolean showAssetTitle = ParamUtil.getBoolean(
			actionRequest, "showAssetTitle");
		boolean showContextLink = ParamUtil.getBoolean(
			actionRequest, "showContextLink");
		int abstractLength = ParamUtil.getInteger(
			actionRequest, "abstractLength");
		String assetLinkBehavior = ParamUtil.getString(
			actionRequest, "assetLinkBehavior");
		boolean showAvailableLocales = ParamUtil.getBoolean(
			actionRequest, "showAvailableLocales");
		String[] extensions = actionRequest.getParameterValues("extensions");
		boolean enablePrint = ParamUtil.getBoolean(
			actionRequest, "enablePrint");
		boolean enableFlags = ParamUtil.getBoolean(
			actionRequest, "enableFlags");
		boolean enableRatings = ParamUtil.getBoolean(
			actionRequest, "enableRatings");
		boolean enableComments = ParamUtil.getBoolean(
			actionRequest, "enableComments");
		boolean enableCommentRatings = ParamUtil.getBoolean(
			actionRequest, "enableCommentRatings");
		boolean enableTagBasedNavigation = ParamUtil.getBoolean(
			actionRequest, "enableTagBasedNavigation");
		String medatadaFields = ParamUtil.getString(
			actionRequest, "metadataFields");

		preferences.setValue("selection-style", "manual");
		preferences.setValue("display-style", displayStyle);
		preferences.setValue("any-asset-type", String.valueOf(anyAssetType));
		preferences.setValues(
			"class-name-ids", ArrayUtil.toStringArray(classNameIds));
		preferences.setValues("scope-ids", ArrayUtil.toStringArray(scopeIds));
		preferences.setValue(
			"show-asset-title", String.valueOf(showAssetTitle));
		preferences.setValue(
			"show-context-link", String.valueOf(showContextLink));
		preferences.setValue("abstract-length", String.valueOf(abstractLength));
		preferences.setValue("asset-link-behavior", assetLinkBehavior);
		preferences.setValue(
			"show-available-locales", String.valueOf(showAvailableLocales));
		preferences.setValues("extensions", extensions);
		preferences.setValue("enable-print", String.valueOf(enablePrint));
		preferences.setValue("enable-flags", String.valueOf(enableFlags));
		preferences.setValue("enable-ratings", String.valueOf(enableRatings));
		preferences.setValue("enable-comments", String.valueOf(enableComments));
		preferences.setValue(
			"enable-comment-ratings", String.valueOf(enableCommentRatings));
		preferences.setValue(
			"enable-tag-based-navigation",
			String.valueOf(enableTagBasedNavigation));
		preferences.setValue("metadata-fields", medatadaFields);
	}

	protected void updateQueryLogic(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getScopeGroupId();

		int[] queryRulesIndexes = StringUtil.split(
			ParamUtil.getString(actionRequest, "queryLogicIndexes"), 0);

		int i = 0;

		for (int queryRulesIndex : queryRulesIndexes) {
			boolean contains = ParamUtil.getBoolean(
				actionRequest, "queryContains" + queryRulesIndex);
			boolean andOperator = ParamUtil.getBoolean(
				actionRequest, "queryAndOperator" + queryRulesIndex);
			String name = ParamUtil.getString(
				actionRequest, "queryName" + queryRulesIndex);

			String[] values = null;

			if (name.equals("assetTags")) {
				values = StringUtil.split(ParamUtil.getString(
					actionRequest, "queryTagNames" + queryRulesIndex));

				AssetTagLocalServiceUtil.checkTags(userId, groupId, values);
			}
			else {
				values = StringUtil.split(ParamUtil.getString(
					actionRequest, "queryCategoryIds" + queryRulesIndex));
			}

			preferences.setValue("queryContains" + i, String.valueOf(contains));
			preferences.setValue(
				"queryAndOperator" + i, String.valueOf(andOperator));
			preferences.setValue("queryName" + i, name);
			preferences.setValues("queryValues" + i, values);

			i++;
		}

		// Clear previous preferences that are now blank

		String[] values = preferences.getValues(
			"queryValues" + i, new String[0]);

		while (values.length > 0) {
			preferences.setValue("queryContains" + i, StringPool.BLANK);
			preferences.setValue("queryAndOperator" + i, StringPool.BLANK);
			preferences.setValue("queryName" + i, StringPool.BLANK);
			preferences.setValues("queryValues" + i, new String[0]);

			i++;

			values = preferences.getValues("queryValues" + i, new String[0]);
		}
	}

	protected void updateRssSettings(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		boolean enableRSS = ParamUtil.getBoolean(
			actionRequest, "enableRSS");
		int rssDelta = ParamUtil.getInteger(actionRequest, "rssDelta");
		String rssDisplayStyle = ParamUtil.getString(
			actionRequest, "rssDisplayStyle");
		String rssFormat = ParamUtil.getString(actionRequest, "rssFormat");
		String rssName = ParamUtil.getString(actionRequest, "rssName");

		preferences.setValue("enable-rss", String.valueOf(enableRSS));
		preferences.setValue("rss-delta", String.valueOf(rssDelta));
		preferences.setValue("rss-display-style", rssDisplayStyle);
		preferences.setValue("rss-format", rssFormat);
		preferences.setValue("rss-name", rssName);
	}

}