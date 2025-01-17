/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.layout.content.page.editor.web.internal.portlet.action;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.processor.PortletRegistry;
import com.liferay.fragment.service.FragmentEntryLinkService;
import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.layout.content.page.editor.web.internal.util.layout.structure.LayoutStructure;
import com.liferay.layout.content.page.editor.web.internal.util.layout.structure.LayoutStructureItem;
import com.liferay.layout.content.page.editor.web.internal.util.layout.structure.LayoutStructureUtil;
import com.liferay.layout.service.LayoutClassedModelUsageLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.segments.constants.SegmentsExperienceConstants;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET,
		"mvc.command.name=/content_layout/delete_item_react"
	},
	service = {AopService.class, MVCActionCommand.class}
)
public class DeleteItemReactMVCActionCommand
	extends BaseMVCActionCommand implements AopService, MVCActionCommand {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws PortletException {

		return super.processAction(actionRequest, actionResponse);
	}

	protected JSONObject deleteItemJSONObject(
			long companyId, long groupId, String itemId, long plid,
			long segmentsExperienceId)
		throws PortalException {

		return LayoutStructureUtil.updateLayoutPageTemplateData(
			groupId, segmentsExperienceId, plid,
			layoutStructure -> _deleteLayoutStructureItem(
				companyId, itemId, layoutStructure, plid));
	}

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String itemId = ParamUtil.getString(actionRequest, "itemId");
		long segmentsExperienceId = ParamUtil.getLong(
			actionRequest, "segmentsExperienceId",
			SegmentsExperienceConstants.ID_DEFAULT);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {
			jsonObject = deleteItemJSONObject(
				themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId(),
				itemId, themeDisplay.getPlid(), segmentsExperienceId);
		}
		catch (Exception e) {
			_log.error(e, e);

			jsonObject.put(
				"error",
				LanguageUtil.get(
					themeDisplay.getRequest(), "an-unexpected-error-occurred"));
		}

		hideDefaultSuccessMessage(actionRequest);

		JSONPortletResponseUtil.writeJSON(
			actionRequest, actionResponse, jsonObject);
	}

	private void _deleteFragmentEntryLink(
			long companyId, long fragmentEntryLinkId, long plid)
		throws PortalException {

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkService.deleteFragmentEntryLink(
				fragmentEntryLinkId);

		if (fragmentEntryLink.getFragmentEntryId() == 0) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				fragmentEntryLink.getEditableValues());

			String portletId = jsonObject.getString(
				"portletId", StringPool.BLANK);

			if (Validator.isNotNull(portletId)) {
				String instanceId = jsonObject.getString(
					"instanceId", StringPool.BLANK);

				_portletLocalService.deletePortlet(
					companyId, PortletIdCodec.encode(portletId, instanceId),
					plid);

				_layoutClassedModelUsageLocalService.
					deleteLayoutClassedModelUsages(
						PortletIdCodec.encode(portletId, instanceId),
						_portal.getClassNameId(Portlet.class), plid);
			}
		}

		List<String> portletIds =
			_portletRegistry.getFragmentEntryLinkPortletIds(fragmentEntryLink);

		for (String portletId : portletIds) {
			_portletLocalService.deletePortlet(companyId, portletId, plid);

			_layoutClassedModelUsageLocalService.deleteLayoutClassedModelUsages(
				portletId, _portal.getClassNameId(Portlet.class), plid);
		}

		_layoutClassedModelUsageLocalService.deleteLayoutClassedModelUsages(
			String.valueOf(fragmentEntryLinkId),
			_portal.getClassNameId(FragmentEntryLink.class), plid);
	}

	private void _deleteLayoutStructureItem(
			long companyId, String itemId, LayoutStructure layoutStructure,
			long plid)
		throws PortalException {

		LayoutStructureItem layoutStructureItem =
			layoutStructure.getLayoutStructureItem(itemId);

		JSONObject itemConfigJSONObject =
			layoutStructureItem.getItemConfigJSONObject();

		long fragmentEntryLinkId = itemConfigJSONObject.getLong(
			"fragmentEntryLinkId");

		if (fragmentEntryLinkId > 0) {
			_deleteFragmentEntryLink(companyId, fragmentEntryLinkId, plid);
		}

		List<String> childrenItemIds = new ArrayList<>(
			layoutStructureItem.getChildrenItemIds());

		for (String childrenItemId : childrenItemIds) {
			_deleteLayoutStructureItem(
				companyId, childrenItemId, layoutStructure, plid);
		}

		layoutStructure.deleteLayoutStructureItem(itemId);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DeleteItemReactMVCActionCommand.class);

	@Reference
	private FragmentEntryLinkService _fragmentEntryLinkService;

	@Reference
	private LayoutClassedModelUsageLocalService
		_layoutClassedModelUsageLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private PortletLocalService _portletLocalService;

	@Reference
	private PortletRegistry _portletRegistry;

}