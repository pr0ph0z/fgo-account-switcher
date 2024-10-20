# FGO Account Switcher
<p align="center"><img src="app/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp" /></p>

### Description
FGO Account Switcher is an app that lets you switch between accounts easily. What it does in the background is just saving the needed FGO internal files (you can read the details more [here](https://www.reddit.com/r/grandorder/comments/hfbz0h/fgo_save_files_for_na_and_jp_play_on_multiple)), save it to **internal app directory**, and whenever you want to switch the active account, it will copy and replace the FGO internal files from the saved internal app directory.

Currently only supports NA.

Bugs may occur since I only developed and tested the app on one device. Let me know if you find one in [Issues](https://github.com/pr0ph0z/fgo-account-switcher/issues).

### Usage
First and foremost you need a rooted device because Android doesn't let you modify other app internal files so a root access is needed. Then you can just download the file from the latest [release page](https://github.com/Fate-Grand-Automata/FGA/releases).

### Plans
There is no clear roadmap on this since I'm only developing this on the weekend, but I do have some nice-to-have updates that the list will be updated regularly if something is released:
- CI to release a new tag and .apk
- Proper theming between light and dark
- Multi-server support
