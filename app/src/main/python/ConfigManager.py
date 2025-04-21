import json
import os

def load_config(config_path: str) -> dict:
    if os.path.exists(config_path):
        with open(config_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {}

def save_config(config_path: str, data: dict):
    with open(config_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

def get_last_downloaded_filename(config: dict) -> str | None:
    return config.get("last_downloaded")

def update_last_downloaded_filename(config_path: str, filename: str):
    config = load_config(config_path)
    config["last_downloaded"] = filename
    save_config(config_path, config)
