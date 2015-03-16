SearchBooster
=============

SearchBooster is an advanced desktop document search tool. It provides a
configurable search index and an advanced search form.



1. Respository organization
---------------------------

The respository is organized as an Eclipse-Project.

ROOT
+ src	  source files
+ test	  some JUnit testing routines
+ lib  contains all required libraries to run SearchBooster



2. How to use
-------------

Starting SearchBooster.jar will open the SearchBooster GUI. On the top left of
the window you will find the search form editor, where you can refine the search
parameters with standard logic operators like AND, OR. Additionally it is
possible to store a build search request.

Below the search form editor you can specify where on your desktop you want to
search for files.

SearchBooster distinguishes between a temporary and a persistent index. The
persistent index can be modified intuitively via the "Properties"-button in the
top right corner of the SearchBooster GUI.

When you search without specifying any search paths you will automatically 
search in the whole persistent index. Otherwise the paths specified will be
searched in for results. If a path to be searched in is not contained in the
persistent index, it will become part of the temporary one. All temporary
indexed files will be lost at the end of the program.



Credits
-------

This program is part of the course "Realization of an I&K Application System"
at the Hamburg Universitiy of Technology.

The course was supervised by
	- Prof. Dr. Sibylle Schupp
	- Dipl.-Inform. Rainer Marrone

Participants of the MRound-Developer-Team:
	- B.Sc. Jan Eric Lange
	- B.Sc. Kai Torben Ohlhus
	- B.Sc. Michael Kunert
	- B.Sc. Tobias Schulz

```
 __  __ _____                       _   _  _   _____             
|  \/  |  __ \                     | |_|#||#|_|  __ \            
| \  / | |__) |___  _   _ _ __   __| |########| |  | | _____   __
| |\/| |  _  // _ \| | | | '_ \ / _` |_|#||#|_| |  | |/ _ \ \ / /
| |  | | | \ \ (_) | |_| | | | | (_| |########| |__| |  __/\ V / 
|_|  |_|_|  \_\___/ \__,_|_| |_|\__,_| |#||#| |_____/ \___| \_/ 
```
