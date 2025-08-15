# MetsChat
[Met's Server](https://mets-svr.com/) で使用予定のチャット系プラグイン

## Requirements
- 4drian3d / MCKotlin Velocity 1.5.1-k2.0.21
- Papermc / Velocity 3.4.x
- Java 17+

## Features
- 公開後に記述

## Setup
導入のために必要な最低限のセットアップ手順

### [`config.toml`](https://github.com/Crab55e/MetsChat/blob/main/src/main/resources/config.toml)
### 1. discord.bot-token の設定
導入先のdiscordサーバーにbotを用意します
```toml
[discord.bot-token]
type = "system-environ" # "system-environ" or "raw-string"
value = "DiscordSRVBotToken"
```
標準ではこのように指定されていますが、typeをraw-stringに書き換えることでvalueの値に直にtokenを書き込むことが出来ます  
用意したbotのtokenを書き込んで、次の手順に移ります

### 2. discord.webhook-url の設定
導入先のサーバーに、チャット共有用のwebhookを作成します
```toml
[discord.webhook-url]
type = "system-environ"
value = "MetsChatWebhookUrl"
```
標準ではこのように指定されていますが、こちらもtypeとvalueを変更できます  
用意したwebhookのurlをvalueに書き込みます

### 3. default-channel-id の設定
minecraftからの各種メッセージ用にチャンネルを作成します
```toml
[discord.general]
default-channel-id = "1362971564836130838"
```
標準ではこのように指定されています。`default-channel-id`の値を用意したチャンネルのIDに書き換えます  
機能ごとに個別に設定することもできるため、必要に応じて[`config.toml`](https://github.com/Crab55e/MetsChat/blob/main/src/main/resources/config.toml)のchannel-idを変更してください

### 4. コマンドの登録

[submit-commands.py](https://github.com/Crab55e/MetsChat/blob/main/submit-commands.py)  
を使用して、必要なコマンドをbotに登録します  
ファイル内に含まれる"設定"セクションのtokenとapp_idを適切に設定してください。