SimpleLocator is a mod that allows you to visualize player locations. These locations are visualized by small panels on the screen.

---

###Location panels

Location panels are small panels that appear at the last known location of a Minecraft user. There are typically 3 parts to a panel.

Part 1: Minecraft username + distance away "shadowjay1 (50m)"  
Part 2: Type of location (indicated by a character) "~"

- Snitch (~)
- /ppbroadcast (o)
- Last seen on radar (-)
- Downloaded SimpleLocator user location (|)
- Downloaded radar location (v)

Part 3: How long ago the location was discovered

The location panels are configurable in the SimpleLocator menu, by default 'L'.

---

###Groups

Groups are a core mechanic to SimpleLocator. Groups are created in the configuration menu and players can be added/removed from the group there as well.

Groups allow you to:

- Set custom panel settings for groups
- See colored circles around users in groups
- Trust groups to send/receive locations to/from all LocatorNet users in the group
- See login/logout messages for players in groups

Groups can also be auto-updated from a URL if the URL points to a raw text file with one username per line. Example: http://pastebin.com/raw.php?i=82BBpBGQ

---

###Miscellaneous

SimpleLocator allows you to see the last known locations of all your offline accounts.

---

###Download

https://mega.nz/#!bVkzkZTQ!3_C81O_PAlH0NaIMY_jQdtWrJijnh-bKYwKjQ4WTVpc

---

###Compilation instructions

- Download Forge and set-up the Forge development environment
- Clone this repository in the src/ folder (```git clone https://github.com/shadowjay1/SimpleLocator/ src/```)
- Run ```gradlew build``` and grab the final jar from the build/libs/ folder
