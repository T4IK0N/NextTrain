import json
import os

def load_config(filepath):
    if not os.path.exists(filepath):
        return {}

    try:
        with open(filepath, "r", encoding="utf-8") as f:
            return json.load(f)
    except (json.JSONDecodeError, ValueError):
        print(f"Config file {filepath} is invalid or empty.")
        return {}

def save_config(config_path: str, data: dict):
    with open(config_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

def get_last_downloaded_filename(config: dict) -> str | None:
    return config.get("last_downloaded")

def update_last_downloaded_filename(config_path: str, filename: str):
    ensure_config_file_exists(config_path)
    config = load_config(config_path)
    if not isinstance(config, dict):
        config = {}
    config["last_downloaded"] = filename
    save_config(config_path, config)

def ensure_config_file_exists(config_path):
    if not os.path.exists(config_path):
        with open(config_path, 'w', encoding='utf-8') as f:
            json.dump({}, f)