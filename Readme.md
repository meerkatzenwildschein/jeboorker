## Download
- [Windows](http://morologia.de/jeboorker/download.php?f=jeboorker.exe)
- [Linux](http://morologia.de/jeboorker/download.php?f=jeboorker.tar.gz)
- [Debian / Ubuntu](http://morologia.de/jeboorker/download.php?f=jeboorker.deb)

## Getting started
If you start Jeboorker the first time, you have to specify the folder where youre ebooks located at. This can be done with the menu File->Read folder. You can add as many folders as you want.

After the read process has been done, all books from all folders you've specified appears in the main area.

![Screenshot](https://raw.githubusercontent.com/meerkatzenwildschein/jeboorker/master/doc/screenshots/screenshot_main.jpg)

## Folder handling
Always you copy some new ebooks with your favourite file manager to your collection, just perfrom a File->Refresh folder for a rescan, so jeboorker can recognize these.

The folders you've added can be toggled in it's visibility state by File -> Show/hide folder.

## Adding new books using Drag and Drop
You can copy Books simply by Dragging it in the Jeboorker list. The book is copied to the same folder as the drop target book is located. You can also drag a book from Jeboorker to your favourite file manager.

## Search books using the filters
Jeboorker provides a filter field at the bottom which allows to filter for keywords. You can specify which kind of data should be searched by checking the desired kinds at the filter combobox at the left of the filter text field. If no filter is selected the filter is performed to title, author and file name.

Another way to filter is to use the filter tree at the left. Each base path registered with Jeboorker can be found in that tree. You can click at the eyeballs to toggle the visibility of each base path. Always you select a base path or a folder beneath a filter is applied to the selection. Clicking at the last entry in the tree will show up all visible entries again. You can also change the base path visibility of all base path by clicking at the last entry eyeball.

## Starting multiple instances of Jeboorker
To startup with another instance just add the following parameter to the startup script.
-Dapplication.suffix=devel