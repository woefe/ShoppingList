package de.wolfgang_popp.shoppinglist.shoppinglist;

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
