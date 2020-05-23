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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.woefe.shoppinglist.shoppinglist.ShoppingList.DEFAULT_CATEGORY;

public class ShoppingListUnmarshaller {
    private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*$");
    private static final Pattern HEADER = Pattern.compile("\\[(.*)]");
    private static final Pattern CATEGORY = Pattern.compile("^Categories:.*$");

    public static ShoppingList unmarshal(String filename) throws IOException, UnmarshallException {
        return unmarshal(new FileInputStream(filename));
    }

    public static ShoppingList unmarshal(InputStream inputStream) throws IOException, UnmarshallException {
        ShoppingList shoppingList;
        String name = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String firstLine = reader.readLine();

        if (firstLine != null) {
            Matcher matcher = HEADER.matcher(firstLine);
            if (matcher.matches()) {
                name = matcher.group(1).trim();
            }
        }

        if (name == null) {
            throw new UnmarshallException("Could not find the name of the list");
        }

        shoppingList = new ShoppingList(name);

        String line;
        while ((line = reader.readLine()) != null) {
            if (!EMPTY_LINE.matcher(line).matches() && !CATEGORY.matcher(line).matches()) {
                ListItem item = createListItem(line, shoppingList);
                shoppingList.getCategories().get(item.getCategory()).add(item);
            }
        }

        if (shoppingList.getCategories().isEmpty()) {
            shoppingList.addDefaultCategory();
        }

        return shoppingList;
    }

    private static ListItem createListItem(String item, ShoppingList shoppingList) {
        boolean isChecked = item.startsWith("//");
        int index;
        String quantity;
        String name;
        String category;

        if (isChecked) {
            item = item.substring(2);
        }

        index = item.lastIndexOf("#");
        int indexCategory = item.lastIndexOf("$");

        if (index != -1) {
            if (indexCategory != -1) {
                quantity = item.substring(index + 1, indexCategory).trim();
            } else {
                quantity = item.substring(index + 1).trim();
            }
            name = item.substring(0, index).trim();
        } else {
            quantity = "";

            if (indexCategory != -1) {
                name = item.substring(0, indexCategory);
            } else {
                name = item.trim();
            }
        }

        if (indexCategory != -1) {
            category = item.substring(indexCategory + 1).trim();
        } else {
            category = DEFAULT_CATEGORY;
        }

        if (!shoppingList.getCategories().containsKey(category)) {
            shoppingList.getCategories().put(category, new ArrayList<ListItem>());
        }

        return new ListItem(isChecked, name.trim(), quantity, category);
    }
}
