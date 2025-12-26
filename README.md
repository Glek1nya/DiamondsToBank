# DiamondToBank — Minecraft Plugin for Paper 1.21.1

DiamondToBank is a simple plugin for Minecraft Paper 1.21.1 that allows players to deposit diamonds(or another item) to their virtual account (using Vault) and withdraw diamonds from their account.

## Features

- `/deposit` — deposit diamonds held in hand to your account (with configurable commission).
- `/take <number>` — withdraw a specified amount of diamonds from your account.
- `/diamondtobankreload` — reload configuration and messages without restarting the server.

## Installation

1. Install on your server:
   - Paper 1.21.1
   - Vault
   - EssentialsX (or another Vault-compatible plugin)
2. Place the plugin JAR file in the server's `plugins` folder.
3. Restart the server.

## Configuration

The plugin supports customization via `config.yml`:
- `deposit_item` — item for depositing to account.
- `deposit_commission` — commission on deposits (0.1 = 10%).
- `take_item` — item for withdrawal from account.

The `messages.yml` file allows you to customize all plugin messages, including colors and formats using MiniMessage.

## Commands

- `/deposit` — deposit to account.
- `/take <number>` — withdraw items from account.
- `/diamondtobankreload` — reload config and messages.

## Requirements

- Java 17+
- Paper 1.21
- Vault
- EssentialsX

42
