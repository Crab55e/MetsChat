import requests
import json

# === 設定 ===
BOT_TOKEN = "TOKEN HERE"  # Botのトークン
APP_ID = "APP ID HERE"  # DiscordのアプリケーションID

command = {
    "name": "playerlist",
    "description": "サーバーでオンラインのプレイヤー一覧を表示します"
}

url = f"https://discord.com/api/v10/applications/{APP_ID}/commands"

headers = {
    "Authorization": f"Bot {BOT_TOKEN}",
    "Content-Type": "application/json"
}

res = requests.post(url, headers=headers, data=json.dumps(command))

print(f"ステータス: {res.status_code} ")
print(res.json())