/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2018.  Wolfgang Popp
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

package com.woefe.shoppinglist.shoppinglist;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

public class ShoppingListMarshaller {
    public static void marshall(OutputStream stream, ShoppingList list) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            List<String> lines = new LinkedList<>();
            lines.add("[ " + list.getName() + " ]\n");
            lines.add("\n");

            for (ListItem item : list) {
                lines.add((item.isChecked() ? "// " : "") + item.getDescription() + " #" + item.getQuantity() + "\n");
            }

            for (String line : lines) {
                writer.write(line);
            }
        }
    }
}
