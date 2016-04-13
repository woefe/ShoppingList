# <img src="https://github.com/popeye123/ShoppingList/blob/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.png" width="50" height="50" /> ShoppingList
A simple shopping list for Android

## ShoppingList text file
ShoppingList saves your shopping list as a simple text file. You can use a file syncing solution like ownCloud, Syncthing
or Dropbox to sync your shopping list across multiple devices (to then edit the list on your desktop computer in Vim 😉).
The syntax of this text file is very easy to understand.

### Syntax
 * The very first line of the file is the name of the list in square brackets
 * Empty lines or lines with only whitespaces are ignored
 * Every item in the list is a single line in the file
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

## Miscellaneous
This project uses git submodules. Make sure the submodules are initialized before compiling the project or use
```shell
git clone --recursive https://github.com/popeye123/ShoppingList.git
```
when cloning the repo.
