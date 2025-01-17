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

package com.liferay.journal.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class JournalSelectDDMTemplateManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public JournalSelectDDMTemplateManagementToolbarDisplayContext(
			HttpServletRequest httpServletRequest,
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse,
			JournalSelectDDMTemplateDisplayContext
				journalSelectDDMTemplateDisplayContext)
		throws Exception {

		super(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			journalSelectDDMTemplateDisplayContext.getTemplateSearch());

		_journalSelectDDMTemplateDisplayContext =
			journalSelectDDMTemplateDisplayContext;
	}

	/**
	 * @deprecated As of Athanasius (7.3.x), replaced by {@link
	 *             #JournalSelectDDMTemplateManagementToolbarDisplayContext(
	 *             HttpServletRequest, LiferayPortletRequest,
	 *             LiferayPortletResponse,
	 *             JournalSelectDDMTemplateDisplayContext)}
	 */
	@Deprecated
	public JournalSelectDDMTemplateManagementToolbarDisplayContext(
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse,
			HttpServletRequest httpServletRequest,
			JournalSelectDDMTemplateDisplayContext
				journalSelectDDMTemplateDisplayContext)
		throws Exception {

		this(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			journalSelectDDMTemplateDisplayContext);
	}

	@Override
	public String getClearResultsURL() {
		PortletURL clearResultsURL = getPortletURL();

		clearResultsURL.setParameter("keywords", StringPool.BLANK);

		return clearResultsURL.toString();
	}

	@Override
	public String getSearchActionURL() {
		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		portletURL.setParameter("mvcPath", "/select_ddm_template.jsp");
		portletURL.setParameter(
			"ddmTemplateId",
			String.valueOf(
				_journalSelectDDMTemplateDisplayContext.getDDMTemplateId()));
		portletURL.setParameter(
			"ddmStructureId",
			String.valueOf(
				_journalSelectDDMTemplateDisplayContext.getDDMStructureId()));
		portletURL.setParameter(
			"eventName",
			_journalSelectDDMTemplateDisplayContext.getEventName());

		return portletURL.toString();
	}

	@Override
	public Boolean isSelectable() {
		return false;
	}

	@Override
	protected String getDefaultDisplayStyle() {
		return "icon";
	}

	@Override
	protected String[] getDisplayViews() {
		return new String[] {"list", "icon"};
	}

	@Override
	protected String[] getNavigationKeys() {
		return new String[] {"all"};
	}

	@Override
	protected String[] getOrderByKeys() {
		return new String[] {"modified-date"};
	}

	private final JournalSelectDDMTemplateDisplayContext
		_journalSelectDDMTemplateDisplayContext;

}