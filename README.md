# DiscoCleaner

DiscoCleaner is a Java command tool allowing the user to clean his music folder.


## Requirements

+ [Java 8](http://www.java.com/en/download/)
+ [ImageMagick](https://www.imagemagick.org/) only for album cover files manipulation


## Installation

+ Download the project or only the DiscoCleaner-x.x.jar file under the *release* directory
+ Run DiscoCleaner-x.x.jar 


## Limitations / Warnings

+ The music folder must have this structure: `<music.directory.path>/<artist>/<album>/<songs>`
+ Most of the functions are limited to MP3 files
+ Make sure to disable `force.enabled` property before running the program on your music folder

## Usage

+ Detect files in artist and album directories
+ Detect directories in song directories
+ Detect empty directories (artist and album)
+ Detect invalid MP3 filenames according to predefined pattern
+ Check and remove MP3 tag according to predefined frames
+ Rename or convert album cover files according to predefined cover file names
+ Check if album cover file exists according to predefined cover file name
+ Convert album cover file from progressive jpeg to baseline jpeg if necessary
+ Load album cover file to MP3 tag if necessary
+ Delete other files than MP3 and album cover file
+ Rename album directory according to predefined mask if necessary (based on MP3 tags)


## Configuration

+ After executing the program for the first time, a properties file is generated:

```
# Music directory path
# default: {user.home}/Music/
music.directory.path=

# File manager program to use for browsing through music directory
# default: null
file.manager=

# Text editor program to use for showing the logs at the end of the process
# default: null
text.editor=

# Valid pattern for MP3 filenames (regex with escape characters)
# default: null
mp3.pattern=

# Valid pattern for album directory names (regex with escape characters)
# default: null
album.pattern=

# Mask to use when renaming an album directory
# %a = artist
# %b = album
# %y = year
# example: "[%y] %b" --> [1973] The Dark Side of the Moon
# default: null
album.mask=

# ID3v2 frames allowed and mandatory in MP3 tag (comma-separated string)
# example: "APIC,TALB,TIT2,TPE1,TRCK,TYER" --> The following frames, and only those, must be in the tag:
# - Attached picture
# - Album title
# - Song title
# - Artist
# - Track number
# - Year
# default: null
tag.frames=

# true if the MP3 file custom tag is allowed, false otherwise
# default: true
custom.tag.allowed=

# Cover file name
# default: null
cover.file.name=

# Obsolete cover file names (comma-separated string) that can be renamed to {cover.file.name}
# default: null
obsolete.cover.file.name=

# Only for jpeg cover.
# true if the cover should be processed:
# - convert progressive to baseline
# - load image to MP3 tag if different from cover file
# default: false
process.cover.enabled=

# true if no confirmation is asked to the user, false otherwise
# default: false
force.enabled=

# true if logs has to be written in a file at the end of the process, false otherwise
# default: false
log.enabled=

# true if the logs has to be shown in an editor at the end of the process, false otherwise
# default: false
show.log.enabled=
```


## Options

+ 4 options can be passed as argument:

```
--version                               Show program's version
--help                                  Show help message
--artist="<artist directory name>"      Proceed with the given artist only
--album="<album directory name>"        Proceed with the given album only
```


## Dependencies

+ [mp3agic](https://github.com/mpatric/mp3agic)
+ [Gradle Shadow](https://github.com/johnrengelman/shadow)
+ [Commons IO](https://commons.apache.org/proper/commons-io/)


## License

Project licensed under the [MIT license](http://opensource.org/licenses/mit-license.php).
