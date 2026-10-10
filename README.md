# ChatProtect

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-1.8--26.3-orange.svg)](https://www.spigotmc.org/)

ChatProtect is a robust and modern moderation plugin that automatically moderates your chat, preventing spam, offensive language, and advertisements. Are you tired of players advertising their servers? Annoyed by a flooded chat?
ChatProtect is the solution!

---

## Features

### AI-powered Chat Moderation:
When enabled, ChatProtect Cloud uses OpenAI's Moderation API to score messages for toxicity and offensive context.

### Massive Blacklist Coverage:
ChatProtect comes with a pregenerated list of blacklisted words. This filter will check every single message on your server. ChatProtect can even detect and block attempts to bypass the chat filter.

### Advertisement & IP Blocking:
Automatically and instantly block players trying to advertise other servers. We block IP addresses, domain names, and suspicious links to keep your players safe.

### Advanced Spam & Flood Prevention:
Keep your chat clean and readable. ChatProtect automatically filters out:
- Repetitive Messaging (Chat flooding)
- Excessive Caps (Screaming/Uppercase spam)
- Random Letter Spam

### Discord Alerts:
Use Discord Webhooks to automatically send alerts to your private staff channel the moment an message is flagged by ChatProtect.

### Spam bot detection:
ChatProtect can identify spam bots, which are often found in hack clients such as Liquidbounce.

![Example](https://i.imgur.com/gConfV5.png)

---

## Installation

1. Download the plugin: Get the latest version of ChatProtect from SpigotMC.
2. Install: Place the ChatProtect.jar file into your server's plugins directory.
3. Restart Server: Restart your server to enable the plugin.
4. Configure: Customize the plugin by editing the config.yml file in the plugins/ChatProtect directory. Replace the default kick and ban commands with your own punishment commands (e.g. replace kicking with a temporary mute).
5. Testing: When testing the plugin make sure you don't have operator / chatprotect.bypass permissions. Otherwise you will bypass the checks.

---

## Commands and Permissions

- `/chatprotect alerts` (`chatprotect.alerts`)
- `/chatprotect reload` (`chatprotect.reload`)
- `/chatprotect clearchat` (`chatprotect.clearchat`)
- `chatprotect.command` (See the command)
- `chatprotect.bypass` (Bypass the detection)

---

## Metrics

![bStats Metrics](https://bstats.org/signatures/bukkit/ChatProtect.svg)
