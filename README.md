# PapesUselessAddons
A very small add-on for meteor client that adds a few things that aren't in other add-ons.

## Module
- **Remove Block Hitboxes:** A module that lets you interact through blocks. Blocks selected will not have a wireframe when looked at, and any interactions afect what is behind the block instead. Useful for interacting through portals.

## New Settings
- **Hide Repeats:** A setting for BetterChat's antispam that hides messages if they have been repeated a certain number of times.
- **Notify:** A new setting category in BetterChat. Plays a customizable sound when a chat message matches your regex. Sounds do not play if a message is  hidden or filtered, preventing ghost notifications. Matches can also be visually highlighted.
- **Xp Orb Labels:** A setting in Nametags. Displays how much experience is inside of an experience orb.

## HUD Element
- **Score & Score preset:** The players score (usually seen on the death screen) can be added to the HUD with the new text element score(), or with the Score text preset.

## Command
- **Score Command:** The score command tells the player what their current score (usually seen on the death screen) is.

## Technical Change
- **Removal of spammy error messages:** When the user has broken settings, potentially due to having lots of players friended, meteor normally spams the logs with error messages on startup. This add-on removes those messages to keep logs cleaner.
