package com.liferay.plugin.portlet.assetpublisher.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.NoSuchCategoryException;
import com.liferay.portlet.asset.NoSuchTagException;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetCategoryProperty;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.model.AssetTagProperty;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetCategoryPropertyLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagPropertyLocalServiceUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletRequest;

public class PluginAssetUtil {

	public static char[] INVALID_CHARACTERS = new char[] {
		CharPool.AMPERSAND, CharPool.APOSTROPHE, CharPool.AT,
		CharPool.BACK_SLASH, CharPool.CLOSE_BRACKET, CharPool.CLOSE_CURLY_BRACE,
		CharPool.COLON, CharPool.COMMA, CharPool.EQUAL, CharPool.GREATER_THAN,
		CharPool.FORWARD_SLASH, CharPool.LESS_THAN, CharPool.NEW_LINE,
		CharPool.OPEN_BRACKET, CharPool.OPEN_CURLY_BRACE, CharPool.PERCENT,
		CharPool.PIPE, CharPool.PLUS, CharPool.POUND, CharPool.QUESTION,
		CharPool.QUOTE, CharPool.RETURN, CharPool.SEMICOLON, CharPool.SLASH,
		CharPool.STAR, CharPool.TILDE
	};

	public static Set<String> addLayoutTags(
		HttpServletRequest request, List<AssetTag> tags) {

		Set<String> layoutTags = getLayoutTagNames(request);

		for (AssetTag tag : tags) {
			layoutTags.add(tag.getName());
		}

		return layoutTags;
	}

	public static void addPortletBreadcrumbEntries(
			long assetCategoryId, HttpServletRequest request,
			PortletURL portletURL)
		throws Exception {

		AssetCategory assetCategory = AssetCategoryLocalServiceUtil.getCategory(
			assetCategoryId);

		List<AssetCategory> ancestorCategories = assetCategory.getAncestors();

		Collections.reverse(ancestorCategories);

		for (AssetCategory ancestorCategory : ancestorCategories) {
			portletURL.setParameter("categoryId", String.valueOf(
				ancestorCategory.getCategoryId()));

			addPortletBreadcrumbEntry(
				request, ancestorCategory.getName(), portletURL.toString());
		}

		portletURL.setParameter("categoryId", String.valueOf(assetCategoryId));

		addPortletBreadcrumbEntry(
			request, assetCategory.getName(), portletURL.toString());
	}

	public static void addPortletBreadcrumbEntry(
			HttpServletRequest request, String title, String url)
		throws Exception {

		List<KeyValuePair> portletBreadcrumbs =
			(List<KeyValuePair>)request.getAttribute(
				"PORTLET_BREADCRUMBS");

		if (portletBreadcrumbs != null) {
			for (KeyValuePair portletBreadcrumb : portletBreadcrumbs) {
				if (title.equals(portletBreadcrumb.getKey())) {
					return;
				}
			}
		}

		PortalUtil.addPortletBreadcrumbEntry(request, title, url);
	}

	public static String getAssetKeywords(String className, long classPK)
		throws SystemException {

		List<AssetTag> tags = AssetTagLocalServiceUtil.getTags(
			className, classPK);
		List<AssetCategory> categories =
			AssetCategoryLocalServiceUtil.getCategories(className, classPK);

		StringBuffer sb = new StringBuffer();

		sb.append(ListUtil.toString(tags, "name"));

		if (!tags.isEmpty()) {
			sb.append(StringPool.COMMA);
		}

		sb.append(ListUtil.toString(categories, "name"));

		return sb.toString();
	}

	public static Set<String> getLayoutTagNames(HttpServletRequest request) {
		Set<String> tagNames = (Set<String>)request.getAttribute(
			"ASSET_LAYOUT_TAG_NAMES");

		if (tagNames == null) {
			tagNames = new HashSet<String>();

			request.setAttribute("ASSET_LAYOUT_TAG_NAMES", tagNames);
		}

		return tagNames;
	}

	public static boolean isValidWord(String word) {
		if (Validator.isNull(word)) {
			return false;
		}
		else {
			char[] wordCharArray = word.toCharArray();

			for (char c : wordCharArray) {
				for (char invalidChar : INVALID_CHARACTERS) {
					if (c == invalidChar) {
						if (_log.isDebugEnabled()) {
							_log.debug(
								"Word " + word + " is not valid because " + c +
									" is not allowed");
						}

						return false;
					}
				}
			}
		}

		return true;
	}

	public static String substituteCategoryPropertyVariables(
			long groupId, long categoryId, String s)
		throws PortalException, SystemException {

		String result = s;

		AssetCategory category = null;

		if (categoryId > 0) {
			try {
				category = AssetCategoryLocalServiceUtil.getCategory(
					categoryId);
			}
			catch (NoSuchCategoryException nsce) {
			}
		}

		if (category != null) {
			List<AssetCategoryProperty> categoryProperties =
				AssetCategoryPropertyLocalServiceUtil.getCategoryProperties(
					categoryId);

			for (AssetCategoryProperty categoryProperty : categoryProperties) {
				result = StringUtil.replace(
					result, "[$" + categoryProperty.getKey() + "$]",
					categoryProperty.getValue());
			}
		}

		return StringUtil.stripBetween(result, "[$", "$]");
	}

	public static String substituteTagPropertyVariables(
			long groupId, String tagName, String s)
		throws PortalException, SystemException {

		String result = s;

		AssetTag tag = null;

		if (tagName != null) {
			try {
				tag = AssetTagLocalServiceUtil.getTag(groupId, tagName);
			}
			catch (NoSuchTagException nste) {
			}
		}

		if (tag != null) {
			List<AssetTagProperty> tagProperties =
				AssetTagPropertyLocalServiceUtil.getTagProperties(
					tag.getTagId());

			for (AssetTagProperty tagProperty : tagProperties) {
				result = StringUtil.replace(
					result, "[$" + tagProperty.getKey() + "$]",
					tagProperty.getValue());
			}
		}

		return StringUtil.stripBetween(result, "[$", "$]");
	}

	public static String toWord(String text) {
		if (Validator.isNull(text)) {
			return text;
		}
		else {
			char[] textCharArray = text.toCharArray();

			for (int i = 0; i < textCharArray.length; i++) {
				char c = textCharArray[i];

				for (char invalidChar : INVALID_CHARACTERS) {
					if (c == invalidChar) {
						textCharArray[i] = CharPool.SPACE;

						break;
					}
				}
			}

			return new String(textCharArray);
		}
	}

	private static Log _log = LogFactoryUtil.getLog(PluginAssetUtil.class);


	
}
