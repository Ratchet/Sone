/*
 * Sone - CreatePostPage.java - Copyright © 2010 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.Template;

/**
 * This page lets the user create a new {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreatePostPage extends SoneTemplatePage {

	/**
	 * Creates a new “create post” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public CreatePostPage(Template template, WebInterface webInterface) {
		super("createPost.html", template, "Page.CreatePost.Title", webInterface, true);
	}

	//
	// TEMPLATEPATH METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, DataProvider dataProvider) throws RedirectException {
		super.processTemplate(request, dataProvider);
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		if (request.getMethod() == Method.POST) {
			String text = request.getHttpRequest().getPartAsStringFailsafe("text", 65536).trim();
			if (text.length() != 0) {
				String recipientId = request.getHttpRequest().getPartAsStringFailsafe("recipient", 43);
				Sone recipient = webInterface.getCore().getSone(recipientId, false);
				Sone currentSone = getCurrentSone(request.getToadletContext());
				webInterface.getCore().createPost(currentSone, recipient, System.currentTimeMillis(), text);
				throw new RedirectException(returnPage);
			}
			dataProvider.set("errorTextEmpty", true);
		}
		dataProvider.set("returnPage", returnPage);
	}

}
