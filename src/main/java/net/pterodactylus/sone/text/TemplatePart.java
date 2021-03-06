/*
 * Sone - TemplatePart.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.text;

import java.io.IOException;
import java.io.Writer;

import net.pterodactylus.util.template.Template;

/**
 * {@link Part} implementation that is rendered using a {@link Template}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TemplatePart implements Part {

	/** The template to render for this part. */
	private final Template template;

	/**
	 * Creates a new template part.
	 *
	 * @param template
	 *            The template to render
	 */
	public TemplatePart(Template template) {
		this.template = template;
	}

	//
	// ACTIONS
	//

	/**
	 * Sets a variable in the template.
	 *
	 * @param key
	 *            The key of the variable
	 * @param value
	 *            The value of the variable
	 * @return This template part (for method chaining)
	 */
	public TemplatePart set(String key, Object value) {
		template.set(key, value);
		return this;
	}

	//
	// PART METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Writer writer) throws IOException {
		template.render(writer);
	}

}
