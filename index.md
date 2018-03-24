# <img src="https://github.com/woefe/ShoppingList/raw/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.png" width="50" height="50" /> ShoppingList
A simple shopping list for Android

<a href="https://f-droid.org/packages/com.woefe.shoppinglist/" target="_blank">
    <img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/>
</a>

## ShoppingList text file
ShoppingList saves your shopping lists as a plain text files. You can use a file syncing solution like
ownCloud/Nextcloud, Syncthing or Dropbox to share your shopping lists across multiple devices (to
then edit the lists on your desktop computer in your favorite Editor 😉). The syntax of a
ShoppingList file is quite simple and easy to read and edit.

### Syntax
 * The very first line of the file is the name of the list in square brackets
 * Empty lines or lines with only whitespaces are ignored
 * Every item of the list corresponds to a single line in the file
 * Checked items start with `//`
 * Specifying the amount of an item is optional
 * The amount of an item and its name are separated by the #-Sign

### Example
```
[ ShoppingList ]

Milk
Bananas #
Juice #2 Liters
// Eggs #12
```
