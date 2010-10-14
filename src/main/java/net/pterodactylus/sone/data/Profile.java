/*
 * FreenetSone - Profile.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.data;

/**
 * A profile stores personal information about a {@link User}. All information
 * is optional and can be {@code null}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Profile {

	/** Whether the profile was modified. */
	private boolean modified;

	/** The first name. */
	private String firstName;

	/** The middle name(s). */
	private String middleName;

	/** The last name. */
	private String lastName;

	/**
	 * Creates a new empty profile.
	 */
	public Profile() {
		/* do nothing. */
	}

	/**
	 * Creates a copy of a profile.
	 *
	 * @param profile
	 *            The profile to copy
	 */
	public Profile(Profile profile) {
		this.firstName = profile.firstName;
		this.middleName = profile.middleName;
		this.lastName = profile.lastName;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether this profile was modified after creation. To clear the
	 * “is modified” flag you need to create a new profile from this one using
	 * the {@link #Profile(Profile)} constructor.
	 *
	 * @return {@code true} if this profile was modified after creation,
	 *         {@code false} otherwise
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Returns the first name.
	 *
	 * @return The first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name.
	 *
	 * @param firstName
	 *            The first name to set
	 */
	public void setFirstName(String firstName) {
		modified |= ((firstName != null) && (!firstName.equals(this.firstName))) || (this.firstName != null);
		this.firstName = firstName;
	}

	/**
	 * Returns the middle name(s).
	 *
	 * @return The middle name
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * Sets the middle name.
	 *
	 * @param middleName
	 *            The middle name to set
	 */
	public void setMiddleName(String middleName) {
		modified |= ((middleName != null) && (!middleName.equals(this.middleName))) || (this.middleName != null);
		this.middleName = middleName;
	}

	/**
	 * Returns the last name.
	 *
	 * @return The last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name.
	 *
	 * @param lastName
	 *            The last name to set
	 */
	public void setLastName(String lastName) {
		modified |= ((lastName != null) && (!lastName.equals(this.lastName))) || (this.lastName != null);
		this.lastName = lastName;
	}

}
