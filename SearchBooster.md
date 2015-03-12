# Introduction #

SearchBooster is an advanced desktop document search tool. It provides a configurable search index and an advanced search form.

# How to use #

Start after downloading [SearchBooster.jar](https://searchbooster.googlecode.com/files/SearchBooster.jar) to open the SearchBooster GUI. On the top left of the window you will find the search form editor, where you can refine the search parameters with standard logic operators like AND, OR. Additionally it is possible to store a build search request.

Below the search form editor you can specify where on your desktop you want to search for files.

SearchBooster distinguishes between a temporary and a persistent index. The persistent index can be modified intuitively via the "Properties"-button in the top right corner of the SearchBooster GUI.

When you search without specifying any search paths you will automatically search in the whole persistent index. Otherwise the paths specified will be searched in for results. If a path to be searched in is not contained in the persistent index, it will become part of the temporary one. All temporary indexed files will be lost at the end of the program.

# Obtaining the source #

https://code.google.com/p/searchbooster/source/checkout?repo=eclipse