/*
 * Sone - GetLikesAjaxPage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web.ajax;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX page that retrieves the number of “likes” a {@link Post} has.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetLikesAjaxPage extends JsonPage {

	/**
	 * Creates a new “get post likes” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetLikesAjaxPage(WebInterface webInterface) {
		super("ajax/getLikes.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		String type = request.getHttpRequest().getParam("type", null);
		String id = request.getHttpRequest().getParam(type, null);
		if ((id == null) || (id.length() == 0)) {
			return new JsonObject().put("success", false).put("error", "invalid-" + type + "-id");
		}
		if ("post".equals(type)) {
			Post post = webInterface.core().getPost(id);
			return new JsonObject().put("success", true).put("likes", webInterface.core().getLikes(post).size());
		} else if ("reply".equals(type)) {
			Reply reply = webInterface.core().getReply(id);
			return new JsonObject().put("success", true).put("likes", webInterface.core().getLikes(reply).size());
		}
		return new JsonObject().put("success", false).put("error", "invalid-type");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

}