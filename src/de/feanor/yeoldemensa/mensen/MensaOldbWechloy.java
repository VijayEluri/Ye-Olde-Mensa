/**
 *   Ye Olde Mensa is an android application for displaying the current
 *   mensa plans of University Oldenburg on an android mobile phone.
 *   
 *   Copyright (C) 2009/2010 Daniel Süpke
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.feanor.yeoldemensa.mensen;

import java.io.IOException;
import java.net.URL;

import android.content.Context;
import de.feanor.htmltokenizer.Element;
import de.feanor.htmltokenizer.SimpleHTMLTokenizer;
import de.feanor.yeoldemensa.Mensa;
import de.feanor.yeoldemensa.MenuItem;

/**
 * Contains the parser for the Mensa Oldenburg/Wechloy.
 * 
 * @author Daniel Süpke
 */
public class MensaOldbWechloy extends Mensa {

	public MensaOldbWechloy(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.feanor.yeoldemensa.Mensa#loadMenu(java.util.Map)
	 */
	@Override
	protected void fetchMenu() throws IOException {
		SimpleHTMLTokenizer tokenizer = new SimpleHTMLTokenizer(
				new URL(
						"http://www.studentenwerk-oldenburg.de/speiseplan/wechloy.php"),
				"iso-8859-1");

		// Skip to next week instead?
		Element element;
		if (getNextWeek()) {
			while ((element = tokenizer.nextText()) != null
					&& !element.content.startsWith("Nächste Woche"))
				;
		}

		addColumn("gericht", "Hauptgericht", 140, tokenizer);
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Beilagen
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Gemüse
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Salat
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Suppe
		addColumn("0,45", "Beilagen (0,45)", 45, tokenizer); // Dessert
	}

	private void addColumn(String delimeter, String type, int price,
			SimpleHTMLTokenizer tokenizer) {
		Element element;

		// Skip to beginning based on delimeter
		while ((element = tokenizer.nextText()) != null
				&& !element.content.equals(delimeter))
			;
		while ((element = tokenizer.nextTag()) != null
				&& !element.content.equals("/td"))
			;

		boolean inCell = false;
		boolean finished = false;

		// Start adding items for each week day
		for (int i = 0; i < 5; i++) {
			finished = false;
			inCell = false;

			// Next element needs to be ignored to remove leading td
			element = tokenizer.nextElement();

			while (!finished) {
				element = tokenizer.nextElement();

				if (element.isText() && !startsWithNumber(element.content)) {
					this.addMenuItem(new MenuItem(Day.values()[i], type,
							element.content, price));
				} else {
					// If a td is encountered, we are in an inner Cell. Do not
					// stop at next /td
					if (element.content.startsWith("td"))
						inCell = true;
					else if (element.content.equals("/td") && inCell)
						inCell = false;
					else if (element.content.equals("/td") && !inCell)
						finished = true;
				}
			}
		}
	}

	/**
	 * Return true if the number starts with a string. Could probably be done
	 * more elegant, e.g. using a RegExp.
	 * 
	 * @param string
	 *            String to examine
	 * @return True, if string starts with a number.
	 */
	private boolean startsWithNumber(String string) {
		for (int i = 0; i < 10; i++) {
			if (string.startsWith(Integer.toString(i))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getName() {
		return "Oldenburg - Wechloy";
	}

	@Override
	public double[] getCoordinates() {
		return new double[] { 53.152147, 8.165046 };
	}

	@Override
	public int getID() {
		return 1;
	}
}
