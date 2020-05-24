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

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ShoppingListMarshaller {
    public static void marshall(@NonNull OutputStream stream, @NonNull ShoppingList list) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            writer.write("[ ");
            writer.write(list.getName());
            writer.write(" ]\n\n");

            writer.write("Categories:");
            int categorySize = list.getAllCategories().size();
            for (int i = 0; i < categorySize; i++) {
                writer.write(list.getAllCategories().get(i));
                if (i < categorySize - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n\n");

            for (String category : list.getAllCategories()) {
                List<ListItem> itemList = list.getCategories().get(category);

                if (itemList != null) {
                    for (ListItem item : itemList) {
                        writeItem(item, writer);
                    }
                    writer.write("\n");
                }
            }
        }
    }

    private static void writeItem(ListItem item, BufferedWriter writer) throws IOException {
        String quantity = item.getQuantity();
        String description = item.getDescription();
        String category = item.getCategory();

        if (item.isChecked()) {
            writer.write("// ");
        }

        if (description != null) {
            writer.write(description);
        }

        if (quantity != null && !quantity.equals("")) {
            writer.write(" #");
            writer.write(quantity);
        }

        if (!TextUtils.isEmpty(category)) {
            writer.write(" $");
            writer.write(category);
        }

        writer.write("\n");
    }
}
