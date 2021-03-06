/*
 * Sone - PartContainer.java - Copyright © 2010 David Roden
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
import java.util.ArrayList;
import java.util.List;

/**
 * Part implementation that can contain an arbitrary amount of other parts.
 * Parts are added using the {@link #add(Part)} method and will be rendered in
 * the order they are added.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PartContainer implements Part {

	/** The parts to render. */
	private final List<Part> parts = new ArrayList<Part>();

	//
	// ACCESSORS
	//

	/**
	 * Adds a part to render.
	 *
	 * @param part
	 *            The part to add
	 */
	public void add(Part part) {
		parts.add(part);
	}

	//
	// PART METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Writer writer) throws IOException {
		for (Part part : parts) {
			part.render(writer);
		}
	}

}
